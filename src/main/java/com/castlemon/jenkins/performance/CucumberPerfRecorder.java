package com.castlemon.jenkins.performance;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.RunMap;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.slaves.SlaveComputer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.castlemon.jenkins.performance.domain.reporting.ProjectRun;
import com.castlemon.jenkins.performance.processor.ReportGenerator;
import com.castlemon.jenkins.performance.util.CucumberPerfUtils;

@SuppressWarnings("unchecked")
public class CucumberPerfRecorder extends Recorder {

	public final String jsonReportDirectory;
	public final String pluginUrlPath;

	private ReportGenerator generator;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CucumberPerfRecorder(String jsonReportDirectory, String pluginUrlPath) {
		this.jsonReportDirectory = jsonReportDirectory;
		this.pluginUrlPath = pluginUrlPath;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		listener.getLogger()
				.println(
						"[CucumberPerfRecorder] Starting Cucumber Performance Report generation...");
		generator = new ReportGenerator();
		File targetBuildDirectory = new File(build.getRootDir(),
				"cucumber-perf-reports");
		if (!targetBuildDirectory.exists()) {
			targetBuildDirectory.mkdirs();
		}
		String buildNumber = Integer.toString(build.getNumber());
		String buildProject = build.getProject().getName();
		listener.getLogger().println(
				"[CucumberPerfRecorder] Reporting on performance for "
						+ buildProject + " #" + buildNumber);
		generateBuildReport(build, listener, targetBuildDirectory, buildNumber,
				buildProject);
		generateProjectReport(build, listener, targetBuildDirectory,
				buildNumber, buildProject);
		build.addAction(new CucumberBuildAction(build));
		return true;
	}

	private void generateProjectReport(AbstractBuild<?, ?> build,
			BuildListener listener, File targetBuildDirectory,
			String buildNumber, String buildProject) throws IOException,
			InterruptedException {
		List<ProjectRun> projectRuns = new ArrayList<ProjectRun>();
		RunMap<?> runMap = build.getProject()._getRuns();
		Iterator<?> iterator = runMap.iterator();
		while (iterator.hasNext()) {
			Run<?, ?> thisBuild = (Run<?, ?>) iterator.next();
			ProjectRun projectRun = new ProjectRun();
			projectRun.setRunDate(thisBuild.getTime());
			projectRun.setBuildNumber(thisBuild.getNumber());
			File workspaceJsonReportDirectory = thisBuild.getArtifactsDir()
					.getParentFile();
			projectRun.setScenarios(CucumberPerfUtils.getData(CucumberPerfUtils
					.findJsonFiles(workspaceJsonReportDirectory,
							"**/cucumber-perf.json"),
					workspaceJsonReportDirectory));
			projectRuns.add(projectRun);
		}
		generator.generateProjectReports(projectRuns, listener,
				targetBuildDirectory, buildProject, buildNumber, pluginUrlPath);
	}

	private void generateBuildReport(AbstractBuild<?, ?> build,
			BuildListener listener, File targetBuildDirectory,
			String buildNumber, String buildProject) throws IOException,
			InterruptedException {
		File workspaceJsonReportDirectory = new File(build.getWorkspace()
				.toURI().getPath());
		if (StringUtils.isNotBlank(jsonReportDirectory)) {
			workspaceJsonReportDirectory = new File(build.getWorkspace()
					.toURI().getPath(), jsonReportDirectory);
		}

		// if we are on a slave
		if (Computer.currentComputer() instanceof SlaveComputer) {
			listener.getLogger().println(
					"[CucumberPerfRecorder] detected slave build ");
			FilePath projectWorkspaceOnSlave = build.getProject()
					.getSomeWorkspace();
			FilePath masterJsonReportDirectory = new FilePath(
					targetBuildDirectory);
			projectWorkspaceOnSlave.copyRecursiveTo("**/cucumber.json", "",
					masterJsonReportDirectory);
		} else {
			// if we are on the master
			listener.getLogger().println(
					"[CucumberPerfRecorder] detected master build ");
			String[] files = CucumberPerfUtils.findJsonFiles(
					workspaceJsonReportDirectory, "cucumber.json");

			if (files.length != 0) {
				for (String file : files) {
					FileUtils.copyFile(
							new File(workspaceJsonReportDirectory.getPath()
									+ "/" + file), new File(
									targetBuildDirectory, file));
				}
			} else {
				listener.getLogger().println(
						"[CucumberPerfRecorder] there were no json results found in: "
								+ workspaceJsonReportDirectory);
			}
		}
		// rename the json file in the performance report directory
		String[] oldJsonReportFiles = CucumberPerfUtils.findJsonFiles(
				targetBuildDirectory, "*.json");
		for (String fileName : oldJsonReportFiles) {
			File file = new File(targetBuildDirectory, fileName);
			String newFileName = "cucumber-perf.json";
			File newFile = new File(targetBuildDirectory, newFileName);
			try {
				FileUtils.moveFile(file, newFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// generate the reports from the targetBuildDirectory
		String[] jsonReportFiles = CucumberPerfUtils.findJsonFiles(
				targetBuildDirectory, "cucumber-perf.json");
		if (jsonReportFiles.length != 0) {
			generator.generateBuildReports(listener, jsonReportFiles,
					targetBuildDirectory, buildProject, buildNumber,
					pluginUrlPath, build.getRootDir());
		}
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Generate Cucumber performance reports";
		}

	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new CucumberProjectAction(project);
	}
}
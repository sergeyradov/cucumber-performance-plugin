package com.castlemon.jenkins.performance;

import com.castlemon.jenkins.performance.domain.reporting.ProjectRun;
import com.castlemon.jenkins.performance.reporting.ReportBuilder;
import com.castlemon.jenkins.performance.util.CucumberPerfUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.slaves.SlaveComputer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CucumberPerfRecorder extends Recorder {

    public final String jsonReportDirectory;
    public final int countOfSortedSummaries;
    private ReportBuilder reportBuilder;
    private File targetBuildDirectory;

    // Fields in config.jelly must match the parameter names in the
    // "DataBoundConstructor"
    @DataBoundConstructor
    public CucumberPerfRecorder(String jsonReportDirectory,
            int countOfSortedSummaries) {
        this.jsonReportDirectory = jsonReportDirectory;
        if (countOfSortedSummaries == 0) {
            this.countOfSortedSummaries = 20;
        } else {
            this.countOfSortedSummaries = countOfSortedSummaries;
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {
        listener.getLogger()
                .println(
                        "[CucumberPerfRecorder] Starting Cucumber Performance Report generation...");
        reportBuilder = new ReportBuilder();
        targetBuildDirectory = new File(build.getRootDir(),
                "cucumber-perf-reports");
        if (!targetBuildDirectory.exists()) {
            targetBuildDirectory.mkdirs();
        }
        String buildProjectName = build.getProject().getName();
        listener.getLogger().println(
                "[CucumberPerfRecorder] Reporting on performance for "
                        + buildProjectName + " #" + Integer.toString(build.getNumber()));
        gatherJsonResultFiles(build, listener, targetBuildDirectory);
        return generateProjectReport(build, listener, targetBuildDirectory,
                buildProjectName);
    }

    private boolean generateProjectReport(AbstractBuild<?, ?> build,
            BuildListener listener, File targetBuildDirectory,
            String buildProjectName) throws IOException,
            InterruptedException {
        List<ProjectRun> projectRuns = new ArrayList<ProjectRun>();
        RunMap<?> runMap = build.getProject()._getRuns();
        for (Run<?, ?> run : runMap) {
            listener.getLogger().println("processing run " + run.getNumber());
            ProjectRun projectRun = new ProjectRun();
            projectRun.setRunDate(run.getTime());
            projectRun.setBuildNumber(run.getNumber());
            File workspaceJsonReportDirectory = run.getArtifactsDir()
                    .getParentFile();
            listener.getLogger().println("directory found " + workspaceJsonReportDirectory.getAbsolutePath());
            projectRun.setFeatures(CucumberPerfUtils.getData(CucumberPerfUtils
                            .findJsonFiles(workspaceJsonReportDirectory,
                                    "**/cucumber-perf*.json"),
                    workspaceJsonReportDirectory));
            listener.getLogger().println("found files");
            // only report on runs that have been analysed
            if (!projectRun.getFeatures().isEmpty()) {
                projectRuns.add(projectRun);
                listener.getLogger().println("adding run " + run.getNumber());
            }

        }
        listener.getLogger().println(
                "[CucumberPerfRecorder] running project reports on "
                        + projectRuns.size() + " builds");
        boolean success = reportBuilder.generateProjectReports(projectRuns,
                targetBuildDirectory, buildProjectName);
        listener.getLogger().println(
                "[CucumberPerfRecorder] project report generation complete");
        return success;
    }

    private void gatherJsonResultFiles(AbstractBuild<?, ?> build,
            BuildListener listener, File targetBuildDirectory)
            throws IOException, InterruptedException {
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
            listener.getLogger().println(
                    "looking in "
                            + workspaceJsonReportDirectory.getAbsolutePath());
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
        int i = 0;
        for (String fileName : oldJsonReportFiles) {
            File file = new File(targetBuildDirectory, fileName);
            String newFileName = "cucumber-perf" + i + ".json";
            File newFile = new File(targetBuildDirectory, newFileName);
            try {
                FileUtils.moveFile(file, newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new CucumberProjectAction(project, countOfSortedSummaries);
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

}

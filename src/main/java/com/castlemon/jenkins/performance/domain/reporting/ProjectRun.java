package com.castlemon.jenkins.performance.domain.reporting;

import java.util.Date;
import java.util.List;

import com.castlemon.jenkins.performance.domain.Scenario;

public class ProjectRun {

	private String projectName;

	private int buildNumber;

	private Date runDate;

	private List<Scenario> scenarios;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(int buildNumber) {
		this.buildNumber = buildNumber;
	}

}
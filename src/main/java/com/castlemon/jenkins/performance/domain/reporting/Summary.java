package com.castlemon.jenkins.performance.domain.reporting;

import hudson.model.AbstractProject;

import java.util.List;

import org.apache.commons.lang.RandomStringUtils;

import com.castlemon.jenkins.performance.domain.enums.SummaryType;
import com.castlemon.jenkins.performance.util.CucumberPerfUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("summary")
public class Summary {

	private static final int nanosInAMilli = 1000000;

	private String id;

	private String name;

	@XStreamAlias("summarytype")
	private SummaryType summaryType;

	private List<PerformanceEntry> entries;

	@XStreamAlias("shortestduration")
	private long shortestDuration = Long.MAX_VALUE;

	@XStreamAlias("longestduration")
	private long longestDuration;

	@XStreamAlias("averageduration")
	private long averageDuration;

	@XStreamAlias("totalbuilds")
	private int totalBuilds;

	@XStreamAlias("passedbuilds")
	private int passedBuilds;

	@XStreamAlias("failedbuilds")
	private int failedBuilds;

	@XStreamAlias("reportedbuilds")
	private int reportedBuilds;

	@XStreamAlias("passedsteps")
	private int passedSteps;

	@XStreamAlias("failedsteps")
	private int failedSteps;

	@XStreamAlias("skippedsteps")
	private int skippedSteps;

	@XStreamAlias("seniorid")
	private String seniorId;

	@XStreamAlias("seniorname")
	private String seniorName;

	private int order;

	@XStreamAlias("numberofsubitems")
	private int numberOfSubItems;

	@XStreamAlias("pagelink")
	private final String pageLink;

	// only used for steps
	private String keyword;

	private List<List<String>> rows;

	@XStreamOmitField
	private List<Summary> subSummaries;

	/*
	 * this field is only used for display and should not be used in other
	 * circumstances
	 */
	@XStreamOmitField
	private AbstractProject<?, ?> project;

	/*
	 * this field is only used for display and should not be used in other
	 * circumstances
	 */
	@XStreamOmitField
	private String urlName;

	public String getUrlName() {
		return urlName;
	}

	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

	public AbstractProject<?, ?> getProject() {
		return project;
	}

	public void setProject(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public Summary() {
		this.pageLink = RandomStringUtils.randomAlphabetic(5);
	}

	public String getPageLink() {
		return pageLink;
	}

	public boolean hasRows() {
		return (rows != null);
	}

	public long calculateAverageDuration() {
		long count = 0l;
		long duration = 0l;
		for (PerformanceEntry entry : entries) {
			if (entry.isPassed()) {
				duration += entry.getElapsedTime();
				count++;
			}
		}
		if (count > 0) {
			averageDuration = duration / count;
			return averageDuration;
		}
		return 0l;
	}

	public String getFormattedAverageDuration() {
		calculateAverageDuration();
		return CucumberPerfUtils.formatDuration(averageDuration);
	}

	public String getFormattedShortestDuration() {
		if (shortestDuration == Long.MAX_VALUE) {
			// field not updated, return 0
			return CucumberPerfUtils.formatDuration(0l);
		}
		return CucumberPerfUtils.formatDuration(shortestDuration);
	}

	public String getFormattedLongestDuration() {
		return CucumberPerfUtils.formatDuration(longestDuration);
	}

	public void addToPassedSteps(int passedSteps) {
		this.passedSteps += passedSteps;
	}

	public void addToSkippedSteps(int skippedSteps) {
		this.skippedSteps += skippedSteps;
	}

	public void addToFailedSteps(long failedSteps) {
		this.failedSteps += failedSteps;
	}

	public void incrementTotalBuilds() {
		this.totalBuilds++;
	}

	public void incrementPassedBuilds() {
		this.passedBuilds++;
	}

	public void incrementFailedBuilds() {
		this.failedBuilds++;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PerformanceEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<PerformanceEntry> entries) {
		this.entries = entries;
	}

	public long getShortestDuration() {
		/*
		 * if (this.shortestDuration == Long.MAX_VALUE) { // field not updated,
		 * return 0 return 0l; }
		 */
		return this.shortestDuration;
	}

	public void setShortestDuration(long shortestDuration) {
		this.shortestDuration = shortestDuration;
	}

	public long getLongestDuration() {
		return longestDuration;
	}

	public void setLongestDuration(long longestDuration) {
		this.longestDuration = longestDuration;
	}

	public int getTotalBuilds() {
		return totalBuilds;
	}

	public void setTotalBuilds(int totalBuilds) {
		this.totalBuilds = totalBuilds;
	}

	public int getPassedBuilds() {
		return passedBuilds;
	}

	public void setPassedBuilds(int passedBuilds) {
		this.passedBuilds = passedBuilds;
	}

	public int getFailedBuilds() {
		return failedBuilds;
	}

	public void setFailedBuilds(int failedBuilds) {
		this.failedBuilds = failedBuilds;
	}

	public int getReportedBuilds() {
		return reportedBuilds;
	}

	public void setReportedBuilds(int reportedBuilds) {
		this.reportedBuilds = reportedBuilds;
	}

	public int getPassedSteps() {
		return passedSteps;
	}

	public void setPassedSteps(int passedSteps) {
		this.passedSteps = passedSteps;
	}

	public int getFailedSteps() {
		return failedSteps;
	}

	public void setFailedSteps(int failedSteps) {
		this.failedSteps = failedSteps;
	}

	public int getSkippedSteps() {
		return skippedSteps;
	}

	public void setSkippedSteps(int skippedSteps) {
		this.skippedSteps = skippedSteps;
	}

	public String getSeniorId() {
		return seniorId;
	}

	public void setSeniorId(String seniorId) {
		this.seniorId = seniorId;
	}

	public String getSeniorName() {
		return seniorName;
	}

	public void setSeniorName(String seniorName) {
		this.seniorName = seniorName;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getNumberOfSubItems() {
		return numberOfSubItems;
	}

	public void setNumberOfSubItems(int numberOfSubItems) {
		this.numberOfSubItems = numberOfSubItems;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	public void setRows(List<List<String>> rows) {
		this.rows = rows;
	}

	public SummaryType getSummaryType() {
		return summaryType;
	}

	public void setSummaryType(SummaryType summaryType) {
		this.summaryType = summaryType;
	}

	public String getGraphData() {
		StringBuilder output = new StringBuilder();
		output.append("[");
		int i = 1;
		for (PerformanceEntry run : this.entries) {
			if (run.isPassed()) {
				output.append("["
						+ run.getBuildNumber()
						+ ", "
						+ CucumberPerfUtils.getDurationInSeconds(run
								.getElapsedTime() / nanosInAMilli) + "]");
				if (i < this.entries.size()) {
					output.append(",");
				}
			}
			i++;
		}
		output.append("]");
		return output.toString();
	}

	public String getAverageData() {
		long totalDuration = 0l;
		long executionCount = 0l;
		StringBuilder output = new StringBuilder();
		for (PerformanceEntry run : this.entries) {
			if (run.isPassed()) {
				totalDuration += run.getElapsedTime();
				executionCount++;
			}
		}
		long average = 0l;
		if (executionCount > 0) {
			average = totalDuration / executionCount;
		}
		output.append("[");
		int i = 1;
		for (PerformanceEntry run : this.entries) {
			if (run.isPassed()) {
				output.append("["
						+ run.getBuildNumber()
						+ ", "
						+ CucumberPerfUtils.getDurationInSeconds(average
								/ nanosInAMilli) + "]");
				if (i < this.entries.size()) {
					output.append(",");
				}
			}
			i++;
		}
		output.append("]");
		return output.toString();
	}

	public List<Summary> getSubSummaries() {
		return subSummaries;
	}

	public void setSubSummaries(List<Summary> subSummaries) {
		this.subSummaries = subSummaries;
	}

}

package hudson.plugins.global_build_stats.model;

import java.io.Serializable;

public class BuildStatConfiguration implements Serializable {

	private String buildStatTitle;
	private int buildStatWidth, buildStatHeight;
	private int historicLength;
	private HistoricScale historicScale;
	private String jobFilter;
	private short shownBuildResults;
	
	public BuildStatConfiguration(String _buildStatTitle, int _buildStatWidth, int _buildStatHeight, 
			int _historicLength, HistoricScale _historicScale, String _jobFilter, 
			boolean successShown, boolean failuresShown, boolean unstablesShown, 
			boolean abortedShown, boolean notBuildsShown){
		
		this.buildStatTitle = _buildStatTitle;
		this.buildStatHeight = _buildStatHeight;
		this.buildStatWidth = _buildStatWidth;
		this.historicLength = _historicLength;
		this.historicScale = _historicScale;
		this.jobFilter = _jobFilter;
		
		this.shownBuildResults = 0;
		this.shownBuildResults |= successShown?BuildResult.SUCCESS.code:0;
		this.shownBuildResults |= failuresShown?BuildResult.FAILURE.code:0;
		this.shownBuildResults |= unstablesShown?BuildResult.UNSTABLE.code:0;
		this.shownBuildResults |= abortedShown?BuildResult.ABORTED.code:0;
		this.shownBuildResults |= notBuildsShown?BuildResult.NOT_BUILD.code:0;
	}

	public boolean isSuccessShown(){
		return (shownBuildResults & BuildResult.SUCCESS.code) != 0;
	}
	
	public boolean isFailuresShown(){
		return (shownBuildResults & BuildResult.FAILURE.code) != 0;
	}
	
	public boolean isUnstablesShown(){
		return (shownBuildResults & BuildResult.UNSTABLE.code) != 0;
	}
	
	public boolean isAbortedShown(){
		return (shownBuildResults & BuildResult.ABORTED.code) != 0;
	}
	
	public boolean isNotBuildShown(){
		return (shownBuildResults & BuildResult.NOT_BUILD.code) != 0;
	}
	
	public String getBuildStatTitle() {
		return buildStatTitle;
	}

	public int getHistoricLength() {
		return historicLength;
	}

	public HistoricScale getHistoricScale() {
		return historicScale;
	}

	public short getShownBuildResults() {
		return shownBuildResults;
	}

	public int getBuildStatWidth() {
		return buildStatWidth;
	}

	public int getBuildStatHeight() {
		return buildStatHeight;
	}

	public String getJobFilter() {
		return jobFilter;
	}
}

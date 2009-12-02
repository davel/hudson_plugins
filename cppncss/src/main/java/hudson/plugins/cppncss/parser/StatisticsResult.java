package hudson.plugins.cppncss.parser;

import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class StatisticsResult  implements Serializable {

	//cppncss function results
	private Collection<Statistic> functionResults = Collections.emptySet();
	//cppncss file results
	private Collection<Statistic> fileResults = Collections.emptySet();
	
	private AbstractBuild<?, ?> owner;

	public void setFunctionResults(Collection<Statistic> functionResults) {
		this.functionResults = functionResults;
	}

	public Collection<Statistic> getFunctionResults() {
		return functionResults;
	}

	public void setFileResults(Collection<Statistic> fileResults) {
		this.fileResults = fileResults;
	}

	public Collection<Statistic> getFileResults() {
		return fileResults;
	}

	public static StatisticsResult merge(StatisticsResult results,
			StatisticsResult result) {
		StatisticsResult mergeResult = new StatisticsResult();
		mergeResult.setFileResults(Statistic.merge(results.getFileResults(), result.getFileResults()));
		mergeResult.setFunctionResults(Statistic.merge(results.getFunctionResults(), result.getFunctionResults()));
		return mergeResult;
	}

	public static StatisticsTotalResult total(StatisticsResult results) {
		StatisticsTotalResult totalResult = new StatisticsTotalResult();
		totalResult.setFunctionTotal( Statistic.total(results.getFunctionResults()));
		totalResult.setFileTotal( Statistic.total(results.getFileResults()));
		return totalResult;
	}

	public void clear() {
		 functionResults.clear();
		 fileResults.clear();
	}

	public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    public void setOwner(AbstractBuild<?, ?> owner) {
        this.owner = owner;
        for (Statistic result : functionResults) {
			result.setOwner(owner);
		}
        
        for (Statistic result : fileResults) {
			result.setOwner(owner);
		}
    }

	public void set(StatisticsResult that) {
		this.fileResults = that.fileResults;
		this.functionResults = that.functionResults;
	}
}

package hudson.plugins.cppncss.parser;

import java.io.Serializable;

public class StatisticsTotalResult  implements Serializable {
	private Statistic functionTotal;
	private Statistic fileTotal;
	
	public void setFunctionTotal(Statistic functionTotal) {
		this.functionTotal = functionTotal;
	}
	public Statistic getFunctionTotal() {
		return functionTotal;
	}
	public void setFileTotal(Statistic fileTotal) {
		this.fileTotal = fileTotal;
	}
	public Statistic getFileTotal() {
		return fileTotal;
	}
	
	public String toSummary(StatisticsTotalResult statisticsTotalResult) {
		return fileTotal.toSummary(statisticsTotalResult.fileTotal);
	}
	public String toSummary() {
		return fileTotal.toSummary();
	}
	
	public void set(StatisticsTotalResult that) {
		this.functionTotal = that.functionTotal;
		this.fileTotal = that.fileTotal;
		
	}
}

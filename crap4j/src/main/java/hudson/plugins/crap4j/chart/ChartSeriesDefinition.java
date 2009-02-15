package hudson.plugins.crap4j.chart;

import hudson.plugins.crap4j.model.ProjectCrapBean;

public abstract class ChartSeriesDefinition {
	
	private final String denotation;
	private final String axisTitle;

	public ChartSeriesDefinition(String denotation,
			String axisTitle) {
		super();
		this.denotation = denotation;
		this.axisTitle = axisTitle;
	}
	
	public String getDenotation() {
		return this.denotation;
	}
	
	public String getAxisTitle() {
		return this.axisTitle;
	}
	
	public abstract Number extractNumberFrom(ProjectCrapBean crap);
	
	public AbstractChartMaker getChartMaker() {
		return new LineChartMaker();
		//return new MultipleLinesChartMaker();
	}
}

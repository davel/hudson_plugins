package hudson.plugins.crap4j;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.crap4j.calculation.HealthBuilder;
import hudson.plugins.crap4j.chart.AbstractChartMaker;
import hudson.plugins.crap4j.chart.AreaChartMaker;
import hudson.plugins.crap4j.chart.ChartSeriesDefinition;
import hudson.plugins.crap4j.chart.CrapDataSet;
import hudson.plugins.crap4j.model.ProjectCrapBean;
import hudson.util.ChartUtil;

import java.io.IOException;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class Crap4JBuildAction implements StaplerProxy, HealthReportingAction {
	
	private static final long serialVersionUID = 8586323795728749743L;
	private final CrapBuildResult crapResult;
	private final AbstractBuild<?, ?> build;
	private transient HealthBuilder healthBuilder;
	
	public Crap4JBuildAction(AbstractBuild<?, ?> owner,
			CrapBuildResult crapResult,
			HealthBuilder healthBuilder) {
		super();
		this.build = owner;
		this.crapResult = crapResult;
		this.healthBuilder = healthBuilder;
	}
	
	//@Override
	public String getDisplayName() {
		return "Crap";
	}
	
	public ProjectCrapBean getCrap() {
		return getTarget().getResultData();
	}
	
	//@Override
	public String getIconFileName() {
		return "/plugin/crap4j/icons/crap-32x32.gif";
	}
	
	//@Override
	public String getUrlName() {
		return "crapResult";
	}
	
	public CrapBuildResult getTarget() {
		this.crapResult.setOwner(this.build);
		return this.crapResult;
	}
	
	public CrapBuildResult getResult() {
		return getTarget();
	}
	
	public boolean hasPreviousCrap() {
		return (null != getTarget().getPrevious());
	}
	
	//@Override
	public HealthReport getBuildHealth() {
		if (null == this.healthBuilder) {
			this.healthBuilder = Crap4JPublisher.DESCRIPTOR.getHealthBuilder();
		}
		return (this.healthBuilder.getHealthReportFor(getCrap()));
	}
	
    public final void doGraphMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (null != ChartUtil.awtProblemCause) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        CrapDataSet dataset = new CrapDataSet(getResult());
        ChartSeriesDefinition definition = getChartDefinitionFor(request);
        ChartUtil.generateClickableMap(request, response,
        		definition.getChartMaker().createChart(
        				dataset.buildCategoryDataSet(definition),
        				definition.getAxisTitle()),
        		500, 200);
    }
    
    public final void doGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (null != ChartUtil.awtProblemCause) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        CrapDataSet dataset = new CrapDataSet(getResult());
        ChartSeriesDefinition definition = getChartDefinitionFor(request);
        ChartUtil.generateGraph(request, response,
        		definition.getChartMaker().createChart(
        				dataset.buildCategoryDataSet(definition),
        				definition.getAxisTitle()),
        		500, 200);
    }
    
    private String getRelPath(StaplerRequest req) {
        String relPath = req.getParameter("rel");
        if (null == relPath) {
        	return "";
        }
        return relPath;
    }
    
    private ChartSeriesDefinition getChartDefinitionFor(final StaplerRequest request) {
    	String dataType = request.getParameter("data");
    	if ("crapMethodCount".equals(dataType)) {
    		return new ChartSeriesDefinition("crap methods", "method count") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getCrapMethodCount();
    			}
    			@Override
    			public AbstractChartMaker getChartMaker() {
    				return new AreaChartMaker(getRelPath(request));
    			}
    		};
    	}
    	if ("crapLoad".equals(dataType)) {
    		return new ChartSeriesDefinition("crap load", "crap load") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getCrapLoad();
    			}
    		};
    	}
    	if ("crap".equals(dataType)) {
    		return new ChartSeriesDefinition("crap", "crap amount") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getTotalCrap();
    			}
    		};
    	}
    	/* default */
    	return new ChartSeriesDefinition("crappyness", "crap method percentage") {
    		@Override
    		public Number extractNumberFrom(ProjectCrapBean crap) {
    			return crap.getCrapMethodPercent();
    		}
    	};
    }
}

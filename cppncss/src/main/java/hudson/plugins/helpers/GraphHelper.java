package hudson.plugins.helpers;

import hudson.model.AbstractBuild;
import hudson.plugins.cppncss.AbstractBuildReport;
import hudson.plugins.cppncss.parser.Statistic;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.DefaultCategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 09-Jan-2008 21:30:15
 */
public class GraphHelper {
	
	public interface CategoryDatasetBuilder {
		CategoryDataset buildData();
	}
	
	public interface DataCollector {
		
		long getCollectedNumber(AbstractBuildReport<?> action);
		String getTitle();
	}
	
    /**
     * Do not instantiate GraphHelper.
     */
    private GraphHelper() {
    }

    /**
     * Getter for property 'graphUnsupported'.
     *
     * @return Value for property 'graphUnsupported'.
     */
    public static boolean isGraphUnsupported() {
        return ChartUtil.awtProblem;
    }

    public static void redirectWhenGraphUnsupported(StaplerResponse rsp, StaplerRequest req) throws IOException {
        // not available. send out error message
        rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
    }

    public static JFreeChart buildChart(final AbstractBuild<?, ?> build, final Integer functionCcnViolationThreshold, final Integer functionNcssViolationThreshold) {
    	
        final JFreeChart chart = ChartFactory.createStackedAreaChart(
                null,                     // chart title
                null,                     // unused
                "Total Files",            // range axis label
                buildDataset(build , new DataCollector() {
					
					public String getTitle() {
						return "Files";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						return action.getResults().getFileResults().size();
					}
				}),      // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                true                      // urls
        );

        chart.setBackgroundPaint(Color.white);


        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        CategoryItemRenderer firstRender= new DefaultCategoryItemRenderer();
        plot.setRenderer(firstRender); 

        int index = 1;
		//Second
        build_category(build, chart, index, Color.BLUE, "Non Commenting Source Statements", new CategoryDatasetBuilder() {
			public CategoryDataset buildData() {
				return buildDataset(build, new DataCollector() {
					
					public String getTitle() {
						return "NCSS";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						return action.getTotals().getFileTotal().getNcss();
					}
				});
			}
        });
        
        index ++;
      //Third
        build_category(build, chart, index, Color.GREEN, "McCabe's Cyclomatic Number", new CategoryDatasetBuilder() {
			public CategoryDataset buildData() {
				return buildDataset(build, new DataCollector() {
					
					public String getTitle() {
						return "CCN";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						return action.getTotals().getFileTotal().getCcn();
					}
				});
			}
		});
        
        index ++;
      //Fouth
        build_category(build, chart, index, Color.ORANGE, "Total Functions", new CategoryDatasetBuilder() {
			public CategoryDataset buildData() {
				return buildDataset(build, new DataCollector() {
					
					public String getTitle() {
						return "Functions";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						return action.getTotals().getFileTotal().getFunctions();
					}
				});
			}
		});
        
        
        index ++;
      //Fifth
        build_category(build, chart, index, Color.CYAN, "CCN Violated Functions", new CategoryDatasetBuilder() {
			public CategoryDataset buildData() {
				return buildDataset(build, new DataCollector() {
					
					public String getTitle() {
						return "CCNVF";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						Collection<Statistic> functionResults = action.getResults().getFunctionResults();
						int ccnViolatedFunctions = 0;
		            	
		            	for (Statistic statistic : functionResults) {
							if(statistic.getCcn() > functionCcnViolationThreshold.intValue())
								ccnViolatedFunctions ++;
						}
						return ccnViolatedFunctions;
					}
				});
			}
		});
        
        index ++;
      //Sixth
        build_category(build, chart, index, Color.MAGENTA, "NCSS Violated Functions", new CategoryDatasetBuilder() {
			public CategoryDataset buildData() {
				return buildDataset(build, new DataCollector() {
					
					public String getTitle() {
						return "NCSSVF";
					}
					
					public long getCollectedNumber(AbstractBuildReport action) {
						Collection<Statistic> functionResults = action.getResults().getFunctionResults();
		            	int ncssViolatedFunctions = 0;
		            	
		            	for (Statistic statistic : functionResults) {
							if(statistic.getNcss() > functionNcssViolationThreshold.intValue())
								ncssViolatedFunctions ++;
						}
						return ncssViolatedFunctions;
					}
				});
			}
		});
        
        return chart;
    }

	private static void build_category(AbstractBuild<?, ?> build,
			final JFreeChart chart, int index, Color color, String title, CategoryDatasetBuilder datasetBuilder) {
		    NumberAxis axis= new NumberAxis(title);
		    axis.setLabelPaint(color);
		    axis.setAxisLinePaint(color);
		    axis.setTickLabelPaint(color);        
		    CategoryPlot categoryPlot = chart.getCategoryPlot();
		    categoryPlot.setRangeAxis(index, axis);        
		    categoryPlot.setDataset(index, datasetBuilder.buildData());
		    categoryPlot.mapDatasetToRangeAxis(index,index);
		    categoryPlot.mapDatasetToDomainAxis(index,0);
		    CategoryItemRenderer rendu= new DefaultCategoryItemRenderer();        
		    rendu.setPaint(color);
		    categoryPlot.setRenderer(index,rendu);
	}
    
	private static CategoryDataset buildDataset(AbstractBuild<?, ?> build, DataCollector collector) {
    	DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
    	
    	for (AbstractBuild<?, ?> lastBuild = build; lastBuild != null; lastBuild = lastBuild.getPreviousBuild()) { 
        	ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(lastBuild);
            AbstractBuildReport action = lastBuild.getAction(AbstractBuildReport.class);
            if (action != null) {
            	builder.add(collector.getCollectedNumber(action), collector.getTitle(), label);
            }
        }
        return builder.build();
	}
}

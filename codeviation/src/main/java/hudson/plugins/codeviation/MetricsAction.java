/* Copyright (c) 2007, http://www.codeviation.org project 
 * This program is made available under the terms of the MIT License. 
 */

package hudson.plugins.codeviation;

import hudson.model.Action;
import hudson.model.Project;
import java.io.IOException;
import org.codeviation.model.Repository;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;


import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.codeviation.model.SourceRoot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerProxy;

/**
 *
 * @author pzajac
 */
public class MetricsAction implements Action,StaplerProxy {
    Project prj;
    String defaultMapping;
//    String name = "namek";
    public MetricsAction(Project prj) {
        this.prj = prj;
    }

    List<SourceRoot> getSourceRoots() {
        return getWrapper().getRepository().getSourceRoots();
    }
    public PAntWrapper getWrapper() {
        return (PAntWrapper) prj.getBuildWrappers().get(hudson.plugins.codeviation.PAntWrapper.DESCRIPTOR);
    }
    public String getIconFileName() {
        return "/plugin/codeviation/images/24x24/codeviation.png";
    }

    public String getDisplayName() {
        return "Projects Metrics";
    }

    public String getUrlName() {
        return "codeviation";
    }
    
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
         Repository rep = getWrapper().getRepository(); 
 
         if(ChartUtil.awtProblemCause != null || rep == null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath()+"/images/headless.png");
            return;
        }
        //number of source roots
         int max = 1;
        Map<String,Integer> tagsMap =  new TreeMap<String,Integer>(); 
        for (SourceRoot src : rep.getSourceRoots()) {
            for (String tag: src.getCvsTags()) {
                Integer val = tagsMap.get(tag);
                int newVal = 1;
                if (val != null) {
                    newVal += val;
                }
                if (newVal > max) {
                    newVal = max;
                }
                tagsMap.put(tag,newVal);
            }
        }
        DataSetBuilder<String,String> dsb = new DataSetBuilder<String,String>();

        for (Map.Entry<String,Integer> entry : tagsMap.entrySet()) {
            dsb.add(entry.getValue(), "number of sourceRoots", entry.getKey());
        } 

        ChartUtil.generateGraph(req,rsp,createChart(dsb.build(),max),400,200);
    }

    private JFreeChart createChart(CategoryDataset dataset,int maxVal) {

        final JFreeChart chart = ChartFactory.createLineChart(
            null,                   // chart title
            null,                   // unused
            "count",                    // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(maxVal);
        rangeAxis.setLowerBound(0);

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setStroke(new BasicStroke(4.0f));

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0,0,0,5.0));

        return chart;
    }

    public Object getTarget() {
        return new RepositoryView(getWrapper().getRepository(),prj);
    }
     
}

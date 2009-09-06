package org.jggug.hudson.plugins.gcrawler;

import static java.lang.String.format;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.ListView;
import hudson.model.PeriodicWork;
import hudson.model.View;
import hudson.model.Descriptor.FormException;
import hudson.views.BuildButtonColumn;
import hudson.views.JobColumn;
import hudson.views.LastDurationColumn;
import hudson.views.LastFailureColumn;
import hudson.views.LastSuccessColumn;
import hudson.views.ListViewColumn;
import hudson.views.StatusColumn;
import hudson.views.WeatherColumn;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.jggug.hudson.plugins.gcrawler.crawlers.GoogleCodeCrawler;
import org.kohsuke.stapler.StaplerRequest;

public class GCrawler extends PeriodicWork {

    @Extension
    public static final GCrawler CRAWLER = new GCrawler();

    private CrawlLogger logger;

    private boolean isActive;

    private CrawlContext context;

    protected GCrawler() {}

    @Override
    protected void doRun() {
        if (isActive) {
            return;
        }

        isActive = true;

        StopWatch watch = new StopWatch();
        watch.start();

        logger = context.getLogger();

        logger.info("crawl start.");
        logger.info("-- Installed Grails Versions --");
        List<String> versions = new ArrayList<String>(context.getGrailsMap().keySet());
        Collections.sort(versions);
        for (String ver : versions) {
            logger.info(format("  %s", ver));
        }
        logger.info("-------------------------------");

        try {
            Future<List<GrailsProjectInfo>> future = Executors.newSingleThreadExecutor()
                .submit(new GoogleCodeCrawler(context));
            List<GrailsProjectInfo> projects = future.get();
            rebuildViews(projects);
            GCrawlerPlugin.getConfig().setGrailsProjectInfoList(projects);
            watch.stop();
            logger.info(format("crawl complete [%s].", DurationFormatUtils.formatDuration(watch.getTime(), "m:ss.SSS")));
        } catch (InterruptedException e) {
            logger.warn(e);
        } catch (ExecutionException e) {
            logger.warn(e);
        } catch (IOException e) {
            logger.warn(e);
        } finally {
            logger.close();
            isActive = false;
            GCrawlerPlugin.getConfig().setLastCrawlDate(new Date());
        }
    }

    @Override
    public long getRecurrencePeriod() {
        // each 1 hour.
        return 60 * 60 * 1000;
    }

    private void rebuildViews(List<GrailsProjectInfo> projects) throws IOException {
        Hudson hudson = Hudson.getInstance();
        for (View view : hudson.getViews()) {
            if (view instanceof ListView)
            hudson.deleteView(view);
        }
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        for (GrailsProjectInfo p : projects) {
            if (!p.hasError()) {
                Set<String> jobNames = map.get(p.getGrailsVersion());
                if (jobNames == null) {
                    map.put(p.getGrailsVersion(), jobNames = new HashSet<String>());
                }
                jobNames.add(format("%s.%s", p.getName(), p.getDomain()));
            }
        }
        for (Entry<String, Set<String>> entry : map.entrySet()) {
            GrailsListView.create(format("grails-%s", entry.getKey())).save(entry.getValue());
        }
    }

    private static class GrailsListView extends ListView {

        private GrailsListView(String name) {
            super(name);
        }

        public void save(Set<String> jobNames) {
            try {
                submit(MockStaplerRequest.create(jobNames));
            } catch (ServletException e) {
                throw new RuntimeException(e);
            } catch (FormException e) {
                throw new RuntimeException(e);
            }
        }

        public static GrailsListView create(String name) {
            GrailsListView result = new GrailsListView(name);
            result.owner = Hudson.getInstance();
            try {
                Hudson.getInstance().addView(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }

    public synchronized boolean isActive() {
        return isActive;
    }

    private static class MockStaplerRequest implements InvocationHandler {

        private Set<String> jobNames;

        private JSONObject json;

        private MockStaplerRequest(Set<String> jobNames) {
            this.jobNames = jobNames;
            json = new JSONObject().accumulate("columns", new JSONArray()
                .element(createElement(StatusColumn.DescriptorImpl.class))
                .element(createElement(WeatherColumn.DescriptorImpl.class))
                .element(createElement(JobColumn.DescriptorImpl.class))
                .element(createElement(LastSuccessColumn.DescriptorImpl.class))
                .element(createElement(LastFailureColumn.DescriptorImpl.class))
                .element(createElement(LastDurationColumn.DescriptorImpl.class))
                .element(createElement(BuildButtonColumn.DescriptorImpl.class)));
        }

        private JSONObject createElement(Class<? extends Descriptor<ListViewColumn>> type) {
            return new JSONObject().accumulate("kind", type.getName());
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("getParameter")) {
                return jobNames.contains(args[0]) ? "true" : null;
            }
            else if (methodName.equals("getSubmittedForm")) {
                return json;
            }
            throw new UnsupportedOperationException(methodName);
        }

        public static StaplerRequest create(Set<String> jobNames) {
            return (StaplerRequest) Proxy.newProxyInstance(
                StaplerRequest.class.getClassLoader(),
                new Class[] {StaplerRequest.class},
                new MockStaplerRequest(jobNames));
        }
    }

    public void setCrawlerContext(CrawlContext context) {
        this.context = context;
    }
}

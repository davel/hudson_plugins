/**
 * Copyright (c) 2009 Cliffano Subagio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.sitemonitor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Performs the web site monitoring process.
 * @author cliffano
 */
public class SiteMonitorBuilder extends Builder {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(SiteMonitorBuilder.class.getName());

    /**
     * The list of web sites to monitor.
     */
    private List<Site> mSites;

    /**
     * Construct {@link SiteMonitorBuilder}.
     * @param sites
     *            the list of web sites to monitor
     */
    public SiteMonitorBuilder(final List<Site> sites) {
        mSites = sites;
    }

    /**
     * @return the list of web sites to monitor
     */
    public List<Site> getSites() {
        return mSites;
    }

    /**
     * Performs the web site monitoring by checking the response code of the
     * site's URL.
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return true if all sites give success response codes, false otherwise
     * @throws InterruptedException
     *             when there's an interruption
     * @throws IOException
     *             when there's an IO error
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {

        List<Result> results = new ArrayList<Result>();
        SiteMonitorDescriptor descriptor = (SiteMonitorDescriptor) getDescriptor();

        boolean hasFailure = false;
        for (Site site : mSites) {

            int responseCode = -1;
            String note;

            try {
                HttpURLConnection connection = (HttpURLConnection) (new URL(
                        site.getUrl())).openConnection();
                connection.setConnectTimeout(descriptor.getTimeout() * 1000);
                responseCode = connection.getResponseCode();

                if (!descriptor.getSuccessResponseCodes().contains(
                        new Integer(responseCode))) {
                    hasFailure = true;
                    note = "FAILURE";
                } else {
                    note = "SUCCESS";
                }
            } catch (Exception e) {
                note = "ERROR " + e.getMessage();
            }

            Result result = new Result(site, responseCode, note);
            results.add(result);
        }

        log(results, listener.getLogger());
        return !hasFailure;
    }

    private void log(final List<Result> results, final PrintStream logger) {
        logger
                .println("\t--------------------------------------------------------------------------------");
        logger.println("\tCODE\tURL\t\t\t\tNOTE");
        logger
                .println("\t--------------------------------------------------------------------------------");
        for (Result result : results) {
            logger
                    .println("\t" + result.getResponseCode() + "\t"
                            + result.getSite().getUrl() + "\t\t\t\t"
                            + result.getNote());
        }
        logger
                .println("\t--------------------------------------------------------------------------------\n\n");
    }
}

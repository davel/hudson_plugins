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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Handles the global and job configuration management.
 * @author cliffano
 */
@Extension
public class SiteMonitorDescriptor extends BuildStepDescriptor<Builder> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(SiteMonitorDescriptor.class.getName());

    /**
     * The form validator.
     */
    private Validator mValidator;

    /**
     * The response codes used to indicate that the web site is up.
     */
    private List<Integer> mSuccessResponseCodes;

    /**
     * The HTTP connection timeout value (in seconds).
     */
    private Integer timeout;

    /**
     * Constructs {@link SiteMonitorDescriptor}.
     */
    public SiteMonitorDescriptor() {
        super(SiteMonitorBuilder.class);
        load();
        mValidator = new Validator();
    }

    /**
     * @return the plugin's display name, used in the job's build drop down list
     */
    @Override
    public final String getDisplayName() {
        return "Monitor Site";
    }

    /**
     * Checks whether this descriptor is applicable.
     * @param clazz
     *            the class
     * @return true
     */
    @Override
    public final boolean isApplicable(
            final Class<? extends AbstractProject> clazz) {
        return true;
    }

    /**
     * @return the success response codes
     */
    public final List<Integer> getSuccessResponseCodes() {
        return mSuccessResponseCodes;
    }
    
    /**
     * @return the success response codes in comma-separated value format
     */
    public final String getSuccessResponseCodesCsv() {
        if (mSuccessResponseCodes == null) {
            mSuccessResponseCodes = new ArrayList<Integer>();
            mSuccessResponseCodes.add(HttpURLConnection.HTTP_OK);
        }
        StringBuffer sb = new StringBuffer();
        for (Integer successResponseCode : mSuccessResponseCodes) {
            sb.append(successResponseCode).append(",");
        }
        return sb.toString().replaceFirst(",$", "");
    }

    /**
     * @return the timeout value in seconds
     */
    public final Integer getTimeout() {
        if (timeout == null) {
            timeout = new Integer(30);
        }
        return timeout;
    }

    /**
     * Handles SiteMonitor configuration for each job.
     * @param request
     *            the stapler request
     * @param json
     *            the JSON data containing job configuration values
     */
    @Override
    public Builder newInstance(StaplerRequest request, JSONObject json) {
        LOGGER.fine("json: " + json);

        List<Site> sites = new ArrayList<Site>();

        Object sitesObject = json.get("sites");
        if (sitesObject instanceof JSONObject) {
            for (Object siteObject : json.getJSONObject("sites").values()) {
                String url = String.valueOf(siteObject);
                sites.add(new Site(url));
            }
        } else if (sitesObject instanceof JSONArray) {
            for (Object siteObject : (JSONArray) sitesObject) {
                if (siteObject instanceof JSONObject) {
                    String url = ((JSONObject) siteObject).getString("url");
                    sites.add(new Site(url));
                }
            }
        } else {
            LOGGER
                    .warning("Unable to parse 'sites' object in JSON data. It's neither JSONObject nor JSONArray");
        }
        return new SiteMonitorBuilder(sites);
    }

    /**
     * Handles SiteMonitor global configuration per Hudson instance.
     * @param request
     *            the stapler request
     * @param json
     *            the JSON data containing job configuration values
     */
    @Override
    public boolean configure(StaplerRequest request, JSONObject json) {
        LOGGER.fine("json: " + json);

        for (String responseCode : json.getString("successResponseCodes")
                .split(",")) {
            mSuccessResponseCodes.add(Integer.parseInt(responseCode.trim()));
        }
        timeout = json.getInt("timeout");
        save();
        return true;
    }

    /**
     * @param value
     *            the value to validate
     * @return true if value is a valid URL, false otherwise
     */
    public FormValidation doCheckUrl(@QueryParameter String value) {
        return mValidator.validateUrl(value);
    }

    /**
     * @param value
     *            the value to validate
     * @return true if value is a valid comma-separated response codes, false
     *         otherwise
     */
    public FormValidation doCheckResponseCodes(@QueryParameter String value) {
        return mValidator.validateResponseCodes(value);
    }

    /**
     * @param value
     *            the value to validate
     * @return true if value is a valid timeout, false otherwise
     */
    public FormValidation doCheckTimeout(@QueryParameter String value) {
        return mValidator.validateTimeout(value);
    }
}

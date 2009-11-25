/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Alan Harder
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
package hudson.plugins.lastsuccessversioncolumn;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;

import java.text.DateFormat;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * View column that shows the last success version by parsing it out of the build description or using the build number.
 * It also shows the date it succeeded.
 * 
 * @author Adam Purkiss
 */
public class LastSuccessVersionColumn extends ListViewColumn {

    public String getShortName(Job job) {
        Run lastSuccessfulBuild = job.getLastSuccessfulBuild();
        StringBuilder stringBuilder = new StringBuilder();

        if (lastSuccessfulBuild != null) {
            String successfulDate = DateFormat.getDateTimeInstance().format(lastSuccessfulBuild.getTimestamp()
                                                                                               .getTime());
            stringBuilder.append(successfulDate);
            String tempDescription = lastSuccessfulBuild.getDescription();
            int index = -1;
            if (tempDescription != null) {
                index = tempDescription.indexOf("[version]");
            }

            stringBuilder.append(" (<a href=\"");
            stringBuilder.append(lastSuccessfulBuild.getUrl());
            stringBuilder.append("\">");

            if (index != -1) {
                stringBuilder.append(tempDescription.substring(index + 9).trim());
            } else {
                stringBuilder.append(Integer.toString(lastSuccessfulBuild.getNumber()));
            }
            stringBuilder.append("</a>)");

        } else {
            stringBuilder.append("N/A");
        }

        return stringBuilder.toString();
    }

    @Extension
    public static final Descriptor<ListViewColumn> DESCRIPTOR = new DescriptorImpl();

    public Descriptor<ListViewColumn> getDescriptor() {
        return DESCRIPTOR;
    }

    private static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public ListViewColumn newInstance(StaplerRequest req,
                                          JSONObject formData) throws FormException {
            return new LastSuccessVersionColumn();
        }

        @Override
        public String getDisplayName() {
            return Messages.LastSuccessVersionColumn_DisplayName();
        }
    }
}

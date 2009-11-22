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
package hudson.plugins.jobtype_column;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import java.util.HashMap;
import net.sf.json.JSONObject;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerRequest;

/**
 * View column that shows the job type.
 * @author Alan.Harder@sun.com
 */
public class JobTypeColumn extends ListViewColumn {

    // Map of Job class' simple name to localizable short name
    private static final HashMap<String,Localizable> labelMap = new HashMap<String,Localizable>();

    static {
        labelMap.put("FreeStyleProject", Messages._FreeStyleProject_ShortName());
        labelMap.put("MavenModuleSet", Messages._MavenModuleSet_ShortName());
        labelMap.put("MatrixProject", Messages._MatrixProject_ShortName());
        labelMap.put("ExternalJob", Messages._ExternalJob_ShortName());
        labelMap.put("JPRTJob", Messages._JPRTJob_ShortName());
    }

    public String getShortName(Job job) {
        Localizable shortName = labelMap.get(job.getClass().getSimpleName());
        return shortName != null ? shortName.toString() : Messages.Unknown_ShortName();
    }

    @Extension
    public static final Descriptor<ListViewColumn> DESCRIPTOR = new DescriptorImpl();

    public Descriptor<ListViewColumn> getDescriptor() {
        return DESCRIPTOR;
    }

    private static class DescriptorImpl extends Descriptor<ListViewColumn> {
        @Override
        public ListViewColumn newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new JobTypeColumn();
        }

        @Override
        public String getDisplayName() {
            return Messages.JobTypeColumn_DisplayName();
        }
    }
}

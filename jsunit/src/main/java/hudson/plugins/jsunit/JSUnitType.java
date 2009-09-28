/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot, Rick Oosterholt                                  *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package hudson.plugins.jsunit;

import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitTypeDescriptor;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;
import net.sf.json.JSONObject;

public class JSUnitType extends XUnitType {

    private JSUnitType(String pattern) {
        super(pattern);
    }

    public String getXsl() {
        return "jsunit-to-junit.xsl";
    }

	@Override
    public XUnitTypeDescriptor<?> getDescriptor() {
        return new JSUnitType.DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends XUnitTypeDescriptor<JSUnitType> {

        public DescriptorImpl() {
            super(JSUnitType.class);
        }

        @Override
        public String getDisplayName() {
            return "JSUnit";
        }

		@Override
        public JSUnitType newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new JSUnitType(formData.getString("pattern"));
        }
    }
}
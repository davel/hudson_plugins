/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
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

package com.thalesgroup.hudson.plugins.xunit.transformer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.FilePath;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

import com.thalesgroup.hudson.plugins.xunit.AbstractWorkspaceTest;
import com.thalesgroup.hudson.plugins.xunit.types.BoostTestType;
import com.thalesgroup.hudson.plugins.xunit.types.*;
import net.sf.json.JSONObject;

public class XUnitTransformerTest extends AbstractWorkspaceTest {

    private XUnitTransformer xUnitTransformer;
    private BuildListener listener;
    private AbstractBuild<?, ?> owner;
    private FilePath junitOutputPath;
    private VirtualChannel channel;

    @Before
    public void initialize() throws Exception {
        listener = mock(BuildListener.class);
        when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        junitOutputPath = new FilePath(new File(parentFile, "junitOutput"));
        if (junitOutputPath.exists()) {
            junitOutputPath.deleteRecursive();
        }
        junitOutputPath.mkdirs();
        channel = mock(VirtualChannel.class);
        owner = mock(AbstractBuild.class);
        super.createWorkspace();
    }

    @After
    public void tearDown() throws Exception {
        super.deleteWorkspace();
    }

    @Test
    public void testEmptyConfig() throws Exception {
        xUnitTransformer = new XUnitTransformer(listener, 0, mock(EnvVars.class), new XUnitType[0], junitOutputPath);
        Boolean result = xUnitTransformer.invoke(new File(workspace.toURI()), channel);
        Assert.assertFalse("With an empty configuration, there is an error.", result);
    }


    private boolean processTransformer(XUnitType[] types) throws Exception {
        xUnitTransformer = new XUnitTransformer(listener, 0, mock(EnvVars.class), types, junitOutputPath);
        return xUnitTransformer.invoke(new File(workspace.toURI()), channel);
    }


    class TextXUnit extends XUnitType {

        private String xsl;

        TextXUnit(String pattern, String xsl) {
            super(pattern);
            this.xsl = xsl;
        }


        public String getXsl() {
            return xsl;
        }

        public XUnitTypeDescriptor<?> getDescriptor() {
            return new DescriptorImpl();
        }

        public class DescriptorImpl extends XUnitTypeDescriptor<PHPUnitType> {

            public DescriptorImpl() {
                super(PHPUnitType.class);
            }

            @Override
            public String getDisplayName() {
                return "Test Tool";
            }

        }

    }

    ;

    private void wrongPattern() throws Exception {
        XUnitType[] types = new XUnitType[]{new TextXUnit("*.txt", "cppunit-to-junit.xsl")};
        workspace.createTextTempFile("report", ".xml", "content");
        Assert.assertFalse("With a wrong pattern, it have to be false", processTransformer(types));
    }

    private void oneMatchWithWrongContent() throws Exception {
        XUnitType[] types = new XUnitType[]{new TextXUnit("*.xml", "cppunit-to-junit.xsl")};
        workspace.createTextTempFile("report", ".xml", "content");
        try {
            processTransformer(types);
            Assert.assertFalse("With a wrong content, there is an exception", false);
        }
        catch (IOException2 ioe) {
            Assert.assertTrue(true);
        }
    }

    private void oneMatchWithValidContent() throws Exception {
        XUnitType[] types = new XUnitType[]{new TextXUnit("*.xml", "cppunit-to-junit.xsl")};
        String content = XUnitXSLUtil.readXmlAsString("cppunit/testcase1/cppunit-successAndFailure.xml");
        File reportFile = new File(new File(workspace.toURI()), "report.xml");
        FileWriter fw = new FileWriter(reportFile);
        fw.write(content);
        fw.close();
        processTransformer(types);
        Assert.assertTrue(true);
    }


    @Test
    public void testTool() throws Exception {
        wrongPattern();
        oneMatchWithWrongContent();
        oneMatchWithValidContent();
    }


}

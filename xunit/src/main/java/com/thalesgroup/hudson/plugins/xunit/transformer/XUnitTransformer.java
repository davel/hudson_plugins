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

import hudson.FilePath;
import hudson.Util;
import hudson.EnvVars;
import hudson.AbortException;
import hudson.util.IOException2;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private static final String JUNIT_FILE_POSTFIX = ".xml";
    private static final String JUNIT_FILE_PREFIX = "TEST-";


    private BuildListener listener;
    private long buildTime;
    private EnvVars env;
    private XUnitType[] types;
    private FilePath junitOutputPath = null;

    public XUnitTransformer(BuildListener listener, long buildTime, EnvVars env, XUnitType[] types, FilePath junitOutputPath) {
        this.listener = listener;
        this.buildTime = buildTime;
        this.env = env;
        this.types = types;
        this.junitOutputPath = junitOutputPath;
    }


    /**
     * Test if the field is empty
     *
     * @param field
     * @return
     */
    private boolean isEmpty(String field) {
        if (field == null) {
            return true;
        }

        if (field.trim().length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Invocation
     *
     * @param ws
     * @param channel
     * @return the Result
     * @throws IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {

        try {

            boolean isInvoked = false;

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocumentBuilder = factory.newDocumentBuilder();
            Transformer writerTransformer = transformerFactory.newTransformer();

            for (XUnitType tool : types) {
                if (!isEmpty(tool.getPattern())) {
                    isInvoked = true;
                    boolean result = processTool(ws,
                            transformerFactory, xmlDocumentBuilder, writerTransformer, tool,
                            new StreamSource(this.getClass().getResourceAsStream(tool.getXsl())));
                    if (!result) {
                        return result;
                    }
                }
            }

            if (!isInvoked) {
                String msg = "[ERROR] - No test report files were found. Configuration error?";
                XUnitLog.log(listener, msg);
                return false;
            }


        }
        catch (Exception e) {
            throw new IOException2("Problem on converting into JUnit reports.", e);
        }

        return true;

    }


    private boolean validateXUnitResultFile(File fileXUnitReportFile)
            throws FactoryConfigurationError {
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parser.parse(fileXUnitReportFile);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    /**
     * /**
     * Collect reports from the given parentpath and the pattern, while
     * filtering out all files that were created before the given time.
     *
     * @param buildTime  the build time
     * @param parentPath parent
     * @param pattern    pattern to seach files
     * @return an array of strings
     */
    private List<String> findtReports(XUnitType testTool, long buildTime, File parentPath, String pattern) throws AbortException {

        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        File baseDir = ds.getBasedir();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = "[ERROR] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + testTool.getDescriptor().getDisplayName() + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + testTool.getDescriptor().getDisplayName() + "'?";
            XUnitLog.log(listener, msg);
            return null;
        }

        boolean parsed = false;
        List<String> resultFiles = new ArrayList<String>();
        for (String value : xunitFiles) {
            File reportFile = new File(baseDir, value);
            //only count files that were actually updated during this build
            if (buildTime - 3000 <= reportFile.lastModified()) {
                resultFiles.add(value);
                parsed = true;
            }
        }

        if (!parsed) {
            long localTime = System.currentTimeMillis();
            if (localTime < buildTime - 1000) {
                // build time is in the the future. clock on this slave must be running behind
                String msg = "[ERROR] - Clock on this slave is out of sync with the master, and therefore \n" +
                        "I can't figure out what test results are new and what are old.\n" +
                        "Please keep the slave clock in sync with the master.";
                XUnitLog.log(listener, msg);
                return null;
            }

            File f = new File(baseDir, xunitFiles[0]);
            String msg = "[ERROR] - " + String.format(
                    "Test reports were found but none of them are new. Did tests run? \n" +
                            "For example, %s is %s old\n", f,
                    Util.getTimeSpanString(buildTime - f.lastModified()));
            XUnitLog.log(listener, msg);

            return null;

        }

        return resultFiles;

    }

    /**
     * Processing the current test tool
     *
     * @param moduleRoot
     * @param transformerFactory
     * @param xmlDocumentBuilder
     * @param writerTransformer
     * @param testTool
     * @param stylesheet
     * @throws TransformerException
     * @throws IOException
     * @throws InterruptedException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private boolean processTool(File moduleRoot, TransformerFactory transformerFactory, DocumentBuilder xmlDocumentBuilder, Transformer writerTransformer, XUnitType testTool, StreamSource stylesheet)
            throws TransformerException, IOException, InterruptedException {

        Transformer toolXMLTransformer = transformerFactory.newTransformer(stylesheet);

        String curPattern = testTool.getPattern();
        curPattern = curPattern.replaceAll("[\t\r\n]+", " ");
        curPattern = Util.replaceMacro(curPattern, this.env);

        List<String> resultFiles = findtReports(testTool, this.buildTime, moduleRoot, curPattern);


        if (resultFiles == null) {
            return false;
        }

        XUnitLog.log(listener, "[" + testTool.getDescriptor().getDisplayName() + "] - Processing " + resultFiles.size() + " files with the pattern '" + testTool.getPattern() + "' relative to '" + moduleRoot + "'.");

        boolean hasInvalidateFiles = false;
        for (String resultFile : resultFiles) {

            File resultFilePathFile = new File(moduleRoot, resultFile);

            if (resultFilePathFile.length() == 0) {
                //Ignore the empty result file (some reason)
                String msg = "[WARNING] - The file '" + resultFilePathFile.getPath() + "' is empty. This file has been ignored.";
                XUnitLog.log(listener, msg);
                continue;
            }


            if (!validateXUnitResultFile(resultFilePathFile)) {

                //register there are unvalid files
                hasInvalidateFiles = true;

                //Ignore unvalid files
                XUnitLog.log(listener, "[WARNING] - The file '" + resultFilePathFile + "' is an invalid file. It has been ignored.");
                continue;
            }


            FilePath junitTargetFile = new FilePath(junitOutputPath, "file" + resultFilePathFile.hashCode() + ".xml");
            try {
                toolXMLTransformer.transform(new StreamSource(resultFilePathFile), new StreamResult(new File(junitTargetFile.toURI())));
                processJUnitFile(xmlDocumentBuilder, writerTransformer, junitTargetFile, junitOutputPath);
            }
            catch (TransformerException te) {
                String msg = "[ERROR] - Couldn't convert the file '" + resultFilePathFile.getPath() + "' into a JUnit file.";
                XUnitLog.log(listener, msg);
                return false;
            }
            catch (SAXException te) {
                String msg = "[ERROR] - Couldn't split JUnit testsuites for the file '" + resultFile + "' into JUnit files with one testsuite.";
                XUnitLog.log(listener, msg);
                return false;
            }
        }
        return true;
    }


    /**
     * Processing the current junit file
     *
     * @param xmlDocumentBuilder
     * @param writerTransformer
     * @param junitFile
     * @param junitOutputPath
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     */
    private void processJUnitFile(DocumentBuilder xmlDocumentBuilder, Transformer writerTransformer, FilePath junitFile, FilePath junitOutputPath)
            throws SAXException, IOException, TransformerException, InterruptedException {

        Document document = xmlDocumentBuilder.parse(new File(junitFile.toURI()));
        NodeList testsuitesNodeList = document.getElementsByTagName("testsuites");
        if (testsuitesNodeList == null || testsuitesNodeList.getLength() == 0) {
            junitFile.renameTo(new FilePath(junitFile.getParent(), JUNIT_FILE_PREFIX + junitFile.getName() + JUNIT_FILE_POSTFIX));
            return;
        }
        splitJunitFile(writerTransformer, testsuitesNodeList, junitOutputPath);
    }

    /**
     * Segragate the current junit file
     *
     * @param writerTransformer
     * @param testsuitesNodeList
     * @param junitOutputPath
     * @throws IOException
     * @throws InterruptedException
     * @throws TransformerException
     */
    private void splitJunitFile(Transformer writerTransformer, NodeList testsuitesNodeList, FilePath junitOutputPath) throws IOException, InterruptedException, TransformerException {
        NodeList elementsByTagName = ((Element) testsuitesNodeList.item(0)).getElementsByTagName("testsuite");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            DOMSource source = new DOMSource(element);
            String suiteName = element.getAttribute("name");
            FilePath junitOutputFile = new FilePath(junitOutputPath, JUNIT_FILE_PREFIX + suiteName.hashCode() + JUNIT_FILE_POSTFIX);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(junitOutputFile.toURI()));
            try {
                StreamResult result = new StreamResult(fileOutputStream);
                writerTransformer.transform(source, result);
            } finally {
                fileOutputStream.close();
            }
        }
    }

}

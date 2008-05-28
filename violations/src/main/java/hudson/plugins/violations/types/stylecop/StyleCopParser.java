package hudson.plugins.violations.types.stylecop;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hudson.plugins.violations.ViolationsParser;
import hudson.plugins.violations.model.FullBuildModel;
import hudson.plugins.violations.model.FullFileModel;
import hudson.plugins.violations.model.Severity;
import hudson.plugins.violations.types.fxcop.XmlElementUtil;
import hudson.plugins.violations.util.AbsoluteFileFinder;
import hudson.plugins.violations.model.Violation;

/**
 * Parses a StyleCop (http://code.msdn.microsoft.com/sourceanalysis/) xml report file.
 * 
 */
public class StyleCopParser implements ViolationsParser {
    
    static final String TYPE_NAME = "stylecop";
    private FullBuildModel model;
    private File reportParentFile;

    public void parse(FullBuildModel model, File projectPath, String fileName, String[] sourcePaths) throws IOException {
        this.model = model;
        this.reportParentFile = new File(fileName).getParentFile();
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new FileInputStream(new File(projectPath, fileName)));

            NodeList mainNode = doc.getElementsByTagName("SourceAnalysisViolations");

            Element rootElement = (Element) mainNode.item(0);
            parseViolations(XmlElementUtil.getNamedChildElements(rootElement, "Violation"));
            
            findSourceFiles(model, projectPath.getPath(), sourcePaths);            
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce);
        } catch (SAXException se) {
            throw new IOException(se);
        }    
    }
    
    /**
     * Go through all violations and see if the source files can be found.
     * @param model model containing all violations
     * @param workspace the workspace
     * @param sourcePaths the optional source paths
     */
    private void findSourceFiles(FullBuildModel model, String workspace, String[] sourcePaths) {
        AbsoluteFileFinder finder = new AbsoluteFileFinder();
        finder.addSourcePath(workspace);
        if (sourcePaths != null) {
            finder.addSourcePaths(sourcePaths);
        }
        
        Map<String, FullFileModel> fileModelMap = model.getFileModelMap();
        for (String key : fileModelMap.keySet()) {
            FullFileModel fileModel = fileModelMap.get(key);
            File sourceFile = finder.getFileForName(fileModel.getDisplayName());
            if (sourceFile != null) {
                fileModel.setSourceFile(sourceFile);
                fileModel.setLastModified(sourceFile.lastModified());
            }
        }
    }
    
    /***
     * Returns the value for the named attribute if it exists
     * @param element the element to check for an attribute
     * @param name the name of the attribute
     * @return the value of the attribute; "" if there is no such attribute.
     */
    private String getString(Element element, String name) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        } else {
            return "";
        }
    }

    /**
     * Parse the Violation tag and add it to the build model
     * @param elements list of Violation tags
     */
    private void parseViolations(List<Element> elements) throws IOException {
        for (Element element : elements) {
            
            Violation violation = new Violation();
            violation.setLine(getString(element, "LineNumber"));
            violation.setMessage(element.getTextContent() + " (" + getString(element, "RuleId") + ")");
            violation.setSeverity(Severity.MEDIUM);
            violation.setSeverityLevel(Severity.MEDIUM_VALUE);
            violation.setType(TYPE_NAME);
            
            String temp = getString(element, "RuleNamespace");
            int i = temp.lastIndexOf('.');
            if (i != -1) {
                violation.setSource(temp.substring(i));
            } else {
                violation.setSource(getString(element,"RuleId"));
            }            
            
            // Add the violation to the model
            String displayName;
            if (reportParentFile == null) {
                displayName = getString(element,"Source");
            } else {
                displayName = reportParentFile.getPath() + File.separator + getString(element,"Source");
            }
            FullFileModel fileModel = model.getFileModel(displayName);
            fileModel.addViolation(violation);
        }
    }
}

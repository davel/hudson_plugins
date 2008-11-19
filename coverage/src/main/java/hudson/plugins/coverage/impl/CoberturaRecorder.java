package hudson.plugins.coverage.impl;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import hudson.plugins.coverage.model.Instance;
import hudson.plugins.coverage.model.JavaModel;
import hudson.plugins.coverage.model.Recorder;
import org.codehaus.stax2.XMLInputFactory2;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since Nov 18, 2008 2:58:27 PM
 */
public class CoberturaRecorder implements Recorder {

// ------------------------------ FIELDS ------------------------------

    /**
     * This field is populated when the recorder is being constructed for a newly built project, for old builds this
     * will be null.
     */
    private final transient Set<File> coberturaXmlResults;

    /**
     * This field is populated when the recorder is being constructed for a newly built project, for old builds this
     * will be null.
     */
    private final transient File sourceCodeRoot;

// -------------------------- STATIC METHODS --------------------------

    private static void safelyClose(XMLEventReader xmlEventReader) {
        if (xmlEventReader != null) {
            try {
                xmlEventReader.close();
            } catch (XMLStreamException e) {
                // ignore
            }
        }
    }

    private static void safelyClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor, will be used to reconstruct results for old builds.
     */
    public CoberturaRecorder() {
        this.coberturaXmlResults = null;
        this.sourceCodeRoot = null;
    }

    /**
     * Creates a new CoberturaRecorder for parsing a new build.
     *
     * @param coberturaXmlResults The cobertura XML result files.
     * @param sourceCodeRoot      The root directory within which the source files should be hiding.
     */
    public CoberturaRecorder(Set<File> coberturaXmlResults, File sourceCodeRoot) {
        this.coberturaXmlResults = coberturaXmlResults;
        this.sourceCodeRoot = sourceCodeRoot;
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Recorder ---------------------

    /**
     * {@inheritDoc}
     */
    public void identifySourceFiles(Instance root) {
        // for the time being I think this is what we want
        reidentifySourceFiles(root, coberturaXmlResults, sourceCodeRoot);
    }

    /**
     * {@inheritDoc}
     */
    public void reidentifySourceFiles(Instance root, Set<File> measurementFiles, File sourceCodeDirectory) {
        XMLInputFactory inputFactory = newXMLInputFactory();
        for (File measurementFile : measurementFiles) {
            if (measurementFile.isFile()) {
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                XMLEventReader r = null;
                try {
                    fis = new FileInputStream(measurementFile);
                    bis = new BufferedInputStream(fis);
                    r = inputFactory.createXMLEventReader(bis);

                    boolean inSources = false;
                    boolean inPackages = false;
                    boolean inCoverage = false;
                    Instance javaInstance = null;
                    Instance packageInstance = null;
                    Set<File> sourceRoots = new HashSet<File>();
                    if (sourceCodeDirectory != null) {
                        sourceRoots.add(sourceCodeDirectory);
                    }
                    while (r.hasNext()) {
                        final XMLEvent event = r.nextEvent();
                        if (event.isStartElement()) {
                            StartElement start = event.asStartElement();
                            final String localPart = start.getName().getLocalPart();
                            if (inCoverage) {
                                if (inSources) {
                                    if ("source".equals(localPart)) {
                                        String sourceDir = r.getElementText();
                                        File source = new File(sourceDir);
                                        if (!source.isAbsolute()) {
                                            if (sourceCodeDirectory != null) {
                                                source = new File(sourceCodeDirectory, sourceDir);
                                            } else {
                                                source = null;
                                            }
                                        }
                                        if (source != null) {
                                            sourceRoots.add(source);
                                        }
                                        System.out.println("source " + source);
                                    }
                                } else if (inPackages) {
                                    if ("package".equals(localPart)) {
                                        String packageName = getAttributeValue(start, "name");
                                        if (packageName != null) {
                                            packageInstance =
                                                    javaInstance.findOrCreateChild(JavaModel.PACKAGE, packageName);
                                        } else {
                                            packageInstance = null;
                                        }
                                        System.out.println("package " + packageName);
                                    } else if (packageInstance != null && "class".equals(localPart)) {
                                        String className = getAttributeValue(start, "name");
                                        String fileName = getAttributeValue(start, "filename");
                                        if (fileName != null && className != null) {
                                            packageInstance.findOrCreateChild(JavaModel.FILE, fileName,
                                                    findFileFromParents(sourceRoots, fileName))
                                                    .addRecorder(this, measurementFile, start.getLocation());
                                        }
                                        System.out.println("class " + className + " (" + fileName + ")");
                                    }
                                } else if ("packages".equals(localPart)) {
                                    inPackages = true;
                                } else if ("sources".equals(localPart)) {
                                    inSources = true;
                                }
                            } else {
                                if ("coverage".equals(localPart)) {
                                    inCoverage = true;
                                    inSources = false;
                                    inPackages = false;
                                    javaInstance = root.findOrCreateChild(JavaModel.LANGUAGE, "");
                                }
                            }
                        } else if (event.isEndElement()) {
                            EndElement end = event.asEndElement();
                            final String localPart = end.getName().getLocalPart();
                            if (inCoverage) {
                                if (inSources) {
                                    if ("sources".equals(localPart)) {
                                        inSources = false;
                                    }
                                } else if (inPackages) {
                                    if ("package".equals(localPart)) {
                                        packageInstance = null;
                                    } else if ("packages".equals(localPart)) {
                                        inPackages = false;
                                    }
                                } else {
                                    if ("coverage".equals(localPart)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    safelyClose(r);
                    safelyClose(bis);
                    safelyClose(fis);
                }
            }
        }
    }

    private XMLInputFactory newXMLInputFactory() {
        XMLInputFactory inputFactory = XMLInputFactory2.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);
        inputFactory.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, Boolean.TRUE);
        return inputFactory;
    }

    /**
     * {@inheritDoc}
     */
    public void parseSourceResults(Instance sourceFile, File measurementFile, Collection<Object> memos) {
        if (sourceFile.getElement().isFileLevel() && measurementFile.isFile() && memos != null && !memos.isEmpty()) {
            SortedSet<Location> locations = convertMemosToSortedLocations(memos);

            // arse arse arse... how to efficiently re-read this file

            FileReader fis = null;
            FileInputStream bis = null;
            XMLEventReader r = null;
            try {
                fis = new FileReader(measurementFile);
                r = newXMLInputFactory().createXMLEventReader(fis);

                while (r.hasNext()) {
                    final XMLEvent event = r.nextEvent();
                    if (event.isStartElement()) {
                        StartElement start = event.asStartElement();
                        System.out.println(start.getName());
                        return;
                    } else if (event.isEndElement()) {
                    }
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                safelyClose(r);
                safelyClose(bis);
                safelyClose(fis);
            }
        }
    }

    private SortedSet<Location> convertMemosToSortedLocations(Collection<Object> memos) {
        SortedSet<Location> locations = new TreeSet<Location>(new Comparator<Location>() {
            public int compare(Location o1, Location o2) {
                int p1 = o1.getCharacterOffset();
                int p2 = o2.getCharacterOffset();
                return p1 > p2 ? 1 : p1 == p2 ? 0 : -1;
            }
        });
        for (Object memo : memos) {
            if (memo instanceof Location) {
                locations.add((Location) memo);
            }
        }
        return locations;
    }

// -------------------------- OTHER METHODS --------------------------

    private File findFileFromParents(Set<File> sourceRoots, String fileName) {
        File file = null;
        for (File sourceDir : sourceRoots) {
            file = new File(sourceDir, fileName);
            if (file.isFile()) {
                return file;
            }
        }
        return file;
    }

    private String getAttributeValue(StartElement start, String localPart) {
        final QName qName = new QName(start.getName().getNamespaceURI(), localPart);
        final Attribute attribute = start.getAttributeByName(qName);
        return attribute == null ? null : attribute.getValue();
    }

}

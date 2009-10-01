/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package hudson.plugins.mavensnapshottrigger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Detects Maven 1.x SNAPSHOT type dependencies which have changed since the last build.
 * <p>
 * Idea: "If there is a newer SNAPSHOT dependency file in the <b>local</b> 
 * repository than the latest build, then trigger a build".
 * <p>
 * This logic does not detect changes in remote repositories automatically. 
 * However, usually builds happen frequently and thus Maven automatically 
 * downloads new dependencies during every build.
 * <p>
 * This implementation is based on MavenSnapshotDependency plugin found in the 
 * CruiseControl distribution. Original code by Tim Shadel.
 * 
 * @author Jarkko Viinamäki
 */
public class MavenSnapshotScanner
{
    /** enable logging for this class */
    private static Logger log = Logger.getLogger(MavenSnapshotScanner.class.getName());
    /** a collection of File objects which point to modified SNAPSHOT files */
    private List<File> modifications;
    /** Maven POM (project.xml) for the project to be scanned */
    private File projectFile;
    /** Pointer to the local Maven repository (contains JARs etc) */
    private File localRepository = new File(System.getProperty("user.home") + "/.maven/repository/");

    /**
     * Sets a full path to the primary project.xml file we are going to scan.
     */
    public void setProjectFile(String s)
    {
        projectFile = new File(s);
    }

    /**
     * Set the path for the local Maven repository.
     * 
     * Properties settings with "maven.local.repo" override this setting.
     * 
     * @param s full path to the local repository
     */
    public void setLocalRepository(String s)
    {
        if( s != null )
        {
          localRepository = new File(s);
        }
    }

    /**
     * Finds out modified SNAPSHOT dependencies.
     * 
     * Note! It is possible that this system misses some dependency changes
     * if snapshots are updated very frequently (i.e. during the build).
     * 
     * @param lastBuild time when the last build occurred
     * @return list of File objects that point to SNAPSHOT dependencies that are
     *    newer than the lastBuild instance
     */
    public List<File> getModifications(Date lastBuild)
    {
        modifications = new ArrayList<File>();

        long lastBuildTime = lastBuild.getTime();
        List<String> filenames = new ArrayList<String>();
        
        getSnapshotFilenames(filenames, projectFile, new ArrayList());

        for (Iterator<String> itr = filenames.iterator(); itr.hasNext();)
        {
          String filename = itr.next();
          File dependency = new File(filename);
          checkFile(dependency, lastBuildTime);
        }

        return modifications;
    }

    /** Check for newer timestamps */
    private void checkFile(File file, long lastBuild)
    {
        if (!file.exists())
        {
            log.warning("Dependency not found on disk: " + file.getName());
        }
        else if ((!file.isDirectory()) && (file.lastModified() > lastBuild))
        {
            modifications.add(file);
            log.fine("Modification detected in " + file.getName());
        }
    }

    /**
     * Parses the Maven project file and finds SNAPSHOT dependencies.
     * 
     * @param filenames this object is used to collect the list of SNAPSHOTs
     * @param mavenFile Maven project.xml file to parse
     * @param callstack Maven POM files already processed (prevents cyclic
     *            dependencies)
     */
    void getSnapshotFilenames(List<String> filenames, File mavenFile, List callstack)
    {
        log.fine("Getting a list of dependencies for " + mavenFile);

        Element mavenElement;
        SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");

        try
        {
            mavenElement = builder.build(mavenFile).getRootElement();
        }
        catch (JDOMException e)
        {
            log.log(Level.SEVERE, "failed to load project file ["
                    + (mavenFile != null ? mavenFile.getAbsolutePath() : "") + "]", e);
            return;
        }
        catch (IOException e)
        {
            log.severe("failed to load project file ["
                    + (mavenFile != null ? mavenFile.getAbsolutePath() : "") + "]");
            return;
        }

        Namespace ns = mavenElement.getNamespace();

        Properties projectProperties = new Properties();
        projectProperties.put("basedir", mavenFile.getParent());

        // set some default properties

        String tmp = mavenElement.getChildText("currentVersion", ns);
        if (tmp != null)
        {
            projectProperties.put("pom.currentVersion", tmp);
        }

        // load Maven properties files
        // http://maven.apache.org/maven-1.x/reference/properties.html

        /**
         * 1. Built-in properties are processed 2. ${basedir}/project.properties
         * (basedir is replaced by the directory where the project.xml file in
         * use resides) 3. ${basedir}/build.properties 4.
         * ${user.home}/build.properties 5. System properties
         */

        loadProperties(projectProperties, 
                new File(mavenFile.getParent() + "/project.properties"));
        loadProperties(projectProperties, 
                new File(mavenFile.getParent() + "/build.properties"));
        loadProperties(projectProperties, 
                new File(System.getProperty("user.home") + "/build.properties"));

        // see if this POM extends a parent POM - if so, first parse the parent

        // TODO: for some unknown reason JDOM/Xerces/SAXParser automatically
        // seems to(?) transform ${basedir} substring inside the extend tag into
        // CWD (which is not what we want since basedir should point to
        // mavenFile dir
        String extend = mavenElement.getChildTextNormalize("extend", ns);
        if (extend != null)
        {
            String parent = replaceVariables(projectProperties, extend);
            File parentFile;

            // first try relative path
            parentFile = new File(mavenFile.getParent() + "/" + parent);
            if (!parentFile.exists())
            {
                parentFile = new File(parent);
            }
            if (!parentFile.exists())
            {
                log.warning("Could not read parent POM! Invalid extend setting: " + extend);
            }
            else if (parentFile.equals(mavenFile))
            {
                log.severe("POM extend tag points to itself!");
            }
            else if (callstack.contains(parentFile))
            {
                log.severe("Cyclic POM inheritance loop detected! Parent POM already processed!");
            }
            else
            {
                callstack.add(mavenFile);
                getSnapshotFilenames(filenames, parentFile, callstack);
            }
        }

        Element depsRoot = mavenElement.getChild("dependencies", ns);

        // No dependencies listed at all
        if (depsRoot == null)
        {
            log.fine("Project descriptor "+mavenFile+" contains no dependencies!");
            return;
        }
        
        // JAR overrides are currently not implemented. Some guidelines how to
        // do it:
        // http://jira.public.thoughtworks.org/browse/CC-141
        // http://maven.apache.org/maven-1.x/using/managing-dependencies.html
        /*
         * boolean mavenJarOverride = false;
         * 
         * String tmp = projectProperties.getProperty("maven.jar.override"); if
         * (tmp != null && (tmp.equalsIgnoreCase("on") ||
         * tmp.equalsIgnoreCase("true"))) { mavenJarOverride = true; }
         */

        List dependencies = depsRoot.getChildren();

        File localRepo = localRepository;
        
        // allow projects and properties settings to override local repo
        if( projectProperties.containsKey("maven.repo.local") )
        {
            localRepo = new File(projectProperties.getProperty("maven.repo.local"));
        }
        
        Iterator itr = dependencies.iterator();
        while (itr.hasNext())
        {
            Element dependency = (Element) itr.next();
            String versionText = dependency.getChildText("version", ns);
            
            if (versionText == null)
            {
                continue;
            }

            // versionText may also include ${pom.currentVersion} and if
            // project/currentVersion is of type xxx-SNAPSHOT, we need to
            // include
            // that dependency
            versionText = replaceVariables(projectProperties, versionText);

            // "the version need only contain the word SNAPSHOT - it does
            // not need to equal it exactly."
            // @see
            // http://maven.apache.org/maven-1.x/using/managing-dependencies.html
            if (versionText.indexOf("SNAPSHOT") != -1)
            {

                String groupId = dependency.getChildText("groupId", ns);
                String artifactId = dependency.getChildText("artifactId", ns);
                String id = dependency.getChildText("id", ns);
                String type = dependency.getChildText("type", ns);

                // replace variables
                artifactId = replaceVariables(projectProperties, artifactId);
                groupId = replaceVariables(projectProperties, groupId);
                id = replaceVariables(projectProperties, id);

                if (type == null)
                {
                    type = "jar";
                }

                // Repository path format:
                // ${repo}/${groupId}/${type}s/${artifactId}-${version}.${type}
                StringBuffer fileName = new StringBuffer();
                
                fileName.append(localRepo.getAbsolutePath());
                fileName.append('/');
                if (groupId != null)
                {
                    fileName.append(groupId);
                }
                else
                {
                    fileName.append(id);
                }
                fileName.append('/');

                if ("ejb-client".equals(type))
                {
                    fileName.append("ejb");
                }
                else
                {
                    fileName.append(type);
                }
                fileName.append('s');
                fileName.append('/');
                if (artifactId != null)
                {
                    fileName.append(artifactId);
                }
                else
                {
                    fileName.append(id);
                }
                fileName.append('-');
                fileName.append(versionText);

                if ("ejb-client".equals(type))
                {
                    fileName.append("-client");
                }

                fileName.append('.');
                if ("uberjar".equals(type) || "ejb".equals(type) || "plugin".equals(type)
                        || "ejb-client".equals(type))
                {
                    fileName.append("jar");
                }
                else
                {
                    fileName.append(type);
                }

                File file = new File(fileName.toString());

                log.fine("Snapshot detected: " + fileName);

                filenames.add(file.getAbsolutePath());
            }
        }
    }

    void loadProperties(Properties properties, File file)
    {
        if (file.exists())
        {

            BufferedInputStream in = null;
            try
            {
                FileInputStream fin = new FileInputStream(file);
                in = new BufferedInputStream(fin);
                properties.load(in);
                log.fine("Loaded " + file.getAbsolutePath());
            }
            catch (IOException ex)
            {
                log.log(Level.SEVERE, 
                        "failed to load project properties file [" + file.getAbsolutePath() + "]",
                        ex);
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (Exception ex)
                {
                    // we don't care
                }
            }
        }
    }

    /**
     * Replaces variables in a string defined as ${key}.
     * 
     * Values for variables are taken from given properties or System
     * properties. Replacement is recursive. If ${key} maps to a string which
     * has other ${keyN} values, those ${keyN} values are replaced also if there
     * is a matching value for them.
     */
    String replaceVariables(Properties p, String value)
    {
        if (value == null || p == null)
        {
            return value;
        }

        int i = value.indexOf("${");
        if (i == -1)
        {
            return value;
        }
        int pos = 0;
        while (i != -1)
        {
            int j = value.indexOf("}", i);
            if (j == -1)
            {
                break;
            }
            String key = value.substring(i + 2, j);

            if (p.containsKey(key))
            {
                value = value.substring(0, i) + p.getProperty(key) + value.substring(j + 1);
                // step one forward from ${ position, otherwise we can get an
                // infinite loop
                pos = i + 1;
            }
            else if (System.getProperty(key) != null)
            {
                value = value.substring(0, i) + System.getProperty(key) + value.substring(j + 1);
                pos = i + 1;
            }
            else
            {
                // could not replace the value, leave it there
                pos = j + 1;
            }

            i = value.indexOf("${", pos);
        }
        return value;
    }
}

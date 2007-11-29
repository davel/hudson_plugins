/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hudson.ivy;

import fr.jayasoft.ivy.DependencyDescriptor;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.ModuleId;
import fr.jayasoft.ivy.parser.ModuleDescriptorParserRegistry;
import hudson.CopyOnWrite;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Trigger the build of other project based on the Ivy dependency managment
 * system.
 */
public class IvyBuildTrigger extends Publisher implements DependecyDeclarer {

    private static final Logger LOGGER = Logger.getLogger(IvyBuildTrigger.class.getName());

    private String ivyFile = "ivy.xml";

    /**
     * Identifies {@link IvyConfiguration} to be used.
     */
    private final String ivyConfName;

    private transient ModuleDescriptor moduleDescriptor;

    /**
     * Contructor
     * 
     * @param ivyFile
     *            the ivy.xml file path within the workspace
     * @param ivyConfName
     *            the Ivy configuration name to use
     */
    public IvyBuildTrigger(final String ivyFile, final String ivyConfName) {
        this.ivyFile = ivyFile;
        this.ivyConfName = ivyConfName;
    }

    /**
     * 
     * @return the ivy.xml file path within the workspace
     */
    public String getIvyFile() {
        return ivyFile;
    }

    /**
     * 
     * @return the Ivy configuration name used
     */
    public String getIvyConfName() {
        return ivyConfName;
    }

    /**
     * 
     * @return the {@link IvyConfiguration} from the {@link #ivyConfName}
     */
    public IvyConfiguration getIvyConfiguration() {
        for (IvyConfiguration i : DESCRIPTOR.getConfigurations()) {
            if (ivyConfName != null && i.getName().equals(ivyConfName)) {
                return i;
            }
        }
        return null;
    }

    /**
     * 
     * @return the Ivy instance based on the {@link #ivyConfName}
     * 
     * @throws ParseException
     * @throws IOException
     */
    public Ivy getIvy() {
        Ivy ivy = new Ivy();
        IvyConfiguration ivyConf = getIvyConfiguration();
        if (ivyConf != null) {
            File conf = new File(ivyConf.getIvyConfPath());
            try {
                ivy.configure(conf);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Parsing error while reading the ivy configuration " + ivyConf.getName()
                        + " at " + ivyConf.getIvyConfPath(), e);
                return null;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "I/O error while reading the ivy configuration " + ivyConf.getName() + " at "
                        + ivyConf.getIvyConfPath(), e);
                return null;
            }
        }
        return ivy;
    }

    /**
     * Get the last computed Ivy module descriptior created from the ivy.xml of
     * this trigger
     * 
     * @param workspace
     *            the path to the root of the workspace
     * @return the Ivy module descriptior
     * @throws ParseException
     * @throws IOException
     */
    public ModuleDescriptor getModuleDescriptor(FilePath workspace) {
        if (moduleDescriptor == null) {
            recomputeModuleDescriptor(workspace);
        }
        return moduleDescriptor;
    }

    /**
     * Force the creation of the module descriptor from the ivy.xml file
     * 
     * @throws ParseException
     * @throws IOException
     */
    public void recomputeModuleDescriptor(FilePath workspace) {
        Ivy ivy = getIvy();
        if (ivy == null) {
            moduleDescriptor = null;
        } else {
            FilePath ivyF = workspace.child(ivyFile);
            try {
                moduleDescriptor = ModuleDescriptorParserRegistry.getInstance().parseDescriptor(ivy,
                        ivyF.toURI().toURL(), ivy.doValidate());
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "The Ivy module descriptor parsing of " + ivyF + " has been interrupted", e);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "The URL is malformed : " + ivyF, e);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Parsing error while reading the ivy file " + ivyF, e);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "I/O error while reading the ivy file " + ivyF, e);
            }
        }
    }

    /**
     * Container the configuration of Ivy
     */
    public static class IvyConfiguration {

        private String name;

        private String ivyConfPath;

        /**
         * Contructor
         * 
         * @param name
         *            the name of the configuration
         * @param ivyConfPath
         *            the full path to the ivy configuration file
         */
        public IvyConfiguration(String name, String ivyConfPath) {
            this.name = name;
            this.ivyConfPath = ivyConfPath;
        }

        /**
         * 
         * @return the full path to the ivy configuration file
         */
        public String getIvyConfPath() {
            return ivyConfPath;
        }

        /**
         * 
         * @return the name of the configuration
         */
        public String getName() {
            return name;
        }

        /**
         * 
         * @return <code>true</code> if the configuration file exists
         */
        public boolean getExists() {
            return new File(ivyConfPath).exists();
        }
    }

    @Override
    public boolean prebuild(Build build, BuildListener listener) {
        // PrintStream logger = listener.getLogger();
        recomputeModuleDescriptor(build.getProject().getWorkspace());
        return true;
    }

    public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
        ModuleDescriptor md = getModuleDescriptor(owner.getWorkspace());
        if (md == null) {
            return;
        }
        Ivy ivy = getIvy();
        if (ivy == null) {
            return;
        }

        List<Project> projects = Hudson.getInstance().getAllItems(Project.class);
        Map<ModuleId, ModuleDescriptor> moduleIdMap = new HashMap<ModuleId, ModuleDescriptor>();
        Map<ModuleId, AbstractProject<?, ?>> projectMap = new HashMap<ModuleId, AbstractProject<?, ?>>();
        for (Project<?, ?> p : projects) {
            if (p.isDisabled()) {
                continue;
            }
            IvyBuildTrigger t = (IvyBuildTrigger) p.getPublisher(DESCRIPTOR);
            if (t != null) {
                ModuleDescriptor m = t.getModuleDescriptor(p.getWorkspace());
                if (m != null) {
                    ModuleId id = m.getModuleRevisionId().getModuleId();
                    moduleIdMap.put(id, m);
                    projectMap.put(id, p);
                }
            }
        }

        Set<ModuleDescriptor> dependencies = new HashSet<ModuleDescriptor>();
        processFilterNodeFromLeaf(md, dependencies, moduleIdMap);

        List<ModuleDescriptor> sortedModules = ivy.sortModuleDescriptors(dependencies);
        List<AbstractProject> deps = new ArrayList<AbstractProject>(sortedModules.size());
        for (ModuleDescriptor m : sortedModules) {
            deps.add(projectMap.get(m.getModuleRevisionId().getModuleId()));
        }
        graph.addDependency(owner, deps);
    }

    /**
     * Adds the current node to the toKeep collection and then processes the
     * each of the direct dependencies of this node that appear in the
     * moduleIdMap (indicating that the dependency is part of this BuildList)
     * 
     * @param node
     *            the node to be processed
     * @param toKeep
     *            the set of ModuleDescriptors that should be kept
     * @param moduleIdMap
     *            reference mapping of moduleId to ModuleDescriptor that are
     *            part of the BuildList
     */
    private void processFilterNodeFromRoot(ModuleDescriptor node, Set<ModuleDescriptor> toKeep,
            Map<ModuleId, ModuleDescriptor> moduleIdMap) {
        DependencyDescriptor[] deps = node.getDependencies();
        for (int i = 0; i < deps.length; i++) {
            ModuleId id = deps[i].getDependencyId();
            ModuleDescriptor n = moduleIdMap.get(id);
            if (n != null) {
                toKeep.add(n);
                processFilterNodeFromRoot(n, toKeep, moduleIdMap);
            }
        }
    }

    /**
     * Search in the moduleIdMap modules depending on node, add them to the
     * toKeep set and process them recursively.
     * 
     * @param node
     *            the node to be processed
     * @param toKeep
     *            the set of ModuleDescriptors that should be kept
     * @param moduleIdMap
     *            reference mapping of moduleId to ModuleDescriptor that are
     *            part of the BuildList
     */
    private void processFilterNodeFromLeaf(ModuleDescriptor node, Set<ModuleDescriptor> toKeep,
            Map<ModuleId, ModuleDescriptor> moduleIdMap) {
        for (ModuleDescriptor m : moduleIdMap.values()) {
            DependencyDescriptor[] deps = m.getDependencies();
            for (int i = 0; i < deps.length; i++) {
                ModuleId id = deps[i].getDependencyId();
                if (node.getModuleRevisionId().getModuleId().equals(id) && !toKeep.contains(m)) {
                    toKeep.add(m);
                    processFilterNodeFromLeaf(m, toKeep, moduleIdMap);
                }
            }
        }
    }

    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * The descriptor of this trigger
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * The descriptor implementation of this trigger
     */
    public static final class DescriptorImpl extends Descriptor<Publisher> {

        @CopyOnWrite
        private volatile IvyConfiguration[] configurations = new IvyConfiguration[0];

        DescriptorImpl() {
            super(IvyBuildTrigger.class);
            load();
        }

        /**
         * 
         * @return every existing configuration
         */
        public IvyConfiguration[] getConfigurations() {
            return configurations;
        }

        @Override
        public String getDisplayName() {
            return "Trigger the build of other project based on the Ivy management system";
        }

        @Override
        protected void convert(Map<String, Object> oldPropertyBag) {
            if (oldPropertyBag.containsKey("configurations")) {
                configurations = (IvyConfiguration[]) oldPropertyBag.get("configurations");
            }
        }

        @Override
        public boolean configure(StaplerRequest req) {
            boolean r = true;

            int i;
            String[] names = req.getParameterValues("ivy_name");
            String[] paths = req.getParameterValues("ivy_conf_path");

            IvyConfiguration[] confs;

            if (names != null && paths != null) {
                int len = Math.min(names.length, paths.length);
                confs = new IvyConfiguration[len];
                for (i = 0; i < len; i++) {
                    if (Util.nullify(names[i]) == null) {
                        continue;
                    }
                    if (Util.nullify(paths[i]) == null) {
                        continue;
                    }
                    confs[i] = new IvyConfiguration(names[i], paths[i]);
                }
            } else {
                confs = new IvyConfiguration[0];
            }

            this.configurations = confs;

            save();

            return r;
        }

        /**
         * Check that the Ivy configuration file exist
         * 
         * @param req
         *            the Stapler request
         * @param rsp
         *            the Stapler response
         * @throws IOException
         * @throws ServletException
         */
        public void doCheckIvyConf(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            // this can be used to check the existence of a file on the server,
            // so needs to be protected
            new FormFieldValidator(req, rsp, true) {
                @Override
                public void check() throws IOException, ServletException {
                    File f = getFileParameter("value");
                    if (f.getPath().equals("")) {
                        error("The Ivy configuration path is required");
                        return;
                    }
                    if (!f.isFile()) {
                        error(f + " is not a file");
                        return;
                    }

                    // I couldn't come up with a simple logic to test for a
                    // maven installation
                    // there seems to be just too much difference between m1 and
                    // m2.

                    ok();
                }
            }.process();
        }

        /**
         * Check that the ivy.xml file exist
         * 
         * @param req
         *            the Stapler request
         * @param rsp
         *            the Stapler response
         * @throws IOException
         * @throws ServletException
         */
        public void doCheckIvyFile(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            new FormFieldValidator.WorkspaceFilePath(req, rsp, true, true).process();
        }

        @Override
        public Publisher newInstance(StaplerRequest req) {
            return new IvyBuildTrigger(req.getParameter("ivy_file"), req.getParameter("ivy_conf_name"));
        }
    }

}

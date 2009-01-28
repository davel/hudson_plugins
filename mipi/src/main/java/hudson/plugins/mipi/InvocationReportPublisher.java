package hudson.plugins.mipi;

/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;
import net.sf.json.JSONObject;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Standard publisher for invocation reports as test results.
 *
 * @author connollys
 * @since Jan 28, 2009 12:09:01 PM
 */
public class InvocationReportPublisher
    extends Publisher
    implements Serializable
{

// ------------------------------ FIELDS ------------------------------

    /**
     * Ensure serializability.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Our descriptor singleton.
     */
    static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * {@link FileSet} "includes" string, like "<code>**</code><code>/invoker-reports/INVOCATION-*.xml</code>"
     */
    private final String invokerResults;

// --------------------------- CONSTRUCTORS ---------------------------

    @DataBoundConstructor
    public InvocationReportPublisher( String invokerResults )
    {
        this.invokerResults = DESCRIPTOR.applyDefaultIncludes( invokerResults );
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Gets the "includes" string for finding invocation reports.
     *
     * @return The "includes" string
     */
    public String getInvokerResults()
    {
        return invokerResults;
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Describable ---------------------

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor()
    {
        return DESCRIPTOR;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Our descriptor class.
     */
    public static class DescriptorImpl
        extends Descriptor<Publisher>
    {

        /**
         * Constructor for {@link hudson.plugins.mipi.InvocationReportPublisher.DescriptorImpl}
         */
        private DescriptorImpl()
        {
            super( InvocationReportPublisher.class );
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName()
        {
            return Messages.InvocationReportPublisher_DisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getHelpFile()
        {
            return "/plugin/mipi/help.html";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Publisher newInstance( StaplerRequest req, JSONObject formData )
            throws FormException
        {
            return req.bindJSON( InvocationReportPublisher.class, formData );
        }

        /**
         * {@inheritDoc}
         */
        public boolean isApplicable( Class<? extends AbstractProject> aClass )
        {
            return !MavenModuleSet.class.isAssignableFrom( aClass ) && !MavenModule.class.isAssignableFrom( aClass );
        }

        /**
         * Performs on-the-fly validation on the file mask wildcard.
         */
        public void doCheck( StaplerRequest req, StaplerResponse rsp )
            throws IOException, ServletException
        {
            new FormFieldValidator.WorkspaceFileMask( req, rsp ).process();
        }

        public String applyDefaultIncludes( String invokerResults )
        {
            if ( invokerResults == null || invokerResults.trim().length() == 0 )
            {
                return "**/target/invoker-reports/INVOCATION-*.xml";
            }
            else
            {
                return invokerResults.trim();
            }
        }

    }

}

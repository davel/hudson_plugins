/**
 * 
 */
package com.mtvi.plateng.hudson;

import static hudson.Util.fixEmpty;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Hudson plugin that invokes a JMX operation upon a successful build.
 * 
 * @author edelsonj
 * @plugin
 */
public class JMXInvokerPublisher extends Publisher {

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private JMXServiceURL mbeanServerUrl;
    private ObjectName objectName;
    private String operationName;
    private String password;
    private String username;

    /**
     * Create a new JMX Publisher instance, converting the mbeanServerURL and
     * objectName parameters to the appropriate JMX types
     * 
     * @throws MalformedURLException if the mbeanServerUrl isn't valid
     * @throws MalformedObjectNameException if the objectName isn't valid.
     *         {@stapler-constructor}
     */
    @DataBoundConstructor
    public JMXInvokerPublisher(String mbeanServerUrl, String objectName, String operationName,
            String username, String password) throws MalformedURLException,
            MalformedObjectNameException {
        this.mbeanServerUrl = createJMXServiceURL(mbeanServerUrl);
        this.objectName = new ObjectName(objectName);
        this.operationName = operationName;
        this.username = username;
        this.password = password;
    }

    /**
     * Create a JMXServiceURL object, converting the short <host>:<port> form
     * of a JMX Service URL to the full form if necessary.
     * 
     * @param value either a host and port or a full JMX URL
     * @return the JMX Service URL
     * @throws MalformedURLException if the URL isn't valid
     */
    private static JMXServiceURL createJMXServiceURL(String value) throws MalformedURLException {
        if (StringUtils.countMatches(value, ":") == 1) {
            value = String.format("service:jmx:rmi:///jndi/rmi://%s/jmxrmi", value);
        }

        return new JMXServiceURL(value);
    }

    /**
     * @return
     * @see hudson.model.Describable#getDescriptor()
     */
    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    public String getMbeanServerUrl() {
        return mbeanServerUrl.toString();
    }

    public String getObjectName() {
        return objectName.toString();
    }

    public String getOperationName() {
        return operationName;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        if (build.getResult() == Result.SUCCESS) {
            // JMXServiceURL url = new
            // JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(mbeanServerUrl, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            try {
                logger.println(String.format("Invoking %s on %s", operationName, objectName));
                mbsc.invoke(objectName, operationName, new Object[0], new String[0]);
            } catch (Exception e) {
                logger.println(String.format("Unable to invoke %s on MBean %s: ", operationName,
                        objectName, e.getMessage()));
                return false;
            } finally {
                jmxc.close();
            }
        }

        return true;
    }

    public static final class DescriptorImpl extends Descriptor<Publisher> {

        public DescriptorImpl() {
            super(JMXInvokerPublisher.class);
            load();
        }

        /**
         * Performs on-the-fly validation of the mbean server url field.
         */
        public void doCheckMBeanServerUrl(StaplerRequest request, StaplerResponse response)
                throws IOException, ServletException {
            new FormFieldValidator(request, response, false) {

                @Override
                protected void check() throws IOException, ServletException {
                    String value = fixEmpty(request.getParameter("value"));
                    if (value == null) {
                        error("MBean Server URL cannot be empty");
                    } else {
                        JMXServiceURL url = null;
                        try {
                            url = createJMXServiceURL(value);
                        } catch (MalformedURLException e) {
                            error("Invalid MBean Server URL - %s", e.getMessage());
                        }

                        // JMXConnector jmxc = null;
                        // try {
                        // jmxc = JMXConnectorFactory.connect(url, null);
                        // } catch (Exception e) {
                        // error("Unable to connect to MBean Server: " +
                        // e.getMessage());
                        // } finally {
                        // if (jmxc != null) {
                        // jmxc.close();
                        // }
                        // }
                    }

                }
            }.process();
        }

        /**
         * Performs on-the-fly validation of the object name field.
         */
        public void doCheckObjectName(StaplerRequest request, StaplerResponse response)
                throws IOException, ServletException {
            new FormFieldValidator(request, response, false) {

                @Override
                protected void check() throws IOException, ServletException {
                    String value = fixEmpty(request.getParameter("value"));
                    if (value == null) {
                        error("Object Name cannot be empty");
                    } else {
                        try {
                            new ObjectName(value);
                        } catch (Exception e) {
                            error("Invalid object name: %s", e.getMessage());
                        }
                    }

                }
            }.process();
        }

        /**
         * Performs on-the-fly validation of the operation name field.
         */
        public void doCheckOperationName(StaplerRequest request, StaplerResponse response)
                throws IOException, ServletException {
            new FormFieldValidator(request, response, false) {

                @Override
                protected void check() throws IOException, ServletException {
                    String value = fixEmpty(request.getParameter("value"));
                    if (value == null) {
                        error("Operation Name cannot be empty");
                    }

                }
            }.process();
        }

        @Override
        public String getDisplayName() {
            return "Invoke a JMX Operation";
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(clazz, formData);
        }
    }

}

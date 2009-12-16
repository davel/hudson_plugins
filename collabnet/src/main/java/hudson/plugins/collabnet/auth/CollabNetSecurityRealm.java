package hudson.plugins.collabnet.auth;

import groovy.lang.Binding;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.collabnet.util.CNHudsonUtil;
import hudson.security.SecurityRealm;
import hudson.util.FormValidation;
import hudson.util.spring.BeanBuilder;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.web.context.WebApplicationContext;

import com.collabnet.ce.webservices.CollabNetApp;


public class CollabNetSecurityRealm extends SecurityRealm {
    private String collabNetUrl;

    /* viewing hudson page from CTF linked app should login to hudson */
    private boolean mEnableSSOAuthFromCTF;

    /* logging in to hudson should login to CTF */
    private boolean mEnableSSOAuthToCTF;

    public CollabNetSecurityRealm(String collabNetUrl, Boolean enableAuthFromCTF, Boolean enableAuthToCTF) {
        this.collabNetUrl = CNHudsonUtil.sanitizeCollabNetUrl(collabNetUrl);
        this.mEnableSSOAuthFromCTF = Boolean.TRUE.equals(enableAuthFromCTF);
        this.mEnableSSOAuthToCTF = Boolean.TRUE.equals(enableAuthToCTF);
    }

    public String getCollabNetUrl() {
        return this.collabNetUrl;
    }

    /**
     * Single sign on preference governing making hudson read CTF's SSO token
     * @return true to enable
     */
    public boolean getEnableSSOAuthFromCTF() {
        return mEnableSSOAuthFromCTF;
    }

    /**
     * Single sign on preference governing making hudson login to CTF upon authenticating
     * @return true to enable
     */
    public boolean getEnableSSOAuthToCTF() {
        return mEnableSSOAuthToCTF;
    }

    @Override
    public SecurityRealm.SecurityComponents createSecurityComponents() {
        return new SecurityRealm.SecurityComponents(new CollabNetAuthManager
                                                    (this.getCollabNetUrl()));
    }

    /**
     * Override the default createFilter.  We want to use one that does not
     * return a 403 on login redirect because that may cause problems when
     * Hudson is run behind a proxy.
     */
    @Override
    public Filter createFilter(FilterConfig filterConfig) {
        Binding binding = new Binding();
        SecurityComponents sc = this.createSecurityComponents();
        binding.setVariable("securityComponents", sc);
        BeanBuilder builder = new BeanBuilder(getClass().getClassLoader());
        builder.parse(getClass().
                      getResourceAsStream("CNSecurityFilters.groovy"),binding);
        WebApplicationContext context = builder.createApplicationContext();
        return (Filter) context.getBean("filter");
    }

    /**
     * The CollabNetSecurityRealm Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
        public DescriptorImpl() {
            super(CollabNetSecurityRealm.class);
        }

        /**
         * @return string to display for configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "CollabNet Security Realm";
        }

        /**
         * @return the url for the help files.
         */
        public static String getHelpUrl() {
            return "/plugin/collabnet/auth/";
        }

        /**
         * @return the path to the help file.
         */
        @Override
        public String getHelpFile() {
            return getHelpUrl() + "help-securityRealm.html";
        }

        /**
         * @param req config page parameters.
         * @return new CollabNetSecurityRealm object, instantiated from the 
         *         configuration form vars.
         * @throws FormException
         */
        @Override
        public CollabNetSecurityRealm newInstance(StaplerRequest req, 
                                                  JSONObject formData) 
            throws FormException {
            return new CollabNetSecurityRealm(
                (String)formData.get("collabneturl"),
                (Boolean)formData.get("enablessofrom"),
                (Boolean)formData.get("enablessoto"));
        }

        /**
         * Form validation for the CollabNet URL.
         *
         * @param value url
         */
        public FormValidation doCollabNetUrlCheck(@QueryParameter String value) {
            if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) return FormValidation.ok();
            String collabNetUrl = value;
            if (collabNetUrl == null || collabNetUrl.equals("")) {
                return FormValidation.error("The CollabNet URL is required.");
            }
            if (!checkSoapUrl(collabNetUrl)) {
                return FormValidation.error("Invalid CollabNet URL.");
            }
            return FormValidation.ok();
        }
        
        /**
         * Check that a URL has the expected SOAP service.
         *
         * @param collabNetUrl for the CollabNet server
         * @return returns true if we can get a wsdl from the url, which
         *         indicates that it's a working CollabNet server.
         */
        private boolean checkSoapUrl(String collabNetUrl) {
            String soapURL = collabNetUrl + CollabNetApp.SOAP_SERVICE + 
                "CollabNet?wsdl";
            HttpClient client = new HttpClient();
            try {
                GetMethod get = new GetMethod(soapURL);
                int status = client.executeMethod(get);
                if (status == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            } catch (IllegalArgumentException iae) {
                return false;
            }
        }    
    }
}

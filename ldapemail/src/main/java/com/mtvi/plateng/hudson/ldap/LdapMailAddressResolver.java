/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.model.User;
import hudson.tasks.MailAddressResolver;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Implementation of hudson.tasks.MailAddressResolver that looks up the email
 * address for a user based on information in an LDAP directory.
 * 
 * @author justinedelson
 * 
 */
public class LdapMailAddressResolver extends MailAddressResolver {

    /**
     * A logger object.
     */
    private static final Logger LOGGER = Logger.getLogger("hudson."
            + LdapMailAddressResolver.class.getName());

    /**
     * Configuration object encapsulating how to connect to the LDAP server.
     */
    private Configuration configuration;

    /**
     * Build an instance wrapping a Configuration object.
     * 
     * @param config the Configuration object
     */
    public LdapMailAddressResolver(Configuration config) {
        configuration = config;
        if (!configuration.isValid()) {
            LOGGER.warning("Provided configuration isn't valid. Check for missing elements.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String findMailAddressFor(User user) {
        return findMailAddressFor(user.getDisplayName());
    }

    /**
     * Look up the email address for a user in the directory.
     * 
     * @param userName the user's username, generally corresponds to the uid
     *        attribute.
     * @return the corresponding email address from the directory, or null if
     *         none can be found.
     */
    protected String findMailAddressFor(String userName) {
        if (configuration.isValid()) {
            try {
                Hashtable<String, String> env = new Hashtable<String, String>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, configuration
                        .getInitialContextFactoryName());
                env.put(Context.PROVIDER_URL, configuration.getServer());

                if (configuration.isBindCredentialsProvided()) {
                    LOGGER.log(Level.INFO, "Using provided credentials for binding to LDAP server");
                    env.put(Context.SECURITY_PRINCIPAL, configuration.getBindDN());
                    env.put(Context.SECURITY_CREDENTIALS, configuration.getBindPassword());
                }
                DirContext ctx = new InitialDirContext(env);

                if (configuration.isPerformSearch()) {
                    return performSearch(ctx, userName);
                } else {
                    return performLookup(ctx, userName);
                }
            } catch (NamingException e) {
                LOGGER.log(Level.SEVERE, "Unable to run LDAP query", e);
            }
        }
        return null;
    }

    private String performLookup(DirContext ctx, String userName) throws NamingException {
        String emailAddress = null;
        String dn = configuration.makeUserDN(userName);

        LOGGER.log(Level.INFO, String.format("Looking up attributes for DN %s", dn));

        Attributes attrs = ctx
                .getAttributes(dn, new String[] { configuration.getEmailAttribute() });
        Attribute attr = attrs.get(configuration.getEmailAttribute());
        if (attr != null) {
            emailAddress = (String) attr.get();
            LOGGER.log(Level.INFO, String.format("Found mail attribute %s for userName %s",
                    emailAddress, userName));
        } else {
            LOGGER.log(Level.INFO, String.format("No mail attribute found for userName %s",
                    userName));
        }
        return emailAddress;
    }

    private String performSearch(DirContext ctx, String userName) throws NamingException {
        String emailAddress = null;
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String filter = String.format("%s=%s", configuration.getSearchAttribute(), userName);

        LOGGER.log(Level.INFO, String.format("Performing LDAP search within %s using %s",
                configuration.getBaseDN(), filter));

        NamingEnumeration<SearchResult> results = ctx.search(configuration.getBaseDN(), filter,
                controls);
        if (results.hasMore()) {
            SearchResult result = results.next();
            Attribute attr = result.getAttributes().get(configuration.getEmailAttribute());
            if (attr != null) {
                emailAddress = (String) attr.get();
                LOGGER.log(Level.INFO, String.format("Found mail attribute %s for userName %s",
                        emailAddress, userName));
            } else {
                LOGGER.log(Level.INFO, String.format("No mail attribute found for userName %s",
                        userName));
            }
        } else {
            LOGGER.log(Level.INFO, String.format("No results found for filter %s inside baseDN %s",
                    filter, configuration.getBaseDN()));
        }
        return emailAddress;
    }
}

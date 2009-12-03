package hudson.plugins.collabnet.util;

import com.collabnet.ce.webservices.CollabNetApp;
import com.collabnet.ce.webservices.DocumentApp;

import com.collabnet.cubit.api.CubitConnector;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import hudson.plugins.collabnet.auth.CNConnection;
import hudson.util.FormValidation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.axis.utils.StringUtils;

import org.kohsuke.stapler.StaplerRequest;

public abstract class CNFormFieldValidator {
    private static Logger log = Logger.getLogger("CNFormFieldValidator");

        // no special permisssion is required for our checks
        // without proper rights, no data will be returned from 
        // CollabNet server anyhow.

    /**
     * Utility function to check that a str contains only valid
     * enviromental variables to interpret.
     *
     * @param str the string to test.
     * @return error message, if any variables are missing, null if all
     *         are found.
     */
    public static String checkInterpretedString(String str) {
        Pattern envPat = Pattern.compile("\\$\\{(\\w*)\\}");
        Matcher matcher = envPat.matcher(str);
        Set<String> envVars = new HashSet<String>(9);
        envVars.add("BUILD_NUMBER");
        envVars.add("BUILD_ID");
        envVars.add("JOB_NAME");
        envVars.add("BUILD_TAG");
        envVars.add("EXECUTOR_NUMBER");
        envVars.add("JAVA_HOME");
        envVars.add("WORKSPACE");
        envVars.add("HUDSON_URL");
        envVars.add("SVN_REVISION");
        envVars.add("CVS_BRANCH");
        String message = null;
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!envVars.contains(key)) {
                if (message == null) {
                    message = "Environmental Variables not found: " + key;
                } else {
                    message += ", " + key;
                }
            }
        }
        return message;
    }

    /**
     * Returns true if a url is valid, false otherwise.
     */
    public static boolean checkUrl(String url) {
        HttpClient client = new HttpClient();
        try {
            GetMethod get = new GetMethod(url);
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

    /**
     * Class for checking that a required value is set.  Expects a 
     * StaplerRequest with a value set to the value and a name set to
     * the name of what is being set (used for error msg).
     */
    public static FormValidation requiredCheck(String value, String name) {
        value = StringUtils.strip(value);
        if (CommonUtil.unset(name)) {
            // ideally this should be set
            name = "above value";
        }
        if (CommonUtil.unset(value)) {
            return FormValidation.error("The " + name + " is required.");
        }
        return FormValidation.ok();
    }

    /**
     * Class for checking an unrequired value that may include
     * interpreted strings (i.e. Hudson environmental values). Expects a 
     * StaplerRequest with value.  If it's a required value, expects a 
     * value name.
     */
    public static FormValidation interpretedCheck(String str, String name, boolean isRequired) {
        if (CommonUtil.unset(str)) {
            if (!isRequired) {
                return FormValidation.ok();
            } else {
                if (CommonUtil.unset(name)) {
                    // ideally this should be set
                    name = "above value";
                }
                return FormValidation.error("The " + name + " is required.");
            }
        }
        String errmsg;
        if ((errmsg = checkInterpretedString(str)) != null) {
            return FormValidation.error(errmsg);
        }

        return FormValidation.ok();
    }

    /**
     * Class for checking an interpreted string which is unrequired.
     */
    public static FormValidation unrequiredInterpretedCheck(String str, String name) {
        return interpretedCheck(str, name, false);
    }

    /**
     * Class for checking an interpreted string which is required.
     */
    public static FormValidation requiredInterpretedCheck(String str, String name) {
        return interpretedCheck(str, name, true);
    }

    /**
     * Class for checking if a Host URL is correct.  Expects a StaplerRequest 
     * with value set to the url.
     */
    public static FormValidation hostUrlCheck(String hostUrl) {
        if (CommonUtil.unset(hostUrl)) {
            return FormValidation.error("The Host URL is required.");
        }
        Protocol acceptAllSsl =
            new Protocol("https",
                         (ProtocolSocketFactory)
                         new EasySSLProtocolSocketFactory(),
                         443);
        Protocol.registerProtocol("https", acceptAllSsl);
        if (!checkUrl(hostUrl)) {
            return FormValidation.error("Invalid Host URL.");
        }
        return FormValidation.ok();
    }

    /**
     * Class for checking if a URL is correct and corresponds to a 
     * CollabNet server.  Expects a StaplerRequest with value set
     * to the url.
     */
    public static FormValidation soapUrlCheck(String collabNetUrl) {
        if (CommonUtil.unset(collabNetUrl)) {
            return FormValidation.error("The CollabNet TeamForge URL is required.");
        }
        if (!checkSoapUrl(collabNetUrl)) {
            return FormValidation.error("Invalid CollabNet TeamForge URL.");
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
    private static boolean checkSoapUrl(String collabNetUrl) {
        String soapURL = collabNetUrl + CollabNetApp.SOAP_SERVICE +
            "CollabNet?wsdl";
        return checkUrl(soapURL);
    }

    /**
     * Class for checking that a login to CollabNet is valid.  Expects
     * a StaplerRequest with url, username, and password set.
     */
    public static FormValidation loginCheck(StaplerRequest request) {
        String password = request.getParameter("password");
        if (CommonUtil.unset(password)) {
            return FormValidation.error("The password is required.");
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        if (cna == null) {
            return FormValidation.warning("Login fails with this CollabNet " +
                    "URL/username/password combination.");
        } else {
            CNHudsonUtil.logoff(cna);
        }
        return FormValidation.ok();
    }
    
    /**
     * Class for checking that a project name is valid.  Expects a 
     * StaplerRequest with url, username, password, and project set.
     */
    public static FormValidation projectCheck(StaplerRequest request) {
        String username = CNHudsonUtil.getUsername(request);
        String project = request.getParameter("project");

        if (CommonUtil.unset(project)) {
            return FormValidation.error("The project is required.");
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        if (cna != null) {
            if (CNHudsonUtil.getProjectId(cna, project) == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("This project cannot be found, or user " +
                        username + " does not have permission " +
                        "to access it.");
            }
            CNHudsonUtil.logoff(cna);
        }
        return FormValidation.ok();
    }

    /**
     * Class to check that the path to a document exists.  Warns about 
     * any missing folders.  Expects a StaplerRequest with url, username,
     * password, project, and path.
     */
    public static FormValidation documentPathCheck(StaplerRequest request) throws IOException {
        String project = request.getParameter("project");
        String path = request.getParameter("path");
        path = path.replaceAll("/+", "/");
        path = CommonUtil.stripSlashes(path);
        if (CommonUtil.unset(path)) {
            return FormValidation.error("The path is required.");
        }
        String errmsg = null;
        if ((errmsg = checkInterpretedString(path)) != null) {
            return FormValidation.error(errmsg);
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        if (projectId != null) {
            DocumentApp da = new DocumentApp(cna);
            String missing = da.verifyPath(projectId, path);
            if (missing != null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Folder '" + missing + "' could not be " +
                        "found in path '" + path + "'.  It (and " +
                        "any subfolders) will " +
                        "be created dynamically.");
            }
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    /**
     * Class to check that a package exists.  Expects a StaplerRequest with 
     * a url, username, password, project, and package.
     */
    public static FormValidation packageCheck(StaplerRequest request) {
        String rpackage = request.getParameter("package");
        String project = request.getParameter("project");
        if (CommonUtil.unset(rpackage)) {
            return FormValidation.error("The package is required.");
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        if (projectId != null) {
            String packageId = CNHudsonUtil.getPackageId(cna, rpackage,
                                                         projectId);
            if (packageId == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Package could not be found.");
            }
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    /**
     * Class to check that a release exists.  Expects a StaplerRequest with 
     * a url, username, password, project, package (optional), and release.
     */
    public static FormValidation releaseCheck(StaplerRequest request) {
        String release = request.getParameter("release");
        String rpackage = request.getParameter("package");
        String project = request.getParameter("project");
        String required = request.getParameter("required");
        if (CommonUtil.unset(release)) {
            if (required.toLowerCase().equals("true")) {
                return FormValidation.error("The release is required.");
            } else {
                return FormValidation.ok();
            }
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        String packageId = CNHudsonUtil.getPackageId(cna, rpackage,
                                                     projectId);
        if (packageId != null) {
            String releaseId = CNHudsonUtil.getReleaseId(cna, packageId,
                                                         release);
            if (releaseId == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Release could not be found.");
            }
        } else if (projectId != null) {
            String releaseId = CNHudsonUtil.getProjectReleaseId(cna,
                                                                projectId,
                                                                release);
            if (releaseId == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Release could not be found.");
            }
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    /**
     * Class to check that a repo exists.  Expects a StaplerRequest with 
     * a url, username, password, and project.
     */
    public static FormValidation repoCheck(StaplerRequest request) {
        String project = request.getParameter("project");
        String repoName = request.getParameter("repo");
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        if (CommonUtil.unset(repoName)) {
            return FormValidation.error("The repository name is required.");
        }
        if (projectId != null) {
            String repoId = CNHudsonUtil.getRepoId(cna, projectId, repoName);
            if (repoId == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Repository could not be " +
                                              "found.");
            }
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    /**
     * Class to check that a tracker exists.  Expects a StaplerRequest with 
     * a url, username, password, project, and tracker.
     */
    public static FormValidation trackerCheck(StaplerRequest request) {
        String tracker = request.getParameter("tracker");
        String project = request.getParameter("project");
        if (CommonUtil.unset(tracker)) {
            return FormValidation.error("The tracker is required.");
        }
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        if (projectId != null) {
            String trackerId = CNHudsonUtil.getTrackerId(cna, projectId,
                                                         tracker);
            if (trackerId == null) {
                CNHudsonUtil.logoff(cna);
                return FormValidation.warning("Tracker could not be found.");
            }
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    /**
     * Class for checking if a user can be assigned a tracker artifact.  
     * Expects a StaplerRequest with login info (url, username, password), 
     * project, and assign (which is the username).
     */
    public static FormValidation assignCheck(StaplerRequest request) {
        String assign = StringUtils.strip(request.getParameter("assign"));
        if (CommonUtil.unset(assign)) {
            return FormValidation.ok();
        }
        String project = request.getParameter("project");
        CollabNetApp cna = CNHudsonUtil.getCollabNetApp(request);
        if (cna == null) {
            return FormValidation.ok();
        }
        String projectId = CNHudsonUtil.getProjectId(cna, project);
        if (projectId == null) {
            CNHudsonUtil.logoff(cna);
            return FormValidation.ok();
        }
        if (!CNHudsonUtil.isUserMember(cna, assign, projectId)) {
            CNHudsonUtil.logoff(cna);
            return FormValidation.warning("This user is not a member of the " +
                    "project.");
        }
        CNHudsonUtil.logoff(cna);
        return FormValidation.ok();
    }

    
    /**
     * Check that a comma-separated list of users exists.
     * The check only works for a logged-in site-admin.  Otherwise,
     * give a warning that we cannot check the users' validity.
     */
    public static FormValidation userListCheck(String userStr) {
        if (userStr == null || userStr.equals("")) {
            return FormValidation.ok();
        }
        CNConnection conn = CNConnection.getInstance();
        if (conn == null || !conn.isSuperUser()) {
            return FormValidation.warning("Cannot check if users exist unless logged " +
                    "in as a TeamForge site admin.  Be careful!");
        }
        Collection<String> invalidUsers = getInvalidUsers(conn, userStr);
        if (!invalidUsers.isEmpty()) {
            return FormValidation.error("The following users do not exist: " +
                  invalidUsers);
        }
        return FormValidation.ok();
    }

    /**
     * @param conn
     * @param userStr
     * @return the collection of users from the array which do not exist.
     */
    private static Collection<String> getInvalidUsers(CNConnection conn, String userStr) {
        String[] users = CommonUtil.splitCommaStr(userStr);
        Collection<String> invalidUsers = new ArrayList<String>();
        for (String user: users) {
            if (!conn.isUsernameValid(user)) {
                invalidUsers.add(user);
            }
        }
        return invalidUsers;
    }

    /**
     * Check that a comma-separated list of groups exists.
     * The check only works for a logged-in site-admin.  Also warns
     * the current user if s/he will be locked out once that user
     * saves the configuration.
     */
    public static FormValidation groupListCheck(String groupStr, String userStr) {
        Collection<String> invalidGroups = getInvalidGroups(groupStr);
        if (!invalidGroups.isEmpty()) {
            return FormValidation.error("The following groups do not exist: " +
                  invalidGroups);
            // anyone who can see if groups are invalid will
            // never be locked out, so we can return here
        }

        if (userStr != null) {
            if (locksOutCurrentUser(userStr, groupStr)) {
                return FormValidation.error("The authorization settings would lock " +
                      "the current user out of this page.  " +
                      "You may want to add your username to " +
                      "the user list.");
            }
        }

        return FormValidation.ok();
    }

    /**
     * @param groupStr
     * @return the collection of groups from the array which do not exist.
     */
    private static Collection<String> getInvalidGroups(String groupStr) {
        CNConnection conn = CNConnection.getInstance();
        if (conn == null) {
            // cannot connect to check.
            return Collections.emptyList();
        }
        if (!conn.isSuperUser()) {
            // only super users can see all groups and do this check.
            return Collections.emptyList();
        }
        String[] groups = CommonUtil.splitCommaStr(groupStr);
        Collection<String> invalidGroups = new ArrayList<String>();
        for (String group: groups) {
            if (!conn.isGroupnameValid(group)) {
                invalidGroups.add(group);
            }
        }
        return invalidGroups;
    }

    /**
     * Return true if the given admin user/groups would mean that
     * the current user would be locked out of the system.
     *
     * @param userStr
     * @param groupStr
     * @return true if the user would not have admin access with these
     *         authorizations.
     */
    private static boolean locksOutCurrentUser(String userStr, String groupStr) {
        CNConnection conn = CNConnection.getInstance();
        if (conn == null) {
            // cannot check
            return false;
        }
        if (conn.isSuperUser()) {
            return false;
        }
        String currentUser = conn.getUsername();
        String[] users = CommonUtil.splitCommaStr(userStr);
        for (String user: users) {
            if (user.equals(currentUser)) {
                return false;
            }
        }
        String[] groups = CommonUtil.splitCommaStr(groupStr);
        if (conn.isMemberOfAny(Arrays.asList(groups))) {
            return false;
        }
        return true;
    }


    /**
     * Class to check for validity of a regex expression.  Expects
     * a StaplerRequest with value set.  
     */
    public static FormValidation regexCheck(String regex) {
        if(!CommonUtil.unset(regex)) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException ex){
                return FormValidation.error("The regular expression is not syntactically "
                      + "correct.");
            }
        }
        return FormValidation.ok();
    }


    /**
     * Class to check if a CUBiT key has the proper format and allows
     * login.  Expects a StaplerRequest with value (key), hostURL, and user
     * set.
     */
    public static FormValidation cubitKeyCheck(StaplerRequest request) {
        String key = request.getParameter("value");
        String hostURL = request.getParameter("hostURL");
        String user = request.getParameter("user");
        if (CommonUtil.unset(key)) {
            return FormValidation.error("The user API key is required.");
        }
        if (!key.matches("\\p{XDigit}{8}-\\p{XDigit}{4}"
                         + "-\\p{XDigit}{4}-\\p{XDigit}{4}"
                         + "-\\p{XDigit}{12}")) {
            if (key.startsWith(" ")) {
                return FormValidation.error("The key's format is invalid.  "
                      + "There is a leading space.");
            } else if (key.endsWith(" ")) {
                return FormValidation.error("The key's format is invalid.  "
                      + "There is a trailing space.");
            } else {
                return FormValidation.error("The key's format is invalid.");
            }
        }
        if (!CommonUtil.unset(hostURL) && !CommonUtil.unset(user)) {
            boolean success = false;
            try {
                success = signedStatus(hostURL, user, key);
            } catch (IllegalArgumentException iae) {
                // failure
                success = false;
            }
            if (!success) {
                return FormValidation.warning("This host URL, username, and user API "
                        + "key combination cannot successfully "
                        + "sign in.");
            }
        }
        return FormValidation.ok();
    }

    /**
     * Utility function to check that host, user, and key work.
     *
     * @param host URL.
     * @param user to login as.
     * @param key to login with.
     * @return true if the status is good.
     */
    private static boolean signedStatus(String host, String user, String key) {
        key = key.toLowerCase();
        CubitConnector conn = new CubitConnector(host, user, key);
        String status;
        try {
            status = conn.callCubitApi("status_signed",
                                          new HashMap<String, String>(),
                                          true);
        } catch (IOException e) {
            return false;
        }
        Pattern pat = Pattern.compile(".*OK.*", Pattern.DOTALL);
        if (pat.matcher(status).matches()) {
            return true;
        } else {
            return false;
        }
    }
}

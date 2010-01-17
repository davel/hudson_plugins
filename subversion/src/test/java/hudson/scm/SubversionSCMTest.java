/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Bruce Chapman, Yahoo! Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.scm;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.slaves.DumbSlave;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.scm.SubversionSCM.ModuleLocation;
import static hudson.scm.SubversionSCM.compareSVNAuthentications;
import hudson.scm.SubversionSCM.DescriptorImpl;
import hudson.scm.browsers.Sventon;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.Url;
import org.jvnet.hudson.test.recipes.PresetData;
import static org.jvnet.hudson.test.recipes.PresetData.DataSet.ANONYMOUS_READONLY;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.auth.SVNUserNameAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Kohsuke Kawaguchi
 */
public class SubversionSCMTest extends HudsonTestCase {
	
    private static final int LOG_LIMIT = 1000;
    private DescriptorImpl descriptor;

    // in some tests we play authentication games with this repo
    String realm = "<https://hudson.dev.java.net:443> CollabNet Subversion Repository";
    String kind = ISVNAuthenticationManager.PASSWORD;
    SVNURL repo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        descriptor = hudson.getDescriptorByType(DescriptorImpl.class);
        repo = SVNURL.parseURIDecoded("https://hudson.dev.java.net/svn/hudson");
    }

    /**
     * Sets guest credentials to access java.net Subversion repo.
     */
    protected void setJavaNetCredential() throws SVNException, IOException {
        // set the credential to access svn.dev.java.net
        descriptor.postCredential("https://svn.dev.java.net/svn/hudson/","guest","",null,new PrintWriter(new NullStream()));
    }

    @PresetData(ANONYMOUS_READONLY)
    @Bug(2380)
    public void testTaggingPermission() throws Exception {
        // create a build
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(loadSvnRepo());
        FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause()).get();
        System.out.println(b.getLog(LOG_LIMIT));
        assertBuildStatus(Result.SUCCESS,b);

        SubversionTagAction action = b.getAction(SubversionTagAction.class);
        assertFalse(b.hasPermission(action.getPermission()));

        WebClient wc = new WebClient();
        HtmlPage html = wc.getPage(b);

        // make sure there's no link to the 'tag this build'
        Document dom = new DOMReader().read(html);
        assertNull(dom.selectSingleNode("//A[text()='Tag this build']"));
        for( HtmlAnchor a : html.getAnchors() )
            assertFalse(a.getHrefAttribute().contains("/tagBuild/"));

        // and no tag form on tagBuild page
        html = wc.getPage(b,"tagBuild/");
        try {
            html.getFormByName("tag");
            fail("should not have been found");
        } catch (ElementNotFoundException e) {
        }

        // and that tagging would fail
        try {
            wc.getPage(b,"tagBuild/submit?name0=test&Submit=Tag");
            fail("should have been denied");
        } catch (FailingHttpStatusCodeException e) {
            // make sure the request is denied
            assertEquals(e.getResponse().getStatusCode(),403);
        }

        // now login as alice and make sure that the tagging would succeed
        wc = new WebClient();
        wc.login("alice","alice");
        html = wc.getPage(b,"tagBuild/");
        HtmlForm form = html.getFormByName("tag");
        submit(form);
    }

    /**
     * Loads a test Subversion repository into a temporary directory, and creates {@link SubversionSCM} for it.
     */
    private SubversionSCM loadSvnRepo() throws Exception {
        return new SubversionSCM("file://" + new CopyExisting(getClass().getResource("/svn-repo.zip")).allocate().toURI().toURL().getPath() + "trunk/a","a");
    }

    @Email("http://www.nabble.com/Hudson-1.266-and-1.267%3A-Subversion-authentication-broken--td21156950.html")
    public void testHttpsCheckOut() throws Exception {
        setJavaNetCredential();
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new SubversionSCM("https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/trivial-ant"));

        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0, new Cause.UserCause()).get());
        assertTrue(b.getWorkspace().child("build.xml").exists());
    }

    @Email("http://www.nabble.com/Hudson-1.266-and-1.267%3A-Subversion-authentication-broken--td21156950.html")
    public void testHttpCheckOut() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new SubversionSCM("http://svn.codehaus.org/plexus/tags/JASF_INIT/plexus-avalon-components/jasf/"));

        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0, new Cause.UserCause()).get());
        assertTrue(b.getWorkspace().child("maven.xml").exists());
    }

    @Url("http://hudson.pastebin.com/m3ea34eea")
    public void testRemoteCheckOut() throws Exception {
        setJavaNetCredential();
        DumbSlave s = createSlave();
        FreeStyleProject p = createFreeStyleProject();
        p.setAssignedLabel(s.getSelfLabel());
        p.setScm(new SubversionSCM("https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/trivial-ant"));

        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0, new Cause.UserCause()).get());
        assertTrue(b.getWorkspace().child("build.xml").exists());
        b = assertBuildStatusSuccess(p.scheduleBuild2(0).get());
    }

    /**
     * Tests the "URL@REV" format in SVN URL.
     */
    @Bug(262)
    public void testRevisionedCheckout() throws Exception {
        setJavaNetCredential();
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new SubversionSCM("https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/trivial-ant@13000"));

        FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause()).get();
        System.out.println(b.getLog(LOG_LIMIT));
        assertTrue(b.getLog(LOG_LIMIT).contains("At revision 13000"));
        assertBuildStatus(Result.SUCCESS,b);

        b = p.scheduleBuild2(0, new Cause.UserCause()).get();
        System.out.println(b.getLog(LOG_LIMIT));
        assertTrue(b.getLog(LOG_LIMIT).contains("At revision 13000"));
        assertBuildStatus(Result.SUCCESS,b);
    }

    /**
     * Tests a checkout with RevisionParameterAction
     */
    public void testRevisionParameter() throws Exception {
        setJavaNetCredential();
        FreeStyleProject p = createFreeStyleProject();
        String url = "https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/trivial-ant";
		p.setScm(new SubversionSCM(url));

        FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), 
        		new RevisionParameterAction(new SubversionSCM.SvnInfo(url, 13000))).get();
        System.out.println(b.getLog(LOG_LIMIT));
        assertTrue(b.getLog(LOG_LIMIT).contains("At revision 13000"));
        assertBuildStatus(Result.SUCCESS,b);
    }

    /**
     * {@link SubversionSCM#pollChanges(AbstractProject , Launcher , FilePath, TaskListener)} should notice
     * if the workspace and the current configuration is inconsistent and schedule a new build.
     */
    @Email("http://www.nabble.com/Proper-way-to-switch---relocate-SVN-tree---tt21173306.html")
    public void testPollingAfterRelocation() throws Exception {
        // fetch the current workspace
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(loadSvnRepo());
        p.scheduleBuild2(0, new Cause.UserCause()).get();

        // as a baseline, this shouldn't detect any change
        TaskListener listener = createTaskListener();
        assertFalse(p.pollSCMChanges(listener));

        // now switch the repository to a new one.
        // this time the polling should indicate that we need a new build
        p.setScm(loadSvnRepo());
        assertTrue(p.pollSCMChanges(listener));

        // build it once again to switch
        p.scheduleBuild2(0, new Cause.UserCause()).get();

        // then no more change should be detected
        assertFalse(p.pollSCMChanges(listener));
    }

    public void testURLWithVariable() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        String url = "http://svn.codehaus.org/plexus/tags/JASF_INIT/plexus-avalon-components/jasf/";
        p.setScm(new SubversionSCM("$REPO" + url.substring(10)));

        String var = url.substring(0, 10);

        FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(),
                new ParametersAction(new StringParameterValue("REPO", var))).get();
        System.out.println(b.getLog(LOG_LIMIT));
        assertBuildStatus(Result.SUCCESS,b);
        assertTrue(b.getWorkspace().child("maven.xml").exists());
    }

    /**
     * Test that multiple repository URLs are all polled.
     */
    @Bug(3168)
    public void testPollMultipleRepositories() throws Exception {
        // fetch the current workspaces
        FreeStyleProject p = createFreeStyleProject();
        String svnBase = "file://" + new CopyExisting(getClass().getResource("/svn-repo.zip")).allocate().toURI().toURL().getPath();
        p.setScm(new SubversionSCM(
        		Arrays.asList(new ModuleLocation(svnBase + "trunk/a", null), new ModuleLocation(svnBase + "branches", null)), 
        		false, null, null, null, null, null));
        FreeStyleBuild build = p.scheduleBuild2(0, new Cause.UserCause()).get();

        // as a baseline, this shouldn't detect any change
        TaskListener listener = createTaskListener();
        assertFalse(p.pollSCMChanges(listener));

        // Force older "current revision" for each repository, make sure both are detected
        Map<String,Long> revInfo = SubversionSCM.parseRevisionFile(build);
        for (String outOfDateItem : new String[] { "branches", "trunk/a" }) {
            FileWriter out = new FileWriter(SubversionSCM.getRevisionFile(build));
            for (Map.Entry<String,Long> entry : revInfo.entrySet()) {
                out.write(entry.getKey());
                out.write('/');
                out.write(Long.toString(
                    entry.getValue().longValue() - (entry.getKey().endsWith(outOfDateItem) ? 1 : 0)));
                out.write('\n');
            }
            out.close();

            // now the polling should indicate that we need a new build
            assertTrue("change was not detected!", p.pollSCMChanges(listener));
        }
    }

    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject p = createFreeStyleProject();

        SubversionSCM scm = new SubversionSCM(
                Arrays.asList(new ModuleLocation("a","c"),new ModuleLocation("b","d")),
                true,new Sventon(new URL("http://www.sun.com/"),"test"),"exclude","user","revprop","excludeMessage");
        p.setScm(scm);
        submit(new WebClient().getPage(p,"configure").getFormByName("config"));
        verify(scm,(SubversionSCM)p.getScm());

        scm = new SubversionSCM(Arrays.asList(new ModuleLocation("a","c")),false,null,"","","","");
        p.setScm(scm);
        submit(new WebClient().getPage(p,"configure").getFormByName("config"));
        verify(scm,(SubversionSCM)p.getScm());
    }

    private void verify(SubversionSCM lhs, SubversionSCM rhs) {
        ModuleLocation[] ll = lhs.getLocations();
        ModuleLocation[] rl = rhs.getLocations();
        assertEquals(ll.length, rl.length);
        for(int i=0; i<ll.length; i++) {
            assertEquals(ll[i].getLocalDir(), rl[i].getLocalDir());
            assertEquals(ll[i].remote, rl[i].remote);
        }

        assertEquals(lhs.isUseUpdate(), rhs.isUseUpdate());
        assertEquals(lhs.getExcludedRegions(), rhs.getExcludedRegions());
        assertEquals(lhs.getExcludedUsers(), rhs.getExcludedUsers());
        assertEquals(lhs.getExcludedRevprop(), rhs.getExcludedRevprop());
        assertEquals(lhs.getExcludedCommitMessages(), rhs.getExcludedCommitMessages());
    }

    public void test1() {
        check("http://foobar/");
        check("https://foobar/");
        check("file://foobar/");
        check("svn://foobar/");
        check("svn+ssh://foobar/");
    }

    public void test2() {
        String[] r = "abc\\ def ghi".split("(?<!\\\\)[ \\r\\n]+");
        for (int i = 0; i < r.length; i++) {
            r[i] = r[i].replaceAll("\\\\ "," ");
        }
        System.out.println(Arrays.asList(r));
        assertEquals(r.length,2);
    }

    private void check(String url) {
        assertTrue(SubversionSCM.URL_PATTERN.matcher(url).matches());
    }

    /**
     * Makes sure that Subversion doesn't check out workspace in 1.6
     */
    @Email("http://www.nabble.com/SVN-1.6-td24081571.html")
    public void testWorkspaceVersion() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(loadSvnRepo());
        FreeStyleBuild b = p.scheduleBuild2(0).get();

        SVNClientManager wc = SubversionSCM.createSvnClientManager();
        SVNStatus st = wc.getStatusClient().doStatus(new File(b.getWorkspace().getRemote()+"/a"), false);
        int wcf = st.getWorkingCopyFormat();
        System.out.println(wcf);
        assertEquals(SVNAdminAreaFactory.WC_FORMAT_14,wcf);
    }

    private static String readFileAsString(File file)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }

    /**
     * Makes sure the symbolic link is checked out correctly. There seems to be 
     */
    @Bug(3904)
    public void testSymbolicLinkCheckout() throws Exception {
        setJavaNetCredential();
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new SubversionSCM("https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/issue-3904"));

        FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause()).get();
        File source = new File(b.getWorkspace().getRemote() + "/readme.txt");
        File linked = new File(b.getWorkspace().getRemote() + "/linked.txt");
        assertEquals("Files '" + source + "' and '" + linked + "' are not identical from user view.", readFileAsString(source), readFileAsString(linked));
    }

    public void testExcludeByUser() throws Exception {
        setJavaNetCredential();
        FreeStyleProject p = createFreeStyleProject( "testExcludeByUser" );
        p.setScm(new SubversionSCM(
                Arrays.asList( new ModuleLocation( "https://svn.dev.java.net/svn/hudson/trunk/hudson/test-projects/testSubversionExclusions@19438", null )),
                true, null, "", "dty", "", "")
                );
        // Do a build to force the creation of the workspace. This works around
        // pollChanges returning true when the workspace does not exist.
        p.scheduleBuild2(0).get();

        boolean foundChanges = p.pollSCMChanges(createTaskListener());
        assertFalse("Polling found changes that should have been ignored", foundChanges);
    }

    /**
     * Do the polling on the slave and make sure it works.
     */
    @Bug(4299)
    public void testPolling() throws Exception {
        File repo = new CopyExisting(getClass().getResource("two-revisions.zip")).allocate();
        SubversionSCM scm = new SubversionSCM("file://" + repo.getPath());

        FreeStyleProject p = createFreeStyleProject();
        p.setScm(scm);
        p.setAssignedLabel(createSlave().getSelfLabel());
        assertBuildStatusSuccess(p.scheduleBuild2(2).get());

        // initial polling on the slave for the code path that doesn't find any change
        assertFalse(p.pollSCMChanges(new StreamTaskListener(System.out)));

        // create a commit
        FreeStyleProject forCommit = createFreeStyleProject();
        forCommit.setScm(scm);
        forCommit.setAssignedLabel(hudson.getSelfLabel());
        FreeStyleBuild b = assertBuildStatusSuccess(forCommit.scheduleBuild2(0).get());
        FilePath newFile = b.getWorkspace().child("foo");
        newFile.touch(System.currentTimeMillis());
        SVNClientManager svnm = SubversionSCM.createSvnClientManager();
        svnm.getWCClient().doAdd(new File(newFile.getRemote()),false,false,false, SVNDepth.INFINITY, false,false);
        SVNCommitClient cc = svnm.getCommitClient();
        cc.doCommit(new File[]{new File(newFile.getRemote())},false,"added",null,null,false,false,SVNDepth.EMPTY);

        // polling on the slave for the code path that doesn't find any change
        assertTrue(p.pollSCMChanges(new StreamTaskListener(System.out)));
    }

    public void testCompareSVNAuthentications() throws Exception {
        assertFalse(compareSVNAuthentications(new SVNUserNameAuthentication("me",true),new SVNSSHAuthentication("me","me",22,true)));
        // same object should compare equal
        _idem(new SVNUserNameAuthentication("me",true));
        _idem(new SVNSSHAuthentication("me","pass",22,true));
        _idem(new SVNSSHAuthentication("me",new File("./some.key"),null,23,false));
        _idem(new SVNSSHAuthentication("me","key".toCharArray(),"phrase",0,false));
        _idem(new SVNPasswordAuthentication("me","pass",true));
        _idem(new SVNSSLAuthentication("certificate".getBytes(),null,true));

        // make sure two Files and char[]s compare the same 
        assertTrue(compareSVNAuthentications(
                new SVNSSHAuthentication("me",new File("./some.key"),null,23,false),
                new SVNSSHAuthentication("me",new File("./some.key"),null,23,false)));
        assertTrue(compareSVNAuthentications(
                new SVNSSHAuthentication("me","key".toCharArray(),"phrase",0,false),
                new SVNSSHAuthentication("me","key".toCharArray(),"phrase",0,false)));

        // negative cases
        assertFalse(compareSVNAuthentications(
                new SVNSSHAuthentication("me",new File("./some1.key"),null,23,false),
                new SVNSSHAuthentication("me",new File("./some2.key"),null,23,false)));
        assertFalse(compareSVNAuthentications(
                new SVNSSHAuthentication("me","key".toCharArray(),"phrase",0,false),
                new SVNSSHAuthentication("yo","key".toCharArray(),"phrase",0,false)));

    }

    private void _idem(SVNAuthentication a) {
        assertTrue(compareSVNAuthentications(a,a));
    }

    /**
     * Make sure that a failed credential doesn't result in an infinite loop
     */
    @Bug(2909)
    public void testInfiniteLoop() throws Exception {
        // creates a purely in memory auth manager
        ISVNAuthenticationManager m = createInMemoryManager();

        // double check that it really knows nothing about the fake repo
        try {
            m.getFirstAuthentication(kind, realm, repo);
            fail();
        } catch (SVNCancelException e) {
            // yep
        }

        // let Hudson have the credential
        descriptor.postCredential(repo.toDecodedString(),"guest","",null,new PrintWriter(System.out));

        // emulate the call flow where the credential fails
        List<SVNAuthentication> attempted = new ArrayList<SVNAuthentication>();
        SVNAuthentication a = m.getFirstAuthentication(kind, realm, repo);
        assertNotNull(a);
        attempted.add(a);
        for (int i=0; i<10; i++) {
            m.acknowledgeAuthentication(false,kind,realm,SVNErrorMessage.create(SVNErrorCode.RA_NOT_AUTHORIZED),a);
            try {
                a = m.getNextAuthentication(kind,realm,repo);
                assertNotNull(a);
                attempted.add(a);
            } catch (SVNCancelException e) {
                // make sure we've tried our fake credential
                for (SVNAuthentication aa : attempted) {
                    if (aa instanceof SVNPasswordAuthentication) {
                        SVNPasswordAuthentication pa = (SVNPasswordAuthentication) aa;
                        if(pa.getUserName().equals("guest") && pa.getPassword().equals(""))
                            return; // yep
                    }
                }
                fail("Hudson didn't try authentication");
            }
        }
        fail("Looks like we went into an infinite loop");
    }

    /**
     * Even if the default providers remember bogus passwords, Hudson should still attempt what it knows.
     */
    @Bug(3936)
    public void test3936()  throws Exception {
        // creates a purely in memory auth manager
        ISVNAuthenticationManager m = createInMemoryManager();

        // double check that it really knows nothing about the fake repo
        try {
            m.getFirstAuthentication(kind, realm, repo);
            fail();
        } catch (SVNCancelException e) {
            // yep
        }

        // teach a bogus credential and have SVNKit store it.
        SVNPasswordAuthentication bogus = new SVNPasswordAuthentication("bogus", "bogus", true);
        m.acknowledgeAuthentication(true,kind,realm,null, bogus);
        assertTrue(compareSVNAuthentications(m.getFirstAuthentication(kind, realm, repo),bogus));
        try {
            attemptAccess(m);
            fail("SVNKit shouldn't yet know how to access");
        } catch (SVNCancelException e) {
        }

        // make sure the failure didn't clean up the cache,
        // since what we want to test here is Hudson trying to supply its credential, despite the failed cache
        assertTrue(compareSVNAuthentications(m.getFirstAuthentication(kind, realm, repo),bogus));

        // now let Hudson have the real credential
        // can we now access the repo?
        descriptor.postCredential(repo.toDecodedString(),"guest","",null,new PrintWriter(System.out));
        attemptAccess(m);
    }

    private void attemptAccess(ISVNAuthenticationManager m) throws SVNException {
        SVNRepository repository = SVNRepositoryFactory.create(repo);
        repository.setAuthenticationManager(m);
        repository.testConnection();
    }

    private ISVNAuthenticationManager createInMemoryManager() {
        ISVNAuthenticationManager m = SVNWCUtil.createDefaultAuthenticationManager(hudson.root,null,null,false);
        m.setAuthenticationProvider(descriptor.createAuthenticationProvider());
        return m;
    }
}

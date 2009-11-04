package hudson.plugins.mercurial;

import hudson.FilePath;
import hudson.Launcher;
import hudson.plugins.mercurial.browser.BitBucket;
import hudson.plugins.mercurial.browser.HgBrowser;
import hudson.plugins.mercurial.browser.HgWeb;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import hudson.util.StreamTaskListener;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.jvnet.hudson.test.Bug;

/**
 * @author Kohsuke Kawaguchi
 */
public class MercurialSCMTest extends HudsonTestCase {
    TaskListener listener = new StreamTaskListener(System.out);
    Launcher launcher;
    private File repo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        launcher = Hudson.getInstance().createLauncher(listener);
        repo = createTmpDir();
    }
        
    public void testBasicOps() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new MercurialSCM(null,repo.getPath(),null,null,null,false));

        hg("init");
        touchAndCommit("a");
        buildAndCheck(p,"a");   // this tests the clone op
        touchAndCommit("b");
        buildAndCheck(p,"b");   // this tests the update op
    }

    @Bug(4281)    
    public void testBranches() throws Exception {
        hg("init");
        touchAndCommit("init");
        hg("tag", "init");
        touchAndCommit("default-1");
        hg("up", "-C", "init");
        hg("branch", "b");
        touchAndCommit("b-1");
        FreeStyleProject p = createFreeStyleProject();
        // Clone off b.
        p.setScm(new MercurialSCM(null, repo.getPath(), "b", null, null, false));
        buildAndCheck(p, "b-1");
        hg("up", "-C", "default");
        touchAndCommit("default-2");
        // Changes in default should be ignored.
        assertFalse(p.pollSCMChanges(new StreamTaskListener(System.out)));
        hg("up", "-C", "b");
        touchAndCommit("b-2");
        // But changes in b should be pulled.
        assertTrue(p.pollSCMChanges(new StreamTaskListener(System.out)));
        buildAndCheck(p, "b-2");
        // Switch to default branch with an existing workspace.
        p.setScm(new MercurialSCM(null, repo.getPath(), null, null, null, false));
        // Should now consider preexisting changesets in default to be poll triggers.
        assertTrue(p.pollSCMChanges(new StreamTaskListener(System.out)));
        // Should switch working copy to default branch.
        buildAndCheck(p, "default-2");
        touchAndCommit("b-3");
        // Changes in other branch should be ignored.
        assertFalse(p.pollSCMChanges(new StreamTaskListener(System.out)));
    }

    @Bug(1099)
    public void testPollingLimitedToModules() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.setScm(new MercurialSCM(null, repo.getPath(), null, "dir1 dir2", null, false));
        hg("init");
        touchAndCommit("dir1/f");
        buildAndCheck(p, "dir1/f");
        touchAndCommit("dir2/f");
        assertTrue(p.pollSCMChanges(new StreamTaskListener(System.out)));
        buildAndCheck(p, "dir2/f");
        touchAndCommit("dir3/f");
        assertFalse(p.pollSCMChanges(new StreamTaskListener(System.out)));
        // No support for partial checkouts yet, so workspace will contain everything.
        buildAndCheck(p, "dir3/f");
    }

    @Bug(4702)
    public void testChangelogLimitedToModules() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        // Control case: no modules specified.
        p.setScm(new MercurialSCM(null, repo.getPath(), null, null, null, false));
        hg("init");
        touchAndCommit("dir1/f1");
        p.scheduleBuild2(0).get();
        touchAndCommit("dir2/f1");
        Iterator<? extends ChangeLogSet.Entry> it = p.scheduleBuild2(0).get().getChangeSet().iterator();
        assertTrue(it.hasNext());
        ChangeLogSet.Entry entry = it.next();
        assertEquals(Collections.singleton("dir2/f1"), new HashSet<String>(entry.getAffectedPaths()));
        assertFalse(it.hasNext());
        p.setScm(new MercurialSCM(null, repo.getPath(), null, "dir1 extra", null, false));
        // dir2/f2 change should be ignored.
        touchAndCommit("dir1/f2");
        touchAndCommit("dir2/f2");
        it = p.scheduleBuild2(0).get().getChangeSet().iterator();
        assertTrue(it.hasNext());
        entry = it.next();
        assertEquals(Collections.singleton("dir1/f2"), new HashSet<String>(entry.getAffectedPaths()));
        assertFalse(it.hasNext());
        // First commit should match (because at least one file does) but not second.
        touchAndCommit("dir2/f3", "dir1/f3");
        touchAndCommit("dir2/f4", "dir2/f5");
        it = p.scheduleBuild2(0).get().getChangeSet().iterator();
        assertTrue(it.hasNext());
        entry = it.next();
        assertEquals(new HashSet<String>(Arrays.asList("dir1/f3", "dir2/f3")), new HashSet<String>(entry.getAffectedPaths()));
        assertFalse(it.hasNext());
        // Any module in the list can trigger an inclusion.
        touchAndCommit("extra/f1");
        it = p.scheduleBuild2(0).get().getChangeSet().iterator();
        assertTrue(it.hasNext());
        entry = it.next();
        assertEquals(Collections.singleton("extra/f1"), new HashSet<String>(entry.getAffectedPaths()));
        assertFalse(it.hasNext());
    }

    private void touchAndCommit(String... names) throws Exception {
        for (String name : names) {
            FilePath toCreate = new FilePath(repo).child(name);
            toCreate.getParent().mkdirs();
            toCreate.touch(0);
            hg("add", name);
        }
        hg("commit", "-m", "added " + Arrays.toString(names));
    }

    private void buildAndCheck(FreeStyleProject p, String name) throws InterruptedException, ExecutionException, IOException {
        FreeStyleBuild b = p.scheduleBuild2(0).get();
        assertTrue(b.getWorkspace().child(name).exists());
        assertNotNull(b.getAction(MercurialTagAction.class));
    }

    private void hg(String... args) throws Exception {
        assertEquals(0,launcher.launch().cmds(new File("hg"), args).pwd(repo).stdout(listener).join());
    }

    /**
     * With an introduction of HgBrowser base class, a care has to be taken to load existing dataset.
     *
     * This test verifies that. 
     */
    @LocalData
    public void testRepositoryBrowserCompatibility() throws Exception {
        FreeStyleProject p = (FreeStyleProject)hudson.getItem("foo");
        MercurialSCM ms = (MercurialSCM)p.getScm();
        assertTrue(ms.getBrowser() instanceof HgWeb);
        assertEqualBeans(new HgWeb("http://www.yahoo.com/"),ms.getBrowser(),"url");
    }
    
    @Bug(4510)
    @LocalData
    public void testPickingUpAlternativeBrowser() throws MalformedURLException, Exception {
        FreeStyleProject p = (FreeStyleProject)hudson.getItem("foo");
        MercurialSCM ms = (MercurialSCM)p.getScm();
        final HgBrowser browser = ms.getBrowser();
        assertEquals("wrong url", new URL("http://bitbucket.org/"), browser.getUrl());
        assertTrue("class:" + browser.getClass(), browser instanceof BitBucket);
        assertEqualBeans(new BitBucket("http://bitbucket.org/"),browser,"url");
    }
    
    @Bug(4514)
    @LocalData
    public void testBrowsersAvailableInDropDown() throws MalformedURLException, Exception {
        FreeStyleProject p = (FreeStyleProject)hudson.getItem("foo");
        MercurialSCM ms = (MercurialSCM)p.getScm();
        final HgBrowser browser = ms.getBrowser();
        assertEquals("wrong url", new URL("http://bitbucket.org/"), browser.getUrl());
        assertTrue("class:" + browser.getClass(), browser instanceof BitBucket);
        assertEqualBeans(new BitBucket("http://bitbucket.org/"),browser,"url");        
        final List<Descriptor<RepositoryBrowser<?>>> browserDescriptors = ms.getDescriptor().getBrowserDescriptors();
        assertTrue("Could not find BitBucket in " + browserDescriptors, browserDescriptors.contains(browser.getDescriptor()));
    }
}
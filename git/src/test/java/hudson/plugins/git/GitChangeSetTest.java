package hudson.plugins.git;

import hudson.plugins.git.GitChangeSet.Path;
import hudson.scm.EditType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import junit.framework.Assert;
import junit.framework.TestCase;

public class GitChangeSetTest extends TestCase {
    
    public GitChangeSetTest(String testName) {
        super(testName);
    }

    public void testChangeSet() {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("Some header junk we should ignore...");
        lines.add("header line 2");
        lines.add("commit 123abc456def");
        lines.add("tree 789ghi012jkl");
        lines.add("parent 345mno678pqr");
        lines.add("author John Author <jauthor@nospam.com> 1234567 -0600");
        lines.add("commiter John Committer <jcommitter@nospam.com> 1234567 -0600");
        lines.add("");
        lines.add("    Commit title.");
        lines.add("    Commit extended description.");
        lines.add("");
        lines.add(":000000 123456 0000000000000000000000000000000000000000 123abc456def789abc012def345abc678def901a A\tsrc/test/add.file");
        lines.add(":123456 000000 123abc456def789abc012def345abc678def901a 0000000000000000000000000000000000000000 D\tsrc/test/deleted.file");
        lines.add(":123456 789012 123abc456def789abc012def345abc678def901a bc234def567abc890def123abc456def789abc01 M\tsrc/test/modified.file");
        lines.add(":123456 789012 123abc456def789abc012def345abc678def901a bc234def567abc890def123abc456def789abc01 R012\tsrc/test/renamedFrom.file\tsrc/test/renamedTo.file");
        GitChangeSet changeSet = new GitChangeSet(lines);

        Assert.assertEquals("123abc456def", changeSet.getId());
        Assert.assertEquals("Commit title.", changeSet.getMsg());
        Assert.assertEquals("Commit title.\nCommit extended description.\n", changeSet.getComment());
        HashSet<String> expectedAffectedPaths = new HashSet<String>(7);
        expectedAffectedPaths.add("src/test/add.file");
        expectedAffectedPaths.add("src/test/deleted.file");
        expectedAffectedPaths.add("src/test/modified.file");
        expectedAffectedPaths.add("src/test/renamedFrom.file\tsrc/test/renamedTo.file");
        Assert.assertEquals(expectedAffectedPaths, changeSet.getAffectedPaths());

        Collection<Path> actualPaths = changeSet.getPaths();
        Assert.assertEquals(4, actualPaths.size());
        for (Path path : actualPaths) {
            if ("src/test/add.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.ADD, path.getEditType());
                Assert.assertNull(path.getSrc());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getDst());
            } else if ("src/test/deleted.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.DELETE, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertNull(path.getDst());
            } else if ("src/test/modified.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.EDIT, path.getEditType());
                Assert.assertEquals("123abc456def789abc012def345abc678def901a", path.getSrc());
                Assert.assertEquals("bc234def567abc890def123abc456def789abc01", path.getDst());
            } else if ("src/test/renamedFrom.file\tsrc/test/renamedTo.file".equals(path.getPath())) {
                Assert.assertEquals(EditType.EDIT, path.getEditType());
                Assert.assertNull(path.getSrc());
                Assert.assertNull(path.getDst());
            } else {
                Assert.fail("Unrecognized path.");
            }
        }
    }

}

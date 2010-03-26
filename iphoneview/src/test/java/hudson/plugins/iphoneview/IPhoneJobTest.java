package hudson.plugins.iphoneview;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.scm.ChangeLogSet;
import java.util.ArrayList;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link IPhoneJob}
 *
 * @author Seiji Sogabe
 */
public class IPhoneJobTest {

    @Mocked
    FreeStyleProject mockProject;

    /**
     * Test of getChangedBuilds method, of class IPhoneJob.
     */
    @Test
    public void testGetChangedBuilds() {

        new Expectations() {
            FreeStyleBuild mockBuild;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockChangeLogSet;
            ChangeLogSet.Entry mockEntry;
            {
                List<FreeStyleBuild> builds = new ArrayList<FreeStyleBuild>();
                builds.add(mockBuild);

                mockProject.getBuilds();
                returns(builds);

                mockBuild.getChangeSet();
                returns(mockChangeLogSet);

                List<ChangeLogSet.Entry> list = new ArrayList<ChangeLogSet.Entry>();
                list.add(mockEntry);
                
                mockChangeLogSet.iterator();
                returns(list.iterator());
            }
        };

        IPhoneJob<FreeStyleProject, FreeStyleBuild> job = new IPhoneJob<FreeStyleProject, FreeStyleBuild>(mockProject);
        List<FreeStyleBuild> builds = job.getChangedBuilds(1);

        assertNotNull(builds);
        assertEquals(1, builds.size());
    }

    /**
     * Test of getChangedBuilds method, of class IPhoneJob.
     */
    @Test
    public void testGetChangedBuilds_ManyBuilds() {


        new Expectations() {
            FreeStyleBuild mockBuild;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockChangeLogSet;
            ChangeLogSet.Entry mockEntry;
            {
                List<FreeStyleBuild> builds = new ArrayList<FreeStyleBuild>();
                builds.add(mockBuild);
                builds.add(mockBuild);
                builds.add(mockBuild);

                mockProject.getBuilds();
                returns(builds);

                for (int i = 0; i < 3; i++) {
                    mockBuild.getChangeSet();
                    returns(mockChangeLogSet);

                    List<ChangeLogSet.Entry> list = new ArrayList<ChangeLogSet.Entry>();
                    list.add(mockEntry);
                    mockChangeLogSet.iterator();
                    returns(list.iterator());
                }
            }
        };

        IPhoneJob<FreeStyleProject, FreeStyleBuild> job = new IPhoneJob<FreeStyleProject, FreeStyleBuild>(mockProject);
        List<FreeStyleBuild> builds = job.getChangedBuilds(2);

        assertNotNull(builds);
        assertEquals(2, builds.size());
    }

    /**
     * Test of getChangedBuilds method, of class IPhoneJob.
     */
    @Test
    public void testGetChangedBuilds_FewBuilds() {

        new Expectations() {
            FreeStyleBuild mockBuild;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockChangeLogSet;
            ChangeLogSet.Entry mockEntry;
            {
                List<FreeStyleBuild> builds = new ArrayList<FreeStyleBuild>();
                builds.add(mockBuild);
                builds.add(mockBuild);
                builds.add(mockBuild);

                mockProject.getBuilds();
                returns(builds);

                for (int i = 0; i < 3; i++) {
                    mockBuild.getChangeSet();
                    returns(mockChangeLogSet);

                    List<ChangeLogSet.Entry> list = new ArrayList<ChangeLogSet.Entry>();
                    list.add(mockEntry);

                    mockChangeLogSet.iterator();
                    returns(list.iterator());
                }
            }
        };

        IPhoneJob<FreeStyleProject, FreeStyleBuild> job = new IPhoneJob<FreeStyleProject, FreeStyleBuild>(mockProject);
        List<FreeStyleBuild> builds = job.getChangedBuilds(5);

        assertNotNull(builds);
        assertEquals(3, builds.size());
    }

    /**
     * Test of getChangedBuilds method, of class IPhoneJob.
     */
    @Test
    public void testGetChangedBuilds_HasNoChangedBuild() {

        new Expectations() {
            FreeStyleBuild mockBuild;
            FreeStyleBuild mockNoChangedBuild;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockChangeLogSet;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockEmptyChangeLogSet;
            ChangeLogSet.Entry mockEntry;
            {
                List<FreeStyleBuild> builds = new ArrayList<FreeStyleBuild>();
                builds.add(mockBuild);
                builds.add(mockNoChangedBuild);

                mockProject.getBuilds();
                returns(builds);

                mockBuild.getChangeSet();
                returns(mockChangeLogSet);

                List<ChangeLogSet.Entry> list = new ArrayList<ChangeLogSet.Entry>();
                list.add(mockEntry);
                mockChangeLogSet.iterator();
                returns(list.iterator());

                mockNoChangedBuild.getChangeSet();
                returns(mockEmptyChangeLogSet);

                List<ChangeLogSet.Entry> emptyList = new ArrayList<ChangeLogSet.Entry>();
                mockEmptyChangeLogSet.iterator();
                returns(emptyList.iterator());
            }
        };

        IPhoneJob<FreeStyleProject, FreeStyleBuild> job = new IPhoneJob<FreeStyleProject, FreeStyleBuild>(mockProject);
        List<FreeStyleBuild> builds = job.getChangedBuilds(2);

        assertNotNull(builds);
        assertEquals(1, builds.size());
    }

    /**
     * Test of getChangedBuilds method, of class IPhoneJob.
     */
    @Test
    public void testGetChangedBuilds_NoBuilds() {


        new Expectations() {
            FreeStyleBuild mockBuild;
            ChangeLogSet<? extends ChangeLogSet.Entry> mockChangeLogSet;
            ChangeLogSet.Entry mockEntry;
            {
                List<FreeStyleBuild> builds = new ArrayList<FreeStyleBuild>();

                mockProject.getBuilds();
                returns(builds);
            }
        };

        IPhoneJob<FreeStyleProject, FreeStyleBuild> job = new IPhoneJob<FreeStyleProject, FreeStyleBuild>(mockProject);
        List<FreeStyleBuild> builds = job.getChangedBuilds(1);

        assertNotNull(builds);
        assertTrue(builds.isEmpty());
    }


}
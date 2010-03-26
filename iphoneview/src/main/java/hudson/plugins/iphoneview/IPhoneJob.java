package hudson.plugins.iphoneview;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IPhoneJob<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>>  {

    private final P project;

    public IPhoneJob(final P project) {
        this.project = project;
    }

    public List<B> getChangedBuilds(final int size) {
        int n = size;
        final List<B> builds = new ArrayList<B>();
        for (B build : project.getBuilds()) {
            final Iterator<? extends ChangeLogSet.Entry> it = build.getChangeSet().iterator();
            if (it.hasNext()) {
                if (n <= 0) {
                    break;
                }
                builds.add(build);
                n--;
            }
        }
        return Collections.unmodifiableList(builds);
    }

    public P getJob() {
        return project;
    }

}

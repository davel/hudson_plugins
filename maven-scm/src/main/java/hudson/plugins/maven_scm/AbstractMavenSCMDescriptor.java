package hudson.plugins.maven_scm;

import hudson.scm.SCMDescriptor;
import hudson.util.FormValidation;
import hudson.Util;

import java.util.List;

import org.kohsuke.stapler.QueryParameter;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractMavenSCMDescriptor extends SCMDescriptor<MavenSCM> {
    protected AbstractMavenSCMDescriptor() {
        super(MavenSCM.class,null);
    }

    public FormValidation doUrlCheck(@QueryParameter String value) {
        String v = Util.fixEmpty(value);
        if(v==null) {
            return FormValidation.ok();
        }
        List list = PluginImpl.MANAGER.validateScmRepository(v);
        if(list.isEmpty())
            return FormValidation.ok();
        else {
            StringBuilder buf = new StringBuilder();
            for (Object o : list)
                buf.append(o).append("<br/>");
            return FormValidation.errorWithMarkup(buf.toString());
        }
    }
}

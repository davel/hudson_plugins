package hudson.plugins.proc;

import hudson.util.ProcessTree.OSProcess;
import hudson.model.AbstractBuild;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import java.util.List;

/**
 * @author Jitendra Kotamraju
 */
public class ProcInfo {
    protected final AbstractBuild run;
    protected final OSProcess proc;

    protected ProcInfo(AbstractBuild run, OSProcess proc) {
        this.run = run;
        this.proc = proc;
    }

    // Is it a Java Process ? Crude way of checking - just checks whether
    // the first argument contains "java"
    private static boolean isJavaProc(OSProcess proc) {
        List<String> args = proc.getArguments();
        return args.size() > 0 && args.get(0).contains("java");
    }

    public String getInfo() {
        return proc.getPid()+" - "+proc.getArguments();
    }

    static ProcInfo getProcInfo(AbstractBuild run, OSProcess proc) {
        return isJavaProc(proc) ? new JavaProcInfo(run, proc) : new ProcInfo(run, proc);
    }

}

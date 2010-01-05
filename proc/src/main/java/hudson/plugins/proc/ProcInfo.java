package hudson.plugins.proc;

import hudson.util.ProcessTree.OSProcess;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import java.util.List;

/**
 * @author Jitendra Kotamraju
 */
public class ProcInfo {
    protected OSProcess proc;

    protected ProcInfo(OSProcess proc) {
        this.proc = proc;
    }

    public HttpResponse doKill() {
        proc.kill();
        return new HttpRedirect("..");
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

    static ProcInfo getProcInfo(OSProcess proc) {
        return isJavaProc(proc) ? new JavaProcInfo(proc) : new ProcInfo(proc);
    }

}

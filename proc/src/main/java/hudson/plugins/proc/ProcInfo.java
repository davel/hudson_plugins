package hudson.plugins.proc;

import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ProcessTree;
import hudson.util.ProcessTree.OSProcess;
import hudson.EnvVars;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpRedirect;

/**
 * @author Jitendra Kotamraju
 */
public class ProcInfo {
    private OSProcess proc;

    ProcInfo(OSProcess proc) {
        this.proc = proc;
    }

    public HttpResponse doKill() {
        proc.kill();
        return new HttpRedirect("..");
    }

    @Override
    public String toString() {
        if (isJavaProc()) {
System.out.println("Java Process="+proc.getArguments());
            return new JavaProcInfo(proc).jstack();
        }
        return proc.getPid()+""+proc.getArguments();
    }

    // Is this a Java Process ?
    private boolean isJavaProc() {
        for(String arg : proc.getArguments()) {
            if (arg.contains("java")) {
                return true;
            }
        }
        return false;
    }

}

package hudson.plugins.proc;

import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ProcessTree;
import hudson.EnvVars;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jitendra Kotamraju
 */
public class ProcInfo {
    private ProcessTree.OSProcess proc;

    ProcInfo(ProcessTree.OSProcess proc) {
        this.proc = proc;
    }

    @Override
    public String toString() {
        return proc.getPid()+""+proc.getArguments();
    }

}

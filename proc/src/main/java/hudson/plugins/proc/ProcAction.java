package hudson.plugins.proc;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Run;
import hudson.remoting.Callable;
import hudson.util.ProcessTree;
import hudson.EnvVars;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jitendra Kotamraju
 */
public class ProcAction implements Action {
    private AbstractBuild run;

    ProcAction(AbstractBuild run) {
        this.run = run;
    }

    public String getIconFileName() {
        return "save.gif";      // TODO fix with our own icon
    }

    public String getDisplayName() {
        return "Processes";
    }

    public String getUrlName() {
        return "proc";
    }

    public ProcInfo getDynamic(String id) throws IOException, InterruptedException {
        ProcessTree.OSProcess osp = getProcessTree().get(Integer.parseInt(id));
        return ProcInfo.getProcInfo(run, osp);
    }

    // returns the list of processes for a build
    public List<ProcessTree.OSProcess> getProcesses() throws IOException, InterruptedException {
        List<ProcessTree.OSProcess> procs = new ArrayList<ProcessTree.OSProcess>();
        EnvVars vars = run.getCharacteristicEnvVars();
        for(ProcessTree.OSProcess osp : getProcessTree()) {
            if (osp.hasMatchingEnvVars(vars)) {
                procs.add(osp);
            }
        }
        return procs;
    }

    // Returned object may be the remote ProcessTree(running on slave) which
    // is serialized and created on this jvm
    private ProcessTree getProcessTree() throws IOException, InterruptedException {
        return run.getBuiltOn().getChannel().call(new ProcessTreeTask());
    }

    // Keep it static inner class, otherwise ProcessAction needs to be
    // specified as Serializable
    private static class ProcessTreeTask implements Callable<ProcessTree, RuntimeException> {
        public ProcessTree call() {
            return ProcessTree.get();
        }
    }

}

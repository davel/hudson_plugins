package hudson.plugins.proc;

import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ProcessTree;
import hudson.EnvVars;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jitendra Kotamraju
 */
public class ProcAction implements Action {
    private Run run;

    ProcAction(Run run) {
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

    public Object getDynamic(String id) {
        ProcessTree.OSProcess osp = ProcessTree.get().get(Integer.parseInt(id));
        return new ProcInfo(osp);
    }

    // returns the list of processes for a build
    public List<ProcessTree.OSProcess> getProcesses() {
        List<ProcessTree.OSProcess> procs = new ArrayList<ProcessTree.OSProcess>();
        EnvVars vars = run.getCharacteristicEnvVars();
        for(ProcessTree.OSProcess osp : ProcessTree.get()) {
            if (osp.hasMatchingEnvVars(vars)) {
                procs.add(osp);
            }
        }
        return procs;
    }

}

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
        return ProcessTree.get().get(Integer.parseInt(id));
    }

    // returns the list of process for a build
    public List<ProcessTree.OSProcess> getProcesses() {
        List<ProcessTree.OSProcess> procs = new ArrayList<ProcessTree.OSProcess>();
        EnvVars vars = run.getCharacteristicEnvVars();
        Iterator<ProcessTree.OSProcess> it = ProcessTree.get().iterator();
        while(it.hasNext()) {
            ProcessTree.OSProcess osp = it.next();
            // TODO OSProcess#hasMatchingEnvVars()
            if (hasMatchingEnvVars(vars, osp.getEnvironmentVariables())) {
                procs.add(osp);
            }
        }
        return procs;
    }

    // TODO remove once we use the new hudson build which exposes
    // TODO OSProcess#hasMatchingEnvVars()
    boolean hasMatchingEnvVars(Map<String,String> modelEnvVar, Map<String,String> procVar) {
        if(modelEnvVar.isEmpty())
            // sanity check so that we don't start rampage.
            return false;

        for (Map.Entry<String,String> e : modelEnvVar.entrySet()) {
            String v = procVar.get(e.getKey());
            if(v==null || !v.equals(e.getValue()))
                return false;   // no match
            else
                System.out.println("Mathing v="+v+" key="+e.getKey());
        }

        return true;
    }

}

/*
 * The MIT License
 *
 * Copyright (c) 2010, Gregory Covert Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.staf;

import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.JDK;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * The build step for STAX jobs.  Runs the job in a STAF enabled
 * JVM, and passes the results back.
 */
public class STAX extends Builder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifies {@link STAFInstallation} to be used. */
    private final String stafInstallationName;
    /**
     * The target endpoint
     */
    private final String endpoint;
    /**
     * Identifies the {@link JDK} to be used  
     */
    private final String jdkInstallationName;
    /**
     * Identifies the stax file to execute
     */
    private final String xmlFileToExecute;
    /**
     * The args to the stax job
     */
    private final String jobArguments;
    /**
     * The function to run
     */
    private final String function;
    /**
     * Should the build be failed if one of the test cases fail.  (if false, a failure
     * is marked as unstable)
     */
    private final boolean fail;
    /**
     * Should the logs be cleared (always true for now, may be option in future)
     */
    private final boolean clearLogs = true;
    /**
     * Should the elapsed time be logged.  (always true for now, may be option in future)
     */
    private final boolean logElapsedTime = true;
    /**
     * Should the TC number of starts.  (always true for now, may be option in future)
     */
    private final boolean logTcNumStarts = true;
    /**
     * Should TC start/stop be logged.  (always true for now, may be option in the future)
     */
    private final boolean logTcStartStop = true;

    /**
     * STAF execution job debug.  Set to true when debugging,
     * as stepping through jstaf calls is not possible, as it runs in
     * a separate process.  Probaby a better way to do this.
     */
    private static final boolean debug = false;

    /**
     */
    @DataBoundConstructor
    public STAX(String stafInstallationName, String endpoint, String jdkInstallationName, String xmlFileToExecute,
            String jobArguments, String function, boolean fail) {
        this.stafInstallationName = stafInstallationName;
        if (endpoint.length() != 0) {
            this.endpoint = endpoint;
        } else {
            this.endpoint = "local";
        }
        this.jdkInstallationName = jdkInstallationName;
        this.xmlFileToExecute = xmlFileToExecute;
        this.jobArguments = jobArguments;
        this.function = function;
        this.fail = fail;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Returns the {@link STAFInstallation} to use when the build takes place
     * ({@code null} if none has been set).
     */
    public STAFInstallation getStafInstallation() {
        for (STAFInstallation installation : getDescriptor().getInstallations()) {
            if (getStafInstallationName() != null && installation.getName().equals(getStafInstallationName())) {
                return installation;
            }
        }

        return null;
    }

    /**
     * Returns the {@link JDK} to use when the build takes place
     * ({@code null} if none has been set).
     */
    public JDK getJdkInstallation() {
        for (JDK installation : Hudson.getInstance().getDescriptorByType(JDK.DescriptorImpl.class).getInstallations()) {
            if (getJdkInstallationName() != null && installation.getName().equals(getJdkInstallationName())) {
                return installation;
            }
        }

        return null;
    }

    public String getStafInstallationName() {
        return stafInstallationName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getJdkInstallationName() {
        return jdkInstallationName;
    }

    public String getXmlFileToExecute() {
        return xmlFileToExecute;
    }

    public String getJobArguments() {
        return jobArguments;
    }

    public String getFunction() {
        return function;
    }

    public boolean getClearLogs() {
        return clearLogs;
    }

    public boolean getLogElapsedTime() {
        return logElapsedTime;
    }

    public boolean getLogTcNumStarts() {
        return logTcNumStarts;
    }

    public boolean getLogTcStartStop() {
        return logTcStartStop;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        AbstractProject project = build.getProject();
        ArgumentListBuilder args = new ArgumentListBuilder();
        EnvVars env = build.getEnvironment(listener);
        VariableResolver<String> varResolver = build.getBuildVariableResolver();

        // --- STAF installation ---

        // has a STAF installation been set? 
        STAFInstallation stafInstallation = getStafInstallation();
        if (stafInstallation == null) {
            listener.fatalError(ResourceBundleHolder.get(STAX.class).format("NoInstallationSet"));
            return false;
        }

        stafInstallation = stafInstallation.forNode(Computer.currentComputer().getNode(), listener);
        stafInstallation = stafInstallation.forEnvironment(env);

        String stafExecutable = stafInstallation.getStafExecutable(launcher);
        if (stafExecutable == null) {
            listener.fatalError(ResourceBundleHolder.get(STAX.class).format("NoStafExecutable", stafInstallation.getName()));
            return false;
        }

        // add all of the required paramaters for the installation to the env
        Map<String, String> stafEnv = stafInstallation.getRequiredEnvVars();
        env.overrideAll(stafEnv);

        // Has a JDK been set.  We may need to use a different JDK so that we don't have library
        // conflicts (32-bit vs 64-bit)  If its not set, then we'll ignore it later
        JDK jdkInstallation = getJdkInstallation();

        if(jdkInstallation != null) {
            jdkInstallation = jdkInstallation.forNode(Computer.currentComputer().getNode(), listener);
            jdkInstallation = jdkInstallation.forEnvironment(env);
        }

        // figure out the correct location of the stax file
        File expandedXmlFile = new File(env.expand(getXmlFileToExecute()));
        FilePath xmlFile;

        if(!expandedXmlFile.isAbsolute() && build.getWorkspace() != null) {
            // ok, path is relative, recreate with workspace
            xmlFile = build.getWorkspace().child(expandedXmlFile.toString());
        }
        else {
            xmlFile = new FilePath(expandedXmlFile);
        }

        if(!xmlFile.exists()) {
            listener.fatalError(ResourceBundleHolder.get(STAX.class).format("XMLFileNotFound", xmlFile));
            return false;
        }

        String jobName = build.getFullDisplayName();

        JStafProc jstafProc = new JStafProc(launcher, listener, env, stafInstallation, jdkInstallation);

        try {
            String message;

            // do a test to make sure the whole STAX job is going to be valid
            String jobRequest = createTestStaxJobRequest(env, varResolver, xmlFile,
                jobName);

            message = ResourceBundleHolder.get(STAX.class).format("TestingStaxJobValid");
            listener.getLogger().println(message);

            STAFResultProxy result = runStaxJob(jstafProc, listener, jobRequest);

            if(!parseResult(result, listener)) return false;

            // Submit the job.  Job is submitted in a paused (HOLD) state
            jobRequest = createStaxJobRequest(env, varResolver, xmlFile,
                jobName);

            message = ResourceBundleHolder.get(STAX.class).format("SubmittingStaxJob");
            listener.getLogger().println(message);

            result = runStaxJob(jstafProc, listener, jobRequest);

            if(!parseResult(result, listener)) return false;

            String jobId = result.getResult();

            message = ResourceBundleHolder.get(STAX.class).format("JobIdReturned", jobId);
            listener.getLogger().println(message);

            message = ResourceBundleHolder.get(STAX.class).format("MonitoringJob", jobId);
            listener.getLogger().println(message);

            // ok, now start monitoring the job, which also releases it from the HOLD
            result = startJobMonitoring(jstafProc, listener, jobId);

            if(!parseResult(result, listener)) return false;

            message = ResourceBundleHolder.get(STAX.class).format("JobComplete", jobId);
            listener.getLogger().println(message);

            // so, our results should old the testcases map, with counts for
            // successes and failures
            Object testResults = result.getResultObj();

            if(testResults instanceof Map) {
                boolean noTestsFailed = parseTestResults((Map)testResults, listener);

                if(noTestsFailed) {
                    return true;
                }
                // return either UNSTABLE or FAILED
                else if (fail){
                    listener.finished(Result.FAILURE);
                    return false;
                }
                else {
                    listener.finished(Result.UNSTABLE);
                    return true;
                }
            }
            else {
                return true;
            }
        } catch (IOException ioe) {
            //Util.displayIOException(ioe, listener);
            if(debug) ioe.printStackTrace(listener.getLogger());
            listener.getLogger().println("IOException: " + ioe.getMessage());

            String errorMessage = ResourceBundleHolder.get(STAX.class).format("ExecutionFailed");

            listener.fatalError(errorMessage);
            return false;
        }
    }

    private boolean parseTestResults(Map testResults, TaskListener listener) {
        boolean notestsfailed = true;
        String message = ResourceBundleHolder.get(STAX.class).format("TestResultsSummary");
        listener.getLogger().println(message);

        for(String name : (Set<String>)testResults.keySet()) {
            int[] result = (int[])testResults.get(name);
            message = ResourceBundleHolder.get(STAX.class).format("TestResult", name, result[0], result[1]);
            listener.getLogger().println(message);
            if(result[1] > 0) notestsfailed = false;
        }

        return notestsfailed;
    }

    private STAFResultProxy runStaxJob(JStafProc jstafProc, final TaskListener listener, final String request) throws IOException, InterruptedException {

        final String endpoint = getEndpoint();

        return jstafProc.execute(
                new Callable<STAFResultProxy, IOException>() {

            public STAFResultProxy call() throws IOException {
                PrintStream out = listener.getLogger();

                out.println(ResourceBundleHolder.get(STAX.class).format("ExecutingStaxJob", request));

                try {
                    if(debug) out.println("Getting staf handle");

                    STAFHandle handle = new STAFHandle("STAX/JobMonitor/Controller");

                    if(debug) out.println("Got staf handle, submitting request to endpoint " + endpoint);

                    STAFResult testResult = handle.submit2(
                        endpoint, "STAX", request);

                    if(debug) out.println("submitted request, result = " + testResult.rc);

                    return new STAFResultProxy(testResult);
                }
                catch (Throwable thrown) {
                    thrown.printStackTrace(out);

                    throw new IOException(thrown);
                }
            }

        });
    }

    private STAFResultProxy startJobMonitoring(JStafProc jstafProc, final TaskListener listener, final String jobId) throws IOException, InterruptedException {

        final String endpoint = getEndpoint();

        return jstafProc.execute(
                new Callable<STAFResultProxy, IOException>() {

            public STAFResultProxy call() throws IOException {
                PrintStream out = listener.getLogger();
                String hostname = "localhost";

                boolean continueMonitoring = true;

                out.println(ResourceBundleHolder.get(STAX.class).format("MonitoringStaxJob"));

                // map of test cases, with success / fail results
                Map<String, int[]> testcaseResults = new HashMap<String, int[]>();

                try {
                    if(debug) out.println("Getting staf handle");

                    STAFHandle handle = new STAFHandle("STAX/JobMonitor/" + hostname + "/" + jobId);

                    if(debug) out.println("Got staf handle, getting machine name");

                    String request = "RESOLVE STRING {STAF/Config/Machine}";

                    STAFResult result = handle.submit2(
                        endpoint, "VAR", request);

                    if(debug) out.println("Retrived varable");

                    if(result.rc != 0) {
                        return new STAFResultProxy(result);
                    }
                    else {
                        hostname = (String) result.resultObj;
                        if(debug) out.println("hostname = " + hostname);
                    }

                    request = "REGISTER TYPE STAX/" + hostname +
                            "/" + jobId + " SUBTYPE Job SUBTYPE Block " +
                            "SUBTYPE Process SUBTYPE STAFCommand SUBTYPE Message " +
                            "SUBTYPE Testcase SUBTYPE TestCaseStatus SUBTYPE subjob " +
                            " MAXATTEMPTS 1 ACKNOWLEDGETIMEOUT 1000 BYHANDLE";

                    result = handle.submit2(
                        endpoint, "Event", request);

                    if(result.rc != 0) {
                        return new STAFResultProxy(result);
                    }

                    if(debug) out.println("Setup monitoring to forward to event queue, now release job");

                    request = "RELEASE JOB " + jobId;

                    result = handle.submit2(
                            endpoint, "STAX", request);

                    if(result.rc != 0) {
                        return new STAFResultProxy(result);
                    }

                    if(debug) out.println("Pull off all events until we see the job terminated event");

                    request = "GET ALL WAIT";

                    while(continueMonitoring) {
                        result = handle.submit2(endpoint, "QUEUE", request);

                        List queueList = (List)result.resultObj;

                        Iterator queueIter = queueList.iterator();

                        while (queueIter.hasNext()) {
                            Map queueMap = (Map)queueIter.next();
                            String queueType = (String)queueMap.get("type");

                            if (queueType == null)
                                continue;  // Ignore message

                            if (queueType.equalsIgnoreCase("STAF/STAXMonitor/End")) {
                                if(debug) out.println("Got end of job");
                                continueMonitoring = false;
                                break; // Don't process any more messages on the queue
                            }
                            else if (!queueType.equalsIgnoreCase("STAF/Service/Event")) {
                                if(debug) out.println("Got an event that we had not registered for");
                                continue; // Ignore messages that don't have this type
                            }

                            Map messageMap = (Map)queueMap.get("message");
                            String type = (String)messageMap.get("type");

                            if(debug) out.println("Processing message of type " + type);

                            if (!type.equalsIgnoreCase("STAX/" +
                                hostname + "/" + jobId)) {
                                if(debug) out.println("Got an event not from our job");
                                continue; // Ignore messages that don't have this type
                            }

                            // Process STAF/Service/Event messages with matching event type
                            String eventID = (String)messageMap.get("eventID");

                            // acknowledge the processing of the event
                            STAFResult ackResult = handle.submit2(
                                STAFHandle.ReqFireAndForget, endpoint,
                                "Event", "ACKNOWLEDGE EVENTID " + eventID);

                            String subtype = (String)messageMap.get("subtype");

                            final Map propertyMap = (Map)messageMap.get("propertyMap");

                            if(debug) out.println("subtype =" + subtype);

                            if (subtype.equals("Message")) {
                                if(debug) out.println("Processing message");
                                processMessageEvent(propertyMap, out);
                            }
                            else if(subtype.equals("Block")) {
                                if(debug) out.println("Processing Block");
                                processBlockEvent(propertyMap, out);
                            }
                            else if (subtype.equals("Process")) {
                                if(debug) out.println("Processing Block");
                                processProcessEvent(propertyMap, out);
                            }
                            else if(subtype.equals("STAFCommand")) {
                                if(debug) out.println("Processing STAFCommand");
                                processSTAFCommandEvent(propertyMap, out);
                            }
                            else if(subtype.equals("SubJob")) {
                                if(debug) out.println("Processing SubJob");
                                processSubJobEvent(propertyMap, out);
                            }
                            else if(subtype.equals("TestcaseStatus")) {
                                if(debug) out.println("Processing TestcaseStatus"); 
                                processTestcaseStatusEvent(propertyMap, testcaseResults, out);                             
                            }
                            else if(subtype.equals("Testcase")) {
                                if(debug) out.println("Processing TestcaseStatus");
                                processTestcaseEvent(propertyMap, testcaseResults, out);
                            }
                            else if (subtype.equals("Job")) {
                                if(debug) out.println("Processing Job");
                                processJobEvent(propertyMap, out);

                                String status = (String)propertyMap.get("status");
                                if(status.equals("end")) {
                                    continueMonitoring = false;
                                }
                            }
                        } // end while iterating through the queue list
                    } // end while continueRunning

                    if(debug) out.println("Job has ended, all messages processed.  Unregister for events");

                    // job has finished, stop all monitoring
                    request = "UNREGISTER TYPE STAX/" + hostname +
                            "/" + jobId + " SUBTYPE Job SUBTYPE Block " +
                            "SUBTYPE Process SUBTYPE STAFCommand " +
                            "SUBTYPE Testcase SUBTYPE TestcaseStatus " +
                            "SUBTYPE Message SUBTYPE subjob";

                    result = handle.submit2(endpoint, "Event",
                            request);

                    if(result.rc != 0) {
                        return new STAFResultProxy(result);
                    }

                    // OK -- so everything worked, just return
                    // the test results for printing

                    return new STAFResultProxy(0, "Test Results", testcaseResults);
                }
                catch (Throwable thrown) {
                    thrown.printStackTrace(out);

                    throw new IOException(thrown);
                }
            }

        });
    }

    private static void processMessageEvent(Map propertyMap, PrintStream out) {
        String message = (String)propertyMap.get("messagetext");

        int timestampIndex = message.indexOf(" ");

        String timestamp = message.substring(0, timestampIndex);
        String content = message.substring(timestampIndex + 1);

        out.println(ResourceBundleHolder.get(STAX.class).format("MessageEvent", timestamp, content));
    }

    private static void processBlockEvent(Map propertyMap, PrintStream out) {
        if(debug) out.println("BLOCK: " + mapToString(propertyMap));

        // don't print anything for block events unless debug enabled
    }

    private static void processProcessEvent(Map propertyMap, PrintStream out) {
        if(debug) out.println("PROCESS: " + mapToString(propertyMap));

        String command = (String)propertyMap.get("command");
        String status = (String)propertyMap.get("status");
        String location = (String)propertyMap.get("location");
        String parms = (String)propertyMap.get("parms");

        if("stop".equals(status)) {
            // pad stop to 5, just to make printing better
            status = status + " ";
        }

        out.println(ResourceBundleHolder.get(STAX.class).format("ProcessEvent",
                command, status, location, parms));
    }

    private static void processSTAFCommandEvent(Map propertyMap, PrintStream out) {
        if(debug) out.println("STAFCommand: " + mapToString(propertyMap));

        String service = (String)propertyMap.get("service");
        String status = (String)propertyMap.get("status");
        String location = (String)propertyMap.get("location");
        String request = (String)propertyMap.get("request");

        if("stop".equals(status)) {
            // pad stop to 5, just to make printing better
            status = status + " ";
        }

        out.println(ResourceBundleHolder.get(STAX.class).format("STAFCommandEvent",
                service, status, location, request));
    }

    private static void processSubJobEvent(Map propertyMap, PrintStream out) {
        if(debug)out.println("SUBJOB: " + mapToString(propertyMap));

        String jobId = (String)propertyMap.get("jobID");
        String status = (String)propertyMap.get("status");

        if("stop".equals(status)) {
            // pad stop to 5, just to make printing better
            status = status + " ";
        }

        out.println(ResourceBundleHolder.get(STAX.class).format("SubJobEvent",
                jobId, status));
    }

    private static void processTestcaseStatusEvent(Map propertyMap, Map<String, int[]> testcaseResults,
            PrintStream out) {
        if(debug) out.println("TESTCASESTATUS: " + mapToString(propertyMap));

        String name = (String)propertyMap.get("name");
        String message = (String)propertyMap.get("message");
        // there's other info, could want to change this later

        out.println(ResourceBundleHolder.get(STAX.class).format("TestcaseStatusEvent",
                name, message));
    }

    private static void processTestcaseEvent(Map propertyMap, Map<String, int[]> testcaseResults, PrintStream out) {
        if(debug) out.println("TESTCASE: " + mapToString(propertyMap));

        String name = (String)propertyMap.get("name");
        String elapsedTime = (String)propertyMap.get("elapsed-time");
        String numStarts = (String)propertyMap.get("num-starts");
        String numPass = (String)propertyMap.get("status-pass");
        String numFail = (String)propertyMap.get("status-fail");
        // there's other info, could want to change this later

        int results[] = testcaseResults.get(name);

        if(results == null) {
            results = new int[2];
        }

        try {
            results[0] = Integer.parseInt(numPass);
        }
        catch (NumberFormatException ex) {}
        try {
            results[1] = Integer.parseInt(numFail);
        }
        catch (NumberFormatException ex) {}

        testcaseResults.put(name, results);

        out.println(ResourceBundleHolder.get(STAX.class).format("TestcaseEvent",
                name, elapsedTime, numStarts, numPass, numFail));
    }

    private static void processJobEvent(Map propertyMap, PrintStream out) {
        if(debug) out.println("JOB: " + mapToString(propertyMap));

        String status = (String)propertyMap.get("status");
        String completionStatus = (String)propertyMap.get("jobCompletionStatus");

        out.println(ResourceBundleHolder.get(STAX.class).format("JobEvent",
            status, completionStatus));
    }

    private static String mapToString(Map aMap) {
        StringBuffer buffer = new StringBuffer();
        Set entries = aMap.entrySet();
        Iterator iterator = entries.iterator();
        buffer.append("[ ");
        while (iterator.hasNext()) {
           Map.Entry entry = (Map.Entry)iterator.next();
           buffer.append("{" + entry.getKey() + " : "
             + entry.getValue() + "}");
        }
        buffer.append(" ]");

        return buffer.toString();
    }

    private String createStaxJobRequest(EnvVars env, VariableResolver<String> varResolver, FilePath xmlFile,
            String jobName) {
        StringBuffer request = new StringBuffer();

        request.append("EXECUTE HOLD file ").append(
                wrapData(xmlFile.getRemote()));

        if (getFunction() != null && !getFunction().equals("")) {
            request.append(" FUNCTION ").append(
                    wrapData(getFunction()));
        }

        String jobArgs = Util.replaceMacro(env.expand(getJobArguments()), varResolver);
        jobArgs = jobArgs.replaceAll("[\t]+", " ");

        if (getJobArguments() != null && !(getJobArguments().equals(""))) {
            request.append(" ARGS ").append(wrapData(jobArgs));
        }

        if (jobName != null && !jobName.equals("")) {
            request.append(" JOBNAME ").append(
                    wrapData(jobName));
        }

        if (getClearLogs()) {
            request.append(" CLEARLOGS Enabled");
        } else {
            request.append(" CLEARLOGS Disabled");
        }

        if (getLogElapsedTime()) {
            request.append(" LOGTCELAPSEDTIME Enabled");
        } else {
            request.append(" LOGTCELAPSEDTIME Disabled");
        }

        if (getLogTcNumStarts()) {
            request.append(" LOGTCNUMSTARTS Enabled");
        } else {
            request.append(" LOGTCNUMSTARTS Disabled");
        }

        if (getLogTcStartStop()) {
            request.append(" LOGTCSTARTSTOP Enabled");
        } else {
            request.append(" LOGTCSTARTSTOP Disabled");
        }

        return request.toString();
    }

    private String createTestStaxJobRequest(EnvVars env, VariableResolver<String> varResolver, FilePath xmlFile,
            String jobName) {
        StringBuffer request = new StringBuffer();

        request.append("EXECUTE file ").append(
                wrapData(xmlFile.getRemote()));

        if (getFunction() != null && !getFunction().equals("")) {
            request.append(" FUNCTION ").append(
                    wrapData(getFunction()));
        }

        String jobArgs = Util.replaceMacro(env.expand(getJobArguments()), varResolver);
        jobArgs = jobArgs.replaceAll("[\t]+", " ");

        if (getJobArguments() != null && !(getJobArguments().equals(""))) {
            request.append(" ARGS ").append(wrapData(jobArgs));
        }

        if (jobName != null && !jobName.equals("")) {
            request.append(" JOBNAME ").append(
                    wrapData(jobName));
        }

        request.append(" TEST");

        return request.toString();
    }

    private boolean parseResult(STAFResultProxy result, TaskListener listener) {
        int r = result.getRc();
        if (r != 0) {
            // handle some known cases for bad error codes to log appropriately
            String errorMessage;
            switch (r) {
                // more should be added.  If there are definite ones to add...
                case (21):
                    errorMessage = ResourceBundleHolder.get(STAX.class).format("StafServiceNotRunning", r);
                default:
                    errorMessage = ResourceBundleHolder.get(STAX.class).format("ExecutionResultNotZero", r, result.getResult());
            }

            listener.fatalError(errorMessage);

            return false;
        }
        else {
            return true;
        }
    }

    // Copied from the jstaf libraries.
    private static String wrapData(String data) {
        return ":" + data.length() + ":" + data;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        protected DescriptorImpl(Class<? extends STAX> clazz) {
            super(clazz);
        }

        @Override
        public String getDisplayName() {
            return ResourceBundleHolder.get(STAX.class).format("DisplayName");
        }

        public STAFInstallation[] getInstallations() {
            return Hudson.getInstance().getDescriptorByType(STAFInstallation.DescriptorImpl.class).getInstallations();
        }

        public JDK[] getJdkInstallations() {
            JDK[] installs = Hudson.getInstance().getDescriptorByType(JDK.DescriptorImpl.class).getInstallations();

            if (installs == null) {
                installs = new JDK[0];
            }

            return installs;
        }

        /**
         * Returns the {@link STAFInstallation.DescriptorImpl} instance.
         */
        public STAFInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(STAFInstallation.DescriptorImpl.class);
        }

        /**
         * Checks for fields
         */
        public FormValidation doCheckXmlFileToExecute(@QueryParameter String value) {
            if (value.equals("")) {
                return FormValidation.error(ResourceBundleHolder.get(STAX.class).format("FileToExecuteCannotBeEmpty", value));
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(STAX.class, formData);
        }
    }
}

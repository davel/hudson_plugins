package hudson.plugins.sctmexecutor;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.plugins.sctmexecutor.exceptions.SCTMException;
import hudson.plugins.sctmexecutor.service.ISCTMService;
import hudson.plugins.sctmexecutor.service.SCTMReRunProxy;
import hudson.plugins.sctmexecutor.service.SCTMService;
import hudson.tasks.Builder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Executes a specified execution definition on Borland's SilkCentral Test Manager.
 * 
 * @author Thomas Fuerer
 * 
 */
public final class SCTMExecutor extends Builder {
  static final int OPT_NO_BUILD_NUMBER = 1;
  static final int OPT_USE_THIS_BUILD_NUMBER = 2;
  static final int OPT_USE_SPECIFICJOB_BUILDNUMBER = 3;
  static final int OPT_USE_LATEST_SCTM_BUILDNUMBER = 4;
  private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor"); //$NON-NLS-1$

  private final int projectId;
  private final String execDefIds;
  private final int delay;
  private final int buildNumberUsageOption;
  private final String jobName;
  private final boolean continueOnError;
  private final boolean collectResults;
  private final boolean ignoreSetupCleanup;
  private String version;

  private boolean succeed;
  private ISCTMService service;
  private String product;
  private List<Integer> execDefIdList;

  @DataBoundConstructor
  public SCTMExecutor(final int projectId, final String execDefIds, final int delay, final int buildNumberUsageOption,
      final String jobName, final boolean contOnErr, final boolean collectResults, final boolean ignoreSetupCleanup, String version) {
    this.projectId = projectId;
    this.execDefIds = execDefIds;
    this.delay = delay;
    this.buildNumberUsageOption = buildNumberUsageOption;
    this.jobName = jobName;
    this.continueOnError = contOnErr;
    this.collectResults = collectResults;
    this.ignoreSetupCleanup = ignoreSetupCleanup;
    this.version = version;
    this.execDefIdList = this.csvToIntList(this.execDefIds);

    SCTMExecutorDescriptor descriptor = getDescriptor();
    String serviceURL = descriptor.getServiceURL();
    try {
      service = new SCTMReRunProxy(new SCTMService(serviceURL, descriptor.getUser(), descriptor
          .getPassword(), projectId));
      this.product = this.service.getProductName(this.execDefIdList.get(0));
    } catch (SCTMException e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format(
          "Creating a remote connection to SCTM host ({0}) failed.", serviceURL), e); //$NON-NLS-1$
    }
  }

  @Override
  public SCTMExecutorDescriptor getDescriptor() {
    return (SCTMExecutorDescriptor) Hudson.getInstance().getDescriptor(getClass());
  }

  public String getExecDefIds() {
    return execDefIds;
  }

  public int getProjectId() {
    return projectId;
  }

  public int getDelay() {
    return delay;
  }

  public int getBuildNumberUsageOption() {
    return this.buildNumberUsageOption;
  }

  public String getJobName() {
    return this.jobName;
  }

  public boolean isContinueOnError() {
    return this.continueOnError;
  }

  public boolean isignoreSetupCleanup() {
    return this.ignoreSetupCleanup;
  }

  public boolean isCollectResults() {
    return this.collectResults;
  }
  
  public Collection<String> getAllVersions() {
    try {
      return this.service.getAllVersions(this.execDefIdList.get(0));
    } catch (SCTMException e) {
      LOGGER.warning("No versions available for product.");
    }
    return Collections.emptyList();
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
      throws InterruptedException, IOException {
    SCTMExecutorDescriptor descriptor = getDescriptor();
    String serviceURL = descriptor.getServiceURL();
    try {
      listener.getLogger().println(Messages.getString("SCTMExecutor.log.successfulLogin")); //$NON-NLS-1$
      FilePath rootDir = createResultDir(build.number, build, listener);

      Collection<Thread> executions = new ArrayList<Thread>(execDefIdList.size());
      for (Integer execDefId : execDefIdList) {
        StdXMLResultWriter resultWriter = null;
        if (collectResults)
          resultWriter = new StdXMLResultWriter(rootDir, serviceURL, String.valueOf(build.number),
              this.ignoreSetupCleanup);
        int buildNumber = -1;
        buildNumber = getOrAddBuildNumber(build, listener, service, execDefId);
        Runnable resultCollector = new ExecutionRunnable(service, execDefId, buildNumber, resultWriter, listener
            .getLogger());

        Thread t = new Thread(resultCollector);
        executions.add(t);
        t.start();
        if (delay > 0 && execDefIdList.size() > 1)
          Thread.sleep(delay * 1000);
      }

      for (Thread t : executions) {
        t.join();
      }

      succeed = true;
    } catch (SCTMException e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format(
          "Creating a remote connection to SCTM host ({0}) failed.", serviceURL), e); //$NON-NLS-1$
      listener.fatalError(e.getMessage());
      succeed = false;
    }
    return continueOnError || succeed;
  }

  private int getOrAddBuildNumber(AbstractBuild<?, ?> build, BuildListener listener, ISCTMService service, int nodeId) throws SCTMException {
    switch (this.buildNumberUsageOption) {
    case OPT_USE_THIS_BUILD_NUMBER:
    case OPT_USE_SPECIFICJOB_BUILDNUMBER:
      int buildnumber = -1;
      if (this.buildNumberUsageOption == OPT_USE_THIS_BUILD_NUMBER)
        buildnumber = build.number;
      else if (this.buildNumberUsageOption == OPT_USE_SPECIFICJOB_BUILDNUMBER)
        buildnumber = getBuildNumberFromUpStreamProject(jobName, build.getProject().getTransitiveUpstreamProjects(), listener);
      
      try {
        if (!service.buildNumberExists(product, version, buildnumber)) {
          listener.getLogger().println(MessageFormat.format("INFO: Add buidnumber ''{0}'' on SCTM.", buildnumber));
          if (!service.addBuildNumber(product, version, buildnumber))
            buildnumber = -1;
        } else
          listener.getLogger().println(MessageFormat.format("INFO: Buildnumber ''{0}'' already exists on SCTM.", buildnumber));
      } catch (IllegalArgumentException e) {
        listener.error(e.getMessage());
        buildnumber = -1;
      }
      return buildnumber;
    case OPT_USE_LATEST_SCTM_BUILDNUMBER:
      return service.getLatestSCTMBuildnumber(product, version);      
    default:
      return -1;
    }
  }

  private int getBuildNumberFromUpStreamProject(String projectName, Set<AbstractProject> upstreamProjects,
      BuildListener listener) {
    for (AbstractProject<?, ?> project : upstreamProjects) {
      if (project.getName().equals(projectName))
        return project.getLastSuccessfulBuild().getNumber();
    }
    listener.error(MessageFormat.format(Messages.getString("SCTMExecutor.err.notAUpstreamJob"), projectName)); //$NON-NLS-1$
    return -1;
  }

  private static FilePath createResultDir(int currentBuildNo, AbstractBuild<?, ?> build, BuildListener listener)
      throws IOException, InterruptedException {
    FilePath rootDir = build.getWorkspace();
    if (rootDir == null) {
      LOGGER.severe("Cannot write the result file because slave is not connected."); //$NON-NLS-1$
      listener.error(Messages.getString("SCTMExecutor.log.slaveNotConnected")); //$NON-NLS-1$
      throw new RuntimeException();
    }

    rootDir = new FilePath(rootDir, "SCTMResults"); //$NON-NLS-1$
    if (rootDir.exists()) {
      final String buildNo = String.valueOf(currentBuildNo);
      List<FilePath> list = rootDir.list();// new TestFileFilter(buildNo));
      if (list.size() > 0) {
        for (FilePath filePath : list) {
          if (filePath.getName().matches("TEST-(\\p{Print}*)-" + buildNo + ".xml"))
            return rootDir; // test results of the current run available, do not clean the results directory
        }
        rootDir.deleteContents(); // clean old results
      }
    } else
      rootDir.mkdirs();
    return rootDir;
  }

  private List<Integer> csvToIntList(String execDefIds) {
    List<Integer> list = new LinkedList<Integer>();
    if (execDefIds.contains(",")) { //$NON-NLS-1$
      String[] ids = execDefIds.split(","); //$NON-NLS-1$
      for (String str : ids) {
        list.add(Integer.valueOf(str));
      }
    } else {
      list.add(Integer.valueOf(execDefIds));
    }
    return list;
  }
}

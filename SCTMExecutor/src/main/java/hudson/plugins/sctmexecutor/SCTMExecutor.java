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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
  static final int OPT_USE_UPSTREAMJOB_BUILDNUMBER = 3;
  private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor"); //$NON-NLS-1$

  private final int projectId;
  private final String execDefIds;
  private final int delay;
  private final int buildNumberUsageOption;
  private final String upStreamJobName;
  private final boolean continueOnError;
  private final boolean collectResults;
  
  private boolean succeed;

  @DataBoundConstructor
  public SCTMExecutor(final int projectId, final String execDefIds, final int delay, final int buildNumberUsageOption, final String upStreamJobName, final boolean contOnErr, final boolean collectResults) {
    this.projectId = projectId;
    this.execDefIds = execDefIds;
    this.delay = delay;
    this.buildNumberUsageOption = buildNumberUsageOption;
    this.upStreamJobName = upStreamJobName;
    this.continueOnError = contOnErr;
    this.collectResults = collectResults;
  }

  @Override
  public SCTMExecutorDescriptor getDescriptor() {
    return (SCTMExecutorDescriptor)Hudson.getInstance().getDescriptor(getClass());
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
  
  public String getUpStreamJobName() {
    return this.upStreamJobName;
  }
  
  public boolean isContinueOnError() {
    return this.continueOnError;
  }
  
  public String[] getUpStreamProjects() {
    Collection<String> jobNames = Hudson.getInstance().getJobNames(); // TODO filter real upstream projects
    
    return jobNames.toArray(new String[jobNames.size()]);
  }

  public boolean isCollectResults() {
    return this.collectResults;
  }
  
  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    SCTMExecutorDescriptor descriptor = (SCTMExecutorDescriptor)Hudson.getInstance().getDescriptor(getClass());
    String serviceURL = descriptor.getServiceURL();
    try {
      ISCTMService service = new SCTMReRunProxy(new SCTMService(serviceURL, descriptor.getUser(), descriptor.getPassword()));
      listener.getLogger().println(Messages.getString("SCTMExecutor.log.successfulLogin")); //$NON-NLS-1$
      FilePath rootDir = createResultDir(build.number, build, listener);
      Collection<Integer> ids = csvToIntList(execDefIds);

      Collection<Future<?>> results = new ArrayList<Future<?>>(ids.size());
      for (Integer execDefId : ids) {        
        StdXMLResultWriter resultWriter = null;
        if (collectResults)
          resultWriter = new StdXMLResultWriter(rootDir, descriptor.getServiceURL(), String.valueOf(build.number));
        Runnable resultCollector = new ExecutionRunnable(service, execDefId, getBuildNumber(build, listener),
            resultWriter, listener.getLogger());
        results.add(descriptor.getExecutorPool().submit(resultCollector));
        if (delay > 0 && ids.size() > 1)
          Thread.sleep(delay*1000);
      }
      
      for (Future<?> res : results) {
        res.get();
      }
      succeed = true;
    } catch (SCTMException e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("Creating a remote connection to SCTM host ({0}) failed.", serviceURL), e); //$NON-NLS-1$
      listener.fatalError(e.getMessage());
      succeed = false;
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Starting or collecting for a SCTM execution failed.", e); //$NON-NLS-1$
      listener.fatalError(MessageFormat.format(Messages.getString("SCTMExecutor.err.noResponseFromSCTM"), e.getMessage())); //$NON-NLS-1$
      succeed = false;
    } 
    return continueOnError || succeed;
  }

  private int getBuildNumber(AbstractBuild<?, ?> build, BuildListener listener) {
    if (OPT_USE_UPSTREAMJOB_BUILDNUMBER == buildNumberUsageOption)
      return getBuildNumberFromUpStreamProject(upStreamJobName, build.getUpstreamBuilds(), listener);
    else if (OPT_USE_THIS_BUILD_NUMBER == buildNumberUsageOption)
      return build.number;
    else
      return -1;
  }

  private int getBuildNumberFromUpStreamProject(String projectName, Map<AbstractProject, Integer> upstreamBuilds, BuildListener listener) {
    for (AbstractProject<?,?> project : upstreamBuilds.keySet()) {
      if (project.getName().equals(projectName))
        return upstreamBuilds.get(project);
    }
    listener.error(MessageFormat.format(Messages.getString("SCTMExecutor.err.notAUpstreamJob"), projectName)); //$NON-NLS-1$
    return -1;
  }

  private static FilePath createResultDir(int currentBuildNo, AbstractBuild<?,?> build, BuildListener listener) throws IOException, InterruptedException {
    FilePath rootDir = build.getProject().getWorkspace();
    if (rootDir == null) {
      LOGGER.severe("Cannot write the result file because slave is not connected."); //$NON-NLS-1$
      listener.error(Messages.getString("SCTMExecutor.log.slaveNotConnected")); //$NON-NLS-1$
      throw new RuntimeException();
    }
    
    rootDir = new FilePath(rootDir, "SCTMResults"); //$NON-NLS-1$
    if (rootDir.exists()) {
      final String buildNo = String.valueOf(currentBuildNo);
      List<FilePath> list = rootDir.list(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.getName().matches("TEST-(\\p{Print}*)-"+buildNo+".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      });
      if (list.size() <= 0)
        rootDir.deleteContents();
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

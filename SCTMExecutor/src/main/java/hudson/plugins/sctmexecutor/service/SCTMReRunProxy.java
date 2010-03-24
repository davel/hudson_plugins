package hudson.plugins.sctmexecutor.service;

import hudson.FilePath;
import hudson.plugins.sctmexecutor.exceptions.SCTMException;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.borland.sctm.ws.execution.entities.ExecutionHandle;
import com.borland.sctm.ws.execution.entities.ExecutionResult;
import com.borland.sctm.ws.performer.SPNamedEntity;

public class SCTMReRunProxy implements ISCTMService {
  static final int MAXRERUN = 2;
  private ISCTMService target;
  private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor.sctmservice"); //$NON-NLS-1$
  
  public SCTMReRunProxy(ISCTMService target) {
    this.target = target;
  } 
  
  @Override
  public ExecutionResult getExecutionResult(ExecutionHandle handle) throws SCTMException {
    return doGetExecutionResult(handle, MAXRERUN);
  }
  
  private ExecutionResult doGetExecutionResult(ExecutionHandle handle, int tryCount) throws SCTMException {
    try {
      return target.getExecutionResult(handle);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("Fetch result for execution definition {0} failed. {1}", handle.getExecDefId(), tryMore));
        return doGetExecutionResult(handle, --tryCount);
      } else
        throw e;        
    }    
  }

  @Override
  public boolean isFinished(ExecutionHandle handle) throws SCTMException {
    return doIsFinished(handle, MAXRERUN);
  }
  
  private boolean doIsFinished(ExecutionHandle handle, int tryCount) throws SCTMException {
    try {
      return this.target.isFinished(handle);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("Fetch state of execution for execution definition {0} failed. {1}", handle.getExecDefId(), tryMore));
        return doIsFinished(handle, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public Collection<ExecutionHandle> start(int executionId) throws SCTMException {
    return doStart(executionId, MAXRERUN);
  }
  
  private Collection<ExecutionHandle> doStart(int executionId, int tryCount) throws SCTMException {
    try {
      return this.target.start(executionId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("Start execution for execution definition {0} failed. {1}", executionId, tryMore));
        return doStart(executionId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public Collection<ExecutionHandle> start(int executionId, String buildNumber) throws SCTMException {
    return doStart(executionId, buildNumber, MAXRERUN);
  }
  
  private Collection<ExecutionHandle> doStart(int executionId, String buildNumber, int tryCount) throws SCTMException {
    try {
      return this.target.start(executionId, buildNumber);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("Start execution for execution definition {0} failed. {1}", executionId, tryMore));
        return doStart(executionId, buildNumber, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public boolean addBuildNumber(String productName, String version, int buildNumber) throws SCTMException {
    return doAddBuildNumber(productName, version, buildNumber, MAXRERUN);
  }

  private boolean doAddBuildNumber(String product, String version, int buildNumber, int tryCount) throws SCTMException {
    try {
      return this.target.addBuildNumber(product, version, buildNumber);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("BuildNumber ''{0}'' cannot be added. {1}", buildNumber, tryMore));
        return doAddBuildNumber(product, version, buildNumber, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public boolean buildNumberExists(String productName, String version, int buildNumber) throws SCTMException {
    return doBuildNumberExists(productName, version, buildNumber, MAXRERUN);
  }

  private boolean doBuildNumberExists(String product, String version, int buildNumber, int tryCount) throws SCTMException {
    try {
      return this.target.buildNumberExists(product, version, buildNumber);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("BuildNumber ''{0}'' cannot be added. {1}", buildNumber, tryMore));
        return doBuildNumberExists(product, version, buildNumber, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public int getLatestSCTMBuildnumber(String productName, String version) throws SCTMException {
    return doGetLatestSCTMBuildnumber(productName, version, MAXRERUN);
  }
  
  private int doGetLatestSCTMBuildnumber(String productName, String version, int tryCount) throws SCTMException {
    try {
      return this.target.getLatestSCTMBuildnumber(productName, version);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("No BuildNumber available on SCTM. {0}", tryMore));
        return doGetLatestSCTMBuildnumber(productName, version, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public String getExecDefinitionName(int execDefId) throws SCTMException {
    return doGetExecDefinitionName(execDefId, MAXRERUN);
  }

  private String doGetExecDefinitionName(int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.getExecDefinitionName(nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("No BuildNumber available on SCTM. {0}", tryMore));
        return doGetExecDefinitionName(nodeId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public Collection<String> getAllVersions(int execDefId) throws SCTMException {
    return doGetAllVersions(execDefId, MAXRERUN);
  }

  private Collection<String> doGetAllVersions(int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.getAllVersions(nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("No version available on SCTM. {0}", tryMore));
        return doGetAllVersions(nodeId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public String getProductName(int nodeId) throws SCTMException {
    return doGetProductName(nodeId, MAXRERUN);
  }

  @Override
  public SPNamedEntity[] getResultFiles(int testDefRunId) throws SCTMException {
    return doGetResultFiles(testDefRunId, MAXRERUN);
  }
  
  private String doGetProductName(int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.getProductName(nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("No product name available on SCTM. {0}", tryMore));
        return doGetProductName(nodeId, --tryCount);
      } else
        throw e;
    }
  }
  
  private SPNamedEntity[] doGetResultFiles(int testDefRunId, int tryCount) throws SCTMException {
    try {
      return this.target.getResultFiles(testDefRunId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("Cannot load any result file from SCTM. {0}", tryMore));
        return doGetResultFiles(testDefRunId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public void loadResultFile(int fileId, FilePath fileName) {
    loadResultFile(fileId, fileName); // TODO: check if retry logic is needed
  }
}

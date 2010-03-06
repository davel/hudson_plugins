package hudson.plugins.sctmexecutor.service;

import hudson.plugins.sctmexecutor.exceptions.SCTMException;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.borland.sctm.ws.execution.entities.ExecutionHandle;
import com.borland.sctm.ws.execution.entities.ExecutionResult;

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
  public boolean addBuildNumber(int buildNumber, int nodeId) throws SCTMException {
    return doAddBuildNumber(buildNumber, nodeId, MAXRERUN);
  }

  private boolean doAddBuildNumber(int buildNumber, int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.addBuildNumber(buildNumber, nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("BuildNumber ''{0}'' cannot be added. {1}", buildNumber, tryMore));
        return doAddBuildNumber(buildNumber, nodeId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public boolean buildNumberExists(int buildNumber, int nodeId) throws SCTMException {
    return doBuildNumberExists(buildNumber, nodeId, MAXRERUN);
  }

  private boolean doBuildNumberExists(int buildNumber, int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.buildNumberExists(buildNumber, nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("BuildNumber ''{0}'' cannot be added. {1}", buildNumber, tryMore));
        return doBuildNumberExists(buildNumber, nodeId, --tryCount);
      } else
        throw e;
    }
  }

  @Override
  public int getLatestSCTMBuildnumber(int nodeId) throws SCTMException {
    return doGetLatestSCTMBuildnumber(nodeId, MAXRERUN);
  }
  
  private int doGetLatestSCTMBuildnumber(int nodeId, int tryCount) throws SCTMException {
    try {
      return this.target.getLatestSCTMBuildnumber(nodeId);
    } catch (SCTMException e) {
      if (tryCount > 0) {
        String tryMore = ""; //$NON-NLS-1$
        if (tryCount > 1)
          tryMore = "Try once more."; //$NON-NLS-1$
        LOGGER.log(Level.WARNING, MessageFormat.format("No BuildNumber available on SCTM. {0}", tryMore));
        return doGetLatestSCTMBuildnumber(nodeId, --tryCount);
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
  

}

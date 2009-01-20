package hudson.plugins.sctmexecutor;

import java.io.PrintStream;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.borland.tm.webservices.tmexecution.ExecutionHandle;
import com.borland.tm.webservices.tmexecution.ExecutionResult;
import com.borland.tm.webservices.tmexecution.ExecutionWebService;

final class ResultCollectorThread extends Thread {
  private static final int MAX_SLEEP = 60;
  private static final int MAX_RETRIES = 2;
  private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor"); //$NON-NLS-1$
  
  private ExecutionHandle handle;
  private ExecutionWebService execService;
  private long sleep = 5; // in s
  private ITestResultWriter writer;
  private PrintStream consolenLogger;
  private int retries;
  private ISessionHandler sessionHandler;
  
  public ResultCollectorThread(PrintStream logger, ExecutionWebService service, ISessionHandler sessionHandler, ExecutionHandle handle, ITestResultWriter writer) {
    super("SCTMExecutor.resultcollector"+handle.getExecDefId()); //$NON-NLS-1$
    this.consolenLogger = logger;
    this.handle = handle;
    this.execService = service;
    this.sessionHandler = sessionHandler;
    this.writer = writer;
  }
  
  /**
   * used only to make unit testing quicker 
   * @param sleep
   */
  void setSleep(long sleep) {
    this.sleep = sleep;
  }
  
  @Override
  public void run() {
    ExecutionResult result = null;
    int stateOfExecution = -2;
    long sessionId = -1;
    try {
      sessionId   = sessionHandler.getSessionId(sessionId);
      do {
        Thread.sleep(sleep*1000); // because sometime SCTM is too slow and the run is not created when we ask for a result
        stateOfExecution = execService.getStateOfExecution(sessionId, handle);
        if (stateOfExecution == -1) {
          result = execService.getExecutionResult(sessionId, handle);
          consolenLogger.println(MessageFormat.format(Messages.getString("ResultCollectorThread.log.resultReceived"), handle.getExecDefId())); //$NON-NLS-1$
        } else if (sleep < MAX_SLEEP) {
          sleep *= 2;
          if (sleep > MAX_SLEEP)
            sleep = MAX_SLEEP;
        }
      } while (result == null);
      
      this.writer.write(result);
    } catch (RemoteException e) {
      if (stateOfExecution == -1 && retries < MAX_RETRIES) { // we get a finished execution but no result, this seems to be a sctm bug/timing problem
        retries++;
        LOGGER.log(Level.WARNING, MessageFormat.format("Execution should be finished, but it is no result available for execution definition {0}. Try again {1}!", handle.getExecDefId(), retries));
        run();
      } else if (e.getMessage().contains("Not logged in.")) {
        LOGGER.log(Level.INFO, "Session lost - open new session by new login and try once more.");
        try {
          sessionId = sessionHandler.getSessionId(sessionId);
        } catch (RemoteException e1) {
          LOGGER.log(Level.INFO, e.getMessage());
          throw new RuntimeException(Messages.getString("ResultCollectorThread.err.collectingResultFailed"), e); //$NON-NLS-1$
        }
        run();
      } else {
        LOGGER.log(Level.SEVERE, "Remote call to SCTM failed during result collection."); //$NON-NLS-1$
        LOGGER.log(Level.INFO, e.getMessage());
        throw new RuntimeException(Messages.getString("ResultCollectorThread.err.collectingResultFailed"), e); //$NON-NLS-1$
      }
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Collecting results aborted."); //$NON-NLS-1$
      LOGGER.log(Level.INFO, e.getMessage());
      interrupt();
      throw new RuntimeException(Messages.getString("ResultCollectorThread.err.collectionResultAborted"), e); //$NON-NLS-1$
    }
  }
}

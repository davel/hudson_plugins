package hudson.plugins.vmware.labmgr;

import hudson.model.Computer;
import hudson.model.Slave;
import hudson.slaves.SlaveComputer;

import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.rpc.ServiceException;

import com.vmware.labmanager.Configuration;
import com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceLocator;
import com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap;

public class VMComputer extends SlaveComputer {

  private static final int ALLOW_TRAFFIC_INOUT = 4;
  private LabManager_x0020_SOAP_x0020_interfaceSoap labManager;
  private final String configurationName;
  private volatile Future<Integer> checkedOutConfigId;

  public VMComputer(Slave slave, URL url) {
    super(slave);
    configurationName = "myCOnfig";
    LabManager_x0020_SOAP_x0020_interfaceLocator locator = new LabManager_x0020_SOAP_x0020_interfaceLocator();
    try {
      labManager = locator.getLabManager_x0020_SOAP_x0020_interfaceSoap(url);
    } catch (ServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public Future<?> connect(boolean forceReconnect) {
    checkedOutConfigId = Computer.threadPoolForRemoting.submit(new java.util.concurrent.Callable<Integer>() {
      public Integer call() throws Exception {
          Configuration configuration = labManager.getSingleConfigurationByName(configurationName);
          int checkoutConfId = labManager.configurationCheckout(configuration.getId(), "hudsonConf"); // FIXME errorhandling
          labManager.configurationDeploy(checkoutConfId, false, ALLOW_TRAFFIC_INOUT);
          while(!configuration.isIsDeployed()) {
            Thread.sleep(1000);
          }
          return checkoutConfId;
      }
    });
      
    return super.connect(forceReconnect);
  }
  
  @Override
  public Future<?> disconnect() {
    Future<?> future = super.disconnect();
    Computer.threadPoolForRemoting.submit(new java.util.concurrent.Callable<Integer>() {
      public Integer call() throws Exception {
          labManager.configurationUndeploy(checkedOutConfigId.get());
          Configuration configuration = labManager.getSingleConfigurationByName(configurationName);
          while(configuration.isIsDeployed()) {
            Thread.sleep(1000);
          }
          labManager.configurationDelete(checkedOutConfigId.get());
          return null;
      }
    });
    
    return future;
  }
}

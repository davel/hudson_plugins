package com.vmware.labmanager;

public class LabManager_x0020_SOAP_x0020_interfaceSoapProxy implements com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap {
  private String _endpoint = null;
  private com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap labManager_x0020_SOAP_x0020_interfaceSoap = null;
  
  public LabManager_x0020_SOAP_x0020_interfaceSoapProxy() {
    _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
  }
  
  public LabManager_x0020_SOAP_x0020_interfaceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
  }
  
  private void _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy() {
    try {
      labManager_x0020_SOAP_x0020_interfaceSoap = (new com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceLocator()).getLabManager_x0020_SOAP_x0020_interfaceSoap();
      if (labManager_x0020_SOAP_x0020_interfaceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)labManager_x0020_SOAP_x0020_interfaceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)labManager_x0020_SOAP_x0020_interfaceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (labManager_x0020_SOAP_x0020_interfaceSoap != null)
      ((javax.xml.rpc.Stub)labManager_x0020_SOAP_x0020_interfaceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap getLabManager_x0020_SOAP_x0020_interfaceSoap() {
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap;
  }
  
  public com.vmware.labmanager.Configuration getConfiguration(int id) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.getConfiguration(id);
  }
  
  public com.vmware.labmanager.Configuration[] getConfigurationByName(java.lang.String name) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.getConfigurationByName(name);
  }
  
  public com.vmware.labmanager.Configuration getSingleConfigurationByName(java.lang.String name) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.getSingleConfigurationByName(name);
  }
  
  public com.vmware.labmanager.Configuration[] listConfigurations(int configurationType) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.listConfigurations(configurationType);
  }
  
  public void configurationPerformAction(int configurationId, int action) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.configurationPerformAction(configurationId, action);
  }
  
  public void configurationDeploy(int configurationId, boolean isCached, int fenceMode) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.configurationDeploy(configurationId, isCached, fenceMode);
  }
  
  public void configurationUndeploy(int configurationId) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.configurationUndeploy(configurationId);
  }
  
  public int configurationClone(int configurationId, java.lang.String newWorkspaceName) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.configurationClone(configurationId, newWorkspaceName);
  }
  
  public int configurationCapture(int configurationId, java.lang.String newLibraryName) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.configurationCapture(configurationId, newLibraryName);
  }
  
  public int configurationCheckout(int configurationId, java.lang.String workspaceName) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.configurationCheckout(configurationId, workspaceName);
  }
  
  public java.lang.String liveLink(java.lang.String configName) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.liveLink(configName);
  }
  
  public void configurationDelete(int configurationId) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.configurationDelete(configurationId);
  }
  
  public void configurationSetPublicPrivate(int configurationId, boolean isPublic) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.configurationSetPublicPrivate(configurationId, isPublic);
  }
  
  public com.vmware.labmanager.Machine[] listMachines(int configurationId) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.listMachines(configurationId);
  }
  
  public com.vmware.labmanager.Machine getMachine(int machineId) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.getMachine(machineId);
  }
  
  public com.vmware.labmanager.Machine getMachineByName(int configurationId, java.lang.String name) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    return labManager_x0020_SOAP_x0020_interfaceSoap.getMachineByName(configurationId, name);
  }
  
  public void machinePerformAction(int machineId, int action) throws java.rmi.RemoteException{
    if (labManager_x0020_SOAP_x0020_interfaceSoap == null)
      _initLabManager_x0020_SOAP_x0020_interfaceSoapProxy();
    labManager_x0020_SOAP_x0020_interfaceSoap.machinePerformAction(machineId, action);
  }
  
  
}
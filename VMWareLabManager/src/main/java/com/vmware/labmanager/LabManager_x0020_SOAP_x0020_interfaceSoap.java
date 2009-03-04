/**
 * LabManager_x0020_SOAP_x0020_interfaceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vmware.labmanager;

public interface LabManager_x0020_SOAP_x0020_interfaceSoap extends java.rmi.Remote {
    public com.vmware.labmanager.Configuration getConfiguration(int id) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Configuration[] getConfigurationByName(java.lang.String name) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Configuration getSingleConfigurationByName(java.lang.String name) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Configuration[] listConfigurations(int configurationType) throws java.rmi.RemoteException;
    public void configurationPerformAction(int configurationId, int action) throws java.rmi.RemoteException;
    public void configurationDeploy(int configurationId, boolean isCached, int fenceMode) throws java.rmi.RemoteException;
    public void configurationUndeploy(int configurationId) throws java.rmi.RemoteException;
    public int configurationClone(int configurationId, java.lang.String newWorkspaceName) throws java.rmi.RemoteException;
    public int configurationCapture(int configurationId, java.lang.String newLibraryName) throws java.rmi.RemoteException;
    public int configurationCheckout(int configurationId, java.lang.String workspaceName) throws java.rmi.RemoteException;
    public java.lang.String liveLink(java.lang.String configName) throws java.rmi.RemoteException;
    public void configurationDelete(int configurationId) throws java.rmi.RemoteException;
    public void configurationSetPublicPrivate(int configurationId, boolean isPublic) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Machine[] listMachines(int configurationId) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Machine getMachine(int machineId) throws java.rmi.RemoteException;
    public com.vmware.labmanager.Machine getMachineByName(int configurationId, java.lang.String name) throws java.rmi.RemoteException;
    public void machinePerformAction(int machineId, int action) throws java.rmi.RemoteException;
}

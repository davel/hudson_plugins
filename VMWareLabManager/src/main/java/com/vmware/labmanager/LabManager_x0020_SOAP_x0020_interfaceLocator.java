/**
 * LabManager_x0020_SOAP_x0020_interfaceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vmware.labmanager;

public class LabManager_x0020_SOAP_x0020_interfaceLocator extends org.apache.axis.client.Service implements com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interface {

/**
 * A web service to allow automation of operations in LabManager
 */

    public LabManager_x0020_SOAP_x0020_interfaceLocator() {
    }


    public LabManager_x0020_SOAP_x0020_interfaceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public LabManager_x0020_SOAP_x0020_interfaceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for LabManager_x0020_SOAP_x0020_interfaceSoap
    private java.lang.String LabManager_x0020_SOAP_x0020_interfaceSoap_address = "https://atlis-labmgr/LabManager/SOAP/LabManager.asmx";

    public java.lang.String getLabManager_x0020_SOAP_x0020_interfaceSoapAddress() {
        return LabManager_x0020_SOAP_x0020_interfaceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String LabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName = "LabManager_x0020_SOAP_x0020_interfaceSoap";

    public java.lang.String getLabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName() {
        return LabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName;
    }

    public void setLabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName(java.lang.String name) {
        LabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName = name;
    }

    public com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap getLabManager_x0020_SOAP_x0020_interfaceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(LabManager_x0020_SOAP_x0020_interfaceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getLabManager_x0020_SOAP_x0020_interfaceSoap(endpoint);
    }

    public com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap getLabManager_x0020_SOAP_x0020_interfaceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoapStub _stub = new com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoapStub(portAddress, this);
            _stub.setPortName(getLabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setLabManager_x0020_SOAP_x0020_interfaceSoapEndpointAddress(java.lang.String address) {
        LabManager_x0020_SOAP_x0020_interfaceSoap_address = address;
    }


    // Use to get a proxy class for LabManager_x0020_SOAP_x0020_interfaceSoap12
    private java.lang.String LabManager_x0020_SOAP_x0020_interfaceSoap12_address = "https://atlis-labmgr/LabManager/SOAP/LabManager.asmx";

    public java.lang.String getLabManager_x0020_SOAP_x0020_interfaceSoap12Address() {
        return LabManager_x0020_SOAP_x0020_interfaceSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String LabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName = "LabManager_x0020_SOAP_x0020_interfaceSoap12";

    public java.lang.String getLabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName() {
        return LabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName;
    }

    public void setLabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName(java.lang.String name) {
        LabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName = name;
    }

    public com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap getLabManager_x0020_SOAP_x0020_interfaceSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(LabManager_x0020_SOAP_x0020_interfaceSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getLabManager_x0020_SOAP_x0020_interfaceSoap12(endpoint);
    }

    public com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap getLabManager_x0020_SOAP_x0020_interfaceSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap12Stub _stub = new com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap12Stub(portAddress, this);
            _stub.setPortName(getLabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setLabManager_x0020_SOAP_x0020_interfaceSoap12EndpointAddress(java.lang.String address) {
        LabManager_x0020_SOAP_x0020_interfaceSoap12_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoapStub _stub = new com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoapStub(new java.net.URL(LabManager_x0020_SOAP_x0020_interfaceSoap_address), this);
                _stub.setPortName(getLabManager_x0020_SOAP_x0020_interfaceSoapWSDDServiceName());
                return _stub;
            }
            if (com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap12Stub _stub = new com.vmware.labmanager.LabManager_x0020_SOAP_x0020_interfaceSoap12Stub(new java.net.URL(LabManager_x0020_SOAP_x0020_interfaceSoap12_address), this);
                _stub.setPortName(getLabManager_x0020_SOAP_x0020_interfaceSoap12WSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("LabManager_x0020_SOAP_x0020_interfaceSoap".equals(inputPortName)) {
            return getLabManager_x0020_SOAP_x0020_interfaceSoap();
        }
        else if ("LabManager_x0020_SOAP_x0020_interfaceSoap12".equals(inputPortName)) {
            return getLabManager_x0020_SOAP_x0020_interfaceSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vmware.com/labmanager", "LabManager_x0020_SOAP_x0020_interface");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vmware.com/labmanager", "LabManager_x0020_SOAP_x0020_interfaceSoap"));
            ports.add(new javax.xml.namespace.QName("http://vmware.com/labmanager", "LabManager_x0020_SOAP_x0020_interfaceSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("LabManager_x0020_SOAP_x0020_interfaceSoap".equals(portName)) {
            setLabManager_x0020_SOAP_x0020_interfaceSoapEndpointAddress(address);
        }
        else 
if ("LabManager_x0020_SOAP_x0020_interfaceSoap12".equals(portName)) {
            setLabManager_x0020_SOAP_x0020_interfaceSoap12EndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

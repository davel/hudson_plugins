/**
 * Machine.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vmware.labmanager;

public class Machine  implements java.io.Serializable {
    private int id;

    private java.lang.String name;

    private java.lang.String description;

    private java.lang.String internalIP;

    private java.lang.String externalIP;

    private java.lang.String macAddress;

    private int memory;

    private int status;

    private boolean isDeployed;

    private int configID;

    private java.lang.String datastoreNameResidesOn;

    private java.lang.String hostNameDeployedOn;

    private java.lang.String ownerFullName;

    public Machine() {
    }

    public Machine(
           int id,
           java.lang.String name,
           java.lang.String description,
           java.lang.String internalIP,
           java.lang.String externalIP,
           java.lang.String macAddress,
           int memory,
           int status,
           boolean isDeployed,
           int configID,
           java.lang.String datastoreNameResidesOn,
           java.lang.String hostNameDeployedOn,
           java.lang.String ownerFullName) {
           this.id = id;
           this.name = name;
           this.description = description;
           this.internalIP = internalIP;
           this.externalIP = externalIP;
           this.macAddress = macAddress;
           this.memory = memory;
           this.status = status;
           this.isDeployed = isDeployed;
           this.configID = configID;
           this.datastoreNameResidesOn = datastoreNameResidesOn;
           this.hostNameDeployedOn = hostNameDeployedOn;
           this.ownerFullName = ownerFullName;
    }


    /**
     * Gets the id value for this Machine.
     * 
     * @return id
     */
    public int getId() {
        return id;
    }


    /**
     * Sets the id value for this Machine.
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * Gets the name value for this Machine.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Machine.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the description value for this Machine.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this Machine.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the internalIP value for this Machine.
     * 
     * @return internalIP
     */
    public java.lang.String getInternalIP() {
        return internalIP;
    }


    /**
     * Sets the internalIP value for this Machine.
     * 
     * @param internalIP
     */
    public void setInternalIP(java.lang.String internalIP) {
        this.internalIP = internalIP;
    }


    /**
     * Gets the externalIP value for this Machine.
     * 
     * @return externalIP
     */
    public java.lang.String getExternalIP() {
        return externalIP;
    }


    /**
     * Sets the externalIP value for this Machine.
     * 
     * @param externalIP
     */
    public void setExternalIP(java.lang.String externalIP) {
        this.externalIP = externalIP;
    }


    /**
     * Gets the macAddress value for this Machine.
     * 
     * @return macAddress
     */
    public java.lang.String getMacAddress() {
        return macAddress;
    }


    /**
     * Sets the macAddress value for this Machine.
     * 
     * @param macAddress
     */
    public void setMacAddress(java.lang.String macAddress) {
        this.macAddress = macAddress;
    }


    /**
     * Gets the memory value for this Machine.
     * 
     * @return memory
     */
    public int getMemory() {
        return memory;
    }


    /**
     * Sets the memory value for this Machine.
     * 
     * @param memory
     */
    public void setMemory(int memory) {
        this.memory = memory;
    }


    /**
     * Gets the status value for this Machine.
     * 
     * @return status
     */
    public int getStatus() {
        return status;
    }


    /**
     * Sets the status value for this Machine.
     * 
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }


    /**
     * Gets the isDeployed value for this Machine.
     * 
     * @return isDeployed
     */
    public boolean isIsDeployed() {
        return isDeployed;
    }


    /**
     * Sets the isDeployed value for this Machine.
     * 
     * @param isDeployed
     */
    public void setIsDeployed(boolean isDeployed) {
        this.isDeployed = isDeployed;
    }


    /**
     * Gets the configID value for this Machine.
     * 
     * @return configID
     */
    public int getConfigID() {
        return configID;
    }


    /**
     * Sets the configID value for this Machine.
     * 
     * @param configID
     */
    public void setConfigID(int configID) {
        this.configID = configID;
    }


    /**
     * Gets the datastoreNameResidesOn value for this Machine.
     * 
     * @return datastoreNameResidesOn
     */
    public java.lang.String getDatastoreNameResidesOn() {
        return datastoreNameResidesOn;
    }


    /**
     * Sets the datastoreNameResidesOn value for this Machine.
     * 
     * @param datastoreNameResidesOn
     */
    public void setDatastoreNameResidesOn(java.lang.String datastoreNameResidesOn) {
        this.datastoreNameResidesOn = datastoreNameResidesOn;
    }


    /**
     * Gets the hostNameDeployedOn value for this Machine.
     * 
     * @return hostNameDeployedOn
     */
    public java.lang.String getHostNameDeployedOn() {
        return hostNameDeployedOn;
    }


    /**
     * Sets the hostNameDeployedOn value for this Machine.
     * 
     * @param hostNameDeployedOn
     */
    public void setHostNameDeployedOn(java.lang.String hostNameDeployedOn) {
        this.hostNameDeployedOn = hostNameDeployedOn;
    }


    /**
     * Gets the ownerFullName value for this Machine.
     * 
     * @return ownerFullName
     */
    public java.lang.String getOwnerFullName() {
        return ownerFullName;
    }


    /**
     * Sets the ownerFullName value for this Machine.
     * 
     * @param ownerFullName
     */
    public void setOwnerFullName(java.lang.String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Machine)) return false;
        Machine other = (Machine) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.id == other.getId() &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.internalIP==null && other.getInternalIP()==null) || 
             (this.internalIP!=null &&
              this.internalIP.equals(other.getInternalIP()))) &&
            ((this.externalIP==null && other.getExternalIP()==null) || 
             (this.externalIP!=null &&
              this.externalIP.equals(other.getExternalIP()))) &&
            ((this.macAddress==null && other.getMacAddress()==null) || 
             (this.macAddress!=null &&
              this.macAddress.equals(other.getMacAddress()))) &&
            this.memory == other.getMemory() &&
            this.status == other.getStatus() &&
            this.isDeployed == other.isIsDeployed() &&
            this.configID == other.getConfigID() &&
            ((this.datastoreNameResidesOn==null && other.getDatastoreNameResidesOn()==null) || 
             (this.datastoreNameResidesOn!=null &&
              this.datastoreNameResidesOn.equals(other.getDatastoreNameResidesOn()))) &&
            ((this.hostNameDeployedOn==null && other.getHostNameDeployedOn()==null) || 
             (this.hostNameDeployedOn!=null &&
              this.hostNameDeployedOn.equals(other.getHostNameDeployedOn()))) &&
            ((this.ownerFullName==null && other.getOwnerFullName()==null) || 
             (this.ownerFullName!=null &&
              this.ownerFullName.equals(other.getOwnerFullName())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getId();
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getInternalIP() != null) {
            _hashCode += getInternalIP().hashCode();
        }
        if (getExternalIP() != null) {
            _hashCode += getExternalIP().hashCode();
        }
        if (getMacAddress() != null) {
            _hashCode += getMacAddress().hashCode();
        }
        _hashCode += getMemory();
        _hashCode += getStatus();
        _hashCode += (isIsDeployed() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += getConfigID();
        if (getDatastoreNameResidesOn() != null) {
            _hashCode += getDatastoreNameResidesOn().hashCode();
        }
        if (getHostNameDeployedOn() != null) {
            _hashCode += getHostNameDeployedOn().hashCode();
        }
        if (getOwnerFullName() != null) {
            _hashCode += getOwnerFullName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Machine.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vmware.com/labmanager", "Machine"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("internalIP");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "internalIP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalIP");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "externalIP"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("macAddress");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "macAddress"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("memory");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "memory"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isDeployed");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "isDeployed"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("configID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "configID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datastoreNameResidesOn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "DatastoreNameResidesOn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostNameDeployedOn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "HostNameDeployedOn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ownerFullName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://vmware.com/labmanager", "OwnerFullName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

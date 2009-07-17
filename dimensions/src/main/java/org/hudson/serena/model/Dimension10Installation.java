package org.hudson.serena.model;

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Dimensions server connection data holder.
 *
 * @author Jose Noheda [jose.noheda@gmail.com]
 */
public final class Dimension10Installation implements Serializable {

    private static final long serialVersionUID = 6989293467772538570L;

    private String name;
    private String user;
    private String dbName;
    private String server;
    private String password;
    private String dbConnection;

    @DataBoundConstructor
    public Dimension10Installation(String name, String server, String dbName, String dbConnection, String user, String password) {
        this.name = name;
        this.dbName = dbName;
        this.dbConnection = dbConnection;
        this.user = user;
        this.server = server;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(String dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String toString() {
        return name + "[server=" + server + ", DB=" + dbName + "@" + dbConnection + "s, user=" + user + "]";
    }

}

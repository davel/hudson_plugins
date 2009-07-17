package org.hudson.serena;

import org.hudson.serena.model.Dimension10Installation;
import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.DimensionsConnectionDetails;
import com.serena.dmclient.api.DimensionsConnectionManager;
import java.util.logging.Logger;

/**
 * Manages Dimensions connection API.
 *
 * @author Jose Noheda [jose.noheda@gmail.com]
 */
public final class ConnectionManager {

    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());

    private ConnectionManager() {
        throw new AssertionError("Do not try to instantiate this class");
    }

    public static DimensionsConnection getConnection(Dimension10Installation dim10) {
        return ConnectionManager.getConnection(dim10.getServer(), dim10.getDbName(), dim10.getDbConnection(), dim10.getUser(), dim10.getPassword());
    }

    public static DimensionsConnection getConnection(final String server, final String dbName, final String dbConnection, final String user, final String password) {
        try {
            DimensionsConnectionDetails connectionDetails = new DimensionsConnectionDetails();
            connectionDetails.setServer(server);
            connectionDetails.setDbName(dbName);
            connectionDetails.setDbConn(dbConnection);
            connectionDetails.setUsername(user);
            connectionDetails.setPassword(password);
            return DimensionsConnectionManager.getConnection(connectionDetails);
        } catch (Exception e) {
            LOGGER.warning("Could not obtain a connection to dimensions [" + server + "]: " + e.getMessage());
        }
        return null;
    }

    public static void close(DimensionsConnection connection) {
        connection.close();
    }

}

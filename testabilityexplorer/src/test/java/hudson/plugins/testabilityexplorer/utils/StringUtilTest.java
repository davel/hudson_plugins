package hudson.plugins.testabilityexplorer.utils;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests the StringUtil class.
 *
 * @author reik.schatz
 */
@Test
public class StringUtilTest
{
    public void testStripPackages()
    {
        Map<String, String> data = getTestData();
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            String source = entry.getKey();
            String expected = entry.getValue();
            String actual = StringUtil.stripPackages(source);
            assertEquals(actual, expected);
        }
    }

    public void testGetPackage(){
        Map<String, String> data = getPackageTestData();
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            String source = entry.getKey();
            String expected = entry.getValue();
            String actual = StringUtil.getPackage(source);
            assertEquals(actual, expected);
        }
    }

    private Map<String,String> getPackageTestData(){
        Map<String,String> data = new LinkedHashMap<String, String>();
        data.put("com.ongame.platform.opapi.migration.impl.OpapiMigrationServiceImpl", "com.ongame.platform.opapi.migration.impl");
        data.put("com.ongame.platform.Aloha", "com.ongame.platform");
        data.put("  com.ongame.platform.Aloha", "com.ongame.platform");
        data.put("  com.ongame.platform.Aloha  ", "com.ongame.platform");
        data.put("  com.ongame.platform.  Aloha", "com.ongame.platform");
        data.put("com.ongame.platform.Aloha;", "com.ongame.platform");
        data.put("  com.ongame.platform;Aloha  ", "com.ongame");
        data.put(" Just for fun ", "Just for fun");
        return data;
    }

    private Map<String, String> getTestData()
    {
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("com.ongame.platform.opapi.migration.impl.OpapiMigrationServiceImpl()", "OpapiMigrationServiceImpl()");
        data.put("com.ongame.platform.opapi.migration.impl.OpapiMigrationServiceImpl(com.ongame.platform.opapi.migration.dao.MigrationDataAccessManager, com.ongame.platform.playeradmin.IPlayerAdminService, com.ongame.platform.intelligence.IIntelligenceService)", "OpapiMigrationServiceImpl(MigrationDataAccessManager, IPlayerAdminService, IIntelligenceService)");
        data.put("java.lang.String connectAccount(com.ongame.platform.playeradmin.Player, com.ongame.platform.playeradmin.Player)", "String connectAccount(Player, Player)");
        data.put("com.ongame.platform.opapi.migrationapi.ConnectAccountResponse connectAccount1(com.ongame.platform.opapi.migrationapi.ConnectAccountRequest)", "ConnectAccountResponse connectAccount1(ConnectAccountRequest)");
        data.put("org.apache.log4j.Logger getLogger(java.lang.Class)", "Logger getLogger(Class)");
        data.put("com.ongame.platform.opapi.migration.impl.OpapiMigrationServiceImpl()", "OpapiMigrationServiceImpl()");
        data.put("com.ongame.platform.opapi.migration.common.ConnectedAccounts(java.lang.String)", "ConnectedAccounts(String)");
        data.put("com.ongame.platform.opapi.migration.common.ConnectedAccounts addAccount(com.ongame.platform.playeradmin.PlayerID)", "ConnectedAccounts addAccount(PlayerID)");
        data.put("com.ongame.platform.playeradmin.Player findPlayer(int, int)", "Player findPlayer(int, int)");
        data.put("javax.swing.JPanel getSDCContentPane()", "JPanel getSDCContentPane()");
        data.put("void start()", "void start()");
        data.put("com.siemens.sdc.server.ui.SDCFrame(com.siemens.sdc.server.common.SDCServerProperties, com.siemens.sdc.server.SDCServer, com.siemens.sdc.server.db.SDCDBConnectionPool, com.siemens.sdc.server.db.tables.SDCTables, com.siemens.sdc.server.comm.HL7MessageTransmitter, com.siemens.sdc.server.comm.SDCClientLoggerListener, com.siemens.sdc.server.ui.SDCDialogFactory, com.siemens.sdc.server.xml.settings.ClientSettingsXMLBuilder)", "SDCFrame(SDCServerProperties, SDCServer, SDCDBConnectionPool, SDCTables, HL7MessageTransmitter, SDCClientLoggerListener, SDCDialogFactory, ClientSettingsXMLBuilder)");
        data.put("java.awt.Component setTemporaryLostComponent(java.awt.Component)", "Component setTemporaryLostComponent(Component)");
        data.put("void setAlwaysOnTop(boolean)", "void setAlwaysOnTop(boolean)");
        data.put("org.apache.log4j.Logger access$000()", "Logger access$000()");
        data.put("void connectIfDisconnected()", "void connectIfDisconnected()");
        data.put("com.siemens.sdc.server.comm.HL7MessageTransmitter()", "HL7MessageTransmitter()");
        data.put("java.io.File[] getMessages()", "File[] getMessages()");
        data.put("", "");
        data.put("text", "text");
        data.put("tex t", "tex t");
        data.put(" tex t ", " tex t ");
        data.put("tex (t", "tex (t");
        data.put("tex (t", "tex (t");
        data.put("tex ()t", "tex ()t");
        data.put("(tex ()t", "(tex ()t");
        data.put(null, null);
        data.put("com.siemens.sdc.server.db.tables.SDCTablesFactory(com.siemens.sdc.server.db.SDCDBConnectionPool)", "SDCTablesFactory(SDCDBConnectionPool)");
        return data;
    }
}

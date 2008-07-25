package com.zanox.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * <p>
 * This class implements a integration test to test the ftp connection.
 * </p>
 * <p>
 * HeadURL: $HeadURL:
 * http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:51:01 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2445 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 * 
 */
public final class TestConnection {

    /**
     * Set the visibility of the default constructor to private because its a utility class.
     */
    private TestConnection() {

    }

    public static void main(String[] args)
        throws IOException {
        for (int i = 0; i < 100; i++) {
            FTPClient ftpClient = new FTPClient();
            ftpClient.setDefaultTimeout(1000);
            ftpClient.connect("l-jboss-02.zanox-live.de", 21);
            // ftpClient.setSoTimeout(timeout)
            ftpClient.login("anonymous", "asdasd@web.de");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            for (FTPFile file : ftpClient.listFiles()) {
                // System.out.println(file.getName());
            }
            System.out.println("finish");
        }
    }
}

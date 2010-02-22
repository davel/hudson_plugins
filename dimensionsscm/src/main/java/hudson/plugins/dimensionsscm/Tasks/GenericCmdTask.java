
/* ===========================================================================
 *  Copyright (c) 2007 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */

/*
 * This experimental plugin extends Hudson support for Dimensions SCM repositories
 *
 * @author Tim Payne
 *
 */

// Package name
package hudson.plugins.dimensionsscm;

// Dimensions imports
import hudson.plugins.dimensionsscm.Logger;


// Hudson imports
import hudson.Util;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.Node;
import hudson.model.Computer;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.Callable;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.model.TaskListener;

// General imports
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.Exception;
import java.util.Calendar;

/**
 * Class implementation of the command process.
 */
public class GenericCmdTask implements FileCallable<Boolean> {

    private static final long serialVersionUID = 1L;

    protected FilePath workspace = null;
    protected TaskListener listener = null;

    protected String userName = "";
    protected String passwd = "";
    protected String database = "";
    protected String server = "";
    protected int version = 2009;

    private String exec = "dmcli";


    /**
     * Utility routine to read file into memory
     *
     * @param fileName
     * @return File
     */
    protected final byte[] loadFile(final File fileName) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        byte [] bytes = new byte[(int) fileName.length()];
        bis.read(bytes);
        bis.close();
        return bytes;
    }

    /**
     * Utility routine to look for an executable in the path
     *
     * @param exeName
     * @return File
     */
     protected static File getExecutable(String exeName) {
        // Get the path environment
        String path = System.getenv("PATH");
        if (path == null)
            path = System.getenv("path");
        if (path == null)
            return null;

        // Split it into directories
        String[] pathDirs = path.split(File.pathSeparator);

        // Hunt through the directories to find the file I want
        File exe = null;
        for (String pathDir : pathDirs) {
            File file = new File(pathDir, exeName);
            if (file.isFile()) {
                exe = file;
                break;
            }
        }
        return exe;
     }


    /**
     * Utility routine to create parameter file for dmcli
     *
     * @return File
     * @throws IOException
     */
     protected File createParamFile()
            throws IOException {
        Calendar nowDateCal = Calendar.getInstance();
        File logFile = new File("a");
        FileWriter logFileWriter = null;
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = logFile.createTempFile("dmCm"+nowDateCal.getTimeInMillis(),null,null);
            logFileWriter = new FileWriter(tmpFile);
            fmtWriter = new PrintWriter(logFileWriter,true);
            fmtWriter.println("-host "+ server);
            fmtWriter.println("-user "+ userName);
            fmtWriter.println("-pass "+ passwd);
            fmtWriter.println("-dbname "+ database);
            fmtWriter.flush();
        } catch (Exception e) {
            throw new IOException("Unable to write command log - " + e.getMessage());
        } finally {
            fmtWriter.close();
        }

        return tmpFile;
    }

    /*
     * Default constructor
     */
    public GenericCmdTask() {
    }

    /*
     * Constructor
     */
    public GenericCmdTask(String userName, String passwd,
                          String database, String server,
                          int version,
                          FilePath workspace,
                          TaskListener listener) {

        this.workspace = workspace;
        this.listener = listener;

        // Server details
        this.userName = userName;
        this.passwd = passwd;
        this.database = database;
        this.server = server;
        this.version = version;
    }


    /*
     * Invoke method
     *
     * @param File
     * @param VirtualChannel
     * @return boolean
     * @throws IOException
     */
    public Boolean invoke(File area, VirtualChannel channel)
              throws IOException {

        boolean retStatus = false;

        // This here code is executed on the slave.
        try {
            listener.getLogger().println("[DIMENSIONS] Running build in '" + area.getAbsolutePath() + "'...");
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                exec += ".exe";
            }
            File exe = getExecutable(exec);
            if (exe == null) {
                listener.getLogger().println("[DIMENSIONS] Error: Cannot locate '" + exec + "' on the slave node.");
            } else {
                listener.getLogger().println("[DIMENSIONS] Located '" + exe.getAbsolutePath() + "' on the slave node.");

                File param = createParamFile();
                if (param == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create parameter file for Dimensions login.");
                    return false;
                }

                retStatus = execute(exe,param,area);
                param.delete();
            }
            return retStatus;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run command callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run command callout - " + e.getMessage());
            return false;
        }
    }

    /*
     * Process the task
     *
     * @param File
     * @param File
     * @param File
     * @return boolean
     */
    public Boolean execute(final File exe, final File param, final File area)
                throws IOException {
        return true;
    }
}



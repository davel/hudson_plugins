/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.copyarchiver;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.FilePath.TarCompression;
import hudson.Functions;
import hudson.Util;
import hudson.model.Hudson;
import hudson.remoting.Future;
import hudson.remoting.Pipe;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import static hudson.util.jna.GNUCLibrary.LIBC;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FilePathArchiver implements Serializable {


    FilePath filePath;

    public FilePathArchiver(FilePath filePath) {
        this.filePath = filePath;
    }

    class CopyImpl extends Copy {
        private int copySize;

        public CopyImpl() {
            setProject(new org.apache.tools.ant.Project());
        }

        protected void doFileOperations() {
            copySize = super.fileCopyMap.size();
            super.doFileOperations();
        }

        public int getNumCopied() {
            return copySize;
        }
    }

    public int copyRecursiveTo(final boolean flatten, final String fileMask, final String excludes, final FilePath target) throws IOException, InterruptedException {
        if (filePath.getChannel() == target.getChannel()) {
            // local to local copy.
            return filePath.act(new FileCallable<Integer>() {
                public Integer invoke(File base, VirtualChannel channel) throws IOException {
                    if (!base.exists()) return 0;
                    assert target.getChannel() == null;

                    try {


                        CopyImpl copyTask = new CopyImpl();
                        copyTask.setTodir(new File(target.getRemote()));
                        copyTask.addFileset(Util.createFileSet(base, fileMask, excludes));
                        copyTask.setIncludeEmptyDirs(false);
                        copyTask.setFlatten(flatten);
                        copyTask.execute();

                        return copyTask.getNumCopied();
                    } catch (BuildException e) {
                        throw new IOException2("Failed to copy " + base + "/" + fileMask + " to " + target, e);
                    }
                }
            });
        } else if (filePath.getChannel() == null) {
            // local -> remote copy
            final Pipe pipe = Pipe.createLocalToRemote();

            FilePath targetTemp = new FilePath(Util.createTempDir());

            Future<Void> future = targetTemp.actAsync(new FileCallable<Void>() {
                public Void invoke(File f, VirtualChannel channel) throws IOException {
                    try {
                        readFromTar(filePath.getRemote() + '/' + fileMask, f, TarCompression.GZIP.extract(pipe.getIn()));
                        return null;
                    } finally {
                        pipe.getIn().close();
                    }
                }
            });
            int r = writeToTar(new File(filePath.getRemote()), fileMask, excludes, TarCompression.GZIP.compress(pipe.getOut()));
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new IOException2(e);
            }


            CopyImpl copyTask = new CopyImpl();
            copyTask.setTodir(new File(target.getRemote()));
            copyTask.addFileset(Util.createFileSet(new File(targetTemp.toURI()), "**/*", null));
            copyTask.setIncludeEmptyDirs(false);
            copyTask.setFlatten(flatten);
            copyTask.execute();

            targetTemp.delete();

            return r;
        } else {
            // remote -> local copy
            final Pipe pipe = Pipe.createRemoteToLocal();

            FilePath targetTemp = new FilePath(Util.createTempDir());

            Future<Integer> future = filePath.actAsync(new FileCallable<Integer>() {
                public Integer invoke(File f, VirtualChannel channel) throws IOException {
                    try {
                        return writeToTar(f, fileMask, excludes, TarCompression.GZIP.compress(pipe.getOut()));
                    } finally {
                        pipe.getOut().close();
                    }
                }
            });
            try {
                readFromTar(filePath.getRemote() + '/' + fileMask, new File(targetTemp.getRemote()), TarCompression.GZIP.extract(pipe.getIn()));
            } catch (IOException e) {// BuildException or IOException
                try {
                    future.get(3, TimeUnit.SECONDS);
                    throw e;    // the remote side completed successfully, so the error must be local
                } catch (ExecutionException x) {
                    // report both errors
                    throw new IOException2(Functions.printThrowable(e), x);
                } catch (TimeoutException _) {
                    // remote is hanging
                    throw e;
                }
            }
            try {
                future.get();
                CopyImpl copyTask = new CopyImpl();
                copyTask.setTodir(new File(target.getRemote()));
                copyTask.addFileset(Util.createFileSet(new File(targetTemp.toURI()), "**/*.*", null));
                copyTask.setIncludeEmptyDirs(false);
                copyTask.setFlatten(flatten);
                copyTask.execute();

                targetTemp.delete();

                return copyTask.getNumCopied();

            } catch (ExecutionException e) {
                throw new IOException2(e);
            }
        }
    }

    /**
     * Writes to a tar stream and stores obtained files to the base dir.
     *
     * @return number of files/directories that are written.
     */
    private Integer writeToTar(File baseDir, String fileMask, String excludes, OutputStream out) throws IOException {
        FileSet fs = Util.createFileSet(baseDir, fileMask, excludes);

        byte[] buf = new byte[8192];

        TarOutputStream tar = new TarOutputStream(new BufferedOutputStream(out));
        tar.setLongFileMode(TarOutputStream.LONGFILE_GNU);
        String[] files;
        if (baseDir.exists()) {
            DirectoryScanner ds = fs.getDirectoryScanner(new org.apache.tools.ant.Project());
            files = ds.getIncludedFiles();
        } else {
            files = new String[0];
        }
        for (String f : files) {
            if (Functions.isWindows())
                f = f.replace('\\', '/');

            File file = new File(baseDir, f);

            TarEntry te = new TarEntry(f);
            te.setModTime(file.lastModified());
            if (!file.isDirectory())
                te.setSize(file.length());

            tar.putNextEntry(te);

            if (!file.isDirectory()) {
                FileInputStream in = new FileInputStream(file);
                int len;
                while ((len = in.read(buf)) >= 0)
                    tar.write(buf, 0, len);
                in.close();
            }

            tar.closeEntry();
        }

        tar.close();

        return files.length;
    }

    /**
     * Reads from a tar stream and stores obtained files to the base dir.
     */
    private static void readFromTar(String name, File baseDir, InputStream in) throws IOException {
        TarInputStream t = new TarInputStream(in);
        try {
            TarEntry te;
            while ((te = t.getNextEntry()) != null) {
                File f = new File(baseDir, te.getName());
                if (te.isDirectory()) {
                    f.mkdirs();
                } else {
                    File parent = f.getParentFile();
                    if (parent != null) parent.mkdirs();

                    OutputStream fos = new FileOutputStream(f);
                    try {
                        IOUtils.copy(t, fos);
                    } finally {
                        fos.close();
                    }
                    f.setLastModified(te.getModTime().getTime());
                    int mode = te.getMode() & 0777;
                    if (mode != 0 && !Hudson.isWindows()) // be defensive
                        try {
                            LIBC.chmod(f.getPath(), mode);
                        } catch (NoClassDefFoundError e) {
                            // be defensive. see http://www.nabble.com/-3.0.6--Site-copy-problem%3A-hudson.util.IOException2%3A--java.lang.NoClassDefFoundError%3A-Could-not-initialize-class--hudson.util.jna.GNUCLibrary-td23588879.html
                        }
                }
            }
        } catch (IOException e) {
            throw new IOException2("Failed to extract " + name, e);
        } finally {
            t.close();
        }
    }


}

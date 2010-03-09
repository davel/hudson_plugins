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

package com.thalesgroup.hudson.plugins.ucm4svn;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;

import java.io.BufferedReader;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;


public class UCM4SVNChangeLogSet extends ChangeLogSet<ChangeLogSet.Entry> {


    private static final Logger LOGGER = Logger.getLogger(UCM4SVNChangeLogSet.class.getName());

    private List<Entry> items = new ArrayList<Entry>();

    public UCM4SVNChangeLogSet(final AbstractBuild build, final File changelogFile) {
        super(build);

        String line;
        BufferedReader reader = null;
       // try {
            /*
            reader = new BufferedReader(new FileReader(changelogFile));
            while((line = reader.readLine()) != null) {
                if (line.indexOf("Updated") >= 0) {
                    final String path = line.substring(line.indexOf('\'') + 1, line.indexOf("' using"));
                    final String item = line.substring(line.indexOf("Item"));
                    items.add(new ChangeLogSet.Entry() {

                        @Override public String getMsg() {
                            return "Updated [" + path + "];" + item;
                        }

                        @Override public User getAuthor() {
                            return null;
                        }

                        @Override public Collection<String> getAffectedPaths() {
                            return Arrays.asList(path);
                        }

                    });
                }
                */

            for (int i = 0; i < 10; i++) {

                final int j = i;

                items.add(new ChangeLogSet.Entry() {


                    @Override
                    public String getMsg() {
                        return "Updated [" + j + "];" + "toto_" + j;
                    }

                    @Override
                    public User getAuthor() {
                        return null;
                    }

                    @Override
                    public Collection<String> getAffectedPaths() {
                        return Arrays.asList("My PATH");
                    }

                });

            }
            /*
        } catch (IOException ioe) {
            LOGGER.warning("Unexpected exception reading Dimensions change log file: " + ioe.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                LOGGER.warning("Could not close Dimensions change log file: " + ioe.getMessage());
            }
        }

*/

    }

    @Override
    public boolean isEmptySet() {
        return items.size() == 0;
    }


    public Iterator<Entry> iterator() {
        return items.iterator();
    }


}


/*
 * The MIT License
 *
 * Copyright (c) 2010, Manufacture Française des Pneumatiques Michelin, Romain Seguy
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.michelin.cio.hudson.plugins.clearcaseucmbaseline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Romain Seguy (http://davadoc.deviantart.com)
 */
public class ClearCaseUcmBaselineUtils {

    public static String prefixWithSlash(final String s) {
        if(s != null && s.length() > 0 && s.startsWith("\\")) {
            return '/' + s.substring(1);
        }
        if(s != null && s.length() > 0 && !s.startsWith("/")) {
            return '/' + s;
        }
        return s;
    }

    /**
     * Processes the cleartool output produced when invoking {@code launcher.run()}
     * to return it as a plain {@link String}.
     */
    public static String processCleartoolOuput(ByteArrayOutputStream baos) throws IOException {
        StringBuilder cleartoolOutput = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
        String line = reader.readLine();
        while(line != null) {
            if(cleartoolOutput.length() > 0) {
                cleartoolOutput.append('\n');
            }
            cleartoolOutput.append(line);
            line = reader.readLine();
        }
        reader.close();

        return cleartoolOutput.toString();
    }

}

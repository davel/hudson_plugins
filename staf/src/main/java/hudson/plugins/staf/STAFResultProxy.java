/*
 * The MIT License
 *
 * Copyright (c) 2010, Gregory Covert Smith
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

package hudson.plugins.staf;

import com.ibm.staf.STAFResult;
import java.io.Serializable;

/**
 * The use of any STAFResult in the Hudson JVM causes the JSTAF library
 * to attempt to load the STAF jni libraries.  We only want the
 * JSTAF libraries loaded in our launched JVMs, so we use this object
 * to proxy any results in those JVMs back to the runtime in Hudson.
 */
public class STAFResultProxy implements Serializable {

    final int rc;
    final String result;
    final Object resultObj;

    public STAFResultProxy(STAFResult stafResult) {
        rc = stafResult.rc;
        result = stafResult.result;
        resultObj = stafResult.resultObj;
    }

    public STAFResultProxy(int rc, String result, Object resultObj) {
        this.rc = rc;
        this.result = result;
        this.resultObj = resultObj;
    }

    public int getRc() {
        return rc;
    }

    public String getResult() {
        return result;
    }

    public Object getResultObj() {
        return resultObj;
    }
}

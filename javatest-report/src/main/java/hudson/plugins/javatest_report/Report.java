/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package hudson.plugins.javatest_report;

import hudson.model.AbstractBuild;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Root of the SQE test report.
 *
 * <p>
 * A {@link Report} is a set of {@link Suite}s.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Report extends TestCollection<Report,Suite> {
    // parent doesn't exist. so specify dummy

    private final JavaTestAction owner;

    public Report(JavaTestAction owner) {
        this.owner = owner;
        setName("JavaTest Result");
        setId("root");
    }

    @Override
    public AbstractBuild getOwner() {
        return owner.owner;
    }

    @Override
    public Report getPreviousResult() {
        JavaTestAction p = owner.getPreviousResult();
        if(p!=null)
            return p.getResult();
        else
            return null;
    }

    /**
     * Loads SQE report file into this {@link Report} object.
     */
    public void add( File reportXml ) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setClassLoader(getClass().getClassLoader());

        digester.push(this);

        digester.addObjectCreate("*/testsuite",Suite.class);
        digester.addObjectCreate("*/test",Test.class);
        digester.addObjectCreate("*/testcase",TestCase.class);
        digester.addSetNext("*/testsuite","add");
        digester.addSetNext("*/test","add");
        digester.addSetNext("*/testcase","add");

        // common properties applicable to more than one TestObjects.
        digester.addSetProperties("*/testsuite");
        digester.addSetProperties("*/test");
        digester.addSetProperties("*/testcase");
        digester.addBeanPropertySetter("*/name");
        digester.addBeanPropertySetter("*/description");
        digester.addSetProperties("*/status","value","statusString");
        digester.addCallMethod("*/attribute", "addAttribute", 2);
        digester.addCallParam("*/attribute/name", 0);
        digester.addCallParam("*/attribute/value", 1);

        digester.setValidating(false);
        digester.parse(reportXml);
    }

    public String getChildTitle() {
        return "Test Suite";
    }
}

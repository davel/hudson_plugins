package hudson.plugins.hgca;

import hudson.MarkupText;

import junit.framework.TestCase;
import java.util.HashMap;

public class HGCALinkAnnotatorTest extends TestCase {

    private static final String TEST_LINK_A = "http://cdetsweb-prd.cisco.com/apps/dumpcr?identifier=";
    private static final String TEST_PATT_A = "(CSC\\w{2}\\d{5})";
    private static final String TEST_LINK_B = "http://some/site?query=";
    private static final String TEST_PATT_B = "\\d+(foo)\\d+";

    public void testLinkSyntax() {
        assertAnnotatedTextEquals("Nothing here.", "Nothing here.");
        assertAnnotatedTextEquals("Text with bug CSCxy12345.", "Text with bug <a href='" + TEST_LINK_A + "CSCxy12345'>CSCxy12345</a>.");
        assertAnnotatedTextEquals("Banana banana foo foo banana", "Banana banana foo foo banana");
        assertAnnotatedTextEquals("Banana banana 22foo22 foo banana", "Banana banana <a href='" + TEST_LINK_B + "foo'>22foo22</a> foo banana");
        
    }

    private void assertAnnotatedTextEquals(String originalText, String expectedAnnotatedText) {
        MarkupText markupText = new MarkupText(originalText);
        HashMap<String,String> annos = new HashMap<String,String>();
        annos.put(TEST_PATT_A, TEST_LINK_A + "$1");
        annos.put(TEST_PATT_B, TEST_LINK_B + "$1");

        HGCALinkAnnotator annotator = new HGCALinkAnnotator();
        annotator.annotate(annos, markupText);

        assertEquals(expectedAnnotatedText, markupText.toString());
    }
}

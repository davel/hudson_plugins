package hudson.plugins.seleniumGrails;

import hudson.tasks.test.AbstractTestResultAction;
import hudson.model.AbstractBuild;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class SeleniumGrailsTestResultAction extends AbstractTestResultAction {

	private String resultFile;

	private int numPassed;
	private int numFailed;
	private int totalTime;
	private String[] details;
	

	public SeleniumGrailsTestResultAction(AbstractBuild build, String resultFile){
		super(build);
		this.resultFile = resultFile;
		parseResults(resultFile);
	}

	public int getTotalCount() {
		return numPassed + numFailed;
	}

	public int getFailCount(){
		return numFailed;
	}

	public Object	getResult() {
		SeleniumGrailsResultBean result = new SeleniumGrailsResultBean();
		result.setPassed(numPassed);
		result.setFailed(numFailed);
		result.setDetails(details);
		return result;
	}

	private void parseResults(String resultFile){
		try{
			Document resultDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultFile);
			NodeList summary = resultDoc.getElementsByTagName("summary");
			NodeList summaryChildren = summary.item(0).getChildNodes();

			for(int i = 0; i < summaryChildren.getLength(); i++){
				Node n = summaryChildren.item(i);
				if(n.getNodeName().equals("numTestPasses")) numPassed = Integer.parseInt(n.getTextContent());
				if(n.getNodeName().equals("numTestFailures")) numFailed = Integer.parseInt(n.getTextContent());
				if(n.getNodeName().equals("totalTime")) totalTime = Integer.parseInt(n.getTextContent());
			}
		  NodeList tests = resultDoc.getElementsByTagName("tests");
			NodeList testsChildren = tests.item(0).getChildNodes();
			details = new String[testsChildren.getLength()];
			for(int i = 0; i < testsChildren.getLength(); i++){
				details[i] = testsChildren.item(i).getTextContent();
			}

		} catch(Exception e){
			e.printStackTrace();
		}
	}


}

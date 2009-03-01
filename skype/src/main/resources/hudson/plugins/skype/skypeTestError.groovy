/*
 * if test error occured that error messsage  send to skype
 *
 * @author udagawa
 */
import com.skype.*;
import groovy.util.*;
import javax.xml.transform.*
import javax.xml.transform.stream.*

class SkypeUtil {
	def sendMessage(String chatName, String message) {
	    Chat[] chats = Skype.getAllChats();
	    for (Chat chat : chats) {
	        String windowTitle = chat.getWindowTitle();
	        // windowTitle : username | title  or title
	        String[] titles = chat.getWindowTitle().split(" \\| ");
	        if (titles.length == 2) {
	            windowTitle = titles[1];
	        }
	        if (chatName.equals(windowTitle)) {
	            chat.send(message);
	            return;
	        }
	    }
	}
}

class NUnitReportTransformer {
//	def XSLT_FILE = 'nunit-to-junit.xsl'
	def XSLT_FILE = '/plugins/skype/WEB-INF/classes/hudson/plugins/skype/nunit-to-junit.xsl'
	def CONVERT_FILE_SUFFIX = '.nunit-to-junit.xml'
	def FILE_ENCODING = 'UTF-8'
	def hudsonHome;
	def transformer;

	def createTransformer() {
		if (transformer == null) {
//			source = new StreamSource(this.getClass().getResourceAsStream(XSLT_FILE));
			transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(hudsonHome + XSLT_FILE));
		}
		return transformer;
	}

	def transfer(reader, writer) {
		createTransformer().transform(
        	new StreamSource(reader), 
        	new StreamResult(writer))
	}
	
	def convertNunitToJunit(fileName, outputDir) {
		def nunitReport = false;
		new File(fileName).withReader(FILE_ENCODING) { reader ->
			try {
				def xml = new XmlSlurper().parse(reader);
				if (!xml.'test-suite'.isEmpty()) {
					nunitReport = true;
				}
			} catch (ignore) { // org.xml.sax.SAXParseException
			}
		}
		if (!nunitReport) {
			return fileName
		}

		def file = new File(fileName).getName();
		def outputFile = new File(outputDir, file + CONVERT_FILE_SUFFIX).getAbsoluteFile().toString();
		new File(fileName).withReader(FILE_ENCODING) { reader ->
			new File(outputFile).withWriter(FILE_ENCODING) { writer ->
				try {
					transfer(reader, writer);
					fileName = outputFile;
				} catch (e) {
					println(e);
				}
			}
		}
		return fileName
	}
}

def class SkypeTestErrorPlugin {
	def debug = false;
	def skypeConfig;
	def message = ""
	def lineSeparator = System.getProperty('line.separator')
	def stackTraceLimitLength = 300; // stack trace is too long
	def unitTests = [
		'junit':false,
		'flexunit':false,
		'phpunit':false,
		'nunit':false,
	]
	def transformer = new NUnitReportTransformer()

	def SkypeTestErrorPlugin(skypeConfig_) {
		skypeConfig = skypeConfig_
		if (debug) {
			println("skype test error started. build is ${skypeConfig.build}. chat name is ${skypeConfig.chatName}. unittest pattern is ${skypeConfig.unitTestPattern}.")
		}
		unitTests.each { key, value -> 
			if (key =~ /${skypeConfig.unitTestPattern}/) {
				unitTests[key] = true;
			}
		}
	}
	
	def checkTestCase(testcase) {
		if (testcase.failure.isEmpty()) {
			return
		}
		message += "${testcase.@classname.text()}.${testcase.@name.text()}" + lineSeparator
		def stackTrace = testcase.failure.toString();
		if (stackTrace.length() > stackTraceLimitLength) {
			message += "${stackTrace.substring(0, stackTraceLimitLength)}" + "..." + lineSeparator;
		}
		else {
			message += stackTrace + lineSeparator;
		}
//		println("[message]\n" + message)
	}

	def createMessage() {

		skypeConfig.build.getProject().getWorkspace().list(skypeConfig.testFilePattern).each { fileName ->
			def file = new File(fileName.toString());
			if (file.lastModified() > skypeConfig.build.due().getTimeInMillis()) {
				if (unitTests.nunit) {
					// xlst
					transformer.hudsonHome = skypeConfig.build.getEnvVars().HUDSON_HOME
					file = transformer.convertNunitToJunit(fileName.toString(), skypeConfig.build.getRootDir().toString());
				}
				if (debug) {
					println("check file = " + file)
				}
				new File(file).withReader("UTF-8") { reader ->
					def xml = new XmlSlurper().parse(reader);

					// junit(ant report, mvn report) and flexunit support. report inlcudes testsuite. testsuite/testcase/failure
					if (unitTests.junit || unitTests.flexunit) {
						xml.testcase.each { testcase ->
							checkTestCase(testcase);
						}
					}

					// phpunit(phing phpunit2 task) and nunit support.  report inlcudes testsuites. testsuites/testsuite/testcase/failure
					if (unitTests.phpunit || unitTests.nunit) {
						xml.testsuite.each { suite ->
							suite.testcase.each { testcase ->
								checkTestCase(testcase);
							}
						}
					}
				}
			}
		}
		return message != ""
	}
	
	def sendMessage() {
		if (debug) {
			println("skype test error end. build is ${skypeConfig.build}. chat name is ${skypeConfig.chatName}. ${message}")
		}
		new SkypeUtil().sendMessage(skypeConfig.chatName, "[${skypeConfig.build.project.name}]Test Error" + lineSeparator + message);
	}
}

def plugin = new SkypeTestErrorPlugin(skypeConfig)
if (plugin.createMessage()) {
	plugin.sendMessage()
}

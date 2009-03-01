/*
 * build status send to skype
 *
 * @author udagawa
 */
import com.skype.*;
import com.skype.connector.*;

def class SkypeUtil {
	def setMoodMessage(String moodMessage) {
	    Skype.getProfile().setMoodMessage(moodMessage);
	}
}

def class SkypeBuildStatusPlugin {
	def debug = false;
	def message = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()).toString();
	def skypeConfig;

	def SkypeBuildStatusPlugin(skypeConfig_) {
		skypeConfig = skypeConfig_;
		if (debug) {
			println("skype build status started. build is ${skypeConfig.build}");
		}
	}
	
	def createMessage() {
		if (skypeConfig.launcher == null) {
			message += " " + skypeConfig.build.project.name + " starting...";
		}
		else {
			message += " " + skypeConfig.build.project.name + " end. result = " + skypeConfig.build.result;
		}
		return true;
	}
	
	def sendMessage() {
		if (debug) {
			println("skype build status end. build is ${skypeConfig.build}. ${message}");
		}
		new SkypeUtil().setMoodMessage(message);
	}
}

def plugin = new SkypeBuildStatusPlugin(skypeConfig);
if (plugin.createMessage()) {
	plugin.sendMessage();
}

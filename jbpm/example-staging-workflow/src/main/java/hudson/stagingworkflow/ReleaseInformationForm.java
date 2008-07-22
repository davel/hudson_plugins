/* 
 * Copyright 2008 Tom Huybrechts and hudson.dev.java.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.  
 * 
 */
package hudson.stagingworkflow;

import hudson.jbpm.model.Form;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ReleaseInformationForm extends Form {

	private String releaseVersion, nextDevelopmentVersion,
			voteEmailAddress, releaseEmailAddress;

	public String getVoteAnnouncementEmailAddress() {
		return voteEmailAddress;
	}

	public void setVoteAnnouncementEmailAddress(String voteAnnouncementEmailAddress) {
		this.voteEmailAddress = voteAnnouncementEmailAddress;
	}

	public String getReleaseAnnouncementEmailAddress() {
		return releaseEmailAddress;
	}


	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public String getVoteEmailAddress() {
		return voteEmailAddress;
	}

	public void setVoteEmailAddress(String voteEmailAddress) {
		this.voteEmailAddress = voteEmailAddress;
	}

	public String getReleaseEmailAddress() {
		return releaseEmailAddress;
	}

	public void setReleaseEmailAddress(String releaseEmailAddress) {
		this.releaseEmailAddress = releaseEmailAddress;
	}

	public String getNextDevelopmentVersion() {
		return nextDevelopmentVersion;
	}

	public void setNextDevelopmentVersion(String nextDevelopmentVersion) {
		this.nextDevelopmentVersion = nextDevelopmentVersion;
	}

	public ReleaseInformationForm(TaskInstance taskInstance) {
		super(taskInstance);
	}

	@Override
	public void handle(StaplerRequest request, StaplerResponse response)
			throws ServletException, IOException {
		TaskInstance ti = getTaskInstance();
		ti.setVariable("releaseVersion", releaseVersion);
		ti.setVariable("nextDevelopmentVersion", nextDevelopmentVersion);
		ti.setVariable("voteEmailAddress", voteEmailAddress);
		ti.setVariable("releaseEmailAddress", releaseEmailAddress);

		JbpmContext context = JbpmConfiguration.getInstance()
				.getCurrentJbpmContext();
		ti.end();
		context.save(ti);
	}

}

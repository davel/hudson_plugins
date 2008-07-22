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

import org.jbpm.taskmgmt.exe.TaskInstance;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class VoteForm extends Form {

	private String vote;
	private String comment;

	public String getVote() {
		return vote;
	}

	public void setVote(String vote) {
		this.vote = vote;
	}

	public void handle(StaplerRequest request, StaplerResponse response)
			throws ServletException, IOException {
		
		TaskInstance ti = getTaskInstance();
		String actorId = ti.getActorId();
		ti.setVariable("vote-" + actorId, vote);
		ti.setVariable("comment-" + actorId, comment);
		
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public VoteForm(TaskInstance taskInstance) {
		super(taskInstance);
		
		vote = (String) taskInstance.getVariable("vote-" + taskInstance.getActorId());
		comment = (String) taskInstance.getVariable("comment-" + taskInstance.getActorId());
	}

}

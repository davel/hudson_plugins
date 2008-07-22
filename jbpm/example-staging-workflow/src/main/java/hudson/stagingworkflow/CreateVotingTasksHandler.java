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

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

public class CreateVotingTasksHandler implements ActionHandler {

	public void execute(ExecutionContext executionContext) throws Exception {
		Token token = executionContext.getToken();
		TaskMgmtInstance tmi = executionContext.getTaskMgmtInstance();

		TaskNode taskNode = (TaskNode) executionContext.getNode();
		Task voteTask = taskNode.getTask("Vote");

		// now, 2 task instances are created for the same task.
		TaskInstance taskInstance = tmi.createTaskInstance(voteTask, token);
		taskInstance.setActorId("user1");
		taskInstance.setVariableLocally("form", VoteForm.class.getName());

		taskInstance = tmi.createTaskInstance(voteTask, token);
		taskInstance.setActorId("user2");
		taskInstance.setVariableLocally("form", VoteForm.class.getName());

		taskInstance = tmi.createTaskInstance(taskNode.getTask("End Vote"),
				token);
		taskInstance.setVariableLocally("form", EndVoteForm.class.getName());
		taskInstance.assign(executionContext); // will assign to the swimlange
	}

}

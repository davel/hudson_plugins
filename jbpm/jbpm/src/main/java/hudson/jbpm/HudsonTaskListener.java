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
package hudson.jbpm;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.ParameterValue;
import hudson.model.ParameterizedProjectTask;
import hudson.model.PeriodicWork;
import hudson.model.Run;
import hudson.model.RunParameterValue;
import hudson.model.StringParameterValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * This task is responsible for picking up new tasks for Hudson, and putting
 * them into the queue
 */
public class HudsonTaskListener extends PeriodicWork {

	private static Logger log = Logger.getLogger(HudsonTaskListener.class
			.getName());

	public HudsonTaskListener() {
		super("JBPM Task Listener");
	}

	@Override
	protected void execute() {
		JbpmContext context = JbpmConfiguration.getInstance()
				.createJbpmContext();
		try {
			List<TaskInstance> tasks = context.getTaskMgmtSession()
					.findTaskInstances("hudson");
			for (TaskInstance task : tasks) {
				if (task.getStart() == null) {
					try {
						// it is possible that this task is already scheduled,
						// but the
						// hudson queue will eliminate duplicates
						scheduleBuild(task);
					} catch (Exception e) {
						log.log(Level.WARNING, "Error while scheduling task "
								+ task.getId(), e);
					}
				}
			}
		} finally {
			context.close();
		}
	}

	private void scheduleBuild(TaskInstance task) {
		Run<?, ?> run = (Run<?, ?>) task.getContextInstance().getVariable(
				"build");

		String projectName = (String) task.getVariableLocally("projectToBuild");
		if (projectName == null) {
			return;
		}

		AbstractProject<?, ?> project = (AbstractProject<?, ?>) Hudson
				.getInstance().getItem(projectName.trim());

		List<ParameterValue> parameters = new ArrayList<ParameterValue>();
		RunParameterValue runParameter = new RunParameterValue(
				"triggeringBuild", run);
		StringParameterValue taskParameter = new StringParameterValue("task",
				Long.toString(task.getId()));
		parameters.add(runParameter);
		parameters.add(taskParameter);

		for (Map.Entry entry: ((Set<Map.Entry>) task.getVariablesLocally().entrySet())) {
			if (entry.getValue() instanceof String) {
				parameters.add(new StringParameterValue(
						(String) entry.getKey(), (String) entry.getValue()));
			}
		}

		Hudson.getInstance().getQueue().add(
				new ParameterizedProjectTask(project, parameters), 0);
	}

}

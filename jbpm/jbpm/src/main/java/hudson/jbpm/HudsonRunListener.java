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

import java.util.logging.Logger;

import hudson.jbpm.PluginImpl;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * listens for builds that are started and completed, and checks if they correspond
 * to jbpm tasks
 * 
 * @author tom
 *
 */
public class HudsonRunListener extends RunListener {

	private static Logger log = Logger.getLogger(HudsonRunListener.class.getName());
	
	public HudsonRunListener() {
		super(AbstractBuild.class);
	}

	@Override
	public synchronized void onCompleted(Run r, TaskListener listener) {
		ParametersAction parameters = r.getAction(ParametersAction.class);
		if (parameters == null) {
			return;
		}
		String task = (String) parameters.getValue("task");
		if (task == null) {
			return;
		}

		long taskInstanceId = Integer.parseInt(task);

		JbpmContext context = JbpmConfiguration.getInstance()
				.createJbpmContext();
		try {
			TaskInstance taskInstance = context
					.loadTaskInstance(taskInstanceId);
			PluginImpl.injectTransientVariables(taskInstance
					.getProcessInstance().getContextInstance());
			taskInstance.setVariableLocally("result", r.getResult().toString());

			taskInstance.end();

			context.save(taskInstance);

			return;
		} catch (Exception e) {
			e.printStackTrace(listener.error("Error in " + getClass().getName()));
		} finally {
			context.close();
		}
	}

	@Override
	public synchronized void onStarted(Run r, TaskListener listener) {
		ParametersAction parameters = r.getAction(ParametersAction.class);
		if (parameters == null) {
			return;
		}
		String task = (String) parameters.getValue("task");
		if (task == null) {
			return;
		}

		long taskInstanceId = Integer.parseInt(task);

		JbpmContext context = JbpmConfiguration.getInstance()
				.createJbpmContext();
		try {
			TaskInstance taskInstance = context.getTaskInstance(taskInstanceId);
			if (taskInstance == null) {
				System.err.println("no task instance found with id "
						+ taskInstanceId);
			}
			PluginImpl.injectTransientVariables(taskInstance
					.getContextInstance());
			taskInstance.setVariableLocally("build", r);
			taskInstance.start();
			context.save(taskInstance);
		} finally {
			context.close();
		}
	}

}

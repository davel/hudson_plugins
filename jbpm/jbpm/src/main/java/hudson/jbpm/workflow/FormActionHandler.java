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
package hudson.jbpm.workflow;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class FormActionHandler implements ActionHandler {

	private String formClass;
	
	public String getFormClass() {
		return formClass;
	}

	public void setFormClass(String formClass) {
		this.formClass = formClass;
	}

	public void execute(ExecutionContext executionContext) throws Exception {
		TaskInstance task = executionContext.getTaskInstance();
		task.setVariableLocally("form", formClass);
	}

}

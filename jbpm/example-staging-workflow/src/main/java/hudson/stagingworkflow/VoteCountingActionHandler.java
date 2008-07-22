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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;

public class VoteCountingActionHandler implements ActionHandler {

	private static final int VOTES_REQUIRED = 1;

	public void execute(ExecutionContext executionContext) throws Exception {
		JbpmContext context = JbpmConfiguration.getInstance()
				.getCurrentJbpmContext();
		ContextInstance ci = executionContext.getContextInstance();
		Map<String, String> variables = ci.getVariables();

		int positive = 0;
		int negative = 0;

		StringBuilder voteResultText = new StringBuilder();

		for (Map.Entry<String, String> entry : variables.entrySet()) {
			if (entry.getKey().startsWith("vote-")) {
				String vote = entry.getValue();
				if (vote.equals("+1")) {
					positive++;
				} else if (vote.equals("-1")) {
					negative++;
				}

				String comment = variables.get("comment-"
						+ entry.getKey().substring(5));
				String user = entry.getKey().substring(5);
				voteResultText.append("User " + user + " voted " + vote);
				if (!StringUtils.isEmpty(comment)) {
					voteResultText.append(" with comment: ").append(comment);
				}
				voteResultText.append("\n");
			}
		}

		voteResultText.append("\n");
		voteResultText.append(String.format(
				"There were %s +1 vote(s) and %s -1 vote(s).\n", positive,
				negative));
		voteResultText.append("\n");

		String voteResult = null;
		if (positive >= VOTES_REQUIRED && positive > negative) {
			voteResultText
					.append("The vote outcome was positive and the artifacts will be deployed to the release repository.");
			voteResult = "positive";
		} else if (positive <= negative) {
			voteResultText
					.append("The vote outcome was negative because there were at least as much -1 as +1 votes.");
			voteResult = "negative";
		} else /*if (positive < VOTES_REQUIRED)*/ {
			voteResultText
					.append("The vote failed because there were less than "
							+ VOTES_REQUIRED + " positive votes.");
			voteResult = "failed";
		}
		ci.setVariable("voteResultText", voteResultText.toString());
		ci.setVariable("voteResult", voteResult);
		context.save(ci.getProcessInstance());
	}
}

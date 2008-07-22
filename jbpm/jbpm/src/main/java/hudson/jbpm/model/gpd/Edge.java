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
package hudson.jbpm.model.gpd;

import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("edge")
public class Edge {
	Label label;
	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	public void setBendpoints(List<BendPoint> bendpoints) {
		this.bendpoints = bendpoints;
	}

	@XStreamImplicit(itemFieldName="bendpoint") public List<BendPoint> bendpoints;
	
	public List<BendPoint> getBendPoints() {
		if (bendpoints == null) {
			return Collections.EMPTY_LIST;
		} else {
			return bendpoints;
		}
	}
}



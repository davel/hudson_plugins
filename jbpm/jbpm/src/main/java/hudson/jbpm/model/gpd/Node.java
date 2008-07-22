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

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("node")
public class Node {
	public void setName(String name) {
		this.name = name;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	@XStreamAsAttribute String name;
	@XStreamAsAttribute int x, y, width, height;
	@XStreamImplicit(itemFieldName="edge") public List<Edge> edges;
	
	private transient NodeState state;
	
	public NodeState getState() {
		return state;
	}
	public void setState(NodeState state) {
		this.state = state;
	}
	public String getName() {
		return name;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public List<Edge> getEdges() {
		return edges;
	}
	
	public Rectangle2D.Double asRectangle() {
		return new Rectangle2D.Double(x, y, width, height);
	}
	public Edge getEdge(int i) {
		return edges != null ? edges.get(i) : null;
	}
}

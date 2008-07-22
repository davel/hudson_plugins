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
package hudson.jbpm.rendering;

import hudson.jbpm.model.gpd.BendPoint;
import hudson.jbpm.model.gpd.Edge;
import hudson.jbpm.model.gpd.GPD;
import hudson.jbpm.model.gpd.NodeState;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.dom4j.DocumentException;
import org.jbpm.JbpmConfiguration;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.log.TokenCreateLog;
import org.jbpm.graph.log.TransitionLog;
import org.jbpm.logging.log.ProcessLog;
import org.jbpm.taskmgmt.log.TaskCreateLog;

public final class ProcessInstanceRenderer extends JComponent {

	private static Color LINE_COLOR = new Color(180, 180, 180);
	private static final Color TEXT_COLOR = Color.BLACK;
	private static final Font FONT = new Font("Tahoma", Font.BOLD, 12);
	private static final Color NODE_COLOR_1 = new Color(220, 220, 235);
	private static final Color NODE_COLOR_2 = Color.WHITE;

	private final ProcessDefinition def;
	// private final ProcessLayout layout;
	private final GPD gpd;

	public ProcessInstanceRenderer(ProcessInstance processInstance, GPD gpd)
			throws DocumentException {
		def = processInstance.getProcessDefinition();
		this.gpd = gpd;

		Map<Token, List<ProcessLog>> logs = JbpmConfiguration.getInstance()
				.getCurrentJbpmContext().getLoggingSession()
				.findLogsByProcessInstance(processInstance.getId());
		handleLog(logs, processInstance.getRootToken());

		setBackground(Color.WHITE);
		setOpaque(true);
		setSize(gpd.getWidth(), gpd.getHeight());

	}

	private void handleLog(Map<Token, List<ProcessLog>> logs, Token token) {
		List<ProcessLog> list = logs.get(token);
		for (ProcessLog log : list) {
			// System.out.println(log);
			if (log instanceof TransitionLog) {
				String source = ((TransitionLog) log).getSourceNode().getName();
				String target = ((TransitionLog) log).getDestinationNode()
						.getName();
				gpd.getNode(source).setState(NodeState.Completed);
				gpd.getNode(target).setState(NodeState.Entered);
			} else if (log instanceof TokenCreateLog) {
				Token subToken = ((TokenCreateLog) log).getChild();
				handleLog(logs, subToken);
			} else if (log instanceof TaskCreateLog) {
				gpd.getNode(
						((TaskCreateLog) log).getTaskInstance().getTask()
								.getTaskNode().getName()).setState(
						NodeState.TaskCreated);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, getWidth(), getHeight());

		Map<String, Node> tasks = def.getNodesMap();
		for (Map.Entry<String, Node> entry : tasks.entrySet()) {
			List<Transition> transitions = entry.getValue()
					.getLeavingTransitions();
			for (int i = 0; transitions != null && i < transitions.size(); i++) {
				Transition transition = transitions.get(i);
				String from = transition.getFrom().getName();
				String to = transition.getTo().getName();
				Edge edge = gpd.getNode(from).getEdge(i);

				paintLine(g2, gpd.getNode(from).asRectangle(), gpd.getNode(to)
						.asRectangle(), edge, transition.getName());
			}
		}

		for (Map.Entry<String, Node> entry : tasks.entrySet()) {
			Node task = tasks.get(entry.getKey());
			Rectangle2D.Double rect = gpd.getNode(task.getName()).asRectangle();
			paintTask(g2, task, rect);
		}

	}

	public static void paintLine(Graphics2D g2, Rectangle2D.Double from,
			Rectangle2D.Double to, Edge edge, String label) {
		List<BendPoint> bendPoints = edge.getBendPoints();

		Point2D.Double fromRectCenter = new Point2D.Double(from.getCenterX(),
				from.getCenterY());
		Point2D.Double toRectCenter = new Point2D.Double(to.getCenterX(), to
				.getCenterY());
		Point2D.Double startPoint = fromRectCenter;
		Point2D.Double endPoint;
		for (int i = 1; i < bendPoints.size() + 2; i++) {
			endPoint = getPoint(i, fromRectCenter, bendPoints, toRectCenter);
			Line2D.Double line = new Line2D.Double(startPoint, endPoint);

			Point2D.Double intersection = new Point2D.Double();
			if (GraphicsUtil.getLineRectangleIntersection(from, line,
					intersection)) {
				line.x1 = intersection.x;
				line.y1 = intersection.y;
			}
			if (GraphicsUtil.getLineRectangleIntersection(to, line,
					intersection)) {
				line.x2 = intersection.x;
				line.y2 = intersection.y;
			}

			drawArrow(g2, line, 1, endPoint == toRectCenter);

			startPoint = endPoint;
		}

		if (label != null) {
			Point2D.Double labelPoint = new Point2D.Double();
			int count = bendPoints.size() + 2;
			if (count % 2 == 0) {
				Point2D.Double a = getPoint(count / 2 - 1, fromRectCenter,
						bendPoints, toRectCenter);
				Point2D.Double b = getPoint(count / 2, fromRectCenter,
						bendPoints, toRectCenter);
				labelPoint.x = (a.x + b.x) / 2 + edge.getLabel().getX();
				labelPoint.y = (a.y + b.y) / 2 + edge.getLabel().getY();
			} else {
				Point2D.Double a = getPoint(count / 2, fromRectCenter,
						bendPoints, toRectCenter);
				labelPoint.x = a.x  + edge.getLabel().getX();
				labelPoint.y = a.y  + edge.getLabel().getY();
			}
			g2.setColor(Color.BLACK);
			int textHeight = g2.getFontMetrics().getAscent();
			g2.drawString(label, (int) labelPoint.x, (int) labelPoint.y + textHeight);
		}
	}

	private static Point2D.Double getPoint(int i, Point2D.Double start,
			List<BendPoint> bendPoints, Point2D.Double end) {
		if (i == 0) {
			return start;
		}
		if (i == bendPoints.size() + 1) {
			return end;
		}
		BendPoint bp = bendPoints.get(i - 1);
		return new Point2D.Double(start.x + bp.getW1(), start.y + bp.getH1());
	}

	public void paintTask(Graphics2D g2, Node node, Rectangle2D.Double rect) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Color nodeColor = getNodeColor(node);
		g2.setPaint(new GradientPaint(new Point2D.Double(rect.x, rect.y),
				nodeColor, new Point2D.Double(rect.x, rect.y + rect.height),
				NODE_COLOR_2));

		g2.fill(rect);
		g2.setPaint(LINE_COLOR);
		g2.draw(rect);

		double imageY = rect.y + rect.height / 2 - 16 / 2;

		Image image = GraphicsUtil.getImage(node);
		if (image != null) {
			int w = image.getWidth(null);
			int h = image.getHeight(null);
			g2.drawImage(image, (int) rect.x + 8, (int) imageY, (int) rect.x
					+ 8 + w, (int) imageY + h, 0, 0, w, h, this);
		}
		int textWidth = g2.getFontMetrics().stringWidth(node.getName());
		int textHeight = g2.getFontMetrics().getAscent();

		g2.setColor(TEXT_COLOR);
		g2.setFont(FONT);

		g2.drawString(node.getName(),
				(int) (rect.x + 12 + (rect.width - textWidth) / 2),
				(int) (rect.y + (rect.height + textHeight) / 2));

	}

	public static void drawArrow(Graphics2D g2d, Line2D.Double line,
			float stroke, boolean arrow) {
		int xCenter = (int) line.getX1();
		int yCenter = (int) line.getY1();
		double x = line.getX2();
		double y = line.getY2();
		double aDir = Math.atan2(xCenter - x, yCenter - y);
		int i1 = 12 + (int) (stroke * 2);
		int i2 = 6 + (int) stroke; // make the arrow head the same size

		Line2D.Double base = new Line2D.Double(x + xCor(i1, aDir + .5), y
				+ yCor(i1, aDir + .5), x + xCor(i1, aDir - .5), y
				+ yCor(i1, aDir - .5));
		Point2D.Double intersect = new Point2D.Double();
		GraphicsUtil.getLineLineIntersection(line, base, intersect);

		g2d.setPaint(LINE_COLOR);
		if (arrow) {
			g2d.draw(new Line2D.Double(xCenter, yCenter, intersect.x,
					intersect.y));

			g2d.setStroke(new BasicStroke(1f)); // make the arrow head solid
			// even if
			// dash pattern has been specified
			Polygon tmpPoly = new Polygon();
			// regardless of the length
			tmpPoly.addPoint((int) x, (int) y); // arrow tip
			tmpPoly.addPoint((int) x + xCor(i1, aDir + .5), (int) y
					+ yCor(i1, aDir + .5));
			// tmpPoly.addPoint(x + xCor(i2, aDir), y + yCor(i2, aDir));
			tmpPoly.addPoint((int) x + xCor(i1, aDir - .5), (int) y
					+ yCor(i1, aDir - .5));
			tmpPoly.addPoint((int) x, (int) y); // arrow tip
			g2d.drawPolygon(tmpPoly);
		} else {
			g2d.draw(new Line2D.Double(xCenter, yCenter, x, y));
		}
//		g2d.setPaint(Color.WHITE);
	}

	private static int yCor(int len, double dir) {
		return (int) (len * Math.cos(dir));
	}

	private static int xCor(int len, double dir) {
		return (int) (len * Math.sin(dir));
	}

	private Color getNodeColor(Node node) {
		NodeState state = gpd.getNode(node.getName()).getState();
		if (state == NodeState.Entered) {
			return new Color(150, 150, 230);
		} else if (state == NodeState.Started) {
			return new Color(150, 255, 150);
		} else if (state == NodeState.Completed) {
			return new Color(50, 235, 50);
		} else if (state == NodeState.TaskCreated) {
			return new Color(235, 150, 150);
		} else {
			return NODE_COLOR_1;
		}
	}
}
package com.Lyeeedar.GrammarEditor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.Lyeeedar.GrammarEditor.EditFrame.CoordinateSystemEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.DefineEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.DivideEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.MeshEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.MoveEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.MultiConditionalEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.OccludeEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.RepeatEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.ResizeEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.RotateEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.SelectEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.SnapEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.SplitEditFrame;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphConnector;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphExpression;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphNode;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphChildCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphCoordinateSystemCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDeferCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDefineCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDivideCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMeshCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMoveCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMultiConditionalCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphOccludeCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRepeatCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphResizeCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRotateCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRuleCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSelectCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSnapCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSplitCompiler;
import com.Lyeeedar.Pirates.ProceduralGeneration.VolumePartitioner;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

public class GraphParser
{
	JsonValue root;
	public GraphParser(JsonValue root)
	{
		this.root = root;
	}
	
	private boolean isStart(LinkedList<GraphNode> nodes, GraphNode n)
	{
		for (GraphNode node : nodes)
		{
			if (node == n) continue;
			
			int val = node.shouldCollapse(n);
			if (val == 0) return false;
			if (val == 1) return false;
		}
		
		return true;
	}
	
	public void parse(EdittableGraph graph)
	{
		JsonValue current = root.child;
		HashMap<String, Object[]> pairs = new HashMap<String, Object[]>();
		while (current != null)
		{
			if (current.name.equalsIgnoreCase("Imports"))
			{
				String[] imports = current.asStringArray();
				for (String i : imports)
				{
					graph.imports.add(i);
				}
			}
			else
			{
				String name = current.name;
				GraphNode node = graph.new GraphNode(name, null);
				
				pairs.put(name, new Object[]{node, current});
			}
			
			current = current.next;
		}
		
		for (Map.Entry<String, Object[]> entry : pairs.entrySet())
		{
			parseNode(graph, (JsonValue) entry.getValue()[1], (GraphNode) entry.getValue()[0], pairs);
		}
		
		GraphNode emptyNode = pairs.containsKey("empty") ? (GraphNode) pairs.remove("empty")[0] : null;
		
		for (Map.Entry<String, Object[]> entry : pairs.entrySet())
		{
			GraphNode node = (GraphNode) entry.getValue()[0];
			if (node.name.startsWith("AutoAssignedName"))
			{
				node.name = "";
			}
			node.updateLinks(emptyNode, null);
			
			graph.nodes.add(node);
		}
		
//		for (GraphNode node : graph.nodes)
//		{
//			node.isStart = isStart(graph.nodes, node);
//			node.depth = 0;
//		}
//		
//		for (GraphNode node : graph.nodes)
//		{
//			if (node.isStart) 
//			{
//				node.depth = 1;
//				node.updateDepth();
//			}
//		}
//		
//		for (GraphNode node : graph.nodes)
//		{
//			node.x = 50+(node.depth-1)*150;
//			node.y = 50;
//		}
	}
	
	private void parseNode(EdittableGraph graph, JsonValue root, GraphNode node, HashMap<String, Object[]> pairs)
	{
		JsonValue current = root.child;
		while (current != null)
		{
			parseExp(graph, current, node, pairs);
			current = current.next;
		}
	}
	
	private void parseExp(EdittableGraph graph, JsonValue current, GraphNode node, HashMap<String, Object[]> pairs)
	{
		String method = current.name;
		
		if (method.equalsIgnoreCase("Rule"))
		{
			GraphExpression exp = graph.new GraphExpression("Rule", node, null, GraphRuleCompiler.class, new String[][]{}, new String[]{"Rule"});
			exp.connectors.get(0).addLink((GraphNode) pairs.get(current.asString())[0]);
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Child"))
		{
			GraphExpression exp = graph.new GraphExpression("Child", node, null, GraphChildCompiler.class, new String[][]{}, new String[]{"Rule"});
			exp.connectors.get(0).addLink((GraphNode) pairs.get(current.asString())[0]);
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("CoordinateSystem"))
		{
			GraphExpression exp = graph.new GraphExpression("CoordinateSystem", node, CoordinateSystemEditFrame.class, GraphCoordinateSystemCompiler.class, new String[][]{}, new String[]{});
			exp.data.put("Coords", current.asString());
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Move"))
		{
			String xstring = current.getString("X", "0");
			String ystring = current.getString("Y", "0");
			String zstring = current.getString("Z", "0");
			
			GraphExpression exp = graph.new GraphExpression("Move", node, MoveEditFrame.class, GraphMoveCompiler.class, new String[][]{}, new String[]{});
			
			if (!xstring.equals("0")) exp.data.put("X", xstring);
			if (!ystring.equals("0")) exp.data.put("Y", ystring);
			if (!zstring.equals("0")) exp.data.put("Z", zstring);
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Rotate"))
		{
			String xstring = current.getString("X", "0");
			String ystring = current.getString("Y", "1");
			String zstring = current.getString("Z", "0");
			String astring = current.getString("Angle", "0");
			
			GraphExpression exp = graph.new GraphExpression("Rotate", node, RotateEditFrame.class, GraphRotateCompiler.class, new String[][]{}, new String[]{});
			
			if (!xstring.equals("0")) exp.data.put("X", xstring);
			if (!ystring.equals("1")) exp.data.put("Y", ystring);
			if (!zstring.equals("0")) exp.data.put("Z", zstring);
			if (!astring.equals("0")) exp.data.put("Angle", astring);
			
			node.insert(exp);
			
		}
		else if (method.equalsIgnoreCase("MultiConditional"))
		{
			GraphExpression exp = graph.new GraphExpression("MultiConditional", node, MultiConditionalEditFrame.class, GraphMultiConditionalCompiler.class, new String[][]{}, new String[]{});
			
			JsonValue c = current.child;
			while (c != null)
			{
				GraphConnector con = graph.new GraphConnector(c.name, exp);
				con.addLink((GraphNode) pairs.get(c.asString())[0]);
				exp.connectors.add(con);
				
				c = c.next;
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Split"))
		{
			GraphExpression exp = graph.new GraphExpression("Split", node, SplitEditFrame.class, GraphSplitCompiler.class, new String[][]{}, new String[]{});
			
			String[] vals = current.asStringArray();
			
			for (String val : vals)
			{
				String[] csv = VolumePartitioner.parseCSV(val);
				GraphConnector con = graph.new GraphConnector(csv[0], exp);
				con.addLink((GraphNode) pairs.get(csv[1])[0]);
				exp.connectors.add(con);
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Select"))
		{
			GraphExpression exp = graph.new GraphExpression("Select", node, SelectEditFrame.class, GraphSelectCompiler.class, new String[][]{}, new String[]{});
			
			String[] vals = current.asStringArray();
			
			for (String val : vals)
			{
				String[] csv = VolumePartitioner.parseCSV(val);
				
				if (csv[0].equalsIgnoreCase("Remainder"))
				{
					GraphConnector con = graph.new GraphConnector(csv[0], exp);
					con.addLink((GraphNode) pairs.get(csv[1])[0]);
					exp.connectors.add(con);
				}
				else
				{
					GraphConnector con = graph.new GraphConnector(csv[0]+","+csv[1], exp);
					if (csv.length > 3) con.name += "," + csv[3];
					con.addLink((GraphNode) pairs.get(csv[2])[0]);
					exp.connectors.add(con);
				}
			}
			
			node.insert(exp);
		}
		else if (method.startsWith("Repeat"))
		{
			String size = current.getString("Size");
			String repeats = current.getString("Repeats", "-1");
			String offset = current.getString("Offset", "0");
			
			String rule = current.getString("Rule", "empty");
			String offsetRule = current.getString("OffsetRule", "empty");
			String remainderRule = current.getString("RemainderRule", "empty");
			String repeatRule = current.getString("RepeatRule", "empty");
			
			String ruleCoord = current.getString("RuleCoord", "xyz");
			String offsetCoord = current.getString("OffsetCoord", "xyz");
			String remainderCoord = current.getString("RemainderCoord", "xyz");
			
			GraphExpression exp = graph.new GraphExpression(method, node, RepeatEditFrame.class, GraphRepeatCompiler.class, new String[][]{}, new String[]{});
			
			exp.data.put("Size", size);
			if (!repeats.equalsIgnoreCase("-1")) exp.data.put("Repeats", repeats);
			if (!offset.equalsIgnoreCase("0")) exp.data.put("Offset", offset);
			if (!ruleCoord.equalsIgnoreCase("xyz")) exp.data.put("RuleCoord", ruleCoord);
			
			GraphConnector con = graph.new GraphConnector("Rule", exp);
			con.addLink((GraphNode) pairs.get(rule)[0]);
			exp.connectors.add(con);
			
			if (!offsetRule.equalsIgnoreCase("empty"))
			{
				con = graph.new GraphConnector("OffsetRule", exp);
				con.addLink((GraphNode) pairs.get(offsetRule)[0]);
				exp.connectors.add(con);
				
				if (!offsetCoord.equalsIgnoreCase("xyz")) exp.data.put("OffsetCoord", offsetCoord);
			}
			
			if (!remainderRule.equalsIgnoreCase("empty"))
			{
				con = graph.new GraphConnector("RemainderRule", exp);
				con.addLink((GraphNode) pairs.get(remainderRule)[0]);
				exp.connectors.add(con);
				
				if (!remainderCoord.equalsIgnoreCase("xyz")) exp.data.put("RemainderCoord", remainderCoord);
			}
			
			if (!repeatRule.equalsIgnoreCase("empty"))
			{
				con = graph.new GraphConnector("RepeatRule", exp);
				con.addLink((GraphNode) pairs.get(repeatRule)[0]);
				exp.connectors.add(con);				
			}
			
			node.insert(exp);
			
		}
		else if (method.startsWith("Divide"))
		{
			GraphExpression exp = graph.new GraphExpression(method, node, DivideEditFrame.class, GraphDivideCompiler.class, new String[][]{}, new String[]{});
			
			String[] vals = current.asStringArray();
			
			for (String val : vals)
			{
				String[] csv = VolumePartitioner.parseCSV(val);
				
				String name = csv[0];
				if (csv.length > 2) name += csv[2];
				
				GraphConnector con = graph.new GraphConnector(name, exp);
				con.addLink((GraphNode) pairs.get(csv[1])[0]);
				exp.connectors.add(con);
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Snap"))
		{
			String xstring = current.getString("X", "0");
			String ystring = current.getString("Y", "0");
			String zstring = current.getString("Z", "0");
			
			GraphExpression exp = graph.new GraphExpression("Snap", node, SnapEditFrame.class, GraphSnapCompiler.class, new String[][]{}, new String[]{});
			
			exp.data.put("X", xstring);
			exp.data.put("Y", ystring);
			exp.data.put("Z", zstring);
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Resize"))
		{
			String xstring = current.getString("X", "100%");
			String ystring = current.getString("Y", "100%");
			String zstring = current.getString("Z", "100%");
			
			GraphExpression exp = graph.new GraphExpression("Resize", node, ResizeEditFrame.class, GraphResizeCompiler.class, new String[][]{}, new String[]{});
			
			if (!xstring.equals("100%")) exp.data.put("X", xstring);
			if (!ystring.equals("100%")) exp.data.put("Y", ystring);
			if (!zstring.equals("100%")) exp.data.put("Z", zstring);
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Define"))
		{
			GraphExpression exp = graph.new GraphExpression("Define", node, DefineEditFrame.class, GraphDefineCompiler.class, new String[][]{}, new String[]{});
			
			JsonValue c = current.child;
			while (c != null)
			{
				exp.data.put(c.name, c.asString());
				
				c = c.next;
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Occlude"))
		{
			GraphExpression exp = graph.new GraphExpression("Occlude", node,OccludeEditFrame.class, GraphOccludeCompiler.class, new String[][]{}, new String[]{});
			
			JsonValue c = current.child;
			while (c != null)
			{
				exp.data.put(c.name, c.asString());
				
				c = c.next;
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Mesh"))
		{
			GraphExpression exp = graph.new GraphExpression("Mesh", node, MeshEditFrame.class, GraphMeshCompiler.class, new String[][]{}, new String[]{});
			
			JsonValue c = current.child;
			while (c != null)
			{
				exp.data.put(c.name, c.asString());
				
				c = c.next;
			}
			
			exp.data.put("Type", "File");
			if (exp.data.get("Name").equalsIgnoreCase("Box"))
			{
				exp.data.put("Type", "Box");
				exp.data.remove("Name");
			}
			else if (exp.data.get("Name").equalsIgnoreCase("Cylinder"))
			{
				exp.data.put("Type", "Cylinder");
				exp.data.remove("Name");
			}
			
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("Defer"))
		{
			GraphExpression exp = graph.new GraphExpression("Defer", node, null, GraphDeferCompiler.class, new String[][]{}, new String[]{});
			node.insert(exp);
		}
		else if (method.equalsIgnoreCase("GraphData"))
		{
			node.x = current.getInt("X");
			node.y = current.getInt("Y");
		}
		else throw new RuntimeException("Cannot read rule type: "+method+"!");
	}
}

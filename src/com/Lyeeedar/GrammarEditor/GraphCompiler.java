package com.Lyeeedar.GrammarEditor;

import java.util.Map;

import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphConnector;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphExpression;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphMethod;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphNode;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphObject;
import com.Lyeeedar.Pirates.ProceduralGeneration.VolumePartitioner;

public abstract class GraphCompiler
{
	
	public abstract String compile();
	
	public static class GraphNodeCompiler extends GraphCompiler
	{
		GraphNode object;
		
		public GraphNodeCompiler(GraphObject object)
		{
			this.object = (GraphNode) object;
		}

		@Override
		public String compile()
		{
			String code = "";
			
			code += object.assignedName + " : {";
			
			code += "GraphData:{X:\""+object.x+"\",Y:\""+object.y+"\"},";
			
			for (GraphObject o : object.objects)
			{
				code += o.compile();
			}
			
			code += "},";
			
			return code;
		}
		
	}
	
	public static class GraphMethodCompiler extends GraphCompiler
	{
		GraphMethod object;
		
		public GraphMethodCompiler(GraphObject object)
		{
			this.object = (GraphMethod) object;
		}

		@Override
		public String compile()
		{
			String code = "";
			
			code += "RuleCall_" + object.name + " : {";
			
			code += "GraphData:{X:\""+object.x+"\",Y:\""+object.y+"\"},";
			
			code += "},";
			
			return code;
		}
		
	}
	
	public static class GraphChildCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphChildCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Child : " + object.connectors.get(0).getLinkedName() + ",";
			return code;
		}
	}
	
	public static class GraphRuleCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphRuleCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Rule : " + object.connectors.get(0).getLinkedName() + ",";
			return code;
		}
	}
	
	public static class GraphCoordinateSystemCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphCoordinateSystemCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "CoordinateSystem : " + object.data.get("Coords") + ",";
			return code;
		}
	}
	
	public static class GraphDeferCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphDeferCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Defer : {},";
			return code;
		}
	}
	
	public static class GraphDefineCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphDefineCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Define : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			return code;
		}
	}
	
	public static class GraphDivideCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphDivideCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = object.name + " : [";
			
			for (GraphConnector connector : object.connectors)
			{
				String[] split = VolumePartitioner.parseCSV(connector.name);
				code += "\"" + split[0] + "," + connector.getLinkedName();
				if (split.length > 1)
				{
					code += "," + split[1];
				}
				code += "\","; 
			}
			
			code += "],";
			return code;
		}
	}
	
	public static class GraphMeshCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphMeshCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Mesh : {";
			
			if (object.data.get("Type").equalsIgnoreCase("File"))
			{
			}
			else
			{
				code += "Name : " + object.data.get("Type") + ",";
			}
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				if (entry.getKey().equalsIgnoreCase("Type")) continue;
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			return code;
		}
	}
	
	public static class GraphMoveCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphMoveCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Move : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphMultiConditionalCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphMultiConditionalCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = object.name + " : {";
			
			for (GraphConnector connector : object.connectors)
			{
				code += "\"" + connector.name + "\" : " + connector.getLinkedName() + ",";
			}
			
			code += "},";
			return code;
		}
	}
	
	public static class GraphOccludeCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphOccludeCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Occlude : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphRepeatCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphRepeatCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = object.name + " : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			for (GraphConnector connector : object.connectors)
			{
				code += connector.name + " : " + connector.getLinkedName() + ",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphResizeCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphResizeCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Resize : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphRotateCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphRotateCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Rotate : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphSelectCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphSelectCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Select : [";
			
			for (GraphConnector connector : object.connectors)
			{
				String[] vals = VolumePartitioner.parseCSV(connector.name);
				String first = vals[0];
				if (vals.length > 1) first += "," + vals[1];
				code += "\"" + first + "," + connector.getLinkedName();
				if (vals.length > 2) code += "," + vals[2];
				code += "\",";
			}
			
			code += "],";
			return code;
		}
	}
	
	public static class GraphSnapCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphSnapCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Snap : {";
			
			for (Map.Entry<String, String> entry : object.data.entrySet())
			{
				code += entry.getKey() + " : \"" +entry.getValue() + "\",";
			}
			
			code += "},";
			
			return code;
		}
	}
	
	public static class GraphSplitCompiler extends GraphCompiler
	{
		GraphExpression object;
		
		public GraphSplitCompiler(GraphObject object)
		{
			this.object = (GraphExpression) object;
		}

		@Override
		public String compile()
		{
			String code = "Split : [";
			
			for (GraphConnector connector : object.connectors)
			{
				code += "\"" + connector.name + "," + connector.getLinkedName() + "\","; 
			}
			
			code += "],";
			return code;
		}
	}
}

package com.Lyeeedar.GrammarEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.Lyeeedar.GrammarEditor.EditFrame.CoordinateSystemEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.DefineEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.DivideEditFrame;
import com.Lyeeedar.GrammarEditor.EditFrame.ImportEditFrame;
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
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphChildCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphCoordinateSystemCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDeferCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDefineCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphDivideCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMeshCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMoveCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphMultiConditionalCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphNodeCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphOccludeCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRepeatCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphResizeCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRotateCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphRuleCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSelectCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSnapCompiler;
import com.Lyeeedar.GrammarEditor.GraphCompiler.GraphSplitCompiler;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class EdittableGraph extends JPanel implements MouseListener, MouseMotionListener
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		frame.setSize(600, 400);
		frame.setVisible(true);

		frame.add(new EdittableGraph());
		frame.validate();
	}
	
	GraphBlank empty = new GraphBlank();
	LinkedList<GraphNode> nodes = new LinkedList<GraphNode>();
	LinkedList<String> imports = new LinkedList<String>();
	ArrayList<Object[]> menuItems;
	int[] out = new int[2];
	private GraphObject selected = null;
	private int lx = 0;
	private int ly = 0;

	public EdittableGraph()
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		createMenuItems();
		revalidate();
		repaint();
	}
	
	public void load(String graph)
	{
		imports.clear();
		nodes.clear();
		JsonValue root = new JsonReader().parse(graph);
		
		GraphParser parser = new GraphParser(root);
		parser.parse(this);
	}

	public String compile()
	{
		HashSet<String> names = new HashSet<String>();
		names.add("empty");
		for (GraphNode node : nodes)
		{
			if (node instanceof GraphMethod)
			{
				
			}
			else
			{
				String name = node.name;
				if (name.equalsIgnoreCase("")) name = "AutoAssignedName";
				
				if (names.contains(name))
				{
					int i = 0;
					while (names.contains(name+i)) i++;
					name += i;
				}
				
				node.assignedName = name;
				names.add(name);
			}
		}
		
		String code = "{";
		
		if (imports.size() > 0)
		{
			code += "Imports:[";
			for (String imp : imports)
			{
				code += imp + ",";
			}
			code += "],";
		}
		
		for (GraphNode node : nodes)
		{
			code += node.compile();
		}
		
		code += "empty:{},}";
		
		code = new Json().prettyPrint(code, 50);
		
		return code;
	}

	public void createMenuItems()
	{
		menuItems = new ArrayList<Object[]>();
		menuItems.add(new Object[]{"New Rule", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				GraphNode node = new GraphNode("", null);
				node.x = lx;
				node.y = ly;
				nodes.add(node);
				repaint();
			}
		}
		});
		menuItems.add(new Object[]{"New Rule Call", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				GraphMethod node = new GraphMethod("RuleCall");
				node.x = lx;
				node.y = ly;
				nodes.add(node);
				repaint();
			}
		}
		});
		menuItems.add(new Object[]{"Edit Imports", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				new ImportEditFrame(imports); 
			}
		}
		});
		
		menuItems.add(new Object[]{"Compile", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.out.println(compile());
			}
		}
		});
	}

	private void markLoops()
	{
		for (GraphNode node : nodes)
		{
			node.depth = 0;
			node.inLoop = false;
		}
		
		for (GraphNode node : nodes)
		{
			for (GraphNode n : nodes)
			{
				n.depth = 0;
			}
			
			node.depth = 1;
			node.updateDepth();
			
		}
	}
	
	private void updateHidden()
	{
		markLoops();
		
		int i = 0;
		
		for (; i < nodes.size(); i++)
		{
			GraphNode node = nodes.get(i);
						
			if (!node.inLoop && shouldCollapse(node))
			{
				if (!node.hidden)
				{
					node.hidden = true;
					i = 0;
				}
			}
			else
			{
				if (node.hidden)
				{
					node.hidden = false;
					i = 0;
				}
			}
		}
	}
	
	private boolean shouldCollapse(GraphNode n)
	{
		int sval = -1;
		for (GraphNode node : nodes)
		{
			if (node == n) continue;
			
			int val = node.shouldCollapse(n);
			if (val == 0) return false;
			if (val == 1) sval = 1;
		}
		
		return sval == 1;
	}
	
	@Override
	public void paint(Graphics g) 
	{
		super.paint(g);

		for (GraphNode node : nodes)
		{
			if (!node.hidden) node.paint(g);
		}

		if (selected != null && selected instanceof GraphConnector)
		{
			selected.getAbsolutePos(out);
			g.drawLine(out[0]+40+((GraphConnector)selected).name.length()*6, out[1]+12, lx, ly);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0){}

	@Override
	public void mouseEntered(MouseEvent arg0){}

	@Override
	public void mouseExited(MouseEvent arg0){}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		lx = arg0.getX();
		ly = arg0.getY();	

		GraphObject chosen = null;
		for (GraphNode node : nodes)
		{
			GraphObject s = node.getItem(arg0.getX(), arg0.getY(), true);
			if (s != null)
			{
				chosen = s;
				break;
			}
		}

		if (chosen == null)
		{
			if (arg0.getButton() == MouseEvent.BUTTON3)
			{
				PopupMenu pm = new PopupMenu(menuItems);
				pm.show(this, lx, ly);
				chosen = empty;
			}
		}
		else if (arg0.getButton() == MouseEvent.BUTTON3)
		{
			chosen.rightClicked(this, lx, ly);
			chosen = empty;
		}
		else if (chosen instanceof GraphExpression)
		{
			GraphNode node = (GraphNode) chosen.parent;

			if (node.objects.size() > 1)
			{
				node.remove(chosen);
				node = new GraphNode("", null);
				node.x = lx;
				node.y = ly;
				node.insert(chosen);
			}

			chosen = node;
		}

		updateHidden();
		selected = chosen;
	}

	public void updateLinks(GraphNode onode, GraphNode nnode)
	{
		for (GraphNode n : nodes)
		{
			n.updateLinks(onode, nnode);
		}
	}

	public void deleteNode(GraphNode node)
	{
		nodes.remove(node);
		updateLinks(node, null);
	}

	public void deleteExpression(GraphExpression exp)
	{
		((GraphNode) exp.parent).remove(exp);
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		lx = arg0.getX();
		ly = arg0.getY();

		if (selected == null)
		{

		}
		else if (selected instanceof GraphConnector)
		{
			boolean needsBreak = true;
			for (GraphNode n : nodes)
			{
				GraphObject o = n.getItem(lx, ly, false);
				if (o != null && o instanceof GraphNode)
				{
					((GraphConnector)selected).addLink(n);
					needsBreak = false;
					break;
				}
				else if (o != null && o == selected)
				{
					needsBreak = false;
					break;
				}
			}
			if (needsBreak) ((GraphConnector)selected).addLink(null);
		}
		else if (selected instanceof GraphMethod)
		{
			
		}
		else if (selected instanceof GraphNode)
		{
			GraphNode sn = (GraphNode) selected;
			if (sn.inserted)
			{
				((GraphNode) sn.parent).mergeNode(sn);
				updateLinks(sn, (GraphNode) sn.parent);
			}
		}

		selected = null;
		this.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0)
	{		
		int dx = arg0.getX() - lx;
		int dy = arg0.getY() - ly;

		lx = arg0.getX();
		ly = arg0.getY();

		if (selected == null)
		{
			for (GraphNode node : this.nodes)
			{
				node.move(dx, dy);
			}
		}
		else if (selected instanceof GraphMethod)
		{
			((GraphMethod) selected).move(dx, dy);
		}
		else if (selected instanceof GraphNode)
		{
			GraphNode sn = (GraphNode) selected;

			GraphNode inside = null;
			for (GraphNode node : this.nodes)
			{
				if (node == sn) continue;
				else if (node.getItem(lx, ly, false) != null)
				{
					inside = node;
				}
			}

			if (sn.inserted && inside == null)
			{
				((GraphNode) sn.parent).remove(sn);
				sn.x = lx;
				sn.y = ly;
				sn.inserted = false;

				this.nodes.add(sn);
			}
			else if (!sn.inserted && inside != null)
			{
				sn.x = inside.x;
				sn.y = ly;
				inside.insert(sn);
				sn.inserted = true;

				this.nodes.remove(sn);
			}
			else if (sn.inserted)
			{
				sn.y = ly;
				((GraphNode) sn.parent).reorder();
			}
			else if (!sn.inserted) sn.move(dx, dy);
		}

		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0){}

	public abstract class GraphObject
	{
		String name;
		int depth;
		boolean collapsed = false;

		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		int[] out = new int[2];

		GraphObject parent = null;

		Class<?> compilerClass;
		
		public GraphObject(String name, GraphObject parent, Class<?> compilerClass)
		{
			this.name = name;
			this.parent = parent;
			this.compilerClass = compilerClass;
		}

		public void getAbsolutePos(int[] out)
		{
			out[0] = x;
			out[1] = y;
		}

		public void getDimensions(int[] out)
		{
			out[0] = width;
			out[1] = height;
		}

		public void setWidth(int width)
		{
			this.width = width;
		}

		public String compile()
		{
			if (compilerClass == null) return "";
			
			GraphCompiler compiler = null;
			
			try
			{
				compiler = (GraphCompiler) compilerClass.getDeclaredConstructor(GraphObject.class).newInstance(this);
			}
			catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e1)
			{
				e1.printStackTrace();
			}
			
			return compiler.compile();
		}
		
		public abstract GraphObject getItem(int x, int y, boolean pressed);
		public abstract void paint(Graphics g, int x, int y);
		public abstract void updateLinks(GraphNode onode, GraphNode nnode);
		public abstract void rightClicked(Component e, int x, int y);
		public abstract int shouldCollapse(GraphNode node);
		public abstract void updateDepth();
	}

	public class GraphNode extends GraphObject
	{
		String assignedName;
		boolean hidden = false;
		boolean inserted = false;
		boolean inLoop = false;
		boolean isStart = false;
		LinkedList<GraphObject> objects = new LinkedList<GraphObject>();
		Comparator<GraphObject> comparator = new Comparator<GraphObject>(){
			@Override
			public int compare(GraphObject arg0, GraphObject arg1)
			{
				return arg0.y-arg1.y;
			}
		};

		ArrayList<Object[]> menuItems;

		public GraphNode(String name, GraphObject parent)
		{
			super(name, parent, GraphNodeCompiler.class);
			createMenuItems();
		}

		protected void createMenuItems()
		{
			final GraphNode ref = this;

			menuItems = new ArrayList<Object[]>();

			menuItems.add(new Object[]{"Rename", new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String s = (String)JOptionPane.showInputDialog(
							null,
							"Enter Rule Name",
							"Rule Name Editor",
							JOptionPane.PLAIN_MESSAGE,
							null, null,
							ref.name);

					if (s != null) {
						ref.name = s;
					}

					repaint();
				}
			}
			});
			
			menuItems.add(new Object[]{"Delete", new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					deleteNode(ref);
					repaint();
				}
			}
			});
			
			menuItems.add(null);
			
			createMenuItem(menuItems, "Child", null, GraphChildCompiler.class, new String[][]{}, new String[]{"Rule"});
			createMenuItem(menuItems, "CoordinateSystem", CoordinateSystemEditFrame.class, GraphCoordinateSystemCompiler.class, new String[][]{{"Coords", "xyz"}}, new String[]{});
			createMenuItem(menuItems, "Defer", null, GraphDeferCompiler.class, new String[][]{}, new String[]{});
			createMenuItem(menuItems, "Define", DefineEditFrame.class, GraphDefineCompiler.class, new String[][]{}, new String[]{});
			createMenuItem(menuItems, "DivideX", DivideEditFrame.class, GraphDivideCompiler.class, new String[][]{}, new String[]{"50%", "50%"}, "Divide");
			createMenuItem(menuItems, "Mesh", MeshEditFrame.class, GraphMeshCompiler.class, new String[][]{{"Type", "File"}, {"Name", "MeshName"}, {"Texture", "TextureName"}}, new String[]{});
			createMenuItem(menuItems, "Move", MoveEditFrame.class, GraphMoveCompiler.class, new String[][]{}, new String[]{});
			createMenuItem(menuItems, "MultiConditional", MultiConditionalEditFrame.class, GraphMultiConditionalCompiler.class, new String[][]{}, new String[]{"1==1", "else"});
			createMenuItem(menuItems, "Occlude", OccludeEditFrame.class, GraphOccludeCompiler.class, new String[][]{{"Name", "OcclusionName"}}, new String[]{});
			createMenuItem(menuItems, "RepeatX", RepeatEditFrame.class, GraphRepeatCompiler.class, new String[][]{{"Size", "1"}}, new String[]{"Rule"}, "Repeat");
			createMenuItem(menuItems, "Resize", ResizeEditFrame.class, GraphResizeCompiler.class, new String[][]{}, new String[]{});
			createMenuItem(menuItems, "Rotate", RotateEditFrame.class, GraphRotateCompiler.class, new String[][]{{"Angle", "180"}}, new String[]{});
			createMenuItem(menuItems, "Rule", null, GraphRuleCompiler.class, new String[][]{}, new String[]{"Rule"});
			createMenuItem(menuItems, "Select", SelectEditFrame.class, GraphSelectCompiler.class, new String[][]{}, new String[]{"Left,1", "Remainder"});
			createMenuItem(menuItems, "Snap", SnapEditFrame.class, GraphSnapCompiler.class, new String[][]{{"X", "0"}, {"Y", "0"}, {"Z", "0"}}, new String[]{});
			createMenuItem(menuItems, "Split", SplitEditFrame.class, GraphSplitCompiler.class, new String[][]{}, new String[]{"Remainder"});
		}
		
		private void createMenuItem(List<Object[]> items, final String name, final Class<?> edit, final Class<?> compiler, final String[][] defaultData, final String[] defaultOuts)
		{
			createMenuItem(items, name, edit, compiler, defaultData, defaultOuts, null);
		}
		
		private void createMenuItem(List<Object[]> items, final String name, final Class<?> edit, final Class<?> compiler, final String[][] defaultData, final String[] defaultOuts, final String overrideName)
		{
			final GraphNode ref = this;
			final String menuName = overrideName != null ? overrideName : name;
			items.add(new Object[] { menuName, new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					insert(new GraphExpression(name, ref, edit, compiler, defaultData, defaultOuts));
					repaint();
				}
			}});
		}

		public void remove(GraphObject object)
		{
			boolean success = objects.remove(object);
			System.out.println(success);
			object.parent = null;
		}

		public void insert(GraphObject object)
		{
			object.parent = this;
			objects.add(object);
			reorder();
		}

		public void mergeNode(GraphNode node)
		{
			int i = 0;
			for (; i < objects.size(); i++)
			{
				if (objects.get(i) == node)
				{
					break;
				}
			}

			for (GraphObject obj : node.objects)
			{
				objects.add(i, obj);
				obj.parent = this;
				i++;
			}

			objects.remove(node);
		}

		public void reorder()
		{
			Collections.sort(objects, comparator);
		}

		public void move(int dx, int dy)
		{
			x += dx;
			y += dy;
		}

		public void paint(Graphics g) 
		{
			update();

			g.setColor(Color.LIGHT_GRAY);
			g.fillRoundRect(x, y, width, height, 25, 25);

			g.setColor(Color.BLACK);
			g.drawString(this.name, x+25, y+15);
			
			String collapse = collapsed ? ">>" : "--" ;
			g.drawString(collapse, x+5, y+15);

			int[] size = new int[2];
			int h = 20;

			if (!collapsed) for (GraphObject obj : objects)
			{
				h += 5;
				g.setColor(Color.BLACK);
				g.drawLine(x+5, y+h, x+width-10, y+h);
				h += 5;

				obj.setWidth(width);
				obj.paint(g, x, y+h);

				size[0] = 0;
				size[1] = 0;
				obj.getDimensions(size);

				h += size[1];
			}
		}

		public void paint(Graphics g, int x, int y)
		{

			this.height = 10;

			g.setColor(Color.GREEN);
			g.fillRoundRect(x+10, y, width-20, 10, 5, 5);
		}

		public void update()
		{
			width = 50+this.name.length()*6;
			height = 25;

			if (!collapsed) for (GraphObject obj : objects)
			{
				int[] size = out;
				size[0] = 0;
				size[1] = 0;
				obj.getDimensions(size);

				width = Math.max(width, size[0]);

				height += size[1];

				height += 10;
			}
		}

		@Override
		public GraphObject getItem(int x, int y, boolean pressed)
		{
			if (hidden) return null;
			
			if (inserted)
			{
				if (x < this.x || x > this.x+this.width || y < this.y || y > this.y+this.height) return null;
				return this;
			}
			else
			{
				if (!collapsed) for (GraphObject obj : objects)
				{
					GraphObject s = obj.getItem(x, y, pressed);
					if (s != null)
					{
						return s;
					}
				}

				if (pressed && x > this.x && x < this.x+5+12 && y > this.y && y < this.y+20)
				{
					collapsed = !collapsed;
					return null;
				}
				
				if (x < this.x || x > this.x+this.width || y < this.y || y > this.y+this.height) return null;

				return this;
			}
		}

		@Override
		public void updateLinks(GraphNode onode, GraphNode nnode)
		{
			for (GraphObject obj : objects)
			{
				obj.updateLinks(onode, nnode);
			}
		}

		@Override
		public void rightClicked(Component e, int x, int y)
		{
			PopupMenu pm = new PopupMenu(menuItems);
			pm.show(e, x, y);
		}

		public int shouldCollapse(GraphNode node)
		{
			int sval = -1;
			for (GraphObject obj : objects)
			{
				int val = obj.shouldCollapse(node);
				if (val == 0)
				{
					if (hidden) return 1;
					else if (collapsed) return 1;
					else return 0;
				}
				else if (val == 1)
				{
					sval = 1;
				}
			}
			return sval;
		}

		@Override
		public void updateDepth()
		{
			for (GraphObject obj : objects)
			{
				obj.depth = depth;
				obj.updateDepth();
			}
		}
	}

	public class GraphExpression extends GraphObject
	{
		private static final int CONNECTOR_OFFSET = 25;

		HashMap<String, String> data = new HashMap<String, String>();
		
		LinkedList<GraphConnector> connectors = new LinkedList<GraphConnector>();

		ArrayList<Object[]> menuItems;

		public GraphExpression(String name, GraphObject parent, Class<?> editClass, Class<?> compilerClass, String[][] defaultData, String[] defaultOut)
		{
			super(name, parent, compilerClass);
			createMenuItems(editClass);

			for (String[] data : defaultData)
			{
				this.data.put(data[0], data[1]);
			}
			
			for (String out : defaultOut)
			{
				connectors.add(new GraphConnector(out, this));
			}
		}

		private void createMenuItems(final Class<?> editClass)
		{
			final GraphExpression ref = this;

			menuItems = new ArrayList<Object[]>();
			if (editClass != null) menuItems.add(new Object[]{"Edit", new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						editClass.getDeclaredConstructor(GraphExpression.class).newInstance(ref);
					}
					catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException
							| SecurityException e1)
					{
						e1.printStackTrace();
					}
				}}});
			menuItems.add(new Object[]{"Delete", new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					deleteExpression(ref);
					repaint();
				}
			}
			});

		}

		public void updateGraphics()
		{
			repaint();
		}
		
		private int longest = 0;
		public void paint(Graphics g, int x, int y) 
		{
			this.x = x;
			this.y = y;

			g.setColor(Color.BLACK);
			g.drawString(this.name, x+25, y+15);
			
			String collapse = collapsed ? ">>" : "--" ;
			g.drawString(collapse, x+5, y+15);

			int h = 20;
			
			if (!collapsed) for (Map.Entry<String, String> entry : data.entrySet())
			{
				String text = entry.getKey() + " = " + entry.getValue();
				g.drawString(text, x+25, y+h+15);
				h += 20;
			}

			int xoff = Math.max(CONNECTOR_OFFSET, width-(longest)/2);

			if (!collapsed) for (GraphConnector connector : connectors)
			{
				connector.paint(g, x+xoff, y+h);
				h += 22;
			}
		}

		@Override
		public GraphObject getItem(int x, int y, boolean pressed)
		{
			if (!collapsed) for (GraphObject select : connectors)
			{
				GraphObject s = select.getItem(x, y, pressed);
				if (s != null)
				{
					return s;
				}
			}
			
			if (pressed && x > this.x && x < this.x+5+12 && y > this.y && y < this.y+20)
			{
				collapsed = !collapsed;
				return null;
			}

			if (x < this.x || x > this.x+this.width || y < this.y || y > this.y+this.height) return null;

			return this;
		}

		@Override
		public void getDimensions(int[] out)
		{
			out[0] = 50+this.name.length()*6;
			out[1] = 25;
			
			if (!collapsed) for (Map.Entry<String, String> entry : data.entrySet())
			{
				String text = entry.getKey() + " = " + entry.getValue();
				int val = 50+text.length()*6;
				if (val > out[0]) out[0] = val;
				out[1] += 20;
			}

			longest = Integer.MAX_VALUE;
			if (!collapsed) for (GraphConnector connector : connectors)
			{
				out[1] += 22;

				longest = Math.min(longest, 50+connector.name.length()*6);
			}
			height = out[1];
		}

		public void removeConnection(GraphConnector c)
		{
			connectors.remove(c);
		}
		
		public void removeConnection(String name)
		{
			Iterator<GraphConnector> itr = connectors.iterator();
			while (itr.hasNext())
			{
				GraphConnector c = itr.next();
				
				if (c.name.equalsIgnoreCase(name))
				{
					itr.remove();
				}
			}
		}
		
		public void addConnectionStart(String name)
		{
			connectors.addFirst(new GraphConnector(name, this));
		}
		
		public void addConnectionBeforeEnd(String name)
		{
			connectors.add(connectors.size()-1, new GraphConnector(name, this));
		}
		
		public void addConnection(String name)
		{
			connectors.add(new GraphConnector(name, this));
		}
		
		public boolean hasConnector(String name)
		{
			for (GraphConnector c : connectors)
			{
				if (c.name.equalsIgnoreCase(name)) return true;
			}
			return false;
		}

		@Override
		public void updateLinks(GraphNode onode, GraphNode nnode)
		{
			for (GraphObject select : connectors)
			{
				select.updateLinks(onode, nnode);
			}
		}

		@Override
		public void rightClicked(Component e, int x, int y)
		{
			PopupMenu pm = new PopupMenu(menuItems);
			pm.show(e, x, y);
		}
	
		public int shouldCollapse(GraphNode node)
		{
			int sval = -1;
			for (GraphConnector connector : connectors)
			{
				int val = connector.shouldCollapse(node);
				if (val == 0)
				{
					if (collapsed) return 1;
					else return 0;
				}
				else if (val == 1)
				{
					sval = 1;
				}
			}
			return sval;
		}

		@Override
		public void updateDepth()
		{
			for (GraphConnector connector : connectors)
			{
				connector.depth = depth;
				connector.updateDepth();
			}
		}
	}

	public class GraphConnector extends GraphObject
	{
		private GraphNode linked;

		public GraphConnector(String name, GraphObject parent)
		{
			super(name, parent, null);
		}
		
		public String getLinkedName()
		{
			if (linked != null)
			{
				return linked.assignedName;
			}
			else return "empty";
		}

		public void addLink(GraphNode node)
		{
			linked = node;
		}

		public void paint(Graphics g, int x, int y)
		{
			this.x = x;
			this.y = y;

			g.setColor(Color.GREEN);
			g.fillRoundRect(x, y, 50+name.length()*6, 20, 25, 25);

			g.setColor(Color.BLACK);
			g.drawString(name, x+25, y+15);

			if (!collapsed && linked != null)
			{
				linked.getAbsolutePos(out);

				float dx = out[0] - x;
				float dy = out[1] - y;

				float dst2 = (float) Math.sqrt(dx * dx + dy * dy);
				float offset = dst2/3.0f;

				Path2D path = new Path2D.Float();
				path.moveTo(x+40+name.length()*6, y+12);
				path.curveTo(x+40+name.length()*6+offset, y+12, out[0]-offset+10, out[1]+12, out[0]+10, out[1]+12);
				Graphics2D g2d = (Graphics2D)g;
				g2d.draw(path);
			}
		}

		@Override
		public GraphObject getItem(int x, int y, boolean pressed)
		{
			if (x < this.x || x > this.x+50+name.length()*6 || y < this.y || y > this.y+20) return null;
			return this;
		}

		@Override
		public void getDimensions(int[] out)
		{
			out[0] = 50+name.length()*6;
			out[1] = 20;
		}


		@Override
		public void updateLinks(GraphNode onode, GraphNode nnode)
		{
			if (linked == onode) linked = nnode;
		}

		@Override
		public void rightClicked(Component e, int x, int y)
		{
			linked = null;
		}
	
		public int shouldCollapse(GraphNode node)
		{
			if (linked == node)
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}

		@Override
		public void updateDepth()
		{
			if (linked != null)
			{
				if (linked.depth != 0 && linked.depth != depth+1)
				{
					linked.depth = depth+1;
					linked.inLoop = true;
				}
				else if (linked.depth != depth+1)
				{
					linked.depth = depth+1;
					linked.updateDepth();
				}
			}
		}
	}

	class GraphBlank extends GraphObject
	{

		public GraphBlank()
		{
			super(null, null, null);
		}

		@Override
		public GraphObject getItem(int x, int y, boolean pressed)
		{
			return null;
		}

		@Override
		public void paint(Graphics g, int x, int y)
		{			
		}

		@Override
		public void updateLinks(GraphNode onode, GraphNode nnode)
		{			
		}

		@Override
		public void rightClicked(Component e, int x, int y)
		{			
		}

		public int shouldCollapse(GraphNode node)
		{
			return -1;
		}

		@Override
		public void updateDepth()
		{
			// TODO Auto-generated method stub
			
		}
	}

	class GraphMethod extends GraphNode
	{

		public GraphMethod(String name)
		{
			super(name, null);
		}
		
		@Override		
		protected void createMenuItems()
		{
			final GraphNode ref = this;

			menuItems = new ArrayList<Object[]>();

			menuItems.add(new Object[]{"Rename", new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String s = (String)JOptionPane.showInputDialog(
							null,
							"Enter Rule Name",
							"Rule Name Editor",
							JOptionPane.PLAIN_MESSAGE,
							null, null,
							ref.name);

					if (s != null) {
						ref.name = s;
					}

					repaint();
				}
			}
			});
			
			menuItems.add(new Object[]{"Delete", new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					deleteNode(ref);
					repaint();
				}
			}
			});
		}
	}
	
	class PopupMenu extends JScrollPopupMenu
	{
		public PopupMenu(List<Object[]> items)
		{
			for (Object[] item : items)
			{
				if (item == null)
				{
					addSeparator();
				}
				else
				{
					JMenuItem mi = new JMenuItem((String)item[0]);
					mi.addActionListener((ActionListener)item[1]);
					add(mi);
				}
			}
		}
	}
}

package com.Lyeeedar.GrammarEditor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphConnector;
import com.Lyeeedar.GrammarEditor.EdittableGraph.GraphExpression;
import com.Lyeeedar.Pirates.ProceduralGeneration.VolumePartitioner;

public abstract class EditFrame extends JFrame
{
	GraphExpression exp;
	
	public EditFrame(GraphExpression exp)
	{
		this.exp = exp;
		
		setLocationRelativeTo(null);
		setSize(600, 400);
		setVisible(true);
	}
	
	public static class ResizeEditFrame extends EditFrame
	{

		public ResizeEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			
			gc.gridx = 0;
			gc.gridy = 0;
			add(new JLabel("X"), gc);
			
			String xtext = exp.data.containsKey("X") ? exp.data.get("X") : "100%";
			final JTextField x = new JTextField(xtext, 5);
			
			gc.gridx = 1;
			add(x, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Y"), gc);
			
			String ytext = exp.data.containsKey("Y") ? exp.data.get("Y") : "100%";
			final JTextField y = new JTextField(ytext, 5);
			
			gc.gridx = 1;
			add(y, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Z"), gc);
			
			String ztext = exp.data.containsKey("Z") ? exp.data.get("Z") : "100%";
			final JTextField z = new JTextField(ztext, 5);
			
			gc.gridx = 1;
			add(z, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String xtext = x.getText();
					String ytext = y.getText();
					String ztext = z.getText();
					
					if (!xtext.equalsIgnoreCase("100%"))
					{
						exp.data.put("X", xtext);
					}
					
					if (!ytext.equalsIgnoreCase("100%"))
					{
						exp.data.put("Y", ytext);
					}
					
					if (!ztext.equalsIgnoreCase("100%"))
					{
						exp.data.put("Z", ztext);
					}
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}
		
	public static class CoordinateSystemEditFrame extends EditFrame
	{

		public CoordinateSystemEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			
			gc.gridx = 0;
			gc.gridy = 0;
			add(new JLabel("CoordinateSystem"), gc);
			
			String test = exp.data.get("Coords");
			final JTextField t = new JTextField(test, 5);
			
			gc.gridx = 1;
			add(t, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String test = t.getText();
					
					exp.data.put("Coords", test);
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}
	
	public static class DefineEditFrame extends EditFrame
	{
		JPanel top = new JPanel();
		public DefineEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			final ArrayList<JTextField[]> pairs = new ArrayList<JTextField[]>();
			
			for (final Map.Entry<String, String> entry : exp.data.entrySet())
			{
				JPanel panel = new JPanel();
				
				JTextField name = new JTextField(entry.getKey(), 10);
				JTextField value = new JTextField(entry.getValue(), 10);
				pairs.add(new JTextField[]{name, value});
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						for (JTextField[] pair : pairs)
						{
							String name = pair[0].getText();
							String value = pair[1].getText();
							
							exp.data.put(name, value);
						}
						exp.data.remove(entry.getKey());
						create();
					}});
				
				panel.add(name);
				panel.add(value);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					for (JTextField[] pair : pairs)
					{
						String name = pair[0].getText();
						String value = pair[1].getText();
						
						exp.data.put(name, value);
					}
					
					String base = "NewVariable";
					int i = 0;
					while (exp.data.containsKey(base+i))
					{
						i++;
					}
					exp.data.put(base+i, "");
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					for (JTextField[] pair : pairs)
					{
						String name = pair[0].getText();
						String value = pair[1].getText();
						
						exp.data.put(name, value);
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}
	
	public static class DivideEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public DivideEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			top.add(new JLabel("Axis"), gc);
			
			final JComboBox<String> axis = new JComboBox<String>(new String[]{"X", "Y", "Z"});

			String axiss = "" + exp.name.charAt(exp.name.length()-1);
			
			axis.setSelectedItem(axiss);
			
			gc.gridx = 1;
			top.add(axis, gc);
			
			gc.gridy++;
			
			final ArrayList<Object[]> pairs = new ArrayList<Object[]>();
			
			for (final GraphConnector connector : exp.connectors)
			{
				JPanel panel = new JPanel();
				
				JTextField value = new JTextField(connector.name, 10);
				pairs.add(new Object[]{connector, value});
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						exp.removeConnection(connector);
						create();
					}});
				
				panel.add(new JLabel("Size"));
				panel.add(value);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.addConnection("1");
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					exp.name = "Divide" + (String) axis.getSelectedItem();
					
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String value = ((JTextField)pair[1]).getText();
						
						connector.name = value;
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}

	public static class MoveEditFrame extends EditFrame
	{

		public MoveEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			
			gc.gridx = 0;
			gc.gridy = 0;
			add(new JLabel("X"), gc);
			
			String xtext = exp.data.containsKey("X") ? exp.data.get("X") : "0";
			final JTextField x = new JTextField(xtext, 5);
			
			gc.gridx = 1;
			add(x, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Y"), gc);
			
			String ytext = exp.data.containsKey("Y") ? exp.data.get("Y") : "0";
			final JTextField y = new JTextField(ytext, 5);
			
			gc.gridx = 1;
			add(y, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Z"), gc);
			
			String ztext = exp.data.containsKey("Z") ? exp.data.get("Z") : "0";
			final JTextField z = new JTextField(ztext, 5);
			
			gc.gridx = 1;
			add(z, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String xtext = x.getText();
					String ytext = y.getText();
					String ztext = z.getText();
					
					if (!xtext.equalsIgnoreCase("0"))
					{
						exp.data.put("X", xtext);
					}
					
					if (!ytext.equalsIgnoreCase("0"))
					{
						exp.data.put("Y", ytext);
					}
					
					if (!ztext.equalsIgnoreCase("0"))
					{
						exp.data.put("Z", ztext);
					}
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}
	
	public static class SnapEditFrame extends EditFrame
	{

		public SnapEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			
			gc.gridx = 0;
			gc.gridy = 0;
			add(new JLabel("X"), gc);
			
			String xtext = exp.data.containsKey("X") ? exp.data.get("X") : "0";
			final JTextField x = new JTextField(xtext, 5);
			
			gc.gridx = 1;
			add(x, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Y"), gc);
			
			String ytext = exp.data.containsKey("Y") ? exp.data.get("Y") : "0";
			final JTextField y = new JTextField(ytext, 5);
			
			gc.gridx = 1;
			add(y, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Z"), gc);
			
			String ztext = exp.data.containsKey("Z") ? exp.data.get("Z") : "0";
			final JTextField z = new JTextField(ztext, 5);
			
			gc.gridx = 1;
			add(z, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String xtext = x.getText();
					String ytext = y.getText();
					String ztext = z.getText();
					
					exp.data.put("X", xtext);
					exp.data.put("Y", ytext);
					exp.data.put("Z", ztext);
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}
	
	public static class RotateEditFrame extends EditFrame
	{

		public RotateEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			
			gc.gridx = 0;
			gc.gridy = 0;
			add(new JLabel("X"), gc);
			
			String xtext = exp.data.containsKey("X") ? exp.data.get("X") : "0";
			final JTextField x = new JTextField(xtext, 5);
			
			gc.gridx = 1;
			add(x, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Y"), gc);
			
			String ytext = exp.data.containsKey("Y") ? exp.data.get("Y") : "1";
			final JTextField y = new JTextField(ytext, 5);
			
			gc.gridx = 1;
			add(y, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Z"), gc);
			
			String ztext = exp.data.containsKey("Z") ? exp.data.get("Z") : "0";
			final JTextField z = new JTextField(ztext, 5);
			
			gc.gridx = 1;
			add(z, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Angle"), gc);
			
			String angles = exp.data.get("Angle");
			final JTextField angle = new JTextField(angles, 5);
			
			gc.gridx = 1;
			add(angle, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String xtext = x.getText();
					String ytext = y.getText();
					String ztext = z.getText();
					String angles = angle.getText();
					
					if (!xtext.equalsIgnoreCase("0"))
					{
						exp.data.put("X", xtext);
					}
					
					if (!ytext.equalsIgnoreCase("1"))
					{
						exp.data.put("Y", ytext);
					}
					
					if (!ztext.equalsIgnoreCase("0"))
					{
						exp.data.put("Z", ztext);
					}
					
					exp.data.put("Angle", angles);
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}

	public static class MultiConditionalEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public MultiConditionalEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
						
			final ArrayList<Object[]> pairs = new ArrayList<Object[]>();
			
			for (final GraphConnector connector : exp.connectors)
			{
				JPanel panel = new JPanel();
				
				JTextField value = new JTextField(connector.name, 20);
				pairs.add(new Object[]{connector, value});
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						for (Object[] pair : pairs)
						{
							GraphConnector connector = (GraphConnector) pair[0];
							String value = ((JTextField)pair[1]).getText();
							
							connector.name = value;
						}
						
						exp.removeConnection(connector);
						create();
					}});
				
				panel.add(value);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String value = ((JTextField)pair[1]).getText();
						
						connector.name = value;
					}
					
					exp.addConnection("1==1");
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String value = ((JTextField)pair[1]).getText();
						
						connector.name = value;
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}
	
	public static class SelectEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public SelectEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
						
			final ArrayList<Object[]> pairs = new ArrayList<Object[]>();
			
			for (final GraphConnector connector : exp.connectors)
			{
				if (connector.name.equalsIgnoreCase("Remainder")) continue;
				
				JPanel panel = new JPanel();
				
				String[] splits = VolumePartitioner.parseCSV(connector.name);
				
				String side = splits[0].trim();
				String size = splits[1].trim();
				String coord = splits.length > 2 ? splits[2] : "xyz" ;
				
				JComboBox<String> choice = new JComboBox<String>(new String[]{"Left", "Right", "Top", "Bottom", "Front", "Back"});
				choice.setSelectedItem(side);
				
				JTextField value = new JTextField(size, 20);
				JTextField coords = new JTextField(coord, 7);
				
				pairs.add(new Object[]{connector, choice, value, coords});
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						exp.removeConnection(connector);
						
						for (Object[] pair : pairs)
						{
							GraphConnector connector = (GraphConnector) pair[0];
							String side = (String) ((JComboBox)pair[1]).getSelectedItem();
							String value = ((JTextField)pair[2]).getText();
							String coord = ((JTextField)pair[3]).getText();
							
							connector.name = side+","+value;
							
							if (!coord.equalsIgnoreCase("xyz"))
							{
								connector.name += ","+coord;
							}
						}
						
						create();
					}});
				
				panel.add(choice);
				panel.add(value);
				panel.add(coords);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String side = (String) ((JComboBox)pair[1]).getSelectedItem();
						String value = ((JTextField)pair[2]).getText();
						String coord = ((JTextField)pair[3]).getText();
						
						connector.name = side+","+value;
						
						if (!coord.equalsIgnoreCase("xyz"))
						{
							connector.name += ","+coord;
						}
					}
					
					exp.addConnectionBeforeEnd("Left,1");
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String side = (String) ((JComboBox)pair[1]).getSelectedItem();
						String value = ((JTextField)pair[2]).getText();
						String coord = ((JTextField)pair[3]).getText();
						
						connector.name = side+","+value;
						
						if (!coord.equalsIgnoreCase("xyz"))
						{
							connector.name += ","+coord;
						}
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}
	
	public static class SplitEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public SplitEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
						
			final ArrayList<Object[]> pairs = new ArrayList<Object[]>();
			
			for (final GraphConnector connector : exp.connectors)
			{
				JPanel panel = new JPanel();
				
				JTextField value = new JTextField(connector.name, 20);
				pairs.add(new Object[]{connector, value});
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						exp.removeConnection(connector);
						create();
					}});
				
				if (connector.name.equalsIgnoreCase("Remainder"))
				{
					value.setEditable(false);
					value.setEnabled(false);
					
					delete.setEnabled(false);
				}
				
				panel.add(value);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.addConnectionStart("OcclusionName");
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					for (Object[] pair : pairs)
					{
						GraphConnector connector = (GraphConnector) pair[0];
						String value = ((JTextField)pair[1]).getText();
						
						connector.name = value;
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}

	public static class OccludeEditFrame extends EditFrame
	{

		public OccludeEditFrame(GraphExpression exp)
		{
			super(exp);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			gc.gridx = 0;
			add(new JLabel("X"), gc);
			
			String xtext = exp.data.containsKey("X") ? exp.data.get("X") : "100%";
			final JTextField x = new JTextField(xtext, 5);
			
			gc.gridx = 1;
			add(x, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Y"), gc);
			
			String ytext = exp.data.containsKey("Y") ? exp.data.get("Y") : "100%";
			final JTextField y = new JTextField(ytext, 5);
			
			gc.gridx = 1;
			add(y, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Z"), gc);
			
			String ztext = exp.data.containsKey("Z") ? exp.data.get("Z") : "100%";
			final JTextField z = new JTextField(ztext, 5);
			
			gc.gridx = 1;
			add(z, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Offset X"), gc);
			
			String oxtext = exp.data.containsKey("OX") ? exp.data.get("OX") : "0";
			final JTextField ox = new JTextField(oxtext, 5);
			
			gc.gridx = 1;
			add(ox, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Offset Y"), gc);
			
			String oytext = exp.data.containsKey("OY") ? exp.data.get("OY") : "0";
			final JTextField oy = new JTextField(oytext, 5);
			
			gc.gridx = 1;
			add(oy, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Offset Z"), gc);
			
			String oztext = exp.data.containsKey("OZ") ? exp.data.get("OZ") : "0";
			final JTextField oz = new JTextField(oztext, 5);
			
			gc.gridx = 1;
			add(oz, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			add(new JLabel("Name"), gc);
			
			String names = exp.data.get("Name");
			final JTextField name = new JTextField(names, 5);
			
			gc.gridx = 1;
			add(name, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					String xtext = x.getText();
					String ytext = y.getText();
					String ztext = z.getText();
					
					String oxtext = ox.getText();
					String oytext = oy.getText();
					String oztext = oz.getText();
					
					String names = name.getText();
					
					if (!xtext.equalsIgnoreCase("100%"))
					{
						exp.data.put("X", xtext);
					}
					
					if (!ytext.equalsIgnoreCase("100%"))
					{
						exp.data.put("Y", ytext);
					}
					
					if (!ztext.equalsIgnoreCase("100%"))
					{
						exp.data.put("Z", ztext);
					}
					
					if (!oxtext.equalsIgnoreCase("0"))
					{
						exp.data.put("OX", oxtext);
					}
					
					if (!oytext.equalsIgnoreCase("0"))
					{
						exp.data.put("OY", oytext);
					}
					
					if (!oztext.equalsIgnoreCase("0"))
					{
						exp.data.put("OZ", oztext);
					}
					
					exp.data.put("Name", names);
					
					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			add(cancel, gc);
		}
		
	}
	
	public static class RepeatEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public RepeatEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			top.add(new JLabel("Axis"), gc);
			
			final JComboBox<String> axis = new JComboBox<String>(new String[]{"X", "Y", "Z"});
			
			String axiss = "" + exp.name.charAt(exp.name.length()-1);
			
			axis.setSelectedItem(axiss);
			
			gc.gridx = 1;
			top.add(axis, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Size"), gc);
			
			String sizes = exp.data.get("Size");
			final JTextField size = new JTextField(sizes, 5);
			
			gc.gridx = 1;
			top.add(size, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Repeats"), gc);
			
			String repeats = exp.data.containsKey("Repeats") ? exp.data.get("Repeats") : "-1";
			final JTextField repeat = new JTextField(repeats, 5);
			
			gc.gridx = 1;
			top.add(repeat, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Offset"), gc);
			
			String offsets = exp.data.containsKey("Offset") ? exp.data.get("Offset") : "0";
			final JTextField offset = new JTextField(offsets, 5);
			
			gc.gridx = 1;
			top.add(offset, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("RuleCoord"), gc);
			
			String rulecoords = exp.data.containsKey("RuleCoord") ? exp.data.get("RuleCoord") : "xyz";
			final JTextField rulecoord = new JTextField(rulecoords, 5);
			
			gc.gridx = 1;
			top.add(rulecoord, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("HasOffsetRule"), gc);
			
			final JCheckBox hasoffset = new JCheckBox();
			hasoffset.setSelected(exp.hasConnector("OffsetRule"));
			
			gc.gridx = 1;
			top.add(hasoffset, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("OffsetCoord"), gc);
			
			String offsetcoords = exp.data.containsKey("OffsetCoord") ? exp.data.get("OffsetCoord") : "xyz";
			final JTextField offsetcoord = new JTextField(offsetcoords, 5);
			
			gc.gridx = 1;
			top.add(offsetcoord, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("HasRemainderRule"), gc);
			
			final JCheckBox hasremainder = new JCheckBox();
			hasremainder.setSelected(exp.hasConnector("RemainderRule"));
			
			gc.gridx = 1;
			top.add(hasremainder, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("RemainderCoord"), gc);
			
			String remaindercoords = exp.data.containsKey("RemainderCoord") ? exp.data.get("RemainderCoord") : "xyz";
			final JTextField remaindercoord = new JTextField(remaindercoords, 5);
			
			gc.gridx = 1;
			top.add(remaindercoord, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					exp.name = "Repeat" + (String) axis.getSelectedItem();
					exp.data.put("Size", size.getText());
					
					if (!repeat.getText().equalsIgnoreCase("-1"))
					{
						exp.data.put("Repeats", repeat.getText());
					}
					
					if (!offset.getText().equalsIgnoreCase("0"))
					{
						exp.data.put("Offset", offset.getText());
					}
					
					if (!rulecoord.getText().equalsIgnoreCase("xyz"))
					{
						exp.data.put("RuleCoord", rulecoord.getText());
					}
					
					if (hasoffset.isSelected())
					{
						if (!exp.hasConnector("OffsetRule"))
						{
							exp.addConnection("OffsetRule");
						}
						
						if (!offsetcoord.getText().equalsIgnoreCase("xyz"))
						{
							exp.data.put("OffsetCoord", offsetcoord.getText());
						}
					}
					else
					{
						exp.removeConnection("OffsetRule");
					}
					
					if (hasremainder.isSelected())
					{
						if (!exp.hasConnector("RemainderRule"))
						{
							exp.addConnection("RemainderRule");
						}
						
						if (!remaindercoord.getText().equalsIgnoreCase("xyz"))
						{
							exp.data.put("RemainderCoord", remaindercoord.getText());
						}
					}
					else
					{
						exp.removeConnection("RemainderRule");
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}
	
	public static class MeshEditFrame extends EditFrame
	{

		JPanel top = new JPanel();
		public MeshEditFrame(GraphExpression exp)
		{
			super(exp);
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			top.add(new JLabel("Type"), gc);
			
			final JComboBox<String> type = new JComboBox<String>(new String[]{"File", "Box", "Cylinder"});					
			type.setSelectedItem(exp.data.get("Type"));
			type.addItemListener(new ItemListener(){

				@Override
				public void itemStateChanged(ItemEvent arg0)
				{
					if (arg0.getStateChange() == ItemEvent.SELECTED)
					{
						String type = (String) arg0.getItem();
						if (!type.equalsIgnoreCase(exp.data.get("Type")))
						{
							exp.data.put("Type", type);
							create();
						}
					}
				}});
			
			gc.gridx = 1;
			top.add(type, gc);
			
			gc.gridy++;
			
			if (exp.data.get("Type").equalsIgnoreCase("File"))
			{
				createFile(gc);
			}
			else if (exp.data.get("Type").equalsIgnoreCase("Box"))
			{
				createBox(gc);
			}
			else if (exp.data.get("Type").equalsIgnoreCase("Cylinder"))
			{
				createCylinder(gc);
			}
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
		public void createFile(GridBagConstraints gc)
		{
			final JFrame ref = this;
			
			gc.gridx = 0;
			top.add(new JLabel("Mesh Name"), gc);
			
			String names = exp.data.containsKey("Name") ? exp.data.get("Name") : "MeshName" ;
			final JTextField name = new JTextField(names, 15);
			
			gc.gridx = 1;
			top.add(name, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Name"), gc);
			
			String textures = exp.data.containsKey("Texture") ? exp.data.get("Texture") : "TextureName" ;
			final JTextField texture = new JTextField(textures, 15);
			
			gc.gridx = 1;
			top.add(texture, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Use Triplanar Scaling"), gc);
			
			final JCheckBox usetripscale = new JCheckBox();
			usetripscale.setSelected(exp.data.containsKey("TriplanarScale"));
			
			gc.gridx = 1;
			top.add(usetripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Triplanar Scale"), gc);
			
			String tripscales = exp.data.containsKey("TriplanarScale") ? exp.data.get("TriplanarScale") : "1" ;
			final JTextField tripscale = new JTextField(tripscales, 15);
			
			gc.gridx = 1;
			top.add(tripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Is Seamless"), gc);
			
			final JCheckBox seamless = new JCheckBox();
			seamless.setSelected(!exp.data.containsKey("Seamless"));
			
			gc.gridx = 1;
			top.add(seamless, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					exp.data.put("Type", "File");
					exp.data.put("Name", name.getText());
					exp.data.put("Texture", texture.getText());
					
					if (usetripscale.isSelected())
					{
						exp.data.put("TriplanarScale", tripscale.getText());
					}
					
					if (!seamless.isSelected())
					{
						exp.data.put("Seamless", "false");
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
		}
		
		public void createBox(GridBagConstraints gc)
		{
			final JFrame ref = this;
			
			gc.gridx = 0;
			top.add(new JLabel("Loft X"), gc);
			
			String loftxs = exp.data.containsKey("loftX") ? exp.data.get("loftX") : "100%" ;
			final JTextField loftx = new JTextField(loftxs, 15);
			
			gc.gridx = 1;
			top.add(loftx, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Loft Z"), gc);
			
			String loftzs = exp.data.containsKey("loftZ") ? exp.data.get("loftZ") : "100%" ;
			final JTextField loftz = new JTextField(loftzs, 15);
			
			gc.gridx = 1;
			top.add(loftz, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Snap X"), gc);
			
			String snapxs = exp.data.containsKey("snapX") ? exp.data.get("snapX") : "0" ;
			final JTextField snapx = new JTextField(snapxs, 15);
			
			gc.gridx = 1;
			top.add(snapx, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Snap Z"), gc);
			
			String snapzs = exp.data.containsKey("snapZ") ? exp.data.get("snapZ") : "0" ;
			final JTextField snapz = new JTextField(snapzs, 15);
			
			gc.gridx = 1;
			top.add(snapz, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Name"), gc);
			
			String textures = exp.data.containsKey("Texture") ? exp.data.get("Texture") : "TextureName" ;
			final JTextField texture = new JTextField(textures, 15);
			
			gc.gridx = 1;
			top.add(texture, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Use Triplanar Scaling"), gc);
			
			final JCheckBox usetripscale = new JCheckBox();
			usetripscale.setSelected(exp.data.containsKey("TriplanarScale"));
			
			gc.gridx = 1;
			top.add(usetripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Triplanar Scale"), gc);
			
			String tripscales = exp.data.containsKey("TriplanarScale") ? exp.data.get("TriplanarScale") : "1" ;
			final JTextField tripscale = new JTextField(tripscales, 15);
			
			gc.gridx = 1;
			top.add(tripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Is Seamless"), gc);
			
			final JCheckBox seamless = new JCheckBox();
			seamless.setSelected(!exp.data.containsKey("Seamless"));
			
			gc.gridx = 1;
			top.add(seamless, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					exp.data.put("Type", "Box");
					exp.data.put("Texture", texture.getText());
					
					if (usetripscale.isSelected())
					{
						exp.data.put("TriplanarScale", tripscale.getText());
					}
					
					if (!seamless.isSelected())
					{
						exp.data.put("Seamless", "false");
					}
					
					if (!loftx.getText().equalsIgnoreCase("100%"))
					{
						exp.data.put("loftX", loftx.getText());
					}
					
					if (!loftz.getText().equalsIgnoreCase("100%"))
					{
						exp.data.put("loftZ", loftz.getText());
					}
					
					if (!snapx.getText().equalsIgnoreCase("0"))
					{
						exp.data.put("snapX", snapx.getText());
					}
					
					if (!snapz.getText().equalsIgnoreCase("0"))
					{
						exp.data.put("snapZ", snapz.getText());
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
		}
		
		public void createCylinder(GridBagConstraints gc)
		{
			final JFrame ref = this;
			
			gc.gridx = 0;
			top.add(new JLabel("Phi"), gc);
			
			String phis = exp.data.containsKey("Phi") ? exp.data.get("Phi") : "8" ;
			final JTextField phi = new JTextField(phis, 15);
			
			gc.gridx = 1;
			top.add(phi, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Hollow Scale"), gc);
			
			String hollows = exp.data.containsKey("HollowScale") ? exp.data.get("HollowScale") : "0" ;
			final JTextField hollow = new JTextField(hollows, 15);
			
			gc.gridx = 1;
			top.add(hollow, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Name"), gc);
			
			String textures = exp.data.containsKey("Texture") ? exp.data.get("Texture") : "TextureName" ;
			final JTextField texture = new JTextField(textures, 15);
			
			gc.gridx = 1;
			top.add(texture, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Use Triplanar Scaling"), gc);
			
			final JCheckBox usetripscale = new JCheckBox();
			usetripscale.setSelected(exp.data.containsKey("TriplanarScale"));
			
			gc.gridx = 1;
			top.add(usetripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Triplanar Scale"), gc);
			
			String tripscales = exp.data.containsKey("TriplanarScale") ? exp.data.get("TriplanarScale") : "1" ;
			final JTextField tripscale = new JTextField(tripscales, 15);
			
			gc.gridx = 1;
			top.add(tripscale, gc);
			
			gc.gridy++;
			
			gc.gridx = 0;
			top.add(new JLabel("Texture Is Seamless"), gc);
			
			final JCheckBox seamless = new JCheckBox();
			seamless.setSelected(!exp.data.containsKey("Seamless"));
			
			gc.gridx = 1;
			top.add(seamless, gc);
			
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					exp.data.clear();
					
					exp.data.put("Type", "Cylinder");
					exp.data.put("Texture", texture.getText());
					
					if (usetripscale.isSelected())
					{
						exp.data.put("TriplanarScale", tripscale.getText());
					}
					
					if (!seamless.isSelected())
					{
						exp.data.put("Seamless", "false");
					}
					
					if (!phi.getText().equalsIgnoreCase("8"))
					{
						exp.data.put("Phi", phi.getText());
					}
					
					if (!hollow.getText().equalsIgnoreCase("0"))
					{
						exp.data.put("HollowScale", hollow.getText());
					}

					ref.dispose();
					exp.updateGraphics();
					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
		}
	}

	public static class ImportEditFrame extends JFrame
	{
		LinkedList<String> imports;
		JPanel top = new JPanel();
		public ImportEditFrame(LinkedList<String> imports)
		{
			setLocationRelativeTo(null);
			setSize(600, 400);
			setVisible(true);
			
			this.imports = imports;
			add(top);
			create();
			pack();
		}
		
		public void create()
		{
			final JFrame ref = this;
			
			top.removeAll();
			top.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			
			final ArrayList<JTextField> pairs = new ArrayList<JTextField>();
			
			for (final String i : imports)
			{
				JPanel panel = new JPanel();
				
				JTextField name = new JTextField(i, 10);
				pairs.add(name);
				
				JButton delete = new JButton("Delete");
				delete.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						imports.clear();
						for (JTextField t : pairs)
						{
							imports.add(t.getText());
						}
						
						imports.remove(i);
						create();
					}});
				
				panel.add(name);
				panel.add(delete);
				
				gc.gridx = 0;
				gc.gridwidth = 2;
				
				top.add(panel, gc);
				
				gc.gridy++;
			}
			
			JButton add = new JButton("Add");
			add.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					imports.clear();
					for (JTextField t : pairs)
					{
						imports.add(t.getText());
					}
					
					imports.add("NewImport");
					
					create();
				}});
			
			gc.gridx = 0;
			gc.gridwidth = 2;
			
			top.add(add, gc);
			
			gc.gridwidth = 1;
			gc.gridy++;
			
			JButton apply = new JButton("Apply");
			apply.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e)
				{
					imports.clear();
					for (JTextField t : pairs)
					{
						imports.add(t.getText());
					}

					ref.dispose();					
				}});
			
			gc.gridx = 0;
			top.add(apply, gc);
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					ref.dispose();
				}});
			
			gc.gridx = 1;
			top.add(cancel, gc);
			
			this.pack();
			this.revalidate();
			this.repaint();
		}
		
	}

}

package com.Lyeeedar.GrammarEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher.ModelBatchers;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.ProceduralGeneration.LSystems.VolumePartitioner;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Main extends JFrame {

	String current = "";
	JPanel left;
	JPanel right;
	JPanel bottom;
	EnhancedJTextArea textArea;

	final Renderer renderer;

	public Main()
	{
		setSize(800, 600);
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				System.exit(0);
			}
		});
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		createMenuBar();
		seperateFrame();

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ParticleEditor";
		cfg.useGL30 = true;
		cfg.width = 800;
		cfg.height = 600;
		renderer = new Renderer();
		LwjglCanvas canvas = new LwjglCanvas(renderer, cfg);

		left.add(canvas.getCanvas());

		setVisible(true);

		right();
	}

	public void save()
	{
		File file = new File(current);
		final JFileChooser fc = new JFileChooser(file.getAbsolutePath());
		fc.setSelectedFile(file);
		int returnVal = fc.showSaveDialog(Main.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getAbsolutePath();

			if (path.endsWith(".json")){}
			else path += ".json";
			
			current = path;

			file = new File(path);

			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			String text = textArea.getText();

			BufferedWriter writer = null;
			try{
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(text);
			}catch ( IOException argh1){}
			finally{
				try{
					if ( writer != null)
						writer.close( );
					JOptionPane.showMessageDialog(Main.this,
							"Grammar saved",
							"",
							JOptionPane.PLAIN_MESSAGE);
				}catch ( IOException eargh2){}
			}
		} else {

		}
	}
	
	public void right()
	{
		right.removeAll();
		right.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		right.add(buttonPanel, BorderLayout.NORTH);

		JButton button = new JButton("Refresh");
		button.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{

				String path = new File("temp").getAbsolutePath();

				if (path.endsWith(".json")){}
				else path += ".json";

				File file = new File(path);

				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				String text = textArea.getText();

				BufferedWriter writer = null;
				try{
					writer = new BufferedWriter(new FileWriter(file));
					writer.write(text);
				}catch ( IOException argh1){argh1.printStackTrace();}
				finally{
					try{
						if ( writer != null)
							writer.close( );
					}catch ( IOException eargh2){}
				}

				renderer.reloadMesh("temp.json");


			}});

		buttonPanel.add(button);
		
		JButton indent = new JButton("Correct Indentation");
		indent.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				JsonValue content = new JsonReader().parse(textArea.getText());
				int caretpos = textArea.getCaretPosition();
				textArea.setText(new Json().prettyPrint(textArea.getText()));
				textArea.setCaretPosition(caretpos);
			}});
		
		buttonPanel.add(indent);

		if (textArea == null)
		{
			textArea = new EnhancedJTextArea(this);
			textArea.setEditable(true);

			AbstractDocument doc = (AbstractDocument)textArea.getDocument();
			doc.setDocumentFilter( new NewLineFilter(textArea) );
		}

		JScrollPane panel = new JScrollPane(textArea);

		right.add(panel);

		right.revalidate();
		right.repaint();
	}

	public void seperateFrame()
	{
		left = new JPanel();
		left.setLayout(new GridLayout(1, 1));
		right = new JPanel();
		right.setLayout(new GridLayout(1, 1));
		bottom = new JPanel();
		bottom.setLayout(new GridLayout(1, 1));

		left.setMinimumSize(new Dimension(500, 300));

		setLayout(new BorderLayout());

		add(left, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
	}

	public void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem miNew = new JMenuItem("New");
		JMenuItem miSave = new JMenuItem("Save");
		miSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}});

		JMenuItem miLoad = new JMenuItem("Load");
		miLoad.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser(new File("").getAbsolutePath());
				fc.addChoosableFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = getExtension(f);

						if (extension.equals("json")) return true;

						return false;
					}

					public String getExtension(File f) {
						String ext = null;
						String s = f.getName();
						int i = s.lastIndexOf('.');

						if (i > 0 &&  i < s.length() - 1) {
							ext = s.substring(i+1).toLowerCase();
						}
						return ext;
					}

					@Override
					public String getDescription() {
						return "Json only";
					}
				});
				int returnVal = fc.showOpenDialog(Main.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getAbsolutePath();
					current = path;

					File file = new File(path);

					String text = null;

					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					try {
						StringBuilder sb = new StringBuilder();
						String line = br.readLine();

						while (line != null) {
							sb.append(line);
							sb.append("\n");
							line = br.readLine();
						}
						text = sb.toString();
					} catch (IOException e1) {
						e1.printStackTrace();
					} finally {
						try {
							br.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					textArea.setText(text);

					right();

				} else {

				}
			}});
		JMenuItem miExit = new JMenuItem("Exit");

		fileMenu.add(miNew);
		fileMenu.add(miSave);
		fileMenu.add(miLoad);
		fileMenu.add(miExit);


		this.setJMenuBar(menuBar);
	}

	public static void main(String[] args) {

		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		System.setProperty("sun.awt.noerasebackground", "true");
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		try {
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
		} catch(Exception ex) {
			ex.printStackTrace();
		}


		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new Main();
			}
		});
	}

	class Renderer implements ApplicationListener
	{
		Controls controls;

		BitmapFont font;
		SpriteBatch batch;
		PerspectiveCamera cam;

		Entity entity;
		LightManager lightManager;

		int width;
		int height;

		float dist = 50;
		float Xangle = 0;
		float Yangle = 0;

		Vector3 tmp = new Vector3();
		Matrix4 tmpMat = new Matrix4();

		@Override
		public void create() {

			controls = new Controls(false);
			Gdx.input.setInputProcessor(controls.ip);

			font = new BitmapFont();
			batch = new SpriteBatch();

			lightManager = new LightManager();
			lightManager.ambientColour.set(0.6f, 0.6f, 0.6f);
			lightManager.directionalLight.direction.set(-0.5f, 1, 1).nor();
			lightManager.directionalLight.colour.set(0.5f, 0.5f, 0.5f);
			lightManager.sort(new Vector3());

			entity = new Entity(false, new MinimalPositionalData());

			batches.put(ModelBatchers.class, new ModelBatchers());
		}

		public void reloadMesh(String file)
		{
			entity.clearRenderables();

			VolumePartitioner vp = VolumePartitioner.load(file);
			vp.evaluate();
			vp.collectMeshes(entity);
		}

		@Override
		public void resize(int width, int height) {
			this.width = width;
			this.height = height;

			cam = new PerspectiveCamera(75, width, height);
			cam.near = 1.0f;
			cam.far = 5000f;

			cam.direction.set(GLOBALS.DEFAULT_ROTATION);
			cam.direction.rotate(Xangle, 0, 1, 0);
			Yrotate(Yangle);

			if (cam.direction.isZero(0.01f)) cam.direction.set(GLOBALS.DEFAULT_ROTATION);

			if (cam.direction.isZero(0.01f)) cam.direction.set(GLOBALS.DEFAULT_ROTATION);

			tmp.set(cam.direction).scl(-1*dist);
			cam.position.set(tmp);

			cam.update();
		}

		HashMap<Class, Batch> batches = new HashMap<Class, Batch>();
		@Override
		public void render() {

			Gdx.graphics.getGL20().glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
			Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			Gdx.graphics.getGL20().glEnable(GL20.GL_CULL_FACE);
			Gdx.graphics.getGL20().glEnable(GL20.GL_DEPTH_TEST);

			entity.update(Gdx.app.getGraphics().getDeltaTime());
			entity.queueRenderables(cam, lightManager, Gdx.app.getGraphics().getDeltaTime(), batches);

			((ModelBatchers) batches.get(ModelBatchers.class)).renderSolid(lightManager, cam);
			((ModelBatchers) batches.get(ModelBatchers.class)).renderTransparent(lightManager, cam);

			if (Gdx.input.isKeyPressed(Keys.UP))
			{
				dist -= Gdx.graphics.getDeltaTime() * 10;
			}

			if (Gdx.input.isKeyPressed(Keys.DOWN))
			{
				dist += Gdx.graphics.getDeltaTime() * 10;
			}

			if (Gdx.input.isTouched())
			{
				Xangle -= controls.getDeltaX();
				Yangle -= controls.getDeltaY();
			}

			dist += controls.scrolled();
			dist = MathUtils.clamp(dist, 5, 500);

			if (Yangle > 60) Yangle = 60;
			if (Yangle < -60) Yangle = -60;

			cam.direction.set(GLOBALS.DEFAULT_ROTATION);
			cam.up.set(GLOBALS.DEFAULT_UP);
			cam.direction.rotate(Xangle, 0, 1, 0);
			Yrotate(Yangle);

			tmp.set(cam.direction).scl(-1*dist);
			cam.position.set(tmp);

			cam.update();
		}

		public void Yrotate (float angle) {	
			Vector3 dir = tmp.set(cam.direction).nor();
			if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
			{
				Vector3 localAxisX = dir;
				localAxisX.crs(cam.up).nor();
				cam.rotate(angle, localAxisX.x, localAxisX.y, localAxisX.z);
			}
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
			Gdx.input.setInputProcessor(controls.ip);
		}

		@Override
		public void dispose() {
		}

	}

}

class NewLineFilter extends DocumentFilter
{
	private final JTextArea textArea;
	private final String[][] autoClose = {{"(", ")"}, {"[", "]"}, {"{", "}"}};
	public NewLineFilter(EnhancedJTextArea textArea)
	{
		this.textArea = textArea;
	}
	
	public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException
	{
		if (str.equals("\n"))
		{
			str = addWhiteSpace(fb.getDocument(), offs);
		}

		super.insertString(fb, offs, str, a);
	}

	public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException
	{
		boolean reset = false;
		int resetOffs = offs;
		if (str.equals("\n"))
		{
			String wspc = addWhiteSpace(fb.getDocument(), offs);
			resetOffs += wspc.length();
			str = wspc;
			System.out.println("char: "+fb.getDocument().getText(offs-1, 1));
			
			for (String[] pair : autoClose)
			{
				if (fb.getDocument().getText(offs-1, 1).equals(pair[0]))
				{
					System.out.println("Adding: "+wspc+pair[1]);
					
					resetOffs += 1;
					str += "\t";
					str += wspc+pair[1];
					reset = true;
				}
			}

		}

		super.replace(fb, offs, length, str, a);
		
		
		if (reset) textArea.setCaretPosition(resetOffs);
	}

	private String addWhiteSpace(Document doc, int offset) throws BadLocationException
	{
		StringBuilder whiteSpace = new StringBuilder("\n");
		Element rootElement = doc.getDefaultRootElement();
		int line = rootElement.getElementIndex( offset );
		int i = rootElement.getElement(line).getStartOffset();

		while (true)
		{
			String temp = doc.getText(i, 1);

			if (temp.equals(" ") || temp.equals("\t"))
			{
				whiteSpace.append(temp);
				i++;
			}
			else
				break;
		}

		return whiteSpace.toString();
	}
}

class EnhancedJTextArea extends JTextArea
{
	protected UndoAction undoAction;
	protected RedoAction redoAction;
	protected SaveAction saveAction;
	protected UndoManager undo = new UndoManager();
	private final Main mainFrame;

	public EnhancedJTextArea(Main mainFrame)
	{
		super(50, 75);
		this.getDocument().addUndoableEditListener(new MyUndoableEditListener());
		this.setTabSize(4);
		
		this.mainFrame = mainFrame;

		undoAction = new UndoAction();
		redoAction = new RedoAction();
		saveAction = new SaveAction();
		saveAction.updateSave();

		addBindings();
	}

	public Action getUndoAction()
	{
		return undoAction;
	}

	public Action getRedoAction()
	{
		return redoAction;
	}

	protected void addBindings() {
		InputMap inputMap = this.getInputMap();
		ActionMap actionMap = this.getActionMap();

		//Ctrl-b to go backward one character
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.backwardAction);

		//Ctrl-f to go forward one character
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.forwardAction);

		//Ctrl-p to go up one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.upAction);

		//Ctrl-n to go down one line
		key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
		inputMap.put(key, DefaultEditorKit.downAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
		inputMap.put(key, "Undo");
		actionMap.put("Undo", undoAction);

		key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
		inputMap.put(key, "Redo");
		actionMap.put("Redo", redoAction);
		
		key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
		inputMap.put(key, "Save");
		actionMap.put("Save", saveAction);

//		inputMap.put(KeyStroke.getKeyStroke("typed ("), "typed (");
//		actionMap.put("typed (", new BracketAction());
	}

	protected class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			//Remember the edit and update the menus
			undo.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	} 

	class BracketAction extends AbstractAction
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JTextComponent tc = (JTextComponent)e.getSource();
			try
			{
				int position = tc.getCaretPosition();
				tc.getDocument().insertString(position, "()", null);
				tc.setCaretPosition(position + 1);
			}
			catch(Exception e2) {}
		}

	}
	
	class SaveAction extends AbstractAction
	{
		public SaveAction() {
			super("Save");
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			mainFrame.save();
			
		}
		
		public void updateSave()
		{
			putValue(Action.NAME, "Save");
			setEnabled(true);
		}
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
}
/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 0.9.6rc1
 * Date: June 13, 2017
 * 
 */

package icradata;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;

@SuppressWarnings("serial")
public class ICrAData extends JFrame {
	
	/// Main entry point
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ICrAData();
			}
		});
	}
	
	/// Global variables
	private String ud = System.getProperty("user.dir");
	//private String fs = System.getProperty("file.separator");
	/// JTextArea internally uses \n which is in conflict with line.separator
	/// http://docs.oracle.com/javase/8/docs/api/javax/swing/text/DefaultEditorKit.html
	private String ls = "\n"; //System.getProperty("line.separator");
	
	/// DecimalFormat - # is optional, while 0 is always written - ###.#### or 000.0000
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private Font monoFont2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private Color backClr = new Color(238, 238, 238);
	
	/// Variable for the result of the calculations
	/// 0-U  1-V  2-S  3-A  4-B  5-points  6-vecA  7-vecB
	private Vector<double[][]> result = null;
	private File fileName = null;
	private Point pp = new Point();
	
	private JTextArea textA, textB;
	private JScrollPane scrollA, scrollB, resScroll;
	private JComboBox<String> comboMethod, comboSize, comboGrid, comboColor;
	private JPopupMenu rightMouse;
	
	private String[] methodNames = new String[] {"\u03BC-biased", "Unbiased", "\u03BD-biased", "Balanced", "Weighted"};
	private String[] sizeDim = new String[] {"500", "750", "1000", "2000", "3000", "5000"};
	
	/// Glass points, double[] saves X, Y, width, height of the rectangle
	private Vector<double[]> glassA = new Vector<double[]>();
	private Vector<double[]> glassB = new Vector<double[]>();
	private MyGlass glass;
	
	/// Colors for the tables
	private String[] colors = new String[] {
			"#FF0000", // 0 Red
			"#FF00FF", // 1 Magenta
			"#9370DB", // 2 DarkMagenta
			"#FFA500", // 3 Orange
			"#FF6347", // 4 Tomato
			"#808000", // 5 Olive
			"#87CEFA", // 6 LightSkyBlue
			"#1E90FF", // 7 DodgerBlue
			"#0000FF", // 8 Blue
			"#00FF7F", // 9 SpringGreen
			"#008000" // 10 Green
	};
	
	
	/// Constructor
	private ICrAData() {
		
		/// This is required
		Locale.setDefault(Locale.US);
		
		/// Look and feel
		try {
			/// Bold mono fonts are better on Windows
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				monoFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
				monoFont2 = new Font(Font.MONOSPACED, Font.BOLD, 14);
			} else
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		} catch (Exception ex) {
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}
		
		/// Set tool-tips to show after half a second and stay 60 seconds
		ToolTipManager.sharedInstance().setInitialDelay(500);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		
		
		/// Variables for the constructor
		Dimension dim100 = new Dimension(100, 25);
		Dimension dim150 = new Dimension(150, 25);
		Dimension scrollSize = new Dimension(500, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		
		/// Items for panel 1
		JButton btnOpen = new JButton("Open File");
		btnOpen.setPreferredSize(dim150);
		btnOpen.setToolTipText("Open a file.");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenListener(evt);
			}
		});
		
		JButton btnSave = new JButton("Save File");
		btnSave.setPreferredSize(dim150);
		btnSave.setToolTipText("Save the opened file, or choose a name for a new file.");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveListener(evt);
			}
		});
		
		JButton btnSave2 = new JButton("Save Copy");
		btnSave2.setPreferredSize(dim150);
		btnSave2.setToolTipText("Save a copy of the text in the input panel, under a new file name.");
		btnSave2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveCopyListener(evt);
			}
		});
		
		/// Items for panel 2
		comboMethod = new JComboBox<String>(methodNames);
		comboMethod.setToolTipText("Select a method for the calculations.");
		comboMethod.setPreferredSize(dim150);
		comboMethod.setFont(monoFont);
		
		JButton btnAnalysis = new JButton("Analysis");
		btnAnalysis.setPreferredSize(dim150);
		btnAnalysis.setToolTipText("Make the calculations and display them.");
		btnAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAnalysisListener(evt);
			}
		});
		
		JButton btnViewData = new JButton("View Data");
		btnViewData.setPreferredSize(dim150);
		btnViewData.setToolTipText("View the matrix data.");
		btnViewData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnViewDataListener(evt);
			}
		});
		
		/// Items for panel 3
		comboSize = new JComboBox<String>(sizeDim);
		comboSize.setToolTipText("Select the plot size in pixels.");
		comboSize.setPreferredSize(dim100);
		comboSize.setEditable(true);
		comboSize.addItemListener(new ComboSizeListener());
		
		comboGrid = new JComboBox<String>(
				new String[] {"Triangle", "Tick marks", "Full grid"});
		comboGrid.setToolTipText("Select the display type.");
		comboGrid.setPreferredSize(dim100);
		comboGrid.addItemListener(new ComboGridListener());
		
		comboColor = new JComboBox<String>(
				new String[] {"Black/White", "Red/Gray"});
		comboColor.setToolTipText("Select the plot colors.");
		comboColor.setPreferredSize(dim100);
		comboColor.addItemListener(new ComboColorListener());
		
		JButton btnExport = new JButton("Export");
		btnExport.setPreferredSize(dim100);
		btnExport.setToolTipText("Export the graphic.");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnExportListener(evt);
			}
		});
		
		/// Panel 1
		JPanel panel1 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel1.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel1.add(btnOpen);
		panel1.add(btnSave);
		panel1.add(btnSave2);
		
		/// Panel 2
		JPanel panel2 = new JPanel();
		panel2.setBackground(backClr);
		panel2.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel2.add(comboMethod);
		panel2.add(btnAnalysis);
		panel2.add(btnViewData);
		
		/// Panel 3
		JPanel panel3 = new JPanel();
		panel3.setBackground(Color.WHITE);
		panel3.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel3.add(comboSize);
		panel3.add(comboGrid);
		panel3.add(comboColor);
		panel3.add(btnExport);
		
		/// Undo manager
		final UndoManager undoMan = new UndoManager();
		/// Actions for the manager - AbstractAction implements Action and ActionListener
		Action actUndo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				if (undoMan.canUndo())
					undoMan.undo();
			}
		};
		Action actRedo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				if (undoMan.canRedo())
					undoMan.redo();
			}
		};
		actUndo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		actRedo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		
		/// Right mouse menu
		JMenuItem mCut = new JMenuItem(new DefaultEditorKit.CutAction());
		JMenuItem mCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
		JMenuItem mPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
		JMenuItem mUndo = new JMenuItem(actUndo);
		JMenuItem mRedo = new JMenuItem(actRedo);
		mCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		mCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		mPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		mUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		mRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		mCut.setBackground(backClr);
		mCopy.setBackground(backClr);
		mPaste.setBackground(backClr);
		mUndo.setBackground(backClr);
		mRedo.setBackground(backClr);
		mCut.setText("Cut");
		mCopy.setText("Copy");
		mPaste.setText("Paste");
		mUndo.setText("Undo");
		mRedo.setText("Redo");
		
		JSeparator mSep = new JSeparator();
		mSep.setBackground(backClr);
		
		rightMouse = new JPopupMenu();
		rightMouse.add(mCut);
		rightMouse.add(mCopy);
		rightMouse.add(mPaste);
		rightMouse.add(mSep);
		rightMouse.add(mUndo);
		rightMouse.add(mRedo);
		
		/// Items for text areas
		textA = new JTextArea("# Copy/paste data here with Ctrl-C/Ctrl-V" + ls +
				"# Lines starting with # are ignored" + ls +
				"# Valid column separators are: tab semicolon comma" + ls +
				"# Numbers 1,7 and 1.7 are recognized" + ls + ls +
				"6;5;3;7;6" + ls +
				"7;7;8;1;3" + ls +
				"4;3;5;9;1" + ls +
				"4;5;6;7;8" + ls + ls);
		textA.addMouseListener(new RightMouseListener());
		textA.setDragEnabled(true);
		textA.setFont(monoFont2);
		textA.getDocument().addUndoableEditListener(undoMan);
		textA.getInputMap().put((KeyStroke)actUndo.getValue(Action.ACCELERATOR_KEY), "ACTION_UNDO");
		textA.getInputMap().put((KeyStroke)actRedo.getValue(Action.ACCELERATOR_KEY), "ACTION_REDO");
		textA.getActionMap().put("ACTION_UNDO", actUndo);
		textA.getActionMap().put("ACTION_REDO", actRedo);
		
		textB = new JTextArea();
		textB.setFont(monoFont2);
		textB.setEditable(false);
		
		JScrollPane scrollTextA = new JScrollPane(textA);
		scrollTextA.setPreferredSize(new Dimension(450, 430));
		scrollTextA.setBorder(null);
		
		JScrollPane scrollTextB = new JScrollPane(textB);
		scrollTextB.setPreferredSize(new Dimension(450, 130));
		scrollTextB.setBorder(null);
		
		scrollA = new JScrollPane();
		scrollA.setPreferredSize(scrollSize);
		scrollA.getViewport().addChangeListener(new ScrollAListener());
		scrollA.setBorder(null);
		
		scrollB = new JScrollPane();
		scrollB.setPreferredSize(scrollSize);
		scrollB.getViewport().addChangeListener(new ScrollBListener());
		scrollB.setBorder(null);
		
		resScroll = new JScrollPane();
		resScroll.getViewport().setBackground(Color.WHITE);
		resScroll.setBorder(null);
		
		/// Panel text areas
		JPanel panelTextAreas = new JPanel();
		panelTextAreas.setLayout(new BorderLayout());
		panelTextAreas.add(panel1, BorderLayout.NORTH);
		panelTextAreas.add(scrollTextA, BorderLayout.CENTER);
		
		/// Split text areas
		JSplitPane splitTextAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTextAreas, scrollTextB);
		splitTextAreas.setOneTouchExpandable(true);
		splitTextAreas.setContinuousLayout(true);
		splitTextAreas.setResizeWeight(1.0);
		splitTextAreas.setDividerSize(10);
		
		/// Split tables
		JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollA, scrollB);
		splitTables.setOneTouchExpandable(true);
		splitTables.setContinuousLayout(true);
		splitTables.setResizeWeight(0.5);
		splitTables.setDividerSize(10);
		
		/// Panel tables
		JPanel panelTables = new JPanel();
		panelTables.setLayout(new BorderLayout());
		panelTables.add(panel2, BorderLayout.NORTH);
		panelTables.add(splitTables, BorderLayout.CENTER);
		
		/// Panel graphic
		JPanel panelGraphic = new JPanel();
		panelGraphic.setLayout(new BorderLayout());
		panelGraphic.add(panel3, BorderLayout.NORTH);
		panelGraphic.add(resScroll, BorderLayout.CENTER);
		
		/// Split left-center
		JSplitPane splitLeftCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitTextAreas, panelTables);
		splitLeftCenter.setOneTouchExpandable(true);
		splitLeftCenter.setContinuousLayout(true);
		splitLeftCenter.setResizeWeight(0.0);
		splitLeftCenter.setDividerSize(10);
		
		/// Split center-right
		JSplitPane splitCenterRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeftCenter, panelGraphic);
		splitCenterRight.setOneTouchExpandable(true);
		splitCenterRight.setContinuousLayout(true);
		splitCenterRight.setResizeWeight(1.0);
		splitCenterRight.setDividerSize(10);
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(splitCenterRight, BorderLayout.CENTER);
		
		setTitle("ICrAData v0.9.6rc1");
		setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitListener(evt);
			}
		});
		
		/// Glass pane
		glass = new MyGlass(this.getContentPane());
		setGlassPane(glass);
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		pack(); /// Size of frame equal to the size of its components
		//setResizable(false);
		setLocationByPlatform(true);
		//setLocationRelativeTo(null); /// Center frame on screen
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		/// These must be here, do not move
		//glass.setVisible(true); // this does not show cursor for resize for table columns
		setVisible(true);
		
	}
	
	/// Show message in lower panel
	private void showMessage(String msgType, String msgText) {
		
		if (msgType.equals("info"))
			textB.append(msgText + ls);
		else if (msgType.equals("warn"))
			textB.append("[WARN] " + msgText + ls);
		else
			textB.append("[ERROR] " + msgText + ls);
		
		textB.setCaretPosition(textB.getText().length());
		
	}
	
	/// Prompt on window closing
	private void exitListener(WindowEvent evt) {
		if (result != null) {
			if (JOptionPane.showConfirmDialog(
					this, "Exit the program?", "Exit",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
					== JOptionPane.YES_OPTION)
				System.exit(0);
		} else
			System.exit(0);
	}
	
	/// Open file
	private void btnOpenListener(ActionEvent evt) {
		
		JFileChooser chOpen = new JFileChooser(ud);
		chOpen.setMultiSelectionEnabled(false);
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated file (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File openFile = chOpen.getSelectedFile();
		
		try {
			String str = "";
			String line = "";
			
			BufferedReader br = new BufferedReader(new FileReader(openFile));
			while ((line = br.readLine()) != null)
				str += line + ls;
			
			br.close();
			
			textA.setText(str);
			fileName = chOpen.getSelectedFile();
			showMessage("info", "Opened file " + openFile.getName() + " (" + openFile + ")");
			
		} catch (Exception ex) {
			showMessage("error", "Could not open file " + openFile.getName() + " (" + openFile + ")");
		}
	}
	
	/// Save file
	private void btnSaveListener(ActionEvent evt) {
		
		if (fileName != null) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
				bw.write(textA.getText().replace("\n", "\r\n"));
				bw.close();
				
				showMessage("info", "Saved file " + fileName.getName() + " (" + fileName + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save file " + fileName.getName() + " (" + fileName + ")");
			}
			
		} else if (textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			if (!saveFile.toString().toLowerCase().endsWith(".txt"))
				saveFile = new File(chSave.getSelectedFile() + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
				bw.write(textA.getText().replace("\n", "\r\n"));
				bw.close();
				
				fileName = saveFile;
				showMessage("info", "Saved file " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save file " + saveFile.getName() + " (" + saveFile + ")");
			}
			
		} else
			showMessage("info", "No file opened and no text written in the input panel");
		
	}
	
	/// Save file as copy
	private void btnSaveCopyListener(ActionEvent evt) {
		
		if (textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			if (!saveFile.toString().toLowerCase().endsWith(".txt"))
				saveFile = new File(chSave.getSelectedFile() + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
				bw.write(textA.getText().replace("\n", "\r\n"));
				bw.close();
				
				showMessage("info", "Saved a copy as " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save a copy as " + saveFile.getName() + " (" + saveFile + ")");
			}
		} else
			showMessage("info", "No text written in the input panel");
		
	}
	
	/// View matrix data
	private void btnViewDataListener(ActionEvent evt) {
		
		if (result != null) {
			final JTextArea text = new JTextArea();
			text.setFont(monoFont2);
			text.addMouseListener(new RightMouseListener());
			
			JScrollPane scrollText = new JScrollPane(text);
			scrollText.setPreferredSize(new Dimension(900, 500));
			
			final Dimension dim125 = new Dimension(125, 25);
			final JFrame viewFrame = new JFrame();
			
			/// Separator - \t ; ,
			final String[] separator = new String[] {"\t", ";", ","};
			
			/// Locale - true for US 1.7 and false for BG 1,7
			final boolean[] locale = new boolean[] {true, false};
			
			/// Show input data
			text.setText( showArray(result.get(0), separator[0], locale[0]) );
			
			/// Combo boxes
			final JComboBox<String> comboData = new JComboBox<String>(
					new String[] {"Input Data", "Criteria Matrix", "Sign Matrix", "Matrix \u03BC", "Matrix \u03BD",
							"Plot Points", "Vector \u03BC", "Vector \u03BD"});
			comboData.setToolTipText("Select a matrix for display.");
			comboData.setPreferredSize(dim125);
			final JComboBox<String> comboSeparator = new JComboBox<String>(
					new String[] {"Tab \\t", "Semicolon ;", "Comma ,"});
			comboSeparator.setToolTipText("Select the column separator.");
			comboSeparator.setPreferredSize(dim125);
			final JComboBox<String> comboLocale = new JComboBox<String>(
					new String[] {"Decimal point", "Decimal comma"});
			comboLocale.setToolTipText("Select the decimal separator.");
			comboLocale.setPreferredSize(dim125);
			
			comboData.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						text.setText(showArray(result.get(comboData.getSelectedIndex()),
								separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]));
						viewFrame.setTitle("View matrix data " + methodNames[comboMethod.getSelectedIndex()]);
					}
				}
			});
			comboSeparator.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						text.setText(showArray(result.get(comboData.getSelectedIndex()),
								separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]));
						viewFrame.setTitle("View matrix data " + methodNames[comboMethod.getSelectedIndex()]);
					}
				}
			});
			comboLocale.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						text.setText(showArray(result.get(comboData.getSelectedIndex()),
								separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]));
						viewFrame.setTitle("View matrix data " + methodNames[comboMethod.getSelectedIndex()]);
					}
				}
			});
			
			JButton btnClose = new JButton("Close");
			btnClose.setPreferredSize(dim125);
			btnClose.setToolTipText("Close the window.");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					viewFrame.setVisible(false);
				}
			});
			
			/// Panel for buttons
			JPanel panelBtn = new JPanel();
			panelBtn.setBackground(backClr);
			panelBtn.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			panelBtn.add(comboData);
			panelBtn.add(comboSeparator);
			panelBtn.add(comboLocale);
			panelBtn.add(btnClose);
			
			/// Frame options
			viewFrame.setTitle("View matrix data " + methodNames[comboMethod.getSelectedIndex()]);
			viewFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
			
			viewFrame.setLayout(new BorderLayout());
			viewFrame.add(panelBtn, BorderLayout.NORTH);
			viewFrame.add(scrollText, BorderLayout.CENTER);
			
			viewFrame.pack();
			viewFrame.setLocationByPlatform(true);
			//resFrame.setLocationRelativeTo(null); /// Center frame on screen
			viewFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			viewFrame.setVisible(true);
		}
		
	}
	
	/// Export the graphic
	private void btnExportListener(ActionEvent evt) {
		
		if (result != null) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			if (!chName.toString().toLowerCase().endsWith(".png"))
				chName = new File(chName.toString() + ".png");
			
			try {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				
				BufferedImage img = new BufferedImage(resPanel.getWidth(), resPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				resPanel.printAll(g);
				
				//Graphics2D g2 = (Graphics2D)img.getGraphics();
				//resPanel.printAll(g2);
				//resPanel.paintAll(g2);
				
				//FileOutputStream fos = new FileOutputStream(ud + "test.jpg");
				//JPEGImageEncoderImpl jpeg = new JPEGImageEncoderImpl(fos);
				
				ImageIO.write(img, "png", chName.getAbsoluteFile());
				
			} catch (Exception ex) {
				showMessage("error", "Could not export the graphic: " + chName.getName());
				//ex.printStackTrace();
			}
		}
	}
	
	private class ComboSizeListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null) {
					int val = 500;
					if (comboSize.getSelectedIndex() > 0)
						val = Integer.parseInt(sizeDim[comboSize.getSelectedIndex()]);
					else if (checkInt((String)comboSize.getSelectedItem()))
						val = Integer.parseInt((String)comboSize.getSelectedItem());
					if (val < 100 || val > 100000)
						val = 500;
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setPreferredSize(new Dimension(val, val));
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	private class ComboGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setMyGrid(comboGrid.getSelectedIndex());
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	private class ComboColorListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setMyColor(comboColor.getSelectedIndex());
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	/// Synchronize scrolling for A
	private class ScrollAListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			int valH = scrollA.getHorizontalScrollBar().getValue();
			int valV = scrollA.getVerticalScrollBar().getValue();
			//System.out.println(valH + " " + valV);
			scrollB.getHorizontalScrollBar().setValue(valH);
			scrollB.getVerticalScrollBar().setValue(valV);
		}
	}
	
	/// Synchronize scrolling for B
	private class ScrollBListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			int valH = scrollB.getHorizontalScrollBar().getValue();
			int valV = scrollB.getVerticalScrollBar().getValue();
			//System.out.println(valH + " " + valV);
			scrollA.getHorizontalScrollBar().setValue(valH);
			scrollA.getVerticalScrollBar().setValue(valV);
		}
	}
	
	/// Listener for right mouse button on text components
	private class RightMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton(evt))
				rightMouse.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
	}
	
	/// Panel for plotting - implements MouseListener, MouseMotionListener, ComponentListener
	private class MyPanel extends JPanel implements Scrollable, MouseListener, MouseMotionListener {
		
		private double[][] points;
		private Rectangle[] arrRect;
		private int plotGrid;
		private int plotColor;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			//setAutoscrolls(true);
			addMouseListener(this);
			addMouseMotionListener(this);
			//addComponentListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		private void setMyGrid(int val) {
			plotGrid = val;
		}
		
		private void setMyColor(int val) {
			plotColor = val;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int width = getWidth();
			int height = getHeight();
			int delta = 20;
			
			/// Grid
			g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
			g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
			g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			
			if (plotGrid == 2) {
				g2.draw(new Line2D.Double(width-delta, delta, width-delta, height-delta)); // right
				g2.draw(new Line2D.Double(delta, delta, width-delta, delta)); // top
				//g2.draw(new Line2D.Double(width-delta, delta, delta, height-delta)); // secondary diagonal
			}
			
			/// Scale so that there are boundaries on the graph
			int widthScale = width-2*delta;
			int heightScale = height-2*delta;
			int line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			if (plotGrid == 1) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
			}
			if (plotGrid == 2) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], width-delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], delta-line, delta+widthScale*marks[i], height-delta+line));
			}
			
			/// Colors - black/white, red/gray
			//g2.setPaint(Color.RED);
			g2.setColor(Color.BLACK);
			setBackground(Color.WHITE);
			if (plotColor == 1) {
				g2.setColor(Color.RED);
				setBackground(backClr);
			}
			
			/// Save rectangles for glass
			arrRect = new Rectangle[points.length];
			int radius = 5;
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < points.length; i++) {
				double x = points[i][0]*widthScale+delta;
				double y = heightScale-points[i][1]*heightScale+delta;
				
				g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				arrRect[i] = new Rectangle((int)x-radius, (int)y-radius, 2*radius, 2*radius);
			}
			
		}
		
		/// Set tooltip text
		public String getToolTipText(MouseEvent evt) {
			
			for (int i = 0; i < arrRect.length; i++) {
				if (arrRect[i].contains(evt.getX(), evt.getY())) {
					return "<html>Coordinates: " + points[i][0] + " " + points[i][1] +
							"<br/>Row: " + Math.round(points[i][2] + 1) + " Column: " + Math.round(points[i][3] + 1) + "</html>";
							//"<br/>Point: " + evt.getX() + " " + evt.getY() + "</html>";
				}
			}
			
			return null;
		}
		
		/// Show the glass when the mouse button is pressed and held
		public void mousePressed(MouseEvent evt) {
			
			/// Set point for dragging
			pp.setLocation(evt.getPoint());
			
			/// Change cursor to crosshair
			//((JPanel)evt.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			//this.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			glass.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			
			/// Clear all points from memory
			glassA.clear();
			glassB.clear();
			
			for (int i = 0; i < arrRect.length; i++) {
				if (arrRect[i].contains(evt.getX(), evt.getY())) {
					MyTable tableA = (MyTable)scrollA.getViewport().getView();
					MyTable tableB = (MyTable)scrollB.getViewport().getView();
					
					/// Rows and columns have index from 0, 1, 2, 3, etc
					/// Rows: the column table header is not counted towards the rows, therefore it is correct
					/// Columns: We have row table header, therefore the column is +1
					/// Wrong cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3])
					/// Correct cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1)
					Rectangle cellA = tableA.getCellRect((int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1), false);
					Rectangle cellB = tableB.getCellRect((int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1), false);
					
					/// Add the points to the glass
					glassA.add(new double[] {cellA.getX(), cellA.getY(), cellA.getWidth(), cellA.getHeight()});
					glassB.add(new double[] {cellB.getX(), cellB.getY(), cellB.getWidth(), cellB.getHeight()});
					
					/*System.out.println("Cell: " + cellA.getX() + " " + cellA.getY() + " " + cellA.getWidth() + " " + cellA.getHeight());
					System.out.println("Coordinates: " + points[i][0] + " " + points[i][1] +
							"   Row: " + Math.round(points[i][2]) + " Column: " + Math.round(points[i][3]) +
							"   Point: " + evt.getX() + " " + evt.getY());
					*/
				}
			}
			
			/// Show the rectangles on the glass
			if (glassA.size() > 0) {
				glass.setVisible(true);
				glass.repaint();
			} else
				glass.setVisible(false);
		}
		
		/// Hide the glass when the mouse button is released
		public void mouseReleased(MouseEvent evt) {
			//((JPanel)evt.getSource()).setCursor(Cursor.getDefaultCursor());
			//this.getRootPane().setCursor(Cursor.getDefaultCursor());
			glass.setCursor(Cursor.getDefaultCursor());
			glass.setVisible(false);
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		public void mouseMoved(MouseEvent evt) {}
		
		/// Move the picture when dragging the mouse
		public void mouseDragged(MouseEvent evt) {
			JPanel thePanel = (JPanel) evt.getSource();
			JViewport theView = (JViewport) thePanel.getParent();
			Point cp = evt.getPoint();
			Point vp = theView.getViewPosition();
			//System.out.println(cp.getX() + " " + cp.getY() + "    " + vp.getX() + " " + vp.getY());
			vp.translate(pp.x-cp.x, pp.y-cp.y);
			thePanel.scrollRectToVisible(new Rectangle(vp, theView.getSize()));
			pp.setLocation(cp);
		}
		
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
		
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}
		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}
		
		public boolean getScrollableTracksViewportHeight() {
			return false; /// default is true, need false
		}
		
		public boolean getScrollableTracksViewportWidth() {
			return false; /// default is true, need false
		}
		
	}
	
	/// Glass pane for drawing
	private class MyGlass extends JComponent {
		
		private Container cont;
		
		private MyGlass(Container contentPane) {
			cont = contentPane;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			for (int i = 0; i < glassA.size(); i++) {
				double[] pointA = glassA.get(i);
				double[] pointB = glassB.get(i);
				Point locF = cont.getParent().getLocationOnScreen();
				
				Point locSA = scrollA.getLocationOnScreen();
				Point locSB = scrollB.getLocationOnScreen();
				Rectangle visSA = scrollA.getViewportBorderBounds();
				Rectangle visSB = scrollB.getViewportBorderBounds();
				
				MyTable tableA = (MyTable)scrollA.getViewport().getView();
				MyTable tableB = (MyTable)scrollB.getViewport().getView();
				
				Point locA = tableA.getLocationOnScreen();
				Point locB = tableB.getLocationOnScreen();
				//Rectangle visibleA = tableA.getVisibleRect();
				//Rectangle visibleB = tableB.getVisibleRect();
				
				int header = tableA.getTableHeader().getHeight();
				int eps = 2;
				//System.out.println(locA.getX() + " " + locA.getY() + "     " + locF.getX() + " " + locF.getY());
				
				/// Draw cells
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLUE);
				
				if (locA.getX() - locF.getX() + pointA[0] >= locSA.getX() - locF.getX() &&
					locA.getY() - locF.getY() + pointA[1] >= locSA.getY() - locF.getY() + header &&
					locA.getX() - locF.getX() + pointA[0] + pointA[2] <= locSA.getX() - locF.getX() + visSA.width + eps &&
					locA.getY() - locF.getY() + pointA[1] + pointA[3] <= locSA.getY() - locF.getY() + visSA.height + header + eps)
					g2.draw(new Rectangle2D.Double(
						locA.getX() - locF.getX() + pointA[0], locA.getY() - locF.getY() + pointA[1], pointA[2], pointA[3]));
				
				if (locB.getX() - locF.getX() + pointB[0] >= locSB.getX() - locF.getX() &&
					locB.getY() - locF.getY() + pointB[1] >= locSB.getY() - locF.getY() + header &&
					locB.getX() - locF.getX() + pointB[0] + pointB[2] <= locSB.getX() - locF.getX() + visSB.width + eps &&
					locB.getY() - locF.getY() + pointB[1] + pointB[3] <= locSB.getY() - locF.getY() + visSB.height + header + eps)
					g2.draw(new Rectangle2D.Double(
						locB.getX() - locF.getX() + pointB[0], locB.getY() - locF.getY() + pointB[1], pointB[2], pointB[3]));
				
				/// Draw table
				//g2.setColor(Color.RED);
				//g2.draw(new Rectangle2D.Double(locA.getX() - locF.getX(), locA.getY() - locF.getY(), visibleA.width, visibleA.height));
				//g2.draw(new Rectangle2D.Double(locB.getX() - locF.getX(), locB.getY() - locF.getY(), visibleB.width, visibleB.height));
				
				/// Draw scrollpane
				//g2.setColor(Color.GREEN);
				//g2.draw(new Rectangle2D.Double(locSA.getX() - locF.getX(), locSA.getY() - locF.getY() + header, visSA.width, visSA.height));
				//g2.draw(new Rectangle2D.Double(locSB.getX() - locF.getX(), locSB.getY() - locF.getY() + header, visSB.width, visSB.height));
				
			}
			
		}
	}
	
	/// Table for data view
	private class MyTable extends JTable {
		
		private double[][] data;
		private String[] rows;
		private String[] cols;
		
		private MyTable(double[][] arrData, String[] rowNames, String[] colNames) {
			if (arrData != null) {
				data = arrData;
				rows = rowNames;
				cols = colNames;
				
				setModel(new MyTableModel());
				setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				setAutoCreateRowSorter(true);
				
				getTableHeader().setFont(monoFont);
				//getTableHeader().setResizingAllowed(false);
				getTableHeader().setReorderingAllowed(false);
				
				setDefaultRenderer(Double.class, new MyCellRenderer());
				//setDefaultRenderer(String.class, new ExCellRenderer());
			}
		}
		
		private class MyTableModel extends AbstractTableModel {
			
			public int getRowCount() {
				return data.length;
			}
			
			public int getColumnCount() {
				return cols.length;
			}
			
			public Object getValueAt(int row, int col) {
				if (col == 0)
					return rows[row];
				else
					return numFormat.format(data[row][col-1]);
					//return data[row][col];
			}
			
			public Class<?> getColumnClass(int col) {
				if (col == 0)
					return String.class;
				else
					return Double.class;
			}
			
			public String getColumnName(int col) {
				return cols[col];
			}
			
			/*public boolean isCellEditable(int row, int col) {
				return true;
			}
			
			public void setValueAt(Object value, int row, int col) {
				if ( dblParse(value.toString()) ) {
					data[row][col] = Double.parseDouble(value.toString());
					fireTableCellUpdated(row, col);
				}
			}*/
		}
		
		private class MyCellRenderer extends DefaultTableCellRenderer {
			
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				
				setFont(monoFont);
				setHorizontalAlignment(SwingConstants.RIGHT);
				
				if (checkDbl( (String)value )) {
					double val = Double.parseDouble( (String)value );
					int ind = (int)Math.round(val*10);
					setForeground( htmlColor(colors[ind]) );
					//setToolTipText("val " + Math.round(val*10));
					
					/*if (val == 0.0)
						setForeground(Color.RED);
					else if (val == 1.0)
						setForeground(Color.GREEN);
					else
						setForeground(Color.BLACK);*/
				}
				
				if (hasFocus)
					setBackground(Color.WHITE);
				else if (isSelected)
					setBackground(Color.LIGHT_GRAY);
				else
					setBackground(Color.WHITE);
				
				return this;
			}
		}
		
		/*private class ExCellRenderer extends DefaultTableCellRenderer {
			
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				
				setFont(monoFont);
				setHorizontalAlignment(SwingConstants.CENTER);
				
				
				return this;
			}
		}*/
		
		public Component prepareRenderer(TableCellRenderer rend, int row, int col) {
			
			if (col == 0)
				return this.getTableHeader().getDefaultRenderer()
						.getTableCellRendererComponent(this, this.getValueAt(row, col), false, false, row, col);
			else
				return super.prepareRenderer(rend, row, col);
		}
		
	}
	
	/// Check if string is integer
	private boolean checkInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Check if string is double
	private boolean checkDbl(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Colors for table
	private Color htmlColor(String hex) {
		
		Color res = new Color(0, 0, 0);
		
		if (hex.startsWith("#"))
			hex = hex.substring(1);
		
		if (hex.length() == 6) {
			int red = Integer.parseInt(hex.substring(0, 2), 16);
			int green = Integer.parseInt(hex.substring(2, 4), 16);
			int blue = Integer.parseInt(hex.substring(4, 6), 16);
			
			res = new Color(red, green, blue);
		}
		
		return res;
	}
	
	/// Determine separator
	private String findSep(Vector<String> vec) {
		
		String line = vec.get(0);
		String separator = "";
		
		if (line.contains("\t"))
			separator = "\t";
		else if (line.contains(";"))
			separator = ";";
		else if (line.contains(","))
			separator = ",";
		
		return separator;
	}
	
	/// Find column
	private int findCol(Vector<String> vec, String separator) {
		
		String[] arr = vec.get(0).split(separator);
		int col = arr.length;
		
		for (int i = 1; i < vec.size(); i++) {
			arr = vec.get(i).split(separator);
			if (col != arr.length)
				return 0;
		}
		
		return col;
	}
	
	/// Load data from the user input
	private void btnAnalysisListener(ActionEvent evt) {
		
		try {
			String data = textA.getText();
			
			if (data.length() == 0) {
				showMessage("info", "Nothing to load");
				return;
			}
			
			Vector<String> vec = new Vector<String>();
			String[] lines = data.split("\r\n|\n|\r");
			for (int i = 0; i < lines.length; i++) {
				if (!lines[i].startsWith("#") && lines[i].length() != 0)
					vec.add(lines[i]);
			}
			
			String separator = "";
			if ((separator = findSep(vec)) == "") {
				showMessage("warn", "Could not determine column separator");
				return;
			}
			
			int col = 0;
			if ((col = findCol(vec, separator)) == 0) {
				showMessage("warn", "Columns are inconsistent");
				return;
			}
			
			if (col < 3 || vec.size() < 3) {
				showMessage("warn", "Minimum matrix size is 3x3");
				return;
			}
			
			String[] arr = vec.get(0).split(separator);
			double[][] res = new double[vec.size()][arr.length];
			
			for (int i = 0; i < vec.size(); i++) {
				arr = vec.get(i).split(separator);
				for (int j = 0; j < arr.length; j++) {
					String val = arr[j].replace(",", ".");
					if (checkDbl(val))
						res[i][j] = Double.parseDouble(val);
				}
			}
			
			/// Make
			makeDisplay(res, comboMethod.getSelectedIndex());
			showMessage("info", "Analysis " + methodNames[comboMethod.getSelectedIndex()]);
			
		} catch (Exception ex) {
			showMessage("error", "Could not load the data");
		}
	}
	
	/// Make calculations and display them
	private void makeDisplay(double[][] arrU, int method) {
		
		try {
			result = makeCalc(arrU, method);
			
			if (result != null) {
				
				double[][] arrA = result.get(3);
				double[][] arrB = result.get(4);
				
				/// Align formatting of R1, R10, R100, etc
				String pRow = "0";
				String pCol = "0";
				if (arrA.length >= 10) pRow += "0";
				if (arrA[0].length >= 10) pCol += "0";
				if (arrA.length >= 100) pRow += "0";
				if (arrA[0].length >= 100) pCol += "0";
				if (arrA.length >= 1000) pRow += "0";
				if (arrA[0].length >= 1000) pCol += "0";
				
				DecimalFormat rowFormat = new DecimalFormat(pRow, new DecimalFormatSymbols(Locale.US));
				DecimalFormat colFormat = new DecimalFormat(pCol, new DecimalFormatSymbols(Locale.US));
				
				/// First row header
				String[] rowNames = new String[arrA.length];
				for (int i = 0; i < rowNames.length; i++)
					rowNames[i] = "R" + rowFormat.format(Integer.valueOf(i + 1));
				
				/// Table column header - account for row header
				String[] colNames = new String[arrA[0].length + 1];
				for (int j = 1; j < colNames.length; j++)
					colNames[j] = "C" + colFormat.format(j);
				
				/// Create the tables
				colNames[0] = "\u03BC";
				MyTable tableA = new MyTable(arrA, rowNames, colNames);
				scrollA.setViewportView(tableA);
				
				colNames[0] = "\u03BD";
				MyTable tableB = new MyTable(arrB, rowNames, colNames);
				scrollB.setViewportView(tableB);
				
				/// Create the graphic
				double[][] arrPoints = result.get(5);
				MyPanel resPanel = new MyPanel(arrPoints);
				resPanel.setPreferredSize(new Dimension(500, 500));
				comboSize.setSelectedIndex(0);
				comboGrid.setSelectedIndex(0);
				comboColor.setSelectedIndex(0);
				resScroll.setViewportView(resPanel);
				
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not display the result");
			//ex.printStackTrace();
		}
	}
	
	/// Make the calculations
	private Vector<double[][]> makeCalc(double[][] arrU, int method) {
		
		try {
			/// Criteria matrix
			double[][] arrV = makeCrit(arrU);
			/// Sign matrix
			double[][] arrS = makeSign(arrV);
			
			double[][] arrA = new double[1][1];
			double[][] arrB = new double[1][1];
			
			/// MU-biased 0  Unbiased 1  NU-biased 2  Balanced 3  Weighted 4
			if (method == 3) {
				/// Make MU-biased 0
				double[][] arrA0 = makeA(arrS, 0);
				double[][] arrB0 = makeB(arrS, 0);
				/// Make NU-biased 2
				double[][] arrA2 = makeA(arrS, 2);
				double[][] arrB2 = makeB(arrS, 2);
				/// Balanced 3
				arrA = matrixMeanValue(arrA0, arrA2);
				arrB = matrixMeanValue(arrB0, arrB2);
				
			} else if (method == 4) {
				/// Make Unbiased 1
				double[][] arrA1 = makeA(arrS, 1);
				double[][] arrB1 = makeB(arrS, 1);
				/// Make P matrix = MU+NU
				double[][] arrP = matrixAddition(arrA1, arrB1);
				//System.out.println(showArray(arrP, ";", true));
				
				/// Weighted 4
				arrA = matrixWeighted(arrA1, arrP);
				arrB = matrixWeighted(arrB1, arrP);
				
			} else {
				arrA = makeA(arrS, method);
				arrB = makeB(arrS, method);
			}
			
			/// Plot points
			double[][] arrPoints = makePlotData(arrA, arrB);
			
			/// Vector data for export
			double[][] vectorA = makeVectorData(arrA);
			double[][] vectorB = makeVectorData(arrB);
			
			/// Return result as vector
			Vector<double[][]> result = new Vector<double[][]>();
			result.add(0, arrU);
			result.add(1, arrV);
			result.add(2, arrS);
			result.add(3, arrA);
			result.add(4, arrB);
			result.add(5, arrPoints);
			result.add(6, vectorA);
			result.add(7, vectorB);
			
			return result;
			
		} catch (Exception ex) {
			showMessage("error", "Could not make the calculations");
			//ex.printStackTrace();
			return null;
		}
	}
	
	/// Make criteria matrix
	private double[][] makeCrit(double[][] arr) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][(cols*(cols-1))/2];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					res[i][cc] = arr[i][k] - arr[i][j+1];
					cc++;
				}
			}
			cc = 0;
		}
		
		return res;
	}
	
	/// Make sign matrix
	private double[][] makeSign(double[][] arr) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][cols];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				res[i][j] = Math.signum(arr[i][j]);
			}
		}
		
		return res;
	}
	
	/// Make MU-A
	private double[][] makeA(double[][] arr, int method) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = sameElem(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
				else
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Make NU-B
	private double[][] makeB(double[][] arr, int method) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = diffElem(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = diffElem(arr[i], arr[j]);
				else
					res[i][j] = diffElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Same elements for MU-A:  0=0  -1=-1  1=1
	private double sameElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if (arrA[i] == arrB[i])
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Same elements for MU-A:  0=/=0  -1=-1  1=1
	private double sameElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == 1) && (arrB[i] == 1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Different elements for NU-B:  0=0  -1=/=1  1=/=-1
	private double diffElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Different elements for NU-B:  0=/=0  -1=/=1  1=/=-1
	private double diffElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) ||
				((arrA[i] == 0) && (arrB[i] == 0)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Make the plot data points
	private double[][] makePlotData(double[][] resA, double[][] resB) {
		
		int rows = resA.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		/// Then add the diagonal elements
		double[][] points = new double[((rows*rows)-rows)/2+rows][4];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix and diagonal elements
				if ( i <= j ) {
					points[cc][0] = resA[i][j]; // coordinate x
					points[cc][1] = resB[i][j]; // coordinate y
					points[cc][2] = i; // index row
					points[cc][3] = j; // index column
					cc++;
				}
			}
		}
		
		return points;
	}
	
	/// Make the vector data
	private double[][] makeVectorData(double[][] res) {
		
		int rows = res.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] points = new double[((rows*rows)-rows)/2][1];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix
				if ( i < j ) {
					points[cc][0] = res[i][j];
					cc++;
				}
			}
		}
		
		return points;
	}
	
	/// Mean value for two matrices - (A1{1,1} + A2{1,1})/2
	private double[][] matrixMeanValue(double[][] arr, double[][] arr2) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = (arr[i][j] + arr2[i][j])/2;
			}
		}
		
		return res;
	}
	
	/// Add two matrices - A1{1,1} + A2{1,1}
	private double[][] matrixAddition(double arr[][], double[][] arr2) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = arr[i][j] + arr2[i][j];
			}
		}
		
		return res;
	}
	
	/// Matrix for method Weighted
	private double[][] matrixWeighted(double arr[][], double[][] arrP) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (arrP[i][j] == 0)
					res[i][j] = 0.5;
				else
					res[i][j] = arr[i][j]/arrP[i][j];
			}
		}
		
		return res;
	}
	
	/// Display array
	private String showArray(double[][] arr, String separator, boolean locale) {
		
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				/// True for 1.7 and false for 1,7
				if (locale)
					res.append(arr[i][j]);
				else
					res.append(String.valueOf(arr[i][j]).replace(".", ","));
					//res += NumberFormat.getInstance(Locale.US).format(arr[i][j]) + separator;
				
				/// Do not display separator after last row element
				if (j != arr[i].length-1)
					res.append(separator);
				
			}
			res.append(ls);
		}
		
		return res.toString();
	}
	
	
	/// LINKS
	
	/// http://docs.oracle.com/javase/tutorial/2d/
	/// http://www.w3schools.com/colors/colors_groups.asp
	
	/// http://stackoverflow.com/questions/340209/generate-colors-between-red-and-green-for-a-power-meter
	/// http://www.javamex.com/tutorials/conversion/decimal_hexadecimal.shtml
	/// http://stackoverflow.com/questions/2303305/window-resize-event
	/// http://stackoverflow.com/questions/2106367/listen-to-jframe-resize-events-as-the-user-drags-their-mouse  super.validate()
	/// http://stackoverflow.com/questions/11375250/set-tooltip-text-at-a-particular-location
	
	/// http://stackoverflow.com/questions/5960799/java-glass-pane
	/// http://stackoverflow.com/questions/2561690/placing-component-on-glass-pane
	
	/// http://stackoverflow.com/questions/1155137/how-to-keep-a-single-column-from-being-reordered-in-a-jtable
	/// http://stackoverflow.com/questions/8776540/painting-over-the-top-of-components-in-swing
	
	/// http://stackoverflow.com/questions/5764467/get-component-from-a-jscrollpane
	/// http://stackoverflow.com/questions/8385856/how-to-get-the-component-inside-a-jscrollpane
	
	/// http://stackoverflow.com/questions/6575578/convert-a-graphics2d-to-an-image-or-bufferedimage#6600346
	/// http://stackoverflow.com/questions/8202253/saving-a-java-2d-graphics-image-as-png-file
	
	/// http://stackoverflow.com/questions/16707397/whats-wrong-with-this-simple-double-calculation
	/// http://stackoverflow.com/questions/179427/how-to-resolve-a-java-rounding-double-issue
	/// https://blogs.oracle.com/CoreJavaTechTips/entry/the_need_for_bigdecimal
	
	/// Undo-redo manager
	/// http://stackoverflow.com/questions/12772821/how-to-implement-undo-redo-in-java-for-mvc-model
	/// http://www.javaworld.com/article/2076698/core-java/add-an-undo-redo-function-to-your-java-apps-with-swing.html
	/// http://www.javadocexamples.com/javax/swing/Action/Action.ACCELERATOR_KEY.html
	/// http://www.javadocexamples.com/java_source/org/embl/ebi/escience/scuflworkers/rserv/RservConfigPanel.java.html
	
	/// change cursor on glass - http://www.javaspecialists.co.za/archive/Issue065.html
	/// unicode symbols - https://stackoverflow.com/questions/16616162/how-to-write-unicode-cross-symbol-in-java
	
	/// Mouse dragging
	/// http://stackoverflow.com/questions/10243257/java-scroll-image-by-mouse-dragging
	/// http://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html
	
	/// Scroll
	/// https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane
	/// https://stackoverflow.com/questions/13213030/jscrollpane-set-scroll-position
	
}

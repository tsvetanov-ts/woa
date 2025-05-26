/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 1.5
 * Date: June 7, 2019
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
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
import javax.swing.table.TableColumn;
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
	private String fs = System.getProperty("file.separator");
	/// JTextArea internally uses \n which is in conflict with line.separator
	/// http://docs.oracle.com/javase/8/docs/api/javax/swing/text/DefaultEditorKit.html
	private String ls = "\n"; //System.getProperty("line.separator");
	
	private String draftDir = ud + fs + "drafts";
	private SimpleDateFormat draftDate = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
	
	/// DecimalFormat - # is optional, while 0 is always written - ###.#### or 000.0000
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private Font monoFont2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private Font sansFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private Font sansFont2 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
	//private Color greenClr = new Color(0, 160, 0);
	//private Color violetClr = new Color(255, 128, 255);
	//private Color blueClr = new Color(64, 192, 255);
	private Color greenClr = new Color(0, 160, 0);
	private Color violetClr = new Color(255, 64, 255);
	private Color blueClr = new Color(0, 160, 255);
	
	private String MU = "\u03BC";
	private String NU = "\u03BD";
	private String AL = "\u03B1";
	private String BE = "\u03B2";
	
	/// Variables
	private HashMap<String, double[][]> result = null;
	private Point pp = new Point();
	private int markCol = -1;
	
	private MyTextArea textA;
	private JTextArea textB;
	private JScrollPane scrollA, scrollB, resScroll;
	private JComboBox<String> comboType, comboAggr, comboMethod, comboSize, comboGrid, comboExport;
	private JCheckBox chkRowNames, chkColNames, chkTranspose, chkOrdPair, chkColor, chkView;
	private JSpinner spinMatCount, spinAlpha, spinBeta, spinPoints;
	
	private String[] typeNames = new String[] {"Standard ICrA", "Second Order ICrA", "Aggregated ICrA"};
	private String[] aggrNames = new String[] {"Average", "MaxMin", "MinMax"};
	private String[] methodNames = new String[] {MU + "-biased", "Unbiased", NU + "-biased", "Balanced", "Weighted"};
	private String[] sizeDim = new String[] {"400", "600", "800", "1000", "2000", "3000", "5000"};
	private int valSize = 400; /// Plot size in pixels
	private int pointSize = 5; /// Point size of the plot
	
	/// Headers
	private String rowNames = "";
	private String colNames = "";
	private String[][] headerNames = new String[][] {null, null, null, null};
	
	/// Glass points, double[] saves X, Y, width, height of the rectangle
	private Vector<double[]> glassA = new Vector<double[]>();
	private Vector<double[]> glassB = new Vector<double[]>();
	private MyGlass glass;
	
	
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
				sansFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
				sansFont2 = new Font(Font.SANS_SERIF, Font.BOLD, 18);
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
		Dimension dim70 = new Dimension(73, 25);
		Dimension dim100 = new Dimension(100, 25);
		Dimension dim150 = new Dimension(151, 25);
		Dimension scrollSize = new Dimension(500, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
		
		/// Items for panel 1
		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.setToolTipText("Open a file.");
		btnOpenFile.setPreferredSize(dim150);
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenFileListener(evt);
			}
		});
		
		JButton btnSaveFile = new JButton("Save File");
		btnSaveFile.setToolTipText("Save the text in the input panel by choosing a file name.");
		btnSaveFile.setPreferredSize(dim150);
		btnSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveFileListener(evt);
			}
		});
		
		JButton btnSaveDraft = new JButton("Save Draft");
		btnSaveDraft.setToolTipText("Save draft in subdirectory \"drafts\".");
		btnSaveDraft.setPreferredSize(dim150);
		btnSaveDraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveDraftListener(evt);
			}
		});
		
		comboType = new JComboBox<String>(typeNames);
		comboType.setToolTipText("Select an ICrA type for the input data.");
		comboType.setPreferredSize(dim150);
		comboType.setFont(monoFont);
		comboType.addItemListener(new ComboTypeListener());
		
		comboAggr = new JComboBox<String>(aggrNames);
		comboAggr.setToolTipText("Select aggregation type.");
		comboAggr.setPreferredSize(dim150);
		comboAggr.setFont(monoFont);
		comboAggr.setVisible(false);
		
		spinMatCount = new JSpinner(new SpinnerNumberModel(3,3,10000,1));
		spinMatCount.setToolTipText("Select the number of matrices. Minimum of 3 matrices.");
		spinMatCount.setPreferredSize(dim70);
		spinMatCount.setFont(monoFont);
		spinMatCount.setVisible(false);
		
		chkRowNames = new JCheckBox("Row Names");
		chkRowNames.setToolTipText("Row names are in the first column.");
		chkRowNames.setBackground(Color.WHITE);
		chkRowNames.setFont(monoFont);
		
		chkColNames = new JCheckBox("Column Names");
		chkColNames.setToolTipText("Column names are in the first row.");
		chkColNames.setBackground(Color.WHITE);
		chkColNames.setFont(monoFont);
		
		chkTranspose = new JCheckBox("Transpose");
		chkTranspose.setToolTipText("Transpose each matrix independently.");
		chkTranspose.setBackground(Color.WHITE);
		chkTranspose.setFont(monoFont);
		
		chkOrdPair = new JCheckBox("Ordered Pair");
		chkOrdPair.setToolTipText("<html>Input two sets of data to load as <b>ordered pair (" + MU + "," + NU + ")</b>. " +
				"Data after <b>#input1</b> is for " + MU + ", data after <b>#input2</b> is for " + NU + ".<br/>" +
				"When using Second Order / Aggregated, input two data sets of at least three matrices each.</html>");
		chkOrdPair.setBackground(Color.WHITE);
		chkOrdPair.setFont(monoFont);
		
		/// Items for panel 2
		comboMethod = new JComboBox<String>(methodNames);
		comboMethod.setToolTipText("Select a method for the calculations.");
		comboMethod.setPreferredSize(dim150);
		comboMethod.setFont(monoFont);
		
		spinAlpha = new JSpinner(new SpinnerNumberModel(0.75,0.5,1.0,0.01));
		spinAlpha.setToolTipText("<html>Value <b>" + AL + "</b> from 0.5 to 1 with 0.01 increment.<br/>" +
				"Affects the colors. See Help or documentation</html>.");
		spinAlpha.setPreferredSize(dim70);
		spinAlpha.setFont(monoFont);
		spinAlpha.addChangeListener(new SpinAlphaListener());
		
		spinBeta = new JSpinner(new SpinnerNumberModel(0.25,0.0,0.5,0.01));
		spinBeta.setToolTipText("<html>Value <b>" + BE + "</b> from 0 to 0.5 with 0.01 increment.<br/>" +
				"Affects the colors. See Help or documentation.</html>");
		spinBeta.setPreferredSize(dim70);
		spinBeta.setFont(monoFont);
		spinBeta.addChangeListener(new SpinBetaListener());
		
		JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,20,1));
		spinDigits.setToolTipText("Select the number of digits after the decimal separator.");
		spinDigits.setPreferredSize(dim70);
		spinDigits.setFont(monoFont);
		spinDigits.addChangeListener(new SpinDigitsListener());
		
		chkColor = new JCheckBox("C");
		chkColor.setToolTipText("Switch table colors to RGB.");
		chkColor.setBackground(Color.WHITE);
		chkColor.setFont(monoFont);
		chkColor.addActionListener(new CheckSwitchListener());
		
		chkView = new JCheckBox("S");
		chkView.setToolTipText("Switch to secondary table view.");
		chkView.setBackground(Color.WHITE);
		chkView.setFont(monoFont);
		chkView.addActionListener(new CheckSwitchListener());
		
		JButton btnAnalysis = new JButton("Analysis");
		btnAnalysis.setToolTipText("Make the calculations and display them.");
		btnAnalysis.setPreferredSize(dim150);
		btnAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAnalysisListener(evt);
			}
		});
		
		JButton btnViewData = new JButton("View Data");
		btnViewData.setToolTipText("View the matrix data.");
		btnViewData.setPreferredSize(dim150);
		btnViewData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnViewDataListener(evt);
			}
		});
		
		JButton btnAbout = new JButton("About");
		btnAbout.setToolTipText("About the program.");
		btnAbout.setPreferredSize(dim150);
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAboutListener(evt);
			}
		});
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setToolTipText("Help information for the program.");
		btnHelp.setPreferredSize(dim70);
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnHelpListener(evt);
			}
		});
		
		/// Items for panel 3
		comboSize = new JComboBox<String>(sizeDim);
		comboSize.setToolTipText("Select the plot size in pixels or input a value from 100 to 100000.");
		comboSize.setPreferredSize(dim70);
		comboSize.setFont(monoFont);
		comboSize.setEditable(true);
		comboSize.addItemListener(new ComboSizeListener());
		
		spinPoints = new JSpinner(new SpinnerNumberModel(5,1,20,1));
		spinPoints.setToolTipText("Select the size of the plot points.");
		spinPoints.setPreferredSize(dim70);
		spinPoints.setFont(monoFont);
		spinPoints.addChangeListener(new SpinPointsListener());
		
		comboGrid = new JComboBox<String>(new String[] {"Triangle", "Ticks", "Grid", "Text", "Text+Grid"});
		comboGrid.setToolTipText("Select the plot type.");
		comboGrid.setPreferredSize(dim100);
		comboGrid.setFont(monoFont);
		comboGrid.addItemListener(new ComboGridListener());
		
		comboExport = new JComboBox<String>(new String[] {"Export", "Screen", "PNG", "TeX"});
		comboExport.setToolTipText("Export the screen as image or the graphic as PNG/TeX.");
		comboExport.setPreferredSize(dim100);
		comboExport.setFont(monoFont);
		comboExport.addItemListener(new ComboExportListener());
		
		/// Panel 1A
		JPanel panel1a = new JPanel();
		panel1a.setBackground(Color.WHITE);
		panel1a.setLayout(flowLayout);
		panel1a.add(btnOpenFile);
		panel1a.add(btnSaveFile);
		panel1a.add(btnSaveDraft);
		
		/// Panel 1B
		JPanel panel1b = new JPanel();
		panel1b.setBackground(Color.WHITE);
		panel1b.setLayout(flowLayout);
		panel1b.add(comboType);
		panel1b.add(comboAggr);
		panel1b.add(spinMatCount);
		
		/// Panel 1C
		JPanel panel1c = new JPanel();
		panel1c.setBackground(Color.WHITE);
		panel1c.setLayout(flowLayout);
		panel1c.add(chkRowNames);
		panel1c.add(chkColNames);
		panel1c.add(chkTranspose);
		panel1c.add(chkOrdPair);
		
		/// Panel 1
		JPanel panel1 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel1.setLayout(new GridLayout(3,0));
		panel1.add(panel1a);
		panel1.add(panel1b);
		panel1.add(panel1c);
		
		/// Panel 2A
		JPanel panel2a = new JPanel();
		panel2a.setBackground(Color.WHITE);
		panel2a.setLayout(flowLayout);
		panel2a.add(comboMethod);
		panel2a.add(spinAlpha);
		panel2a.add(spinBeta);
		panel2a.add(spinDigits);
		panel2a.add(btnHelp);
		panel2a.add(chkColor);
		
		/// Panel 2B
		JPanel panel2b = new JPanel();
		panel2b.setBackground(Color.WHITE);
		panel2b.setLayout(flowLayout);
		panel2b.add(btnAnalysis);
		panel2b.add(btnViewData);
		panel2b.add(btnAbout);
		panel2b.add(chkView);
		
		/// Panel 2
		JPanel panel2 = new JPanel();
		panel2.setBackground(Color.WHITE);
		panel2.setLayout(new GridLayout(2,0));
		panel2.add(panel2a);
		panel2.add(panel2b);
		
		/// Panel 3
		JPanel panel3 = new JPanel();
		panel3.setBackground(Color.WHITE);
		panel3.setLayout(flowLayout);
		panel3.add(comboSize);
		panel3.add(spinPoints);
		panel3.add(comboGrid);
		panel3.add(comboExport);
		
		
		/// Items for text areas
		textA = new MyTextArea("# Open file or copy/paste data here" + ls +
				"# Column separators: tab semicolon comma" + ls +
				"# Recognized numbers: 1.7 and 1,7" + ls + ls +
				"#rownames: A1, A2, A3, A4" + ls +
				"#colnames: B1, B2, B3, B4, B5" + ls + ls +
				"6;5;3;7;6" + ls +
				"7;7;8;1;3" + ls +
				"4;3;5;9;1" + ls +
				"4;5;6;7;8" + ls + ls);
		
		textB = new JTextArea();
		textB.setFont(monoFont2);
		textB.setEditable(false);
		
		JScrollPane scrollTextA = new JScrollPane(textA);
		scrollTextA.setPreferredSize(new Dimension(450, 460));
		scrollTextA.setBorder(null);
		
		JScrollPane scrollTextB = new JScrollPane(textB);
		scrollTextB.setPreferredSize(new Dimension(450, 100));
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
		resScroll.setPreferredSize(new Dimension(400, 400));
		resScroll.getViewport().setBackground(Color.WHITE);
		resScroll.setBorder(null);
		
		/// Panel text areas
		JPanel panelAreas = new JPanel();
		panelAreas.setLayout(new BorderLayout());
		panelAreas.add(panel1, BorderLayout.NORTH);
		panelAreas.add(scrollTextA, BorderLayout.CENTER);
		
		/// Split text areas
		JSplitPane splitAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panelAreas, scrollTextB);
		splitAreas.setOneTouchExpandable(true);
		splitAreas.setResizeWeight(1.0);
		splitAreas.setDividerSize(10);
		
		/// Split tables
		JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollA, scrollB);
		splitTables.setOneTouchExpandable(true);
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
		JSplitPane splitLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitAreas, panelTables);
		splitLeft.setOneTouchExpandable(true);
		splitLeft.setResizeWeight(0.0);
		splitLeft.setDividerSize(10);
		
		/// Split center-right
		JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitLeft, panelGraphic);
		splitRight.setOneTouchExpandable(true);
		splitRight.setResizeWeight(1.0);
		splitRight.setDividerSize(10);
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(splitRight, BorderLayout.CENTER);
		
		setTitle("ICrAData v1.5");
		setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitListener(evt);
			}
		});
		
		/// Glass pane
		glass = new MyGlass(this.getContentPane());
		setGlassPane(glass);
		
		/// Thread for saving drafts
		(new Thread(new MyThread())).start();
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		//setResizable(false);
		pack();
		setLocationByPlatform(true);
		//setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}
	
	/// Show custom message and append to lower text area
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
		if (JOptionPane.showConfirmDialog(
				this, "Exit the program?", "Exit",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
				== JOptionPane.YES_OPTION) {
			btnSaveDraftListener(null);
			System.exit(0);
		}
	}
	
	/// Open file
	private void btnOpenFileListener(ActionEvent evt) {
		
		JFileChooser chOpen = new JFileChooser(ud);
		chOpen.setMultiSelectionEnabled(false);
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated values (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File openFile = chOpen.getSelectedFile();
		ud = openFile.getParent();
		
		try {
			String str = "";
			String line = "";
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(openFile), "UTF8"));
			while ((line = br.readLine()) != null) {
				str += line + ls;
				
				/// Load values for parameters
				if (line.startsWith("#type:") && line.length() > 6 && checkInt(line.substring(6).trim())) {
					int val = Integer.parseInt(line.substring(6).trim());
					if (val >= 0 && val <= 2)
						comboType.setSelectedIndex(val);
				}
				if (line.startsWith("#count:") && line.length() > 7 && checkInt(line.substring(7).trim())) {
					int val = Integer.parseInt(line.substring(7).trim());
					if (val >= 3 && val <= 10000)
						spinMatCount.setValue(val);
				}
				if (line.startsWith("#chrow:") && line.length() > 7 && checkInt(line.substring(7).trim())) {
					if (Integer.parseInt(line.substring(7).trim()) == 1)
						chkRowNames.setSelected(true);
					else
						chkRowNames.setSelected(false);
				}
				if (line.startsWith("#chcol:") && line.length() > 7 && checkInt(line.substring(7).trim())) {
					if (Integer.parseInt(line.substring(7).trim()) == 1)
						chkColNames.setSelected(true);
					else
						chkColNames.setSelected(false);
				}
				if (line.startsWith("#chtr:") && line.length() > 6 && checkInt(line.substring(6).trim())) {
					if (Integer.parseInt(line.substring(6).trim()) == 1)
						chkTranspose.setSelected(true);
					else
						chkTranspose.setSelected(false);
				}
				if (line.startsWith("#chord:") && line.length() > 7 && checkInt(line.substring(7).trim())) {
					if (Integer.parseInt(line.substring(7).trim()) == 1)
						chkOrdPair.setSelected(true);
					else
						chkOrdPair.setSelected(false);
				}
			}
			br.close();
			
			textA.setText(str);
			showMessage("info", "Opened file " + openFile.getName() + " (" + openFile + ")");
			
		} catch (Exception ex) {
			showMessage("error", "Could not open file " + openFile.getName() + " (" + openFile + ")");
		}
	}
	
	/// Save file
	private void btnSaveFileListener(ActionEvent evt) {
		
		if (textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			ud = saveFile.getParent();
			if (!saveFile.toString().toLowerCase().endsWith(".txt"))
				saveFile = new File(chSave.getSelectedFile() + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(saveFile, false), "UTF8"));
				bw.write(saveText());
				bw.close();
				showMessage("info", "Saved file " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save file " + saveFile.getName() + " (" + saveFile + ")");
			}
		} else
			showMessage("info", "No text in the input panel");
	}
	
	/// Save draft
	private void btnSaveDraftListener(ActionEvent evt) {
		
		if (textA.getText().length() > 0) {
			(new File(draftDir)).mkdir();
			File saveFile = new File(draftDir + fs + "ICrAData-" + draftDate.format(new Date()) + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(saveFile, false), "UTF8"));
				bw.write(saveText());
				bw.close();
				showMessage("info", "Saved draft " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save draft " + saveFile.getName() + " (" + saveFile + ")");
			}
			
		} else
			showMessage("info", "No text in the input panel");
	}
	
	/// Save the text with parameters
	private String saveText() {
		
		/// Get the text
		String[] line = textA.getText().split(ls);
		int vType = comboType.getSelectedIndex();
		int vCount = (int)spinMatCount.getValue();
		int vRow = (chkRowNames.isSelected() ? 1 : 0);
		int vCol = (chkColNames.isSelected() ? 1 : 0);
		int vTr = (chkTranspose.isSelected() ? 1 : 0);
		int vOrd = (chkOrdPair.isSelected() ? 1 : 0);
		
		/// Find what is in the text
		boolean markDate = false;
		boolean markType = false;
		boolean markCount = false;
		boolean markRow = false;
		boolean markCol = false;
		boolean markTr = false;
		boolean markOrd = false;
		
		for (int i = 0; i < line.length; i++) {
			if (line[i].startsWith("#ICrAData")) {
				line[i] = "#ICrAData " + logDate.format(new Date());
				markDate = true;
			}
			if (line[i].startsWith("#type")) {
				line[i] = "#type: " + vType;
				markType = true;
			}
			if (line[i].startsWith("#count:") && vType > 0) {
				line[i] = "#count: " + vCount;
				markCount = true;
			}
			if (line[i].startsWith("#chrow")) {
				line[i] = "#chrow: " + vRow;
				markRow = true;
			}
			if (line[i].startsWith("#chcol")) {
				line[i] = "#chcol: " + vCol;
				markCol = true;
			}
			if (line[i].startsWith("#chtr")) {
				line[i] = "#chtr: " + vTr;
				markTr = true;
			}
			if (line[i].startsWith("#chord")) {
				line[i] = "#chord: " + vOrd;
				markOrd = true;
			}
		}
		
		/// Return as string and replace \n with \r\n
		String strData = (markDate ? "" : "#ICrAData " + logDate.format(new Date()) + "\r\n") +
				(markType ? "" : "#type: " + vType + "\r\n") +
				(!markCount && vType > 0  ? "#count: " + vCount + "\r\n" : "") +
				(markRow ? "" : "#chrow: " + vRow + "\r\n") +
				(markCol ? "" : "#chcol: " + vCol + "\r\n") +
				(markTr ? "" : "#chtr: " + vTr + "\r\n") +
				(markOrd ? "" : "#chord: " + vOrd + "\r\n");
		for (int i = 0; i < line.length; i++)
			strData += line[i] + "\r\n";
		
		return strData;
	}
	
	/// View matrix data
	private void btnViewDataListener(ActionEvent evt) {
		
		try {
			if (result != null) {
				
				/// Make a local copy of the hash map
				final HashMap<String, double[][]> hmap = new HashMap<String, double[][]>();
				Iterator<String> it = result.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					hmap.put(key, result.get(key));
				}
				
				/// Array with all keys from the hash map
				String[] arrKeys = new String[hmap.size()];
				it = hmap.keySet().iterator();
				int hsize = 0;
				while (it.hasNext())
					arrKeys[hsize++] = it.next();
				
				/// Sort the array
				Arrays.sort(arrKeys);
				
				/// Display index
				int cind = 0;
				for (int i = 0; i < arrKeys.length; i++)
					if (arrKeys[i].equals("MatrixMU"))
						cind = i;
				
				/// Make a local copy of the headers
				final String[][] hdrs = new String[][] {null, null, null, null};
				if (headerNames[0] != null) {
					hdrs[0] = new String[headerNames[0].length];
					for (int j = 0; j < headerNames[0].length; j++)
						hdrs[0][j] = headerNames[0][j];
				}
				if (headerNames[1] != null) {
					hdrs[1] = new String[headerNames[1].length];
					for (int j = 0; j < headerNames[1].length; j++)
						hdrs[1][j] = headerNames[1][j];
				}
				if (headerNames[2] != null) {
					hdrs[2] = new String[headerNames[2].length];
					for (int j = 0; j < headerNames[2].length; j++)
						hdrs[2][j] = headerNames[2][j];
				}
				if (headerNames[3] != null) {
					hdrs[3] = new String[headerNames[3].length];
					for (int j = 0; j < headerNames[3].length; j++)
						hdrs[3][j] = headerNames[3][j];
				}
				
				/// Separator \t ; , &
				final String[] separator = new String[] {"\t", ";", ",", " & "};
				/// Locale
				final Locale[] locale = new Locale[] {Locale.US, new Locale("bg", "BG")};
				
				Dimension dim70 = new Dimension(70, 25);
				Dimension dim125 = new Dimension(125, 25);
				Dimension dim150 = new Dimension(150, 25);
				final JFrame viewFrame = new JFrame();
				
				/// Text area
				final MyTextArea text = new MyTextArea(
						viewArray(hmap.get("MatrixMU"), "MatrixMU", separator[0], locale[0], 4, hdrs, true));
				
				JScrollPane scrollText = new JScrollPane(text);
				scrollText.setPreferredSize(new Dimension(900, 500));
				
				/// Panel items
				final JComboBox<String> comboData = new JComboBox<String>(arrKeys);
				comboData.setToolTipText("Select a matrix for display.");
				comboData.setPreferredSize(dim150);
				comboData.setFont(monoFont);
				comboData.setSelectedIndex(cind);
				comboData.setMaximumRowCount(13);
				
				final JComboBox<String> comboSeparator = new JComboBox<String>(
						new String[] {"Tab \\t", "Semicolon ;", "Comma ,", "TeX &"});
				comboSeparator.setToolTipText("Select the column separator.");
				comboSeparator.setPreferredSize(dim125);
				comboSeparator.setFont(monoFont);
				
				final JComboBox<String> comboLocale = new JComboBox<String>(
						new String[] {"Point", "Comma"});
				comboLocale.setToolTipText("Select the decimal separator.");
				comboLocale.setPreferredSize(dim125);
				comboLocale.setFont(monoFont);
				
				final JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,20,1));
				spinDigits.setToolTipText("Select the number of digits after the decimal separator.");
				spinDigits.setPreferredSize(dim70);
				spinDigits.setFont(monoFont);
				
				final JCheckBox chkHeader = new JCheckBox("Headers");
				chkHeader.setToolTipText("Show the table headers.");
				chkHeader.setBackground(Color.WHITE);
				chkHeader.setFont(monoFont);
				chkHeader.setSelected(true);
				
				/// Listeners
				comboData.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							text.setText(viewArray(hmap.get((String)comboData.getSelectedItem()),
								(String)comboData.getSelectedItem(),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								hdrs,
								chkHeader.isSelected()));
						}
					}
				});
				
				comboSeparator.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							text.setText(viewArray(hmap.get((String)comboData.getSelectedItem()),
								(String)comboData.getSelectedItem(),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								hdrs,
								chkHeader.isSelected()));
						}
					}
				});
				
				comboLocale.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							text.setText(viewArray(hmap.get((String)comboData.getSelectedItem()),
								(String)comboData.getSelectedItem(),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								hdrs,
								chkHeader.isSelected()));
						}
					}
				});
				
				spinDigits.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent evt) {
						text.setText(viewArray(hmap.get((String)comboData.getSelectedItem()),
							(String)comboData.getSelectedItem(),
							separator[comboSeparator.getSelectedIndex()],
							locale[comboLocale.getSelectedIndex()],
							(int)spinDigits.getValue(),
							hdrs,
							chkHeader.isSelected()));
					}
				});
				
				chkHeader.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						text.setText(viewArray(hmap.get((String)comboData.getSelectedItem()),
							(String)comboData.getSelectedItem(),
							separator[comboSeparator.getSelectedIndex()],
							locale[comboLocale.getSelectedIndex()],
							(int)spinDigits.getValue(),
							hdrs,
							chkHeader.isSelected()));
					}
				});
				
				JButton btnSave = new JButton("Save");
				btnSave.setPreferredSize(dim125);
				btnSave.setToolTipText("Save the matrix.");
				btnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JFileChooser chSave = new JFileChooser(ud);
						chSave.setMultiSelectionEnabled(false);
						chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
						
						if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
							return;
						
						File saveFile = chSave.getSelectedFile();
						ud = saveFile.getParent();
						if (!saveFile.toString().toLowerCase().endsWith(".txt"))
							saveFile = new File(chSave.getSelectedFile() + ".txt");
						
						try {
							BufferedWriter bw = new BufferedWriter(
									new OutputStreamWriter(new FileOutputStream(saveFile, false), "UTF8"));
							bw.write(text.getText().replace(ls, "\r\n"));
							bw.close();
							showMessage("info", "Saved matrix " + saveFile.getName() + " (" + saveFile + ")");
							
						} catch (Exception ex) {
							showMessage("error", "Could not save matrix " + saveFile.getName() + " (" + saveFile + ")");
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
				
				/// Panel for items
				JPanel panelBtn = new JPanel();
				panelBtn.setBackground(Color.WHITE);
				panelBtn.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
				panelBtn.add(comboData);
				panelBtn.add(comboSeparator);
				panelBtn.add(comboLocale);
				panelBtn.add(spinDigits);
				panelBtn.add(chkHeader);
				panelBtn.add(btnSave);
				panelBtn.add(btnClose);
				
				/// Frame options
				viewFrame.setTitle(typeNames[comboType.getSelectedIndex()] + " - " +
						(comboType.getSelectedIndex() == 2 ? aggrNames[comboAggr.getSelectedIndex()] + " - " : "") +
						methodNames[comboMethod.getSelectedIndex()]);
				viewFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
				
				viewFrame.setLayout(new BorderLayout());
				viewFrame.add(panelBtn, BorderLayout.NORTH);
				viewFrame.add(scrollText, BorderLayout.CENTER);
				
				viewFrame.pack();
				viewFrame.setLocationByPlatform(true);
				viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				viewFrame.setVisible(true);
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not view the internal data");
		}
	}
	
	/// Display array - array, array id, separator, locale, digits, headers, show header
	private String viewArray(double[][] arr, String arrid, String separator, Locale locale, int digits,
			String[][] hdrs, boolean showh) {
		
		/// Determine headers
		String[] rowh = null;
		String[] colh = null;
		
		if (arrid.contains("MatrixMU") || arrid.contains("MatrixNU") || arrid.contains("MatrixDist")) {
			rowh = (arrid.startsWith("Y") ? hdrs[2] : hdrs[0]);
			colh = (arrid.startsWith("Y") ? hdrs[2] : hdrs[0]);
			
		} else if (arrid.contains("Input1") || arrid.contains("Input2")) {
			rowh = (arrid.startsWith("Y") ? hdrs[2] : hdrs[0]);
			colh = (arrid.startsWith("Y") ? hdrs[3] : hdrs[1]);
			
		} else if (arrid.contains("PlotPoints")) {
			rowh = new String[arr.length];
			for (int i = 0; i < arr.length; i++)
				rowh[i] = String.valueOf(i+1);
			colh = new String[] {MU, NU, "Row", "Column", "Distance"};
			
		} else if (arrid.contains("CriteriaMatrix") || arrid.contains("SignMatrix")) {
			rowh = (arrid.startsWith("Y") ? hdrs[2] : hdrs[0]);
			
			String[] cols = (arrid.startsWith("Y") ? hdrs[3] : hdrs[1]);
			colh = new String[(cols.length*(cols.length-1))/2];
			int cc = 0;
			for (int k = 0; k < cols.length-1; k++)
				for (int j = k; j < cols.length-1; j++)
					colh[cc++] = cols[k] + "-" + cols[j+1];
			
		} else if (arrid.contains("Vector")) {
			String[] rows = (arrid.startsWith("Y") ? hdrs[2] : hdrs[0]);
			rowh = new String[arr.length];
			for (int i = 0; i < rowh.length; i++)
				rowh[i] = rows[(int)arr[i][1]] + "-" + rows[(int)arr[i][2]];
			
			colh = new String[] {(arrid.contains("MU") ? MU : (arrid.contains("NU") ? NU : "Distance")), "Row", "Column"};
		}
		
		/// Make 0.0000, must be with dot, not with comma
		String strDig = "0.";
		for (int i = 0; i < digits; i++)
			strDig += "0";
		
		/// DecimalFormat usFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
		/// DecimalFormat bgFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(new Locale("bg", "BG")));
		DecimalFormat decFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(locale));
		
		String sl = ls;
		if (separator == " & ")
			sl = " \\\\" + ls;
		
		/// StringBuilder is created much faster than String
		StringBuilder res = new StringBuilder();
		
		/// Column header
		if (showh) {
			res.append(" " + separator);
			
			if (colh == null) {
				for (int j = 0; j < arr[0].length; j++) {
					res.append("?");
					if (j != arr[0].length-1)
						res.append(separator);
				}
				
			} else {
				for (int j = 0; j < colh.length; j++) {
					res.append(colh[j]);
					if (j != colh.length-1)
						res.append(separator);
				}
			}
			res.append(sl);
		}
		
		/// Table data
		for (int i = 0; i < arr.length; i++) {
			if (showh)
				res.append((rowh == null ? "?" : rowh[i]) + separator);
			
			for (int j = 0; j < arr[i].length; j++) {
				res.append(decFormat.format(arr[i][j]));
				if (j != arr[i].length-1)
					res.append(separator);
			}
			res.append(sl);
		}
		
		return res.toString();
	}
	
	/// About the program
	private void btnAboutListener(ActionEvent evt) {
		
		JFrame aboutFrame = new JFrame();
		MyTextArea text = new MyTextArea(
				"InterCriteria Analysis proposed for the first time by this article:" + ls +
				"  Atanassov K., D. Mavrov, V. Atanassova," + ls +
				"  Intercriteria Decision Making: A New Approach for Multicriteria Decision Making," + ls +
				"    Based on Index Matrices and Intuitionistic Fuzzy Sets," + ls +
				"  Issues in Intuitionistic Fuzzy Sets and Generalized Nets, Vol. 11, 2014, 1-8." + ls + ls +
				"Main paper for the software application:" + ls +
				"  Ikonomov N., P. Vassilev, O. Roeva," + ls +
				"  ICrAData – Software for InterCriteria Analysis," + ls +
				"  International Journal Bioautomation, Vol. 22(1), 2018, 1-10." + ls + ls +
				"This software application has been developed with the partial financial support of:" + ls +
				"  Project DFNI-I-02-5 InterCriteria Analysis: A New Approach to Decision Making," + ls +
				"    funded by the National Science Fund of Bulgaria, 2014-2018" + ls +
				"  Project DN 17/06 A New Approach, Based on an Intercriteria Data Analysis," + ls +
				"    to Support Decision Making in 'in silico' Studies of Complex Biomolecular Systems," + ls +
				"    funded by the National Science Fund of Bulgaria, 2017-present" + ls + ls + ls +
				"InterCriteria Analysis Data" + ls +
				"  Version: 1.5" + ls +
				"  Date: June 7, 2019" + ls +
				"  Compiled by: Eclipse Indigo SR2" + ls + ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		aboutFrame.setTitle("About");
		aboutFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		aboutFrame.add(scrollText);
		
		aboutFrame.pack();
		aboutFrame.setLocationByPlatform(true);
		aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutFrame.setVisible(true);
	}
	
	/// Help information
	private void btnHelpListener(ActionEvent evt) {
		
		JFrame helpFrame = new JFrame();
		MyTextArea text = new MyTextArea(
				"InterCriteria Analysis Data" + ls + ls +
				">>> Left panel" + ls +
				"Open text file or comma separated values file." + ls +
				"Open MS Excel/LibreOffice Calc and copy/paste the table with optional headers in the panel." + ls +
				"Select Row and Column names if header was copied, or type after #rownames and #colnames." + ls +
				"Markers #input1 and #input2 are required for Ordered Pair." + ls + ls +
				">>> Center panel" + ls +
				"Value " + AL + " is from 0.5 to 1, value " + BE + " is from 0 to 0.5, both with 0.01 increment." + ls +
				"When " + MU + " > " + AL + " and " + NU + " < " + BE + ", that is Positive Consonance, color is green." + ls +
				"When " + MU + " < " + BE + " and " + NU + " > " + AL + ", that is Negative Consonance, color is light blue." + ls +
				"In all other cases, that is Dissonance, color is violet." + ls + ls +
				">>> Right panel" + ls +
				"Screenshot of the program can be created from Export -> Screen." + ls +
				"Export -> PNG/TeX saves the graphic from the panel in the respective format." + ls + ls +
				"Use Java 64-bit on Windows 64-bit. Check from Control Panel -> Java." + ls +
				"Download from: https://java.com/en/download/manual.jsp" + ls + ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		helpFrame.setTitle("Help");
		helpFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		helpFrame.add(scrollText);
		
		helpFrame.pack();
		helpFrame.setLocationByPlatform(true);
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		helpFrame.setVisible(true);
	}
	
	/// Export the screen
	private void comboExportScreen() {
		
		JFileChooser chSave = new JFileChooser(ud);
		chSave.setMultiSelectionEnabled(false);
		chSave.setAcceptAllFileFilterUsed(false);
		chSave.setFileFilter(new FileNameExtensionFilter("Screen (*.png)", "png"));
		
		if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		
		File chName = chSave.getSelectedFile();
		ud = chName.getParent();
		if (!chName.toString().toLowerCase().endsWith(".png"))
			chName = new File(chName.toString() + ".png");
		
		try {
			BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			this.printAll(g2);
			g2.dispose();
			
			/// Windows 10 has invisible 7px border, only 1px is visible, affects the Robot()
			//BufferedImage img = new Robot().createScreenCapture(new Rectangle(
			//		this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight()));
			
			/// Works sometimes, not reliable enough
			//Robot robot = new Robot();
			//robot.keyPress(KeyEvent.VK_ALT);
			//robot.keyPress(KeyEvent.VK_PRINTSCREEN);
			//robot.keyRelease(KeyEvent.VK_PRINTSCREEN);
			//robot.keyRelease(KeyEvent.VK_ALT);
			//BufferedImage img = (BufferedImage)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.imageFlavor);
			
			ImageIO.write(img, "png", chName.getAbsoluteFile());
			showMessage("info", "Saved screen " + chName.getName() + " (" + chName + ")");
			
		} catch (Exception ex) {
			showMessage("error", "Could not save screen " + chName.getName());
		}
	}
	
	/// Export the graphic as PNG image
	private void comboExportPNG() {
		
		if (result != null) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setAcceptAllFileFilterUsed(false);
			chSave.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			ud = chName.getParent();
			if (!chName.toString().toLowerCase().endsWith(".png"))
				chName = new File(chName.toString() + ".png");
			
			try {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				
				BufferedImage img = new BufferedImage(resPanel.getWidth(), resPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = img.createGraphics();
				resPanel.printAll(g2);
				g2.dispose();
				
				//Graphics g = img.getGraphics();
				//resPanel.printAll(g);
				
				//Graphics2D g2 = (Graphics2D)img.getGraphics();
				//resPanel.printAll(g2);
				//resPanel.paintAll(g2);
				
				//FileOutputStream fos = new FileOutputStream(ud + "test.jpg");
				//JPEGImageEncoderImpl jpeg = new JPEGImageEncoderImpl(fos);
				
				ImageIO.write(img, "png", chName.getAbsoluteFile());
				showMessage("info", "Saved PNG image " + chName.getName() + " (" + chName + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save PNG image " + chName.getName());
			}
		}
	}
	
	/// Export the graphic as TeX file
	private void comboExportTeX() {
		
		if (result != null) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setAcceptAllFileFilterUsed(false);
			chSave.setFileFilter(new FileNameExtensionFilter("TeX file (*.tex)", "tex"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			ud = chName.getParent();
			if (!chName.toString().toLowerCase().endsWith(".tex"))
				chName = new File(chName.toString() + ".tex");
			
			try {
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(chName, false), "UTF8"));
				bw.write(exportTeX(result.get("PlotPoints")).replace(ls, "\r\n"));
				bw.close();
				showMessage("info", "Saved TeX file " + chName.getName() + " (" + chName + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save TeX file " + chName.getName() + " (" + chName + ")");
			}
		}
	}
	
	/// Export to TeX
	private String exportTeX(double[][] arrPoints) {
		
		/// Triangle 0  Ticks 1  Grid 2  Text 3
		int vGrid = comboGrid.getSelectedIndex();
		StringBuilder res = new StringBuilder();
		
		/// picture \makebox(0,0)[cc]{$Z_1$}   c r l b t
		/// http://www.emerson.emory.edu/services/latex/latex_51.html
		/// https://tex.stackexchange.com/questions/32791/picture-environment-rotating-text
		res.append("%%% ICrAData TeX Export " + logDate.format(new Date()) + ls +
			"\\documentclass[11pt]{article}" + ls +
			(vGrid >= 3 ? "\\usepackage{graphicx}" + ls : "") + 
			"\\begin{document}" + ls +
			"\\thispagestyle{empty}" + ls + ls +
			"%%% Change unitlength and font size to scale the graphic" + ls +
			"%%% Font sizes: \\tiny \\scriptsize \\footnotesize \\small \\normalsize \\large \\Large \\LARGE \\huge \\Huge" + ls +
			"\\begin{center}" + ls +
			(vGrid >= 3 ? "\\newcommand{\\myticks}{\\scriptsize}" + ls +
				"\\newcommand{\\mytext}{\\normalsize}" + ls : "") +
			"\\setlength{\\unitlength}{20pt} %10pt=4mm" + ls +
			"\\linethickness{0.5pt}" + ls +
			"\\begin{picture}" +
			(vGrid >= 3 ? "(11.5,11.5)(-1.5,-1.5)" + ls : "(10,10)" + ls) +
			"\\put(0,0){\\line(0,1){10}}" + ls +
			"\\put(0,0){\\line(1,0){10}}" + ls +
			"\\put(10,0){\\line(-1,1){10}}" + ls);
		
		/// Ticks
		if (vGrid == 1) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + ls);
		}
		
		/// Grid
		if (vGrid == 2 || vGrid == 4) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){" + Integer.valueOf(10 - i) + ".15}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){" + Integer.valueOf(10 - i) + ".15}}" + ls);
		}
		
		/// Text
		if (vGrid >= 3) {
			res.append("\\put(5,-1.2){\\makebox(0,0)[cc]{\\mytext Degree of agreement, $\\mu$}}" + ls +
				"\\put(-1.3,5){\\makebox(0,0)[cc]{\\rotatebox{90}{\\mytext Degree of disagreement, $\\nu$}}}" + ls +
				"\\put(0,-0.4){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(10,-0.4){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls +
				"\\put(-0.33,0){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(-0.33,10){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls);
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + ls +
					"\\put(" + i + ",-0.4){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + ls +
					"\\put(-0.5," + i + "){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls);
		}
		
		/// Points
		for (int i = 0; i < arrPoints.length; i++)
			res.append("\\put(" + arrPoints[i][0]*10 + "," + arrPoints[i][1]*10 + "){\\circle*{0.23}}" + ls);
		
		res.append("\\end{picture}" + ls +
			"\\end{center}" + ls + ls +
			"\\end{document}" + ls);
		
		return res.toString();
	}
	
	/// Refresh the tables
	private void refreshTable() {
		if (result != null) {
			if (!chkView.isSelected()) {
				/// Create the tables for primary view
				scrollA.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], MU, 1));
				scrollB.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], NU, 2));
			} else {
				/// Create the tables for secondary view
				scrollA.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], "(" + MU + "," + NU + ")", 3));
				scrollB.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], "Distance", 4));
			}
		}
	}
	
	/// Spinner for alpha
	private class SpinAlphaListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			refreshTable();
		}
	}
	
	/// Spinner for beta
	private class SpinBetaListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			refreshTable();
		}
	}
	
	/// Spinner for number of digits
	private class SpinDigitsListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			JSpinner spinner = (JSpinner)evt.getSource();
			
			/// Make 0.0000, must be with dot, not with comma
			String strDig = "0.";
			for (int i = 0; i < (int)spinner.getValue(); i++)
				strDig += "0";
			
			/// Number format
			numFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(Locale.US));
			
			refreshTable();
			
			if (result != null) {	
				/// Refresh the graphic
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
	}
	
	/// Refresh tables on checkbox events
	private class CheckSwitchListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			refreshTable();
		}
	}
	
	/// Selector for ICrA type
	private class ComboTypeListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboType.getSelectedIndex() == 0) {
					comboAggr.setVisible(false);
					spinMatCount.setVisible(false);
				} else if (comboType.getSelectedIndex() == 1) {
					comboAggr.setVisible(false);
					spinMatCount.setVisible(true);
				} else {
					comboAggr.setVisible(true);
					spinMatCount.setVisible(true);
				}
			}
		}
	}
	
	/// Selector for plot size
	private class ComboSizeListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboSize.getSelectedIndex() >= 0)
					valSize = Integer.parseInt(sizeDim[comboSize.getSelectedIndex()]);
				else if (checkInt((String)comboSize.getSelectedItem()))
					valSize = Integer.parseInt((String)comboSize.getSelectedItem());
				
				if (valSize < 100 || valSize > 100000) {
					valSize = 400;
					comboSize.setSelectedIndex(0);
				}
				
				if (result != null) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setPreferredSize(new Dimension(valSize, valSize));
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	/// Spinner for point size
	private class SpinPointsListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			JSpinner spinner = (JSpinner)evt.getSource();
			pointSize = (int)spinner.getValue();
			if (result != null) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setMyPoint((int)spinner.getValue());
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
	}
	
	/// Selector for plot type
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
	
	/// Selector for export type
	private class ComboExportListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboExport.getSelectedIndex() == 1)
					comboExportScreen();
				else if (comboExport.getSelectedIndex() == 2)
					comboExportPNG();
				else if (comboExport.getSelectedIndex() == 3)
					comboExportTeX();
				
				comboExport.setSelectedIndex(0);
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
	
	/// Save drafts at 15 minute interval
	private class MyThread implements Runnable {
		public void run() {
			try {
				while(true) {
					Thread.sleep(15*60*1000); // 1000=1s
					btnSaveDraftListener(null);
				}
			} catch(Exception ex) {
				showMessage("error", "Automatic draft saving failed");
			}
		}
	}
	
	/// Extend TextArea with UndoManager, MouseListener, KeyEvents
	private class MyTextArea extends JTextArea implements MouseListener {
		
		private JPopupMenu rightMouse;
		
		private MyTextArea(String str) {
			setText(str);
			setFont(monoFont2);
			setDragEnabled(true);
			addMouseListener(this);
			
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
			
			getDocument().addUndoableEditListener(undoMan);
			getInputMap().put((KeyStroke)actUndo.getValue(Action.ACCELERATOR_KEY), "ACTION_UNDO");
			getInputMap().put((KeyStroke)actRedo.getValue(Action.ACCELERATOR_KEY), "ACTION_REDO");
			getActionMap().put("ACTION_UNDO", actUndo);
			getActionMap().put("ACTION_REDO", actRedo);
			
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
			mCut.setBackground(Color.WHITE);
			mCopy.setBackground(Color.WHITE);
			mPaste.setBackground(Color.WHITE);
			mUndo.setBackground(Color.WHITE);
			mRedo.setBackground(Color.WHITE);
			mCut.setText("Cut");
			mCopy.setText("Copy");
			mPaste.setText("Paste");
			mUndo.setText("Undo");
			mRedo.setText("Redo");
			
			JSeparator mSep = new JSeparator();
			mSep.setBackground(Color.WHITE);
			
			rightMouse = new JPopupMenu();
			rightMouse.add(mCut);
			rightMouse.add(mCopy);
			rightMouse.add(mPaste);
			rightMouse.add(mSep);
			rightMouse.add(mUndo);
			rightMouse.add(mRedo);
			
		}
		
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
		private int plotPoint = 0;
		private int plotGrid = 0;
		private int plotMarkRow = -1;
		private int plotMarkCol = -1;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			setBackground(Color.WHITE);
			addMouseListener(this);
			addMouseMotionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		private void setMyPoint(int val) {
			plotPoint = val;
		}
		
		private void setMyGrid(int val) {
			plotGrid = val;
		}
		
		private void setCellMarker(int row, int col) {
			plotMarkRow = row;
			plotMarkCol = col;
		}
		
		private void setColMarker(int col) {
			markCol = col;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(sansFont);
			
			int width = getWidth();
			int height = getHeight();
			int delta = 20;
			if (plotGrid >= 3)
				delta = 60;
			
			/// Grid
			g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
			g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
			g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			
			/// Scale so that there are boundaries on the graph
			int widthScale = width-2*delta;
			int heightScale = height-2*delta;
			int line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double[] marks2 = new double[] {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
			
			/// Ticks
			if (plotGrid == 1) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
				
			}
			
			/// Grid
			if (plotGrid == 2 || plotGrid == 4) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+widthScale*marks[i], delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], delta+heightScale*marks[i], delta+widthScale*marks[i], height-delta+line));
				
			}
			
			/// Text
			if (plotGrid >= 3) {
				for (int i = 0; i < marks.length; i++) {
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
					int widthMark = g2.getFontMetrics(sansFont).stringWidth(String.valueOf(marks2[i]));
					g2.drawString(String.valueOf(marks2[i]), (int)(delta-4*line-widthMark/2), (int)(delta+heightScale*marks[i]+1.35*line));
				}
				
				for (int i = 0; i < marks.length; i++) {
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
					int widthMark = g2.getFontMetrics(sansFont).stringWidth(String.valueOf(marks2[i]));
					g2.drawString(String.valueOf(marks[i]), (int)(delta+widthScale*marks[i]-widthMark/2), (int)(height-delta+4.5*line));
				}
				
				g2.drawString("0", (int)(delta-3.3*line), (int)(delta+heightScale+1.35*line));
				g2.drawString("1", (int)(delta-3.3*line), (int)(delta+1.35*line));
				
				g2.drawString("0", (int)(delta-0.7*line), (int)(delta+heightScale+4.5*line));
				g2.drawString("1", (int)(delta+widthScale-line), (int)(delta+heightScale+4.5*line));
				
				g2.setFont(sansFont2);
				int widthMU = g2.getFontMetrics(sansFont2).stringWidth("Degree of agreement, " + MU);
				int widthNU = g2.getFontMetrics(sansFont2).stringWidth("Degree of disagreement, " + NU);
				g2.drawString("Degree of agreement, " + MU, (int)(delta+widthScale*marks[4]-widthMU/2), (int)(height-delta+10*line));
				
				AffineTransform g2Orig = g2.getTransform();
				g2.rotate(Math.toRadians(-90));
				g2.drawString("Degree of disagreement, " + NU, (int)(-(delta+heightScale*marks[4]+widthNU/2)), (int)(delta-9*line));
				g2.setTransform(g2Orig);
			}
			
			/// Colors
			//g2.setPaint(Color.RED);
			g2.setColor(Color.BLACK);
			setBackground(Color.WHITE);
			
			/// Save rectangles for glass
			arrRect = new Rectangle[points.length];
			int radius = plotPoint;
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < points.length; i++) {
				double x = points[i][0]*widthScale+delta;
				double y = heightScale-points[i][1]*heightScale+delta;
				arrRect[i] = new Rectangle((int)x-radius, (int)y-radius, 2*radius, 2*radius);
				g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
			}
			
			/// Mark the selected point in the table
			for (int i = 0; i < points.length; i++) {
				
				/// All points from that column - mark in blue - use ICrAData.markCol
				if (markCol > -1 && (points[i][3] == markCol || points[i][2] == markCol)) {
					double x = points[i][0]*widthScale+delta;
					double y = heightScale-points[i][1]*heightScale+delta;
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				}
				
				/// Cell that has focus - mark in red - use ICrAData.MyPanel.plotMarkRow/plotMarkCol
				if (plotMarkRow > -1 && plotMarkRow > -1 &&
						((points[i][2] == plotMarkRow && points[i][3] == plotMarkCol) ||
						(points[i][3] == plotMarkRow && points[i][2] == plotMarkCol))) {
					double x = points[i][0]*widthScale+delta;
					double y = heightScale-points[i][1]*heightScale+delta;
					g2.setColor(Color.RED);
					g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				}
			}
			
		}
		
		/// Set tool-tip text
		public String getToolTipText(MouseEvent evt) {
			
			for (int i = 0; i < arrRect.length; i++) {
				if (arrRect[i].contains(evt.getX(), evt.getY())) {
					return "<html>Row: " + headerNames[0][(int)Math.round(points[i][2])] +
							"<br/>Column: " + headerNames[0][(int)Math.round(points[i][3])] + 
							"<br/>" + MU + ": " + numFormat.format(points[i][0]) +
							"<br/>" + NU + ": " + numFormat.format(points[i][1]) +
							"<br/>Distance: " + numFormat.format(points[i][4]) +
							"</html>";
				}
			}
			
			return null;
		}
		
		/// Show the glass when the left mouse button is pressed and held
		public void mousePressed(MouseEvent evt) {
			if (SwingUtilities.isLeftMouseButton(evt)) {
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
						
						/// View the cell
						scrollA.getHorizontalScrollBar().setValue((int)cellA.getX());
						scrollA.getVerticalScrollBar().setValue((int)cellA.getY());
						scrollB.getHorizontalScrollBar().setValue((int)cellB.getX());
						scrollB.getVerticalScrollBar().setValue((int)cellB.getY());
						
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
		}
		
		/// Hide the glass when the left mouse button is released
		public void mouseReleased(MouseEvent evt) {
			if (SwingUtilities.isLeftMouseButton(evt)) {
				//((JPanel)evt.getSource()).setCursor(Cursor.getDefaultCursor());
				//this.getRootPane().setCursor(Cursor.getDefaultCursor());
				glass.setCursor(Cursor.getDefaultCursor());
				glass.setVisible(false);
			}
		}
		
		/// Clear blue markers on mouse right-click
		public void mouseClicked(MouseEvent evt) {
			if (result != null) {
				if (SwingUtilities.isRightMouseButton(evt)) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setColMarker(-1);
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
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
				
				int headerA = tableA.getTableHeader().getHeight();
				int headerB = tableB.getTableHeader().getHeight();
				int eps = 2;
				//System.out.println(locA.getX() + " " + locA.getY() + "     " + locF.getX() + " " + locF.getY());
				
				/// Draw cells
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLUE);
				
				if (locA.getX() - locF.getX() + pointA[0] >= locSA.getX() - locF.getX() &&
					locA.getY() - locF.getY() + pointA[1] >= locSA.getY() - locF.getY() + headerA &&
					locA.getX() - locF.getX() + pointA[0] + pointA[2] <= locSA.getX() - locF.getX() + visSA.width + eps &&
					locA.getY() - locF.getY() + pointA[1] + pointA[3] <= locSA.getY() - locF.getY() + visSA.height + headerA + eps)
					g2.draw(new Rectangle2D.Double(
						locA.getX() - locF.getX() + pointA[0], locA.getY() - locF.getY() + pointA[1], pointA[2], pointA[3]));
				
				if (locB.getX() - locF.getX() + pointB[0] >= locSB.getX() - locF.getX() &&
					locB.getY() - locF.getY() + pointB[1] >= locSB.getY() - locF.getY() + headerB &&
					locB.getX() - locF.getX() + pointB[0] + pointB[2] <= locSB.getX() - locF.getX() + visSB.width + eps &&
					locB.getY() - locF.getY() + pointB[1] + pointB[3] <= locSB.getY() - locF.getY() + visSB.height + headerB + eps)
					g2.draw(new Rectangle2D.Double(
						locB.getX() - locF.getX() + pointB[0], locB.getY() - locF.getY() + pointB[1], pointB[2], pointB[3]));
				
				/// Draw table
				//g2.setColor(Color.RED);
				//g2.draw(new Rectangle2D.Double(locA.getX()-locF.getX(), locA.getY()-locF.getY(), visibleA.width, visibleA.height));
				//g2.draw(new Rectangle2D.Double(locB.getX()-locF.getX(), locB.getY()-locF.getY(), visibleB.width, visibleB.height));
				
				/// Draw scrollpane
				//g2.setColor(Color.GREEN);
				//g2.draw(new Rectangle2D.Double(locSA.getX()-locF.getX(), locSA.getY()-locF.getY()+headerA, visSA.width, visSA.height));
				//g2.draw(new Rectangle2D.Double(locSB.getX()-locF.getX(), locSB.getY()-locF.getY()+headerB, visSB.width, visSB.height));
				
			}
			
		}
	}
	
	/// Table for data view
	private class MyTable extends JTable implements MouseListener {
		
		private double[][] arrA;
		private double[][] arrB;
		private String[][] data;
		private String[] rows;
		private String[] cols;
		
		private Action actCopy = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				saveTableData();
			}
		};
		
		private MyTable(double[][] arrA, double[][] arrB, double[][] arrD, String[] headers, String name, int type) {
			
			data = new String[arrA.length][arrA[0].length];
			for (int i = 0; i < arrA.length; i++) {
				for (int j = 0; j < arrA[0].length; j++) {
					if (type == 1)
						data[i][j] = numFormat.format(arrA[i][j]);
					else if (type == 2)
						data[i][j] = numFormat.format(arrB[i][j]);
					else if (type == 3)
						data[i][j] = "(" + numFormat.format(arrA[i][j]) + "," + numFormat.format(arrB[i][j]) + ")";
					else if (type == 4)
						data[i][j] = numFormat.format(arrD[i][j]);
				}
			}
			
			rows = headers;
			cols = new String[headers.length + 1];
			cols[0] = name;
			for (int i = 0; i < headers.length; i++)
				cols[i+1] = headers[i];
			
			this.arrA = arrA;
			this.arrB = arrB;
			addMouseListener(this);
			
			setModel(new MyTableModel());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setAutoCreateRowSorter(true);
			setColumnSelectionAllowed(true);
			setRowSelectionAllowed(false);
			
			getTableHeader().setFont(monoFont);
			//getTableHeader().setResizingAllowed(false);
			getTableHeader().setReorderingAllowed(false);
			
			setDefaultRenderer(String.class, new MyCellRenderer());
			//setDefaultRenderer(String.class, new ExCellRenderer());
			((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			
			/// Modify the Ctrl-C key binding
			actCopy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			getInputMap().put((KeyStroke)actCopy.getValue(Action.ACCELERATOR_KEY), "ACTION_COPY");
			getActionMap().put("ACTION_COPY", actCopy);
			
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
					return data[row][col-1];
			}
			
			public Class<?> getColumnClass(int col) {
				return String.class;
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
				setForeground(Color.BLACK);
				
				if (col > 0 && row != col-1) {
					if (arrA[row][col-1] > (double)spinAlpha.getValue() &&
						arrB[row][col-1] < (double)spinBeta.getValue())
						setForeground((chkColor.isSelected() ? Color.GREEN : greenClr));
					
					else if (arrA[row][col-1] < (double)spinBeta.getValue() &&
							arrB[row][col-1] > (double)spinAlpha.getValue())
						setForeground((chkColor.isSelected() ? Color.BLUE : blueClr));
					
					else
						setForeground((chkColor.isSelected() ? Color.RED : violetClr));
					
					//checkDouble((String)value)
					//double val = Double.parseDouble((String)value);
					
					/// Positive consonance - mu > alpha && nu < 1-alpha
					/*if ((tableName.equals(MU) && val > 0.75) || (tableName.equals(NU) && val < 1-0.75))
						setForeground(greenClr);
					
					/// Negative consonance - mu < beta & nu > 1-beta
					if ((tableName.equals(MU) && val < 0.25) || (tableName.equals(NU) && val > 1-0.25))
						setForeground(blueClr);
					
					/// Dissonance - mu in (beta,alpha) && nu in (1-beta,1-alpha)
					if ((tableName.equals(MU) && val > 0.25 && val < 0.75) || (tableName.equals(NU) && val > 1-0.75 && val < 1-0.25))
						setForeground(violetClr);
					*/
					//setToolTipText("ind " + Math.round(val*10));
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
			
			/// Initialize components
			Component comp = super.prepareRenderer(rend, row, col);
			Component hdr = super.prepareRenderer(rend, row, col);
			TableColumn tableCol = this.getColumnModel().getColumn(col);
			TableColumn hdrCol = this.getTableHeader().getColumnModel().getColumn(col);
			
			/// Retrieve header component before assigning first column
			hdr = this.getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(this, hdrCol.getHeaderValue(), false, false, -1, col);
			
			/// Make first column appear as header
			if (col == 0)
				comp = this.getTableHeader().getDefaultRenderer()
						.getTableCellRendererComponent(this, this.getValueAt(row, col), false, false, row, col);
			
			/// Dynamic size of the columns
			tableCol.setPreferredWidth(Math.max(Math.max(
					tableCol.getPreferredWidth(), hdrCol.getPreferredWidth()), Math.max(
					comp.getPreferredSize().width + this.getIntercellSpacing().width + 10,
					hdr.getPreferredSize().width + this.getIntercellSpacing().width + 10)));
			
			return comp;
		}
		
		/// Save table data to clipboard
		public void saveTableData() {
			try {
				//String tableData = "cell " + this.getSelectedRow() + " " + this.getSelectedColumn() + " " +
				//		this.getValueAt(this.rowAtPoint(evt.getPoint()), this.columnAtPoint(evt.getPoint()));
				//System.out.println(tableData);
				
				int rows = this.getRowCount();
				int[] cols = this.getSelectedColumns(); //this.getColumnCount();
				StringBuilder res = new StringBuilder();
				
				/// Column header
				for (int j = 0; j < cols.length; j++) {
					res.append(this.getColumnName(cols[j]));
					if (j != cols.length-1)
						res.append("\t");
				}
				res.append(ls);
				
				/// Table data
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols.length; j++) {
						res.append(this.getValueAt(i, cols[j]));
						if (j != cols.length-1)
							res.append("\t");
					}
					if (i != rows-1)
						res.append(ls);
				}
				
				/// Save to clipboard
				StringSelection sel = new StringSelection(res.toString());
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(sel, sel);
				
			} catch (Exception ex) {
				showMessage("error", "Coult not copy table data to clipboard");
			}
		}
		
		/// Show row markers on left-click and hide them on right-click
		public void mouseClicked(MouseEvent evt) {
			if (result != null) {
				if (SwingUtilities.isLeftMouseButton(evt)) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setColMarker(this.getSelectedColumn()-1);
					resPanel.repaint();
					resPanel.revalidate();
					
				} else if (SwingUtilities.isRightMouseButton(evt)) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setColMarker(-1);
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
		
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
		/// Show cell marker on mouse press and hold
		public void mousePressed(MouseEvent evt) {
			//int valR = this.rowAtPoint(evt.getPoint());
			//int valC = this.columnAtPoint(evt.getPoint());
			//System.out.println("mouse " + this.getValueAt(valR, valC));
			
			//int valR = this.getSelectedRow();
			//int valC = this.getSelectedColumn();
			//System.out.println("mouse " + valR + " " + valC);
			
			if (result != null) {
				if (SwingUtilities.isLeftMouseButton(evt)) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setCellMarker(this.getSelectedRow(), this.getSelectedColumn()-1);
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
		
		/// Hide cell marker on mouse release
		public void mouseReleased(MouseEvent evt) {
			if (result != null) {
				if (SwingUtilities.isLeftMouseButton(evt)) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setCellMarker(-1, -1);
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
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
	private boolean checkDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Check for ordered pair
	private boolean checkOrdPair(String str) {
		
		boolean mark1 = false;
		boolean mark2 = false;
		
		String[] lines = str.split(ls);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			
			if (line.startsWith("#input1"))
				mark1 = true;
			if (line.startsWith("#input2"))
				mark2 = true;
		}
		
		return mark1 && mark2;
	}
	
	/// Find separator
	private String findSep(String line) {
		
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
	
	/// Make matrix data - vec, matrix count, shift right, shift down, transpose
	private Vector<double[][]> makeData(Vector<String> vec, int matCount, int sr, int sd, boolean transpose) {
		
		String separator = "";
		if ((separator = findSep(vec.get(0))) == "") {
			showMessage("warn", "Could not determine column separator");
			return null;
		}
		
		int col = 0;
		if ((col = findCol(vec, separator)) == 0) {
			showMessage("warn", "Columns are inconsistent");
			return null;
		}
		
		if (vec.size()-sd < 3 || col-sr < 3) {
			showMessage("warn", "Minimum matrix size is 3x3");
			return null;
		}
		
		String[] arr = vec.get(0).split(separator);
		double[][] dres = new double[1][1];
		
		if (!transpose) {
			dres = new double[vec.size()-sd][arr.length-sr];
			
			if (sr > 0)
				rowNames = "";
			if (sd > 0 && sr == 0)
				colNames = vec.get(0);
			if (sd > 0 && sr > 0)
				colNames = vec.get(0).substring(vec.get(0).indexOf(separator)+1);
			
			for (int i = 0; i < vec.size()-sd; i++) {
				arr = vec.get(i+sd).split(separator);
				
				for (int j = 0; j < arr.length-sr; j++) {
					if (j == 0 && sr > 0)
						rowNames += arr[j] + separator;
					
					String val = arr[j+sr].replace(",", ".");
					if (checkDouble(val))
						dres[i][j] = Double.parseDouble(val);
				}
			}
			
		} else {
			dres = new double[arr.length-sr][vec.size()-sd];
			
			if (sr > 0)
				rowNames = "";
			if (sd > 0 && sr == 0)
				colNames = vec.get(0);
			if (sd > 0 && sr > 0)
				colNames = vec.get(0).substring(vec.get(0).indexOf(separator)+1);
			
			for (int i = 0; i < vec.size()-sd; i++) {
				arr = vec.get(i+sd).split(separator);
				
				for (int j = 0; j < arr.length-sr; j++) {
					if (j == 0 && sr > 0)
						rowNames += arr[j] + separator;
					
					String val = arr[j+sr].replace(",", ".");
					if (checkDouble(val))
						dres[j][i] = Double.parseDouble(val);
				}
			}
		}
		
		/// Result
		Vector<double[][]> vres = new Vector<double[][]>();
		if (matCount == 1) {
			vres.add(dres);
			return vres;
		}
		
		if (matCount < 3) {
			showMessage("warn", "Requires at least 3 matrices");
			return null;
		}
		
		/// Split into matrices
		if (!transpose) {
			if (dres.length % matCount != 0) {
				showMessage("warn", "Input data length must be fully divisible by matrix count");
				return null;
			}
			
			int vrows = dres.length/matCount;
			for (int k = 0; k < matCount; k++) {
				double[][] dmat = new double[vrows][dres[0].length];
				
				for (int i = 0; i < vrows; i++)
					for (int j = 0; j < dres[0].length; j++)
						dmat[i][j] = dres[i+k*vrows][j];
				
				vres.add(dmat);
			}
		} else {
			if (dres[0].length % matCount != 0) {
				showMessage("warn", "Input data length must be fully divisible by matrix count");
				return null;
			}
			
			int vcols = dres[0].length/matCount;
			for (int k = 0; k < matCount; k++) {
				double[][] dmat = new double[dres.length][vcols];
				
				for (int i = 0; i < dres.length; i++)
					for (int j = 0; j < vcols; j++)
						dmat[i][j] = dres[i][j+k*vcols];
				
				vres.add(dmat);
			}
		}
		
		return vres;
	}
	
	/// Make matrix header - type 0 criteria, type 1 object
	private String[] makeHeader(String strRowN, String strColN, int len, int type, boolean transpose) {
		
		/// Align digits formatting
		/// Value of 30 is 2 digits, so it is 2 zeros for R01, C12 
		/// value of 250 is 3 digits, so it is 3 zeros for R003, C111
		String nalign = "";
		for (int i = 0; i < String.valueOf(len).length(); i++)
			nalign += "0";
		
		/// Symbols
		DecimalFormat nformat = new DecimalFormat(nalign, new DecimalFormatSymbols(Locale.US));
		
		/// Automatic headers
		String[] arrNames = new String[len];
		for (int i = 0; i < arrNames.length; i++) {
			if (type == 0)
				arrNames[i] = (!transpose ? "C" : "O");
			else if (type == 1)
				 arrNames[i] = (!transpose ? "O" : "C");
			
			arrNames[i] += nformat.format(Integer.valueOf(i + 1));
		}
		
		/// Assign string for manual headers
		String str = "";
		if (type == 0)
			str = (!transpose ? strRowN : strColN);
		else if (type == 1)
			str = (!transpose ? strColN : strRowN);
		
		/// Manual headers
		String separator = "";
		if (str.length() > 0 && (separator = findSep(str)) != "") {
			String[] arrStr = str.split(separator);
			for (int i = 0; i < arrStr.length && i < arrNames.length; i++)
				if (arrStr[i].trim().length() > 0)
					arrNames[i] = arrStr[i].trim();
		}
		
		return arrNames;
	}
	
	/// Load data from the user input
	private void btnAnalysisListener(ActionEvent evt) {
		
		/// Read global variables
		int vType = comboType.getSelectedIndex();
		int vAggr = comboAggr.getSelectedIndex();
		int vMatCount = (int)spinMatCount.getValue();
		
		int vRowN = (chkRowNames.isSelected() ? 1 : 0);
		int vColN = (chkColNames.isSelected() ? 1 : 0);
		boolean vTranspose = chkTranspose.isSelected();
		boolean vOrdPair = chkOrdPair.isSelected();
		
		int vMethod = comboMethod.getSelectedIndex();
		
		/// Clear global variables
		rowNames = "";
		colNames = "";
		headerNames = new String[][] {null, null, null, null};
		
		/// Read user input
		String data = textA.getText();
		if (data.length() == 0) {
			showMessage("info", "Nothing to load");
			return;
		}
		
		/// Check for ordered pair
		if (vOrdPair && !checkOrdPair(data)) {
			showMessage("info", "Ordered pair requires two data sets after #input1 and #input2");
			return;
		}
		
		/// Instance for the algorithm
		ICrA icra = new ICrA();
		
		/// Ordered pair - two data sets
		if (vOrdPair && checkOrdPair(data)) {
			
			Vector<String> vec1 = new Vector<String>();
			Vector<String> vec2 = new Vector<String>();
			
			try {
				boolean mark1 = false;
				boolean mark2 = false;
				
				String[] lines = data.split(ls);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					
					if (line.startsWith("#rownames:"))
						rowNames = line.substring(10);
					if (line.startsWith("#colnames:"))
						colNames = line.substring(10);
					
					if (mark1 && !mark2 && !line.startsWith("#") && line.length() != 0)
						vec1.add(line);
					if (!mark1 && mark2 && !line.startsWith("#") && line.length() != 0)
						vec2.add(line);
					
					if (line.startsWith("#input1")) {
						mark1 = true;
						mark2 = false;
					}
					if (line.startsWith("#input2")) {
						mark1 = false;
						mark2 = true;
					}
				}
				
			} catch (Exception ex) {
				showMessage("error", "Could not load the data");
				return;
			}
			
			/// Make result
			try {
				if (vec1.size() == 0 || vec2.size() == 0) {
					showMessage("warn", "No data to read");
					return;
				}
				
				if (vType == 0) {
					/// Standard
					Vector<double[][]> vres1 = makeData(vec1, 1, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, 1, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("warn", "Ordered pair - " + typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeStandard(vres1, vres2, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					headerNames[1] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else if (vType == 1) {
					/// Second Order
					Vector<double[][]> vres1 = makeData(vec1, vMatCount, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, vMatCount, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("warn", "Ordered pair - " + typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeSecondOrder(vres1, vres2, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres1.size(), 0, vTranspose);
					int vsize = vres1.get(0).length;
					headerNames[1] = makeHeader(rowNames, colNames, (vsize*vsize-vsize)/2, 1, vTranspose);
					headerNames[2] = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					headerNames[3] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else {
					/// Aggregated
					Vector<double[][]> vres1 = makeData(vec1, vMatCount, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, vMatCount, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("warn", "Ordered pair - " + typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeAggregated(vres1, vres2, vAggr, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					headerNames[1] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + aggrNames[vAggr] + " - " + methodNames[vMethod]);
				}
				
			} catch (Exception ex) {
				showMessage("error", "Ordered pair - could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("error", "Not enough memory. Start the application with: " + ls +
						"  java -Xmx10240m -jar ICrAData.jar");
				return;
			}
			
			
		/// Normal - one data set
		} else {
			
			/// Read data
			Vector<String> vec = new Vector<String>();
			try {
				String[] lines = data.split(ls);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					
					if (line.startsWith("#rownames:"))
						rowNames = line.substring(10);
					if (line.startsWith("#colnames:"))
						colNames = line.substring(10);
					
					if (!line.startsWith("#") && line.length() != 0)
						vec.add(line);
				}
				
			} catch (Exception ex) {
				showMessage("error", "Could not load the data");
				return;
			}
			
			/// Make result
			try {
				if (vec.size() == 0) {
					showMessage("warn", "No data to read");
					return;
				}
				
				if (vType == 0) {
					/// Standard
					Vector<double[][]> vres = makeData(vec, 1, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("warn", typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeStandard(vres, null, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					headerNames[1] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage("info", typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else if (vType == 1) {
					/// Second Order
					Vector<double[][]> vres = makeData(vec, vMatCount, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("warn", typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeSecondOrder(vres, null, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres.size(), 0, vTranspose);
					int vsize = vres.get(0).length;
					headerNames[1] = makeHeader(rowNames, colNames, (vsize*vsize-vsize)/2, 1, vTranspose);
					headerNames[2] = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					headerNames[3] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage("info", typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else {
					/// Aggregated
					Vector<double[][]> vres = makeData(vec, vMatCount, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("warn", typeNames[vType] + " - failed to read data");
						return;
					}
					
					result = icra.makeAggregated(vres, null, vAggr, vMethod, vOrdPair);
					headerNames[0] = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					headerNames[1] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage("info", typeNames[vType] + " - " + aggrNames[vAggr] + " - " + methodNames[vMethod]);
				}
				
			} catch (Exception ex) {
				showMessage("error", "Could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("error", "Not enough memory. Start the application with: " + ls +
						"  java -Xmx10240m -jar ICrAData.jar");
				return;
			}
		}
		
		
		/// Display the result
		try {
			if (result != null) {
				
				/// Create the tables
				scrollA.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], MU, 1));
				scrollB.setViewportView(new MyTable(result.get("MatrixMU"), result.get("MatrixNU"),
						result.get("MatrixDist"), headerNames[0], NU, 2));
				
				/// Create the graphic
				double[][] arrPoints = result.get("PlotPoints");
				MyPanel resPanel = new MyPanel(arrPoints);
				resPanel.setPreferredSize(new Dimension(valSize, valSize));
				resPanel.setMyPoint(pointSize);
				resPanel.setColMarker(-1);
				resScroll.setViewportView(resPanel);
				
				/// Reset combo boxes - these events must be fired after resPanel is created, otherwise NullPointerException
				chkView.setSelected(false);
				comboSize.setSelectedItem(String.valueOf(valSize)); /// event fired
				comboGrid.setSelectedIndex(0); /// event fired
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not display the result");
		}
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
	
	/// Change cursor on glass - http://www.javaspecialists.co.za/archive/Issue065.html
	/// Unicode symbols - https://stackoverflow.com/questions/16616162/how-to-write-unicode-cross-symbol-in-java
	
	/// Mouse dragging
	/// http://stackoverflow.com/questions/10243257/java-scroll-image-by-mouse-dragging
	/// http://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html
	
	/// Scroll
	/// https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane
	/// https://stackoverflow.com/questions/13213030/jscrollpane-set-scroll-position
	
	/// Locale - new Locale("bg", "BG")
	/// http://www.oracle.com/technetwork/java/javase/javase7locales-334809.html
	
	/// Gradient
	/// https://stackoverflow.com/questions/27532/generating-gradients-programmatically
	
	/// Mouse click on table
	/// https://stackoverflow.com/questions/7350893/click-event-on-jtable-java?noredirect=1
	/// https://stackoverflow.com/questions/1378096/actionlistener-on-jlabel-or-jtable-cell
	
	/// Auto-resize columns in table
	/// https://stackoverflow.com/questions/17627431/auto-resizing-the-jtable-column-widths
	/// https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
	
	/// This rotates the coordinate system
	/// https://stackoverflow.com/questions/10083913/how-to-rotate-text-with-graphics2d-in-java#10084078
	
	/// Width of string
	/// https://stackoverflow.com/questions/258486/calculate-the-display-width-of-a-string-in-java
	
	/// List single selection
	/// https://stackoverflow.com/questions/23557000/how-to-disable-multiselect-of-listbox-jlist-in-java
	
	/// Update clipboard on mouse click with full table data
	/// http://forums.codeguru.com/showthread.php?516091-How-can-I-copy-the-headers-along-with-the-table-data-in-a-JTable
	/// https://stackoverflow.com/questions/9484407/jtable-override-ctrlc-behaviour
	/// https://stackoverflow.com/questions/3591945/copying-to-the-clipboard-in-java
	/// https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html
	
	/// Center header text
	/// https://stackoverflow.com/questions/7493369/jtable-right-align-header
	
	/// Screen capture
	/// https://stackoverflow.com/questions/58305/is-there-a-way-to-take-a-screenshot-using-java-and-save-it-to-some-sort-of-image#58326
	
	/// Pressing Alt+PrintScreen and then retrieving clipboard contents, but it is not reliable enough
	/// https://stackoverflow.com/questions/14595483/using-java-to-send-key-combinations
	/// https://stackoverflow.com/questions/7105778/get-readable-text-only-from-clipboard
	
	/// Adjusting table header size
	/// https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
	
}

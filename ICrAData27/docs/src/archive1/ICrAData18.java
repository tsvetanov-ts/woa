/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 1.8
 * Date: January 9, 2021
 * Compiled by: OpenJDK 11.0.8 (javac --release 7 *.java)
 * 
 */

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
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import javax.swing.ListCellRenderer;
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
	private SimpleDateFormat msgDate = new SimpleDateFormat("HH:mm:ss");
	
	/// DecimalFormat - # is optional, while 0 is always written - ###.#### or 000.0000
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private Font monoFont2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private Font lblFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private Font lblFont2 = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	
	private Color poscClr = new Color(0, 160, 0); /// green
	private Color negcClr = new Color(255, 64, 64); /// red
	private Color dissClr = new Color(255, 64, 255); /// magenta
	
	private String MU = "\u03BC";
	private String NU = "\u03BD";
	private String AL = "\u03B1";
	private String BE = "\u03B2";
	
	/// Variables
	private double[][] result = null;
	private Point pp = new Point();
	private int markCol = -1;
	
	private MyTextArea textA;
	private JTextArea textB;
	private JScrollPane scrollA, scrollB, resScroll;
	private JComboBox<String> comboMethod, comboVariant, comboTable1, comboTable2, comboSize;
	private JCheckBox chkRowNames, chkColNames, chkTranspose, chkOrdPair;
	private JSpinner spinMatCnt, spinAlpha, spinBeta, spinPoints;
	private JCheckBox cbColor, cbMarks, cbGrid, cbText;
	
	private String[] methodNames = new String[] {"Standard", "Aggr Average", "Aggr Max/Min", "Aggr Min/Max", "Criteria Pair"};
	//{"Standard ICrA", "Second Order ICrA", "Aggregated ICrA"};
	private String[] variantNames = new String[] {MU + "-biased", "Unbiased", NU + "-biased", "Balanced", "Weighted"};
	private String[] sizeDim = new String[] {"400", "600", "800", "1000", "2000", "3000", "5000"};
	
	private int valSize = 400; /// Plot size in pixels
	private int pointSize = 5; /// Point size of the plot
	
	/// Headers
	private String rowNames = "";
	private String colNames = "";
	private String[] hdrs = null;
	
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
				//sansFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
				//sansFont2 = new Font(Font.SANS_SERIF, Font.BOLD, 18);
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
		Dimension dim0 = new Dimension(0, 0);
		Dimension dim50 = new Dimension(50, 25);
		Dimension dim60 = new Dimension(60, 25);
		Dimension dim70 = new Dimension(70, 25);
		Dimension dim100 = new Dimension(100, 25);
		Dimension dim150 = new Dimension(135, 25);
		Dimension dim450 = new Dimension(415, 25);
		Dimension scrollSize = new Dimension(500, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
		
		/// Items for panel 1a
		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.setToolTipText("Open a file.");
		btnOpenFile.setPreferredSize(dim150);
		btnOpenFile.setFont(lblFont);
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenFileListener(evt);
			}
		});
		
		JButton btnSaveFile = new JButton("Save File");
		btnSaveFile.setToolTipText("Save the text in the input panel by choosing a file name.");
		btnSaveFile.setPreferredSize(dim150);
		btnSaveFile.setFont(lblFont);
		btnSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveFileListener(evt);
			}
		});
		
		JButton btnSaveDraft = new JButton("Save Draft");
		btnSaveDraft.setToolTipText("Save draft in subdirectory \"drafts\".");
		btnSaveDraft.setPreferredSize(dim150);
		btnSaveDraft.setFont(lblFont);
		btnSaveDraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveDraftListener(evt);
			}
		});
		
		/// Items for panel 1b
		JLabel lblMethod = new JLabel("ICrA Method");
		lblMethod.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMethod.setPreferredSize(dim150);
		lblMethod.setFont(lblFont);
		
		comboMethod = new JComboBox<String>(methodNames);
		comboMethod.setToolTipText("<html>Method for InterCriteria Analysis.<br/>Standard directly applies the base algorithm.<br/>" +
				"The others require at least three input matrices.</html>");
		comboMethod.setPreferredSize(dim150);
		comboMethod.setFont(lblFont);
		comboMethod.addItemListener(new ComboMethodListener());
		
		JLabel lblMatCnt = new JLabel("MatCnt");
		lblMatCnt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMatCnt.setPreferredSize(dim70);
		lblMatCnt.setFont(lblFont);
		
		spinMatCnt = new JSpinner(new SpinnerNumberModel(1,1,10000,1));
		spinMatCnt.setToolTipText("Matrix count is applied to Aggregated and Criteria Pair.");
		spinMatCnt.setPreferredSize(dim60);
		spinMatCnt.setFont(monoFont);
		spinMatCnt.setEnabled(false);
		
		/// Items for panel 1c
		JLabel lblVariant = new JLabel("ICrA Variant");
		lblVariant.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVariant.setPreferredSize(dim150);
		lblVariant.setFont(lblFont);
		
		comboVariant = new JComboBox<String>(variantNames);
		comboVariant.setToolTipText("<html>Variant for InterCriteria Analysis.<br/>This is the base algorithm.</html>");
		comboVariant.setPreferredSize(dim150);
		comboVariant.setFont(lblFont);
		
		chkRowNames = new JCheckBox("RowNames");
		chkRowNames.setToolTipText("Row names are in the first column.");
		chkRowNames.setFont(lblFont);
		chkRowNames.setSelected(true);
		
		chkColNames = new JCheckBox("ColNames");
		chkColNames.setToolTipText("Column names are in the first row.");
		chkColNames.setFont(lblFont);
		chkColNames.setSelected(true);
		
		chkTranspose = new JCheckBox("Transpose");
		chkTranspose.setToolTipText("Transpose each matrix independently.");
		chkTranspose.setFont(lblFont);
		
		chkOrdPair = new JCheckBox("OrderedPair");
		chkOrdPair.setToolTipText("<html>Input two sets of data to load as ordered pair (" + MU + "," + NU + ").<br/>" +
				"Data after #input1 is for " + MU + ", data after #input2 is for " + NU + ".<br/>" +
				"Aggregated/Criteria Pair require two data sets of at least three matrices each.</html>");
		chkOrdPair.setFont(lblFont);
		
		MyComboBox comboChk = new MyComboBox(new JCheckBox[] {chkRowNames, chkColNames, chkTranspose, chkOrdPair});
		comboChk.setToolTipText("Select the input options.");
		comboChk.setPreferredSize(dim150);
		
		/// Items for panel 1d
		JButton btnAnalysis = new JButton("Analysis");
		btnAnalysis.setToolTipText("Make the calculations and display them.");
		btnAnalysis.setPreferredSize(dim450);
		btnAnalysis.setFont(lblFont);
		btnAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAnalysisListener(evt);
			}
		});
		
		/// Items for panel 2a
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAlpha.setPreferredSize(dim50);
		lblAlpha.setFont(lblFont);
		
		spinAlpha = new JSpinner(new SpinnerNumberModel(0.75,0.5,1.0,0.01));
		spinAlpha.setToolTipText("<html>Table and plot colors:<br/>" +
				MU + " &gt; " + AL + " and " + NU + " &lt; " + BE + " - positive consonance (green),<br/>" + 
				MU + " &lt; " + BE + " and " + NU + " &gt; " + AL + " - negative consonance (red),<br/>" +
				"all other cases - dissonance (magenta).</html>");
		spinAlpha.setPreferredSize(dim60);
		spinAlpha.setFont(monoFont);
		spinAlpha.addChangeListener(new SpinAlphaListener());
		
		JLabel lblBeta = new JLabel("Beta");
		lblBeta.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBeta.setPreferredSize(dim50);
		lblBeta.setFont(lblFont);
		
		spinBeta = new JSpinner(new SpinnerNumberModel(0.25,0.0,0.5,0.01));
		spinBeta.setToolTipText("<html>Table and plot colors:<br/>" +
				MU + " &gt; " + AL + " and " + NU + " &lt; " + BE + " - positive consonance (green),<br/>" + 
				MU + " &lt; " + BE + " and " + NU + " &gt; " + AL + " - negative consonance (red),<br/>" +
				"all other cases - dissonance (magenta).</html>");
		spinBeta.setPreferredSize(dim60);
		spinBeta.setFont(monoFont);
		spinBeta.addChangeListener(new SpinBetaListener());
		
		/// Items for panel 2b
		JLabel lblDigits = new JLabel("Digits");
		lblDigits.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDigits.setPreferredSize(dim50);
		lblDigits.setFont(lblFont);
		
		JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,16,1));
		spinDigits.setToolTipText("Digits after the decimal separator.");
		spinDigits.setPreferredSize(dim60);
		spinDigits.setFont(monoFont);
		spinDigits.addChangeListener(new SpinDigitsListener());
		
		JLabel lblWidth = new JLabel("Width");
		lblWidth.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWidth.setPreferredSize(dim50);
		lblWidth.setFont(lblFont);
		
		JSpinner spinWidth = new JSpinner(new SpinnerNumberModel(80,10,1000,10));
		spinWidth.setToolTipText("Table column width.");
		spinWidth.setPreferredSize(dim60);
		spinWidth.setFont(monoFont);
		spinWidth.addChangeListener(new SpinWidthListener());
		
		/// Items for panel 2c
		JLabel lblTable1 = new JLabel("Table 1");
		lblTable1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTable1.setPreferredSize(dim60);
		lblTable1.setFont(lblFont);
		
		comboTable1 = new JComboBox<String>(new String[] {MU + "/" + NU + " upper/lower", "(" + MU + ";" + NU + ") table",
				MU + " table", NU + " table", "distance to (1;0)", "distance to (0;1)", "distance to (0;0)"});
		comboTable1.setToolTipText("Display table 1.");
		comboTable1.setPreferredSize(dim150);
		comboTable1.setFont(lblFont);
		//comboTable1.setMaximumRowCount(9);
		comboTable1.setSelectedIndex(2);
		comboTable1.addItemListener(new ComboTable1Listener());
		
		JLabel lblTable2 = new JLabel("Table 2");
		lblTable2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTable2.setPreferredSize(dim60);
		lblTable2.setFont(lblFont);
		
		comboTable2 = new JComboBox<String>(new String[] {MU + "/" + NU + " upper/lower", "(" + MU + ";" + NU + ") table",
				MU + " table", NU + " table", "distance to (1;0)", "distance to (0;1)", "distance to (0;0)"});
		comboTable2.setToolTipText("Display table 2.");
		comboTable2.setPreferredSize(dim150);
		comboTable2.setFont(lblFont);
		//comboTable2.setMaximumRowCount(9);
		comboTable2.setSelectedIndex(3);
		comboTable2.addItemListener(new ComboTable2Listener());
		
		/// Items for panel 2d
		JButton btnExport = new JButton("Export");
		btnExport.setToolTipText("Export tables.");
		btnExport.setPreferredSize(dim100);
		btnExport.setFont(lblFont);
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnExportListener(evt);
			}
		});
		
		JButton btnInfo = new JButton("Info");
		btnInfo.setToolTipText("Information for the application.");
		btnInfo.setPreferredSize(dim100);
		btnInfo.setFont(lblFont);
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnInfoListener(evt);
			}
		});
		
		JButton btnScreen = new JButton("Screen");
		btnScreen.setToolTipText("Screenshot of the application.");
		btnScreen.setPreferredSize(dim100);
		btnScreen.setFont(lblFont);
		btnScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnScreenListener(evt);
			}
		});
		
		JButton btnAbout = new JButton("About");
		btnAbout.setToolTipText("About the application.");
		btnAbout.setPreferredSize(dim100);
		btnAbout.setFont(lblFont);
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAboutListener(evt);
			}
		});
		
		/// Items for panel 3
		comboSize = new JComboBox<String>(sizeDim);
		comboSize.setToolTipText("Plot size or value from 100 to 10000.");
		comboSize.setPreferredSize(dim60);
		comboSize.setFont(monoFont);
		comboSize.setEditable(true);
		comboSize.addItemListener(new ComboSizeListener());
		
		spinPoints = new JSpinner(new SpinnerNumberModel(pointSize,1,20,1));
		spinPoints.setToolTipText("Circle size.");
		spinPoints.setPreferredSize(dim60);
		spinPoints.setFont(monoFont);
		spinPoints.addChangeListener(new SpinPointsListener());
		
		cbColor = new JCheckBox("Color");
		cbColor.setToolTipText("Colors for the plot points.");
		cbColor.setFont(lblFont);
		cbColor.setSelected(true);
		cbMarks = new JCheckBox("Marks");
		cbMarks.setFont(lblFont);
		cbMarks.setToolTipText("Marks for the plot.");
		cbGrid = new JCheckBox("Grid");
		cbGrid.setFont(lblFont);
		cbGrid.setToolTipText("Grid for the plot.");
		cbText = new JCheckBox("Text");
		cbText.setFont(lblFont);
		cbText.setToolTipText("Text for the plot.");
		
		MyComboBox comboOptions = new MyComboBox(new JCheckBox[] {cbColor, cbMarks, cbGrid, cbText});
		comboOptions.setToolTipText("Plot options.");
		comboOptions.setPreferredSize(dim100);
		
		JButton btnSaveTeX = new JButton("TeX");
		btnSaveTeX.setToolTipText("Plot as TeX file.");
		btnSaveTeX.setPreferredSize(dim60);
		btnSaveTeX.setFont(lblFont);
		btnSaveTeX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveTeXListener(evt);
			}
		});
		
		JButton btnSavePNG = new JButton("PNG");
		btnSavePNG.setToolTipText("Plot as PNG image.");
		btnSavePNG.setPreferredSize(dim60);
		btnSavePNG.setFont(lblFont);
		btnSavePNG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSavePNGListener(evt);
			}
		});
		
		/// Panel 1a
		JPanel panel1a = new JPanel();
		panel1a.setBackground(Color.WHITE);
		panel1a.setLayout(flowLayout);
		panel1a.add(btnOpenFile);
		panel1a.add(btnSaveFile);
		panel1a.add(btnSaveDraft);
		
		/// Panel 1b
		JPanel panel1b = new JPanel();
		panel1b.setBackground(Color.WHITE);
		panel1b.setLayout(flowLayout);
		panel1b.add(lblMethod);
		panel1b.add(comboMethod);
		panel1b.add(lblMatCnt);
		panel1b.add(spinMatCnt);
		
		/// Panel 1c
		JPanel panel1c = new JPanel();
		panel1c.setBackground(Color.WHITE);
		panel1c.setLayout(flowLayout);
		panel1c.add(lblVariant);
		panel1c.add(comboVariant);
		panel1c.add(comboChk);
		
		/// Panel 1d
		JPanel panel1d = new JPanel();
		panel1d.setBackground(Color.WHITE);
		panel1d.setLayout(flowLayout);
		panel1d.add(btnAnalysis);
		
		/// Panel 1
		JPanel panel1 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel1.setLayout(new GridLayout(4,0));
		panel1.add(panel1a);
		panel1.add(panel1b);
		panel1.add(panel1c);
		panel1.add(panel1d);
		
		/// Panel 2a
		JPanel panel2a = new JPanel();
		panel2a.setBackground(Color.WHITE);
		panel2a.setLayout(flowLayout);
		panel2a.add(lblAlpha);
		panel2a.add(spinAlpha);
		panel2a.add(lblDigits);
		panel2a.add(spinDigits);
		panel2a.add(lblTable1);
		panel2a.add(comboTable1);
		panel2a.add(btnExport);
		panel2a.add(btnInfo);
		
		/// Panel 2b
		JPanel panel2b = new JPanel();
		panel2b.setBackground(Color.WHITE);
		panel2b.setLayout(flowLayout);
		panel2b.add(lblBeta);
		panel2b.add(spinBeta);
		panel2b.add(lblWidth);
		panel2b.add(spinWidth);
		panel2b.add(lblTable2);
		panel2b.add(comboTable2);
		panel2b.add(btnScreen);
		panel2b.add(btnAbout);
		
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
		panel3.add(comboOptions);
		panel3.add(btnSaveTeX);
		panel3.add(btnSavePNG);
		
		
		/// Items for text areas
		textA = new MyTextArea("# Open file or copy/paste data here" + ls +
				"# Column separators: tab semicolon comma" + ls +
				"# Recognized numbers: 1.7 and 1,7" + ls + ls +
				"x;E;F;G;H;I" + ls +
				"A;6;5;3;7;6" + ls +
				"B;7;7;8;1;3" + ls +
				"C;4;3;5;9;1" + ls +
				"D;4;5;6;7;8" + ls + ls);
		
		textB = new JTextArea();
		textB.setFont(monoFont2);
		textB.setEditable(false);
		showMessage("ICrAData v1.8");
		
		JScrollPane scrollTextA = new JScrollPane(textA);
		scrollTextA.setMinimumSize(dim0);
		scrollTextA.setPreferredSize(new Dimension(425, 425));
		scrollTextA.setBorder(null);
		
		JScrollPane scrollTextB = new JScrollPane(textB);
		scrollTextB.setMinimumSize(dim0);
		scrollTextB.setPreferredSize(new Dimension(425, 100));
		scrollTextB.setBorder(null);
		
		scrollA = new JScrollPane();
		scrollA.setMinimumSize(dim0);
		scrollA.setPreferredSize(scrollSize);
		scrollA.getViewport().addChangeListener(new ScrollAListener());
		scrollA.setBorder(null);
		
		scrollB = new JScrollPane();
		scrollB.setMinimumSize(dim0);
		scrollB.setPreferredSize(scrollSize);
		scrollB.getViewport().addChangeListener(new ScrollBListener());
		scrollB.setBorder(null);
		
		resScroll = new JScrollPane();
		resScroll.setMinimumSize(dim0);
		resScroll.setPreferredSize(new Dimension(400, 400));
		resScroll.getViewport().setBackground(Color.WHITE);
		resScroll.setBorder(null);
		
		/// Panel text areas
		JPanel panelAreas = new JPanel();
		panelAreas.setMinimumSize(dim0);
		panelAreas.setLayout(new BorderLayout());
		panelAreas.add(panel1, BorderLayout.NORTH);
		panelAreas.add(scrollTextA, BorderLayout.CENTER);
		
		/// Split text areas
		JSplitPane splitAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panelAreas, scrollTextB);
		splitAreas.setMinimumSize(dim0);
		splitAreas.setOneTouchExpandable(true);
		splitAreas.setResizeWeight(1.0);
		splitAreas.setDividerSize(10);
		
		/// Split tables
		JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollA, scrollB);
		splitTables.setMinimumSize(dim0);
		splitTables.setOneTouchExpandable(true);
		splitTables.setResizeWeight(0.5);
		splitTables.setDividerSize(10);
		
		/// Panel tables
		JPanel panelTables = new JPanel();
		panelTables.setMinimumSize(dim0);
		panelTables.setLayout(new BorderLayout());
		panelTables.add(panel2, BorderLayout.NORTH);
		panelTables.add(splitTables, BorderLayout.CENTER);
		
		/// Panel graphic
		JPanel panelGraphic = new JPanel();
		panelGraphic.setMinimumSize(dim0);
		panelGraphic.setLayout(new BorderLayout());
		panelGraphic.add(panel3, BorderLayout.NORTH);
		panelGraphic.add(resScroll, BorderLayout.CENTER);
		
		/// Split left-center
		JSplitPane splitLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitAreas, panelTables);
		splitLeft.setMinimumSize(dim0);
		splitLeft.setOneTouchExpandable(true);
		splitLeft.setResizeWeight(0.0);
		splitLeft.setDividerSize(10);
		
		/// Split center-right
		JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitLeft, panelGraphic);
		splitRight.setMinimumSize(dim0);
		splitRight.setOneTouchExpandable(true);
		splitRight.setResizeWeight(1.0);
		splitRight.setDividerSize(10);
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(splitRight, BorderLayout.CENTER);
		
		setTitle("ICrAData v1.8");
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
	private void showMessage(String msg) {
		textB.append(msgDate.format(new Date()) + " " + msg + ls);
		textB.setCaretPosition(textB.getText().length());
	}
	
	/// Prompt on window closing
	private void exitListener(WindowEvent evt) {
		if (JOptionPane.showConfirmDialog(
				this, "Exit the application?", "Exit",
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
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("CSV file (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File openFile = chOpen.getSelectedFile();
		ud = openFile.getParent();
		
		try {
			FileInputStream fiX = new FileInputStream(openFile);
			InputStreamReader isX = new InputStreamReader(fiX, "UTF8");
			BufferedReader reX = new BufferedReader(isX);
			
			String line = reX.readLine();
			if (line.trim().startsWith("#icradata")) {
				String[] arr = line.trim().substring(9).trim().split(" ");
				if (arr.length == 6) {
					//for (int i = 0; i < arr.length; i++)
					//	System.out.println("elem " + i + " " + arr[i]);
					
					if (checkInt(arr[0])) {
						int val = Integer.parseInt(arr[0]);
						if (val >= 0 && val <= 4)
							comboMethod.setSelectedIndex(val);
					}
					
					if (checkInt(arr[1])) {
						int val = Integer.parseInt(arr[1]);
						if (val >= 1 && val <= 10000)
							spinMatCnt.setValue(val);
					}
					
					if (checkInt(arr[2])) {
						if (Integer.parseInt(arr[2]) == 1)
							chkRowNames.setSelected(true);
						else
							chkRowNames.setSelected(false);
					}
					
					if (checkInt(arr[3])) {
						if (Integer.parseInt(arr[3]) == 1)
							chkColNames.setSelected(true);
						else
							chkColNames.setSelected(false);
					}
					
					if (checkInt(arr[4])) {
						if (Integer.parseInt(arr[4]) == 1)
							chkTranspose.setSelected(true);
						else
							chkTranspose.setSelected(false);
					}
					
					if (checkInt(arr[5])) {
						if (Integer.parseInt(arr[5]) == 1)
							chkOrdPair.setSelected(true);
						else
							chkOrdPair.setSelected(false);
					}
				}
			}
			
			String str = line + ls;
			while ( (line = reX.readLine()) != null )
				str += line + ls;
			reX.close();
			
			textA.setText(str);
			showMessage("Opened file " + openFile.getName() + " (" + openFile + ")");
			
		} catch (Exception ex) {
			showMessage("[Error] Could not open file " + openFile.getName() + " (" + openFile + ")");
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
				FileOutputStream foX = new FileOutputStream(saveFile, false);
				OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
				BufferedWriter wrX = new BufferedWriter(osX);
				
				wrX.write(saveText());
				wrX.close();
				showMessage("Saved file " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("[Error] Could not save file " + saveFile.getName() + " (" + saveFile + ")");
			}
		} else
			showMessage("No text in the input panel");
	}
	
	/// Save draft
	private void btnSaveDraftListener(ActionEvent evt) {
		
		if (textA.getText().length() > 0) {
			(new File(draftDir)).mkdir();
			File saveFile = new File(draftDir + fs + "ICrAData-" + draftDate.format(new Date()) + ".txt");
			
			try {
				FileOutputStream foX = new FileOutputStream(saveFile, false);
				OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
				BufferedWriter wrX = new BufferedWriter(osX);
				
				wrX.write(saveText());
				wrX.close();
				showMessage("Saved draft " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("[Error] Could not save draft " + saveFile.getName() + " (" + saveFile + ")");
			}
			
		} else
			showMessage("No text in the input panel");
	}
	
	/// Save the text with parameters
	private String saveText() {
		
		String res = "#icradata " + comboMethod.getSelectedIndex() + " " +
				(int)spinMatCnt.getValue() + " " +
				(chkRowNames.isSelected() ? 1 : 0) + " " +
				(chkColNames.isSelected() ? 1 : 0) + " " +
				(chkTranspose.isSelected() ? 1 : 0) + " " +
				(chkOrdPair.isSelected() ? 1 : 0) + "\r\n";
		
		String[] data = textA.getText().split(ls);
		int st = (data[0].trim().startsWith("#icradata") ? 1 : 0);
		
		for (int i = st; i < data.length; i++)
			res += data[i] + "\r\n";
		
		return res;
	}
	
	/// Export table
	private String saveTable(int valT, int valC, int valD, int digits) {
		
		/// Make 0.0000, must be with dot, not with comma
		String strDig = "0.";
		for (int i = 0; i < digits; i++)
			strDig += "0";
		
		/// DecimalFormat usFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
		/// DecimalFormat bgFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(new Locale("bg", "BG")));
		/// Locale[] locale = new Locale[] {Locale.US, new Locale("bg", "BG")};
		DecimalFormat decFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(
				(valD == 0 ? Locale.US : new Locale("bg", "BG")) ));
		
		String sep = "\t";
		switch (valC) {
		case 1: sep = ";"; break;
		case 2: sep = ","; break;
		case 3: sep = " & "; break;
		default: sep = "\t"; break;
		}
		
		String eol = ls;
		if (valC == 3)
			eol = " \\\\ " + ls;
		
		/// StringBuilder is created much faster than String
		StringBuilder res = new StringBuilder();
		
		if (valT == 0) { /// mu/nu upper/lower
			
			res.append(MU + "/" + NU + "-upper/lower" + sep);
			for (int j = 0; j < hdrs.length; j++) {
				res.append(hdrs[j]);
				if (j != hdrs.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < result.length; i++) {
				res.append(hdrs[i] + sep);
				for (int j = 0; j < result[i].length; j++) {
					res.append(decFormat.format(result[i][j]));
					if (j != result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		} else if (valT == 1) { /// (mu;nu) table
			
			res.append("(" + MU + ";" + NU + ")-table" + sep);
			for (int j = 0; j < hdrs.length; j++) {
				res.append(hdrs[j]);
				if (j != hdrs.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < result.length; i++) {
				res.append(hdrs[i] + sep);
				for (int j = 0; j < result[i].length; j++) {
					if (i < j)
						res.append("(" + decFormat.format(result[i][j]) + ";" + decFormat.format(result[j][i]) + ")");
					else if (i > j)
						res.append("(" + decFormat.format(result[j][i]) + ";" + decFormat.format(result[i][j]) + ")");
					else
						res.append("(0;0)");
					
					if (j != result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		} else if (valT == 2) { /// mu table
			
			res.append(MU + "-table" + sep);
			for (int j = 0; j < hdrs.length; j++) {
				res.append(hdrs[j]);
				if (j != hdrs.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < result.length; i++) {
				res.append(hdrs[i] + sep);
				for (int j = 0; j < result[i].length; j++) {
					if (i < j)
						res.append(decFormat.format(result[i][j]));
					else if (i > j)
						res.append(decFormat.format(result[j][i]));
					else
						res.append(decFormat.format(0));
					
					if (j != result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		} else if (valT == 3) { /// nu table
			
			res.append(NU + "-table" + sep);
			for (int j = 0; j < hdrs.length; j++) {
				res.append(hdrs[j]);
				if (j != hdrs.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < result.length; i++) {
				res.append(hdrs[i] + sep);
				for (int j = 0; j < result[i].length; j++) {
					if (i < j)
						res.append(decFormat.format(result[j][i]));
					else if (i > j)
						res.append(decFormat.format(result[i][j]));
					else
						res.append(decFormat.format(0));
					
					if (j != result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		} else if (valT == 4) { /// vector upper
			
			res.append("vector-upper" + sep + MU + sep + NU + sep + "row" + sep + "col" + eol);
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < result[i].length; j++) {
					if (i < j) {
						res.append(hdrs[i] + "-" + hdrs[j] + sep +
							decFormat.format(result[i][j]) + sep +
							decFormat.format(result[j][i]) + sep +
							(i+1) + sep + (j+1) + eol);
					}
				}
			}
			
		} else if (valT == 5) { /// vector lower
			
			res.append("vector-lower" + sep + MU + sep + NU + sep + "row" + sep + "col" + eol);
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < result[i].length; j++) {
					if (i > j) {
						res.append(hdrs[i] + "-" + hdrs[j] + sep +
							decFormat.format(result[j][i]) + sep +
							decFormat.format(result[i][j]) + sep +
							(i+1) + sep + (j+1) + eol);
					}
				}
			}
			
		}
		
		return res.toString();
	}
	
	/// Export data
	private void btnExportListener(ActionEvent evt) {
		
		try {
			if (result != null) {
				final JDialog expFrame = new JDialog(this, "Export", true);
				
				Dimension dim60 = new Dimension(60, 25);
				Dimension dim100 = new Dimension(100, 25);
				Dimension dim150 = new Dimension(135, 25);
				FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
				
				JLabel lblTable = new JLabel("Table");
				lblTable.setHorizontalAlignment(SwingConstants.RIGHT);
				lblTable.setPreferredSize(dim60);
				lblTable.setFont(lblFont);
				
				JLabel lblColSep = new JLabel("ColSep");
				lblColSep.setHorizontalAlignment(SwingConstants.RIGHT);
				lblColSep.setPreferredSize(dim60);
				lblColSep.setFont(lblFont);
				
				JLabel lblDecSep = new JLabel("DecSep");
				lblDecSep.setHorizontalAlignment(SwingConstants.RIGHT);
				lblDecSep.setPreferredSize(dim60);
				lblDecSep.setFont(lblFont);
				
				JLabel lblDigits = new JLabel("Digits");
				lblDigits.setHorizontalAlignment(SwingConstants.RIGHT);
				lblDigits.setPreferredSize(dim60);
				lblDigits.setFont(lblFont);
				
				final JComboBox<String> comboTable = new JComboBox<String>(new String[] {
						MU + "/" + NU + " upper/lower", "(" + MU + ";" + NU + ") table",
						MU + " table", NU + " table", "vector upper", "vector lower"});
				comboTable.setToolTipText("Display tables.");
				comboTable.setPreferredSize(dim150);
				comboTable.setFont(lblFont);
				comboTable.setMaximumRowCount(13);
				
				final JComboBox<String> comboColSep = new JComboBox<String>(
						new String[] {"Tab \\t", "Semicolon ;", "Comma ,", "TeX &"});
				comboColSep.setToolTipText("Column separator.");
				comboColSep.setPreferredSize(dim150);
				comboColSep.setFont(lblFont);
				
				final JComboBox<String> comboDecSep = new JComboBox<String>(
						new String[] {"Point .", "Comma ,"});
				comboDecSep.setToolTipText("Decimal separator.");
				comboDecSep.setPreferredSize(dim150);
				comboDecSep.setFont(lblFont);
				
				final JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,16,1));
				spinDigits.setToolTipText("Digits after the decimal separator.");
				spinDigits.setPreferredSize(dim60);
				spinDigits.setFont(lblFont);
				
				JButton btnExport = new JButton("Export");
				btnExport.setToolTipText("Export table.");
				btnExport.setPreferredSize(dim100);
				btnExport.setFont(lblFont);
				btnExport.addActionListener(new ActionListener() {
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
							FileOutputStream foX = new FileOutputStream(saveFile, false);
							OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
							BufferedWriter wrX = new BufferedWriter(osX);
							
							wrX.write(saveTable(comboTable.getSelectedIndex(), comboColSep.getSelectedIndex(),
									comboDecSep.getSelectedIndex(), (int)spinDigits.getValue()).replace(ls, "\r\n"));
							wrX.close();
							showMessage("Saved table " + saveFile.getName() + " (" + saveFile + ")");
							
						} catch (Exception ex) {
							showMessage("[Error] Could not save table " + saveFile.getName() + " (" + saveFile + ")");
						}
					}
				});
				
				JButton btnClose = new JButton("Close");
				btnClose.setToolTipText("Close window.");
				btnClose.setPreferredSize(dim100);
				btnClose.setFont(lblFont);
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						expFrame.setVisible(false);
					}
				});
				
				JPanel panel1 = new JPanel();
				panel1.setLayout(flowLayout);
				panel1.add(lblTable);
				panel1.add(comboTable);
				
				JPanel panel2 = new JPanel();
				panel2.setLayout(flowLayout);
				panel2.add(lblColSep);
				panel2.add(comboColSep);
				
				JPanel panel3 = new JPanel();
				panel3.setLayout(flowLayout);
				panel3.add(lblDecSep);
				panel3.add(comboDecSep);
				
				JPanel panel4 = new JPanel();
				panel4.setLayout(flowLayout);
				panel4.add(lblDigits);
				panel4.add(spinDigits);
				
				JPanel panel5 = new JPanel();
				panel5.setLayout(flowLayout);
				panel5.add(btnExport);
				panel5.add(btnClose);
				
				expFrame.setLayout(new GridLayout(5,0));
				expFrame.add(panel1);
				expFrame.add(panel2);
				expFrame.add(panel3);
				expFrame.add(panel4);
				expFrame.add(panel5);
				
				expFrame.setResizable(false);
				expFrame.pack();
				//expFrame.setLocationByPlatform(true);
				expFrame.setLocationRelativeTo(null);
				expFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				expFrame.setVisible(true);
			}
			
		} catch (Exception ex) {
			showMessage("[Error] Could not export data");
		}
	}
	
	/// Information for the application
	private void btnInfoListener(ActionEvent evt) {
		
		JFrame infoFrame = new JFrame();
		MyTextArea text = new MyTextArea(
				"InterCriteria Analysis Data" + ls + ls +
				">>> Left panel" + ls +
				"Open text file or comma separated values file." + ls +
				"Open MS Excel/LibreOffice Calc and copy/paste the table with optional headers." + ls +
				"Select Row and Column names if header was copied, or type after #rownames and #colnames." + ls +
				"Markers #input1 and #input2 are required for Ordered Pair." + ls + ls +
				">>> Center panel" + ls +
				"Value " + AL + " is from 0.5 to 1, value " + BE + " is from 0 to 0.5, both with 0.01 increment." + ls +
				"When " + MU + " > " + AL + " and " + NU + " < " + BE + ", that is Positive Consonance, color is green." + ls +
				"When " + MU + " < " + BE + " and " + NU + " > " + AL + ", that is Negative Consonance, color is red." + ls +
				"In all other cases, that is Dissonance, color is magenta." + ls + ls +
				">>> Right panel" + ls +
				"Zoom plot by changing the selector 400 pixels. Circle size default is 5." + ls +
				"Buttons TeX/PNG save the graphic from the panel in the respective format." + ls + ls +
				"Use Java 64-bit on Windows 64-bit. Check from Control Panel -> Java." + ls +
				"Download from: https://github.com/ojdkbuild/ojdkbuild" + ls +
				"Direct link: https://github.com/ojdkbuild/ojdkbuild/releases/download/java-11-openjdk-11.0.8.10-1/" +
				"java-11-openjdk-11.0.8.10-1.windows.ojdkbuild.x86_64.msi" + ls + ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		infoFrame.setTitle("Information");
		infoFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		infoFrame.add(scrollText);
		
		infoFrame.pack();
		infoFrame.setLocationByPlatform(true);
		infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		infoFrame.setVisible(true);
	}
	
	/// About the application
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
				"  ICrAData - Software for InterCriteria Analysis," + ls +
				"  International Journal Bioautomation, Vol. 22(1), 2018, 1-10." + ls + ls +
				"This software application has been developed with the partial financial support of:" + ls +
				"  Changes in versions from 1.3 to 1.8 have been implemented for" + ls +
				"    project DN 17/06 ``A New Approach, Based on an Intercriteria Data Analysis," + ls +
				"    to Support Decision Making in 'in silico' Studies of Complex Biomolecular Systems''," + ls +
				"    funded by the National Science Fund of Bulgaria." + ls +
				"  Changes in versions from 0.9.6 to 1.2 have been implemented for" + ls +
				"    project DFNI-I-02-5 ``InterCriteria Analysis: A New Approach to Decision Making''," + ls +
				"    funded by the National Science Fund of Bulgaria." + ls + ls + ls +
				"InterCriteria Analysis Data" + ls +
				"  Version: 1.8" + ls +
				"  Date: January 9, 2021" + ls +
				"  Compiled by: OpenJDK 11.0.8 (javac --release 7 *.java)" + ls + ls);
		
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
	
	/// Export the screen
	private void btnScreenListener(ActionEvent evt) {
		
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
			BufferedImage img = new BufferedImage(
					this.getContentPane().getWidth(), this.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			this.getContentPane().printAll(g2);
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
			showMessage("Saved screen " + chName.getName() + " (" + chName + ")");
			
		} catch (Exception ex) {
			showMessage("[Error] Could not save screen " + chName.getName());
		}
	}
	
	/// Export the graphic as PNG image
	private void btnSavePNGListener(ActionEvent evt) {
		
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
				showMessage("Saved PNG image " + chName.getName() + " (" + chName + ")");
				
			} catch (Exception ex) {
				showMessage("[Error] Could not save PNG image " + chName.getName());
			}
		}
	}
	
	/// Export the graphic as TeX file
	private void btnSaveTeXListener(ActionEvent evt) {
		
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
				FileOutputStream foX = new FileOutputStream(chName, false);
				OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
				BufferedWriter wrX = new BufferedWriter(osX);
				
				wrX.write(exportTeX().replace(ls, "\r\n"));
				wrX.close();
				showMessage("Saved TeX file " + chName.getName() + " (" + chName + ")");
				
			} catch (Exception ex) {
				showMessage("[Error] Could not save TeX file " + chName.getName() + " (" + chName + ")");
			}
		}
	}
	
	/// Color as TeX string
	private String exportColor(double mu, double nu, double a, double b) {
		if (mu > a && nu < b)
			return "\\color{posc}"; /// positive consonance
		else if (mu < b && nu > a)
			return "\\color{negc}"; /// negative consonance
		else
			return "\\color{diss}"; /// dissonance
	}
	
	/// Export to TeX
	private String exportTeX() {
		
		double chLimA = (double)spinAlpha.getValue();
		double chLimB = (double)spinBeta.getValue();
		double chPnts = (double)(0.05)*(int)(spinPoints.getValue());
		boolean chClrs = cbColor.isSelected();
		boolean chTcks = cbMarks.isSelected();
		boolean chGrid = cbGrid.isSelected();
		boolean chText = cbText.isSelected();
		
		/// Colors 0  Ticks 1  Grid 2  Text 3
		StringBuilder res = new StringBuilder();
		
		/// picture \makebox(0,0)[cc]{$Z_1$}   c r l b t
		/// http://www.emerson.emory.edu/services/latex/latex_51.html
		/// https://tex.stackexchange.com/questions/32791/picture-environment-rotating-text
		res.append("%%% ICrAData TeX Export " + logDate.format(new Date()) + ls +
			"\\documentclass[11pt]{article}" + ls +
			(chText ? "\\usepackage{graphicx}" + ls : "") +
			(chClrs ? "\\usepackage{xcolor}" + ls : "") +
			"\\begin{document}" + ls +
			"\\thispagestyle{empty}" + ls + ls +
			"%%% Change unitlength and font size to scale the graphic" + ls +
			"%%% Font sizes: \\tiny \\scriptsize \\footnotesize \\small \\normalsize \\large \\Large \\LARGE \\huge \\Huge" + ls +
			"\\begin{center}" + ls +
			(chClrs ?
				"\\definecolor{posc}{RGB}{0, 160, 0}" + ls +
				"\\definecolor{negc}{RGB}{255, 64, 64}" + ls +
				"\\definecolor{diss}{RGB}{255, 64, 255}" + ls
				: "") +
			"\\newcommand{\\myrad}{" + chPnts + "}" + ls +
			(chText ? "\\newcommand{\\myticks}{\\scriptsize}" + ls +
				"\\newcommand{\\mytext}{\\normalsize}" + ls : "") +
			"\\setlength{\\unitlength}{20pt} %10pt=4mm" + ls +
			"\\linethickness{0.5pt}" + ls +
			"\\begin{picture}" +
			(chText ? "(11.5,11.5)(-1.5,-1.5)" + ls : "(10,10)" + ls) +
			"\\put(0,0){\\line(0,1){10}}" + ls +
			"\\put(0,0){\\line(1,0){10}}" + ls +
			"\\put(10,0){\\line(-1,1){10}}" + ls);
		
		/// Ticks
		if (chTcks && !chGrid) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + ls);
		}
		
		/// Grid
		if (chGrid) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){" + Integer.valueOf(10 - i) + ".15}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){" + Integer.valueOf(10 - i) + ".15}}" + ls);
		}
		
		/// Text
		if (chText) {
			res.append("\\put(5,-1.2){\\makebox(0,0)[cc]{\\mytext Degree of agreement, $\\mu$}}" + ls +
				"\\put(-1.3,5){\\makebox(0,0)[cc]{\\rotatebox{90}{\\mytext Degree of disagreement, $\\nu$}}}" + ls +
				"\\put(0,-0.4){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(10,-0.4){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls +
				"\\put(-0.33,0){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(-0.33,10){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls);
			for (int i = 1; i < 10; i++)
				res.append(
					"\\put(" + i + ",-0.4){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls +
					"\\put(-0.5," + i + "){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls);
		}
		
		/// Points
		for (int i = 0; i < result.length; i++) {
			for (int j = i+1; j < result[0].length; j++) {
				res.append("\\put(" + result[i][j]*10 + "," + result[j][i]*10 + "){" +
					(chClrs ? exportColor(result[i][j],result[j][i],chLimA,chLimB) : "") +
					"\\circle*{\\myrad}}" + ls);
			}
		}
		
		res.append("\\end{picture}" + ls +
			"\\end{center}" + ls + ls +
			"\\end{document}" + ls);
		
		return res.toString();
	}
	
	/// Refresh the tables and the graphic
	private void refreshTable() {
		
		if (result != null) {
			/// Refresh the tables
			scrollA.setViewportView(new MyTable(result, hdrs, MU, comboTable1.getSelectedIndex()));
			scrollB.setViewportView(new MyTable(result, hdrs, NU, comboTable2.getSelectedIndex()));
			
			/// Refresh the graphic
			MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
			resPanel.repaint();
			resPanel.revalidate();
		}
	}
	
	/// Refresh the graphic, fired from ActionListener in MyComboBox
	private void refreshGraphic() {
		
		if (result != null) {
			MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
			resPanel.setMyOptions(new boolean[] {
					cbColor.isSelected(), cbMarks.isSelected(),
					cbGrid.isSelected(), cbText.isSelected()});
			resPanel.repaint();
			resPanel.revalidate();
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
		}
	}
	
	/// Spinner for column width
	private class SpinWidthListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			
			if (result != null) {
				MyTable tableA = (MyTable)scrollA.getViewport().getView();
				MyTable tableB = (MyTable)scrollB.getViewport().getView();
				
				int val = (int) ((JSpinner)evt.getSource()).getValue();
				int cols = tableA.getTableHeader().getColumnModel().getColumnCount();
				for (int i = 0; i < cols; i++) {
					tableA.getTableHeader().getColumnModel().getColumn(i).setPreferredWidth(val);
					tableA.getTableHeader().getColumnModel().getColumn(i).setMinWidth(val);
					tableA.getTableHeader().getColumnModel().getColumn(i).setMaxWidth(val);
					tableB.getTableHeader().getColumnModel().getColumn(i).setPreferredWidth(val);
					tableB.getTableHeader().getColumnModel().getColumn(i).setMinWidth(val);
					tableB.getTableHeader().getColumnModel().getColumn(i).setMaxWidth(val);
				}
			}
		}
	}
	
	/// Selector for ICrA method
	private class ComboMethodListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboMethod.getSelectedIndex() == 0) {
					spinMatCnt.setValue((int)1);
					spinMatCnt.setEnabled(false);
				} else {
					spinMatCnt.setEnabled(true);
				}
			}
		}
	}
	
	/// Selector for Table1
	private class ComboTable1Listener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null)
					scrollA.setViewportView(new MyTable(result, hdrs, MU, comboTable1.getSelectedIndex()));
			}
		}
	}
	
	/// Selector for Table2
	private class ComboTable2Listener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null)
					scrollB.setViewportView(new MyTable(result, hdrs, NU, comboTable2.getSelectedIndex()));
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
				
				if (valSize < 100 || valSize > 10000) {
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
				resPanel.setMyPoint(pointSize);
				resPanel.repaint();
				resPanel.revalidate();
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
					Thread.sleep(15*60*1000); /// 1000=1s
					btnSaveDraftListener(null);
				}
			} catch(Exception ex) {
				showMessage("[Error] Automatic draft saving failed");
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
			mCut.setText("Cut");
			mCopy.setText("Copy");
			mPaste.setText("Paste");
			mUndo.setText("Undo");
			mRedo.setText("Redo");
			
			JSeparator mSep = new JSeparator();
			
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
		
		private Rectangle[][] arrRect;
		private boolean[] plotOptions;
		private double plotPoint = 0;
		private int plotMarkRow = -1;
		private int plotMarkCol = -1;
		
		private MyPanel() {
			setBackground(Color.WHITE);
			addMouseListener(this);
			addMouseMotionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		private void setMyOptions(boolean[] val) {
			plotOptions = val;
		}
		
		private void setMyPoint(double val) {
			plotPoint = val;
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
			g2.setFont(lblFont2);
			
			int width = getWidth();
			int height = getHeight();
			double delta = 20;
			if (plotOptions[3])
				delta = 60;
			
			/// Grid
			g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
			g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
			g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			
			/// Scale so that there are boundaries on the graph
			double widthScale = width-2*delta;
			double heightScale = height-2*delta;
			double line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			
			/// Ticks
			if (plotOptions[1] && !plotOptions[2]) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
				
			}
			
			/// Grid
			if (plotOptions[2]) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+widthScale*marks[i], delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], delta+heightScale*marks[i], delta+widthScale*marks[i], height-delta+line));
				
			}
			
			/// Text
			if (plotOptions[3]) {
				for (int i = 0; i < marks.length; i++) {
					int widthMark = g2.getFontMetrics(this.getFont()).stringWidth(String.valueOf(marks[marks.length-1-i]));
					g2.drawString(String.valueOf(marks[marks.length-1-i]), (int)(delta-3.5*line-widthMark/2), (int)(delta+heightScale*marks[i]+1.2*line));
				}
				
				for (int i = 0; i < marks.length; i++) {
					int widthMark = g2.getFontMetrics(this.getFont()).stringWidth(String.valueOf(marks[i]));
					g2.drawString(String.valueOf(marks[i]), (int)(delta+widthScale*marks[i]-widthMark/2), (int)(height-delta+4.0*line));
				}
				
				//g2.drawString("0", (int)(delta-3.0*line), (int)(delta+heightScale+1.2*line));
				//g2.drawString("1", (int)(delta-3.0*line), (int)(delta+1.2*line));
				
				//g2.drawString("0", (int)(delta-0.5*line), (int)(delta+heightScale+4.0*line));
				//g2.drawString("1", (int)(delta+widthScale-0.5*line), (int)(delta+heightScale+4.0*line));
				
				//int widthMU = g2.getFontMetrics(this.getFont()).stringWidth("Degree of agreement, " + MU);
				//int widthNU = g2.getFontMetrics(this.getFont()).stringWidth("Degree of disagreement, " + NU);
				//g2.drawString("Degree of agreement, " + MU, (int)(delta+widthScale*marks[4]-widthMU/2), (int)(height-delta+10*line));
				g2.drawString("Degree of agreement, " + MU, (int)(delta), (int)(height-delta+10*line));
				
				AffineTransform g2Orig = g2.getTransform();
				g2.rotate(Math.toRadians(-90));
				//g2.drawString("Degree of disagreement, " + NU, (int)(-(delta+heightScale*marks[4]+widthNU/2)), (int)(delta-9*line));
				g2.drawString("Degree of disagreement, " + NU, (int)(-(delta+heightScale)), (int)(delta-9*line));
				g2.setTransform(g2Orig);
			}
			
			/// Colors
			//g2.setPaint(Color.RED);
			g2.setColor(Color.BLACK);
			
			int rows = result.length;
			int cols = result[0].length;
			
			/// Save rectangles for glass
			arrRect = new Rectangle[rows][cols];
			double radius = plotPoint;
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					/// The colors
					if (plotOptions[0]) {
						if (result[i][j] > (double)spinAlpha.getValue() &&
							result[j][i] < (double)spinBeta.getValue())
							g2.setColor(poscClr);
						else if (result[i][j] < (double)spinBeta.getValue() &&
								result[j][i] > (double)spinAlpha.getValue())
							g2.setColor(negcClr);
						else
							g2.setColor(dissClr);
					}
					
					/// The points
					double x = result[i][j]*widthScale+delta;
					double y = heightScale-result[j][i]*heightScale+delta;
					arrRect[i][j] = new Rectangle((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
					g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				}
			}
			
			/// All points from that column - mark in blue - use ICrAData.markCol
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					if (markCol > -1 && (j == markCol || i == markCol)) {
						double x = result[i][j]*widthScale+delta;
						double y = heightScale-result[j][i]*heightScale+delta;
						g2.setColor(Color.BLUE);
						g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
					}
				}
			}
			
			/// Cell that has focus - mark in red - use ICrAData.MyPanel.plotMarkRow/plotMarkCol
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					if ( plotMarkRow > -1 && plotMarkCol > -1 &&
							((i == plotMarkRow && j == plotMarkCol) || (j == plotMarkRow && i == plotMarkCol)) ) {
						double x = result[i][j]*widthScale+delta;
						double y = heightScale-result[j][i]*heightScale+delta;
						g2.setColor(Color.CYAN);
						g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
					}
				}
			}
			
		}
		
		/// Set tool-tip text
		public String getToolTipText(MouseEvent evt) {
			
			for (int i = 0; i < arrRect.length; i++) {
				for (int j = i+1; j < arrRect[0].length; j++) {
					if (arrRect[i][j].contains(evt.getX(), evt.getY())) {
						return "<html>Row: " + hdrs[(int)Math.round(i)] +
								"<br/>Column: " + hdrs[(int)Math.round(j)] + 
								"<br/>" + MU + ": " + numFormat.format(result[i][j]) +
								"<br/>" + NU + ": " + numFormat.format(result[j][i]) +
								"</html>";
					}
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
					for (int j = i+1; j < arrRect[0].length; j++) {
						if (arrRect[i][j].contains(evt.getX(), evt.getY())) {
							MyTable tableA = (MyTable)scrollA.getViewport().getView();
							MyTable tableB = (MyTable)scrollB.getViewport().getView();
							
							/// Rows and columns have index from 0, 1, 2, 3, etc
							/// Rows: the column table header is not counted towards the rows, therefore it is correct
							/// Columns: We have row table header, therefore the column is +1
							/// Wrong cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3])
							/// Correct cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1)
							Rectangle cellA = tableA.getCellRect((int)Math.round(i), (int)Math.round(j+1), false);
							Rectangle cellB = tableB.getCellRect((int)Math.round(i), (int)Math.round(j+1), false);
							
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
		
		private String[][] wdata;
		private String[] rhead;
		private String[] chead;
		
		private Action actCopy = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				saveTableData();
			}
		};
		
		private MyTable(double[][] matR, String[] headers, String name, int disp) {
			
			int rows = matR.length;
			int cols = matR[0].length;
			
			wdata = new String[rows][cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					
					if (disp == 0) { /// MU and NU
						if (i < j) /// upper triangular
							wdata[i][j] = numFormat.format(matR[i][j]);
						else if (i > j) /// lower triangular
							wdata[i][j] = numFormat.format(matR[i][j]);
						else if (i == j)
							wdata[i][j] = "---";
						
					} else if (disp == 1) { /// (MU;NU) table
						if (i < j) /// upper triangular
							wdata[i][j] = "(" + numFormat.format(matR[i][j]) + ";" + numFormat.format(matR[j][i]) + ")";
						else if (i > j) /// lower triangular
							wdata[i][j] = "(" + numFormat.format(matR[j][i]) + ";" + numFormat.format(matR[i][j]) + ")";
						else if (i == j)
							wdata[i][j] = "---";
						
					} else if (disp == 2) { /// MU table
						if (i < j) /// upper triangular
							wdata[i][j] = numFormat.format(matR[i][j]);
						else if (i > j) /// lower triangular
							wdata[i][j] = numFormat.format(matR[j][i]);
						else if (i == j)
							wdata[i][j] = "---";
						
					} else if (disp == 3) { /// NU table
						if (i < j) /// upper triangular
							wdata[i][j] = numFormat.format(matR[j][i]);
						else if (i > j) /// lower triangular
							wdata[i][j] = numFormat.format(matR[i][j]);
						else if (i == j)
							wdata[i][j] = "---";
						
					} else if (disp == 4) { /// distance to (1;0)
						if (i < j)
							wdata[i][j] = numFormat.format(Math.sqrt( (1-matR[i][j])*(1-matR[i][j]) + matR[j][i]*matR[j][i] ));
						else if (i > j)
							wdata[i][j] = numFormat.format(Math.sqrt( (1-matR[j][i])*(1-matR[j][i]) + matR[i][j]*matR[i][j] ));
						else
							wdata[i][j] = "---";
						
					} else if (disp == 5) { /// distance to (0;1)
						if (i < j)
							wdata[i][j] = numFormat.format(Math.sqrt( matR[i][j]*matR[i][j] + (1-matR[j][i])*(1-matR[j][i]) ));
						else if (i > j)
							wdata[i][j] = numFormat.format(Math.sqrt( matR[j][i]*matR[j][i] + (1-matR[i][j])*(1-matR[i][j]) ));
						else
							wdata[i][j] = "---";
						
					} else if (disp == 6) { /// distance to (0;0)
						if (i < j)
							wdata[i][j] = numFormat.format(Math.sqrt( matR[i][j]*matR[i][j] + matR[j][i]*matR[j][i] ));
						else if (i > j)
							wdata[i][j] = numFormat.format(Math.sqrt( matR[j][i]*matR[j][i] + matR[i][j]*matR[i][j] ));
						else
							wdata[i][j] = "---";
					}
				}
			}
			
			rhead = headers;
			chead = new String[headers.length + 1];
			chead[0] = name;
			for (int i = 0; i < headers.length; i++)
				chead[i+1] = headers[i];
			
			addMouseListener(this);
			
			setModel(new MyTableModel());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setAutoCreateRowSorter(false);
			setColumnSelectionAllowed(true);
			setRowSelectionAllowed(false);
			
			getTableHeader().setFont(monoFont);
			getTableHeader().setResizingAllowed(true);
			getTableHeader().setReorderingAllowed(false);
			//getTableHeader().getColumnModel().getColumn(2).setWidth(200);
			//setRowHeight((int)(getRowHeight()*sc));
			
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
				return wdata.length;
			}
			
			public int getColumnCount() {
				return chead.length;
			}
			
			public Object getValueAt(int row, int col) {
				if (col == 0)
					return rhead[row];
				else
					return wdata[row][col-1];
			}
			
			public Class<?> getColumnClass(int col) {
				return String.class;
			}
			
			public String getColumnName(int col) {
				return chead[col];
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
					setForeground(this.getColor(row,col));
					//checkDouble((String)value)
					//double val = Double.parseDouble((String)value);
					//setToolTipText("ind " + Math.round(val*10));
				}
				
				if (hasFocus)
					setBackground(Color.CYAN);
				else if (isSelected)
					setBackground(Color.LIGHT_GRAY);
				else
					setBackground(Color.WHITE);
				
				return this;
			}
			
			public Color getColor(int row, int col) {
				
				if (result[row][col-1] > (double)spinAlpha.getValue() &&
					result[col-1][row] < (double)spinBeta.getValue())
					return (row < col-1 ? poscClr : negcClr);
				else if (result[row][col-1] < (double)spinBeta.getValue() &&
						result[col-1][row] > (double)spinAlpha.getValue())
					return (row < col-1 ? negcClr : poscClr);
				else
					return dissClr;
			}
		}
		
		/*private class ExCellRenderer extends DefaultTableCellRenderer {
			
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				
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
			tableCol.setPreferredWidth( Math.max(
					Math.max(tableCol.getPreferredWidth(), hdrCol.getPreferredWidth()),
					Math.max(comp.getPreferredSize().width + this.getIntercellSpacing().width + 10,
						hdr.getPreferredSize().width + this.getIntercellSpacing().width + 10 ) ) );
			
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
				
				/// Save to clip board
				StringSelection ssel = new StringSelection(res.toString());
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(ssel, ssel);
				
			} catch (Exception ex) {
				showMessage("[Error] Coult not copy table data to clipboard");
			}
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
		/// Show cell markers on mouse press and hold
		public void mousePressed(MouseEvent evt) {
			//int valR = this.rowAtPoint(evt.getPoint());
			//int valC = this.columnAtPoint(evt.getPoint());
			//System.out.println("mouse " + this.getValueAt(valR, valC));
			
			//int valR = this.getSelectedRow();
			//int valC = this.getSelectedColumn();
			//System.out.println("mouse " + valR + " " + valC);
			
			if (result != null && SwingUtilities.isLeftMouseButton(evt)) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setColMarker(this.getSelectedColumn()-1);
				resPanel.setCellMarker(this.getSelectedRow(), this.getSelectedColumn()-1);
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
		
		/// Hide cell markers on mouse release
		public void mouseReleased(MouseEvent evt) {
			if (result != null && SwingUtilities.isLeftMouseButton(evt)) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setColMarker(-1);
				resPanel.setCellMarker(-1, -1);
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
		
	}
	
	/// Combo box for graphic options, only check boxes accepted
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class MyComboBox extends JComboBox<JCheckBox> {
		
		public MyComboBox(JCheckBox[] chs) {
			super(chs);
			setFont(lblFont);
			setRenderer(new MyComboBoxRenderer());
			addActionListener(new MyComboBoxListener());
		}
		
		private class MyComboBoxListener implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				JCheckBox chk = (JCheckBox)getSelectedItem();
				chk.setSelected(!chk.isSelected());
				refreshGraphic();
			}
		}
		
		private class MyComboBoxRenderer implements ListCellRenderer {
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
				Component comp = (Component)value;
				
				if (isSelected) {
					comp.setBackground(list.getSelectionBackground());
					comp.setForeground(list.getSelectionForeground());
				} else {
					comp.setBackground(list.getBackground());
					comp.setForeground(list.getForeground());
				}
				
				return comp;
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
			showMessage("[Warn] Could not determine column separator");
			return null;
		}
		
		int col = 0;
		if ((col = findCol(vec, separator)) == 0) {
			showMessage("[Warn] Columns are inconsistent");
			return null;
		}
		
		if (vec.size()-sd < 3 || col-sr < 3) {
			showMessage("[Warn] Minimum matrix size is 3x3");
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
		
		/// Split into matrices
		if (!transpose) {
			if (dres.length < 9) {
				showMessage("[Warn] Minimum matrix size is 3x3");
				return null;
			}
			
			if (dres.length % matCount != 0) {
				showMessage("[Warn] Input data length must be fully divisible by matrix count");
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
			if (dres[0].length < 9) {
				showMessage("[Warn] Minimum matrix size is 3x3");
				return null;
			}
			
			if (dres[0].length % matCount != 0) {
				showMessage("[Warn] Input data length must be fully divisible by matrix count");
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
		int vMethod = comboMethod.getSelectedIndex();
		int vMatCnt = (int)spinMatCnt.getValue();
		int vVariant = comboVariant.getSelectedIndex();
		
		int vRowN = (chkRowNames.isSelected() ? 1 : 0);
		int vColN = (chkColNames.isSelected() ? 1 : 0);
		boolean vTranspose = chkTranspose.isSelected();
		boolean vOrdPair = chkOrdPair.isSelected();
		
		if (vMethod != 0 && vMatCnt < 3) {
			showMessage("[Warn] Requires at least 3 matrices");
			return;
		}
		
		/// Clear global variables
		rowNames = "";
		colNames = "";
		hdrs = null;
		
		/// Read user input
		String data = textA.getText();
		if (data.length() == 0) {
			showMessage("Nothing to load");
			return;
		}
		
		/// Check for ordered pair
		if (vOrdPair && !checkOrdPair(data)) {
			showMessage("Ordered pair requires two data sets after #input1 and #input2");
			return;
		}
		
		/// Result
		double[][] matR = null;
		
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
				showMessage("[Error] Could not load the data");
				return;
			}
			
			/// Make result
			try {
				if (vec1.size() == 0 || vec2.size() == 0) {
					showMessage("[Warn] No data to read");
					return;
				}
				
				if (vMethod == 0) {
					/// Standard
					Vector<double[][]> vres1 = makeData(vec1, 1, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, 1, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("[Warn] Ordered pair - " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres1, vres2, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("Ordered pair - " + methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 1 || vMethod == 2 || vMethod == 3) {
					/// Aggregated
					Vector<double[][]> vres1 = makeData(vec1, vMatCnt, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, vMatCnt, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("[Warn] Ordered pair - " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres1, vres2, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("Ordered pair - " + methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 4) {
					/// Criteria Pair
					Vector<double[][]> vres1 = makeData(vec1, vMatCnt, vRowN, vColN, vTranspose);
					Vector<double[][]> vres2 = makeData(vec2, vMatCnt, vRowN, vColN, vTranspose);
					if (vres1 == null || vres2 == null) {
						showMessage("[Warn] Ordered pair - " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres1, vres2, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres1.size(), 0, vTranspose);
					//int vsize = vres1.get(0).length;
					//headerNames[1] = makeHeader(rowNames, colNames, (vsize*vsize-vsize)/2, 1, vTranspose);
					//headerNames[2] = makeHeader(rowNames, colNames, vres1.get(0).length, 0, vTranspose);
					//headerNames[3] = makeHeader(rowNames, colNames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("Ordered pair - " + methodNames[vMethod] + " - " + variantNames[vVariant]);
				}
				
			} catch (Exception ex) {
				showMessage("[Error] Ordered pair - could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("[Error] Not enough memory. Start the application with: " + ls +
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
				showMessage("[Error] Could not load the data");
				return;
			}
			
			/// Make result
			try {
				if (vec.size() == 0) {
					showMessage("[Warn] No data to read");
					return;
				}
				
				if (vMethod == 0) {
					/// Standard
					Vector<double[][]> vres = makeData(vec, 1, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("[Warn] " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres, null, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 1 || vMethod == 2 || vMethod == 3) {
					/// Aggregated
					Vector<double[][]> vres = makeData(vec, vMatCnt, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("[Warn] " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres, null, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 4) {
					/// Criteria Pair
					Vector<double[][]> vres = makeData(vec, vMatCnt, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("[Warn] " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres, null, vMethod+1, vVariant+1, vOrdPair);
					hdrs = makeHeader(rowNames, colNames, vres.size(), 0, vTranspose);
					//int vsize = vres.get(0).length;
					//headerNames[1] = makeHeader(rowNames, colNames, (vsize*vsize-vsize)/2, 1, vTranspose);
					//headerNames[2] = makeHeader(rowNames, colNames, vres.get(0).length, 0, vTranspose);
					//headerNames[3] = makeHeader(rowNames, colNames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
				}
				
			} catch (Exception ex) {
				showMessage("[Error] Could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("[Error] Not enough memory. Start the application with: " + ls +
						"  java -Xmx10240m -jar ICrAData.jar");
				return;
			}
		}
		
		/// Only if non-null
		if (matR != null)
			result = matR;
		
		/*for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++)
				System.out.print(result[i][j] + " ");
			System.out.println();
		}*/
		
		/// Display the result
		try {
			if (result != null) {
				
				/// Create the tables
				scrollA.setViewportView(new MyTable(result, hdrs, MU, comboTable1.getSelectedIndex()));
				scrollB.setViewportView(new MyTable(result, hdrs, NU, comboTable2.getSelectedIndex()));
				
				/// Create the graphic
				MyPanel resPanel = new MyPanel();
				resPanel.setPreferredSize(new Dimension(valSize, valSize));
				resPanel.setMyPoint(pointSize);
				resPanel.setMyOptions(new boolean[] {
						cbColor.isSelected(), cbMarks.isSelected(),
						cbGrid.isSelected(), cbText.isSelected()});
				resScroll.setViewportView(resPanel);
				
				/// Reset combo boxes - these events must be fired after resPanel is created, otherwise NullPointerException
				comboSize.setSelectedItem(String.valueOf(valSize)); /// event fired
			}
			
		} catch (Exception ex) {
			showMessage("[Error] Could not display the result");
			
		} catch (Error err) {
			showMessage("[Error] Not enough memory. Start the application with: " + ls +
					"  java -Xmx10240m -jar ICrAData.jar");
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
	
	/// Combo box with check boxes
	/// https://stackoverflow.com/questions/1573159/java-check-boxes-in-a-jcombobox
	/// https://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html#renderer
	/// https://github.com/gavalian/groot/blob/master/src/main/java/org/jlab/groot/ui/JComboCheckBox.java
	/// https://github.com/caduandrade/japura-gui/blob/master/src/main/java/org/japura/gui/renderer/CheckListRenderer.java
	
	/// Resizing split panes all the way - setMinimumSize(new Dimension(0, 0)) to all components that are part of split panes
	/// https://docs.oracle.com/javase/tutorial/uiswing/components/splitpane.html#divider
	
	/// Tool-tip and option pane fonts
	/// https://uwudamith.wordpress.com/2011/09/02/how-to-change-tooltips-font-in-java-customize-java-tooltips/
	/// https://stackoverflow.com/questions/17059575/how-to-change-the-font-in-joptionpane-showinputdialog-jtextfield
	
	/// Fonts in general
	/// https://community.oracle.com/thread/1482257
	/// https://stackoverflow.com/questions/1951558/list-of-java-swing-ui-properties
	
	/// Modal window
	/// https://stackoverflow.com/questions/1481405/how-to-make-a-jframe-modal-in-swing-java
	
}


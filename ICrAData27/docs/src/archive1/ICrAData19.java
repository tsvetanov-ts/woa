/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 1.9
 * Date: October 5, 2024
 * Compiled by: Java 17.0.11 (javac --release 7 *.java)
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

@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class ICrAData extends JFrame {
	
	/// Main entry point
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ICrAData();
			}
		});
	}
	
	/// System variables
	private String m_ud = System.getProperty("user.dir");
	private String m_fs = System.getProperty("file.separator");
	/// JTextArea internally uses \n which is in conflict with line.separator
	/// https://docs.oracle.com/javase/8/docs/api/javax/swing/text/DefaultEditorKit.html
	private String m_ls = "\n"; //System.getProperty("line.separator");
	
	/// Fonts
	private Font m_font1 = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private Font m_font2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private Font m_font3 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private Font m_font4 = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	
	/// Unicode alpha=\u03B1 beta=\u03B2 mu=\u03BC nu=\u03BD
	/// DecimalFormat - # is optional, while 0 is always written - ###.#### or 000.0000
	private DecimalFormat m_numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	
	/// Result
	private double[][] m_result = null;
	private String[] m_headers = null;
	private String m_rownames = "";
	private String m_colnames = "";
	
	/// Components
	private MyTextArea m_textA;
	private JTextArea m_textB;
	private JScrollPane m_scrollA, m_scrollB, m_scrollX;
	private JComboBox<String> m_cmbMethod, m_cmbVariant, m_cmbTable1, m_cmbTable2;
	private JCheckBox m_chkRowNames, m_chkColNames, m_chkTranspose, m_chkOrdPair;
	private JCheckBox m_chkPlotColor, m_chkPlotMarks, m_chkPlotGrid, m_chkPlotText;
	private JSpinner m_spinMatCnt, m_spinAlpha, m_spinBeta, m_spinPointSize;
	
	/// MyGlass points: double[4] saves cell x,y,width,height
	private Vector<double[]> m_glassA = new Vector<double[]>();
	private Vector<double[]> m_glassB = new Vector<double[]>();
	private MyGlass m_glassX;
	
	/// Constructor
	private ICrAData() {
		
		/// This is required
		Locale.setDefault(Locale.US);
		
		/// Look and feel
		try {
			/// Bold mono fonts are better on Windows
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				m_font1 = new Font(Font.MONOSPACED, Font.BOLD, 12);
				m_font2 = new Font(Font.MONOSPACED, Font.BOLD, 14);
				//m_font3 = new Font(Font.SANS_SERIF, Font.BOLD, 16);
				//m_font4 = new Font(Font.SANS_SERIF, Font.BOLD, 18);
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
		
		/// FlowLayout default is center and 5,5 unit gap
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
		
		/// Items for panel 1a
		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.setToolTipText("Open a file.");
		btnOpenFile.setPreferredSize(dim150);
		btnOpenFile.setFont(m_font3);
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenFileListener(evt);
			}
		});
		
		JButton btnSaveFile = new JButton("Save File");
		btnSaveFile.setToolTipText("Save the text in the input panel by choosing a file name.");
		btnSaveFile.setPreferredSize(dim150);
		btnSaveFile.setFont(m_font3);
		btnSaveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveFileListener(evt);
			}
		});
		
		JButton btnSaveDraft = new JButton("Save Draft");
		btnSaveDraft.setToolTipText("Save draft in subdirectory \"drafts\".");
		btnSaveDraft.setPreferredSize(dim150);
		btnSaveDraft.setFont(m_font3);
		btnSaveDraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveDraftListener(evt);
			}
		});
		
		/// Items for panel 1b
		JLabel lblMethod = new JLabel("ICrA Method");
		lblMethod.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMethod.setPreferredSize(dim150);
		lblMethod.setFont(m_font3);
		
		m_cmbMethod = new JComboBox<String>(
				new String[] {"Standard", "Aggr Average", "Aggr Max/Min", "Aggr Min/Max", "Criteria Pair"});
		m_cmbMethod.setToolTipText("<html>Method for InterCriteria Analysis.<br/>Standard directly applies the base algorithm.<br/>" +
				"The others require at least three input matrices.</html>");
		m_cmbMethod.setPreferredSize(dim150);
		m_cmbMethod.setFont(m_font3);
		m_cmbMethod.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					if (((JComboBox<String>)evt.getSource()).getSelectedIndex() == 0) {
						m_spinMatCnt.setValue(1);
						m_spinMatCnt.setEnabled(false);
					} else {
						m_spinMatCnt.setEnabled(true);
					}
				}
			}
		});
		
		JLabel lblMatCnt = new JLabel("MatCnt");
		lblMatCnt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMatCnt.setPreferredSize(dim70);
		lblMatCnt.setFont(m_font3);
		
		m_spinMatCnt = new JSpinner(new SpinnerNumberModel(1,1,10000,1));
		m_spinMatCnt.setToolTipText("Matrix count is applied to Aggregated and Criteria Pair.");
		m_spinMatCnt.setPreferredSize(dim60);
		m_spinMatCnt.setFont(m_font1);
		m_spinMatCnt.setEnabled(false);
		
		/// Items for panel 1c
		JLabel lblVariant = new JLabel("ICrA Variant");
		lblVariant.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVariant.setPreferredSize(dim150);
		lblVariant.setFont(m_font3);
		
		m_cmbVariant = new JComboBox<String>(new String[] {"\u03BC-biased", "Unbiased", "\u03BD-biased", "Balanced", "Weighted"});
		m_cmbVariant.setToolTipText("<html>Variant for InterCriteria Analysis.<br/>This is the base algorithm.</html>");
		m_cmbVariant.setPreferredSize(dim150);
		m_cmbVariant.setFont(m_font3);
		
		m_chkRowNames = new JCheckBox("RowNames");
		m_chkRowNames.setToolTipText("Row names are in the first column.");
		m_chkRowNames.setFont(m_font3);
		m_chkRowNames.setSelected(true);
		
		m_chkColNames = new JCheckBox("ColNames");
		m_chkColNames.setToolTipText("Column names are in the first row.");
		m_chkColNames.setFont(m_font3);
		m_chkColNames.setSelected(true);
		
		m_chkTranspose = new JCheckBox("Transpose");
		m_chkTranspose.setToolTipText("Transpose each matrix independently.");
		m_chkTranspose.setFont(m_font3);
		
		m_chkOrdPair = new JCheckBox("OrderedPair");
		m_chkOrdPair.setToolTipText("<html>Input two sets of data to load as ordered pair (\u03BC,\u03BD).<br/>" +
				"Data after #input1 is for \u03BC, data after #input2 is for \u03BD.<br/>" +
				"Aggregated/Criteria Pair require two data sets of at least three matrices each.</html>");
		m_chkOrdPair.setFont(m_font3);
		
		MyComboBox cmbInputOpt = new MyComboBox(new JCheckBox[] {m_chkRowNames, m_chkColNames, m_chkTranspose, m_chkOrdPair});
		cmbInputOpt.setToolTipText("Select the input options.");
		cmbInputOpt.setPreferredSize(dim150);
		
		/// Items for panel 1d
		JButton btnAnalysis = new JButton("Analysis");
		btnAnalysis.setToolTipText("Make the calculations and display them.");
		btnAnalysis.setPreferredSize(dim450);
		btnAnalysis.setFont(m_font3);
		btnAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAnalysisListener(evt);
			}
		});
		
		/// Items for panel 2a
		JLabel lblAlpha = new JLabel("Alpha");
		lblAlpha.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAlpha.setPreferredSize(dim50);
		lblAlpha.setFont(m_font3);
		
		m_spinAlpha = new JSpinner(new SpinnerNumberModel(0.75,0.5,1.0,0.01));
		m_spinAlpha.setToolTipText("<html>Table and plot colors:<br/>" +
				"\u03BC &gt; \u03B1 and \u03BD &lt; \u03B2 - positive consonance (green),<br/>" + 
				"\u03BC &lt; \u03B2 and \u03BD &gt; \u03B1 - negative consonance (red),<br/>" +
				"all other cases - dissonance (magenta).</html>");
		m_spinAlpha.setPreferredSize(dim60);
		m_spinAlpha.setFont(m_font1);
		m_spinAlpha.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				/// Refresh the graphic
				if (m_result != null) {
					MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
					resX.repaint();
					resX.revalidate();
				}
			}
		});
		
		JLabel lblBeta = new JLabel("Beta");
		lblBeta.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBeta.setPreferredSize(dim50);
		lblBeta.setFont(m_font3);
		
		m_spinBeta = new JSpinner(new SpinnerNumberModel(0.25,0.0,0.5,0.01));
		m_spinBeta.setToolTipText("<html>Table and plot colors:<br/>" +
				"\u03BC &gt; \u03B1 and \u03BD &lt; \u03B2 - positive consonance (green),<br/>" + 
				"\u03BC &lt; \u03B2 and \u03BD &gt; \u03B1 - negative consonance (red),<br/>" +
				"all other cases - dissonance (magenta).</html>");
		m_spinBeta.setPreferredSize(dim60);
		m_spinBeta.setFont(m_font1);
		m_spinBeta.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				/// Refresh the graphic
				if (m_result != null) {
					MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
					resX.repaint();
					resX.revalidate();
				}
			}
		});
		
		/// Items for panel 2b
		JLabel lblDigits = new JLabel("Digits");
		lblDigits.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDigits.setPreferredSize(dim50);
		lblDigits.setFont(m_font3);
		
		JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,16,1));
		spinDigits.setToolTipText("Digits after the decimal separator.");
		spinDigits.setPreferredSize(dim60);
		spinDigits.setFont(m_font1);
		spinDigits.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				JSpinner spinner = (JSpinner)evt.getSource();
				/// Make 0.0000, must be with dot, not with comma
				String strDig = "0.";
				for (int i = 0; i < (int)spinner.getValue(); i++)
					strDig += "0";
				/// Number format
				m_numFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(Locale.US));
				/// Refresh the tables and the graphic
				if (m_result != null) {
					m_scrollA.setViewportView(new MyTable(m_result, m_headers, m_cmbTable1.getSelectedIndex()));
					m_scrollB.setViewportView(new MyTable(m_result, m_headers, m_cmbTable2.getSelectedIndex()));
					MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
					resX.repaint();
					resX.revalidate();
				}
			}
		});
		
		JLabel lblWidth = new JLabel("Width");
		lblWidth.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWidth.setPreferredSize(dim50);
		lblWidth.setFont(m_font3);
		
		JSpinner spinWidth = new JSpinner(new SpinnerNumberModel(80,10,1000,10));
		spinWidth.setToolTipText("Table column width.");
		spinWidth.setPreferredSize(dim60);
		spinWidth.setFont(m_font1);
		spinWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (m_result != null) {
					MyTable tableA = (MyTable)m_scrollA.getViewport().getView();
					MyTable tableB = (MyTable)m_scrollB.getViewport().getView();
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
		});
		
		/// Items for panel 2c
		JLabel lblTable1 = new JLabel("Table 1");
		lblTable1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTable1.setPreferredSize(dim60);
		lblTable1.setFont(m_font3);
		
		m_cmbTable1 = new JComboBox<String>(new String[] {
				"\u03BC table", "\u03BD table","(\u03BC;\u03BD) table",
				"distance(1;0)", "distance(0;1)", "distance(0;0)"});//, "\u03BC/\u03BD output"});
		m_cmbTable1.setToolTipText("Display table 1.");
		m_cmbTable1.setPreferredSize(dim150);
		m_cmbTable1.setFont(m_font1);
		//m_cmbTable1.setMaximumRowCount(9);
		m_cmbTable1.setSelectedIndex(0);
		m_cmbTable1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					if (m_result != null)
						m_scrollA.setViewportView(new MyTable(m_result, m_headers, m_cmbTable1.getSelectedIndex()));
				}
			}
		});
		
		JLabel lblTable2 = new JLabel("Table 2");
		lblTable2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTable2.setPreferredSize(dim60);
		lblTable2.setFont(m_font3);
		
		m_cmbTable2 = new JComboBox<String>(new String[] {
				"\u03BC table", "\u03BD table","(\u03BC;\u03BD) table",
				"distance(1;0)", "distance(0;1)", "distance(0;0)"});//, "\u03BC/\u03BD output"});
		m_cmbTable2.setToolTipText("Display table 2.");
		m_cmbTable2.setPreferredSize(dim150);
		m_cmbTable2.setFont(m_font1);
		//m_cmbTable2.setMaximumRowCount(9);
		m_cmbTable2.setSelectedIndex(1);
		m_cmbTable2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					if (m_result != null)
						m_scrollB.setViewportView(new MyTable(m_result, m_headers, m_cmbTable2.getSelectedIndex()));
				}
			}
		});
		
		/// Items for panel 2d
		JButton btnExport = new JButton("Export");
		btnExport.setToolTipText("Export tables.");
		btnExport.setPreferredSize(dim100);
		btnExport.setFont(m_font3);
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnExportListener(evt);
			}
		});
		
		JButton btnInfo = new JButton("Info");
		btnInfo.setToolTipText("Information for the application.");
		btnInfo.setPreferredSize(dim100);
		btnInfo.setFont(m_font3);
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnInfoListener(evt);
			}
		});
		
		JButton btnScreen = new JButton("Screen");
		btnScreen.setToolTipText("Screenshot of the application.");
		btnScreen.setPreferredSize(dim100);
		btnScreen.setFont(m_font3);
		btnScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnScreenListener(evt);
			}
		});
		
		JButton btnAbout = new JButton("About");
		btnAbout.setToolTipText("About the application.");
		btnAbout.setPreferredSize(dim100);
		btnAbout.setFont(m_font3);
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAboutListener(evt);
			}
		});
		
		/// Items for panel 3
		JComboBox<String> cmbPlotSize = new JComboBox<String>(new String[] {"400", "600", "800", "1000", "2000", "3000", "5000"});
		cmbPlotSize.setToolTipText("Plot size or value from 100 to 10000.");
		cmbPlotSize.setPreferredSize(dim60);
		cmbPlotSize.setFont(m_font1);
		cmbPlotSize.setEditable(true);
		cmbPlotSize.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					String str = (String)((JComboBox<String>)evt.getSource()).getSelectedItem();
					int val = 400;
					if (checkInt(str)) {
						int v = Integer.parseInt(str);
						if (v >= 100 && v <= 10000)
							val = v;
					}
					if (m_result != null) {
						MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
						resX.setPreferredSize(new Dimension(val, val));
						resX.repaint();
						resX.revalidate();
					}
				}
			}
		});
		
		m_spinPointSize = new JSpinner(new SpinnerNumberModel(5,1,20,1));
		m_spinPointSize.setToolTipText("Point size.");
		m_spinPointSize.setPreferredSize(dim60);
		m_spinPointSize.setFont(m_font1);
		m_spinPointSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (m_result != null) {
					MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
					resX.setMyPoint( (int)((JSpinner)evt.getSource()).getValue() );
					resX.repaint();
					resX.revalidate();
				}
			}
		});
		
		m_chkPlotColor = new JCheckBox("Color");
		m_chkPlotColor.setToolTipText("Colors for the plot points.");
		m_chkPlotColor.setFont(m_font3);
		m_chkPlotColor.setSelected(true);
		m_chkPlotMarks = new JCheckBox("Marks");
		m_chkPlotMarks.setFont(m_font3);
		m_chkPlotMarks.setToolTipText("Marks for the plot.");
		m_chkPlotGrid = new JCheckBox("Grid");
		m_chkPlotGrid.setFont(m_font3);
		m_chkPlotGrid.setToolTipText("Grid for the plot.");
		m_chkPlotText = new JCheckBox("Text");
		m_chkPlotText.setFont(m_font3);
		m_chkPlotText.setToolTipText("Text for the plot.");
		
		MyComboBox cmbPlotOpt = new MyComboBox(new JCheckBox[] {m_chkPlotColor, m_chkPlotMarks, m_chkPlotGrid, m_chkPlotText});
		cmbPlotOpt.setToolTipText("Plot options.");
		cmbPlotOpt.setPreferredSize(dim100);
		
		JButton btnSaveTeX = new JButton("TeX");
		btnSaveTeX.setToolTipText("Save plot as TeX file.");
		btnSaveTeX.setPreferredSize(dim60);
		btnSaveTeX.setFont(m_font3);
		btnSaveTeX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveTeXListener(evt);
			}
		});
		
		JButton btnSavePNG = new JButton("PNG");
		btnSavePNG.setToolTipText("Save plot as PNG image.");
		btnSavePNG.setPreferredSize(dim60);
		btnSavePNG.setFont(m_font3);
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
		panel1b.add(m_cmbMethod);
		panel1b.add(lblMatCnt);
		panel1b.add(m_spinMatCnt);
		
		/// Panel 1c
		JPanel panel1c = new JPanel();
		panel1c.setBackground(Color.WHITE);
		panel1c.setLayout(flowLayout);
		panel1c.add(lblVariant);
		panel1c.add(m_cmbVariant);
		panel1c.add(cmbInputOpt);
		
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
		panel2a.add(m_spinAlpha);
		panel2a.add(lblDigits);
		panel2a.add(spinDigits);
		panel2a.add(lblTable1);
		panel2a.add(m_cmbTable1);
		panel2a.add(btnExport);
		panel2a.add(btnInfo);
		
		/// Panel 2b
		JPanel panel2b = new JPanel();
		panel2b.setBackground(Color.WHITE);
		panel2b.setLayout(flowLayout);
		panel2b.add(lblBeta);
		panel2b.add(m_spinBeta);
		panel2b.add(lblWidth);
		panel2b.add(spinWidth);
		panel2b.add(lblTable2);
		panel2b.add(m_cmbTable2);
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
		panel3.add(cmbPlotSize);
		panel3.add(m_spinPointSize);
		panel3.add(cmbPlotOpt);
		panel3.add(btnSaveTeX);
		panel3.add(btnSavePNG);
		
		
		/// Items for text areas
		m_textA = new MyTextArea("# Open file or copy/paste data here" + m_ls +
				"# Column separators: tab semicolon comma" + m_ls +
				"# Recognized numbers: 1.7 and 1,7" + m_ls + m_ls +
				"x;E;F;G;H;I" + m_ls +
				"A;6;5;3;7;6" + m_ls +
				"B;7;7;8;1;3" + m_ls +
				"C;4;3;5;9;1" + m_ls +
				"D;4;5;6;7;8" + m_ls + m_ls);
		
		m_textB = new JTextArea();
		m_textB.setFont(m_font2);
		m_textB.setEditable(false);
		m_textB.setLineWrap(true);
		m_textB.setWrapStyleWord(true);
		showMessage("ICrAData v1.9");
		
		JScrollPane scrollTextA = new JScrollPane(m_textA);
		scrollTextA.setMinimumSize(dim0);
		scrollTextA.setPreferredSize(new Dimension(425, 425));
		scrollTextA.setBorder(null);
		
		JScrollPane scrollTextB = new JScrollPane(m_textB);
		scrollTextB.setMinimumSize(dim0);
		scrollTextB.setPreferredSize(new Dimension(425, 100));
		scrollTextB.setBorder(null);
		
		m_scrollA = new JScrollPane();
		m_scrollA.setMinimumSize(dim0);
		m_scrollA.setPreferredSize(scrollSize);
		m_scrollA.setBorder(null);
		m_scrollA.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				int valH = m_scrollA.getHorizontalScrollBar().getValue();
				int valV = m_scrollA.getVerticalScrollBar().getValue();
				//System.out.println(valH + " " + valV);
				m_scrollB.getHorizontalScrollBar().setValue(valH);
				m_scrollB.getVerticalScrollBar().setValue(valV);
			}
		});
		
		m_scrollB = new JScrollPane();
		m_scrollB.setMinimumSize(dim0);
		m_scrollB.setPreferredSize(scrollSize);
		m_scrollB.setBorder(null);
		m_scrollB.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				int valH = m_scrollB.getHorizontalScrollBar().getValue();
				int valV = m_scrollB.getVerticalScrollBar().getValue();
				//System.out.println(valH + " " + valV);
				m_scrollA.getHorizontalScrollBar().setValue(valH);
				m_scrollA.getVerticalScrollBar().setValue(valV);
			}
		});
		
		m_scrollX = new JScrollPane();
		m_scrollX.setMinimumSize(dim0);
		m_scrollX.setPreferredSize(new Dimension(400, 400));
		m_scrollX.getViewport().setBackground(Color.WHITE);
		m_scrollX.setBorder(null);
		
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
		JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, m_scrollA, m_scrollB);
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
		panelGraphic.add(m_scrollX, BorderLayout.CENTER);
		
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
		
		setTitle("ICrAData v1.9");
		setIconImage(Toolkit.getDefaultToolkit().getImage("docs/images/x-icon.png"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitListener(evt);
			}
		});
		
		/// Glass pane
		m_glassX = new MyGlass(this.getContentPane());
		setGlassPane(m_glassX);
		
		/// Thread for saving drafts
		Thread t = new Thread(new Runnable() {
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
		});
		t.start();
		
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
		m_textB.append((new SimpleDateFormat("HH:mm:ss")).format(new Date()) + " " + msg + m_ls);
		m_textB.setCaretPosition(m_textB.getText().length());
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
		
		JFileChooser chOpen = new JFileChooser(m_ud);
		chOpen.setMultiSelectionEnabled(false);
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("CSV file (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File openFile = chOpen.getSelectedFile();
		m_ud = openFile.getParent();
		
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
							m_cmbMethod.setSelectedIndex(val);
					}
					
					if (checkInt(arr[1])) {
						int val = Integer.parseInt(arr[1]);
						if (val >= 1 && val <= 10000)
							m_spinMatCnt.setValue(val);
					}
					
					if (checkInt(arr[2])) {
						if (Integer.parseInt(arr[2]) == 1)
							m_chkRowNames.setSelected(true);
						else
							m_chkRowNames.setSelected(false);
					}
					
					if (checkInt(arr[3])) {
						if (Integer.parseInt(arr[3]) == 1)
							m_chkColNames.setSelected(true);
						else
							m_chkColNames.setSelected(false);
					}
					
					if (checkInt(arr[4])) {
						if (Integer.parseInt(arr[4]) == 1)
							m_chkTranspose.setSelected(true);
						else
							m_chkTranspose.setSelected(false);
					}
					
					if (checkInt(arr[5])) {
						if (Integer.parseInt(arr[5]) == 1)
							m_chkOrdPair.setSelected(true);
						else
							m_chkOrdPair.setSelected(false);
					}
				}
			}
			
			String str = line + m_ls;
			while ( (line = reX.readLine()) != null )
				str += line + m_ls;
			reX.close();
			
			m_textA.setText(str);
			showMessage("Opened file " + openFile.getName() + " (" + openFile + ")");
			
		} catch (Exception ex) {
			showMessage("[Error] Could not open file " + openFile.getName() + " (" + openFile + ")");
		}
	}
	
	/// Save file
	private void btnSaveFileListener(ActionEvent evt) {
		
		if (m_textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(m_ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			m_ud = saveFile.getParent();
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
		} else {
			showMessage("No text in the input panel");
		}
	}
	
	/// Save draft
	private void btnSaveDraftListener(ActionEvent evt) {
		
		if (m_textA.getText().length() > 0) {
			(new File(m_ud + m_fs + "drafts")).mkdir();
			File saveFile = new File(m_ud + m_fs + "drafts" + m_fs +
				"ICrAData-" + (new SimpleDateFormat("yyyyMMdd-HHmmss-SSS")).format(new Date()) + ".txt");
			
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
			
		} else {
			showMessage("No text in the input panel");
		}
	}
	
	/// Save the text with parameters
	private String saveText() {
		
		String res = "#icradata " + m_cmbMethod.getSelectedIndex() + " " +
				(int)m_spinMatCnt.getValue() + " " +
				(m_chkRowNames.isSelected() ? 1 : 0) + " " +
				(m_chkColNames.isSelected() ? 1 : 0) + " " +
				(m_chkTranspose.isSelected() ? 1 : 0) + " " +
				(m_chkOrdPair.isSelected() ? 1 : 0) + "\r\n";
		
		String[] data = m_textA.getText().split(m_ls);
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
		
		String eol = m_ls;
		if (valC == 3)
			eol = " \\\\ " + m_ls;
		
		/// StringBuilder is created much faster than String
		StringBuilder res = new StringBuilder();
		
		/// mu & nu square tables
		if (valT == 0) {
			
			/// mu table
			res.append("\u03BC-table" + sep);
			for (int j = 0; j < m_headers.length; j++) {
				res.append(m_headers[j]);
				if (j != m_headers.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < m_result.length; i++) {
				res.append(m_headers[i] + sep);
				for (int j = 0; j < m_result[i].length; j++) {
					if (i < j)
						res.append(decFormat.format(m_result[i][j]));
					else if (i > j)
						res.append(decFormat.format(m_result[j][i]));
					else
						res.append(decFormat.format(1.0));
					
					if (j != m_result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
			res.append(eol);
			
			/// nu table
			res.append("\u03BD-table" + sep);
			for (int j = 0; j < m_headers.length; j++) {
				res.append(m_headers[j]);
				if (j != m_headers.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < m_result.length; i++) {
				res.append(m_headers[i] + sep);
				for (int j = 0; j < m_result[i].length; j++) {
					if (i < j)
						res.append(decFormat.format(m_result[j][i]));
					else if (i > j)
						res.append(decFormat.format(m_result[i][j]));
					else
						res.append(decFormat.format(0.0));
					
					if (j != m_result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		/// (mu;nu) pairs table
		} else if (valT == 1) {
			
			res.append("(\u03BC;\u03BD)-pairs-table" + sep);
			for (int j = 0; j < m_headers.length; j++) {
				res.append(m_headers[j]);
				if (j != m_headers.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < m_result.length; i++) {
				res.append(m_headers[i] + sep);
				for (int j = 0; j < m_result[i].length; j++) {
					if (i < j)
						res.append("(" + decFormat.format(m_result[i][j]) + ";" + decFormat.format(m_result[j][i]) + ")");
					else if (i > j)
						res.append("(" + decFormat.format(m_result[j][i]) + ";" + decFormat.format(m_result[i][j]) + ")");
					else
						res.append("(" + decFormat.format(1.0) + ";" + decFormat.format(0.0) + ")");
					
					if (j != m_result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
			
		/// vector by row
		} else if (valT == 2) {
			
			res.append("vector-by-row" + sep + "\u03BC" + sep + "\u03BD" + sep + "row" + sep + "col" + eol);
			for (int i = 0; i < m_result.length; i++) {
				for (int j = 0; j < m_result[i].length; j++) {
					if (i < j) {
						res.append(m_headers[i] + "-" + m_headers[j] + sep +
							decFormat.format(m_result[i][j]) + sep +
							decFormat.format(m_result[j][i]) + sep +
							(i+1) + sep + (j+1) + eol);
					}
				}
			}
			
		/// vector by column
		} else if (valT == 3) {
			res.append("vector-by-column" + sep + "\u03BC" + sep + "\u03BD" + sep + "row" + sep + "col" + eol);
			for (int i = 0; i < m_result.length; i++) {
				for (int j = 0; j < m_result[i].length; j++) {
					if (i > j) {
						res.append(m_headers[i] + "-" + m_headers[j] + sep +
							decFormat.format(m_result[j][i]) + sep +
							decFormat.format(m_result[i][j]) + sep +
							(i+1) + sep + (j+1) + eol);
					}
				}
			}
			
		}/* else if (valT == 4) { /// mu/nu output
			
			res.append("\u03BC/\u03BD-output" + sep);
			for (int j = 0; j < m_headers.length; j++) {
				res.append(m_headers[j]);
				if (j != m_headers.length-1)
					res.append(sep);
			}
			res.append(eol);
			
			for (int i = 0; i < m_result.length; i++) {
				res.append(m_headers[i] + sep);
				for (int j = 0; j < m_result[i].length; j++) {
					res.append(decFormat.format(m_result[i][j]));
					if (j != m_result[i].length-1)
						res.append(sep);
				}
				res.append(eol);
			}
		}*/
		
		return res.toString();
	}
	
	/// Export data
	private void btnExportListener(ActionEvent evt) {
		
		try {
			if (m_result != null) {
				/// JDialog(Frame owner, String title, boolean modal)
				final JDialog expDialog = new JDialog(this, "Export", true);
				
				Dimension dim60 = new Dimension(60, 25);
				Dimension dim150 = new Dimension(120, 25);
				Dimension dim200 = new Dimension(200, 25);
				
				JLabel lblColSep = new JLabel("ColSep");
				lblColSep.setHorizontalAlignment(SwingConstants.RIGHT);
				lblColSep.setPreferredSize(dim60);
				lblColSep.setFont(m_font3);
				
				JLabel lblDecSep = new JLabel("DecSep");
				lblDecSep.setHorizontalAlignment(SwingConstants.RIGHT);
				lblDecSep.setPreferredSize(dim60);
				lblDecSep.setFont(m_font3);
				
				JLabel lblDigits = new JLabel("Digits");
				lblDigits.setHorizontalAlignment(SwingConstants.RIGHT);
				lblDigits.setPreferredSize(dim60);
				lblDigits.setFont(m_font3);
				
				final JComboBox<String> cmbTable = new JComboBox<String>(new String[] {
						"\u03BC & \u03BD square tables", "(\u03BC;\u03BD) pairs table",
						"vector by row", "vector by column"});//, "\u03BC/\u03BD output"});
				cmbTable.setToolTipText("Display tables.");
				cmbTable.setPreferredSize(dim200);
				cmbTable.setFont(m_font1);
				cmbTable.setMaximumRowCount(13);
				
				final JComboBox<String> cmbColSep = new JComboBox<String>(
						new String[] {"Tab \\t", "Semicolon ;", "Comma ,", "TeX &"});
				cmbColSep.setToolTipText("Column separator.");
				cmbColSep.setPreferredSize(dim150);
				cmbColSep.setFont(m_font1);
				
				final JComboBox<String> cmbDecSep = new JComboBox<String>(
						new String[] {"Point .", "Comma ,"});
				cmbDecSep.setToolTipText("Decimal separator.");
				cmbDecSep.setPreferredSize(dim150);
				cmbDecSep.setFont(m_font1);
				
				final JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,16,1));
				spinDigits.setToolTipText("Digits after the decimal separator.");
				spinDigits.setPreferredSize(dim60);
				spinDigits.setFont(m_font1);
				
				JButton btnExport = new JButton("Export");
				btnExport.setToolTipText("Export table.");
				btnExport.setPreferredSize(dim200);
				btnExport.setFont(m_font3);
				btnExport.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JFileChooser chSave = new JFileChooser(m_ud);
						chSave.setMultiSelectionEnabled(false);
						chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
						
						if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
							return;
						
						File saveFile = chSave.getSelectedFile();
						m_ud = saveFile.getParent();
						if (!saveFile.toString().toLowerCase().endsWith(".txt"))
							saveFile = new File(chSave.getSelectedFile() + ".txt");
						
						try {
							FileOutputStream foX = new FileOutputStream(saveFile, false);
							OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
							BufferedWriter wrX = new BufferedWriter(osX);
							
							wrX.write(saveTable(cmbTable.getSelectedIndex(), cmbColSep.getSelectedIndex(),
									cmbDecSep.getSelectedIndex(), (int)spinDigits.getValue()).replace(m_ls, "\r\n"));
							wrX.close();
							showMessage("Saved table " + saveFile.getName() + " (" + saveFile + ")");
							
						} catch (Exception ex) {
							showMessage("[Error] Could not save table " + saveFile.getName() + " (" + saveFile + ")");
						}
					}
				});
				
				JButton btnClose = new JButton("Close");
				btnClose.setToolTipText("Close window.");
				btnClose.setPreferredSize(dim200);
				btnClose.setFont(m_font3);
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						expDialog.setVisible(false);
					}
				});
				
				/// FlowLayout default is center and 5,5 unit gap
				FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
				
				JPanel panel1 = new JPanel(flowLayout);
				panel1.add(cmbTable);
				
				JPanel panel2 = new JPanel(flowLayout);
				panel2.add(lblColSep);
				panel2.add(cmbColSep);
				
				JPanel panel3 = new JPanel(flowLayout);
				panel3.add(lblDecSep);
				panel3.add(cmbDecSep);
				
				JPanel panel4 = new JPanel(flowLayout);
				panel4.add(lblDigits);
				panel4.add(spinDigits);
				
				JPanel panel5 = new JPanel(flowLayout);
				panel5.add(btnExport);
				
				JPanel panel6 = new JPanel(flowLayout);
				panel6.add(btnClose);
				
				expDialog.setLayout(new GridLayout(6,0));
				expDialog.add(panel1);
				expDialog.add(panel2);
				expDialog.add(panel3);
				expDialog.add(panel4);
				expDialog.add(panel5);
				expDialog.add(panel6);
				
				expDialog.setResizable(false);
				expDialog.pack();
				//expDialog.setLocationByPlatform(true);
				expDialog.setLocationRelativeTo(this);
				expDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				expDialog.setVisible(true);
				
			} else {
				showMessage("No data to export");
			}
			
		} catch (Exception ex) {
			showMessage("[Error] Could not export the data");
		}
	}
	
	/// Information for the application
	private void btnInfoListener(ActionEvent evt) {
		
		JFrame infoFrame = new JFrame();
		MyTextArea text = new MyTextArea(
				"InterCriteria Analysis Data" + m_ls + m_ls +
				">>> Left panel" + m_ls +
				"Open text file or comma separated values file." + m_ls +
				"Open MS Excel/LibreOffice Calc and copy/paste the table with optional headers." + m_ls +
				"Select Row and Column names if header was copied, or type after #rownames and #colnames." + m_ls +
				"Markers #input1 and #input2 are required for Ordered Pair." + m_ls + m_ls +
				">>> Center panel" + m_ls +
				"Value \u03B1 is from 0.5 to 1, value \u03B2 is from 0 to 0.5, both with 0.01 increment." + m_ls +
				"When \u03BC > \u03B1 and \u03BD < \u03B2, that is Positive Consonance, color is green." + m_ls +
				"When \u03BC < \u03B2 and \u03BD > \u03B1, that is Negative Consonance, color is red." + m_ls +
				"In all other cases, that is Dissonance, color is magenta." + m_ls + m_ls +
				">>> Right panel" + m_ls +
				"Zoom the plot by changing the 400 pixels selector. Default point size is 5." + m_ls +
				"Buttons TeX/PNG save the graphic from the panel in the respective format." + m_ls + m_ls +
				">>> Use only Java LTS: 11, 17, 21, 25, etc. Java 8 does not have support for HiDPI." + m_ls +
				"Download Java from: https://www.oracle.com/java/technologies/downloads/" + m_ls +
				"Direct link: https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.msi" + m_ls + m_ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		infoFrame.setTitle("Information");
		infoFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/images/x-icon.png"));
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
				"InterCriteria Analysis proposed for the first time by this article:" + m_ls +
				"  Atanassov K., D. Mavrov, V. Atanassova," + m_ls +
				"  Intercriteria Decision Making: A New Approach for Multicriteria Decision Making," + m_ls +
				"    Based on Index Matrices and Intuitionistic Fuzzy Sets," + m_ls +
				"  Issues in Intuitionistic Fuzzy Sets and Generalized Nets, Vol. 11, 2014, 1-8." + m_ls + m_ls +
				"Main paper for the software application:" + m_ls +
				"  Ikonomov N., P. Vassilev, O. Roeva," + m_ls +
				"  ICrAData - Software for InterCriteria Analysis," + m_ls +
				"  International Journal Bioautomation, Vol. 22(1), 2018, 1-10." + m_ls + m_ls +
				"This software application has been developed with the partial financial support of:" + m_ls +
				"  Changes in versions from 1.3 to 1.8 have been implemented for" + m_ls +
				"    project DN 17/06 ``A New Approach, Based on an Intercriteria Data Analysis," + m_ls +
				"    to Support Decision Making in 'in silico' Studies of Complex Biomolecular Systems''," + m_ls +
				"    funded by the National Science Fund of Bulgaria." + m_ls +
				"  Changes in versions from 0.9.6 to 1.2 have been implemented for" + m_ls +
				"    project DFNI-I-02-5 ``InterCriteria Analysis: A New Approach to Decision Making''," + m_ls +
				"    funded by the National Science Fund of Bulgaria." + m_ls + m_ls + m_ls +
				"InterCriteria Analysis Data" + m_ls +
				"  Version: 1.9" + m_ls +
				"  Date: October 5, 2024" + m_ls +
				"  Compiled by: Java 17.0.11 (javac --release 7 *.java)" + m_ls + m_ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		aboutFrame.setTitle("About");
		aboutFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/images/x-icon.png"));
		aboutFrame.add(scrollText);
		
		aboutFrame.pack();
		aboutFrame.setLocationByPlatform(true);
		aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutFrame.setVisible(true);
	}
	
	/// Export the screen
	private void btnScreenListener(ActionEvent evt) {
		
		JFileChooser chSave = new JFileChooser(m_ud);
		chSave.setMultiSelectionEnabled(false);
		chSave.setAcceptAllFileFilterUsed(false);
		chSave.setFileFilter(new FileNameExtensionFilter("Screen (*.png)", "png"));
		
		if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			return;
		
		File chName = chSave.getSelectedFile();
		m_ud = chName.getParent();
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
		
		if (m_result != null) {
			JFileChooser chSave = new JFileChooser(m_ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setAcceptAllFileFilterUsed(false);
			chSave.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			m_ud = chName.getParent();
			if (!chName.toString().toLowerCase().endsWith(".png"))
				chName = new File(chName.toString() + ".png");
			
			try {
				MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
				
				BufferedImage img = new BufferedImage(resX.getWidth(), resX.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = img.createGraphics();
				resX.printAll(g2);
				g2.dispose();
				
				//Graphics g = img.getGraphics();
				//resX.printAll(g);
				
				//Graphics2D g2 = (Graphics2D)img.getGraphics();
				//resX.printAll(g2);
				//resX.paintAll(g2);
				
				//FileOutputStream fos = new FileOutputStream(m_ud + "test.jpg");
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
		
		if (m_result != null) {
			JFileChooser chSave = new JFileChooser(m_ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setAcceptAllFileFilterUsed(false);
			chSave.setFileFilter(new FileNameExtensionFilter("TeX file (*.tex)", "tex"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			m_ud = chName.getParent();
			if (!chName.toString().toLowerCase().endsWith(".tex"))
				chName = new File(chName.toString() + ".tex");
			
			try {
				FileOutputStream foX = new FileOutputStream(chName, false);
				OutputStreamWriter osX = new OutputStreamWriter(foX, "UTF8");
				BufferedWriter wrX = new BufferedWriter(osX);
				
				wrX.write(exportTeX().replace(m_ls, "\r\n"));
				wrX.close();
				showMessage("Saved TeX file " + chName.getName() + " (" + chName + ")");
				
			} catch (Exception ex) {
				showMessage("[Error] Could not save TeX file " + chName.getName() + " (" + chName + ")");
				ex.printStackTrace();
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
		
		/// picture \makebox(0,0)[cc]{$Z_1$}   c r l b t
		/// http://www.emerson.emory.edu/services/latex/latex_51.html
		/// https://tex.stackexchange.com/questions/32791/picture-environment-rotating-text
		StringBuilder res = new StringBuilder();
		res.append("%%% ICrAData TeX Export " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + m_ls +
			"\\documentclass[11pt]{article}" + m_ls +
			(m_chkPlotText.isSelected() ? "\\usepackage{graphicx}" + m_ls : "") +
			(m_chkPlotColor.isSelected() || m_chkPlotGrid.isSelected() ? "\\usepackage{xcolor}" + m_ls : "") +
			"\\begin{document}" + m_ls +
			"\\thispagestyle{empty}" + m_ls + m_ls +
			"%%% Change unitlength and font size to scale the graphic" + m_ls +
			"%%% Font sizes: \\tiny \\scriptsize \\footnotesize \\small \\normalsize \\large \\Large \\LARGE \\huge \\Huge" + m_ls +
			"\\begin{center}" + m_ls +
			(m_chkPlotColor.isSelected() ?
				"\\definecolor{posc}{RGB}{0, 160, 0}" + m_ls +
				"\\definecolor{negc}{RGB}{255, 64, 64}" + m_ls +
				"\\definecolor{diss}{RGB}{255, 64, 255}" + m_ls
				: "") +
			(m_chkPlotGrid.isSelected() ? "\\definecolor{grid}{RGB}{192, 192, 192}" + m_ls : "") +
			"\\newcommand{\\myrad}{" + (0.05*(int)m_spinPointSize.getValue()) + "}" + m_ls +
			(m_chkPlotText.isSelected() ? "\\newcommand{\\myticks}{\\scriptsize}" + m_ls +
				"\\newcommand{\\mytext}{\\normalsize}" + m_ls : "") +
			"\\setlength{\\unitlength}{20pt} %10pt=4mm" + m_ls +
			"\\linethickness{0.5pt}" + m_ls +
			"\\begin{picture}" +
			(m_chkPlotText.isSelected() ? "(11.5,11.5)(-1.5,-1.5)" + m_ls : "(10,10)" + m_ls)
		);
		
		/// Ticks
		if (m_chkPlotMarks.isSelected() && !m_chkPlotGrid.isSelected()) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + m_ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + m_ls);
		}
		
		/// Grid
		if (m_chkPlotGrid.isSelected()) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\color{grid}\\line(0,1){" + Integer.valueOf(10 - i) + ".15}}" + m_ls +
					"\\put(-0.15," + i + "){\\color{grid}\\line(1,0){" + Integer.valueOf(10 - i) + ".15}}" + m_ls);
		}
		
		/// Triangle
		res.append("\\put(0,0){\\line(0,1){10}}" + m_ls +
				"\\put(0,0){\\line(1,0){10}}" + m_ls +
				"\\put(10,0){\\line(-1,1){10}}" + m_ls);
		
		/// Text
		if (m_chkPlotText.isSelected()) {
			res.append("\\put(5,-1.2){\\makebox(0,0)[cc]{\\mytext Degree of agreement, $\\mu$}}" + m_ls +
				"\\put(-1.3,5){\\makebox(0,0)[cc]{\\rotatebox{90}{\\mytext Degree of disagreement, $\\nu$}}}" + m_ls +
				"\\put(0,-0.4){\\makebox(0,0)[cc]{\\myticks $0$}}" + m_ls +
				"\\put(10,-0.4){\\makebox(0,0)[cc]{\\myticks $1$}}" + m_ls +
				"\\put(-0.33,0){\\makebox(0,0)[cc]{\\myticks $0$}}" + m_ls +
				"\\put(-0.33,10){\\makebox(0,0)[cc]{\\myticks $1$}}" + m_ls);
			for (int i = 1; i < 10; i++)
				res.append(
					"\\put(" + i + ",-0.4){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + m_ls +
					"\\put(-0.5," + i + "){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + m_ls);
		}
		
		/// Points
		for (int i = 0; i < m_result.length; i++) {
			for (int j = i+1; j < m_result[0].length; j++) {
				res.append("\\put(" + m_result[i][j]*10 + "," + m_result[j][i]*10 + "){" +
					(m_chkPlotColor.isSelected() ? exportColor(m_result[i][j], m_result[j][i],
						(double)m_spinAlpha.getValue(), (double)m_spinBeta.getValue()) : "") +
					"\\circle*{\\myrad}}" + m_ls);
			}
		}
		
		res.append("\\end{picture}" + m_ls +
			"\\end{center}" + m_ls + m_ls +
			"\\end{document}" + m_ls);
		
		return res.toString();
	}
	
	/// Extend TextArea with UndoManager, MouseListener, KeyEvents
	private class MyTextArea extends JTextArea implements MouseListener {
		
		private JPopupMenu a_rightmouse;
		
		private MyTextArea(String str) {
			setText(str);
			setFont(m_font2);
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
			
			a_rightmouse = new JPopupMenu();
			a_rightmouse.add(mCut);
			a_rightmouse.add(mCopy);
			a_rightmouse.add(mPaste);
			a_rightmouse.add(new JSeparator());
			a_rightmouse.add(mUndo);
			a_rightmouse.add(mRedo);
			
		}
		
		public void mouseClicked(MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton(evt))
				a_rightmouse.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
	}
	
	/// Panel for plotting - implements MouseListener, MouseMotionListener, ComponentListener
	private class MyPanel extends JPanel implements Scrollable, MouseListener, MouseMotionListener {
		
		private Rectangle[][] p_rectdata;
		private boolean[] p_options;
		private double p_pointsize = 0;
		private int p_cellrow = -1;
		private int p_cellcol = -1;
		private int p_markcol = -1;
		private Point p_dragpoint = new Point();
		
		private MyPanel() {
			setBackground(Color.WHITE);
			addMouseListener(this);
			addMouseMotionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		private void setMyOptions(boolean[] val) {
			p_options = val;
		}
		
		private void setMyPoint(double val) {
			p_pointsize = val;
		}
		
		private void setCellMarker(int row, int col) {
			p_cellrow = row;
			p_cellcol = col;
		}
		
		private void setColMarker(int col) {
			p_markcol = col;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(m_font4);
			
			int width = getWidth();
			int height = getHeight();
			double delta = 20;
			if (p_options[3])
				delta = 60;
			
			/// Scale so that there are boundaries on the graph
			double widthScale = width-2*delta;
			double heightScale = height-2*delta;
			double line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			
			/// Ticks
			if (p_options[1] && !p_options[2]) {
				g2.setColor(Color.BLACK);
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
			}
			
			/// Grid
			if (p_options[2]) {
				g2.setColor(new Color(192, 192, 192));
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+widthScale*marks[i], delta+heightScale*marks[i]));
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], delta+heightScale*marks[i], delta+widthScale*marks[i], height-delta+line));
			}
			
			/// Triangle
			{
				g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
				g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
				g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			}
			
			/// Text
			if (p_options[3]) {
				g2.setColor(Color.BLACK);
				
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
				
				//int widthMU = g2.getFontMetrics(this.getFont()).stringWidth("Degree of agreement, \u03BC");
				//int widthNU = g2.getFontMetrics(this.getFont()).stringWidth("Degree of disagreement, \u03BD");
				//g2.drawString("Degree of agreement, \u03BC", (int)(delta+widthScale*marks[4]-widthMU/2), (int)(height-delta+10*line));
				g2.drawString("Degree of agreement, \u03BC", (int)(delta), (int)(height-delta+10*line));
				
				AffineTransform g2Orig = g2.getTransform();
				g2.rotate(Math.toRadians(-90));
				//g2.drawString("Degree of disagreement, \u03BD", (int)(-(delta+heightScale*marks[4]+widthNU/2)), (int)(delta-9*line));
				g2.drawString("Degree of disagreement, \u03BD", (int)(-(delta+heightScale)), (int)(delta-9*line));
				g2.setTransform(g2Orig);
			}
			
			/// Colors
			//g2.setPaint(Color.RED);
			g2.setColor(Color.BLACK);
			
			int rows = m_result.length;
			int cols = m_result[0].length;
			
			/// Save rectangles for glass
			p_rectdata = new Rectangle[rows][cols];
			double radius = p_pointsize;
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					/// Plot colors
					if (p_options[0]) {
						if (m_result[i][j] > (double)m_spinAlpha.getValue() &&
							m_result[j][i] < (double)m_spinBeta.getValue())
							g2.setColor(new Color(0, 160, 0)); /// PLOT green
						else if (m_result[i][j] < (double)m_spinBeta.getValue() &&
								m_result[j][i] > (double)m_spinAlpha.getValue())
							g2.setColor(new Color(255, 64, 64)); /// PLOT red
						else
							g2.setColor(new Color(255, 64, 255)); /// PLOT magenta
					}
					
					/// Plot points
					double x = m_result[i][j]*widthScale+delta;
					double y = heightScale-m_result[j][i]*heightScale+delta;
					p_rectdata[i][j] = new Rectangle((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
					g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				}
			}
			
			/// All points from that column - mark in blue
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					if (p_markcol > -1 && (j == p_markcol || i == p_markcol)) {
						double x = m_result[i][j]*widthScale+delta;
						double y = heightScale-m_result[j][i]*heightScale+delta;
						g2.setColor(Color.BLUE);
						g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
					}
				}
			}
			
			/// Cell that has focus - mark in red
			for (int i = 0; i < rows; i++) {
				for (int j = i+1; j < cols; j++) {
					if ( p_cellrow > -1 && p_cellcol > -1 &&
							((i == p_cellrow && j == p_cellcol) || (j == p_cellrow && i == p_cellcol)) ) {
						double x = m_result[i][j]*widthScale+delta;
						double y = heightScale-m_result[j][i]*heightScale+delta;
						g2.setColor(Color.CYAN);
						g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
					}
				}
			}
			
		}
		
		/// Set tool-tip text
		public String getToolTipText(MouseEvent evt) {
			
			for (int i = 0; i < p_rectdata.length; i++) {
				for (int j = i+1; j < p_rectdata[0].length; j++) {
					if (p_rectdata[i][j].contains(evt.getX(), evt.getY())) {
						return "<html>Row: " + m_headers[i] +
								"<br/>Column: " + m_headers[j] +
								"<br/>\u03BC: " + m_numFormat.format(m_result[i][j]) +
								"<br/>\u03BD: " + m_numFormat.format(m_result[j][i]) +
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
				p_dragpoint.setLocation(evt.getPoint());
				
				/// Change cursor to crosshair
				//((JPanel)evt.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				//this.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				m_glassX.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				
				/// Clear all points from memory
				m_glassA.clear();
				m_glassB.clear();
				
				for (int i = 0; i < p_rectdata.length; i++) {
					for (int j = i+1; j < p_rectdata[0].length; j++) {
						if (p_rectdata[i][j].contains(evt.getX(), evt.getY())) {
							MyTable tableA = (MyTable)m_scrollA.getViewport().getView();
							MyTable tableB = (MyTable)m_scrollB.getViewport().getView();
							
							/// Rows and columns have index from 0, 1, 2, 3, etc
							/// Rows: the column table header is not counted towards the rows, therefore it is correct
							/// Columns: We have row table header, therefore the column is +1
							Rectangle cellA = tableA.getCellRect(i, j+1, false);
							Rectangle cellB = tableB.getCellRect(i, j+1, false);
							
							/// View the cell
							m_scrollA.getHorizontalScrollBar().setValue((int)cellA.getX());
							m_scrollA.getVerticalScrollBar().setValue((int)cellA.getY());
							m_scrollB.getHorizontalScrollBar().setValue((int)cellB.getX());
							m_scrollB.getVerticalScrollBar().setValue((int)cellB.getY());
							
							/// Add the points to the glass
							m_glassA.add(new double[] {cellA.getX(), cellA.getY(), cellA.getWidth(), cellA.getHeight()});
							m_glassB.add(new double[] {cellB.getX(), cellB.getY(), cellB.getWidth(), cellB.getHeight()});
						}
					}
				}
				
				/// Show the rectangles on the glass
				if (m_glassA.size() > 0) {
					m_glassX.setVisible(true);
					m_glassX.repaint();
				} else {
					m_glassX.setVisible(false);
				}
			}
		}
		
		/// Hide the glass when the left mouse button is released
		public void mouseReleased(MouseEvent evt) {
			if (SwingUtilities.isLeftMouseButton(evt)) {
				//((JPanel)evt.getSource()).setCursor(Cursor.getDefaultCursor());
				//this.getRootPane().setCursor(Cursor.getDefaultCursor());
				m_glassX.setCursor(Cursor.getDefaultCursor());
				m_glassX.setVisible(false);
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
			Point newpoint = evt.getPoint();
			Point viewpoint = theView.getViewPosition();
			//System.out.println(newpoint.getX() + " " + newpoint.getY() + "    " + viewpoint.getX() + " " + viewpoint.getY());
			viewpoint.translate(p_dragpoint.x - newpoint.x, p_dragpoint.y - newpoint.y);
			thePanel.scrollRectToVisible(new Rectangle(viewpoint, theView.getSize()));
			p_dragpoint.setLocation(newpoint);
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
		
		private Container g_contain;
		
		private MyGlass(Container contentPane) {
			g_contain = contentPane;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			for (int i = 0; i < m_glassA.size(); i++) {
				double[] pointA = m_glassA.get(i);
				double[] pointB = m_glassB.get(i);
				Point locF = g_contain.getParent().getLocationOnScreen();
				
				Point locSA = m_scrollA.getLocationOnScreen();
				Point locSB = m_scrollB.getLocationOnScreen();
				Rectangle visSA = m_scrollA.getViewportBorderBounds();
				Rectangle visSB = m_scrollB.getViewportBorderBounds();
				
				MyTable tableA = (MyTable)m_scrollA.getViewport().getView();
				MyTable tableB = (MyTable)m_scrollB.getViewport().getView();
				
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
		
		private String[][] t_data;
		private String[] t_rows;
		private String[] t_cols;
		
		private Action t_action = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				saveTableData();
			}
		};
		
		private MyTable(double[][] matR, String[] headers, int disp) {
			
			/// Headers
			t_rows = headers;
			t_cols = new String[headers.length + 1];
			t_cols[0] = "x";
			for (int i = 0; i < headers.length; i++)
				t_cols[i+1] = headers[i];
			
			/// Data
			int rows = matR.length;
			int cols = matR[0].length;
			t_data = new String[rows][cols];
			
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					
					/// MU table
					if (disp == 0) {
						if (i < j) /// upper triangular
							t_data[i][j] = m_numFormat.format(matR[i][j]);
						else if (i > j) /// lower triangular
							t_data[i][j] = m_numFormat.format(matR[j][i]);
						else
							t_data[i][j] = m_numFormat.format(1.0);
						t_cols[0] = "\u03BC";
						
					/// NU table
					} else if (disp == 1) {
						if (i < j) /// upper triangular
							t_data[i][j] = m_numFormat.format(matR[j][i]);
						else if (i > j) /// lower triangular
							t_data[i][j] = m_numFormat.format(matR[i][j]);
						else
							t_data[i][j] = m_numFormat.format(0.0);
						t_cols[0] = "\u03BD";
						
					/// (MU;NU) table
					} else if (disp == 2) {
						if (i < j) /// upper triangular
							t_data[i][j] = "(" + m_numFormat.format(matR[i][j]) + ";" + m_numFormat.format(matR[j][i]) + ")";
						else if (i > j) /// lower triangular
							t_data[i][j] = "(" + m_numFormat.format(matR[j][i]) + ";" + m_numFormat.format(matR[i][j]) + ")";
						else
							t_data[i][j] = "(" + m_numFormat.format(1.0) + ";" + m_numFormat.format(0.0) + ")";
						t_cols[0] = "(\u03BC;\u03BD)";
						
					/// distance to (1;0)
					} else if (disp == 3) {
						if (i < j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( (1-matR[i][j])*(1-matR[i][j]) + matR[j][i]*matR[j][i] ));
						else if (i > j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( (1-matR[j][i])*(1-matR[j][i]) + matR[i][j]*matR[i][j] ));
						else
							t_data[i][j] = m_numFormat.format(0.0);
						t_cols[0] = "dist(1;0)";
						
					/// distance to (0;1)
					} else if (disp == 4) {
						if (i < j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( matR[i][j]*matR[i][j] + (1-matR[j][i])*(1-matR[j][i]) ));
						else if (i > j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( matR[j][i]*matR[j][i] + (1-matR[i][j])*(1-matR[i][j]) ));
						else
							t_data[i][j] = m_numFormat.format(0.0);
						t_cols[0] = "dist(0;1)";
						
					/// distance to (0;0)
					} else if (disp == 5) {
						if (i < j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( matR[i][j]*matR[i][j] + matR[j][i]*matR[j][i] ));
						else if (i > j)
							t_data[i][j] = m_numFormat.format(Math.sqrt( matR[j][i]*matR[j][i] + matR[i][j]*matR[i][j] ));
						else
							t_data[i][j] = m_numFormat.format(0.0);
						t_cols[0] = "dist(0;0)";
						
					}/* else if (disp == 6) { /// MU and NU
						t_data[i][j] = m_numFormat.format(matR[i][j]);
						t_cols[0] = "\u03BC/\u03BD";
					}*/
				}
			}
			
			addMouseListener(this);
			
			setModel(new MyTableModel());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setAutoCreateRowSorter(false);
			setColumnSelectionAllowed(true);
			setRowSelectionAllowed(false);
			
			getTableHeader().setFont(m_font1);
			getTableHeader().setResizingAllowed(true);
			getTableHeader().setReorderingAllowed(false);
			//getTableHeader().getColumnModel().getColumn(2).setWidth(200);
			setRowHeight(20);//setRowHeight((int)(getRowHeight()*sc));
			
			setDefaultRenderer(String.class, new MyCellRenderer());
			//setDefaultRenderer(String.class, new ExCellRenderer());
			((DefaultTableCellRenderer)getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
			
			/// Modify the Ctrl-C key binding
			t_action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			getInputMap().put((KeyStroke)t_action.getValue(Action.ACCELERATOR_KEY), "ACTION_COPY");
			getActionMap().put("ACTION_COPY", t_action);
			
		}
		
		private class MyTableModel extends AbstractTableModel {
			
			public int getRowCount() {
				return t_data.length;
			}
			
			public int getColumnCount() {
				return t_cols.length;
			}
			
			public Object getValueAt(int row, int col) {
				if (col == 0)
					return t_rows[row];
				else
					return t_data[row][col-1];
			}
			
			public Class<?> getColumnClass(int col) {
				return String.class;
			}
			
			public String getColumnName(int col) {
				return t_cols[col];
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
				
				setFont(m_font1);
				setHorizontalAlignment(SwingConstants.RIGHT);
				setForeground(Color.BLACK);
				
				if (col > 0 && row != col-1)
					setForeground(this.getColor(row,col)); /// TABLE cell
				else if (row == col-1)
					setForeground(new Color(128, 128, 128)); /// TABLE gray
				
				if (hasFocus)
					setBackground(Color.CYAN);
				else if (isSelected)
					setBackground(new Color(224, 224, 224)); /// TABLE light gray
				else
					setBackground(Color.WHITE);
				
				return this;
			}
			
			public Color getColor(int row, int col) {
				
				if (m_result[row][col-1] > (double)m_spinAlpha.getValue() &&
					m_result[col-1][row] < (double)m_spinBeta.getValue())
					return (row < col-1 ? new Color(0, 160, 0) : new Color(255, 64, 64)); /// PLOT green red
				else if (m_result[row][col-1] < (double)m_spinBeta.getValue() &&
						m_result[col-1][row] > (double)m_spinAlpha.getValue())
					return (row < col-1 ? new Color(255, 64, 64) : new Color(0, 160, 0)); /// PLOT red green
				else
					return new Color(255, 64, 255); /// PLOT magenta
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
				res.append(m_ls);
				
				/// Table data
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols.length; j++) {
						res.append(this.getValueAt(i, cols[j]));
						if (j != cols.length-1)
							res.append("\t");
					}
					if (i != rows-1)
						res.append(m_ls);
				}
				
				/// Save to clipboard
				StringSelection ssel = new StringSelection(res.toString());
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(ssel, ssel);
				
			} catch (Exception ex) {
				showMessage("[Error] Coult not copy table data to clipboard");
			}
		}
		
		/// Show cell markers on mouse press and hold
		public void mousePressed(MouseEvent evt) {
			//int valR = this.rowAtPoint(evt.getPoint());
			//int valC = this.columnAtPoint(evt.getPoint());
			//System.out.println("mouse " + this.getValueAt(valR, valC));
			
			//int valR = this.getSelectedRow();
			//int valC = this.getSelectedColumn();
			//System.out.println("mouse " + valR + " " + valC);
			
			if (m_result != null && SwingUtilities.isLeftMouseButton(evt)) {
				MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
				resX.setColMarker(this.getSelectedColumn()-1);
				resX.setCellMarker(this.getSelectedRow(), this.getSelectedColumn()-1);
				resX.repaint();
				resX.revalidate();
			}
		}
		
		/// Hide cell markers on mouse release
		public void mouseReleased(MouseEvent evt) {
			if (m_result != null && SwingUtilities.isLeftMouseButton(evt)) {
				MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
				resX.setColMarker(-1);
				resX.setCellMarker(-1, -1);
				resX.repaint();
				resX.revalidate();
			}
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
	}
	
	/// Combo box for graphic options, only check boxes accepted
	private class MyComboBox extends JComboBox<JCheckBox> {
		
		public MyComboBox(JCheckBox[] arr) {
			super(arr);
			setFont(m_font3);
			setRenderer((ListCellRenderer<JCheckBox>)new MyComboBoxRenderer());
			addActionListener(new MyComboBoxListener());
		}
		
		private class MyComboBoxListener implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				JCheckBox chk = (JCheckBox)getSelectedItem();
				chk.setSelected(!chk.isSelected());
				/// Refresh the graphic
				if (m_result != null) {
					MyPanel resX = (MyPanel)m_scrollX.getViewport().getView();
					resX.setMyOptions(new boolean[] {
							m_chkPlotColor.isSelected(), m_chkPlotMarks.isSelected(),
							m_chkPlotGrid.isSelected(), m_chkPlotText.isSelected()});
					resX.repaint();
					resX.revalidate();
				}
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
		
		String[] lines = str.split(m_ls);
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
				m_rownames = "";
			if (sd > 0 && sr == 0)
				m_colnames = vec.get(0);
			if (sd > 0 && sr > 0)
				m_colnames = vec.get(0).substring(vec.get(0).indexOf(separator)+1);
			
			for (int i = 0; i < vec.size()-sd; i++) {
				arr = vec.get(i+sd).split(separator);
				
				for (int j = 0; j < arr.length-sr; j++) {
					if (j == 0 && sr > 0)
						m_rownames += arr[j] + separator;
					
					String val = arr[j+sr].replace(",", ".");
					if (checkDouble(val))
						dres[i][j] = Double.parseDouble(val);
				}
			}
			
		} else {
			dres = new double[arr.length-sr][vec.size()-sd];
			
			if (sr > 0)
				m_rownames = "";
			if (sd > 0 && sr == 0)
				m_colnames = vec.get(0);
			if (sd > 0 && sr > 0)
				m_colnames = vec.get(0).substring(vec.get(0).indexOf(separator)+1);
			
			for (int i = 0; i < vec.size()-sd; i++) {
				arr = vec.get(i+sd).split(separator);
				
				for (int j = 0; j < arr.length-sr; j++) {
					if (j == 0 && sr > 0)
						m_rownames += arr[j] + separator;
					
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
		int vMethod = m_cmbMethod.getSelectedIndex();
		int vMatCnt = (int)m_spinMatCnt.getValue();
		int vVariant = m_cmbVariant.getSelectedIndex();
		
		int vRowN = (m_chkRowNames.isSelected() ? 1 : 0);
		int vColN = (m_chkColNames.isSelected() ? 1 : 0);
		boolean vTranspose = m_chkTranspose.isSelected();
		boolean vOrdPair = m_chkOrdPair.isSelected();
		
		if (vMethod != 0 && vMatCnt < 3) {
			showMessage("[Warn] Requires at least 3 matrices");
			return;
		}
		
		/// Clear global variables
		m_rownames = "";
		m_colnames = "";
		m_headers = null;
		
		/// Read user input
		String data = m_textA.getText();
		if (data.length() == 0) {
			showMessage("Nothing to load");
			return;
		}
		
		/// Check for ordered pair
		if (vOrdPair && !checkOrdPair(data)) {
			showMessage("Ordered pair requires two data sets after #input1 and #input2");
			return;
		}
		
		/// Names
		String[] methodNames = new String[] {"Standard", "Aggr Average", "Aggr Max/Min", "Aggr Min/Max", "Criteria Pair"};
		String[] variantNames = new String[] {"\u03BC-biased", "Unbiased", "\u03BD-biased", "Balanced", "Weighted"};
		
		/// Result
		double[][] matR = null;
		
		/// Ordered pair - two data sets
		if (vOrdPair && checkOrdPair(data)) {
			
			Vector<String> vec1 = new Vector<String>();
			Vector<String> vec2 = new Vector<String>();
			
			try {
				boolean mark1 = false;
				boolean mark2 = false;
				
				String[] lines = data.split(m_ls);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					
					if (line.startsWith("#rownames:"))
						m_rownames = line.substring(10);
					if (line.startsWith("#colnames:"))
						m_colnames = line.substring(10);
					
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
					m_headers = makeHeader(m_rownames, m_colnames, vres1.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(m_rownames, m_colnames, vres1.get(0)[0].length, 1, vTranspose);
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
					m_headers = makeHeader(m_rownames, m_colnames, vres1.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(m_rownames, m_colnames, vres1.get(0)[0].length, 1, vTranspose);
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
					m_headers = makeHeader(m_rownames, m_colnames, vres1.size(), 0, vTranspose);
					//int vsize = vres1.get(0).length;
					//headerNames[1] = makeHeader(m_rownames, m_colnames, (vsize*vsize-vsize)/2, 1, vTranspose);
					//headerNames[2] = makeHeader(m_rownames, m_colnames, vres1.get(0).length, 0, vTranspose);
					//headerNames[3] = makeHeader(m_rownames, m_colnames, vres1.get(0)[0].length, 1, vTranspose);
					showMessage("Ordered pair - " + methodNames[vMethod] + " - " + variantNames[vVariant]);
				}
				
			} catch (Exception ex) {
				showMessage("[Error] Ordered pair - could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("[Error] Not enough memory. Start the application with: " + m_ls +
						"  java -Xmx10240m -jar ICrAData.jar");
				return;
			}
			
		/// Normal - one data set
		} else {
			
			/// Read data
			Vector<String> vec = new Vector<String>();
			try {
				String[] lines = data.split(m_ls);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					
					if (line.startsWith("#rownames:"))
						m_rownames = line.substring(10);
					if (line.startsWith("#colnames:"))
						m_colnames = line.substring(10);
					
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
					m_headers = makeHeader(m_rownames, m_colnames, vres.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(m_rownames, m_colnames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 1 || vMethod == 2 || vMethod == 3) {
					/// Aggregated
					Vector<double[][]> vres = makeData(vec, vMatCnt, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("[Warn] " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres, null, vMethod+1, vVariant+1, vOrdPair);
					m_headers = makeHeader(m_rownames, m_colnames, vres.get(0).length, 0, vTranspose);
					//headerNames[1] = makeHeader(m_rownames, m_colnames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
					
				} else if (vMethod == 4) {
					/// Criteria Pair
					Vector<double[][]> vres = makeData(vec, vMatCnt, vRowN, vColN, vTranspose);
					if (vres == null) {
						showMessage("[Warn] " + methodNames[vMethod] + " - failed to read data");
						return;
					}
					
					matR = new ICrA().makeICrA(vres, null, vMethod+1, vVariant+1, vOrdPair);
					m_headers = makeHeader(m_rownames, m_colnames, vres.size(), 0, vTranspose);
					//int vsize = vres.get(0).length;
					//headerNames[1] = makeHeader(m_rownames, m_colnames, (vsize*vsize-vsize)/2, 1, vTranspose);
					//headerNames[2] = makeHeader(m_rownames, m_colnames, vres.get(0).length, 0, vTranspose);
					//headerNames[3] = makeHeader(m_rownames, m_colnames, vres.get(0)[0].length, 1, vTranspose);
					showMessage(methodNames[vMethod] + " - " + variantNames[vVariant]);
				}
				
			} catch (Exception ex) {
				showMessage("[Error] Could not make the calculations");
				return;
				
			} catch (Error err) {
				showMessage("[Error] Not enough memory. Start the application with: " + m_ls +
						"  java -Xmx10240m -jar ICrAData.jar");
				return;
			}
		}
		
		/// Only if non-null
		if (matR != null)
			m_result = matR;
		
		/*for (int i = 0; i < m_result.length; i++) {
			for (int j = 0; j < m_result[0].length; j++)
				System.out.print(m_result[i][j] + " ");
			System.out.println();
		}*/
		
		/// Display the result
		try {
			if (m_result != null) {
				/// Create the tables
				m_scrollA.setViewportView(new MyTable(m_result, m_headers, m_cmbTable1.getSelectedIndex()));
				m_scrollB.setViewportView(new MyTable(m_result, m_headers, m_cmbTable2.getSelectedIndex()));
				/// Create the graphic
				MyPanel resX = new MyPanel();
				resX.setPreferredSize(new Dimension(400, 400));
				resX.setMyPoint(5);
				resX.setMyOptions(new boolean[] {
						m_chkPlotColor.isSelected(), m_chkPlotMarks.isSelected(),
						m_chkPlotGrid.isSelected(), m_chkPlotText.isSelected()});
				m_scrollX.setViewportView(resX);
			}
			
		} catch (Exception ex) {
			showMessage("[Error] Could not display the result");
			
		} catch (Error err) {
			showMessage("[Error] Not enough memory. Start the application with: " + m_ls +
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


/**
 * 
 * InterCriterial Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 0.7
 * Date: April 8, 2016
 * 
 */

package icadata;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultEditorKit;

@SuppressWarnings("serial")
public class ICAData extends JFrame {
	
	/// Main entry point
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ICAData();
			}
		});
	}
	
	/// Global variables
	private String ud = System.getProperty("user.dir");
	//private String fs = System.getProperty("file.separator");
	private String ls = System.getProperty("line.separator");
	
	/// DecimalFormat - # is optional, while 0 is always written
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
	private Font monoFont2 = new Font(Font.MONOSPACED, Font.BOLD, 14);
	private Color backClr = new Color(238, 238, 238);
	private Color colorAzure = makeColor("#F0FFFF");
	//private Color colorLightGray = makeColor("#D3D3D3");
	
	/// Variables for the result of the calculations
	/// 0 U  1 V  2 S  3 A  4 B  5 points
	private Vector<double[][]> result;
	private JScrollPane scrollA, scrollB;
	private JComboBox<String> comboMethod;
	private String[] methodNames = new String[] {"MU-biased", "NU-biased", "Intended"};
	
	/// Glass points, double[] saves X, Y, width, height of the rectangle
	private Vector<double[]> glassA = new Vector<double[]>();
	private Vector<double[]> glassB = new Vector<double[]>();
	private MyGlass glass;
	private JPopupMenu rightMouse;
	
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
	private ICAData() {
		
		/// This is required
		Locale.setDefault(Locale.US);
		
		/// Look and feel
		try {
			if (System.getProperty("os.name").toLowerCase().contains("windows"))
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			else
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			//Color backClr = this.getBackground();  MetalLookAndFeel 238, 238, 238  WindowsLookAndFeel 212, 208, 200
			//UIManager.put("swing.boldMetal", Boolean.FALSE);
		} catch (Exception ex) {
			/// do nothing here
		}
		
		/// Set tool-tips to show after half a second and stay 60 seconds
		ToolTipManager.sharedInstance().setInitialDelay(500);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		
		
		/// Variables for the constructor
		Dimension dim150 = new Dimension(150, 25);
		Dimension scrollSize = new Dimension(800, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		
		
		/// Items for buttons
		JButton btnOpen = new JButton("Load Data");
		btnOpen.setPreferredSize(dim150);
		btnOpen.setToolTipText("Load data by copy/paste from another program.");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenListener(evt);
			}
		});
		JButton btnView = new JButton("View Graphic");
		btnView.setPreferredSize(dim150);
		btnView.setToolTipText("View the result graphic.");
		btnView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnViewListener(evt);
			}
		});
		JButton btnExport = new JButton("Export Data");
		btnExport.setPreferredSize(dim150);
		btnExport.setToolTipText("Export the matrix data.");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnExportListener(evt);
			}
		});
		JButton btnExit = new JButton("Exit");
		btnExit.setPreferredSize(dim150);
		btnExit.setToolTipText("Exit the program.");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});
		comboMethod = new JComboBox<String>(methodNames);
		comboMethod.setToolTipText("Select a method for the calculations.");
		comboMethod.setPreferredSize(dim150);
		comboMethod.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				comboMethodListener(evt);
			}
		});
		
		/// Toolbar for buttons
		JToolBar btnBar = new JToolBar();
		btnBar.setBackground(backClr);
		btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		btnBar.setFloatable(false);
		btnBar.add(btnOpen);
		btnBar.add(comboMethod);
		btnBar.add(btnView);
		btnBar.add(btnExport);
		btnBar.add(btnExit);
		
		
		/// Right mouse menu
		JMenuItem mCut = new JMenuItem(new DefaultEditorKit.CutAction());
		mCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		mCut.setBackground(backClr);
		mCut.setText("Cut");
		JMenuItem mCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
		mCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		mCopy.setBackground(backClr);
		mCopy.setText("Copy");
		JMenuItem mPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
		mPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		mPaste.setBackground(backClr);
		mPaste.setText("Paste");
		
		rightMouse = new JPopupMenu();
		rightMouse.add(mCut);
		rightMouse.add(mCopy);
		rightMouse.add(mPaste);
		
		
		/// Items for tables
		scrollA = new JScrollPane();
		scrollA.setPreferredSize(scrollSize);
		
		scrollB = new JScrollPane();
		scrollB.setPreferredSize(scrollSize);
		
		/// Panels for tables
		JPanel panelA = new JPanel();
		panelA.setBackground(backClr);
		panelA.setLayout(new GridLayout(1, 0));
		panelA.setBorder(BorderFactory.createTitledBorder("Matrix MU"));
		panelA.add(scrollA);
		
		JPanel panelB = new JPanel();
		panelB.setBackground(backClr);
		panelB.setLayout(new GridLayout(1, 0));
		panelB.setBorder(BorderFactory.createTitledBorder("Matrix NU"));
		panelB.add(scrollB);
		
		
		/// Center panel
		JPanel panelCenter = new JPanel();
		panelCenter.setName("panelCenter");
		panelCenter.setLayout(new GridLayout(2, 0));
		panelCenter.add(panelA);
		panelCenter.add(panelB);
		
		
		/// Glass pane
		glass = new MyGlass(this.getContentPane());
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(btnBar, BorderLayout.NORTH);
		getContentPane().add(panelCenter, BorderLayout.CENTER);
		
		setTitle("ICAData v0.7");
		setGlassPane(glass);
		setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		pack(); /// Size of frame equal to the size of its components
		//setResizable(false);
		setLocationByPlatform(true);
		//setLocationRelativeTo(null); /// Center frame on screen
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/// These must be here, do not move
		//glass.setVisible(true); // this does not show cursor for resize for table columns
		setVisible(true);
		
	}
	
	/// Show custom message on the center of the screen, usable from anonymous classes
	private void showMessage(String msgType, String msgText) {
		
		if (msgType.equals("info"))
			JOptionPane.showMessageDialog(null, msgText, "Information", JOptionPane.INFORMATION_MESSAGE);
		else if (msgType.equals("warn"))
			JOptionPane.showMessageDialog(null, msgText, "Warning", JOptionPane.WARNING_MESSAGE);
		else
			JOptionPane.showMessageDialog(null, msgText, "Error", JOptionPane.ERROR_MESSAGE);
		
	}
	
	/// Check whether input is a double number
	private boolean dblParse(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Open file
	private void btnOpenListener(ActionEvent evt) {
		
		final JTextArea text = new JTextArea();
		text.setFont(monoFont2);
		text.addMouseListener(new RightMouse());
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(700, 400));
		final JFrame openFrame = new JFrame();
		final Dimension dim125 = new Dimension(125, 25);
		
		text.setText("# Use Ctrl-C/Ctrl-V shortcuts to copy/paste data from another program here." + ls +
				"# Or use the right mouse button. Lines starting with # are ignored." + ls +
				"# Valid column separators are: tab \\t semicolon ; comma ," + ls +
				"# Numbers 1,7 and 1.7 are recognized and loaded as 1.7 for the calculations." + ls + ls);
		
		JButton btnLoad = new JButton("Accept");
		btnLoad.setPreferredSize(dim125);
		btnLoad.setToolTipText("Load the data into the program.");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if ( loadData(text.getText()) )
					openFrame.setVisible(false);
			}
		});
		JButton btnClose = new JButton("Close");
		btnClose.setPreferredSize(dim125);
		btnClose.setToolTipText("Close the window.");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openFrame.setVisible(false);
			}
		});
		
		/// Toolbar for buttons
		JToolBar btnBar = new JToolBar();
		btnBar.setBackground(backClr);
		btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		btnBar.setFloatable(false);
		//btnBar.add(comboData);
		//btnBar.add(lblInfo);
		btnBar.add(btnLoad);
		btnBar.add(btnClose);
		
		/// Frame options
		openFrame.setTitle("Load Data");
		openFrame.getContentPane().setBackground(backClr);
		openFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
		
		openFrame.setLayout(new BorderLayout());
		openFrame.add(btnBar, BorderLayout.NORTH);
		openFrame.add(scrollText, BorderLayout.CENTER);
		
		openFrame.pack();
		openFrame.setLocationByPlatform(true);
		//resFrame.setLocationRelativeTo(null); /// Center frame on screen
		openFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		openFrame.setVisible(true);
		
	}
	
	/// View Graphic Result
	private void btnViewListener(ActionEvent evt) {
		
		if (result != null) {
			
			double[][] arrPoints = result.get(5);
			
			final MyPanel resPanel = new MyPanel( arrPoints );
			final JScrollPane resScroll = new JScrollPane(resPanel);
			//resScroll.setAutoscrolls(true);
			final JFrame resFrame = new JFrame();
			
			final Dimension dim125 = new Dimension(125, 25);
			final Dimension[] arrDim = new Dimension[] {
					new Dimension(500, 500),
					new Dimension(750, 750),
					new Dimension(1000, 1000),
					new Dimension(2000, 2000),
					new Dimension(3000, 3000),
					new Dimension(5000, 5000)
			};
			
			final JComboBox<String> comboSize = new JComboBox<String>(
					new String[] {"500", "750", "1000", "2000", "3000", "5000"});
			comboSize.setToolTipText("Plot size in pixels: 500x500, 750x750, etc.");
			comboSize.setPreferredSize(dim125);
			comboSize.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					
					resPanel.setPreferredSize(arrDim[comboSize.getSelectedIndex()]);
					resPanel.repaint();
					resPanel.revalidate();
					resScroll.repaint();
					resScroll.revalidate();
					
					if (comboSize.getSelectedIndex() == 0)
						resFrame.pack();
					
					//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					/*if (comboSize.getSelectedIndex() == 0) {
						resPanel.setPreferredSize(dim500);
						resPanel.repaint();
						resPanel.revalidate();
						resScroll.repaint();
						resScroll.revalidate();
						if (resFrame.getSize().getWidth() > 500 ||
							resFrame.getSize().getHeight() > 500)
							resFrame.pack();
						
					} else if (comboSize.getSelectedIndex() == 1) {
						resPanel.setPreferredSize(dim750);
						resPanel.repaint();
						resPanel.revalidate();
						resScroll.repaint();
						resScroll.revalidate();
						if (resFrame.getSize().getWidth() > 750 ||
							resFrame.getSize().getHeight() > 750)
							resFrame.pack();
						
					} else {
						resPanel.setPreferredSize(dim1000);
						resPanel.repaint();
						resPanel.revalidate();
						resScroll.repaint();
						resScroll.revalidate();
						//resFrame.pack();
						//SwingUtilities.updateComponentTreeUI(resFrame);
						//resFrame.repaint();
					}
					
					*/
					
				}
			});
			
			/// Buttons for control
			JButton btnExport = new JButton("Export");
			btnExport.setPreferredSize(dim125);
			btnExport.setToolTipText("Export the graphic.");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					
					JFileChooser chSave = new JFileChooser(ud);
					chSave.setMultiSelectionEnabled(false);
					chSave.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
					
					if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
						return;
					
					File chName = chSave.getSelectedFile();
					
					if (!chName.toString().endsWith(".png"))
						chName = new File(chName.toString() + ".png");
					
					
					try {
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
						showMessage("error", "Could not save file: " + chName.getName());
						//ex.printStackTrace();
					}
					
				}
			});
			JButton btnClose = new JButton("Close");
			btnClose.setPreferredSize(dim125);
			btnClose.setToolTipText("Close the window.");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					resFrame.setVisible(false);
				}
			});
			
			/// Toolbar for buttons
			JToolBar btnBar = new JToolBar();
			btnBar.setBackground(backClr);
			btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			btnBar.setFloatable(false);
			btnBar.add(comboSize);
			btnBar.add(btnExport);
			btnBar.add(btnClose);
			
			/// Panel options
			//resPanel.setBorder(BorderFactory.createTitledBorder("Result"));
			//resPanel.setBorder(BorderFactory.createEtchedBorder());
			resPanel.setBackground(backClr);
			resPanel.setPreferredSize(arrDim[0]);
			
			/// Frame options
			resFrame.setTitle("Result for " + methodNames[comboMethod.getSelectedIndex()]);
			resFrame.getContentPane().setBackground(backClr);
			resFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
			
			resFrame.setLayout(new BorderLayout());
			resFrame.add(btnBar, BorderLayout.NORTH);
			resFrame.add(resScroll, BorderLayout.CENTER);
			
			resFrame.pack();
			resFrame.setLocationByPlatform(true);
			//resFrame.setLocationRelativeTo(null); /// Center frame on screen
			resFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			resFrame.setVisible(true);
		}
		
	}
	
	private void btnExportListener(ActionEvent evt) {
		
		if (result != null) {
			
			final JTextArea text = new JTextArea();
			text.setFont(monoFont2);
			text.addMouseListener(new RightMouse());
			JScrollPane scrollText = new JScrollPane(text);
			scrollText.setPreferredSize(new Dimension(600, 300));
			final JFrame exFrame = new JFrame();
			final Dimension dim125 = new Dimension(125, 25);
			
			/// Separator - \t ; ,
			final String[] separator = new String[] {"\t", ";", ","};
			
			/// Locale - true for US 1.7 and false for BG 1,7
			final boolean[] locale = new boolean[] {true, false};
			
			/// Show input data
			text.setText( showArray(result.get(0), separator[0], locale[0]) );
			
			/// Combo boxes
			final JComboBox<String> comboData = new JComboBox<String>(
					new String[] {"Input Data", "Criterial Matrix", "Sign Matrix", "Matrix MU", "Matrix NU", "Plot Points"});
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
					text.setText( showArray(result.get(comboData.getSelectedIndex()),
							separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]) );
					exFrame.setTitle("Matrix for " + methodNames[comboMethod.getSelectedIndex()]);
				}
			});
			comboSeparator.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					text.setText( showArray(result.get(comboData.getSelectedIndex()),
							separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]) );
					exFrame.setTitle("Matrix for " + methodNames[comboMethod.getSelectedIndex()]);
				}
			});
			comboLocale.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					text.setText( showArray(result.get(comboData.getSelectedIndex()),
							separator[comboSeparator.getSelectedIndex()], locale[comboLocale.getSelectedIndex()]) );
					exFrame.setTitle("Matrix for " + methodNames[comboMethod.getSelectedIndex()]);
				}
			});
			
			JButton btnClose = new JButton("Close");
			btnClose.setPreferredSize(dim125);
			btnClose.setToolTipText("Close the window.");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					exFrame.setVisible(false);
				}
			});
			
			/// Toolbar for buttons
			JToolBar btnBar = new JToolBar();
			btnBar.setBackground(backClr);
			btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			btnBar.setFloatable(false);
			btnBar.add(comboData);
			btnBar.add(comboSeparator);
			btnBar.add(comboLocale);
			btnBar.add(btnClose);
			
			/// Frame options
			exFrame.setTitle("Matrix for " + methodNames[comboMethod.getSelectedIndex()]);
			exFrame.getContentPane().setBackground(backClr);
			exFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
			
			exFrame.setLayout(new BorderLayout());
			exFrame.add(btnBar, BorderLayout.NORTH);
			exFrame.add(scrollText, BorderLayout.CENTER);
			
			exFrame.pack();
			exFrame.setLocationByPlatform(true);
			//resFrame.setLocationRelativeTo(null); /// Center frame on screen
			exFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			exFrame.setVisible(true);
		}
		
	}
	
	/// Select method
	private void comboMethodListener(ItemEvent evt) {
		
		if (result != null)
			makeStuff(result.get(0), comboMethod.getSelectedIndex());
		
	}
	
	
	/// Load data from the user input
	private boolean loadData(String data) {
		
		try {
			
			if (data.length() == 0) {
				showMessage("warn", "Nothing to load.");
				return false;
			}
			
			String[] arr = data.split("\r\n|\n|\r");
			for (int i = 0; i < arr.length; i++) {
				if (!arr[i].startsWith("#") && arr[i].length() != 0)
					System.out.println(i + " | " + arr[i]);
			}
			
			/*
			String[] arr = vec.get(0).split(";");
			double[][] res = new double[vec.size()][arr.length];
			int cc = 0;
			
			for (int i = 0; i < vec.size(); i++) {
				arr = vec.get(i).split(";");
				for (int j = 0; j < arr.length; j++) {
					if (dblParse(arr[j]))
						res[cc][j] = Double.parseDouble(arr[j]);
				}
				cc++;
			}
			
			comboMethod.setSelectedIndex(0);
			makeStuff(res, 0);
			*/
			return true;
			
		} catch (Exception ex) {
			showMessage("error", "Could not load the data.");
			//ex.printStackTrace();
			return false;
		}
	}
	
	/// Make stuff happen
	private void makeStuff(double[][] arrU, int method) {
		
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
				colNames[0] = "";
				for (int j = 1; j < colNames.length; j++)
					colNames[j] = "C" + colFormat.format(j);
				
				/// Create the tables
				MyTable tableA = new MyTable(arrA, rowNames, colNames);
				scrollA.setViewportView(tableA);
				
				MyTable tableB = new MyTable(arrB, rowNames, colNames);
				scrollB.setViewportView(tableB);
				
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not make stuff happen.");
			//ex.printStackTrace();
		}
		
	}
	
	/// Make the calculations
	private Vector<double[][]> makeCalc(double[][] arrU, int method) {
		
		try {
			
			double[][] arrV = makeCrit(arrU);
			double[][] arrS = makeSign(arrV);
			
			double[][] arrA = makeA(arrS, method);
			double[][] arrB = makeB(arrS, method);
			
			double[][] arrPoints = makePlotData(arrA, arrB);
			
			Vector<double[][]> result = new Vector<double[][]>();
			result.add(0, arrU);
			result.add(1, arrV);
			result.add(2, arrS);
			result.add(3, arrA);
			result.add(4, arrB);
			result.add(5, arrPoints);
			
			return result;
			
		} catch (Exception ex) {
			showMessage("error", "Could not make the calculations.");
			//ex.printStackTrace();
			return null;
		}
		
	}
	
	/// Make V from U
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
	
	/// Make the sign of V
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
	
	
	/// Make the result MU - denote with A
	private double[][] makeA(double[][] arr, int method) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = sameElemA(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
				else
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	/// Make the result NU - denote with B
	private double[][] makeB(double[][] arr, int method) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = diffElemNoZero(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = diffElemB(arr[i], arr[j]);
				else
					res[i][j] = diffElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Compare for same elements zero, -1 -1 and 1 1 for MU - A
	private double sameElemA(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if (arrA[i] == arrB[i])
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	/// Compare for same elements -1 -1 and 1 1 for MU - A
	private double sameElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == 1) && (arrB[i] == 1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Compare for -1 and 1 elements for NU - B
	private double diffElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	/// Compare for zero, -1 and 1 elements for NU - B
	private double diffElemB(double[] arrA, double[] arrB) {
		
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
	
	
	/// Display array
	private String showArray(double[][] arr, String separator, boolean locale) {
		
		String res = "";
		
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (locale)
					res += arr[i][j] + separator;
				else
					res += String.valueOf(arr[i][j]).replace(".", ",") + separator;
					//res += NumberFormat.getInstance(Locale.US).format(arr[i][j]) + separator;
			}
			res += ls;
		}
		
		return res;
	}
	
	
	/// Colors for table
	private Color makeColor(String hex) {
		
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
				
				//getTableHeader().setFont(monoFont);
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
				
				if (dblParse( (String)value )) {
					double val = Double.parseDouble( (String)value );
					int ind = (int)Math.round(val*10);
					setForeground( makeColor(colors[ind]) );
					//setToolTipText("val " + Math.round(val*10));
					
					/*if (val == 0.0)
						setForeground(Color.RED);
					else if (val == 1.0)
						setForeground(Color.GREEN);
					else
						setForeground(Color.BLACK);*/
				}
				
				if (hasFocus)
					setBackground(colorAzure);
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
	
	/// Panel for plotting - implements MouseListener, MouseMotionListener, ComponentListener
	private class MyPanel extends JPanel implements MouseListener {
		
		private double[][] points;
		private Rectangle[] arrRect;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			
			addMouseListener(this);
			//addMouseMotionListener(this);
			//addComponentListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int width = getWidth();
			int height = getHeight();
			int delta = 20;
			
			g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
			g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
			//g2.draw(new Line2D.Double(width-delta, delta, width-delta, height-delta)); // right
			//g2.draw(new Line2D.Double(delta, delta, width-delta, delta)); // top
			
			g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			//g2.draw(new Line2D.Double(width-delta, delta, delta, height-delta)); // secondary diagonal
			
			/// Scale so that there are boundaries on the graph
			int widthScale = width-2*delta;
			int heightScale = height-2*delta;
			int line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			for (int i = 0; i < marks.length; i++)
				g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
			
			for (int i = 0; i < marks.length; i++)
				g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
			
			/// Save rectangles for glass
			arrRect = new Rectangle[points.length];
			
			int radius = 5;
			g2.setColor(Color.RED);
			//g2.setPaint(Color.RED);
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < points.length; i++) {
				double x = points[i][0]*widthScale+delta;
				double y = heightScale-points[i][1]*heightScale+delta;
				
				g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				arrRect[i] = new Rectangle((int)x-radius, (int)y-radius, 2*radius, 2*radius);
			}
			
		}
		
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
			
			/// Change cursor to crosshair
			((JPanel)evt.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			
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
			((JPanel)evt.getSource()).setCursor(Cursor.getDefaultCursor());
			glass.setVisible(false);
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
		/*public void mouseMoved(MouseEvent evt) {}
		public void mouseDragged(MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton(evt)) {
				
				//JPanel panel = (JPanel) evt.getSource();
				//int radius = 30;
				//Rectangle rect = new Rectangle(evt.getX()-radius, evt.getY()-radius, 2*radius, 2*radius);
				//panel.scrollRectToVisible(rect);
				//panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				
				
				JPanel thePanel = (JPanel) evt.getSource();
				JViewport theView = (JViewport) thePanel.getParent();
				Point cp = evt.getPoint();
				Point vp = theView.getViewPosition();
				System.out.println(cp.getX() + " " + cp.getY() + "    " + vp.getX() + " " + vp.getY());
				vp.translate(pp.x-cp.x, pp.y-cp.y);
				thePanel.scrollRectToVisible(new Rectangle(vp, theView.getSize()));
				thePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				pp.setLocation(cp);
				
				JPanel thePanel = (JPanel) evt.getSource();
				JViewport theView = (JViewport) thePanel.getParent();
				Point cp = evt.getPoint();
				Point vp = theView.getViewPosition();
				System.out.println(cp.getX() + " " + cp.getY() + "    " + vp.getX() + " " + vp.getY());
				vp.translate(pp.x-cp.x, pp.y-cp.y);
				Point qq = new Point();
				qq.x = (int)vp.getX() + 20;
				qq.y = (int)vp.getY() + 20;
				thePanel.scrollRectToVisible(new Rectangle(qq, theView.getSize()));
				thePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				pp.setLocation(cp);
			}
			
		}*/
		
		
		
		/*public void componentShown(ComponentEvent evt) {}
		public void componentHidden(ComponentEvent evt) {}
		public void componentMoved(ComponentEvent evt) {}
		
		public void componentResized(ComponentEvent evt) {
			Component panel = (Component) evt.getSource();
			resLabel.setText("Size of drawing area: " + panel.getWidth() + " " + panel.getHeight());
		}
		*/
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
				MyTable tableA = (MyTable)scrollA.getViewport().getView();
				MyTable tableB = (MyTable)scrollB.getViewport().getView();
				
				Point locA = tableA.getLocationOnScreen();
				Point locB = tableB.getLocationOnScreen();
				Point locF = cont.getParent().getLocationOnScreen();
				//System.out.println(locA.getX() + " " + locA.getY() + "     " + locF.getX() + " " + locF.getY());
				
				double[] pointA = glassA.get(i);
				double[] pointB = glassB.get(i);
				Rectangle2D rectA = new Rectangle2D.Double(
						locA.getX() - locF.getX() + pointA[0], locA.getY() - locF.getY() + pointA[1], pointA[2], pointA[3]);
				Rectangle2D rectB = new Rectangle2D.Double(
						locB.getX() - locF.getX() + pointB[0], locB.getY() - locF.getY() + pointB[1], pointB[2], pointB[3]);
				g2.setColor(Color.BLUE);
				//g2.fill(rectA);
				//g2.fill(rectB);
				g2.setStroke(new BasicStroke(2));
				g2.draw(rectA);
				g2.draw(rectB);
			}
			
		}
	}
	
	/// Listener for right mouse button on text components
	private class RightMouse implements MouseListener {
		public void mouseClicked(MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton(evt))
				rightMouse.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
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
	
	/// http://stackoverflow.com/questions/10243257/java-scroll-image-by-mouse-dragging
	
	/// http://stackoverflow.com/questions/16707397/whats-wrong-with-this-simple-double-calculation
	/// http://stackoverflow.com/questions/179427/how-to-resolve-a-java-rounding-double-issue
	/// https://blogs.oracle.com/CoreJavaTechTips/entry/the_need_for_bigdecimal
	
	
}

/**
 * 
 * InterCriterial Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 0.5
 * Date: April 3, 2016
 * 
 */

package icadata;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

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
	//private String ls = System.getProperty("line.separator");
	
	/// DecimalFormat - # is optional, while 0 is always written
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
	private Color backClr = new Color(238, 238, 238);
	private Color colorAzure = makeColor("#F0FFFF");
	//private Color colorLightGray = makeColor("#D3D3D3");
	
	/// Variables for the result of the calculations
	private Vector<double[][]> result;
	private JScrollPane scrollA, scrollB;
	
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
		
		//FlowLayout panelFlow = new FlowLayout(FlowLayout.LEADING, 5, 0);
		//FlowLayout mainFlow = new FlowLayout(FlowLayout.LEADING);
		
		
		/// Items for buttons
		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.setPreferredSize(dim150);
		btnOpenFile.setToolTipText("Open a CSV file.");
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent args) {
				btnOpenFileListener(args);
			}
		});
		JButton btnView = new JButton("View Graphic");
		btnView.setPreferredSize(dim150);
		btnView.setToolTipText("View the result graphic.");
		btnView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent args) {
				btnViewListener(args);
			}
		});
		JButton btnExit = new JButton("Exit");
		btnExit.setPreferredSize(dim150);
		btnExit.setToolTipText("Exit the program.");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent args) {
				System.exit(0);
			}
		});
		
		/// Toolbar for buttons
		JToolBar btnBar = new JToolBar();
		btnBar.setBackground(backClr);
		btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		btnBar.setFloatable(false);
		btnBar.add(btnOpenFile);
		btnBar.add(btnView);
		btnBar.add(btnExit);
		
		
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
		
		setTitle("ICAData v0.5");
		setGlassPane(glass);
		//setJMenuBar(createMenus());
		//setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		pack(); /// Size of frame equal to size of its components
		//setResizable(false);
		setLocationByPlatform(true);
		//setLocationRelativeTo(null); /// Center frame on screen
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/// These must be here, do not move
		//glass.setVisible(true); // this does not show cursor for resize for table columns
		setVisible(true);
		
	}
	
	/// Show custom message, shorter than showMessageDialog
	private void showMessage(String msgType, String msgText) {
		
		if (msgType.equals("info"))
			JOptionPane.showMessageDialog(this, msgText, "Information", JOptionPane.INFORMATION_MESSAGE);
		else if (msgType.equals("warn"))
			JOptionPane.showMessageDialog(this, msgText, "Warning", JOptionPane.WARNING_MESSAGE);
		else
			JOptionPane.showMessageDialog(this, msgText, "Error", JOptionPane.ERROR_MESSAGE);
		
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
	private void btnOpenFileListener(ActionEvent args) {
		
		JFileChooser chOpen = new JFileChooser(ud);
		chOpen.setMultiSelectionEnabled(false);
		chOpen.setFileFilter(new FileNameExtensionFilter("CSV file (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File chName = chOpen.getSelectedFile();
		
		try {
			BufferedReader reOpen = new BufferedReader(new FileReader(chName.getAbsoluteFile()));
			
			String line = "";
			Vector<String> data = new Vector<String>();
			
			while ( (line = reOpen.readLine()) != null ) {
				data.add(line);
				//System.out.println(line);
			}
			
			reOpen.close();
			
			loadData(data);
			
		} catch (Exception ex) {
			showMessage("error", "Could not open file: " + chName.getName());
			//ex.printStackTrace();
		}
	}
	
	/// Load data from file
	private void loadData(Vector<String> vec) {
		
		try {
			
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
			
			//System.out.println("open file data");
			//showArray(res);
			
			makeStuff(res);
			
		} catch (Exception ex) {
			showMessage("error", "Could not load the data.");
			//ex.printStackTrace();
		}
	}
	
	/// Make stuff happen
	private void makeStuff(double[][] arrU) {
		
		try {
			result = makeCalc(arrU);
			
			if (result != null) {
				
				double[][] arrA = result.get(0);
				double[][] arrB = result.get(1);
				
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
	
	/// View Graphic Result
	private void btnViewListener(ActionEvent args) {
		
		//if (resFrame != null)
		//	resFrame.setVisible(true);
		
		if (result != null) {
			
			double[][] arrPoints = result.get(2);
			
			final MyPanel resPanel = new MyPanel( arrPoints );
			final JFrame resFrame = new JFrame();
			
			final Dimension dim150 = new Dimension(75, 25);
			final Dimension dim500 = new Dimension(500, 500);
			final Dimension dim750 = new Dimension(750, 750);
			final Dimension dim1000 = new Dimension(1000, 1000);
			
			/// Buttons for control
			JButton btnExport = new JButton("Export");
			btnExport.setPreferredSize(dim150);
			btnExport.setToolTipText("Export the graphic.");
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {
					
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
			JButton btn500 = new JButton("500");
			btn500.setPreferredSize(dim150);
			btn500.setToolTipText("Make the size 500x500.");
			btn500.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {
					resPanel.setPreferredSize(dim500);
					resFrame.pack();
				}
			});
			JButton btn750 = new JButton("750");
			btn750.setPreferredSize(dim150);
			btn750.setToolTipText("Make the size 750x750.");
			btn750.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {
					resPanel.setPreferredSize(dim750);
					resFrame.pack();
				}
			});
			JButton btn1000 = new JButton("1000");
			btn1000.setPreferredSize(dim150);
			btn1000.setToolTipText("Make the size 1000x1000.");
			btn1000.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {
					
					//resFrame.setVisible(false);
					//Dimension qqq = ;
					///resFrame.setMinimumSize(qqq);
					resPanel.setPreferredSize(dim1000);
					//resFrame.setMaximumSize(qqq);
					resFrame.pack();
					//SwingUtilities.updateComponentTreeUI(resFrame);
					//resFrame.repaint();
					//resFrame.setVisible(true);
				}
			});
			JButton btnClose = new JButton("Close");
			btnClose.setPreferredSize(dim150);
			btnClose.setToolTipText("Close the window.");
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent args) {
					resFrame.setVisible(false);
				}
			});
			
			/// Toolbar for buttons
			JToolBar btnBar = new JToolBar();
			btnBar.setBackground(backClr);
			btnBar.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
			btnBar.setFloatable(false);
			btnBar.add(btnExport);
			btnBar.add(btn500);
			btnBar.add(btn750);
			btnBar.add(btn1000);
			btnBar.add(btnClose);
			
			/// Panel options
			//resPanel.setBorder(BorderFactory.createTitledBorder("Result"));
			resPanel.setBorder(BorderFactory.createEtchedBorder());
			Dimension panelSize = new Dimension(500, 500);
			resPanel.setBackground(backClr);
			//resPanel.setMinimumSize(panelSize);
			resPanel.setPreferredSize(panelSize);
			
			/// Status options
			//JLabel resLabel = new JLabel("Ready.");
			//resLabel.setFont(monoFont);
			
			/// Frame options
			resFrame.setTitle("Result");
			resFrame.getContentPane().setBackground(backClr);
			
			resFrame.setLayout(new BorderLayout());
			resFrame.add(btnBar, BorderLayout.NORTH);
			resFrame.add(resPanel, BorderLayout.CENTER);
			//resFrame.add(resLabel, BorderLayout.SOUTH);
			
			resFrame.pack();
			resFrame.setLocationByPlatform(true);
			//resFrame.setLocationRelativeTo(null); /// Center frame on screen
			resFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			resFrame.setVisible(true);
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
					//return data[row][col];
					return numFormat.format(data[row][col-1]);
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
	
	/// Panel for plotting - implements MouseListener, ComponentListener
	private class MyPanel extends JPanel implements MouseListener {
		
		private double[][] points;
		private Rectangle[] arrRect;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			
			addMouseListener(this);
			//addComponentListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			//int gap = 20;
			int width = getWidth();
			int height = getHeight();
			
			//g2.draw(new Line2D.Double(gap, gap, width-gap, gap));
			//g2.draw(new Line2D.Double(gap, gap, gap, height-gap));
			//g2.draw(new Line2D.Double(width-gap, gap, width-gap, height-gap));
			//g2.draw(new Line2D.Double(gap, height-gap, width-gap, height-gap));
			
			g2.draw(new Line2D.Double(0, 0, width, height));
			g2.draw(new Line2D.Double(width, 0, 0, height));
			//g2.draw(new Line2D.Double(gap, gap, w-gap, h-gap));
			//g2.draw(new Line2D.Double(1000-gap, gap, gap, 1000-gap));
			
			
			arrRect = new Rectangle[points.length];
			
			int radius = 5;
			g2.setPaint(Color.RED);
			//g2.fill(new Ellipse2D.Double(20, 20, 10, 10));
			for (int i = 0; i < points.length; i++) {
				double x = points[i][0]*width;
				double y = height-points[i][1]*height;
				
				g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				Rectangle rect = new Rectangle((int)x-radius, (int)y-radius, 2*radius, 2*radius);
				arrRect[i] = rect;
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
		
		public void mouseClicked(MouseEvent evt) {
			
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
		
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		
		/*public void componentShown(ComponentEvent evt) {}
		public void componentHidden(ComponentEvent evt) {}
		public void componentMoved(ComponentEvent evt) {}
		
		public void componentResized(ComponentEvent evt) {
			Component panel = (Component) evt.getSource();
			resLabel.setText("Size of drawing area: " + panel.getWidth() + " " + panel.getHeight());
		}
		*/
	}
	
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
				
			//Component c = SwingUtilities.getDeepestComponentAt(cont, 100, 100);
			//Component[] cc = cont.getComponents();
			//for (int i = 0; i < cc.length; i++) {
				//if (cc[i].getName().equals("panelCenter"))
				//	System.out.println(cc[i].toString());
			//}
		}
	}
	
	/// Colors
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
	
	/*private Color makeColor(double power) {
		
		double hue = power * 0.4;
		double saturation = 0.9;
		double brightness = 0.9;
		
		return Color.getHSBColor((float)hue, (float)saturation, (float)brightness);
	}*/
	
	
	/// Make the calculations
	private Vector<double[][]> makeCalc(double[][] arrU) {
		
		try {
			
			double[][] arrV = makeCrit(arrU);
			
			//System.out.println("arrV");
			//showArray(arrV);
			
			
			double[][] arrS = makeSign(arrV);
			
			//System.out.println("arrS");
			//showArray(arrS);
			
			
			double[][] arrA = makeA(arrS);
			
			//System.out.println("arrA");
			//showArray(arrA);
			
			
			double[][] arrB = makeB(arrS);
			
			//System.out.println("arrB");
			//showArray(arrB);
			
			
			double[][] arrPoints = makePlotData(arrA, arrB);
			
			System.out.println("arrPoints");
			showArray(arrPoints);
			
			Vector<double[][]> result = new Vector<double[][]>();
			result.add(0, arrA);
			result.add(1, arrB);
			result.add(2, arrPoints);
			
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
	private double[][] makeA(double[][] arr) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = sameElem(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Compare for same elements for MU - A
	private double sameElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if (arrA[i] == arrB[i])
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	
	/// Make the result NU - denote with B
	private double[][] makeB(double[][] arr) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = diffElem(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Compare for -1 and 1 elements for NU - B
	private double diffElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	
	/// Make the plot data points
	private double[][] makePlotData(double[][] resA, double[][] resB) {
		
		int rows = resA.length;
		
		/// Number of points - size of square matrix minus the diagonal elements divided by two
		double[][] points = new double[((rows*rows)-rows)/2][4];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix
				if ( i < j ) {
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
	private void showArray(double[][] arr) {
		
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
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
	
	
}

/**
 * 
 * InterCriterial Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 0.5
 * Date: April 2, 2016
 * 
 */

package icadata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

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
	
	/// Variables for the result of the calculations
	private Vector<double[][]> result;
	private JScrollPane scrollA, scrollB;
	private JLabel resLabel;
	
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
		
		/// This is for safety, not really needed
		Locale.setDefault(Locale.US);
		
		/// Look and feel
		try {
			/// Bold monospaced fonts are better on Windows
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//monoFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
				//monoFont2 = new Font(Font.MONOSPACED, Font.BOLD, 14);
			} else
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			//Color backClr = this.getBackground();  MetalLookAndFeel 238, 238, 238  WindowsLookAndFeel 212, 208, 200
			
			/// In case cross-platform look and feel is loaded - no bold fonts
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		} catch (Exception ex) {
			/// Even on error - no bold fonts when cross-platform
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}
		
		
		/// Variables for the constructor
		Dimension dim150 = new Dimension(150, 25);
		
		Dimension scrollSize = new Dimension(800, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		
		//FlowLayout panelFlow = new FlowLayout(FlowLayout.LEADING, 5, 0);
		//FlowLayout mainFlow = new FlowLayout(FlowLayout.LEADING);
		
		makeColor("#1E90FF");
		
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
		btnView.setToolTipText("View the graphic again.");
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
		panelCenter.setLayout(new GridLayout(2, 0));
		panelCenter.add(panelA);
		panelCenter.add(panelB);
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(btnBar, BorderLayout.NORTH);
		getContentPane().add(panelCenter, BorderLayout.CENTER);
		
		setTitle("ICAData v0.5");
		//setJMenuBar(createMenus());
		//setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		pack();
		//setResizable(false);
		setLocationByPlatform(true);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
				System.out.println(line);
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
			
			System.out.println("open file data");
			showArray(res);
			
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
				
				String[] colNames = new String[arrA[0].length];
				for (int i = 0; i < colNames.length; i++)
					colNames[i] = "C" + Integer.valueOf(i + 1);
				
				MyTable tableA = new MyTable(arrA, colNames);
				scrollA.setViewportView(tableA);
				
				MyTable tableB = new MyTable(arrB, colNames);
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
			
			MyPanel resPanel = new MyPanel( arrPoints );
			//resPanel.setBorder(BorderFactory.createTitledBorder("Result"));
			resPanel.setBorder(BorderFactory.createEtchedBorder());
			Dimension panelSize = new Dimension(500, 500);
			//resPanel.setMinimumSize(panelSize);
			resPanel.setPreferredSize(panelSize);
			
			resLabel = new JLabel("Ready.");
			resLabel.setFont(monoFont);
			
			JFrame resFrame = new JFrame();
			resFrame.setTitle("Result");
			resFrame.getContentPane().setBackground(backClr);
			
			resFrame.setLayout(new BorderLayout());
			resFrame.add(resPanel, BorderLayout.CENTER);
			resFrame.add(resLabel, BorderLayout.SOUTH);
			
			resFrame.pack();
			resFrame.setLocationByPlatform(true);
			resFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			resFrame.setVisible(true);
		}
		
	}
	
	
	/// Table for data view
	private class MyTable extends JTable {
		
		private double[][] data;
		private String[] cols;
		
		private MyTable(double[][] arrData, String[] colNames) {
			if (arrData != null) {
				data = arrData;
				cols = colNames;
				
				setModel(new MyTableModel());
				setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				setDefaultRenderer(Double.class, new MyCellRenderer());
				//setDefaultRenderer(String.class, new MyCellRenderer());
			}
		}
		
		private class MyTableModel extends AbstractTableModel {
			
			public int getRowCount() {
				return data.length;
			}
			
			public int getColumnCount() {
				return data[0].length;
			}
			
			public Object getValueAt(int row, int col) {
				//return data[row][col];
				return numFormat.format(data[row][col]);
			}
			
			public Class<?> getColumnClass(int col) {
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
					setToolTipText("val " + Math.round(val*10));
					
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
		
	}
	
	/// Panel for plotting
	private class MyPanel extends JPanel implements MouseListener, ComponentListener {
		
		private double[][] points;
		private Rectangle[] arrRect;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			
			addMouseListener(this);
			addComponentListener(this);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponents(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int gap = 20;
			int width = getWidth();
			int height = getHeight();
			
			g2.draw(new Line2D.Double(gap, gap, width-gap, gap));
			g2.draw(new Line2D.Double(gap, gap, gap, height-gap));
			
			//g2.draw(new Line2D.Double(gap, gap, w-gap, h-gap));
			g2.draw(new Line2D.Double(0, 0, width, height));
			
			g2.draw(new Line2D.Double(width-gap, gap, width-gap, height-gap));
			g2.draw(new Line2D.Double(gap, height-gap, width-gap, height-gap));
			
			//g2.draw(new Line2D.Double(1000-gap, gap, gap, 1000-gap));
			g2.draw(new Line2D.Double(width, 0, 0, height));
			
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
		
		public void mouseClicked(MouseEvent evt) {
			
			for (int i = 0; i < arrRect.length; i++) {
				Rectangle rect = arrRect[i];
				if (rect.contains(evt.getX(), evt.getY()))
					resLabel.setText("Coordinates: " + points[i][0] + " " + points[i][1] +
						"   Point: " + evt.getX() + " " + evt.getY());
			}
			
		}
		
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		
		public void componentShown(ComponentEvent evt) {}
		public void componentHidden(ComponentEvent evt) {}
		public void componentMoved(ComponentEvent evt) {}
		
		public void componentResized(ComponentEvent evt) {
			Component panel = (Component) evt.getSource();
			resLabel.setText("Size of drawing area: " + panel.getWidth() + " " + panel.getHeight());
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
			
			System.out.println("arrV");
			showArray(arrV);
			
			
			double[][] arrS = makeSign(arrV);
			
			System.out.println("arrS");
			showArray(arrS);
			
			
			double[][] arrA = makeA(arrS);
			
			System.out.println("arrA");
			showArray(arrA);
			
			
			double[][] arrB = makeB(arrS);
			
			System.out.println("arrB");
			showArray(arrB);
			
			
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
		double[][] points = new double[((rows*rows)-rows)/2][2];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix
				if ( i > j ) {
					points[cc][0] = resA[i][j];
					points[cc][1] = resB[i][j];
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
	/// http://stackoverflow.com/questions/340209/generate-colors-between-red-and-green-for-a-power-meter
	/// http://www.javamex.com/tutorials/conversion/decimal_hexadecimal.shtml
	/// http://stackoverflow.com/questions/2303305/window-resize-event
	/// http://stackoverflow.com/questions/2106367/listen-to-jframe-resize-events-as-the-user-drags-their-mouse  super.validate()
	
	
}

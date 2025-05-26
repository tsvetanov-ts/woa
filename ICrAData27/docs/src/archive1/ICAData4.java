/**
 * 
 * InterCriterial Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 0.0.2
 * Date: April 2, 2016
 * 
 */

package icadata;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
	
	private Color backClr = new Color(238, 238, 238);
	
	private JScrollPane scrollMU, scrollNU;
	private JDialog resDialog;
	
	
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
		Dimension dim150 = new Dimension(150, 23);
		
		Dimension scrollSize = new Dimension(600, 200);
		Dimension frameSize = new Dimension(1000, 700);
		
		FlowLayout panelFlow = new FlowLayout(FlowLayout.LEADING, 5, 0);
		FlowLayout mainFlow = new FlowLayout(FlowLayout.LEADING);
		
		
		/// Items for panel buttons
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
		
		
		//tableMU = new MyTable();
		//t1.setPreferredSize(new Dimension(100, 100));
		scrollMU = new JScrollPane();
		scrollMU.setPreferredSize(scrollSize);
		
		scrollNU = new JScrollPane();
		scrollNU.setPreferredSize(scrollSize);
		
		
		JPanel pButtons = new JPanel();
		pButtons.setBackground(backClr);
		pButtons.setLayout(panelFlow);
		pButtons.setBorder(BorderFactory.createTitledBorder("Buttons"));
		pButtons.add(btnOpenFile);
		pButtons.add(btnView);
		pButtons.add(btnExit);
		
		JPanel pMU = new JPanel();
		pMU.setBackground(backClr);
		pMU.setLayout(panelFlow);
		pMU.setBorder(BorderFactory.createTitledBorder("Matrix MU"));
		pMU.add(scrollMU);
		
		JPanel pNU = new JPanel();
		pNU.setBackground(backClr);
		pNU.setLayout(panelFlow);
		pNU.setBorder(BorderFactory.createTitledBorder("Matrix NU"));
		pNU.add(scrollNU);
		
		
		/// Define frame layout
		setLayout(mainFlow);
		//add(panelWest, BorderLayout.WEST);
		//add(scrollCenter, BorderLayout.CENTER);
		
		getContentPane().add(pButtons);
		getContentPane().add(pMU);
		getContentPane().add(pNU);
		
		
		setTitle("ICAData v0.0.2");
		//setJMenuBar(createMenus());
		//setIconImage(Toolkit.getDefaultToolkit().getImage("docs/icon.jpg"));
		
		setMinimumSize(frameSize);
		setPreferredSize(frameSize);
		//pack();
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
	
	/// Open file
	private void btnViewListener(ActionEvent args) {
		
		if (resDialog != null)
			resDialog.setVisible(true);
		
	}
	
	/// Make stuff happen
	private void makeStuff(double[][] arrU) {
		
		Vector<double[][]> result = makeCalc(arrU);
		if (result != null) {
			
			double[][] arrMU = result.get(0);
			double[][] arrNU = result.get(1);
			double[][] arrPoints = result.get(2);
			
			Object[] colMU = new Object[arrMU[0].length];
			for (int i = 0; i < colMU.length; i++)
				colMU[i] = "Column" + i;
			
			MyTable tableMU = new MyTable(arrMU);
			scrollMU.setViewportView(tableMU);
			
			MyTable tableNU = new MyTable(arrNU);
			scrollNU.setViewportView(tableNU);
			
			
			
			
			MyPanel pResult = new MyPanel( arrPoints );
			
			resDialog = new JDialog();
			resDialog.setTitle("Result");
			//resDialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
			
			resDialog.add(pResult);
			
			Dimension pDialogSize = new Dimension(1000, 1000);
			resDialog.setMinimumSize(pDialogSize);
			resDialog.setPreferredSize(pDialogSize);
			//polDialog.setLocationRelativeTo(list); // problematic when there is horizontal scrollbar
			
			//resDialog.setVisible(true);
		}
		
	}
	
	/// Table for data view
	private class MyTable extends JTable {
		
		private double[][] data;
		
		private MyTable(double[][] arrData) {
			data = arrData;
			
			setModel(new MyTableModel());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setDefaultRenderer(Double.class, new MyCellRenderer());
		}
		
		private class MyTableModel extends AbstractTableModel {
			
			public int getRowCount() {
				return data.length;
			}
			
			public int getColumnCount() {
				return data[0].length;
			}
			
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
			
			public Class<?> getColumnClass(int col) {
				return Double.class;
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
				
				if (value instanceof Double) {
					setHorizontalAlignment(SwingConstants.RIGHT);
				}
				
				if (hasFocus)
					this.setBackground(Color.BLUE);
				else if (isSelected)
					this.setBackground(Color.LIGHT_GRAY);
				else
					this.setBackground(Color.WHITE);
				
				return this;
			}
		}
		
	}
	
	/// Panel for plotting
	private class MyPanel extends JPanel {
		
		private double[][] points;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
		}
		
		//http://docs.oracle.com/javase/tutorial/2d/
		protected void paintComponent(Graphics g) {
			super.paintComponents(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int d = 5;
			int w = getWidth();
			int h = getHeight();
			
			g2.draw(new Line2D.Double(d, d, w-d, d));
			g2.draw(new Line2D.Double(d, d, d, h-d));
			//g2.draw(new Line2D.Double(d, d, w-d, h-d));
			
			g2.draw(new Line2D.Double(w-d, d, w-d, h-d));
			g2.draw(new Line2D.Double(d, h-d, w-d, h-d));
			
			g2.draw(new Line2D.Double(1000, 0, 0, 1000));
			
			g2.setPaint(Color.RED);
			//g2.fill(new Ellipse2D.Double(20, 20, 10, 10));
			for (int i = 0; i < points.length; i++) {
				g2.fill(new Ellipse2D.Double(points[i][0]*1000, points[i][1]*1000, 10, 10));
			}
			
		}
		
	}
	
	
	/// Make the calculations
	private Vector<double[][]> makeCalc(double[][] arrU) {
		
		try {
			
			double[][] arrV = makeCrit(arrU);
			
			System.out.println("arrV");
			showArray(arrV);
			
			
			double[][] arrS = makeSign(arrV);
			
			System.out.println("arrS");
			showArray(arrS);
			
			
			double[][] arrMU = makeMU(arrS);
			
			System.out.println("arrMU");
			showArray(arrMU);
			
			
			double[][] arrNU = makeNU(arrS);
			
			System.out.println("arrNU");
			showArray(arrNU);
			
			
			double[][] arrPoints = makePlotData(arrMU, arrNU);
			
			System.out.println("arrPoints");
			showArray(arrPoints);
			
			Vector<double[][]> result = new Vector<double[][]>();
			result.add(0, arrMU);
			result.add(1, arrNU);
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
	
	
	/// Make the result MU
	private double[][] makeMU(double[][] arr) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = sameElem(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Compare for same elements for MU
	private double sameElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if (arrA[i] == arrB[i])
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	
	/// Make the result NU
	private double[][] makeNU(double[][] arr) {
		
		int rows = arr.length;
		
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				res[i][j] = diffElem(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Compare for -1 and 1 elements for NU
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
	
}

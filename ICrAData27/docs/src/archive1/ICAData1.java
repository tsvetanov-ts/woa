package icadata;

public class ICAData {
	
	/// Main entry point
	public static void main(String[] args) {
		new ICAData();
	}
	
	/// Constructor
	private ICAData() {
		
		System.out.println("works");
		
		int rows = 4;
		int cols = 5;
		
		double[][] arrU = new double[rows][cols];
		
		arrU[0][0] = 6;
		arrU[0][1] = 5;
		arrU[0][2] = 3;
		arrU[0][3] = 7;
		arrU[0][4] = 6;
		
		arrU[1][0] = 7;
		arrU[1][1] = 7;
		arrU[1][2] = 8;
		arrU[1][3] = 1;
		arrU[1][4] = 3;
		
		arrU[2][0] = 4;
		arrU[2][1] = 3;
		arrU[2][2] = 5;
		arrU[2][3] = 9;
		arrU[2][4] = 1;
		
		arrU[3][0] = 4;
		arrU[3][1] = 5;
		arrU[3][2] = 6;
		arrU[3][3] = 7;
		arrU[3][4] = 8;
		
		System.out.println("arrU");
		showArray(arrU);
		
		
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
			
		} catch (Exception ex) {
			ex.printStackTrace();
			//System.out.println(ex.toString());
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
		
		double res = (double) cc/arrA.length;
		
		return res;
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
		
		double res = (double) cc/arrA.length;
		
		return res;
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

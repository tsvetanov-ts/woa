/**
 * 
 * InterCriteria Analysis
 * 
 */

package icradata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class ICrA {
	
	/// Constructor
	public ICrA() {
	}
	
	/// Make Standard ICrA
	public HashMap<String, double[][]> makeStandard(
			Vector<double[][]> vecU, Vector<double[][]> vecU2,
			int method, boolean isPair) {
		
		if (isPair)
			return makeCalc(vecU.get(0), vecU2.get(0), method, isPair);
		else
			return makeCalc(vecU.get(0), null, method, isPair);
	}
	
	/// Make Second Order ICrA
	public HashMap<String, double[][]> makeSecondOrder(
			Vector<double[][]> vecU, Vector<double[][]> vecU2,
			int method, boolean isPair) {
		
		try {
			int vecsize = vecU.size();
			int matsize = vecU.get(0).length;
			int secsize = (matsize*matsize-matsize)/2;
			
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			HashMap<String, double[][]> calc = null;
			double[][] calcA = new double[vecsize][secsize];
			double[][] calcB = new double[vecsize][secsize];
			
			/// Save calculations in calcA and calcB
			for (int k = 0; k < vecsize; k++) {
				calc = (isPair ?
					makeCalc(vecU.get(k), vecU2.get(k), method, isPair) :
					makeCalc(vecU.get(k), null, method, isPair));
				
				double[][] mat = calc.get("MatrixMU");
				int cc = 0;
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						if (i < j)
							calcA[k][cc++] = mat[i][j];
				
				mat = calc.get("MatrixNU");
				cc = 0;
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						if (i < j)
							calcB[k][cc++] = mat[i][j];
				
				Iterator<String> it = calc.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					res.put("Y" + Integer.valueOf(k + 1) + key, calc.get(key));
				}
			}
			
			/// Second Order ICrA
			calc = makeCalc(calcA, calcB, method, true);
			Iterator<String> it = calc.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				res.put(key, calc.get(key));
			}
			
			return res;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/// Make Aggregated ICrA
	public HashMap<String, double[][]> makeAggregated(
			Vector<double[][]> vecU, Vector<double[][]> vecU2,
			int aggr, int method, boolean isPair) {
		
		try {
			int vecsize = vecU.size();
			int matsize = vecU.get(0).length;
			
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			double[][] arrA = new double[matsize][matsize];
			double[][] arrB = new double[matsize][matsize];
			
			HashMap<String, double[][]> calc = null;
			double[][][] calcA = new double[vecsize][matsize][matsize];
			double[][][] calcB = new double[vecsize][matsize][matsize];
			
			/// Save calculations in calcA and calcB
			for (int k = 0; k < vecsize; k++) {
				calc = (isPair ?
					makeCalc(vecU.get(k), vecU2.get(k), method, isPair) :
					makeCalc(vecU.get(k), null, method, isPair));
				
				double[][] mat = calc.get("MatrixMU");
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						calcA[k][i][j] = mat[i][j];
				
				mat = calc.get("MatrixNU");
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						calcB[k][i][j] = mat[i][j];
				
				Iterator<String> it = calc.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					res.put("Z" + Integer.valueOf(k + 1) + key, calc.get(key));
				}
			}
			
			/// Save the aggregation in arrA and arrB
			/// Average 0  MaxMin 1  MinMax 2
			for (int i = 0; i < matsize; i++) {
				for (int j = 0; j < matsize; j++) {
					if (aggr == 0) {
						double valA = 0;
						double valB = 0;
						for (int k = 0; k < vecsize; k++) {
							valA += calcA[k][i][j];
							valB += calcB[k][i][j];
						}
						
						arrA[i][j] = (double)valA/vecsize;
						arrB[i][j] = (double)valB/vecsize;
						
					} else if (aggr == 1) {
						double valA = 0;
						double valB = 1000000;
						for (int k = 0; k < vecsize; k++) {
							if (valA < calcA[k][i][j])
								valA = calcA[k][i][j];
							if (valB > calcB[k][i][j])
								valB = calcB[k][i][j];
						}
						
						arrA[i][j] = valA;
						arrB[i][j] = valB;
						
					} else {
						double valA = 1000000;
						double valB = 0;
						for (int k = 0; k < vecsize; k++) {
							if (valA > calcA[k][i][j])
								valA = calcA[k][i][j];
							if (valB < calcB[k][i][j])
								valB = calcB[k][i][j];
						}
						
						arrA[i][j] = valA;
						arrB[i][j] = valB;
					}
				}
			}
			
			/// Make distance
			double[][] arrDist = makeDistance(arrA, arrB);
			
			/// Plot points
			double[][] arrPoints = makePlotData(arrA, arrB, arrDist);
			
			/// Vector data for export
			double[][] vectorA = makeVectorData(arrA);
			double[][] vectorA2 = makeVectorData2(arrA);
			double[][] vectorB = makeVectorData(arrB);
			double[][] vectorB2 = makeVectorData2(arrB);
			double[][] vectorDist = makeVectorData(arrDist);
			double[][] vectorDist2 = makeVectorData2(arrDist);
			
			/// Save the result
			res.put("MatrixMU", arrA);
			res.put("MatrixNU", arrB);
			res.put("MatrixDist", arrDist);
			res.put("PlotPoints", arrPoints);
			
			res.put("VectorMU", vectorA);
			res.put("VectorMUalt", vectorA2);
			res.put("VectorNU", vectorB);
			res.put("VectorNUalt", vectorB2);
			res.put("VectorDist", vectorDist);
			res.put("VectorDistalt", vectorDist2);
			
			return res;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/// Make the calculations
	private HashMap<String, double[][]> makeCalc(double[][] arrU, double[][] arrU2, int method, boolean isPair) {
		
		try {
			/// Criteria matrix
			double[][] arrV = new double[1][1];
			/// Sign matrix
			double[][] arrS = new double[1][1];
			
			if (isPair)
				arrS = makeCritSign(arrU, arrU2);
			else {
				arrV = makeCrit(arrU);
				arrS = makeSign(arrV);
			}
			
			/// Matrix MU and NU
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
			
			/// Make distance
			double[][] arrDist = makeDistance(arrA, arrB);
			
			/// Plot points
			double[][] arrPoints = makePlotData(arrA, arrB, arrDist);
			
			/// Vector data for export
			double[][] vectorA = makeVectorData(arrA);
			double[][] vectorA2 = makeVectorData2(arrA);
			double[][] vectorB = makeVectorData(arrB);
			double[][] vectorB2 = makeVectorData2(arrB);
			double[][] vectorDist = makeVectorData(arrDist);
			double[][] vectorDist2 = makeVectorData2(arrDist);
			
			/// Result as hash map
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			res.put("Input1", arrU);
			if (isPair)
				res.put("Input2", arrU2);
			else
				res.put("CriteriaMatrix", arrV);
			res.put("SignMatrix", arrS);
			
			res.put("MatrixMU", arrA);
			res.put("MatrixNU", arrB);
			res.put("MatrixDist", arrDist);
			res.put("PlotPoints", arrPoints);
			
			res.put("VectorMU", vectorA);
			res.put("VectorMUalt", vectorA2);
			res.put("VectorNU", vectorB);
			res.put("VectorNUalt", vectorB2);
			res.put("VectorDist", vectorDist);
			res.put("VectorDistalt", vectorDist2);
			
			return res;
			
		} catch (Exception ex) {
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
	
	/// Make criteria/sign matrix for ordered pair
	private double[][] makeCritSign(double[][] arr, double[][] arr2) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][(cols*(cols-1))/2];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					if (arr[i][k] >= arr[i][j+1] && arr2[i][k] < arr2[i][j+1])
						res[i][cc] = 1;
					else if (arr[i][k] > arr[i][j+1] && arr2[i][k] <= arr2[i][j+1])
						res[i][cc] = 1;
					else if (arr[i][k] <= arr[i][j+1] && arr2[i][k] > arr2[i][j+1])
						res[i][cc] = -1;
					else if (arr[i][k] < arr[i][j+1] && arr2[i][k] >= arr2[i][j+1])
						res[i][cc] = -1;
					else if (arr[i][k] == arr[i][j+1] && arr2[i][k] == arr2[i][j+1])
						res[i][cc] = 0;
					else
						res[i][cc] = 9;
					cc++;
				}
			}
			cc = 0;
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
			if( ((arrA[i] == 1) && (arrB[i] == 1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) ||
				((arrA[i] == 0) && (arrB[i] == 0)) )
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
	
	/// Mean value for two matrices - (A1{1,1} + A2{1,1})/2
	private double[][] matrixMeanValue(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = (arrA[i][j] + arrB[i][j])/2;
		
		return res;
	}
	
	/// Add two matrices - A1{1,1} + A2{1,1}
	private double[][] matrixAddition(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = arrA[i][j] + arrB[i][j];
		
		return res;
	}
	
	/// Matrix for method Weighted
	private double[][] matrixWeighted(double[][] arr, double[][] arrP) {
		
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
	
	/// Make distance
	private double[][] makeDistance(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = Math.sqrt((1-arrA[i][j])*(1-arrA[i][j]) + arrB[i][j]*arrB[i][j]);
				//res[i][j] = Math.sqrt(Math.pow(1-arrA[i][j], 2) + Math.pow(arrB[i][j], 2));
				//res[i][j] = Math.pow(Math.pow(1-arrA[i][j], 2) + Math.pow(arrB[i][j], 2), 0.5);
		
		return res;
	}
	
	/// Make plot data points
	private double[][] makePlotData(double[][] arrA, double[][] arrB, double[][] arrDist) {
		
		int rows = arrA.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][5];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix and diagonal elements
				if ( i < j ) {
					res[cc][0] = arrA[i][j]; // coordinate x
					res[cc][1] = arrB[i][j]; // coordinate y
					res[cc][2] = i; // index row
					res[cc][3] = j; // index column
					res[cc][4] = arrDist[i][j]; // distance
					cc++;
				}
			}
		}
		
		return res;
	}
	
	/// Make vector data
	private double[][] makeVectorData(double[][] arr) {
		
		int rows = arr.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][3];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix
				if ( i < j ) {
					res[cc][0] = arr[i][j]; // value
					res[cc][1] = i; // index row
					res[cc][2] = j; // index column
					cc++;
				}
			}
		}
		
		return res;
	}
	
	/// Make vector data 2
	private double[][] makeVectorData2(double[][] arr) {
		
		int rows = arr.length;
		/// Lower triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][3];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get lower triangular matrix
				if ( i > j ) {
					res[cc][0] = arr[i][j]; // value
					res[cc][1] = i; // index row
					res[cc][2] = j; // index column
					cc++;
				}
			}
		}
		
		return res;
	}
	
}

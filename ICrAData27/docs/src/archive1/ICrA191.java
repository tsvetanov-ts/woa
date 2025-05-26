/**
 * 
 * InterCriteria Analysis
 * 
 */

import java.util.Vector;

public class ICrA {
	
	/// Constructor
	public ICrA() {
	}
	
	/// Make criteria matrix
	private double[][] makeCrit(double[][] matA) {
		
		int rows = matA.length;
		int cols = matA[0].length;
		double[][] matC = new double[rows][(cols*(cols-1))/2];
		
		for (int i = 0; i < rows; i++) {
			int cc = 0;
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					matC[i][cc++] = Math.signum(matA[i][k] - matA[i][j+1]);
				}
			}
		}
		
		return matC;
	}
	
	/// Make criteria matrix for ordered pair
	private double[][] makeCritPair(double[][] matA, double[][] matB) {
		
		int rows = matA.length;
		int cols = matA[0].length;
		double[][] matC = new double[rows][(cols*(cols-1))/2];
		
		for (int i = 0; i < rows; i++) {
			int cc = 0;
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					if (matA[i][k] >= matA[i][j+1] && matB[i][k] < matB[i][j+1])
						matC[i][cc] = 1;
					else if (matA[i][k] > matA[i][j+1] && matB[i][k] <= matB[i][j+1])
						matC[i][cc] = 1;
					else if (matA[i][k] <= matA[i][j+1] && matB[i][k] > matB[i][j+1])
						matC[i][cc] = -1;
					else if (matA[i][k] < matA[i][j+1] && matB[i][k] >= matB[i][j+1])
						matC[i][cc] = -1;
					else if (matA[i][k] == matA[i][j+1] && matB[i][k] == matB[i][j+1])
						matC[i][cc] = 0;
					else
						matC[i][cc] = 9;
					cc++;
				}
			}
		}
		
		return matC;
	}
	
	/// Counter A - equal elements:  0==0  -1==-1  1==1  (flag==1)   0=/=0  -1==-1  1==1  (flag==0)
	private double counterA(double[] arrA, double[] arrB, int size, int flag) {
		
		int cc = 0;
		for (int i = 0; i < size; i++) {
			if (((arrA[i] ==  1) && (arrB[i] ==  1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) ||
				((arrA[i] ==  0) && (arrB[i] ==  0) && flag==1))
				cc++;
		}
		
		return (double)cc/size;
	}

	/// Counter B - different elements:  0==0  -1=/=1  1=/=-1  (flag==0)   0=/=0  -1=/=1  1=/=-1  (flag==1)
	private double counterB(double[] arrA, double[] arrB, int size, int flag) {
		
		int cc = 0;
		for (int i = 0; i < size; i++) {
			if (((arrA[i] == -1) && (arrB[i] ==  1)) ||
				((arrA[i] ==  1) && (arrB[i] == -1)) ||
				((arrA[i] ==  0) && (arrB[i] ==  0) && flag==1))
				cc++;
		}
		
		return (double)cc/size;
	}
	
	/// Make result - mubiased1 flags 1 0, unbiased2 flags 0 0, nubiased3 flags 0 1
	private double[][] makeResult(double[][] matC, int icavar) {
		
		int rows = matC.length;
		int cols = matC[0].length;
		double[][] matR = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			/// upper triangular - matrix MU-A
			for (int j = i+1; j < rows; j++)
				matR[i][j] = counterA(matC[i], matC[j], cols, (icavar==1 ? 1 : 0));
			/// lower triangular - matrix NU-B
			for (int j = 0; j < i; j++)
				matR[i][j] = counterB(matC[i], matC[j], cols, (icavar==3 ? 1 : 0));
			/// ignore diagonal elements
		}
		
		return matR;
	}
	
	/// Balanced4 - mean value for mubiasedR1 and nubiasedR3 - (R1{i,j} + R3{i,j})/2
	private double[][] varBalanced(double[][] matR1, double[][] matR3) {
		
		int rows = matR1.length;
		double[][] matR = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				if (i != j)
					matR[i][j] = (matR1[i][j] + matR3[i][j])/2;
		
		return matR;
	}
	
	/// Weighted5 - each element of unbiasedR2 divided by the sum of MU+NU - R2{i,j}/(R2{i,j} + R2{j,i})
	private double[][] varWeighted(double[][] matR2) {
		
		int rows = matR2.length;
		double[][] matR = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				if (i != j)
					matR[i][j] = (matR2[i][j] + matR2[j][i] != 0 ? matR2[i][j]/(matR2[i][j] + matR2[j][i]) : 0.5);
		
		return matR;
	}
	
	/// Make calculations - variant - mubiased1, Unbiased2, nubiased3, Balanced4, Weighted5
	private double[][] makeCalc(double[][] matW, double[][] matZ, int icavar, boolean pair) {
		
		/// Criteria matrix - sign of difference between elements per row
		double[][] matC = (pair ? makeCritPair(matW, matZ) : makeCrit(matW));
		
		/// Result matrix - MU-A uppper triangular, NU-B lower triangular, ignore diagonal elements
		double[][] matR = null;
		
		/// Variant - mubiased1, Unbiased2, nubiased3, Balanced4, Weighted5
		if (icavar == 1 || icavar == 2 || icavar == 3) {
			matR = makeResult(matC, icavar);
			
		} else if (icavar == 4) {
			/// Make mubiased1, nubiased3
			double[][] matR1 = makeResult(matC, 1);
			double[][] matR3 = makeResult(matC, 3);
			/// Balanced4 - mean value of 1 and 3
			matR = varBalanced(matR1, matR3);
			
		} else if (icavar == 5) {
			/// Make Unbiased2
			double[][] matR2 = makeResult(matC, 2);
			/// Weighted5 - each element of 2 divided by the sum of (i,j)+(j,i)
			matR = varWeighted(matR2);
		}
		
		return matR;
	}
	
	/// Make ICrA - method - Standard 1, Aggregated Average2 MaxMin3 MinMax4, Criteria Pair 5
	public double[][] makeICrA(Vector<double[][]> vecW, Vector<double[][]> vecZ, int icamth, int icavar, boolean pair) {
		
		try {
			/// Standard ICrA, ignore matrix count
			if (icamth == 1) {
				return (pair ?
						makeCalc(vecW.get(0), vecZ.get(0), icavar, pair) :
						makeCalc(vecW.get(0), null, icavar, pair));
				
			/// Aggregated ICrA
			} else if (icamth == 2 || icamth == 3 || icamth == 4) {
				
				int vecsize = vecW.size();
				int matsize = vecW.get(0).length;
				
				/// Result
				double[][] matR = new double[matsize][matsize];
				
				/// Result for matrix count
				double[][][] aggr = new double[vecsize][matsize][matsize];
				for (int k = 0; k < vecsize; k++) {
					aggr[k] = ( pair ?
								makeCalc(vecW.get(k), vecZ.get(k), icavar, pair) :
								makeCalc(vecW.get(k), null, icavar, pair) );
				}
				
				/// Aggregation
				if (icamth == 2) { /// average
					for (int i = 0; i < matsize; i++) {
						for (int j = 0; j < matsize; j++) {
							if (i != j) {
								double val = 0;
								for (int k = 0; k < vecsize; k++)
									val += aggr[k][i][j];
								matR[i][j] = val/vecsize;
							}
						}
					}
					
				} else if (icamth == 3) { /// maxmin
					for (int i = 0; i < matsize; i++) {
						for (int j = i+1; j < matsize; j++) { /// max MU-A
							double val = 0;
							for (int k = 0; k < vecsize; k++)
								if (val < aggr[k][i][j])
									val = aggr[k][i][j];
							matR[i][j] = val;
						}
						for (int j = 0; j < i; j++) { /// min NU-B
							double val = 1000000;
							for (int k = 0; k < vecsize; k++)
								if (val > aggr[k][i][j])
									val = aggr[k][i][j];
							matR[i][j] = val;
						}
					}
					
				} else if (icamth == 4) { /// minmax
					for (int i = 0; i < matsize; i++) {
						for (int j = i+1; j < matsize; j++) { /// min MU-A
							double val = 1000000;
							for (int k = 0; k < vecsize; k++)
								if (val > aggr[k][i][j])
									val = aggr[k][i][j];
							matR[i][j] = val;
						}
						for (int j = 0; j < i; j++) { /// max NU-B
							double val = 0;
							for (int k = 0; k < vecsize; k++)
								if (val < aggr[k][i][j])
									val = aggr[k][i][j];
							matR[i][j] = val;
						}
					}
				}
				
				return matR;
				
			/// Criteria Pair ICrA
			} else if (icamth == 5) {
				
				int vecsize = vecW.size();
				int matsize = vecW.get(0).length;
				int secsize = (matsize*matsize-matsize)/2;
				
				/// Result for matrix count
				double[][][] aggr = new double[vecsize][matsize][matsize];
				for (int k = 0; k < vecsize; k++) {
					aggr[k] = ( pair ?
								makeCalc(vecW.get(k), vecZ.get(k), icavar, pair) :
								makeCalc(vecW.get(k), null, icavar, pair) );
				}
				
				/// New matrices for MU-A and NU-B - matcnt x (asize*asize-asize)/2
				double[][] matA = new double[vecsize][secsize];
				double[][] matB = new double[vecsize][secsize];
				
				/// Iterate over aggr[k] matrices, which are asizeXasize
				/// Save upper triangular part as rows of matcntXbsize matrix
				for (int k = 0; k < vecsize; k++) {
					int cc = 0;
					for (int i = 0; i < matsize; i++) {
						for (int j = i+1; j < matsize; j++) {
							matA[k][cc] = aggr[k][i][j]; /// new-MU-A saves aggr[k]-MU-A from (i,j) index
							matB[k][cc] = aggr[k][j][i]; /// new-NU-B saves aggr[k]-NU-B from (j,i) index
							cc++;
						}
					}
				}
				
				/// Result for criteria pair with two matrices as ordered pair
				return makeCalc(matA, matB, icavar, true);
				
			/// Invalid method value
			} else {
				return null;
			}
			
		} catch (Exception ex) {
			return null;
		}
	}
	
}


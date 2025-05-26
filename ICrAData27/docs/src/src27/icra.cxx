/// ICrA - InterCriteria Analysis

#include <omp.h>
#include "icradata.h"
#include "zerofun.h"

/// Make criteria matrix
double** makeCrit(double** matA, int rows, int cols) {
	
	double** matC = (double**) malloc(sizeof(double*) * rows);
	for (int i = 0; i < rows; i++)
		matC[i] = (double*) malloc(sizeof(double) * ((cols*(cols-1))/2));
	zeroDoubleMatrix(matC, rows, (cols*(cols-1))/2);
	
	#pragma omp parallel for
	for (int i = 0; i < rows; i++) {
		int cc = 0;
		for (int k = 0; k < cols-1; k++) {
			for (int j = k; j < cols-1; j++) {
				matC[i][cc++] = (matA[i][k]-matA[i][j+1] > 0 ? 1 : (matA[i][k]-matA[i][j+1] < 0 ? -1 : 0));
				//cc++;
			}
		}
	}
	
	return matC;
}

/// Make criteria matrix for ordered pair
double** makeCritPair(double** matA, double** matB, int rows, int cols) {
	
	double** matC = (double**) malloc(sizeof(double*) * rows);
	for (int i = 0; i < rows; i++)
		matC[i] = (double*) malloc(sizeof(double) * ((cols*(cols-1))/2));
	zeroDoubleMatrix(matC, rows, (cols*(cols-1))/2);
	
	#pragma omp parallel for
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
double counterA(double* arrA, double* arrB, int size, int flag) {
	
	int cc = 0;
	for (int i = 0; i < size; i++) {
		if (((arrA[i] ==  1) && (arrB[i] ==  1)) ||
			((arrA[i] == -1) && (arrB[i] == -1)) ||
			((arrA[i] ==  0) && (arrB[i] ==  0) && flag))
			cc++;
	}
	
	return (double)cc/size;
}

/// Counter B - different elements:  0==0  -1=/=1  1=/=-1  (flag==0)   0=/=0  -1=/=1  1=/=-1  (flag==1)
double counterB(double* arrA, double* arrB, int size, int flag) {
	
	int cc = 0;
	for (int i = 0; i < size; i++) {
		if (((arrA[i] == -1) && (arrB[i] ==  1)) ||
			((arrA[i] ==  1) && (arrB[i] == -1)) ||
			((arrA[i] ==  0) && (arrB[i] ==  0) && flag))
			cc++;
	}
	
	return (double)cc/size;
}

/// Make result - mubiased1 flags 1 0, unbiased2 flags 0 0, nubiased3 flags 0 1
double** makeResult(double** matC, int rows, int cols, int icavar) {
	
	double** matR = (double**) malloc(sizeof(double*) * rows);
	for (int i = 0; i < rows; i++)
		matR[i] = (double*) malloc(sizeof(double) * rows);
	zeroDoubleMatrix(matR, rows, rows);
	
	#pragma omp parallel for
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
double** varBalanced(double** matR1, double** matR3, int rows) {
	
	double** matR = (double**) malloc(sizeof(double*) * rows);
	for (int i = 0; i < rows; i++)
		matR[i] = (double*) malloc(sizeof(double) * rows);
	zeroDoubleMatrix(matR, rows, rows);
	
	for (int i = 0; i < rows; i++)
		for (int j = 0; j < rows; j++)
			if (i != j)
				matR[i][j] = (matR1[i][j] + matR3[i][j])/2;
	
	return matR;
}

/// Weighted5 - each element of unbiasedR2 divided by the sum of MU+NU - R2{i,j}/(R2{i,j} + R2{j,i})
double** varWeighted(double** matR2, int rows) {
	
	double** matR = (double**) malloc(sizeof(double*) * rows);
	for (int i = 0; i < rows; i++)
		matR[i] = (double*) malloc(sizeof(double) * rows);
	zeroDoubleMatrix(matR, rows, rows);
	
	for (int i = 0; i < rows; i++)
		for (int j = 0; j < rows; j++)
			if (i != j)
				matR[i][j] = matR2[i][j]/(matR2[i][j] + matR2[j][i]);
	
	return matR;
}

/// Free matrix data
void freeMatrix(double** matA, int rows) {
	if (matA != NULL && rows > 0) {
		for (int i = 0; i < rows; i++)
			free(matA[i]);
		free(matA);
	}
}

/// Make calculations - variant - mubiased1, Unbiased2, nubiased3, Balanced4, Weighted5
struct vizres makeCalc(double** matW, double** matZ, int rows, int cols, int icavar, int pair) {
	
	/// Matrix dimensions
	/// matW rows cols
	/// matC rows (cols*(cols-1))/2
	/// matR rows rows
	
	/// Criteria matrix - sign of difference between elements per row
	double** matC = (pair ? makeCritPair(matW, matZ, rows, cols) : makeCrit(matW, rows, cols));
	
	//printf("\nmatC\n");
	//showDoubleMatrix(matC, rows, (cols*(cols-1))/2);
	
	/// Result matrix - MU-A uppper triangular, NU-B lower triangular, ignore diagonal elements
	double** matR = NULL;
	
	/// Variant - mubiased1, Unbiased2, nubiased3, Balanced4, Weighted5
	if (icavar == 1 || icavar == 2 || icavar == 3) {
		matR = makeResult(matC, rows, (cols*(cols-1))/2, icavar);
		
	} else if (icavar == 4) {
		double** matR1 = makeResult(matC, rows, (cols*(cols-1))/2, 1);
		double** matR3 = makeResult(matC, rows, (cols*(cols-1))/2, 3);
		matR = varBalanced(matR1, matR3, rows); /// mean value of 1 and 3
		freeMatrix(matR3, rows);
		freeMatrix(matR1, rows);
		
	} else if (icavar == 5) {
		double** matR2 = makeResult(matC, rows, (cols*(cols-1))/2, 2);
		matR = varWeighted(matR2, rows); /// each element of 2 divided by the sum of (i,j)+(j,i)
		freeMatrix(matR2, rows);
	}
	
	freeMatrix(matC, rows);
	
	/// Result
	struct vizres vres;
	vres.matR = matR;
	vres.size = rows;
	
	return vres;
}

/// Make ICrA - method - Standard 1, Aggregated Average2 MaxMin3 MinMax4, Criteria Pair 5
struct vizres makeICrA(double** matW, int rows, int cols, int icavar, int icamth, int matcnt) {
	
	/// Standard ICrA, ignore matrix count
	if (icamth == 1) {
		return makeCalc(matW, NULL, rows, cols, icavar, 0);
		
	/// Aggregated ICrA
	} else if (icamth == 2 || icamth == 3 || icamth == 4) {
		
		struct vizres vres;
		vres.size = 0;
		
		/// Matrix rows must be fully divisible by matrix count
		if (rows % matcnt != 0) {
			vres.size = -7;
			return vres;
		}
		
		/// New size of the matrix
		int asize = rows/matcnt;
		
		/// Result for matrix count
		struct vizres* aggr = (struct vizres*) malloc(sizeof(struct vizres) * matcnt);
		for (int k = 0; k < matcnt; k++)
			aggr[k] = makeCalc(matW+k*asize, NULL, asize, cols, icavar, 0);
		
		/// Result
		double** matR = (double**) malloc(sizeof(double*) * asize);
		for (int i = 0; i < asize; i++)
			matR[i] = (double*) malloc(sizeof(double) * asize);
		zeroDoubleMatrix(matR, asize, asize);
		
		if (icamth == 2) { /// average
			for (int i = 0; i < asize; i++) {
				for (int j = 0; j < asize; j++) {
					if (i != j) {
						double val = 0;
						for (int k = 0; k < matcnt; k++)
							val += aggr[k].matR[i][j];
						matR[i][j] = (double)val/matcnt;
					}
				}
			}
			
		} else if (icamth == 3) { /// maxmin
			for (int i = 0; i < asize; i++) {
				for (int j = i+1; j < asize; j++) { /// max MU-A
					double val = 0;
					for (int k = 0; k < matcnt; k++)
						if (val < aggr[k].matR[i][j])
							val = aggr[k].matR[i][j];
					matR[i][j] = val;
				}
				for (int j = 0; j < i; j++) { /// min NU-B
					double val = 1000000;
					for (int k = 0; k < matcnt; k++)
						if (val > aggr[k].matR[i][j])
							val = aggr[k].matR[i][j];
					matR[i][j] = val;
				}
			}
			
		} else if (icamth == 4) { /// minmax
			for (int i = 0; i < asize; i++) {
				for (int j = i+1; j < asize; j++) { /// min MU-A
					double val = 1000000;
					for (int k = 0; k < matcnt; k++)
						if (val > aggr[k].matR[i][j])
							val = aggr[k].matR[i][j];
					matR[i][j] = val;
				}
				for (int j = 0; j < i; j++) { /// max NU-B
					double val = 0;
					for (int k = 0; k < matcnt; k++)
						if (val < aggr[k].matR[i][j])
							val = aggr[k].matR[i][j];
					matR[i][j] = val;
				}
			}
		}
		
		/// Free resources
		for (int k = 0; k < matcnt; k++)
			freeMatrix(aggr[k].matR, aggr[k].size);
		free(aggr);
		
		/// Result
		vres.matR = matR;
		vres.size = asize;
		return vres;
		
	/// Criteria Pair ICrA
	} else if (icamth == 5) {
		
		struct vizres vres;
		vres.size = 0;
		
		/// Matrix rows must be fully divisible by matrix count
		if (rows % matcnt != 0) {
			vres.size = -8;
			return vres;
		}
		
		/// New size of the matrix
		int asize = rows/matcnt;
		
		/// Result for matrix count
		struct vizres* aggr = (struct vizres*) malloc(sizeof(struct vizres) * matcnt);
		for (int k = 0; k < matcnt; k++)
			aggr[k] = makeCalc(matW+k*asize, NULL, asize, cols, icavar, 0);
		
		/// New matrices for MU-A and NU-B - matcnt x (asize*asize-asize)/2
		int bsize = (asize*asize-asize)/2;
		
		double** matA = (double**) malloc(sizeof(double*) * matcnt);
		for (int i = 0; i < matcnt; i++)
			matA[i] = (double*) malloc(sizeof(double) * bsize);
		zeroDoubleMatrix(matA, matcnt, bsize);
		
		double** matB = (double**) malloc(sizeof(double*) * matcnt);
		for (int i = 0; i < matcnt; i++)
			matB[i] = (double*) malloc(sizeof(double) * bsize);
		zeroDoubleMatrix(matB, matcnt, bsize);
		
		/// Iterate over aggr[k] matrices, which are asizeXasize
		/// Save upper triangular part as rows of matcntXbsize matrix
		for (int k = 0; k < matcnt; k++) {
			int cc = 0;
			for (int i = 0; i < asize; i++) {
				for (int j = i+1; j < asize; j++) {
					matA[k][cc] = aggr[k].matR[i][j]; /// new-MU-A saves aggr[k]-MU-A from (i,j) index
					matB[k][cc] = aggr[k].matR[j][i]; /// new-NU-B saves aggr[k]-NU-B from (j,i) index
					cc++;
				}
			}
		}
		
		/// Result for criteria pair with two matrices as ordered pair
		vres = makeCalc(matA, matB, matcnt, bsize, icavar, 1);
		
		/// Free resources
		freeMatrix(matB, matcnt);
		freeMatrix(matA, matcnt);
		for (int k = 0; k < matcnt; k++)
			freeMatrix(aggr[k].matR, aggr[k].size);
		free(aggr);
		
		return vres;
		
	/// Invalid method value
	} else {
		struct vizres vres;
		vres.size = -9;
		return vres;
	}
	
}


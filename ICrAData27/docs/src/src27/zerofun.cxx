/// Zero/show functions of array/matrix

#include <stdio.h>
#include <stdlib.h>

/// Zero functions
void zeroIntArray(int* arrA, int size) {
	for (int i = 0; i < size; i++)
		arrA[i] = (int)0;
}

void zeroIntMatrix(int** matA, int rows, int cols) {
	for (int i = 0; i < rows; i++)
		for (int j = 0; j < cols; j++)
			matA[i][j] = (int)0;
}

void zeroDoubleArray(double* arrA, int size) {
	for (int i = 0; i < size; i++)
		arrA[i] = (double)0;
}

void zeroDoubleMatrix(double** matA, int rows, int cols) {
	for (int i = 0; i < rows; i++)
		for (int j = 0; j < cols; j++)
			matA[i][j] = (double)0;
}

/// Show functions
void showIntArray(int* arrA, int size) {
	for (int i = 0; i < size; i++)
		printf("%d ", arrA[i]);
}

void showIntMatrix(int** matA, int rows, int cols) {
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++)
			printf("%d ", matA[i][j]);
		printf("\n");
	}
}

void showDoubleArray(double* arrA, int size) {
	for (int i = 0; i < size; i++)
		printf("%.2f ", arrA[i]);
}

void showDoubleMatrix(double** matA, int rows, int cols) {
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++)
			printf("%.2f ", matA[i][j]);
		printf("\n");
	}
}


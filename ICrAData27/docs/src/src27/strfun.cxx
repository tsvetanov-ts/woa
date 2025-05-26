/// String functions

#include "icradata.h"
#include "zerofun.h"
#include <FL/fl_utf8.h>

/// Buffers
char* elembuff = (char*) malloc(sizeof(char) * 256);

/// Copy sour into dest with max len and close string
void strncpy_safe(char* dest, const char* sour, int len) {
	*dest = '\0'; /// ensure empty string in case of failure
	if (sour == NULL || strlen(sour) <= 0) return;
	int c = 0;
	while (c < (int)strlen(sour) && c < len-1) {
		*(dest+c) = *(sour+c);
		c++;
	}
	*(dest+c) = '\0'; /// close string
}

/// Append sour after dest with max len for dest and close string
void strncat_safe(char* dest, const char* sour, int len) {
	int d = strlen(dest);
	if (d < 0 || d+1 >= len || sour == NULL || strlen(sour) <= 0) return;
	int c = 0;
	while (c < (int)strlen(sour) && c+d < len-1) {
		*(dest+c+d) = *(sour+c);
		c++;
	}
	*(dest+c+d) = '\0'; /// close string
}

/**
/// Show string raw data
void stringShowRaw(char* start, char* end) {
	for ( ; start < end; start++)
		printf("%d ", *start);
}

/// Trim spaces and tabs, return pointer to first character
char* stringTrimLine(char* str) {
	char* end = str+strlen(str)-1;
	
	while (*str == ' ' || *str == '\t')
		*str++ = '\0';
	while (*end == ' ' || *end == '\t')
		*end-- = '\0';
	
	return (str < end ? str : end);
}

/// Clear comments
void stringClearComments(char* str, char ch) {
	for ( ; *str; str++)
		if (*str == ch)
			while (*str != '\0')
				*str++ = '\0';
}

/// Array clear comments
void strArrComments(char** arrF, int flines, char ch) {
	for (int i = 0; i < flines; i++)
		if (strlen(arrF[i]) > 0)
			stringClearComments(arrF[i], ch);
}
**/

/// Replace line endings from Win \r\n and Mac \r to UNIX \n
void stringLineEnd(char* str) {
	for ( ; *str; str++)
		if (*str == '\r')
			*str = (*(str+1) == '\n' ? ' ' : '\n');
}

/// Count characters in string
int stringCountChar(char* start, char* end, char ch) {
	int cc = 0;
	for ( ; start < end; start++)
		if (*start == ch)
			cc++;
	
	return cc;
}

/// Also clear ; , from both sides of each line
char* stringTrimLine(char* str) {
	char* end = str+strlen(str)-1;
	
	while (*str == ' ' || *str == '\t' || *str == ';' || *str == ',')
		*str++ = '\0';
	while (*end == ' ' || *end == '\t' || *end == ';' || *end == ',')
		*end-- = '\0';
	
	return (str < end ? str : end);
}

/// Save the address of the character after each \n
/// Replace \n with \0, therefore this function returns array of pointers to char*
char** strArrMake(char* str, int flines) {
	
	char** arrF = (char**) malloc(sizeof(char*) * flines);
	int k = 0;
	arrF[k++] = str;
	
	//printf("arrF %d %d\n", 0, *arrF[0]);	
	while (*str) {
		if (*str == '\n') {
			*str = '\0';
			arrF[k++] = ++str;
		} else
			str++;
	}
	
	return (k == flines ? arrF : NULL);
}

/// Show string array
void strArrShow(char** arrF, int flines) {
	for (int i = 0; i < flines; i++)
		printf("%p %d |%s|\n", arrF[i], i, arrF[i]);
}

/// Array trim line
void strArrTrim(char** arrF, int flines) {
	for (int i = 0; i < flines; i++)
		if (strlen(arrF[i]) > 0)
			arrF[i] = stringTrimLine(arrF[i]);
}

/// Array parameters
int strArrPar(char** arrF, int flines, const char* ss) {
	for (int i = 0; i < flines; i++)
		if (strlen(arrF[i]) > 0 && strstr(arrF[i], ss) == arrF[i])
			return i;
	
	return -1;
}

/// Line element
double dataElem(char* start, int ind, char sep) {
	char* end = start+strlen(start);
	char* pos = start;
	int cc = 0;
	
	for ( ; pos <= end; pos++) {
		if (*pos == sep || *pos == '\n' || *pos == '\0') {
			
			if (cc == ind) {
				int vlen = pos-start;
				if (vlen > 0 && vlen < 255) {
					for (int i = 0; i < vlen; i++) {
						elembuff[i] = (*start == ',' ? '.' : *start);
						start++;
					}
					elembuff[vlen] = '\0';
					
					//printf("%d|%s|\n", vlen, elembuff);
					return atof(elembuff);
				} else
					return 0;
			}
			
			start = pos+1;
			cc++;
		}
	}
	
	return 0x32DCD5;
}

/// String header
char* dataHead(char* start, int ind, char sep) {
	char* end = start+strlen(start);
	char* pos = start;
	int cc = 0;
	
	for ( ; pos <= end; pos++) {
		if (*pos == sep || *pos == '\n' || *pos == '\0') {
			
			if (cc == ind) {
				int vlen = pos-start;
				if (vlen > 0 && vlen < 255) {
					for (int i = 0; i < vlen; i++)
						elembuff[i] = *start++;
					elembuff[vlen] = '\0';
					return elembuff;
				} else {
					elembuff[0] = '-';
					elembuff[1] = '\0';
					return elembuff;
				}
			}
			
			start = pos+1;
			cc++;
		}
	}
	
	elembuff[0] = elembuff[1] = elembuff[2] = 'x';
	elembuff[3] = '\0';
	return elembuff;
}

/// Open file and save contents as char*
char* openFile(const char* fname) {
	
	/// Open file
	FILE* fptr = fl_fopen(fname, "rb");
	if (fptr == NULL)
		return NULL;
	
	/// Seek to end to find file size
	fseek(fptr, 0, SEEK_END);
	int flen = ftell(fptr);
	rewind(fptr);
	
	/// Read file in char array
	char* fbuf = (char*) malloc(sizeof(char) * (flen+1));
	int fch = fread(fbuf, sizeof(char), flen, fptr);
	if (fch != flen)
		fbuf[0] = '\0';
	
	/// Close string
	fbuf[flen] = '\0';
	
	/// Close file
	fclose(fptr);
	
	return fbuf;
}

/// Load file
struct vizfile loadFile(char* fbuf) {
	
	struct vizfile vfile;
	vfile.flines = -1;
	
	/// Convert line endings
	stringLineEnd(fbuf);
	
	/// Count new line characters
	int flines = stringCountChar(fbuf, fbuf+strlen(fbuf)+1, '\n')+1;
	//printf("lines %d\n", flines);
	
	/// At least 4 rows in the file
	if (flines < 4) {
		vfile.flines = -4;
		return vfile;
	}
	
	/// Make string array
	char** arrF = strArrMake(fbuf, flines);
	//strArrShow(arrF, flines);
	
	/// Trim each line
	strArrTrim(arrF, flines);
	
	/// Clear # comments
	//strArrComments(arrF, flines, '#');
	
	//printf("\narrF\n");
	//strArrShow(arrF, flines);
	
	/// Result
	vfile.arrF = arrF;
	vfile.flines = flines;
	
	return vfile;
}

/// Column count
int colCount(int* indC, int size) {
	
	int cols = 0;
	
	/// Find the maximum
	for (int i = 0; i < size; i++)
		if (cols < indC[i])
			cols = indC[i];
	
	/// Make sure that all values are the max
	for (int i = 0; i < size; i++)
		if (indC[i] != cols)
			return -1;
	
	return cols;
}

/// Read file and return struct
struct vizdata readFile(struct vizfile vfile, int sep, int hdr, int tr) {
	
	struct vizdata vdata;
	vdata.rW = vdata.cW = vdata.rsize = vdata.csize = 0;
	
	char** arrF = vfile.arrF;
	int flines = vfile.flines;
	
	/// Separator tab\t semicolon; comma,
	char csep = (sep == 0 ? '\t' : (sep == 1 ? ';' : ','));
	
	/// Number of rows
	int rows = 0;
	for (int i = 0; i < flines; i++)
		if (strlen(arrF[i]) > 0 && strstr(arrF[i], "#") != arrF[i])
			rows++;
	
	/// New string array with data lines only
	char** arrD = (char**) malloc(sizeof(char*) * rows);
	int cc = 0;
	for (int i = 0; i < flines; i++)
		if (strlen(arrF[i]) > 0 && strstr(arrF[i], "#") != arrF[i])
			arrD[cc++] = arrF[i];
	
	//printf("\narrD\n");
	//strArrShow(arrD, rows);
	
	/// Number of columns
	int* indC = (int*) malloc(sizeof(int) * rows);
	zeroIntArray(indC, rows);
	for (int i = 0; i < rows; i++)
		indC[i] = stringCountChar(arrD[i], arrD[i]+strlen(arrD[i]), csep) + 1;
	
	int cols = colCount(indC, rows);
	
	/// All column sizes must match
	if (cols == -1) {
		vdata.rW = vdata.cW = vdata.rsize = vdata.csize = -5;
		return vdata;
	}
	
	//printf("\nrows %d   cols %d\n", rows, cols);
	
	/// Careful where these two lines are
	rows -= hdr;
	cols -= hdr;
	
	/// Matrix size 3x3 at least
	if (rows < 3 || cols < 3) {
		vdata.rW = vdata.cW = vdata.rsize = vdata.csize = -3;
		return vdata;
	}
	
	/// Matrix size - normal rowsXcols, transpose colsXrows
	int rW = (tr ? cols : rows);
	int cW = (tr ? rows : cols);
	
	/// Allocate matrix
	double** matW = (double**) malloc(sizeof(double*) * rW);
	for (int i = 0; i < rW; i++)
		matW[i] = (double*) malloc(sizeof(double) * cW);
	zeroDoubleMatrix(matW, rW, cW);
	
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			if (tr) /// careful, only index of matW changes
				matW[j][i] = dataElem(arrD[i+hdr], j+hdr, csep);
			else
				matW[i][j] = dataElem(arrD[i+hdr], j+hdr, csep);
		}
	}
	
	//printf("\nmatW\n");
	//showDoubleMatrix(matW, rW, cW);
	
	/// Headers
	char** rhead = (char**) malloc(sizeof(char*) * rows);
	for (int i = 0; i < rows; i++)
		rhead[i] = (char*) malloc(sizeof(char) * 256);
	
	char** chead = (char**) malloc(sizeof(char*) * cols);
	for (int j = 0; j < cols; j++)
		chead[j] = (char*) malloc(sizeof(char) * 256);
	
	if (hdr) {
		for (int i = 0; i < rows; i++)
			strncpy_safe(rhead[i], dataHead(arrD[i+hdr], 0, csep), 255);
			//printf("rhead %s\n", dataHead(arrD[i+hdr], 0));
		for (int j = 0; j < cols; j++)
			strncpy_safe(chead[j], dataHead(arrD[0], j+hdr, csep), 255);
			//printf("chead %s\n", dataHead(arrD[0], j+hdr));
		
	} else {
		for (int i = 0; i < rows; i++) {
			snprintf(elembuff, 255, "A%d", (i+1));
			strncpy_safe(rhead[i], elembuff, 255);
		}
		for (int j = 0; j < cols; j++) {
			snprintf(elembuff, 255, "B%d", (j+1));
			strncpy_safe(chead[j], elembuff, 255);
		}
	}
	
	/// Debug
	//for (int i = 0; i < rows; i++)
	//	printf("rhead %s\n", rhead[i]);
	//for (int j = 0; j < cols; j++)
	//	printf("chead %s\n", chead[j]);
	
	/// Free resources
	free(indC);
	free(arrD);
	
	/// Result
	vdata.matW = matW;
	vdata.rW = rW;
	vdata.cW = cW;
	if (tr) {
		vdata.rhead = chead;
		vdata.rsize = cols;
		vdata.chead = rhead;
		vdata.csize = rows;
	} else {
		vdata.rhead = rhead;
		vdata.rsize = rows;
		vdata.chead = chead;
		vdata.csize = cols;
	}
	return vdata;
}

/// Save file
int saveFile(const char* fn, struct vizfile vfile, int icamth, int matcnt, int hdr, int tr, int sep) {
	
	FILE* fptr = fl_fopen(fn, "wb");
	if (fptr == NULL)
		return -1;
	
	char** arrF = vfile.arrF;
	int flines = vfile.flines;
	
	/// Parameters
	/// #icradata 0 1 0 0 0 0
	/// method=0 matcnt=1 rowh=0 colh=0 transpose=0 ordpair=0
	/// ICrAData2 uses only 1,2,3,5 elements
	/// Saves one more element for separator
	snprintf(elembuff, 255, "#icradata %d %d %d %d %d %d %d\n", icamth, matcnt, hdr, hdr, tr, 0, sep);
	fwrite(elembuff, strlen(elembuff), 1, fptr);
	
	/// Skip this index
	int p = strArrPar(arrF, flines, "#icradata");
	
	/// Save file
	for (int i = 0; i < flines; i++) {
		if (strlen(arrF[i]) > 0) {
			if (i != p) {
				fwrite(arrF[i], strlen(arrF[i]), 1, fptr);
				fwrite("\n", 1, 1, fptr);
			}
		}
	}
	
	int flen = ftell(fptr);
	fclose(fptr);
	
	return flen;
}


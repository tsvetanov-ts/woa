/// ICrAData header

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

/// Structs
struct vizfile {
	char** arrF;
	int  flines;
};

struct vizdata {
	double** matW;
	int        rW;
	int        cW;
	char**  rhead;
	int     rsize;
	char**  chead;
	int     csize;
};

struct vizres {
	double** matR;
	int      size;
};


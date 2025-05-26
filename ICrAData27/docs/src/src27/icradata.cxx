/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 2.7
 * Date: 22 March 2025
 * Compiled by: GCC/MSVC
 * 
 */

#include <FL/Fl.H>
#include <FL/Fl_Window.H>
#include <FL/Fl_Button.H>
#include <FL/Fl_Box.H>
#include <FL/Fl_Text_Buffer.H>
#include <FL/Fl_Text_Display.H>
#include <FL/Fl_Text_Editor.H>

#include <FL/Fl_Tile.H>
#include <FL/Fl_Check_Button.H>
#include <FL/Fl_Menu_Item.H>
#include <FL/Fl_Choice.H>
#include <FL/Fl_Spinner.H>
#include <FL/Fl_Clock.H>
#include <FL/Fl_Light_Button.H>

#include <FL/Fl_Native_File_Chooser.H>
#include <FL/Fl_Image_Surface.H>
#include <FL/fl_draw.H>
#include <png.h>
#include <omp.h>

#if defined(__GNUC__)
#include <pthread.h>
#elif defined(_WIN32)
#include <process.h>
#endif

#include "icradata.h"
#include "strfun.h"
#include "icra.h"
#include "mytable.h"
#include "mydraw.h"

/// Global variables
Fl_Window* mainWin;
MyDraw* drawWin;
Fl_Window* clockWin;

Fl_Choice* chMth;
Fl_Choice* chVar;
Fl_Choice* chSep;
Fl_Spinner* chMatCnt;
Fl_Check_Button* chTr;
Fl_Check_Button* chHdr;
Fl_Choice* chTable;
Fl_Choice* chTable2;
Fl_Spinner* chPlotMatrix;

Fl_Text_Buffer* buffInput;
Fl_Text_Display* textInput;
Fl_Text_Buffer* buffMsg;
Fl_Text_Display* textMsg;

Fl_Menu_Item listMth[] = {
	{"Standard"},
	{"Aggr Average"},
	{"Aggr MaxMin"},
	{"Aggr MinMax"},
	{"Criteria Pair"},
	{0}
};
Fl_Menu_Item listVar[] = {
	{"\xce\xbc-biased"},
	{"Unbiased"},
	{"\xce\xbd-biased"},
	{"Balanced"},
	{"Weighted"},
	{0}
};
Fl_Menu_Item listSep[] = {
	{"Tab \\t"},
	{"Semicolon ;"},
	{"Comma ,"},
	{0}
};
Fl_Menu_Item listTable[] = {
	{"---"},
	{"input"},
	{"\xce\xbc/\xce\xbd output"},
	{"(\xce\xbc;\xce\xbd) table"},
	{"\xce\xbc table"},
	{"\xce\xbd table"},
	{0}
};
Fl_Menu_Item listExpTable[] = {
	{"input"},
	{"\xce\xbc/\xce\xbd output"},
	{"(\xce\xbc;\xce\xbd) table"},
	{"\xce\xbc table"},
	{"\xce\xbd table"},
	{"vector upper"},
	{"vector lower"},
	{0}
};
Fl_Menu_Item listExpColSep[] = {
	{"Tab \\t"},
	{"Semicolon ;"},
	{"Comma ,"},
	{"TeX &"},
	{0}
};
Fl_Menu_Item listExpDecSep[] = {
	{"Point ."},
	{"Comma ,"},
	{0}
};
int expVal[] = {0,0,0,4};
double plotVal[] = {0.75,0.25, 5,3000,20, 1,0,0,0};

/// Buffers
time_t* ttbuff = (time_t*) malloc(sizeof(time_t));
struct tm* tmbuff = (struct tm*) malloc(sizeof(struct tm));
char* timebuff = (char*) malloc(sizeof(char) * 32);
char* msgbuff  = (char*) malloc(sizeof(char) * 256);
char* strbuff  = (char*) malloc(sizeof(char) * 256);
char* filebuff = (char*) malloc(sizeof(char) * 256);

struct vizfile vfile;
struct vizdata vdata;
struct vizres vres;
MyTable* vtable;
MyTable* vtable2;

/// Free vfile
void freevfile() {
	if (vfile.arrF != NULL && vfile.flines > 0) {
		free(vfile.arrF);
		vfile.flines = -1;
	}
}

/// Free vdata
void freevdata() {
	if (vdata.matW != NULL && vdata.rW > 0) {
		for (int i = 0; i < vdata.rW; i++)
			free(vdata.matW[i]);
		free(vdata.matW);
		vdata.rW = vdata.cW = -1;
	}
	if (vdata.rhead != NULL && vdata.rsize > 0) {
		for (int i = 0; i < vdata.rsize; i++)
			free(vdata.rhead[i]);
		free(vdata.rhead);
		vdata.rsize = -1;
	}
	if (vdata.chead != NULL && vdata.csize > 0) {
		for (int i = 0; i < vdata.csize; i++)
			free(vdata.chead[i]);
		free(vdata.chead);
		vdata.csize = -1;
	}
}

/// Free vres
void freevres() {
	if (vres.matR != NULL && vres.size > 0) {
		for (int i = 0; i < vres.size; i++)
			free(vres.matR[i]);
		free(vres.matR);
		vres.size = -1;
	}
}

/// Message
void msg(const char* m) {
	time(ttbuff);
	#if defined(__unix__)
	localtime_r(ttbuff, tmbuff);
	#else
	localtime_s(tmbuff, ttbuff);
	#endif
	strftime(timebuff, 32, "%H:%M:%S", tmbuff);
	snprintf(msgbuff, 255, "%s %s\n", timebuff, m);
	buffMsg->append(msgbuff);
	textMsg->scroll(textMsg->count_lines(0, textMsg->buffer()->length(), 0), 0);
}

/// Listen for clean
void listenClean(Fl_Widget* wdj, void* ptr) {
	/// Clear tables first
	vtable->clear();
	vtable->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
	vtable2->clear();
	vtable2->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
	/// Clear plot
	drawWin->myPlotData(NULL,-1);
	drawWin->redraw();
	/// Clear global data
	freevfile();
	freevdata();
	freevres();
	/// Clear
	chTable->value(0);
	chTable2->value(0);
	buffInput->remove(0, buffInput->length());
	buffMsg->remove(0, buffMsg->length());
	msg("ICrAData v2.7");
}

/// Listen for method change
void listenMethod(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Choice*) wdj)->value() );
	if (val == 0) {
		chHdr->activate();
		chTr->activate();
		chMatCnt->value(1);
		chMatCnt->deactivate();
	} else {
		chHdr->value(0);
		chTr->value(0);
		chHdr->deactivate();
		chTr->deactivate();
		chMatCnt->activate();
	}
}

/// Listen for open file
void listenOpen(Fl_Widget* wdj, void* ptr) {
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_FILE);
	fc.title("Open file");
	fc.directory(".");
	fc.filter("Text file\t*.txt\nCSV file\t*.csv");
	if (fc.show() != 0)
		return;
	
	/// Open file
	char* fbuf = openFile(fc.filename());
	if (fbuf == NULL) {
		msg("Could not open file");
		return;
	} else if (strlen(fbuf) <= 0) {
		msg("File has zero length");
		free(fbuf);
		return;
	} else {
		snprintf(strbuff, 255, "File opened (%d bytes) %s", (int)strlen(fbuf), fc.filename());
		msg(strbuff);
	}
	
	/// Replace line endings
	stringLineEnd(fbuf);
	
	/// Parameters
	if (strstr(fbuf, "#icradata") == fbuf) {
		
		/// #icradata 0 1 0 0 0 0
		/// method=0 matcnt=1 rowh=0 colh=0 transpose=0 ordpair=0
		/// ICrAData2 uses only 1,2,3,5 elements
		/// Saves one more element for separator
		
		/// Method
		int v = dataElem(fbuf, 1, ' ');
		if (v >= 0 && v <= 4) {
			chMth->value(v);
			listenMethod(chMth, NULL);
		}
		
		/// MatCnt
		v = dataElem(fbuf, 2, ' ');
		if (v >= 1 && v <= 9999)
			chMatCnt->value(v);
		
		/// Headers
		v = dataElem(fbuf, 3, ' ');
		if (v == 0 || v == 1)
			chHdr->value(v);
		
		/// Transpose
		v = dataElem(fbuf, 5, ' ');
		if (v == 0 || v == 1)
			chTr->value(v);
		
		/// Separator
		v = dataElem(fbuf, 7, ' ');
		if (v == 0 || v == 1 || v == 2)
			chSep->value(v);
		
	}
	
	/// Display file
	buffInput->text(fbuf);
	
	/// Clean
	free(fbuf);
}

/// Listen for save file
void listenSave(Fl_Widget* wdj, void* ptr) {
	
	if (strlen(buffInput->text()) <= 0) {
		msg("Nothing to save");
		return;
	}
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_SAVE_FILE);
	fc.title("Save file");
	fc.directory(".");
	fc.filter("Text file\t*.txt");
	if (fc.show() != 0)
		return;
	
	strncpy_safe(filebuff, fc.filename(), 255);
	if (strstr(filebuff, ".txt") == NULL)
		strncat_safe(filebuff, ".txt", 255);
	
	struct vizfile vs = loadFile(buffInput->text());
	int s = saveFile(filebuff, vs, chMth->value(), chMatCnt->value(), chHdr->value(), chTr->value(), chSep->value());
	if (vs.flines != -1)
		free(vs.arrF);
	
	if (s == -1) {
		msg("Could not save file");
		return;
	}
	
	snprintf(strbuff, 255, "File saved (%d bytes) %s", s, filebuff);
	msg(strbuff);
}

/// Thread update
void thUpdate() {
	
	/// Make result table
	vtable->clear();
	vtable->rows(vres.size);
	vtable->cols(vres.size);
	vtable->myCellData(vres.matR,vres.size,vres.size, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, 2);
	chTable->value(2);
	
	vtable2->clear();
	vtable2->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
	chTable2->value(0);
	
	/// Make result plot
	drawWin->myPlotData(vres.matR, vres.size);
	drawWin->redraw();
	
	snprintf(strbuff, 255, "%s - %s", listMth[chMth->value()].label(), listVar[chVar->value()].label());
	msg(strbuff);
	
	clockWin->hide();
}

/// Thread function
void* thFunction(void* arg) {
	vres = makeICrA(vdata.matW, vdata.rW, vdata.cW, chVar->value()+1, chMth->value()+1, chMatCnt->value());
	thUpdate();
	return NULL;
}

/// Listen for calculations
void listenCalc(Fl_Widget* wdj, void* ptr) {
	
	if (strlen(buffInput->text()) <= 0) {
		msg("Not enough data");
		return;
	}
	
	/// Free resources
	freevfile();
	
	/// Load file
	vfile = loadFile(buffInput->text());
	
	if (vfile.flines == -4) {
		msg("Data should have at least 4 lines");
		return;
	}
	
	snprintf(strbuff, 255, "Data loaded (%d lines)", vfile.flines);
	msg(strbuff);
	
	if (vfile.flines < 4) {
		msg("Not enough data lines");
		return;
	}
	
	/// Clear result
	vtable->clear();
	vtable->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
	vtable2->clear();
	vtable2->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
	drawWin->myPlotData(NULL,-1);
	drawWin->redraw();
	
	/// Free resources
	freevdata();
	
	/// Read file
	vdata = readFile(vfile, chSep->value(), chHdr->value(), chTr->value());
	
	//printf("\nmatW\n");
	//showDoubleMatrix(vdata.matW, vdata.rW, vdata.cW);
	
	if (vdata.rW == -5) {
		msg("All column sizes per row must match (including the header)");
		return;
	}
	
	if (vdata.rW == -3) {
		msg("Minimum matrix size is 3x3");
		return;
	}
	
	if (chMth->value() > 0) {
		if (chMatCnt->value() < 3) {
			msg("Non-standard methods require at least 3 matrices");
			return;
		}
		
		if ((double)vdata.rW/(int)chMatCnt->value() < 3.0) {
			snprintf(strbuff, 255, "Each matrix must have at least 3 rows: %d/%d=%.6f",
				vdata.rW, (int)chMatCnt->value(), (double)vdata.rW/chMatCnt->value());
			msg(strbuff);
			return;
		}
		
		if (vdata.rW % (int)chMatCnt->value() != 0) {
			snprintf(strbuff, 255, "Matrix rows must be fully divisible by matrix count: %d/%d=%.6f",
				vdata.rW, (int)chMatCnt->value(), (double)vdata.rW/chMatCnt->value());
			msg(strbuff);
			return;
		}
		
		snprintf(strbuff, 255, "Matrix rows: %d/%d=%.6f",
			vdata.rW, (int)chMatCnt->value(), (double)vdata.rW/chMatCnt->value());
		msg(strbuff);
	}
	
	/// Free resources
	freevres();
	
	/// Big data
	if (vdata.rW >= chPlotMatrix->value() || vdata.cW >= chPlotMatrix->value()) {
		clockWin->resize(
			mainWin->x()+mainWin->w()/2-clockWin->w()/2,
			mainWin->y()+mainWin->h()/2-clockWin->h()/2,
			clockWin->w(), clockWin->h());
		clockWin->show();
		
		#if defined(__GNUC__)
		pthread_t pt;
		pthread_create(&pt, NULL, &thFunction, NULL);
		#elif defined(_WIN32)
		_beginthread((void( __cdecl* )( void* ))thFunction, 0, NULL);
		#endif
		
		return;
	}
	
	/// Make ICrA
	vres = makeICrA(vdata.matW, vdata.rW, vdata.cW, chVar->value()+1, chMth->value()+1, chMatCnt->value());
	
	//printf("\nmatR\n");
	//showDoubleMatrix(vres.matR, vres.size, vres.size);
	
	/// Make result table
	vtable->clear();
	vtable->rows(vres.size);
	vtable->cols(vres.size);
	vtable->myCellData(vres.matR,vres.size,vres.size, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, 4);
	chTable->value(4);
	
	vtable2->clear();
	vtable2->rows(vres.size);
	vtable2->cols(vres.size);
	vtable2->myCellData(vres.matR,vres.size,vres.size, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, 5);
	chTable2->value(5);
	
	/// Make result plot
	drawWin->myPlotData(vres.matR, vres.size);
	drawWin->redraw();
	
	snprintf(strbuff, 255, "%s - %s", listMth[chMth->value()].label(), listVar[chVar->value()].label());
	msg(strbuff);
}

/// Listen for check alpha
void listenChAlpha(Fl_Widget* wdj, void* ptr) {
	double val = double( ((Fl_Spinner*)wdj)->value() );
	plotVal[0] = val;
	vtable->myLimitA(val);
	vtable->redraw();
	vtable2->myLimitA(val);
	vtable2->redraw();
	drawWin->myLimitA(val);
	drawWin->redraw();
}

/// Listen for check beta
void listenChBeta(Fl_Widget* wdj, void* ptr) {
	double val = double( ((Fl_Spinner*)wdj)->value() );
	plotVal[1] = val;
	vtable->myLimitB(val);
	vtable->redraw();
	vtable2->myLimitB(val);
	vtable2->redraw();
	drawWin->myLimitB(val);
	drawWin->redraw();
}

/// Listen for digits
void listenChDigits(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*) wdj)->value() );
	vtable->myDigits(val);
	vtable->redraw();
	vtable2->myDigits(val);
	vtable2->redraw();
}

/// Listen for column width
void listenChWidth(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*) wdj)->value() );
	vtable->row_header_width(val);
	vtable->col_width_all(val);
	vtable->redraw();
	vtable2->row_header_width(val);
	vtable2->col_width_all(val);
	vtable2->redraw();
}

/// Listen for table display
void listenChTable(Fl_Widget* wdj, void* ptr) {
	if (vdata.rW == -1 || vres.size == -1) return;
	int val = int( ((Fl_Choice*) wdj)->value() );
	
	if (val == 0) { /// nothing
		vtable->clear();
		vtable->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
		
	} else if (val == 1) { /// input
		vtable->clear();
		vtable->rows(vdata.rW);
		vtable->cols(vdata.cW);
		vtable->myCellData(vdata.matW,vdata.rW,vdata.cW, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, val);
		
	} else if (val >= 2 && val <= 5) { /// result
		vtable->clear();
		vtable->rows(vres.size);
		vtable->cols(vres.size);
		vtable->myCellData(vres.matR,vres.size,vres.size, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, val);
	}
}

/// Listen for table 2 display
void listenChTable2(Fl_Widget* wdj, void* ptr) {
	if (vdata.rW == -1 || vres.size == -1) return;
	int val = int( ((Fl_Choice*) wdj)->value() );
	
	if (val == 0) { /// nothing
		vtable2->clear();
		vtable2->myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0);
		
	} else if (val == 1) { /// input
		vtable2->clear();
		vtable2->rows(vdata.rW);
		vtable2->cols(vdata.cW);
		vtable2->myCellData(vdata.matW,vdata.rW,vdata.cW, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, val);
		
	} else if (val >= 2 && val <= 5) { /// result
		vtable2->clear();
		vtable2->rows(vres.size);
		vtable2->cols(vres.size);
		vtable2->myCellData(vres.matR,vres.size,vres.size, vdata.rhead,vdata.rsize, vdata.chead,vdata.csize, val);
	}
}

/// Listen for threads
void listenChThreads(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*)wdj)->value() );
	omp_set_num_threads(val);
}

/// Listen for colors
void listenChColor(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Light_Button*) wdj)->value() );
	if (val) {
		Fl::background(244, 240, 236);
		Fl::background2(255, 255, 255);
		Fl::foreground(0, 0, 0);
	} else {
		Fl::background(70, 70, 70);
		Fl::background2(50, 50, 50);
		Fl::foreground(255, 255, 255);
	}
	
	mainWin->redraw();
}

/// Close window
void listenClose(Fl_Widget* wdj, void* ptr) {
	((Fl_Window*)ptr)->hide();
}

/// Listen for export values
void listenExpTable(Fl_Widget* wdj, void* ptr) {
	expVal[0] = int( ((Fl_Choice*) wdj)->value() );
}
void listenExpColSep(Fl_Widget* wdj, void* ptr) {
	expVal[1] = int( ((Fl_Choice*) wdj)->value() );
}
void listenExpDecSep(Fl_Widget* wdj, void* ptr) {
	expVal[2] = int( ((Fl_Choice*) wdj)->value() );
}
void listenExpDigits(Fl_Widget* wdj, void* ptr) {
	expVal[3] = int( ((Fl_Spinner*) wdj)->value() );
}

/// Export data
void listenExp(Fl_Widget* wdj, void* ptr) {
	
	if (vdata.rW == -1 || vres.size == -1)
		return;
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_SAVE_FILE);
	fc.title("Save table");
	fc.directory(".");
	fc.filter("Text file\t*.txt");
	if (fc.show() != 0)
		return;
	
	strncpy_safe(filebuff, fc.filename(), 255);
	if (strstr(filebuff, ".txt") == NULL)
		strncat_safe(filebuff, ".txt", 255);
	
	FILE* fptr = fl_fopen(filebuff, "wb");
	if (fptr == NULL) {
		msg("Failed to export data to file");
		return;
	}
	
	int disp = expVal[0]; /// table to export
	const char* csep = "\t"; /// column separator
	if (expVal[1] == 1) csep = ";";
	else if (expVal[1] == 2) csep = ",";
	else if (expVal[1] == 3) csep = "&";
	
	const char* eol = "\n";
	if (expVal[1] == 3) eol = " \\\\ \n";
	
	int dsep = expVal[2]; /// decimal separator
	int dgts = expVal[3]; /// digits
	
	char sdig1[64];
	char sdig2[64];
	char sdig3[64];
	snprintf(sdig1, 63, "%%.%df", dgts);
	snprintf(sdig2, 63, "(%%.%df;%%.%df)", dgts, dgts);
	snprintf(sdig3, 63, "%%s-%%s%%s%%.%df%%s%%.%df%%s%%d%%s%%d", dgts, dgts);
	
	if (disp == 0) { /// input
		fwrite("input", 5, 1, fptr);
		for (int j = 0; j < vdata.csize; j++) {
			fwrite(csep, 1, 1, fptr);
			fwrite(vdata.chead[j], strlen(vdata.chead[j]), 1, fptr);
		}
		fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vdata.rW; i++) {
			fwrite(vdata.rhead[i], strlen(vdata.rhead[i]), 1, fptr);
			
			for (int j = 0; j < vdata.cW; j++) {
				snprintf(strbuff, 255, sdig1, vdata.matW[i][j]);
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(csep, 1, 1, fptr);
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
			fwrite(eol, strlen(eol), 1, fptr);
		}
		
	} else if (disp == 1) { /// mu/nu output
		fwrite("\xce\xbc/\xce\xbd-output", 12, 1, fptr);
		for (int j = 0; j < vdata.rsize && j < vres.size; j++) {
			fwrite(csep, 1, 1, fptr);
			fwrite(vdata.rhead[j], strlen(vdata.rhead[j]), 1, fptr);
		}
		fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			if (i < vdata.rsize)
				fwrite(vdata.rhead[i], strlen(vdata.rhead[i]), 1, fptr);
			else
				fwrite("---", 3, 1, fptr);
			
			for (int j = 0; j < vres.size; j++) {
				snprintf(strbuff, 255, sdig1, vres.matR[i][j]);
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(csep, 1, 1, fptr);
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
			fwrite(eol, strlen(eol), 1, fptr);
		}
		
	} else if (disp == 2) { /// (mu;nu) table
		fwrite("(\xce\xbc;\xce\xbd)-table", 13, 1, fptr);
		for (int j = 0; j < vdata.rsize && j < vres.size; j++) {
			fwrite(csep, 1, 1, fptr);
			fwrite(vdata.rhead[j], strlen(vdata.rhead[j]), 1, fptr);
		}
		fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			if (i < vdata.rsize)
				fwrite(vdata.rhead[i], strlen(vdata.rhead[i]), 1, fptr);
			else
				fwrite("---", 3, 1, fptr);
			
			for (int j = 0; j < vres.size; j++) {
				if (i < j)
					snprintf(strbuff, 255, sdig2, vres.matR[i][j], vres.matR[j][i]);
				else if (i > j)
					snprintf(strbuff, 255, sdig2, vres.matR[j][i], vres.matR[i][j]);
				else
					snprintf(strbuff, 255, sdig2, 1.0, 0.0);
				
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(csep, 1, 1, fptr);
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
			fwrite(eol, strlen(eol), 1, fptr);
		}
		
	} else if (disp == 3) { /// mu table
		fwrite("\xce\xbc-table", 8, 1, fptr);
		for (int j = 0; j < vdata.rsize && j < vres.size; j++) {
			fwrite(csep, 1, 1, fptr);
			fwrite(vdata.rhead[j], strlen(vdata.rhead[j]), 1, fptr);
		}
		fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			if (i < vdata.rsize)
				fwrite(vdata.rhead[i], strlen(vdata.rhead[i]), 1, fptr);
			else
				fwrite("---", 3, 1, fptr);
			
			for (int j = 0; j < vres.size; j++) {
				if (i < j)
					snprintf(strbuff, 255, sdig1, vres.matR[i][j]);
				else if (i > j)
					snprintf(strbuff, 255, sdig1, vres.matR[j][i]);
				else
					snprintf(strbuff, 255, sdig1, 1.0);
				
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(csep, 1, 1, fptr);
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
			fwrite(eol, strlen(eol), 1, fptr);
		}
		
	} else if (disp == 4) { /// nu table
		fwrite("\xce\xbd-table", 8, 1, fptr);
		for (int j = 0; j < vdata.rsize && j < vres.size; j++) {
			fwrite(csep, 1, 1, fptr);
			fwrite(vdata.rhead[j], strlen(vdata.rhead[j]), 1, fptr);
		}
		fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			if (i < vdata.rsize)
				fwrite(vdata.rhead[i], strlen(vdata.rhead[i]), 1, fptr);
			else
				fwrite("---", 3, 1, fptr);
			
			for (int j = 0; j < vres.size; j++) {
				if (i < j)
					snprintf(strbuff, 255, sdig1, vres.matR[j][i]);
				else if (i > j)
					snprintf(strbuff, 255, sdig1, vres.matR[i][j]);
				else
					snprintf(strbuff, 255, sdig1, 0.0);
				
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(csep, 1, 1, fptr);
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
			fwrite(eol, strlen(eol), 1, fptr);
		}
		
	} else if (disp == 5) { /// vector upper
		fwrite("vector-upper", 12, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("\xce\xbc", 2, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("\xce\xbd", 2, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("row", 3, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("col", 3, 1, fptr); fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			for (int j = i+1; j < vres.size; j++) {
				snprintf(strbuff, 255, sdig3,
					(i<vdata.rsize ? vdata.rhead[i] : "---"), (i<vdata.rsize ? vdata.rhead[j] : "---"), csep,
					vres.matR[i][j], csep, vres.matR[j][i], csep, (i+1), csep, (j+1));
				
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(strbuff, strlen(strbuff), 1, fptr);
				fwrite(eol, strlen(eol), 1, fptr);
			}
		}
		
	} else if (disp == 6) { /// vector lower
		fwrite("vector-lower", 12, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("\xce\xbc", 2, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("\xce\xbd", 2, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("row", 3, 1, fptr); fwrite(csep, 1, 1, fptr);
		fwrite("col", 3, 1, fptr); fwrite(eol, strlen(eol), 1, fptr);
		
		for (int i = 0; i < vres.size; i++) {
			for (int j = 0; j < i; j++) {
				snprintf(strbuff, 255, sdig3,
					(i<vdata.rsize ? vdata.rhead[i] : "---"), (i<vdata.rsize ? vdata.rhead[j] : "---"), csep,
					vres.matR[j][i], csep, vres.matR[i][j], csep, (i+1), csep, (j+1));
				
				if (dsep) {
					for (int k = 0; k < (int)strlen(strbuff); k++)
						if (strbuff[k] == '.') strbuff[k] = ',';
				}
				
				fwrite(strbuff, strlen(strbuff), 1, fptr);
				fwrite(eol, strlen(eol), 1, fptr);
			}
		}
	}
	
	int flen = ftell(fptr);
	fclose(fptr);
	
	snprintf(strbuff, 255, "Exported data (%d bytes) %s", flen, filebuff);
	msg(strbuff);
}

/// Export window
void listenExpWin(Fl_Widget* wdj, void* ptr) {
	
	if (vdata.rW == -1 || vres.size == -1)
		return;
	
	Fl_Window* expWin = new Fl_Window(
		mainWin->x()+mainWin->w()/2-100, mainWin->y()+mainWin->h()/2-110,
		200, 220, "Export");
	
	Fl_Choice* expTable = new Fl_Choice(10+60, 10, 120, 25, "Table");
	expTable->tooltip("Display tables.");
	expTable->menu(listExpTable);
	expTable->value(expVal[0]);
	expTable->callback(listenExpTable, expTable);
	
	Fl_Choice* expColSep = new Fl_Choice(10+60, 10+25+10, 120, 25, "ColSep");
	expColSep->tooltip("Column separator.");
	expColSep->menu(listExpColSep);
	expColSep->value(expVal[1]);
	expColSep->callback(listenExpColSep, expColSep);
	
	Fl_Choice* expDecSep = new Fl_Choice(10+60, (10+25)*2+10, 120, 25, "DecSep");
	expDecSep->tooltip("Decimal separator.");
	expDecSep->menu(listExpDecSep);
	expDecSep->value(expVal[2]);
	expDecSep->callback(listenExpDecSep, expDecSep);
	
	Fl_Spinner* expDigits = new Fl_Spinner(10+60, (10+25)*3+10, 60, 25, "Digits");
	expDigits->tooltip("Digits after the decimal separator.");
	expDigits->minimum(1);
	expDigits->maximum(16);
	expDigits->step(1);
	expDigits->value(expVal[3]);
	expDigits->callback(listenExpDigits, expDigits);
	
	Fl_Button* btnExp = new Fl_Button(10+40, (10+25)*4+10, 100, 25, "Export");
	btnExp->callback(listenExp, expWin);
	
	Fl_Button* btnClose = new Fl_Button(10+40, (10+25)*5+10, 100, 25, "Close");
	btnClose->callback(listenClose, expWin);
	
	expWin->resizable(0);
	expWin->set_modal();
	expWin->end();
	expWin->show();
}

/// Listen for plot options
void listenPlotPoints(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*) wdj)->value() );
	plotVal[2] = val;
	drawWin->myPlotPoints(val);
	drawWin->redraw();
}
void listenPlotMatrix(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*) wdj)->value() );
	plotVal[3] = val;
	drawWin->myPlotMatrix(val);
	drawWin->redraw();
}
void listenPlotJump(Fl_Widget* wdj, void* ptr) {
	int val = int( ((Fl_Spinner*) wdj)->value() );
	plotVal[4] = val;
	drawWin->myPlotJump(val);
	drawWin->redraw();
}

/// Color as TeX string
const char* plotColor(double mu, double nu, double a, double b) {
	if (mu > a && nu < b)
		return "\\color{posc}"; /// positive consonance
	else if (mu < b && nu > a)
		return "\\color{negc}"; /// negative consonance
	else
		return "\\color{diss}"; /// dissonance
}

/// Listen for plot TeX
void listenPlotTeX(Fl_Widget* wdj, void* ptr) {
	
	if (vdata.rW == -1 || vres.size == -1)
		return;
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_SAVE_FILE);
	fc.title("Save TeX file");
	fc.directory(".");
	fc.filter("TeX\t*.tex");
	if (fc.show() != 0)
		return;
	
	strncpy_safe(filebuff, fc.filename(), 255);
	if (strstr(filebuff, ".tex") == NULL)
		strncat_safe(filebuff, ".tex", 255);
	
	FILE* fptr = fl_fopen(filebuff, "wb");
	if (fptr == NULL) {
		msg("Failed to save TeX file");
		return;
	}
	
	double chLimA = plotVal[0];
	double chLimB = plotVal[1];
	double chPoints = plotVal[2];
	int chMatrix = plotVal[3];
	int chJump = plotVal[4];
	int chColor = plotVal[5];
	int chMarks = plotVal[6];
	int chGrid = plotVal[7];
	int chText = plotVal[8];
	
	/// Preamble
	const char* txt1 = "%%% ICrAData TeX Export\n"
		"\\documentclass[11pt]{article}\n"
		"\\usepackage{graphicx}\n"
		"\\usepackage{xcolor}\n"
		"\\begin{document}\n"
		"\\thispagestyle{empty}\n\n"
		"%%% Change unitlength and font size to scale the graphic\n"
		"%%% Font sizes: \\tiny \\scriptsize \\footnotesize \\small \\normalsize \\large \\Large \\LARGE \\huge \\Huge\n"
		"\\begin{center}\n";
	fwrite(txt1, strlen(txt1), 1, fptr);
	
	/// Colors - chColor
	const char* txt2 =  "\\definecolor{posc}{rgb}{0.2,0.8,0.2}\n"
						"\\definecolor{negc}{rgb}{0.8,0.2,0.2}\n"
						"\\definecolor{diss}{rgb}{0.8,0.2,0.8}\n";
	if (chColor)
		fwrite(txt2, strlen(txt2), 1, fptr);
	
	/// Circle radius
	snprintf(strbuff, 255, "\\newcommand{\\myrad}{%.6f}\n", (0.05*chPoints));
	fwrite(strbuff, strlen(strbuff), 1, fptr);
	
	/// Commands for text - chText
	const char* txt3 =  "\\newcommand{\\myticks}{\\scriptsize}\n"
						"\\newcommand{\\mytext}{\\normalsize}\n";
	if (chText)
		fwrite(txt3, strlen(txt3), 1, fptr);
	
	/// Picture
	snprintf(strbuff, 255,
		"\\setlength{\\unitlength}{20pt} %%10pt=4mm\n"
		"\\linethickness{0.5pt}\n"
		"\\begin{picture}%s\n"
		"\\put(0,0){\\line(0,1){10}}\n"
		"\\put(0,0){\\line(1,0){10}}\n"
		"\\put(10,0){\\line(-1,1){10}}\n", (chText ? "(11.5,11.5)(-1.5,-1.5)" : "(10,10)"));
	fwrite(strbuff, strlen(strbuff), 1, fptr);
	
	/// Marks
	if (chMarks && !chGrid) {
		for (int i = 1; i < 10; i++) {
			snprintf(strbuff, 255,  "\\put(%d,-0.15){\\line(0,1){0.3}}\n"
									"\\put(-0.15,%d){\\line(1,0){0.3}}\n", i, i);
			fwrite(strbuff, strlen(strbuff), 1, fptr);
		}
	}
	
	/// Grid
	if (chGrid) {
		for (int i = 1; i < 10; i++) {
			snprintf(strbuff, 255,  "\\put(%d,-0.15){\\line(0,1){%d.15}}\n"
									"\\put(-0.15,%d){\\line(1,0){%d.15}}\n", i, (10-i), i, (10-i));
			fwrite(strbuff, strlen(strbuff), 1, fptr);
		}
	}
	
	/// Text
	if (chText) {
		const char* txt4 = "\\put(5,-1.2){\\makebox(0,0)[cc]{\\mytext Degree of agreement, $\\mu$}}\n"
			"\\put(-1.3,5){\\makebox(0,0)[cc]{\\rotatebox{90}{\\mytext Degree of disagreement, $\\nu$}}}\n"
			"\\put(0,-0.4){\\makebox(0,0)[cc]{\\myticks $0$}}\n"
			"\\put(10,-0.4){\\makebox(0,0)[cc]{\\myticks $1$}}\n"
			"\\put(-0.33,0){\\makebox(0,0)[cc]{\\myticks $0$}}\n"
			"\\put(-0.33,10){\\makebox(0,0)[cc]{\\myticks $1$}}\n";
		fwrite(txt4, strlen(txt4), 1, fptr);
		
		for (int i = 1; i < 10; i++) {
			snprintf(strbuff, 255,  "\\put(%d,-0.4){\\makebox(0,0)[cc]{\\myticks $0.%d$}}\n"
									"\\put(-0.5,%d){\\makebox(0,0)[cc]{\\myticks $0.%d$}}\n", i, i, i, i);
			fwrite(strbuff, strlen(strbuff), 1, fptr);
		}
	}
	
	/// Points
	if (vres.size > 0 && vres.size < chMatrix) {
		for (int i = 0; i < vres.size; i++) {
			for (int j = i+1; j < vres.size; j++) {
				snprintf(strbuff, 255, "\\put(%.6f,%.6f){%s\\circle*{\\myrad}}\n",
					vres.matR[i][j]*10, vres.matR[j][i]*10,
					(chColor ? plotColor(vres.matR[i][j],vres.matR[j][i],chLimA,chLimB) : ""));
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
		}
		
	} else if (vres.size >= chMatrix) {
		for (int i = 0; i < vres.size; i+=chJump) {
			for (int j = i+1; j < vres.size; j+=chJump) {
				snprintf(strbuff, 255, "\\put(%.6f,%.6f){%s\\circle*{\\myrad}}\n",
					vres.matR[i][j]*10, vres.matR[j][i]*10,
					(chColor ? plotColor(vres.matR[i][j],vres.matR[j][i],chLimA,chLimB) : ""));
				fwrite(strbuff, strlen(strbuff), 1, fptr);
			}
		}
	}
	
	/// End picture
	const char* txt5 = "\\end{picture}\n"
		"\\end{center}\n"
		"\\end{document}\n";
	fwrite(txt5, strlen(txt5), 1, fptr);
	
	int flen = ftell(fptr);
	fclose(fptr);
	
	snprintf(strbuff, 255, "Saved TeX file (%d bytes) %s", flen, filebuff);
	msg(strbuff);
}

/// Save PNG file
/// https://stackoverflow.com/questions/46596654/fltk-desktop-screenshot-issue
int save_png(const char *file,unsigned char* p,int w,int h){
    FILE *fp;
    fp = fl_fopen(file, "wb");
    if (fp == NULL)return 1;
    png_structp png = png_create_write_struct(PNG_LIBPNG_VER_STRING,
                                      0, 0, 0);
    if (png == NULL){
       fclose (fp);
       return 2;
    }
    png_infop info = png_create_info_struct(png);
    png_bytep ptr = (png_bytep)p;
    png_init_io(png, fp);
    png_set_IHDR(png, info, w, h, 8, PNG_COLOR_TYPE_RGB, PNG_INTERLACE_NONE,
         PNG_COMPRESSION_TYPE_DEFAULT, PNG_FILTER_TYPE_DEFAULT);
    png_write_info(png, info);
    for (int i = h; i>0; i--, ptr += w * 3) {
       png_write_row(png,ptr);
    }
    png_write_end(png,info);
    png_destroy_write_struct(&png, &info);
    fclose(fp);
    return 0;
}

/// Listen for screenshot
void listenScreenshot(Fl_Widget* wdj, void* ptr) {
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_SAVE_FILE);
	fc.title("Save screenshot");
	fc.directory(".");
	fc.filter("Screenshot\t*.png");
	if (fc.show() != 0)
		return;
	
	strncpy_safe(filebuff, fc.filename(), 255);
	if (strstr(filebuff, ".png") == NULL)
		strncat_safe(filebuff, ".png", 255);
	
	/// --- test/device.cxx @lines560-590
	/// Constructor for Fl_Image_Surface - 0 1 toggles HiDPI from Fl::screen_scale in main()
	Fl_Image_Surface* surface = new Fl_Image_Surface(mainWin->w(), mainWin->h(), 1);
	surface->draw(mainWin);
	Fl_Image* img = surface->image();
	delete surface;
	
	if (img != NULL) {
		/**
		Fl_Window* q = new Fl_Window(img->w(), img->h());
		Fl_Box* b = new Fl_Box(0, 0, img->w(), img->h());
		b->image(img);
		q->show();
		**/
		
		int s = save_png(filebuff, (unsigned char*)img->data()[0], img->data_w(), img->data_h());
		snprintf(strbuff, 255, "%s (%s)", (s == 0 ? "Saved screenshot" : "Failed to save screenshot"), filebuff);
		msg(strbuff);
		delete img;
		
	} else {
		msg("Error on image read");
	}
	/**
	http://seriss.com/people/erco/fltk/#Fl_Image
		const char *buf = img->data()[0];
	
	https://github.com/fltk/fltk/issues/31
		Fl_Image::data_w() and data_h() give the image dimensions in pixels and are unmutable;
		Fl_Image::w() and h() give the image drawing size in FLTK units.
		They initially equal Fl_Image::data_w() and data_h().
		Function Fl_Image::scale() allows to set the drawing size independently from
		the raw data size.
	**/
}

/// Listen for plot image
void listenPlotImage(Fl_Widget* wdj, void* ptr) {
	
	Fl_Native_File_Chooser fc;
	fc.type(Fl_Native_File_Chooser::BROWSE_SAVE_FILE);
	fc.title("Save PNG image");
	fc.directory(".");
	fc.filter("PNG\t*.png");
	if (fc.show() != 0)
		return;
	
	strncpy_safe(filebuff, fc.filename(), 255);
	if (strstr(filebuff, ".png") == NULL)
		strncat_safe(filebuff, ".png", 255);
	
	int d = (drawWin->w() < drawWin->h() ? drawWin->w() : drawWin->h());
	Fl_Image_Surface* surface = new Fl_Image_Surface(d, d, 1);
	surface->draw(drawWin);
	Fl_Image* img = surface->image();
	delete surface;
	
	if (img != NULL) {
		int s = save_png(filebuff, (unsigned char*)img->data()[0], img->data_w(), img->data_h());
		snprintf(strbuff, 255, "%s (%s)", (s == 0 ? "Saved PNG image" : "Failed to save PNG image"), filebuff);
		msg(strbuff);
		delete img;
	} else {
		msg("Error on image read");
	}
}

/// Listen for plot options
void listenPlotOpt(Fl_Widget* wdj, void* ptr) {
	
	/// --- test/menubar.cxx @lines47-54
	Fl_Menu_* mw = (Fl_Menu_*)wdj;
	const Fl_Menu_Item* m = mw->mvalue();
	int mq = m->value();
	long ma = (long)m->argument();
	//printf("menu %s %d %d\n", m->label(), mq, ma); 
	
	if (ma == 11) {
		plotVal[5] = mq;
		drawWin->myPlotColor(mq);
		drawWin->redraw();
	} else if (ma == 12) {
		plotVal[6] = mq;
		drawWin->myPlotMarks(mq);
		drawWin->redraw();
	} else if (ma == 13) {
		plotVal[7] = mq;
		drawWin->myPlotGrid(mq);
		drawWin->redraw();
	} else if (ma == 14) {
		plotVal[8] = mq;
		drawWin->myPlotText(mq);
		drawWin->redraw();
	}
}

/// Information window
void listenInfoWin(Fl_Widget* wdj, void* ptr) {
	
	Fl_Window* infoWin = new Fl_Window(
		mainWin->x()+mainWin->w()/2-450, mainWin->y()+mainWin->h()/2-250, 900, 500, "Information");
	
	Fl_Text_Buffer* buffInfo = new Fl_Text_Buffer();
	buffInfo->text(
		"InterCriteria Analysis Data\n\n"
		">>> Left panel\n"
		"Open text file or comma separated values file with separators tab, semicolon, comma.\n"
		"Open MS Excel/LibreOffice Calc and copy/paste the table with optional headers.\n"
		"Select Headers if header was copied, select Transpose if needed.\n"
		"All data lines (except #) are loaded as a single matrix.\n"
		"Matrix count can split the matrix for non-standard methods.\n\n"
		">>> Center panel\n"
		"Colors in the table and plot are determined from \xce\xb1 and \xce\xb2:\n"
		"    \xce\xbc > \xce\xb1 and \xce\xbd < \xce\xb2 - positive consonance (green),\n"
		"    \xce\xbc < \xce\xb2 and \xce\xbd > \xce\xb1 - negative consonance (red),\n"
		"    all other cases - dissonance (magenta).\n"
		"Button Export saves the result as a matrix or vector in a text file.\n"
		"Button Screen creates a screenshot of the application.\n\n"
		">>> Right panel\n"
		"Circle size - size of the plot points.\n"
		"Options - toggle Color/Marks/Grid/Text.\n"
		"TeX - saves the plot in TeX format.\n"
		"PNG - saves the plot in PNG format as displayed in the right panel.\n"
		"Clock/Matrix - when matrix rows or columns are greater than this value, "
			"show a clock during calculations and apply Jump for the plot.\n"
		"Jump/Matrix - skip this many elements when drawing the plot.\n"
		"Threads - maximum count of CPU threads to use for the calculations.\n\n"
		);
	
	Fl_Text_Display* textInfo = new Fl_Text_Display(5, 5, 890, 460, NULL);
	textInfo->textfont(FL_COURIER);
	textInfo->textsize(14);
	textInfo->wrap_mode(Fl_Text_Display::WRAP_AT_BOUNDS, 0);
	textInfo->buffer(buffInfo);
	
	Fl_Button* btnClose = new Fl_Button(400, 470, 100, 25, "Close");
	btnClose->callback(listenClose, infoWin);
	
	infoWin->resizable(textInfo);
	infoWin->set_modal();
	infoWin->end();
	infoWin->show();
}

/// About window
void listenAboutWin(Fl_Widget* wdj, void* ptr) {
	
	Fl_Window* aboutWin = new Fl_Window(
		mainWin->x()+mainWin->w()/2-450, mainWin->y()+mainWin->h()/2-250, 900, 500, "About");
	
	Fl_Text_Buffer* buffAbout = new Fl_Text_Buffer();
	buffAbout->text(
		"InterCriteria Analysis proposed for the first time by this article:\n"
		"  Atanassov K., D. Mavrov, V. Atanassova,\n"
		"  Intercriteria Decision Making: A New Approach for Multicriteria Decision Making,\n"
		"    Based on Index Matrices and Intuitionistic Fuzzy Sets,\n"
		"  Issues in Intuitionistic Fuzzy Sets and Generalized Nets, Vol. 11, 2014, 1-8.\n\n"
		"Main paper for the software application:\n"
		"  Ikonomov N., P. Vassilev, O. Roeva,\n"
		"  ICrAData - Software for InterCriteria Analysis,\n"
		"  International Journal Bioautomation, Vol. 22(1), 2018, 1-10.\n\n"
		"This software application has been developed with the partial financial support of:\n"
		"  Changes in versions from 1.3 to 2.6 have been implemented for\n"
		"    project DN 17/06 ``A New Approach, Based on an Intercriteria Data Analysis,\n"
		"    to Support Decision Making in `in silico' Studies of Complex Biomolecular Systems'',\n"
		"    funded by the National Science Fund of Bulgaria.\n"
		"  Changes in versions from 0.9.6 to 1.2 have been implemented for\n"
		"    project DFNI-I-02-5 ``InterCriteria Analysis: A New Approach to Decision Making'',\n"
		"    funded by the National Science Fund of Bulgaria.\n\n\n"
		"InterCriteria Analysis Data\n"
		"  Version: 2.7\n"
		"  Date: 22 March 2025\n"
		"  Compiled by: GCC/MSVC\n\n"
		);
	
	Fl_Text_Display* textAbout = new Fl_Text_Display(5, 5, 890, 460, NULL);
	textAbout->textfont(FL_COURIER);
	textAbout->textsize(14);
	textAbout->wrap_mode(Fl_Text_Display::WRAP_AT_BOUNDS, 0);
	textAbout->buffer(buffAbout);
	
	Fl_Button* btnClose = new Fl_Button(400, 470, 100, 25, "Close");
	btnClose->callback(listenClose, aboutWin);
	
	aboutWin->resizable(textAbout);
	aboutWin->set_modal();
	aboutWin->end();
	aboutWin->show();
}

/// Exit application
void listenExit(Fl_Widget* wdj, void* ptr) {
	exit(0);
}

/// Exit window
void listenExitWin(Fl_Widget* wdj, void* ptr) {
	
	Fl_Window* exitWin = new Fl_Window(
		mainWin->x()+mainWin->w()/2-125, mainWin->y()+mainWin->h()/2-45,
		250, 90, "Exit");
	
	Fl_Box* msgText = new Fl_Box(10, 20, 10, 10, "Exit the application?");
	msgText->align(FL_ALIGN_RIGHT);
	msgText->box(FL_NO_BOX);
	
	Fl_Button* btnExit = new Fl_Button(20, 50, 100, 25, "Exit");
	btnExit->callback(listenExit, exitWin);
	
	Fl_Button* btnClose = new Fl_Button(130, 50, 100, 25, "Cancel");
	btnClose->callback(listenClose, exitWin);
	
	exitWin->resizable(0);
	exitWin->set_modal();
	exitWin->end();
	exitWin->show();
}

/// Constructor
int main(int argc, char* argv[]) {
	
	/// Screen scale
	//Fl::screen_scale(0, 2.0);
	
	/// Set light mode
	Fl::background(244, 240, 236);
	Fl::background2(255, 255, 255);
	Fl::foreground(0, 0, 0);
	
	/// Window
	mainWin = new Fl_Window(1400, 700, "ICrAData v2.7");
	
	/// Tiles
	Fl_Tile* tile = new Fl_Tile(0, 0, 1400, 700, NULL);
	
	
	/// Panel left 1
	Fl_Group* panelL1 = new Fl_Group(0, 0, 400, 185, NULL);
	panelL1->box(FL_DOWN_BOX);
	
	Fl_Button* btnOpen = new Fl_Button(10, 10, 120, 25, "Open");
	btnOpen->tooltip("Open file. Parameters, if present, are loaded from first line.");
	btnOpen->callback(listenOpen, btnOpen);
	
	Fl_Button* btnSave = new Fl_Button(10+120+10, 10, 120, 25, "Save");
	btnSave->tooltip("Save file. Parameters are saved on first line.");
	btnSave->callback(listenSave, btnSave);
	
	Fl_Button* btnClean = new Fl_Button((10+120)*2+10, 10, 120, 25, "Clean");
	btnClean->tooltip("Release all working memory used by the application.");
	btnClean->callback(listenClean, btnClean);
	
	chMth = new Fl_Choice(10+120+10, 10+25+10, 120, 25, "ICrA Method");
	chMth->tooltip("Method for InterCriteria Analysis. Standard directly applies the base algorithm. "
		"The others require at least three input matrices.");
	chMth->menu(listMth);
	chMth->callback(listenMethod, chMth);
	
	chVar = new Fl_Choice(10+120+10, (10+25)*2+10, 120, 25, "ICrA Variant");
	chVar->tooltip("Variant for InterCriteria Analysis. This is the base algorithm.");
	chVar->menu(listVar);
	
	chSep = new Fl_Choice(10+120+10, (10+25)*3+10, 120, 25, "Separator");
	chSep->tooltip("Column separator for the data.");
	chSep->menu(listSep);
	
	chMatCnt = new Fl_Spinner((10+120)*2+10+60, 10+25+10, 60, 25, "MatCnt");
	chMatCnt->tooltip("Matrix count is applied to Aggregated and Criteria Pair.");
	chMatCnt->minimum(1);
	chMatCnt->maximum(9999);
	chMatCnt->step(1);
	chMatCnt->value(1);
	chMatCnt->deactivate();
	
	chTr = new Fl_Check_Button((10+120)*2+10, (10+25)*2+10, 120, 25, "Transpose");
	chTr->tooltip("Transpose the matrix. Applies only to Standard.");
	chTr->value(0);
	
	chHdr = new Fl_Check_Button((10+120)*2+10, (10+25)*3+10, 120, 25, "Headers");
	chHdr->tooltip("Row and column headers for the data. Applies only to Standard.");
	chHdr->value(1);
	
	Fl_Button* btnCalc = new Fl_Button(10, (10+25)*4+10, 400-10-10, 25, "Analysis");
	btnCalc->tooltip("Make the calculations and display them.");
	btnCalc->callback(listenCalc, btnCalc);
	
	panelL1->resizable(0);
	panelL1->end();
	
	
	/// Panel left 2
	Fl_Group* panelL2 = new Fl_Group(0, 185, 400, 400, NULL);
	panelL2->box(FL_DOWN_BOX);
	
	buffInput = new Fl_Text_Buffer();
	buffInput->text(
		"# Open file or copy/paste data here\n"
		"# Column separators: tab semicolon comma\n"
		"# Recognized numbers: 1.7 and 1,7\n\n"
		"x;E;F;G;H;I\n"
		"A;6;5;3;7;6\n"
		"B;7;7;8;1;3\n"
		"C;4;3;5;9;1\n"
		"D;4;5;6;7;8\n"
		);
	
	textInput = new Fl_Text_Editor(0, 185, 400, 400, NULL);
	textInput->textfont(FL_COURIER);
	textInput->textsize(14);
	textInput->wrap_mode(Fl_Text_Editor::WRAP_NONE, 0);
	textInput->buffer(buffInput);
	
	panelL2->end();
	
	
	/// Panel left 3
	Fl_Group* panelL3 = new Fl_Group(0, 585, 400, 115, NULL);
	panelL3->box(FL_DOWN_BOX);
	
	buffMsg = new Fl_Text_Buffer();
	buffMsg->text("");
	
	textMsg = new Fl_Text_Display(0, 585, 400, 115, NULL);
	textMsg->textfont(FL_COURIER);
	textMsg->textsize(14);
	textMsg->wrap_mode(Fl_Text_Display::WRAP_NONE, 0);
	textMsg->buffer(buffMsg);
	
	panelL3->end();
	
	
	/// Panel center 1
	Fl_Group* panelC1 = new Fl_Group(400, 0, 680, 80, NULL);
	panelC1->box(FL_DOWN_BOX);
	
	Fl_Spinner* chAlpha = new Fl_Spinner(400+10+60, 10, 60, 25, "Alpha");
	chAlpha->tooltip("Table and plot colors: \xce\xbc > \xce\xb1 and \xce\xbd < \xce\xb2 - positive consonance (green), "
		"\xce\xbc < \xce\xb2 and \xce\xbd > \xce\xb1 - negative consonance (red), "
		"all other cases - dissonance (magenta).");
	chAlpha->minimum(0.5);
	chAlpha->maximum(1.0);
	chAlpha->step(0.01);
	chAlpha->value(0.75);
	chAlpha->callback(listenChAlpha, chAlpha);
	
	Fl_Spinner* chBeta = new Fl_Spinner(400+10+60, 10+25+10, 60, 25, "Beta");
	chBeta->tooltip("Table and plot colors: \xce\xbc > \xce\xb1 and \xce\xbd < \xce\xb2 - positive consonance (green), "
		"\xce\xbc < \xce\xb2 and \xce\xbd > \xce\xb1 - negative consonance (red), "
		"all other cases - dissonance (magenta).");
	chBeta->minimum(0.0);
	chBeta->maximum(0.5);
	chBeta->step(0.01);
	chBeta->value(0.25);
	chBeta->callback(listenChBeta, chBeta);
	
	Fl_Spinner* chDigits = new Fl_Spinner(400+(10+60)*2+60, 10, 60, 25, "Digits");
	chDigits->tooltip("Digits after the decimal separator.");
	chDigits->minimum(1);
	chDigits->maximum(16);
	chDigits->step(1);
	chDigits->value(4);
	chDigits->callback(listenChDigits, chDigits);
	
	Fl_Spinner* chCW = new Fl_Spinner(400+(10+60)*2+60, 10+25+10, 60, 25, "Width");
	chCW->tooltip("Table column width.");
	chCW->minimum(10);
	chCW->maximum(500);
	chCW->step(10);
	chCW->value(80);
	chCW->callback(listenChWidth, chCW);
	
	chTable = new Fl_Choice(400+(10+120)*2+10+60, 10, 120, 25, "Table 1");
	chTable->tooltip("Display table 1.");
	chTable->menu(listTable);
	chTable->callback(listenChTable, chTable);
	
	chTable2 = new Fl_Choice(400+(10+120)*2+10+60, 10+25+10, 120, 25, "Table 2");
	chTable2->tooltip("Display table 2.");
	chTable2->menu(listTable);
	chTable2->callback(listenChTable2, chTable2);
	
	Fl_Button* btnExport = new Fl_Button(400+(10+120)*3+10+60, 10, 100, 25, "Export");
	btnExport->tooltip("Export tables.");
	btnExport->callback(listenExpWin, btnExport);
	
	Fl_Button* btnScreenshot = new Fl_Button(400+(10+120)*3+10+60, 10+25+10, 100, 25, "Screen");
	btnScreenshot->tooltip("Screenshot of the application.");
	btnScreenshot->callback(listenScreenshot, btnScreenshot);
	
	Fl_Button* btnInfo = new Fl_Button(400+(10+120)*3+10+60+10+100, 10, 100, 25, "Info");
	btnInfo->tooltip("Information for the application.");
	btnInfo->callback(listenInfoWin, btnInfo);
	
	Fl_Button* btnAbout = new Fl_Button(400+(10+120)*3+10+60+10+100, 10+25+10, 100, 25, "About");
	btnAbout->tooltip("About the application.");
	btnAbout->callback(listenAboutWin, btnAbout);
	
	panelC1->resizable(0);
	panelC1->end();
	
	
	/// Panel center 2
	Fl_Group* panelC2 = new Fl_Group(400, 80, 680, 310, NULL);
	panelC2->box(FL_DOWN_BOX);
	
	vtable = new MyTable(400, 80, 680, 310, NULL);
	
	panelC2->end();
	
	
	/// Panel center 3
	Fl_Group* panelC3 = new Fl_Group(400, 390, 680, 310, NULL);
	panelC3->box(FL_DOWN_BOX);
	
	vtable2 = new MyTable(400, 390, 680, 310, NULL);
	
	panelC3->end();
	
	
	/// Panel right 1
	Fl_Group* panelR1 = new Fl_Group(1080, 0, 320, 45, NULL);
	panelR1->box(FL_DOWN_BOX);
	
	Fl_Spinner* chPlotPoints = new Fl_Spinner(1080+10, 10, 50, 25, NULL);
	chPlotPoints->tooltip("Circle size.");
	chPlotPoints->minimum(1);
	chPlotPoints->maximum(20);
	chPlotPoints->step(1);
	chPlotPoints->value(5);
	chPlotPoints->callback(listenPlotPoints, chPlotPoints);
	
	Fl_Menu_Item listPlotOpt[] = {
		{"Color", 0, 0, (void*)11, FL_MENU_TOGGLE|FL_MENU_VALUE},
		{"Marks", 0, 0, (void*)12, FL_MENU_TOGGLE},
		{"Grid", 0, 0, (void*)13, FL_MENU_TOGGLE},
		{"Text", 0, 0, (void*)14, FL_MENU_TOGGLE},
		{0}
	};
	
	Fl_Menu_Button* chPlotOpt = new Fl_Menu_Button(1080+10+50+10, 10, 100, 25, "Options");
	chPlotOpt->tooltip("Plot options.");
	chPlotOpt->menu(listPlotOpt);
	chPlotOpt->callback(listenPlotOpt, chPlotOpt);
	
	Fl_Button* btnPlotTeX = new Fl_Button(1080+10+50+10+100+10, 10, 60, 25, "TeX");
	btnPlotTeX->tooltip("Plot as TeX file.");
	btnPlotTeX->callback(listenPlotTeX, btnPlotTeX);
	
	Fl_Button* btnPlotImage = new Fl_Button(1080+10+50+10+100+10+60+10, 10, 60, 25, "PNG");
	btnPlotImage->tooltip("Plot as PNG image.");
	btnPlotImage->callback(listenPlotImage, btnPlotImage);
	
	panelR1->resizable(0);
	panelR1->end();
	
	
	/// Panel right 2
	Fl_Group* panelR2 = new Fl_Group(1080, 45, 320, 575, NULL);
	panelR2->box(FL_DOWN_BOX);
	
	drawWin = new MyDraw(1080, 45, 320, 575, NULL);
	
	panelR2->end();
	
	
	/// Panel right 3
	Fl_Group* panelR3 = new Fl_Group(1080, 620, 320, 80, NULL);
	panelR3->box(FL_DOWN_BOX);
	
	chPlotMatrix = new Fl_Spinner(1080+10+80+10, 620+10, 70, 25, "Clock/Matrix");
	chPlotMatrix->tooltip("Matrix break point.");
	chPlotMatrix->minimum(1000);
	chPlotMatrix->maximum(100000);
	chPlotMatrix->step(1000);
	chPlotMatrix->value(3000);
	chPlotMatrix->callback(listenPlotMatrix, chPlotMatrix);
	
	Fl_Spinner* chPlotJump = new Fl_Spinner(1080+10+80+10, 620+10+25+10, 70, 25, "Jump/Matrix");
	chPlotJump->tooltip("Jump after matrix break point.");
	chPlotJump->minimum(5);
	chPlotJump->maximum(100);
	chPlotJump->step(5);
	chPlotJump->value(20);
	chPlotJump->callback(listenPlotJump, chPlotJump);
	
	int th = omp_get_max_threads();
	Fl_Spinner* chThreads = new Fl_Spinner(1080+10+80+10+70+10+60, 620+10, 50, 25, "Threads");
	chThreads->tooltip("Maximum CPU threads.");
	chThreads->minimum(1);
	chThreads->maximum(th);
	chThreads->step(1);
	chThreads->value(th);
	chThreads->callback(listenChThreads, chThreads);
	
	Fl_Light_Button* chColor = new Fl_Light_Button(1080+10+80+10+70+10, 620+10+25+10, 110, 25, "Light/Dark");
	chColor->tooltip("Switch between light and dark mode.");
	chColor->value(1);
	chColor->callback(listenChColor, chColor);
	
	panelR3->resizable(0);
	panelR3->end();
	
	tile->end();
	
	/// Initialize variables
	vfile.flines = -1;
	vdata.rW = vdata.cW = vdata.rsize = vdata.csize = -1;
	vres.size = -1;
	msg("ICrAData v2.7");
	
	mainWin->end();
	mainWin->resizable(mainWin);
	mainWin->callback(listenExitWin, mainWin);
	
	/// Clock window
	clockWin = new Fl_Window(200, 200, NULL);
	new Fl_Clock(0, 0, 200, 200, NULL);
	clockWin->border(0);
	clockWin->resizable(0);
	clockWin->set_modal();
	clockWin->end();
	
	mainWin->show(argc, argv);
	return Fl::run();
}


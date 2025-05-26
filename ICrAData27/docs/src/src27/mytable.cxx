/// MyTable

#include <FL/Fl.H>
#include <FL/fl_draw.H>

#include "mytable.h"

/// MyTable - cell data
void MyTable::myCellData(double** wd, int wr, int wc, char** rh, int rs, char** ch, int cs, int ds) {
	wdata = wd;
	wrows = wr;
	wcols = wc;
	rhead = rh;
	rsize = rs;
	chead = ch;
	csize = cs;
	disp  = ds;
}

/// MyTable - set limits
void MyTable::myLimitA(double a) { lima = a; }
void MyTable::myLimitB(double b) { limb = b; }
void MyTable::myDigits(int d) { dgts = d; }

/// MyTable - cell color based on limits
Fl_Color MyTable::myColor(int r, int c) {
	
	if (disp > 1 && wrows > 0 && wcols > 0 && r != c) {
		if (wdata[r][c] > lima && wdata[c][r] < limb)
			return (r < c ? FL_DARK_GREEN : FL_RED);
		else if (wdata[r][c] < limb && wdata[c][r] > lima)
			return (r < c ? FL_RED : FL_DARK_GREEN);
		else
			return FL_MAGENTA;
	}
	
	return Fl::get_color(FL_FOREGROUND_COLOR);
}

/// MyTable - constructor
MyTable::MyTable(int x, int y, int w, int h, const char* lbl) : Fl_Table(x, y, w, h, lbl) {
	/// no data
	myCellData(NULL,-1,-1, NULL,-1, NULL,-1, 0); /// this must be first line
	myLimitA(0.75);
	myLimitB(0.25);
	myDigits(4);
	rows(0);
	cols(0);
	/// table
	box(FL_DOWN_FRAME);
	table_box(FL_NO_BOX);
	selection_color(FL_CYAN);
	/// rows
	row_resize(0);
	row_resize_min(10);
	row_height_all(20);
	row_header(1);
	row_header_width(80);
	/// cols
	col_resize(0);
	col_resize_min(10);
	col_width_all(80);
	col_header(1);
	col_header_height(25);
}

/// MyTable - draw cell
void MyTable::draw_cell(TableContext context, int r, int c, int x, int y, int w, int h) {
	
	static char scell[64];
	static char srhead[256];
	static char schead[256];
	snprintf(scell, 63, "---");
	snprintf(srhead, 255, "---");
	snprintf(schead, 255, "---");
	
	static char sdig1[64];
	static char sdig2[64];
	snprintf(sdig1, 63, "%%.%df", dgts);
	snprintf(sdig2, 63, "(%%.%df;%%.%df)", dgts, dgts);
	
	if (disp == 1) { /// input 1
		if (wrows > 0 && wcols > 0)
			snprintf(scell, 63, sdig1, wdata[r][c]);
		if (rsize > 0 && r < rsize && r < wrows)
			snprintf(srhead, 255, "%s", rhead[r]);
		if (csize > 0 && c < csize && c < wcols)
			snprintf(schead, 255, "%s", chead[c]);
		
	} else if (disp > 1) {
		if (wrows > 0 && wcols > 0 && r != c) {
			
			if (disp == 2) /// mu and nu 2
				snprintf(scell, 63, sdig1, wdata[r][c]);
			
			else if (disp == 3 && r < c) /// (mu;nu) upper 3
				snprintf(scell, 63, sdig2, wdata[r][c], wdata[c][r]);
			else if (disp == 3 && r > c) /// (mu;nu) lower 3
				snprintf(scell, 63, sdig2, wdata[c][r], wdata[r][c]);
			
			else if (disp == 4 && r < c) /// mu upper 4
				snprintf(scell, 63, sdig1, wdata[r][c]);
			else if (disp == 4 && r > c) /// mu lower 4
				snprintf(scell, 63, sdig1, wdata[c][r]);
			
			else if (disp == 5 && r < c) /// nu upper 5
				snprintf(scell, 63, sdig1, wdata[c][r]);
			else if (disp == 5 && r > c) /// nu lower 5
				snprintf(scell, 63, sdig1, wdata[r][c]);
			
		}
		
		if (rsize > 0 && r < rsize && r < wrows)
			snprintf(srhead, 255, "%s", rhead[r]);
		if (rsize > 0 && c < rsize && c < wcols)
			snprintf(schead, 255, "%s", rhead[c]);
	}
	
	switch (context) {
	case CONTEXT_STARTPAGE:
		fl_font(FL_HELVETICA, 14);
		return;
	case CONTEXT_ROW_HEADER:
		fl_push_clip(x,y,w,h);
			fl_draw_box(FL_THIN_UP_BOX, x,y,w,h, row_header_color());
			fl_color(Fl::get_color(FL_FOREGROUND_COLOR));
			fl_draw(srhead, x,y,w,h, FL_ALIGN_CENTER);
		fl_pop_clip();
		return;
	case CONTEXT_COL_HEADER:
		fl_push_clip(x,y,w,h);
			fl_draw_box(FL_THIN_UP_BOX, x,y,w,h, col_header_color());
			fl_color(Fl::get_color(FL_FOREGROUND_COLOR));
			fl_draw(schead, x,y,w,h, FL_ALIGN_CENTER);
		fl_pop_clip();
		return;
	case CONTEXT_CELL:
		fl_push_clip(x,y,w,h);
			/// cell background
			fl_color(is_selected(r,c) ? selection_color() : Fl::get_color(FL_BACKGROUND2_COLOR));
			fl_rectf(x,y,w,h); /// rectangle fill
			/// cell contents
			fl_color(myColor(r,c));
			fl_draw(scell, x,y,w,h, FL_ALIGN_RIGHT);
			/// cell border
			fl_color(Fl::get_color(FL_BACKGROUND_COLOR));
			fl_rect(x,y,w,h); /// no fill
		fl_pop_clip();
		return;
	default:
		return;
	}
}


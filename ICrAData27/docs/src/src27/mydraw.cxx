/// MyDraw

#include <FL/Fl.H>
#include <FL/fl_draw.H>

#include "mydraw.h"

/// MyDraw - constructor
MyDraw::MyDraw(int x, int y, int w, int h, const char* lbl) : Fl_Box(x, y, w, h, lbl) {
	myPlotData(NULL, -1);
	myLimitA(0.75);
	myLimitB(0.25);
	myPlotPoints(5);
	myPlotMatrix(3000);
	myPlotJump(20);
	myPlotColor(1);
	myPlotMarks(0);
	myPlotGrid(0);
	myPlotText(0);
}

/// MyDraw - plot data
void MyDraw::myPlotData(double** wd, int ws) {
	wdata = wd;
	wsize = ws;
}

void MyDraw::myLimitA(double a) { lima = a; }
void MyDraw::myLimitB(double b) { limb = b; }
void MyDraw::myPlotPoints(int v) { vpoints = v; }
void MyDraw::myPlotMatrix(int v) { vmatrix = v; }
void MyDraw::myPlotJump(int v) { vjump = v; }
void MyDraw::myPlotColor(int v) { vcolor = v; }
void MyDraw::myPlotMarks(int v) { vmarks = v; }
void MyDraw::myPlotGrid(int v) { vgrid = v; }
void MyDraw::myPlotText(int v) { vtext = v; }

/// MyDraw - color based on limits
Fl_Color MyDraw::myColor(int r, int c) {
	
	if (vcolor && wsize > 0 && r != c) {
		if (wdata[r][c] > lima && wdata[c][r] < limb)
			return (r < c ? FL_DARK_GREEN : FL_RED);
		else if (wdata[r][c] < limb && wdata[c][r] > lima)
			return (r < c ? FL_RED : FL_DARK_GREEN);
		else
			return FL_MAGENTA;
	}
	
	return Fl::get_color(FL_FOREGROUND_COLOR);
}

/// Draw
void MyDraw::draw() {
	
	int x = this->x();
	int y = this->y();
	int w = this->w();
	int h = this->h();
	
	int mm = (w < h ? w : h);
	int dd = 20 * (vtext ? 2 : 1);
	int nn = mm-2*dd;
	int rr = (double)mm/64.0;
	
	/// Clear display
	fl_color(Fl::get_color(FL_BACKGROUND2_COLOR));
	fl_rectf(x,y,w,h); /// rectangle fill
	
	/// Color for triangle/marks/grid/points
	fl_color(Fl::get_color(FL_FOREGROUND_COLOR));
	
	/// Triangle
	fl_line_style(0);
	fl_line(x+dd, y+dd,    x+dd,    y+mm-dd); /// left
	fl_line(x+dd, y+mm-dd, x+mm-dd, y+mm-dd); /// bottom
	fl_line(x+dd, y+dd,    x+mm-dd, y+mm-dd); /// main diagonal
	
	/// Marks
	if (vmarks && !vgrid) {
		for (int i = 1; i < 10; i++) /// left
			fl_line(x+dd-rr, y+dd+0.1*i*nn, x+dd+rr, y+dd+0.1*i*nn);
		for (int i = 1; i < 10; i++) /// bottom
			fl_line(x+dd+0.1*i*nn, y+dd+nn-rr, x+dd+0.1*i*nn, y+dd+nn+rr);
	}
	
	/// Grid
	if (vgrid) {
		for (int i = 1; i < 10; i++) /// left
			fl_line(x+dd-rr, y+dd+0.1*i*nn, x+dd+0.1*i*nn, y+dd+0.1*i*nn);
		for (int i = 1; i < 10; i++) /// bottom
			fl_line(x+dd+0.1*i*nn, y+dd+0.1*i*nn, x+dd+0.1*i*nn, y+dd+nn+rr);
	}
	
	/// Text
	if (vtext) {
		fl_font(FL_HELVETICA, 12);
		const char* tm[] = {"0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"};
		for (int i = 1; i < 10; i++) {
			fl_draw(tm[10-i], x+0.5*dd-rr, y+1.1*dd+0.1*i*nn); /// left
			fl_draw(tm[i], x+0.8*dd+0.1*i*nn, y+1.6*dd+nn-rr); /// bottom
		}
		
		/**
		fl_font(FL_HELVETICA, 12);
		const char* t1 = "Degree of agreement, \xce\xbc";
		const char* t2 = "Degree of disagreement, \xce\xbd";
		///int t1w, t1h, t2w, t2h = 0;
		///fl_measure(t1, t1w, t1h, 0);
		///fl_measure(t2, t2w, t2h, 0);
		fl_draw(      t1, x+dd, y+1.85*dd+nn); /// bottom
		fl_draw(90.0, t2, x+0.25*dd, y+dd+nn); /// left
		**/
	}
	
	/// Points
	/// fl_circle is faster and more precise than fl_arc / fl_pie
	if (wsize > 0 && wsize < vmatrix) {
		for (int i = 0; i < wsize; i++) {
			for (int j = i+1; j < wsize; j++) {
				fl_begin_polygon();
				fl_color(myColor(i,j));
				fl_circle(x+dd+wdata[i][j]*nn, y+nn+dd-wdata[j][i]*nn, vpoints);
				fl_end_polygon();
			}
		}
		
	} else if (wsize >= vmatrix) {
		for (int i = 0; i < wsize; i+=vjump) {
			for (int j = i+1; j < wsize; j+=vjump) {
				fl_begin_polygon();
				fl_color(myColor(i,j));
				fl_circle(x+dd+wdata[i][j]*nn, y+nn+dd-wdata[j][i]*nn, vpoints);
				fl_end_polygon();
			}
		}
	}
	
}


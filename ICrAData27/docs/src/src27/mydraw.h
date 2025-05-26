/// MyDraw header

#include <FL/Fl_Box.H>

class MyDraw : public Fl_Box {
	private:
		double** wdata;
		int      wsize;
		double    lima;
		double    limb;
		int    vpoints;
		int    vmatrix;
		int      vjump;
		int     vcolor;
		int     vmarks;
		int      vgrid;
		int      vtext;
	protected:
		void draw();
		Fl_Color myColor(int r, int c);
	public:
		MyDraw(int x, int y, int w, int h, const char* lbl);
		void myPlotData(double** wd, int ws);
		void myLimitA(double a);
		void myLimitB(double b);
		void myPlotPoints(int v);
		void myPlotMatrix(int v);
		void myPlotJump(int v);
		void myPlotColor(int v);
		void myPlotMarks(int v);
		void myPlotGrid(int v);
		void myPlotText(int v);
};


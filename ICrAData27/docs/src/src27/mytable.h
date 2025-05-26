/// MyTable header

#include <FL/Fl_Table.H>

class MyTable : public Fl_Table {
	private:
		double** wdata;
		int      wrows;
		int      wcols;
		char**   rhead;
		int      rsize;
		char**   chead;
		int      csize;
		int       disp;
		double    lima;
		double    limb;
		int       dgts;
	protected:
		void draw_cell(TableContext context, int r=0, int c=0, int x=0, int y=0, int w=0, int h=0);
		Fl_Color myColor(int r, int c);
	public:
		MyTable(int x, int y, int w, int h, const char* lbl);
		void myCellData(double** wd, int wr, int wc, char** rh, int rs, char** ch, int cs, int ds);
		void myLimitA(double a);
		void myLimitB(double b);
		void myDigits(int d);
};


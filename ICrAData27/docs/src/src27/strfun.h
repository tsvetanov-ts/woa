/// String functions header

void strncpy_safe(char* dest, const char* sour, int len);
void strncat_safe(char* dest, const char* sour, int len);
void stringLineEnd(char* str);
double dataElem(char* start, int ind, char sep);
char* openFile(const char* fname);
struct vizfile loadFile(char* fbuf);
struct vizdata readFile(struct vizfile vfile, int sep, int hdr, int tr);
int saveFile(const char* fn, struct vizfile vfile, int icamth, int matcnt, int hdr, int tr, int sep);


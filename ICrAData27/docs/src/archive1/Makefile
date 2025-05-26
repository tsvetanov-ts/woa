# Makefile for ICrAData

# Debug   -g -Xlint:all
# Release --release 7

JAVAC      = javac --release 7
JAVAR      = java -cp .
JAVAD      = jar -c
TARGET     = ICrAData.jar
SOURCES    = $(wildcard *.java)
CLASSES    = $(SOURCES:.java=.class)
RM         = del /F

%.class : %.java
	$(JAVAC) $<

.PHONY: all clean run deploy

all: $(CLASSES)

clean:
	$(RM) $(CLASSES) $(TARGET) *.class

run:
	$(JAVAR) ICrAData

deploy:
	$(JAVAD) -e ICrAData -f $(TARGET) *.class


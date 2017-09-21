all: Assemble.class CodeKey.class Converter.class Offset.class OpCodeTable.class Operands.class OutputWriter.class ProcessFile.class SymbolTable.class
%.class: %.java
	javac -d . -classpath . $<
clean:
	rm -f *.class 
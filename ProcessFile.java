import java.io.File;
import java.util.Scanner;

public class ProcessFile {
	private OutputWriter output;	//write to file
	private SymbolTable symbols;	//generate table of symbols
	private Converter convert;		//make conversions
	private Operands operands;		//generate opcodes
	private OpCodeTable opcodes;	//store opcode values and types
	public static int line; // global line number
	public static String BASE; // global reference to base symbol
	public static int OFFSET; // global reference to current file offset
	public static String[] LINEARGS = { null, null, null }; // line arguments
															// for standard out
	private String encoding; // used specified encoding type, defaults at UTF-8
	private boolean START = false; // if start found
	private boolean endFound = false; // if end found, done
	private int startingOffset = 0; // if starting offset specified, standard out needs to know, (zeroed out after initial print)

	ProcessFile(String encoding) {	//init
		output = new OutputWriter();
		symbols = new SymbolTable();
		convert = new Converter();
		operands = new Operands(symbols);
		opcodes = new OpCodeTable();
		ProcessFile.BASE = null; // prepare static variables (potentially on multiple files)
		ProcessFile.line = 0; // line number
		ProcessFile.OFFSET = 0; // offset value
		this.encoding = encoding; // specify file encoding type for scanner
	}

	Scanner input; // firstpass file scanner
	Scanner inputFirst; // second file scanner
	int offset = 0;

	public void runFile(String filename) {
		String[] outfile = filename.split("\\.");	//delimit with period
		try {
			File file = new File(filename);
			input = new Scanner(file, this.encoding); // try input scanner with encoding (defaulted at UTF-8)
			inputFirst = new Scanner(file, this.encoding); // scanner for first pass
			output.open(outfile[0]); // open new write file
		} catch (IllegalArgumentException type) {
			System.out.println("ERROR: Bad encoding type: " + this.encoding);
			return;		//if encoding type specified does not work with scanner (does not catch legal encoding type on different file)
		} catch (Exception ex) {
			System.out.println("ERROR: Bad filename: " + filename);
			return;
		}
		firstPass(); // set symTab with offsets
		ProcessFile.line = 1; // start @ line 1
		readLoop(); // generate the actual code through file
		return;
	}

	public void readLoop() {
		String instruction;
		String line;

		int dispOffset; // offset to display
		String instructionOut;
		while (input.hasNextLine() && !endFound) {
			line = input.nextLine();
			if (line.trim().isEmpty() && !input.hasNextLine()) { // if this line and next line are empty
				break; // nothing in line, end
			}
			dispOffset = ProcessFile.OFFSET; // get current offset before change is made with readline()
			instruction = readLine(line); // get instruction
			
			if (instruction.length() > 8) { // if instruction is bigger than 8 chars, truncate for display purposes
				instructionOut = instruction.substring(0, 3) + "...";
			} else {
				instructionOut = instruction; // if not, instruction out is the	same as file write instruction
			}
			standardOut(ProcessFile.line, dispOffset, ProcessFile.LINEARGS, instructionOut); // standard out with the line
			output.writeLine(instruction); // write to output .exe
			output.set(); // flush file stream
			ProcessFile.line++; // increment line #
		}
		// final offset display with line
		String offsetOut = String.format("%6s", convert.byteBin(ProcessFile.OFFSET)).replace(' ', '0'); // final display

		String lineOut = String.format("%03d\t", ProcessFile.line);
		String stdOut = String.format("%-4s%-10s", lineOut, offsetOut);
		System.out.println(stdOut);
		symbols.display(); // display final symbol table
		output.close(); // close out file
		input.close(); // close in file
		return; // done
	}

	public String readLine(String line) { // read line from file and delegate to operands
		String[] argsArray;
		int opcode = 0;
		int offsetChange = 0;
		char[] characters = line.toCharArray();	//for checking individual characters
		// initial checks
		if (characters.length <= 0 || characters[0] == '.') {
			ProcessFile.LINEARGS = new String[] { line, "", "" }; // if comment or no line, display as simple
			return ""; // comment or empty line
		}

		String arg1; // opcode
		String arg2; // operand
		argsArray = line.trim().split("\\s+");
		if (argsArray[0].equalsIgnoreCase(".") || argsArray[0].equalsIgnoreCase("END")) {
			ProcessFile.LINEARGS = new String[] { "", line.trim(), "" };
			if (argsArray[0].equalsIgnoreCase("END")) { // check if end found to terminate reading loop

				endFound = true;
			}
			return ""; // comment or single symbol in line or end
		}

		if (characters[0] == ' ' || characters[0] == '\t' || characters[0] == '\r' || characters[0] == '\n') { // check if symbol
			// no symbol
			arg1 = argsArray[0]; // modify line components accordingly
			String arg2Out;
			if (arg1.equalsIgnoreCase("START") && !this.START) { // check if start and if not found

				String newOffset;
				try {
					ProcessFile.OFFSET = Integer.parseInt(argsArray[2].trim(), 16);
					newOffset = ProcessFile.OFFSET + ""; // attempt to change offset with arg
				} catch (Exception noOffset) {
					newOffset = "";
				}
				ProcessFile.LINEARGS = new String[] { "", arg1, newOffset };
				this.START = true; // display this offset on line, for stdOut
				return "";
			}
			if (opcodes.args(arg1)) {
				try {
					arg2 = argsArray[1]; // find operand at next trimmed spot
					arg2Out = arg2;
				} catch (ArrayIndexOutOfBoundsException arg2Error) {
					System.out.println("ERROR line " + ProcessFile.line + ": operand not found for: " + arg1);
					return "";
				}
			} else {
				arg2 = null; // otherwise, no arg
				arg2Out = ""; // display will be empty string
			}
			ProcessFile.LINEARGS = new String[] { "", arg1, arg2Out };
			
		} else {
			arg1 = argsArray[1]; // symbol at argsArray[0]
			String arg2Out;
			if (arg1.equalsIgnoreCase("START") && !this.START) {
				String newOffset;
				try {
					ProcessFile.OFFSET = Integer.parseInt(argsArray[2].trim(), 16); // if start, get offset
					newOffset = argsArray[2];
					this.startingOffset = ProcessFile.OFFSET;

				} catch (Exception noOffset) {
					newOffset = "";
				}
				ProcessFile.LINEARGS = new String[] { argsArray[0], arg1, newOffset };
				this.START = true; // display this offset on line, for stdOut
				return "";
			}
			if (opcodes.args(arg1)) { // if takes args, find next or display error
				try {
					arg2 = argsArray[2];
					arg2Out = arg2;
				} catch (ArrayIndexOutOfBoundsException arg2Error) {
					System.out.println("ERROR line " + ProcessFile.line + ": operand not found for: " + arg1);
					return "";
				}
			} else {
				arg2 = null; // if takes no args, null
				arg2Out = "";
			}
			ProcessFile.LINEARGS = new String[] { argsArray[0], arg1, arg2Out }; // for stdout
		}

		if (arg1.equalsIgnoreCase("RESW") || arg1.equalsIgnoreCase("RESB") || arg1.equalsIgnoreCase("BYTE")
				|| arg1.equalsIgnoreCase("WORD")) { // check for reservations
			// convert offset change, return nothing
			String operandValue = arg2;
			String zeroes; // word - 18 bytes = 9 zeroes
			StringBuilder data = new StringBuilder();

			if (arg1.equalsIgnoreCase("RESW")) {
				Integer size = Integer.parseInt(operandValue);
				zeroes = "000000"; // times # to reserve
				for (int i = 0; i < size; i++) {
					data.append(zeroes);
				}
				offsetChange = 3 * size; // change offset
				ProcessFile.OFFSET += offsetChange;
				return data.toString();
				
			} else if (arg1.equalsIgnoreCase("RESB")) {
				Integer size = Integer.parseInt(operandValue);
				zeroes = "00"; // times # to reserve
				for (int i = 0; i < size; i++) {
					data.append(zeroes);
				}
				offsetChange = 1 * size;
				ProcessFile.OFFSET += offsetChange;
				return data.toString();
				
			} else if (arg1.equalsIgnoreCase("BYTE")) {
				String[] dataRes = arg2.split("’|\\'");
				String reserve = dataRes[1];
				String out;
				offsetChange = 1;
				if (dataRes[0].equalsIgnoreCase("C")) {
					// offset twice as big if characters
					offsetChange *= reserve.length(); // characters twice as big
					out = convert.charInt(reserve);

				} else {
					out = reserve;
				}
				ProcessFile.OFFSET += offsetChange; // change offset
				return out;
				
			} else {
				// "word"
				int reserve = Integer.parseInt(arg2);
				ProcessFile.OFFSET += 3;
				String out = String.format("%6s", convert.byteBin(reserve)).replace(' ', '0');
				return out;

			}
		}

		if (arg1.equalsIgnoreCase("BASE")) {
			return ""; // ignore, set in firstpass
		}
		String[] operandArray = { arg1, arg2 };
		opcode = operands.processLine(operandArray); // process operands
		offsetChange = operands.getOffsetChange(arg1);
		// dynamically change zero pad when making string
		StringBuilder pad = new StringBuilder();
		pad.append("%"); // format "%/format/s this ensures that leading zeros are not dropped
		pad.append(offsetChange * 2); // get format * 2 (00)
		pad.append("s");
		String currentCode;
		if (opcode == 0) {
			ProcessFile.OFFSET += offsetChange;
			return "00";
		}
		try {
			currentCode = String.format(pad.toString(), convert.byteBin(opcode)).replace(' ', '0'); // generate code
		} catch (Exception code) {
			System.out.println("ERROR line " + ProcessFile.line + ": code format error: " + opcode);
			return "";
		}

		ProcessFile.OFFSET += offsetChange; // change for next line
		String output = currentCode;
		return output;

	}

	public void firstPass() {
		String instruction;
		int offsetChange;
		int offset = 0;

		while (inputFirst.hasNextLine()) {
			ProcessFile.line++;
			instruction = inputFirst.nextLine();
			char[] characters = instruction.toCharArray();

			if (characters.length <= 0 || instruction.trim().isEmpty()) {
				continue; // nothing in line continue to next line if next
			}

			String[] argsArray = instruction.trim().split("\\s+");

			String symbol;
			String arg1;
			String arg2;
			boolean startFound = false;

			if (characters[0] == '.' || argsArray[0].equals(".")) {
				continue;
			}
			if (characters[0] == ' ' || characters[0] == '\t' || characters[0] == '\r' || characters[0] == '\n') {
				arg1 = argsArray[0]; // no symbol in line
				if (!opcodes.args(arg1)) { // check if code takes argg
					offsetChange = opcodes.size(arg1.trim());
					offset += offsetChange;
					continue;
				}
				try {
					arg2 = argsArray[1]; // attempt to find arg
				} catch (Exception second) {
					System.out.println(
							"ERROR line " + ProcessFile.line + ": no argument found for " + arg1 + " (first pass)");
					continue;
				}

				if (arg1.equalsIgnoreCase("START") && !startFound) {
					try {
						offset = Integer.parseInt(arg2.trim(), 16);
					} catch (Exception noOffset) {
					}
					startFound = true;
					continue;
				}
			} else { // symbol
				symbol = argsArray[0]; // symbol in line
				arg1 = argsArray[1];

				boolean added = symbols.addSym(symbol, offset);
				if (!added) { // add symbol found (will be replaced if start)
					System.out.println("ERROR line " + ProcessFile.line + ": duplicate symbol.");
				}

				if (!opcodes.args(arg1)) { // check if code takes args
					offsetChange = opcodes.size(arg1.trim());
					offset += offsetChange;
					continue;
				}
				try {
					arg2 = argsArray[2]; // attempt to find args
				} catch (Exception second) {
					System.out.println("ERROR line " + ProcessFile.line + ": no argument found for " + arg1);
					continue;
				}

				if (arg1.equalsIgnoreCase("START") && !startFound) { // check if start and if no start previously recorded
					try {
						offset = Integer.parseInt(arg2.trim(), 16); // parse new offset
					} catch (Exception noOffset) {
						offset = 0; // if not integer, no change
					}
					boolean replaced = symbols.replace(symbol, offset); // replace symbol at start with new start offset

					if (!replaced) { // unable to change symbol in table
						System.out.println("ERROR line " + ProcessFile.line
								+ ": Attempt to change starting offset on START directive line failed");
					}
					startFound = true; // start found
					continue;
				}

			}

			if (arg1.equalsIgnoreCase("END")) {
				break; // done
			}

			if (arg1.equalsIgnoreCase("BASE")) {
				ProcessFile.BASE = arg2.trim(); // set base symbol
				continue;
			}

			if (arg1.equalsIgnoreCase("RESW")) {
				String operandValue = arg2; // change offset with reservation
				Integer size = Integer.parseInt(operandValue);
				offset += 3 * size;
				continue;
			} else if (arg1.equalsIgnoreCase("RESB")) {
				// change offset with reservation
				String operandValue = arg2;
				Integer size = Integer.parseInt(operandValue);
				offset += 1 * size;
				continue;
			} else if (arg1.equalsIgnoreCase("WORD")) {
				offset += 3; // change offset
				continue;
			} else if (arg1.equalsIgnoreCase("BYTE")) {
				String[] dataRes = arg2.split("’|\\'");
				String reserve = dataRes[1];
				if (dataRes[0].equalsIgnoreCase("C")) {
					offset += reserve.length(); // offset twice as big if characters
				} else {
					offset += 1;
				}
				continue;
			} else {
				offsetChange = opcodes.size(arg1.trim()); // get size
				offset += offsetChange; // change offset
			}
		}
	}

	public void standardOut(int lineNum, int offset, String[] args, String instruction) {
		if (this.startingOffset > 0) { // special case where display offset needs to be changed immediately
			offset = this.startingOffset; // happens once
			this.startingOffset = 0;
		}
		// formatting for standard out with left justified fields
		String offsetOut = String.format("%6s", convert.byteBin(offset)).replace(' ', '0');
		String lineOut = String.format("%03d\t", lineNum);
		String argsOut = String.format("%-10s%-10s%-15s", args[0], args[1], args[2]);
		String stdOut = String.format("%-4s%-10s%-30s%-10s", lineOut, offsetOut, argsOut, instruction);
		System.out.println(stdOut); // display line
	}
}

import java.util.HashMap;
import java.util.Map;

public class Operands {
	private final int n = 131072;	//n set for format three
	private final int i = 65536;	//i set for format three
	private final int x = 32768;	//x set for format three
	private final int e = 1048576; //always 4
	private SymbolTable symTab;		//symbol table passed in constructor
	private OpCodeTable codeLookup;	//codelookup
	private Offset offset = new Offset();	//new offset
	private final String[] regs = {"A","X", "L", "B", "S", "T", "F", "PC", "SW"};	//register table
	private final Integer[] codes = {0, 1, 2, 3, 4, 5, 6, 8, 9};	//register table values
	Map<String, Integer> regTab;
	
	Operands(SymbolTable currentsymTab) {
		this.symTab = currentsymTab;	//symbol table passed as argument to constructor
		codeLookup = new OpCodeTable();		//generic opcode table
		this.regTab = new HashMap<String, Integer>();	//build register table
		for (int i = 0;i<codes.length;i++) {
			regTab.put(regs[i], codes[i]);
		}
	}
	
	public int processLine(String[] allArgs) {
		int nOut = n;
		int iOut = i;
		int xOut = x;
		int additional = 0;	//computed values
		String opcode = allArgs[0];
		String operand = allArgs[1];

		if (operand != null && !codeLookup.args(opcode)) {
			System.out.println("ERROR line " + ProcessFile.line + ": symbol found after instruction that takes no symbols: " + allArgs[0] + " " + operand);
			//if argument needed but not found
		}
		
		if (operand == null || !codeLookup.args(allArgs[0])) {
			if (opcode.equalsIgnoreCase("RSUB")) {
				return codeLookup.getCode(opcode) + n + i;
				//special case
			}
			else {
				return codeLookup.getCode(opcode);
			}
		}
		
		if (allArgs.length < 2) {
			System.out.println("ERROR line " + ProcessFile.line + ": no operand found after " + allArgs[0]);
			return 0;
		}
		
		char[] operandChars = operand.toCharArray();

		int opCodeInt = codeLookup.getCode(opcode);	//check possible with '+' as well
		int opCodeSize = codeLookup.size(opcode);
				
		if (opCodeSize == 4) { //format 4
			nOut = n * 256;	//expand format to four
			iOut = i * 256;	//expand format ...
			xOut = x * 256;	//expand format ...
			additional += e;
			if (operand.endsWith(",X")) {	//if four and indexed
				operand = operand.substring(0, operand.length() - 2);
				additional += xOut ;
			}
			if (operandChars[0] == '#') {	//check for #
				additional += iOut;
				if (isInteger(operand.substring(1))) {	//check if Integer found
						additional += Integer.parseInt(operand.substring(1));	//get specified value after removing #
				}
				else {
					int targetOffset = symTab.getOffset(operand.substring(1));	//get offset of symbol
					additional += targetOffset;
				}
			}
			else if (operandChars[0] == '@') {
				additional += nOut;
				if (isInteger(operand.substring(1))) {	//check if integer found
					additional += Integer.parseInt(operand.substring(1));
				}
				else {
					int targetOffset = symTab.getOffset(operand.substring(1));	//else get offset
					
					additional += targetOffset;
				}
			}
			else {
				int targetOffset = symTab.getOffset(operand);	//get offset
				additional += targetOffset;
				additional += nOut;
				additional += iOut;
			}
			return opCodeInt + additional;	//done
		}
		else if (opCodeSize == 3) { //format 3
			if (operand.endsWith(",X")) { //check if indexed
				operand = operand.substring(0, operand.length() - 2);
				additional += xOut;
			}
			
			if (operandChars[0] == '#') {	//check for #
				additional += i;	
				if (isInteger(operand.substring(1))) {
					additional += Integer.parseInt(operand.substring(1));
				}
				else {
					int targetOffset = symTab.getOffset(operand.substring(1));
					int currentOffset = ProcessFile.OFFSET + opCodeSize; //next "offset"

					additional += this.offset.calcOffset(currentOffset, targetOffset, this.symTab, symTab.baseFound());
				}
			}
			else if (operandChars[0] == '@') {	//check for @
				additional += n;
				if (isInteger(operand.substring(1))) {
					additional += Integer.parseInt(operand.substring(1));
				}
				else {
					try {
						int targetOffset = symTab.getOffset(operand.substring(1));
						int currentOffset = ProcessFile.OFFSET + opCodeSize; //next "offset"
						additional += this.offset.calcOffset(currentOffset, targetOffset, this.symTab, symTab.baseFound());
					} catch (Exception operand1) {
						System.out.println("ERROR line " + ProcessFile.line + ": failed to find and calculate offset for symbol: " + operand);
					}
				}
			}
			else {
				int targetOffset;
				additional += nOut + iOut;
				try {
					targetOffset = symTab.getOffset(operand);
				} catch (Exception args) {
					System.out.println("ERROR line " + ProcessFile.line + ": failed to find and calculate offset for symbol: " + operand);
					return 0;
				}
				int currentOffset = ProcessFile.OFFSET + opCodeSize; //next "offset"
				additional += this.offset.calcOffset(currentOffset, targetOffset, this.symTab, symTab.baseFound());
			}
			
			return opCodeInt + additional;
		}
		else if (opCodeSize == 2) { //format 2
			//check for 1 or two registers (delimited with comma)
			if (opcode.equalsIgnoreCase("SHIFTL") || opcode.equalsIgnoreCase("SHIFTR")) {
				String[] argRegs  = operand.trim().split(",");
				additional = this.regTab.get(argRegs[0]);
				additional *= 16;
				if (Integer.parseInt(argRegs[1]) > 16) {
					System.out.println("ERROR line " + ProcessFile.line + ": numeric arg too big.");
					return 0;
				}
				additional += (Integer.parseInt(argRegs[1]) - 1);
				return opCodeInt + additional;
			}
			if (opcode.equalsIgnoreCase("SVC")) {
				//special case	
				try {
					additional += Integer.parseInt(operand) * 16;
					return opCodeInt + additional;
				} catch (Exception args) {
					System.out.println("ERROR line " + ProcessFile.line + ": numeric arg for SVC not found.");
					return 0;
				}
			}
			try {
				if (operand.contains(",")) {
					String[] registers  = operand.trim().split(",");
					int reg1;
					if (this.regTab.containsKey(registers[0])) {
						reg1 = this.regTab.get(registers[0]);
					}
					else {
						reg1 = Integer.parseInt(registers[0]);
					}
					reg1 *=16;
					
					int reg2;
					if (this.regTab.containsKey(registers[1])) {
						reg2 = this.regTab.get(registers[1]);
					}
					else {
						reg2 = Integer.parseInt(registers[1]);
					}
					additional += reg1 + reg2;
				}
				else {
					
					if (this.regTab.containsKey(operand.trim())) {
						additional += this.regTab.get(operand.trim()) * 16;
					}
					else {
						additional += Integer.parseInt(operand) * 16;
					}
				}
			} catch (Exception args) {
				System.out.println("ERROR line " + ProcessFile.line + ": register arguments: " + operand + " not found.");
				return 0;
			}
			return opCodeInt + additional;
		}
		else { //format 1
			return opCodeInt;	//just return code
		}			
	}

	public int getOffsetChange(String opcode) {
		return codeLookup.size(opcode);	//lookup offset change
	}
	
	public static boolean isInteger(String s) {
		//suppress exceptions to determine if integer
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}
}


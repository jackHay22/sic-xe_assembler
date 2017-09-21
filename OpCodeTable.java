import java.util.HashMap;
import java.util.Map;

public class OpCodeTable {
	private Map<String, CodeKey> optab;
	//private data sets used to initialize opcode lookup table
	private final String[] codes = {"add","addf","addr","and","clear","comp","compf","compr","div","divf","divr",
			"fix","float","hio","j","jeq","jgt","jlt","jsub","lda","ldb","ldch","ldf","ldl","lds","ldt","ldx",
			"lps", "mul", "mulf","mulr","norm","or","rd","rmo","rsub","shiftl","shiftr","sio","ssk","sta","stb",
			"stch", "stf","sti","stl","sts","stsw","stt","stx","sub","subf","subr","svc","td","tio","tix","tixr","wd"};
	private final 	Integer[] opcodes = {1572864, 5767168, 36864, 4194304, 46080, 2621440, 8912896, 40960, 
			2359296, 6553600, 39936, 196, 192, 244, 3932160, 3145728, 3407872, 3670016, 4718592, 0, 6815744, 5242880, 
			7340032, 524288, 7077888, 7602176, 262144, 13631488, 2097152, 6291456, 38912, 200, 4456448, 14155776, 
			44032, 4980736, 41984, 43008, 240, 15466496, 786432, 7864320, 5505024, 8388608, 13893632, 1310720, 
			8126464, 15204352, 8650752, 1048576, 1835008, 6029312, 37888, 45056,14680064, 248, 2883584, 47104, 14417920};
	private final Integer[] sizes = {3, 3, 2, 3, 2, 3, 3, 2, 3, 3, 2, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
			3, 3, 3, 2, 1, 3, 3, 2, 3, 2, 2, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3, 1, 3, 2, 3};
	
	OpCodeTable() {
		optab = new HashMap<String, CodeKey>();	//hashmap built with opcode string and codekey containing code, size, and operands boolean
		for (int i = 0;i<codes.length;i++) {
			if (codes[i].equals("fix") || codes[i].equals("float") || codes[i].equals("hio") || 
					codes[i].equals("norm") || codes[i].equals("rsub") || codes[i].equals("sio") || codes[i].equals("tio")) {
				//initialize new CodeKey with data
				optab.put(codes[i], new CodeKey(opcodes[i], sizes[i], false)); //these instructions don't take args
			}
			else {
				optab.put(codes[i], new CodeKey(opcodes[i], sizes[i], true)); 
				//else, current code key takes arguments and is constructed with true for codekey operand
			} 
		}
	}
	
	public int getCode(String opcode) { //any modifier before code will have been stripped
		char[] opchars = opcode.trim().toCharArray();
		int intcode = 0;
		CodeKey out;
		try {
			if (opchars[0] == '+'){		//preempt lookup with character check to expand opcode size and lookup the string without the plus
				out = optab.get(opcode.substring(1).trim().toLowerCase());
				intcode = out.getCode();
				intcode*=256;
			}
			else {	//get code from optab
				out = optab.get(opcode.trim().toLowerCase());		//returns a codekey object
				intcode = out.getCode();	//get value associated with codekey object
			}
			return intcode;
		} catch (Exception code) {
			System.out.println("ERROR line " + ProcessFile.line + ": opcode lookup failed on symbol: " + opcode);
		}
		return 0;
	}
	public boolean args(String opcode) { 
		CodeKey out;
		char[] opchars = opcode.trim().toCharArray();
		if (opcode.equalsIgnoreCase("RESW") || opcode.equalsIgnoreCase("RESB") || 
				opcode.equalsIgnoreCase("BYTE") || opcode.equalsIgnoreCase("WORD") || opcode.equalsIgnoreCase("BASE") || opcode.equalsIgnoreCase("START")) {
			//these instructions take operands but are not found in table
			return true;
		}
		if (opcode.equalsIgnoreCase("START")) {
			//start can take an operand
			return true;
		}
		else if (opcode.equalsIgnoreCase("END")) {
			//end can take an operand
			return true;
		}
		if (opchars[0] == '+') {
			//if plus, lookup chopped string
			out = optab.get(opcode.substring(1).trim().toLowerCase());
		}
		else {
			//else, lookup returns codekey
			out = optab.get(opcode.trim().toLowerCase());
		}
		boolean result = true;
		try {
			result = out.getOperand();	//getOperand on returned codekey
		} catch (Exception lookup) {
			System.out.println("ERROR line " + ProcessFile.line + ": opcode args check lookup failed on symbol: " + opcode);
		}
		return result;
	}
	public int size(String opcode) { //takes modifier
		char[] opchars = opcode.toCharArray();
		CodeKey out;
		try {
			if (opchars[0] == '+'){
				//error checking, if plus found on format 2/1, error
				if (optab.get(opcode.substring(1).trim().toLowerCase()).getSize() != 3) { //check if size
					System.out.println("ERROR line " + ProcessFile.line + ": extended format not available for " + opcode);
				}
				return 4;} 
			else {
				out = optab.get(opcode.trim().toLowerCase()); 
				return out.getSize();	//get size value in codekey
			}
		} catch (Exception size) {
			System.out.println("ERROR line " + ProcessFile.line + ": opcode lookup failed on symbol: " + opcode);
			return 0;
		}
	}	
}

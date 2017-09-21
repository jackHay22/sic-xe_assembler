
public class Offset {
	private final int b = 16384;	//format three b value
	private final int p = 8192;		//format three p value
	
	public int calcOffset(int current, int target, SymbolTable symtab, boolean baseFound) {
		int pOut = p;	//start by checking pc and preemptively set this output field
		int bOut = 0;	//preempt output field

		int disp = target - current;	//attempt calculation with pc relative
		if (disp < -2048 || disp > 2047) {	//pc relative not possible
			pOut = 0;	//pc not possible, remove output field
			bOut = b;	//set b field
			if (!baseFound) {	//check if base declared
				System.out.println("ERROR line " + ProcessFile.line + ": Base not declared");
				return 0;	//no base but pc not possible
			}
			disp = target - symtab.getOffset(ProcessFile.BASE);	//locate symbol offset refered to by base, compute
			if (disp < 0 || disp > 4095) {	//base not possible
				System.out.println("ERROR line " + ProcessFile.line + ": Could not perform pc relative or base relative addressing.");	
				System.out.println("TARGET: " + target + " CURRENT: " + current + " DISP: " + disp);	//show programmer offset values
				return 0;
			}
		}
		if (disp < 0) {
			disp = twosComplement(disp); //if negative, take twos complement
		}
		return disp + bOut + pOut;
	}
	public int twosComplement(int val) {
		val *= -1; //absolute value (only negative values passed)
		return 4095 - val + 1; //size 3 format addressing
	}
}

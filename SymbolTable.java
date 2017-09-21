import java.util.*;

public class SymbolTable {
	Map<String, Integer> symtab;
	Converter convert;
	
	SymbolTable() {
		symtab = new HashMap<String, Integer>(); //build new symbol table
		convert = new Converter();	//create new converter object	
	}
	
	public boolean addSym(String symbol, int offset) {
		if (symtab.containsValue(symbol)) {
			return false;	//if already in table, notify with boolean
		}
		else {
			symtab.put(symbol, offset);	//else, add
			return true;
		}	
	}
	public int getOffset(String symbol) {
		if (symtab.get(symbol) == null) {
			System.out.println("ERROR line " + ProcessFile.line + ": Bad symbol: " + symbol);
			//could not find offset
			return 0;
		}
		return symtab.get(symbol); //else, return offset
		
	}
	public boolean baseFound() {
		String baseLoc = ProcessFile.BASE; //checks if base declared
		if (baseLoc == null) {
			return false;	//no base found
		}
		else {
			return true;
		}
	}
	public void display() {
		//displays contents of table
		System.out.println("Computed symbol table: ");
		System.out.println("----------------------");
		for (String name: symtab.keySet()){
            String key = name.toString();
            int valuenum = symtab.get(name); 
            String value = String.format("%5s", convert.byteBin(valuenum)).replace(' ', '0');
            System.out.println(key + "\t offset: " + value);  
		}
		System.out.println("----------------------");
	}
	public boolean check(String symbol) {
		if (symtab.containsValue(symbol)) {
			//true if found
			return true;
		}
		else {
			//false if not found
			return false;
		}
	}
	public boolean replace(String symbol, int offset) {
		if (!symtab.containsKey(symbol)) {	//if the table does not contain the value, cannot replace
			return false;	//failed to replace
		}
		else {
			symtab.replace(symbol, offset);
			return true;	//replaced
		}
	}
	
}

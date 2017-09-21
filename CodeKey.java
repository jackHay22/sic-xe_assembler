public class CodeKey {
	//this class is used to build a hashmap in OpCodeTable.java that supports 3 values: code, string, size
	private int opcode;
	private int size;
	private boolean operand;
	public CodeKey(int opcode, int size, boolean operand) {
	    this.opcode = opcode;	//create data containing object to be used as opcode table entry
	    this.size = size;
	    this.operand = operand;
	}
	public int getCode() {	//get code on private member
		return this.opcode;
	}
	public int getSize() {	//get size on private member
		return this.size;
	}
	public boolean getOperand() {	//get operand on private member (returns true if operation takes operands)
		//if false, no operands
		return this.operand;
	}
	@Override
	public int hashCode() {		//mapping
		final int prime = 31;
		int result = 1;
		result = prime * result + opcode;
		return result;
	}
	@Override
	public boolean equals(Object obj) {		//override to compare objects
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeKey other = (CodeKey) obj;
		if (opcode != other.opcode)
			return false;
		if (size != other.size)
			return false;
		return true;
	}
}
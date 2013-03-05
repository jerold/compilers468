
// implements languages based on the micro-pascal virtual machine described at
// http://www.cs.montana.edu/~patrick.berens/micro_vm/newMicroMachineDefinitions.pdf

public interface Compiler {
	
	// Back-end functions
	
	void openFile(String output);
	void writeLabel();
	void write(String code);
	
	// Halt Instruction
	
	/**
	 * Terminates program execution.
	 */
	void halt();
	
	// I/O instructions
	
	/**
	 * Read an integer value into dst from STDIN
	 */
	void readInt();
	
	/**
	 * Read a float value into dst from STDIN
	 */
	void readFloat();
	
	/**
	 * Write the value in src to STDOUT
	 */
	void write();
	
	/**
	 * Performs POP A WRT A
	 */
	void writeStack();
	
	/**
	 * Write the value in src followed by a newline to STDOUT
	 */
	void writeLine();
	
	/**
	 * Performs POP A WRTLN A
	 */
	void writeLineStack();
	
	// MEMORY instructions
	
	/**
	 * Performs dst <-- src
	 */
	void move();
	
	//  - INTEGER
	
	/**
	 * Performs dst <-- -src
	 */
	void negate();
	
	/**
	 * Performs dst <-- src1 + src2
	 */
	void add();
	
	/**
	 * Performs dst <-- src1 - src2
	 */
	void subtract();
	
	/**
	 * Performs dst <-- src1 x src2
	 */
	void multiply();
	
	/**
	 * Performs dst <-- src1 / src2
	 */
	void divide();
	
	/**
	 * Performs dst <-- src1 % src2
	 */
	void modulus();
	
	//  - FLOAT
	
	/**
	 * Performs dst <-- -src
	 */
	void negateFloat();
	
	/**
	 * Performs dst <-- src1 + src2
	 */
	void addFloat();
	
	/**
	 * Performs dst <-- src1 - src2
	 */
	void subtractFloat();
	
	/**
	 * Performs dst <-- src1 x src2
	 */
	void multiplyFloat();
	
	/**
	 * Performs dst <-- src1 / src2
	 */
	void divideFloat();
	
	//  - CASTING
	
	/**
	 * Performs dst <-- (int)src
	 */
	void castInteger();
	
	/**
	 * Performs dst <-- (float)src
	 */
	void castFloat(); 
	
	

}

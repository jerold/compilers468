
// implements languages based on the micro-pascal virtual machine described at
// http://www.cs.montana.edu/~patrick.berens/micro_vm/newMicroMachineDefinitions.pdf

public interface Compiler {
	
	// Back-end functions
	
	void openFile(String output);
	void writeFile();
	void turnOff();
	
	// Halt Instruction
	
	/**
	 * Terminates program execution.
	 */
	void halt();
	
	// I/O instructions
	
	/**
	 * Read an integer value into dst from STDIN
	 */
	void readInt(String dst);
	
	/**
	 * Read a float value into dst from STDIN
	 */
	void readFloat(String dst);
	
	/**
	 * Write the value in src to STDOUT
	 */
	void write(String src);
	
	/**
	 * Performs POP A WRT A
	 */
	void writeStack();
	
	/**
	 * Write the value in src followed by a newline to STDOUT
	 */
	void writeLine(String src);
	
	/**
	 * Performs POP A WRTLN A
	 */
	void writeLineStack();
	
	// MEMORY instructions
	
	/**
	 * Performs dst <-- src
	 */
	void move(String src, String dst);
	
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
	void castInteger(String src, String dst);
	
	/**
	 * Performs dst <-- (float)src
	 */
	void castFloat(String src, String dst); 
	
	// STACK instructions
	
	/**
	 * Push src on top of the stack
	 */
	void push(String src);
	
	/**
	 * Pop the top of the stack into dst 
	 */
	void pop(String dst);
	
	//  - INTEGER
	
	/**
	 * Performs POP A PUSH -A
	 */
	void negateStack();
	
	/**
	 * Performs POP A POP B PUSH B + A
	 */
	void addStack();
	
	/**
	 * Performs POP A POP B PUSH B - A
	 */
	void subtractStack();
	
	/**
	 * Performs POP A POP B PUSH B x A
	 */
	void multiplyStack();
	
	/**
	 * Performs POP A POP B PUSH B / A
	 */
	void divideStack();
	
	/**
	 * Performs POP A POP B PUSH B % A
	 */
	void modulusStack();
	
	//  - FLOAT
	
	/**
	 * Performs POP A PUSH -A
	 */
	void negateStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B + A
	 */
	void addStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B - A
	 */
	void subtractStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B x A
	 */
	void multiplyStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B / A
	 */
	void divideStackFloat();
	
	//  - CASTING
	
	/**
	 * Performs POP A PUSH (int)A
	 */
	void castStackInteger();
	
	/**
	 * Performs POP A PUSH (float)A
	 */
	void castStackFloat(); 
	
	// LABEL instructions
	
	/**
	 * Drop label n at current position
	 * 
	 * @return	The label that was generated.
	 */
	String label();
	
	/**
	 * Manually drop label
	 * 
	 * @param label	The label that should be entered.
	 */
	void label(String l);
	
	/**
	 * Skip label for later use.
	 * 
	 * @return	The label that was skipped.
	 */
	String skipLabel();
	
	/**
	 * Get the string that will be injected when the label() function is called.
	 * 
	 * @param index	The number of times in the future label will be called.
	 * @return		The label that will be produced by the label calls.
	 */
	String getLabel(int index);
	
	// LOGICAL OPERATOR instructions - defined for Booleans
	
	/**
	 * Performs POP A POP B PUSH B and A
	 */
	void andStack();
	
	/**
	 * Performs POP A POP B PUSH B or A
	 */
	void orStack();
	
	/**
	 * Performs POP A PUSH not A
	 */
	void notStack();
	
	// COMPARE instructions - defined for Integers and Floats
	
	//  - INTEGER
	
	/**
	 * Performs POP A POP B PUSH B = A
	 */
	void compareEqualStack();
	
	/**
	 * Performs POP A POP B PUSH B >= A
	 */
	void compareGreaterEqualStack();
	
	/**
	 * Performs POP A POP B PUSH B > A
	 */
	void compareGreaterStack();
	
	/**
	 * Performs POP A POP B PUSH B <= A
	 */
	void compareLessEqualStack();
	
	/**
	 * Performs POP A POP B PUSH B < A
	 */
	void compareLessStack();
	
	/**
	 * Performs POP A POP B PUSH B <> A
	 */
	void compareNotEqualStack();
	 
	//  - INTEGER
		
	/**
	 * Performs POP A POP B PUSH B = A
	 */
	void compareEqualStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B >= A
	 */
	void compareGreaterEqualStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B > A
	 */
	void compareGreaterStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B <= A
	 */
	void compareLessEqualStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B < A
	 */
	void compareLessStackFloat();
	
	/**
	 * Performs POP A POP B PUSH B <> A
	 */
	void compareNotEqualStackFloat();
	
	// STACK BRANCH instructions - defined for Booleans
	
	/**
	 * Performs POP A BEQ A #1 Ln
	 */
	void branchTrueStack(String l);
	
	/**
	 * Performs POP A BEQ A #0 Ln
	 */
	void branchFalseStack(String l);
			
	// BRANCH instructions - defined for Integers and Floats
	
	/**
	 * Branch to label l
	 */
	void branch(String l);
			
	//  - INTEGER
	
	/**
	 * Branch to label n if src1 = src2
	 */
	void branchEqual(int n);
	
	/**
	 * Branch to label n if src1 >= src2
	 */
	void branchGreaterEqual(int n);
	
	/**
	 * Branch to label n if src1 > src2
	 */
	void branchGreater(int n);
	
	/**
	 * Branch to label n if src1 <= src2
	 */
	void branchLessEqual(int n);
	
	/**
	 * Branch to label n if src1 < src2
	 */
	void branchLess(int n);
	
	/**
	 * Branch to label n if src1 <> src2
	 */
	void branchNotEqual(int n);

	//  - FLOAT
	
	/**
	 * Branch to label n if src1 = src2
	 */
	void branchEqualFloat(int n);
	
	/**
	 * Branch to label n if src1 >= src2
	 */
	void branchGreaterEqualFloat(int n);
	
	/**
	 * Branch to label n if src1 > src2
	 */
	void branchGreaterFloat(int n);
	
	/**
	 * Branch to label n if src1 <= src2
	 */
	void branchLessEqualFloat(int n);
	
	/**
	 * Branch to label n if src1 < src2
	 */
	void branchLessFloat(int n);
	
	/**
	 * Branch to label n if src1 <> src2
	 */
	void branchNotEqualFloat(int n);
	
	// SUBROUTINE instructions
	
	/**
	 * Performs PUSH PC BR Ln
	 */
	void call(String l);
	
	/**
	 * Performs POP PC
	 */
	void returnCall();
	
	/**
	 * Prints out stack addresses and values – Doesn’t affect state of machine.
	 */
	void printStack();
	
	/**
	 * Prints out registers – Doesn’t affect state of machine.
	 */
	void printRegisters();
	
	/**
	 * Retrieves the line of assembly the compiler is on.
	 * 
	 * @return	The last line to be printed
	 */
	int getLine();
	
	/**
	 * Injects the last line of assembly written into another line
	 * 
	 * @param line	The line that the code should be relocated to (must be less than the current)
	 */
	void injectLast(int line);

}

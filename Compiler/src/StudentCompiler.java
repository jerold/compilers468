import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class StudentCompiler implements Compiler {

	private String root;	// the root directory to compile to
	private String output;  // filename to output to
	private int label;		// Incrementor for label
	FileWriter fstream;
	BufferedWriter out;
	
	public StudentCompiler() {
		label = 0;
		root = "src/bin/";
		output = root+"out.txt";
	}
	
	public void openFile(String output) {
		this.output = root+output;
		try {
			fstream = new FileWriter(output);
		} catch (IOException e) {
			System.out.println("Unable to open output file");
		}
		out = new BufferedWriter(fstream);
	}
	
	public void writeLabel() {
		this.write("label"+label++);
	}
	
	public void write(String code) {
		try {
			out.write(code);
		} catch (IOException e) {
			System.out.println("Unable to write to the output file");
		}
	}
	
	// HALT instruction
	
	public void halt() {
		write("HLT");
	}
	
	// I/O instructions
	
	public void readInt() {
		write("RD dst");
	}
	
	public void readFloat() {
		write("RDF dst");
	}
	
	public void write() {
		write("WRT src");
	}
	
	public void writeStack() {
		write("WRTS");
	}
	
	public void writeLine() {
		write("WRTLN src");
	}
	
	public void writeLineStack() {
		write("WRTLNS");
	}
	
	// MEMORY instructions
	
	

}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TestSuite {
	
	private Scanner scanner;
	private Parser parser;
	private Compiler compiler;
	private ServerUpload su;
	private String directory;
	private FileReader fr;
	private BufferedReader br;
	private ArrayList<File> sourceFiles = new ArrayList<File>();
	private ArrayList<File> outputFiles = new ArrayList<File>();
	
	public TestSuite(Compiler compiler, String directory) {
		this.compiler = compiler;
		this.directory = directory;
		this.scanner = new Scanner();
		this.su = new ServerUpload();
		this.su.stripMessage();
		this.su.noShow();
	}
	
	public void run() {
		readFiles();
		matchFiles();
		for (int i=0; i<sourceFiles.size(); i++) {
			File fin = sourceFiles.get(i);
			System.out.println("running "+fin.getName());
			System.out.println("######################################################");
			scanner.openFile(fin.getPath());
			parser = new Parser(scanner,compiler);
			parser.run();
			int code = execute();
			if (code>=0) {
				String output = this.su.getOutput();
				File fout = getFile(fin.getName(),outputFiles);
				int line = compare(output,fout);
				// found a discrepancy, print it out
				if (line>=0) {
					System.out.println("FOUND DISCREPANCY ON LINE "+line);
				} else {
					System.out.println("correct");
				}
			} else if (code==1) {
				System.out.println("\nCOMPILE ERROR, PLEASE CORRECT");
				break;
			}
			System.out.println("");
		}
	}
	
	private int compare(String output, File file) {
		int linenumber = -1;
		String line = null;
		String data[] = output.split("\n");
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) { }
		br = new BufferedReader(fr);
		String s = "";
		try {
			int i = -1;
			while (br.ready()) {
				i++;
				line = br.readLine();
				if (data.length-1>i || !line.equals(data[i])) {
					System.out.println(line+" vs "+data[i]);
					// discrepancy
					linenumber = i;
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading output file during comparison.");
		}
		return linenumber;
	}
	
	private int execute() {
		int code = 1;
		if (compiler.checkOK()) {
			// connection error
			if (!su.go()) {
				code = 0;
				System.out.println("Unable to connect to server.");
			}
		} else {
			code = -1;
			System.out.println("File failed to compile.");
		}
		return code;
	}
	
	private void printList(ArrayList<File> list) {
		for (int i=0; i<list.size(); i++) {
			System.out.println(list.get(i).getName());
		}
	}
	
	private void readFiles() {
		File src = new File(directory+"/src");
		File output = new File(directory+"/output");
		// loop through src files
		for (final File file : src.listFiles()) {
			if (file.isFile() && file.getName().substring(file.getName().length()-4).equals(".txt")) {
				sourceFiles.add(file);
	        }
	    }
		// loop through output files
		for (final File file : output.listFiles()) {
			if (file.isFile() && file.getName().substring(file.getName().length()-4).equals(".txt")) {
				outputFiles.add(file);
	        }
	    }
	}
	
	// Removes src files without corresponding output files
	private void matchFiles() {
		for (int i=0; i<sourceFiles.size(); i++) {
			if (!inList(sourceFiles.get(i).getName(), outputFiles)) {
				sourceFiles.remove(i);
			}
		}
	}
	
	private boolean inList(String filename, ArrayList<File> list) {
		return !(getFile(filename,list)==null);
	}
	
	private File getFile(String filename, ArrayList<File> list) {
		File result = null;
		for (int i=0; i<list.size(); i++) {
			File f = list.get(i);
			if (f.getName().equals(filename)) {
				result = f;
				break;
			}
		}
		return result;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}

}

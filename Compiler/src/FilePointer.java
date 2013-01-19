/**
 * FilePointer preserves the actual fileReader and location, and
 * also manages a buffer (char[]) which allows us to go back in
 * the file's character sequence as needed
 * @author Jerold
 *
 */
import java.util.*;
import java.io.*;

public class FilePointer {
	static String fileName;
	static int fileLine;
	static int bufferColumn;
	static int peekColumn;
	static boolean bufferLoaded;
	
	public static BufferedReader reader;
	public char[] buffer;
	
	public static void FilePointer(String fileIn) {
		fileName = fileIn;
		
		fileLine = 0;
		bufferColumn = 0;
		peekColumn = 0;
		bufferLoaded = false;
	}
	
	public void loadBuffer() {
		try {
			String line = null;
			int currentLineNo = 0;
			reader = new BufferedReader (new FileReader(fileName));
			
			// Read up to the line we want
			while(currentLineNo<fileLine) {
				if (reader.readLine()==null) {
					// oops, early end of file
					throw new IOException("File too small");
				}
				currentLineNo++;
			}
			
			// Read in Current line and populate buffer
			line = reader.readLine();
			buffer = line.toCharArray();
			bufferColumn = 0;
			peekColumn = 0;
			bufferLoaded = true;
		} catch (IOException ex) {
			System.out.println("Problem reading file.\n" + ex.getMessage());
		} finally {
			try { if (reader!=null) reader.close(); } catch(IOException ignore) {}
		}
	}
	
	// getNext will grab the next character in the file, if we are at the end of the line
	// this method will take us to the next line and fetch us the 1st character from there
	public char getNext() {
		if (!bufferLoaded)
			loadBuffer();
		bufferColumn++;
		peekColumn++;
		if (bufferColumn >= buffer.length) {
			fileLine++;
			loadBuffer();
		}
		return buffer[bufferColumn];
	}
	
	// Liberty taken in this method is that we will never need to peek on a new line
	// This is based on the assumption that tokens will not be split between lines
	// if we peek past the end of the line we return a newline which all tokens should
	// terminate on.  It should never come to this because all tokenizers should see the
	// NewLine character as a non-valid token component... just saying. :P
	public char peekNext() {
		peekColumn++;
		if (peekColumn >= buffer.length) {
			return '\n';
		}
		return buffer[peekColumn];
	}
	
	// Only reason to use backUp is if we've gone to far trying to fetch a greedy token
	// (smaller token already found, but we're going for paydirt).  Assume we'll never need
	// to go back to a previous line because of the tokens don't extend to different lines thing
	public void backUp(int distance) {
		bufferColumn-=distance;
		peekColumn-=distance;
	}
}

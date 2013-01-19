/**
 * FilePointer preserves the actual fileReader and location, and
 * also manages a buffer (char[]) which allows us to go back in
 * the file's character sequence as needed
 * @author Jerold
 *
 */
import java.io.*;

public class FilePointer {
	static String fileName;
	static int fileLine;
	static int bufferColumn;
	static int peekColumn;
	static boolean bufferLoaded;
	static boolean atEndOfFile;
	
	public static BufferedReader reader;
	public char[] buffer;
	
	public FilePointer(String fileIn) {
		fileName = fileIn;
		
		fileLine = 0;
		bufferColumn = 0;
		setPeekToBufferColumn();
		bufferLoaded = false;
		atEndOfFile = false;
	}
	
	// Load buffer will be called on a line by line basis.
	public void loadBuffer() {
		bufferLoaded = false;
		
		// System.out.println("\nLoading Buffer: Line[" + fileLine + "] of file[" + fileName + "]");
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
			
			if (line == null) {
				atEndOfFile = true;
			} else {
				buffer = line.toCharArray();
				bufferColumn = 0;
				setPeekToBufferColumn();
				bufferLoaded = true;
			}
		} catch (IOException ex) {
			System.out.println("Problem reading file.\n" + ex.getMessage());
		} finally {
			try { if (reader!=null) reader.close(); } catch(IOException ignore) {}
		}
	}
	
	// No idea what this does.  Too heavy
	public boolean endOfFile() {
		return atEndOfFile;
	}
	
	// getNext will grab the next character in the file, if we are at the end of the line
	// this method will take us to the next line and fetch us the 1st character from there
	public char getNext() {
		if (!bufferLoaded)
			loadBuffer();
		if (atEndOfFile)
			return '\u0000';
		
		char retChar = buffer[bufferColumn];
		
		bufferColumn++;
		setPeekToBufferColumn();
		if (bufferColumn >= buffer.length) {
			// end of current line, buffer next line
			fileLine++;
			loadBuffer();
		}
		
		return retChar;
	}
	
	// Liberty taken in this method is that we will never need to peek on a new line
	// This is based on the assumption that tokens will not be split between lines
	// if we peek past the end of the line we return '\u0000' which all tokens should
	// terminate on.  It should never come to this because all tokenizers should see the
	// NewLine character as a non-valid token component... just saying. :P
	public char peekNext() {
		if (!bufferLoaded)
			loadBuffer();
		if (peekColumn >= buffer.length) {
			return '\u0000';
		}
		return buffer[peekColumn++];
	}
	
	public void setPeekToBufferColumn() {
		peekColumn = bufferColumn;
	}
	
	// Only reason to use backUp is if we've gone to far trying to fetch a greedy token
	// (smaller token already found, but we're going for paydirt).  Assume we'll never need
	// to go back to a previous line because of the tokens don't extend to different lines thing
	public void backUp(int distance) {
		bufferColumn-=distance;
		bufferColumn = Math.max(bufferColumn, 0);
		setPeekToBufferColumn();
	}
	
	public void skipWhiteSpace() {
		if (endOfFile())
			return;
		char nextChar = peekNext();
		if (nextChar ==  '\u0000' || nextChar ==  ' ' || nextChar ==  '\n' || nextChar ==  '\t') {
			getNext();
			skipWhiteSpace();
		} 
		// setPeekToBufferColumn();
		// nextChar = peekNext();
		// System.out.println("w["+ nextChar + "]");
		setPeekToBufferColumn();
		return;
	}
	
	// Returns current Line in File
	public int getLineNumber() {
		return fileLine;
	}
	
	// Returns current column of current line in File
	public int getColumnNumber() {
		return bufferColumn;
	}
}

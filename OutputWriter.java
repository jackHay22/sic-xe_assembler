import java.io.*;
import javax.xml.bind.DatatypeConverter;

public class OutputWriter {

	private OutputStream writer;
	
	public void open(String filename) {
		try {
			writer = new FileOutputStream(filename + ".exe"); }	//attempt to create new exe with filename
		catch (Exception noFile) {
			System.out.println("Error: Could not write to output file: " + filename + ".exe");
		}
	}
	public void writeLine(String line) {
		byte[] bytes;
		if (line.length() == 0) {
			return;	//if nothing to write
		}
		try {
			bytes = toByteArray(line);	//attempt string conversion
		} catch (Exception convert) {
			System.out.println("ERROR: Write Error: could not convert " + line + " to binary and write to file");
			return;
		}
		try {
			writer.write(bytes);	//attempt to write byte array to file
		} catch (IOException write) {
			System.out.println("ERROR: Write Error: could not write " + line + " to file");
		}
	}
	public void close() {
		try {
			writer.close();	//close file
		} catch (IOException write) {
			System.out.println("ERROR: Write Error: could not close file");
		}
	}
	public void set() {
		try {
			writer.flush();	//flush read buffer to prepare for next line
		} catch (IOException write) {
			System.out.println("ERROR: Write Error: could not flush file stream");
		}
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);	//return byte array that maintains zeros on string
	}
}

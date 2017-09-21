public class Assemble {

	public static void main(String[] args) {
		String file;
		try {
			file = args[0];	//checks for args in cmd tail
		} catch (Exception none1) {
			System.out.println("ERROR: File not found in command tail");
			System.out.println("Use: \'-info\' or \'-encoding\' or \'<filename>\'");
			return;
		}
		
		if (file.equalsIgnoreCase("-info")) {
			System.out.println("\n\tSIC/XE ASSEMBLER VERSION 1.2, Jack Hay 2016. \n");
			//display assembler info
		}
		else if (file.equalsIgnoreCase("-encoding")) {	//if encoding type specified
			String filename1;
			String encoding;
			try {
				encoding = args[1];
				filename1 = args[2];
				//attempt to change scanner encoding
				
			} catch (Exception fileArg) {
				System.out.println("ERROR: Filename and encoding type not specified with encoding directive");
				System.out.println("Use: -encoding <type> <filename> \t (where type is UTF-8, UTF-16, etc...)");
				return;
			}
			ProcessFile newFile = new ProcessFile(encoding.trim()); //specify command line encoding type for processfile
			//run file with specified encoding
			newFile.runFile(filename1);
 
		}
		else {
			ProcessFile newFile;
			for (String filename : args) { //for all filenames in cmd tail
				newFile = new ProcessFile("UTF-8");	//defaults with UTF-8
				newFile.runFile(filename);	//execute on file
			}
		}
	}
}

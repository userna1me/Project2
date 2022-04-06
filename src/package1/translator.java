package package1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class translator {
	
	public static void main(String args[]) {
		
		// read input file
		// translate it
		// produce output file
		
		List<String> fileContents = readFile(args[0]); // assuming args[0] is input filename
		
		
	}
	
	private static List<String> readFile(String filename) {
		List<String> fileContents = new ArrayList<>();
		try {
			
			File f = new File(filename);
			Scanner s = new Scanner(f);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				fileContents.add(line);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return fileContents;
	}
	
}

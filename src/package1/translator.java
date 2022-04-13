package package1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class translator {
	private static Pattern string = Pattern.compile("^.+$");
	// private static Pattern cha = Pattern.compile("^.$"); // any characters
	// private static Pattern var_assign = Pattern.compile("^var (.+) is (.+)|(.+) is (.+)$");
	private static Pattern math_symbol = Pattern.compile("^+|-|\\*|/|%$");
	private static Pattern intVal = Pattern.compile("^\\d+$");
	private static Pattern real = Pattern.compile("^(\\d+).(\\d+)$");
	private static Pattern bool = Pattern.compile("^true|false$");
	
	/* <nested_expr>
	 * <print>
	 * <var>
	 * <bool_expr>
	 * <bool_expr1>
	 * <bool_base>
	 * <math_expr>
	 * <math_expr1>
	 * <neg>
	 * <math_base>
	 * <var_assign>
	 * <var_list>
	 * <val_list>
	 * <val>
	 * <if_stat>
	 * <loop>
	 * <num>
	 */
	
	private static int nested = 0;
	
	public static void main(String args[]) {
		// read input file, assuming args[0] is input filename
		if (args[0] != null) {
			ArrayList<String> fileContents = readFile(args[0]);
			
			// translate it
			//ArrayList<String> javaCode = compile(fileContents);
			
			// produce output file
			
			
		// interactive system? 
		} else {
			Scanner input = new Scanner(System.in);
			String cmd = input.nextLine();
			while (!cmd.equals("exit")) {
				parse(cmd);
				
				cmd = input.nextLine();
			}
		}
	}
	
	private static boolean parse(String line) {
		int match = expr(line);
		
		
		
		return false;
	}
	
	private static ArrayList<String> compile(ArrayList<String> codes) {
		ArrayList<String> javaCode = new ArrayList<String>();
		
		
		return javaCode;
	}
	
	private static int expr(String line) {
		boolean match = false;
		if (print(line)) return 1;
		else if (varAssign(line)) return 2;
		else if (loop(line)) return 3;
		else if (nestedExpr(line)) return 4;		
		return 0;
	}
	
	private static boolean nestedExpr(String line) {
		for (int i = 0; i < nested; i++) {
			if (line.charAt(i) != '\t') {
				if (i == nested - 1) {
					nested--;
					break;
				} else return false;
			}
		}
		if (expr(line.substring(nested)) == 0) return false;
		return true;
	}
	
	private static boolean print(String line) {
		
		
		return false;
	}
	
	private static boolean string(String line) {
		
		
		return false;
	}
	
	private static boolean var(String line) {
		
		
		return false;
	}
	
	private static boolean boolExpr(String line) {
		
		
		return false;
	}
	
	private static boolean boolExpr1(String line) {
		
		
		return false;
	}
	
	private static boolean boolBase(String line) {
		
		
		return false;
	}
	
	private static boolean mathExpr(String line) {
		
		
		return false;
	}
	
	private static boolean mathExpr1(String line) {
		
		
		return false;
	}
	
	private static boolean neg(String line) {
		
		
		return false;
	}
	
	private static boolean mathBase(String line) {
		
		
		return false;
	}
	
	private static boolean varAssign(String line) {
		
		
		return false;
	}
	
	private static boolean varList(String line) {
		
		
		return false;
	}
	
	private static boolean valList(String line) {
		
		
		return false;
	}
	
	private static boolean val(String line) {
		
		
		return false;
	}
	
	private static boolean ifStat(String line) {
		
		
		return false;
	}
	
	private static boolean loop(String line) {
		
		
		return false;
	}
	
	/***
	 * Given the filepath, reads a file
	 * and returns a list of the lines from the file.
	 */
	private static ArrayList<String> readFile(String filename) {
		ArrayList<String> fileContents = new ArrayList<>();
		try {
			File f = new File(filename);
			Scanner s = new Scanner(f);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				fileContents.add(line);
				
				//System.out.println(line);
				//System.out.println(line.charAt(0) == '\t');
				
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fileContents;
	}
	
}

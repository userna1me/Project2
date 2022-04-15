package package1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class translator {
	private static Pattern math_symbol = Pattern.compile("^+|-|\\*|/|%$");
	
	private static HashMap<String, Object> globalVariables;
	
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
			ArrayList<String> javaCode = compile(fileContents);
			
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
		int match = expr(line, globalVariables);
		
		
		
		return false;
	}
	
	private static ArrayList<String> compile(ArrayList<String> codes) {
		ArrayList<String> javaCode = new ArrayList<String>();
		
		for (int i = 0; i<codes.size(); i++) {
			if (codes.get(i).trim().charAt(0) != '#') 
				expr(codes.get(i), globalVariables);
		}
		
		return javaCode;
	}
	
	private static int expr(String line, HashMap<String, Object> variables) {
		String[] result = print(line, variables);
		if (result[0] != null) {
			System.out.println("=============");
			System.out.println(result[0]);
			System.out.println("-------------");
			System.out.print(result[1]);
			System.out.println("=============");
		}
		//if (print(line)) return 1;
		//else if (varAssign(line)) return 2;
		//else if (loop(line)) return 3;
		//else if (nestedExpr(line)) return 4;		
		return 0;
	}
	
	private static boolean nestedExpr(String line, HashMap<String, Object> variables) {
		for (int i = 0; i < nested; i++) {
			if (line.charAt(i) != '\t') {
				if (i == nested - 1) {
					nested--;
					break;
				} else return false;
			}
		}
		if (expr(line.substring(nested), variables) == 0) return false;
		return true;
	}
	
	private static void checkVariable(String var, HashMap<String, Object> variables) {
		if (variables.containsKey(var)) {
			Object val = variables.get(var);
			if (val == null) {
				System.err.println("Error: "+var+" doesn't have a value");
				//System.exit(1);
			}
		} else {
			System.err.println("Error: "+var+" isn't declared");
			//System.exit(1);
		}
	}
	
	/**
	 * <print>
	 */
	private static String[] print(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (line.substring(0, 6).equals("print ")) {
			line = line.trim();
			String expr = line.substring(6).trim();
			int len = expr.length();
			
			// "<string>"
			if (expr.charAt(0) == '"' && expr.charAt(len-1) == '"') {
				// in this case it can be anything, no further matching needed
				// TODO might need to check for slashes
				// TODO error handling
				javaCode = "System.out.println(" + expr;
				javaCode += ");";
				match = "<print>: " + line + "\n";
				match += "<string>: " + expr.substring(1, len-1) + "\n";
				
			} else {
				// <var>
				String[] temp = var(expr);
				if (temp[0] != null) {
					if (temp[1].contains("<string>")) 
						checkVariable(expr, variables);
					javaCode = "System.out.println(" + temp[0] +");";
					match = "<print>: " + line + "\n";
					match += temp[1];
				}
				
				// <bool_expr>
				String[] boolTemp = boolExpr(expr, variables);
				if (boolTemp[0] != null) {
					javaCode = "System.out.println(" + boolTemp[0] + ");";
					match = "<print>: " + line + "\n";
					match += boolTemp[1];					
				}
				
				// <math_expr>
				String[] mathTemp = mathExpr(expr, variables);
			}
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] boolExpr(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		// TODO Error handling, integer.. 
		
		if (line.contains(" or ")) {
			String[] temp = line.split(" or ", 2);
			String[] t1 = boolExpr(temp[0], variables);
			String[] t2 = boolExpr1(temp[1], variables);
			javaCode = "(" + t1[0] + ") || (" + t2[0] + ")";
			match = "<bool_expr>: " + line + "\n";
			match += t1[1];
			match += t2[1];
		} else {
			String[] t = boolExpr1(line, variables);
			javaCode = t[0];
			match = "<bool_expr>: " + line + "\n";
			match += t[1];
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] boolExpr1(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (line.contains(" and ")) {
			String[] temp = line.split(" and ", 2);
			String[] t1 = boolExpr1(temp[0], variables);
			String[] t2 = boolExpr2(temp[1], variables);
			javaCode = "(" + t1[0] + ") && (" + t2[0] + ")";
			match = "<bool_expr1>: " + line + "\n";
			match += t1[1];
			match += t2[1];
		} else {
			String[] t = boolExpr2(line, variables);
			javaCode = t[0];
			match = "<bool_expr1>: " + line + "\n";
			match += t[1];
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] boolExpr2(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		String op = "";
		String[] temp = new String[2];
		if (line.contains(" != ")) {
			op = " != ";
			temp = line.split(" != ", 2);
		} else if (line.contains(" == ")) {
			op = " == ";
			temp = line.split(" == ", 2);
		} else if (line.contains(" < ")) {
			op = " < ";
			temp = line.split(" < ", 2);
		} else if (line.contains(" > ")) {
			op = " > ";
			temp = line.split(" > ", 2);
		} else if (line.contains(" <= ")) {
			op = " <= ";
			temp = line.split(" <= ", 2);
		} else if (line.contains(" >= ")) {
			op = " >= ";
			temp = line.split(" >= ", 2);
		} else {
			String[] t = boolBase(line, variables);
			javaCode = t[0];
			match = "<bool_expr2>: " + line + "\n";
			match += t[1];
			
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		String[] t1 = boolExpr2(temp[0], variables);
		String[] t2 = boolBase(temp[1], variables);
		javaCode = "(" + t1[0] + ")" + op + "(" + t2[0] + ")";
		match = "<bool_expr2>: "+line+"\n";
		match += t1[1];
		match += t2[1];
		
		//System.out.println("line: "+line);
		//System.out.println(">");
		//System.out.println(match);
		//System.out.println(">");
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] boolBase(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		line = line.trim();
		String[] temp = new String[2];
		// TODO error handling
		if (line.charAt(0) == '(' && line.charAt(line.length()-1) == ')') {
			temp = boolExpr(line, variables);
			if (temp[0] != null) 
				javaCode = temp[0];
		} else {
			temp = var(line);
			if (temp[0] == null) {
				System.err.println(temp[1]);
				//System.exit(1);
			}
			if (temp[1].contains("<string>")) checkVariable(line, variables);
			javaCode = temp[0];
		}
		match = "<bool_base>: " + line + "\n";
		match += temp[1];
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] mathExpr(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
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
	
	private static String loop(String line) {
		if (line.substring(0, 4).equals("for ")) {
			
		} else if (line.substring(0, 6).equals("while ")) {
			
		}
		
		return null;
	}
	
	/**
	 * <var>
	 */
	private static String[] var(String line) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		String var = line.trim();
		
		// TODO simplify code
		
		// <bool>
		String[] boolTemp = bool(var);
		if (boolTemp[0] != null) {
			javaCode = boolTemp[0];
			match = "<var>: " + var + "\n";
			match += boolTemp[1];
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		
		// <num>
		String[] numTemp = num(var);
		if (numTemp[0] != null) {
			match = "<var>: " + var + "\n";
			match += numTemp[1];
			javaCode = numTemp[0]; // the number in String
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		// Error handling: no letter than assume intended to do num
		boolean tf = true;
		for (int i = 0; i<var.length(); i++) {
			if (Character.isLetter(var.charAt(i))) {
				tf = false;
				break;
			}
		}
		if (tf) {
			match = numTemp[1];
			parsed[1] = match;
			return parsed;
		}
		
		// <string> or variable name
		String[] varTemp = variable(var);
		if (varTemp[0] != null) {
			javaCode = varTemp[0];
			match = "<var>: " + var + "\n";
			match += varTemp[1];
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}

		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	/**
	 * <string> or varialb name
	 */
	private static String[] variable(String var) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (!Character.isLetter(var.charAt(0))) {
			System.err.println("Error: "+var+" is not a valid variable name, "
					+ "valid variable name has to start with a letter");
			//System.exit(1);
		} 
		for (int i = 0; i<var.length(); i++) {
			char t = var.charAt(0);
			if (Character.isAlphabetic(t) || Character.isDigit(t) || t == '_')
				continue;
			else {
				System.err.println("Error: "+var+" is not a valid variable name, "
						+ "valid variable name only contains letter, digit and _");
				//System.exit(1);
			}
		}
		javaCode = var;
		match = "<string>: " + var + "\n";
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	/**
	 * <num>
	 */
	private static String[] num(String var) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		try {
			double num = Double.parseDouble(var);
			javaCode = String.valueOf(num);
			match = "<num>: " + var + "\n";			
			if (Math.ceil(num) == Math.floor(num)) {
				javaCode = javaCode.substring(0, javaCode.length()-2);
				match += "<int>: " + var + "\n";
			} else match += "<real>: " + var + "\n";
		} catch (NumberFormatException e) {
			match = "Error: "+var+" is not a valid number\n";
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	/**
	 * <bool> 
	 */
	private static String[] bool(String var) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (var.equals("true") || var.equals("false")) {
			javaCode = var;
			match = "<bool>: " + var + "\n";
		} else match = "Error: " + var + " is not a boolean value\n";
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
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
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fileContents;
	}
	
}

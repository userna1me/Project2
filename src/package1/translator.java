package package1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Deque;
import java.util.HashMap;
import java.util.Scanner;

public class translator {
	private static HashMap<String, Object> globalVariables;
	private static int nested = 0;
	
	private static ArrayList<Deque<String>> nestedStack;
	
	public static void main(String args[]) {
//		// read input file, assuming args[0] is input filename
//		if (args[0] != null) {
//			ArrayList<String> fileContents = readFile(args[0]);
//			globalVariables = new HashMap<String, Object>();
//			nestedStack = new ArrayList<Deque<String>>();
//			
//			// translate it
//			ArrayList<String> javaCode = compile(fileContents);
//			
//			// produce output file
//			// writeOutputFile(javaCode);
//			
//		// TODO interactive system? 
//		} else {
//			Scanner input = new Scanner(System.in);
//			String cmd = input.nextLine();
//			while (!cmd.equals("exit")) {
//				parse(cmd);
//				
//				cmd = input.nextLine();
//			}
//		}
		globalVariables = new HashMap<String, Object>();
		String res[];
		String stmt;
		stmt = "var i is 9";
		res = varAssign(stmt, globalVariables);
		for (String s : res)
			System.out.println(s);
		stmt = "i is 1 + 5";
		res = varAssign(stmt, globalVariables);
		for (String s : res)
			System.out.println(s);
		stmt = "i is / 2";
		res = varAssign(stmt, globalVariables);
		for (String s : res)
			System.out.println(s);
		stmt = "var i is 2";
		res = varAssign(stmt, globalVariables);
		for (String s : res)
			System.out.println(s);
		
	}
	
	/**
	 * Parse a single input line from System.in
	 * @param line
	 * @return
	 */
	private static boolean parse(String line) {
		String[] parsed = expr(line, globalVariables, false);
		
		return false;
	}
	
	private static ArrayList<String> compile(ArrayList<String> codes) {
		ArrayList<String> javaCodes = new ArrayList<String>();
		ArrayList<String> explictParsing = new ArrayList<String>();
		
		for (int i = 0; i < codes.size(); i++) {
			String code = codes.get(i);
			String trimed = code.trim();
			if (trimed.length() == 0) continue;
			if (trimed.charAt(0) != '#') {
				String[] temp = expr(code, globalVariables, false);
				if (temp[0] != null) {
					javaCodes.add(temp[0]);
					explictParsing.add(temp[1]);
				} else {
					System.err.print("[At line " + (i+1) +"] ");
					System.err.print(temp[1]);
					System.exit(1);
				}
				
				System.out.println(javaCodes.get(i));
				//System.out.println(nested);
				System.out.println("------------");
				System.out.print(explictParsing.get(i));
				
			} else {
				javaCodes.add("//" + code.substring(1));
				explictParsing.add("<commemt>: " + code + "\n");
			}
		}
		if (nested != 0) {
			String temp = javaCodes.get(javaCodes.size()-1);
			temp += "}";
			javaCodes.remove(javaCodes.size()-1);
			javaCodes.add(temp);
		}
		
		System.out.println("+++");
		for (String code: javaCodes) System.out.println(code);
		
		return javaCodes;
	}
	
	private static String[] expr(String line, HashMap<String, Object> variables, boolean fromNested) {
		if (!fromNested) {
			String[] nestedResult = nestedExpr(line, variables);
			if (nestedResult[0] != null) return nestedResult;
		}
		
		String[] printResult = print(line, variables);
		if (printResult[0] != null) return printResult;
		
		String[] varAssignResult = varAssign(line, variables);
		if (varAssignResult[0] != null) return varAssignResult;

		//String[] loopResult = loop(line, variables);
		
		String[] ifResult = ifStat(line, variables);		
		if (ifResult[0] != null) return ifResult;
		
		return ifResult;
	}
	
	private static String checkVariable(String var, HashMap<String, Object> variables) {
		if (variables.containsKey(var)) {
			Object val = variables.get(var);
			if (val == null) {
				return "Error: "+var+" doesn't have a value";
				//System.exit(1);
			}
		} else {
			return "Error: "+var+" isn't declared";
			//System.exit(1);
		}
		
		return null;
	}
	
	private static String[] loop(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (line.substring(0, 4).equals("for ")) {
		} else if (line.substring(0, 6).equals("while ")) {
		}

		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static  String[] nestedExpr(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (nested == 0) return parsed;

		String tab = "";
		boolean end = false;
		for (int i = 0; i < nested; i++) {
			if (line.charAt(i) != '\t') {
				if (i == nested - 1) {
					nested--;
					end = true;
					break;
				} ;
			}
			tab += '\t';
		}
		
		String trimedLine = line.trim();		
		String[] temp = expr(trimedLine, variables, true);
		
		if (temp[0] != null) {			
			javaCode = tab + temp[0];
			if (end) {
				javaCode = "} " + javaCode;
				match = "<nested_expr>: " + line + "\n";
				Deque<String> tl = nestedStack.get(nested);
				if (javaCode.contains("else if")) {
					if (tl.contains("if")) nested++;
					else {
						javaCode = null;
						match = "Error: missing if statement";
					}
				} else if (javaCode.contains("else")) {
					if (tl.contains("if")) nested++;
					else {
						javaCode = null;
						match = "Error: missing if statement";
						tl.pop();
					}
				}
			}
		} else {
			parsed[1] = temp[1];
			return parsed;
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] ifStat(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		//TODO error handling, check variable
		
		line = line.trim();
		String[] temp = new String[2];
		String head = "";
		String ns = "";
		if (line.length() > 3 && line.substring(0, 3).equals("if ")) {			
			nested++;
			head = "if";
			ns = line.substring(3).trim();
			Deque<String> tl = new ArrayDeque<String>();
			tl.push("if");
			nestedStack.add(tl);			
		} else if (line.length() > 8 && line.substring(0, 8).equals("else if ")) {
			if (nestedStack.isEmpty() || nestedStack.get(nested) == null 
					|| !nestedStack.get(nested).peek().equals("if")) {
				parsed[1] = "Error: Mising if statement";
				return parsed;
			}
			head = "else if";
			ns = line.substring(8).trim();
		} else if (line.length() == 4 && line.substring(0, 4).equals("else")) {
			if (nestedStack.isEmpty() || nestedStack.get(nested) == null 
					|| !nestedStack.get(nested).peek().equals("if")) {
				parsed[1] = "Error: Mising if statement";
				return parsed;
			}
			javaCode = "else {";
			match = "<if_stat>: " + line + "\n";
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		} else {
			parsed[1] = "Error: invalid if else statement";
			return parsed;
		}
		
		temp = boolExpr(ns, variables);
		
		if (temp[0] != null) {
			javaCode = head + " (" + temp[0] + ") {";
			match = "<if_stat>: " + line + "\n";
			match += temp[1];
		} else {
			parsed[1] = temp[1];
			return parsed;
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}

	/**
	 * <print>
	 */
	private static String[] print(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (line.length() > 6 && line.substring(0, 6).equals("print ")) {
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
			} else if (expr.charAt(0) == '"') match = "Error: missing \" at the end";
			else if (expr.charAt(len-1) == '"') match = "Error: missing \" in the beginning";
			else {
				// <var>
				String[] temp = var(expr);
				if (temp[0] != null) {
					if (temp[1].contains("<string>")) {
						String str = checkVariable(expr, variables);
						if (str != null) {
							parsed[1] = str;
							return parsed;
						}
					}
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
				if (mathTemp[0] != null) {
					javaCode = "System.out.println(" + mathTemp[0] + ");";
					match = "<print>: " + line + "\n";
					match += mathTemp[1];
				}
				// Error handling
				if (javaCode == null) {
					if (expr.contains(" + ") || expr.contains(" - ") || expr.contains(" * ")
							|| expr.contains(" / ") || expr.contains(" % ")) 
						match = mathTemp[1];
					else match = boolTemp[1];
				}
			}
		}
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String reverse(String str) {
		StringBuilder sb = new StringBuilder();
		sb.append(str);
		sb.reverse();
		return sb.toString();
	}
	
	private static String[] mathExpr(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		// TODO Error handling, integer.. 
		String op = "";
		String[] temp = new String[2];
		String t = reverse(line);
		
		if (line.contains(" + ")) {
			op = " + ";
			temp = t.split(" + ", 2);
		} else if (line.contains(" - ")) {
			op = " - ";
			temp = t.split(" - ", 2);
		} else {
			String[] ts = mathExpr1(line, variables);
			javaCode = ts[0];
			match = "<math_expr1>: " + line + "\n";
			match += ts[1];
			
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		String[] t1 = mathExpr(reverse(temp[1]), variables);
		String[] t2 = mathExpr1(reverse(temp[0]), variables);
		if (t1[0] != null && t2[0] != null) {
			javaCode = "(" + t1[0] + ")" + op + "(" + t2[0] + ")";
			match = "<math_expr>: "+line+"\n";
			match += t1[1];
			match += t2[1];
		} else if (t1[0] == null) match = t1[1];
		else match = t2[1];
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] mathExpr1(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		// TODO Error handling, integer.. 
		String op = "";
		String[] temp = new String[2];
		String t = reverse(line);
		
		if (line.contains(" * ")) {
			op = " * ";
			temp = t.split(" * ", 2);
		} else if (line.contains(" / ")) {
			op = " / ";
			temp = t.split(" / ", 2);
		} else if (line.contains(" % ")) {
				op = " % ";
				temp = t.split(" % ", 2);
		} else {
			String[] ts = neg(line, variables);
			javaCode = ts[0];
			match = "<math_expr1>: " + line + "\n";
			match += ts[1];
			
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		String[] t1 = mathExpr1(reverse(temp[1]), variables);
		String[] t2 = neg(reverse(temp[0]), variables);
		if (t1[0] != null && t2[0] != null) {
			javaCode = "(" + t1[0] + ")" + op + "(" + t2[0] + ")";
			match = "<math_expr1>: "+line+"\n";
			match += t1[1];
			match += t2[1];
		} else if (t1[0] == null) match = t1[1];
		else match = t2[1];
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] neg(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		line = line.trim();
		String neg = "";
		String base = "";
		if (line.charAt(0) == '-') {
			neg = "-";
			base = line.substring(1);
		} else base = line;
		String[] temp = mathBase(base, variables);
		if (temp[0] != null) {
			javaCode = neg + temp[0];
			match = "<neg>: " + line + "\n";
			match += temp[1];
		} else match = temp[1];
		
		parsed[0] = javaCode;
		parsed[1] = match;
		return parsed;
	}
	
	private static String[] mathBase(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		if (line.charAt(0) == '(' && line.charAt(line.length()-1) == ')') {
			String[] temp = boolExpr(line, variables);
			if (temp[0] != null) {
				javaCode = temp[0];
				match = "<math_base>: " + line + "\n";
				match += temp[1];
				parsed[0] = javaCode;
				parsed[1] = match;
				return parsed;
			}
		} 
		
		if (line.equals("true") || line.equals("false")) {
			parsed[1] = "Error: " + line + "is a boolean";
			return parsed; 
		}
		
		String[] varTemp = var(line);
		if (varTemp[0] != null) {
			javaCode = varTemp[0];
			match = "<math_base>: " + line + "\n";
			match += varTemp[1];
		} else match = varTemp[1];
		
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
			String t = reverse(line);
			String[] temp = t.split(" ro ", 2);
			String[] t1 = boolExpr(reverse(temp[1]), variables);
			String[] t2 = boolExpr1(reverse(temp[0]), variables);
			if (t1[0] != null && t2[0] != null) {
				javaCode = "(" + t1[0] + ") || (" + t2[0] + ")";
				match = "<bool_expr>: " + line + "\n";
				match += t1[1];
				match += t2[1];
			} else if (t1[0] == null) match = t1[1];
			else match = t2[1];
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
			String t = reverse(line);
			String[] temp = t.split(" dna ", 2);
			String[] t1 = boolExpr(reverse(temp[1]), variables);
			String[] t2 = boolExpr1(reverse(temp[0]), variables);
			if (t1[0] != null && t2[0] != null) {
				javaCode = "(" + t1[0] + ") && (" + t2[0] + ")";
				match = "<bool_expr1>: " + line + "\n";
				match += t1[1];
				match += t2[1];
			} else if (t1[0] == null) match = t1[1];
			else match = t2[1];
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
		String t = reverse(line);
		
		if (line.contains(" != ")) {
			op = " != ";
			temp = t.split(" =! ", 2);
		} else if (line.contains(" == ")) {
			op = " == ";
			temp = t.split(" == ", 2);
		} else if (line.contains(" < ")) {
			op = " < ";
			temp = t.split(" < ", 2);
		} else if (line.contains(" > ")) {
			op = " > ";
			temp = t.split(" > ", 2);
		} else if (line.contains(" <= ")) {
			op = " <= ";
			temp = t.split(" =< ", 2);
		} else if (line.contains(" >= ")) {
			op = " >= ";
			temp = t.split(" => ", 2);
		} else {
			String[] ts = boolBase(line, variables);
			javaCode = ts[0];
			match = "<bool_expr2>: " + line + "\n";
			match += ts[1];
			
			parsed[0] = javaCode;
			parsed[1] = match;
			return parsed;
		}
		String[] t1 = boolExpr2(reverse(temp[1]), variables);
		String[] t2 = boolBase(reverse(temp[0]), variables);
		if (t1[0] != null && t2[0] != null) {
			javaCode = "(" + t1[0] + ")" + op + "(" + t2[0] + ")";
			match = "<bool_expr2>: "+line+"\n";
			match += t1[1];
			match += t2[1];
		} else if (t1[0] == null) match = t1[1];
		else match = t2[1];
		
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
				parsed[1] = temp[1];
				return parsed;
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
	
	/**
	 * Variable assign line parsing
	 * @param line
	 * @return
	 */
	private static String[] varAssign(String line, HashMap<String, Object> variables) {
		line = line.trim();
		line = removeComment(line);
		String[] result = new String[2];
		String javaStatement;
		
		// TODO add variables to globalVariables
		// TODO? if theres a space between (var ) and " is ", then its a multiple-var assignment 
		
		if (line.substring(0,4).equals("var ")) {	// if variable declaration
			int i = 4;
			while( ! line.substring(i,i+4).equals(" is ")) {
				i++;
			}
			String varName = line.substring(4,i);		// split into var name and var assignment
			String assignment = line.substring(i+4);
			
			//check variables
			if (variables.containsKey(varName)) {
				result[0] = null;
				result[1] = "Variable has already been declared.";
				return result;
			} else {
				variables.put(varName, assignment);  // (x : x + y)
			}
			
			String[] statementParse = resolveStatement(assignment);
			if (statementParse[0] == null) {
				javaStatement = null;
				result[1] = statementParse[1];
			} else {
				javaStatement = statementParse[0] + " " + varName + " = " + statementParse[1] + ";";
			}
			result[0] = javaStatement;
		} else {		// else (line doesn't start with var) (existing variable reassignment)
			
			// TODO don't allow redefinintions of a different type
			int i = 0;
			while( ! line.substring(i,i+4).equals(" is ")) {
				i++;
			}
			String varName = line.substring(0,i);		// split into var name and var assignment
			String assignment = line.substring(i+4);			
			String[] statementParse = resolveStatement(assignment);
			if (statementParse[0] == null) {
				javaStatement = null;
				result[1] = statementParse[1];
			} else {
				if (statementParse[1].charAt(0) == '/') {
					javaStatement = varName + " " + statementParse[0] + " " + varName + " = " + statementParse[1] + ";";
				} else {
					javaStatement = statementParse[0] + " " + varName + " = " + statementParse[1] + ";";
				}
			}
			result[0] = javaStatement;
		}
		return result;
	}
	
	/**
	 * resolves the statement into an array of two strings:
	 * 		1. the return type of the statement
	 * 		2. the statement 
	 * In the case of an error: returns [null, error message]
	 * @param statement (in our language) like "46" or "x + 52" or "false"
	 * @return [return type (or null), return value] in java
	 */
	private static String[] resolveStatement(String statement) {
		statement.trim();
		String[] result = new String[2];
		int len = statement.length();
		int strMathLoc = strContainsMath(statement);
		// if statement is Not an equation
		if (strMathLoc == -1) {	
			if (Character.isDigit(statement.charAt(0))) {
				// if number
				try {
					Integer.parseInt(statement);
					result[0] = "int";
					result[1] = statement;
					return result;
				} catch(Exception e) {}
				try {
					Double.parseDouble(statement);
					result[0] = "double";
					result[1] = statement;
					return result;
				} catch(Exception e) {
					result[0] = null;
					result[1] = "Invalid variable assignment - number cannot be parsed.";
				}
			} else if (statement.charAt(0) == '"' && statement.charAt(len-1) == '"') {
				// if string
				result[0] = "String";
				result[1] = statement;
			} else if (statement.equals("true") || statement.equals("false")) {
				// if bool
				result[0] = "boolean";
				result[1] = statement;
			} else if (globalVariables.containsKey(statement)) {
				// if existing var
				Object value = globalVariables.get(statement);
				String type = value.getClass().getCanonicalName();
				if (type.equals("Integer"))
					result[0] = "int";
				else if (type.equals("Boolean"))
					result[0] = "boolean";
				else if (type.equals("Double"))
					result[0] = "double";
				else
					result[0] = type;
				result[1] = statement;
			} else {
				// else unknown
				result[0] = null;
				result[1] = "Invalid variable assignment.";
			}
		} else {	// if statement is an equation
			if (statement.charAt(strMathLoc) == '=') {
				// boolean exception
				result[0] = "boolean";
				result[1] = statement;
				return result;
			}
			String part1 = statement.substring(0, strMathLoc);
			String operator = statement.substring(strMathLoc, strMathLoc+1);
			String part2 = statement.substring(strMathLoc + 1);
			part1 = part1.trim();
			part2 = part2.trim();
			String[] part1info = resolveStatement(part2);
			String[] part2info = resolveStatement(part2);
			
			if (part1.equals("")) {
				if (part2info[0] == null) {
					result[0] = null;
					result[1] = "Invalid assignment equation. here";
					return result;
				} else {
					result[0] = part2info[0];
					result[1] = statement;
					return result;
				}
			}
			
			if (part1info[0] == null || part2info == null) {
				result[0] = null;
				result[1] = "Invalid assignment equation.";
				return result;
			} else if (part1info[0].equals(part2info[0])) {
				result[0] = part1info[0];
				result[1] = statement;
			} else {
				result[0] = null;
				result[1] = "Invalid assignment equation. Equation cannot be resolved.";
			}
		}
		return result;
	}
	
	/**
	 * Helper for varAssign - determines index of the first math operator in the given string,
	 * or returns -1 if none found.
	 */
	private static int strContainsMath(String line) {
		for (int i = 0; i < line.length()-1; i++)
			if (line.charAt(i) == '+'
					|| line.charAt(i) == '-'
					|| line.charAt(i) == '/'
					|| line.charAt(i) == '*'
					|| line.charAt(i) == '=' && line.charAt(i+i) == '=')
				return i;
		return -1;
	}
	
	/**
	 * Removes the comment (everything after #) from a line
	 * @param line
	 * @return
	 */
	private static String removeComment(String line) {
		for (int i = 0; i < line.length(); i++)
			if (line.charAt(i) == '#')
				return line.substring(0,i);
		return line;
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
		
		/// <string> or variable name
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
			match = "Error: "+var+" is not a valid variable name, "
					+ "valid variable name has to start with a letter";
			//System.exit(1);
			parsed[1] = match;
			return parsed;
		} 
		for (int i = 0; i<var.length(); i++) {
			char t = var.charAt(0);
			if (Character.isAlphabetic(t) || Character.isDigit(t) || t == '_')
				continue;
			else {
				match = "Error: "+var+" is not a valid variable name, "
						+ "valid variable name only contains letter, digit and _";
				//System.exit(1);
				parsed[1] = match;
				return parsed;
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
	
	/**
	 * Writes compiled javaCode into a file
	 * @param javaCode
	 */
	private static void writeOutputFile(ArrayList<String> javaCode) {
		try {
			// create file
			String filename = "output.txt";  // output file called 'output.txt', feel free to change
			File output = new File(filename);
			if (output.createNewFile()) {
				// success
			} else {
				System.out.println("File already exists.");
			}
			// write to file
			FileWriter writer = new FileWriter(filename);
			for (String line : javaCode) {
				writer.write(line);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}

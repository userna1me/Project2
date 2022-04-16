package package1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class translator {
	private static HashMap<String, Object> globalVariables;
	private static int nested = 0;
	
	private static ArrayList<String> nestedStack;
	
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		// read input file, assuming args[0] is input filename
		if (args[0] != null) {
			ArrayList<String> fileContents = readFile(args[0]);
			globalVariables = new HashMap<String, Object>();
			nestedStack = new ArrayList<String>();
			// translate it
			ArrayList<String> javaCodes = compile(fileContents);
			// output the code
			printResult(javaCodes, args[0]);
		// TODO interactive system? 
		} else {
			Scanner input = new Scanner(System.in);
			System.out.print(">> ");
			String cmd = input.nextLine();
			while (!cmd.equals("exit")) {
				parse(cmd);
				System.out.print(">> ");
				cmd = input.nextLine();
			}
		}
	}
	
	/**
	 * Print out the translated java codes
	 * 
	 * @param codes
	 * @param className
	 */
	private static void printResult(ArrayList<String> codes, String className) {
		System.out.println("public class "+className+"{ ");
		System.out.println();
		System.out.println("\tpublic static void main(String[] args) {");
		// can only take one command line input, stroe in arg
		System.out.println("\t\tint arg = 0;");
		System.out.println("\t\tif (args.length>0) {");
		System.out.println("\t\t\ttry { arg = (int) Double.parseDouble(args[0]);");
		System.out.println("\t\t\t} catch (NumberFormatException e) {");
		System.out.println("\t\t\t\tSystem.err.println(\"Error: command line input must be integer\");");
		System.out.println("\t\t\t\tSystem.exit(1);");
		System.out.println("\t\t\t}");
		System.out.println("\t\t}");
		System.out.println();
		for (String code: codes) {
			System.out.print("\t\t");
			System.out.println(code);
		}
		System.out.println("\t}\n\n}");
	}
	
	/**
	 * Parse a single input line from System.in
	 * @param line
	 */
	private static void parse(String line) {
		String[] parsed = expr(line, globalVariables, false);
		if (parsed[0] != null) {
			System.out.println(parsed[0]);
			System.out.println("+++++parsing process+++++");
			System.out.print(parsed[1]);
			System.out.println("+++++++++++++++++++++++++");
		} else {
			System.err.println(parsed[1]);
		}
	}
	
	private static ArrayList<String> compile(ArrayList<String> codes) {
		ArrayList<String> javaCodes = new ArrayList<String>();
		ArrayList<String> explicitParsing = new ArrayList<String>();
		
		for (int i = 0; i < codes.size(); i++) {
			String code = codes.get(i);
			String trimed = code.trim();
			if (trimed.length() == 0) {
				javaCodes.add(" ");
				explicitParsing.add(" ");
				continue;
			}
			if (trimed.charAt(0) != '#') {
				String[] temp = expr(code, globalVariables, false);
				if (temp[0] != null) {
					javaCodes.add(temp[0]);
					explicitParsing.add(temp[1]);
					
					System.out.println(">> "+code);
					System.out.println(temp[1]);
					
				} else {
					System.err.print("[At line " + (i+1) +"] ");
					System.err.print(temp[1]);
					System.exit(1);
				}
			} else {
				javaCodes.add("//" + code.substring(1));
				explicitParsing.add("<commemt>: " + code + "\n");
			}
		}
		while (nested != 0) {
			String temp = javaCodes.get(javaCodes.size()-1);
			temp += "}";
			nested--;
			javaCodes.remove(javaCodes.size()-1);
			javaCodes.add(temp);
		}

		writeOutputFile(explicitParsing, codes);
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
		
		String[] ifResult = ifStat(line, variables);		
		if (ifResult[0] != null) return ifResult;
		
		String[] loopResult = loop(line, variables);
		if (loopResult[0] != null) return loopResult;
		
		String trimed = line.trim();
		if (trimed.length() >= 5 && trimed.substring(0, 5).equals("print")) 
			return printResult;
		else if (trimed.length() >= 2 && trimed.substring(0, 3).equals("if")
				|| trimed.length() >= 4 && trimed.substring(0, 4).equals("else"))
			return ifResult;
		else if (line.length() > 3 && line.substring(0, 4).equals("for")
				|| line.length() > 5 && line.substring(0, 6).equals("while"))
			return loopResult;
		else if (line.length() > 3 && line.substring(0, 4).equals("var"))
			return varAssignResult;
		
		String[] parsed = new String[2];
		parsed[1] = "Error: In valid syntax";
		return parsed;
	}
	
	private static String checkVariable(String var, HashMap<String, Object> variables) {
		if (variables.containsKey(var)) {
			Object val = variables.get(var);
			if (val == null) return "Error: "+var+" doesn't have a value";
		} else return "Error: "+var+" isn't declared";
		
		return null;
	}
	
	private static String[] loop(String line, HashMap<String, Object> variables) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		
		line = line.trim();
		String label = "";
		if (line.length() > 4 && line.substring(0, 4).equals("for ")) {
			nested++;
			label = "for";
			String var = line.substring(4).trim();
			if (var.equals("true") || var.equals("false")) {
				parsed[1] = "Error: invalid for loop condition";
				return parsed;
			}
			
			String[] temp = var(var);
			if (temp[0] != null) {
				if (temp[1].contains("<string>")) {
					String str = checkVariable(var, variables);
					if (str != null) {
						parsed[1] = str;
						return parsed;
					} else {
						Object val = variables.get(var);
						if (val instanceof String && ((String) val).contains(" ") && 
								!((String) val).contains("\""))
							{}
						else if (num(val.toString())[0] == null) {
							parsed[1] = "Error: "+var+" is not a number";
							return parsed;
						}
					}
				}
				match = "<loop>: " + line + "\n";
				match += temp[1];
				javaCode = "for (int i = 0; i < ("+temp[0] + "); i++) {";
			} else {
				parsed[1] = temp[1];
				return parsed;
			}			
		} else if (line.length() > 6 && line.substring(0, 6).equals("while ")) {
			nested++;
			label = "while";	
			String[] temp = boolExpr(line.substring(6).trim(), variables);
			if (temp[0] != null) {
				javaCode = "while (" + temp[0] + ") {";
				match = "<loop>: " + line;
				match += temp[1];
			} else match = temp[1];		
		} else {
			parsed[1] = "Error: invalid loop statement";
			return parsed;
		}
		
		if (javaCode != null) nestedStack.add(label);

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
		int diff = 0;
		for (int i = 0; i < nested; i++) {
			if (line.charAt(i) != '\t') {
				end = true;
				diff = nested - i;
				nested = i;
				break;
			}
			tab += '\t';
		}

		String trimedLine = line.trim();		
		String[] temp = expr(trimedLine, variables, true);
		
		if (temp[0] != null) {			
			javaCode = tab + temp[0];
			if (end) {
				
				//System.out.println("nested: "+nested);
				//System.out.println("diff: "+diff);
				//System.out.println(nestedStack);
				
				if (diff >= 1) {
					javaCode = "} " + javaCode;
					diff--;
				}
				while (diff >= 1) {
					javaCode = "} " + javaCode;
					diff--;
					if (javaCode.contains("else if") || javaCode.contains("else {"))
						nestedStack.remove(nested);
					else nestedStack.remove(0);
				}
				
				while (nestedStack.size() > nested) {
					nestedStack.remove(0);
				}

				match = "<nested_expr>: " + line + "\n";
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
		} else if (line.length() > 8 && line.substring(0, 8).equals("else if ")) {
			nested++;
			if (nestedStack.isEmpty() || nestedStack.get(nested-1) == null 
					|| !nestedStack.get(nested-1).equals("if")) {
				parsed[1] = "Error: Mising if statement";
				return parsed;
			}
			head = "else if";
			ns = line.substring(8).trim();
		} else if (line.length() == 4 && line.substring(0, 4).equals("else")) {
			nested++;
			if (nestedStack.isEmpty() || nestedStack.get(nested-1) == null 
					|| !nestedStack.get(nested-1).equals("if")) {
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
			
			if (head.equals("if")) nestedStack.add("if");	
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
		} else {
			match = "Error: invalid print statement";
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
		
		if (!line.contains(" is ")) {
			result[1] = "Error: invalid variable assignment";
			return result;
		}
		
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
					// javaStatement = statementParse[0] + " " + varName + " = " + statementParse[1] + ";";
					javaStatement = varName + " = " + statementParse[1] + ";";
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
//					Object value = globalVariables.get(statement);
//					String type = value.getClass().getCanonicalName();
//					if (type.equals("Integer"))
//						result[0] = "int";
//					else if (type.equals("Boolean"))
//						result[0] = "boolean";
//					else if (type.equals("Double"))
//						result[0] = "double";
//					else if (type.equals("String"))
//						result[0] = "String";
//					else
//						//result[0] = type;
						result[0] = "";
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
			for (int i = 0; i < line.length()-1; i++) {
				if ( line.charAt(i) == '+'
						|| line.charAt(i) == '-'
						|| line.charAt(i) == '/'
						|| line.charAt(i) == '*'
						|| line.substring(i,i+2).equals("=="))
					return i;
			}
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
	
	/**
	 * <var>
	 */
	private static String[] var(String line) {
		String[] parsed = new String[2];
		String javaCode = null;
		String match = null;
		String var = line.trim();

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
				parsed[1] = match;
				return parsed;
			}
		}
		
		HashSet<String> label = new HashSet<String>();
		label.add("if"); label.add("else"); label.add("for"); label.add("while");
		label.add("true"); label.add("false");label.add("var"); label.add("is");
		if (label.contains(var)) {
			parsed[1] = "Error: variable name can't be" + var + "\n";
			return parsed;
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
	private static void writeOutputFile(ArrayList<String> parsing, ArrayList<String> codes) {
		try {
			// create file
			String filename = "Explicit Parsing.txt";
			File output = new File(filename);
			if (output.createNewFile()) {
				// success
			} else {
				System.out.println("File already exists.");
			}
			// write to file
			FileWriter writer = new FileWriter(filename);
			for (int i = 0; i < codes.size(); i++) {
				if (codes.get(i).trim().length() == 0) continue;
				writer.write(">> " + codes.get(i) + "\n");
				
				if (parsing.get(i) == null) writer.write("null\n");
				
				else writer.write(parsing.get(i));
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}

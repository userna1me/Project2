import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public class example {
	private static Pattern var_assign = Pattern.compile("^(.+) = (.+)\\.$");
	private static Pattern type_var_dec = Pattern.compile("^(\\w+) (\\w+)$");
	private static Pattern type = Pattern.compile("^int|bool$");
	private static Pattern intVal = Pattern.compile("^\\d+$");
	private static Pattern var = Pattern.compile("^\\w+$");
	private static Pattern bool = Pattern.compile("^t|f$");
	
	public static void main (String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.print(">> ");
		String cmd = in.nextLine();
		while(!cmd.equals("exit")) {
			parseCmd(cmd);
			System.out.print(">> ");
			cmd = in.nextLine();
		}
	}
	
	private static void parseCmd(String cmd) {
		varAssign(cmd);
	}
	
	private static boolean varAssign(String cmd) {
		Matcher m = var_assign.matcher(cmd);
		boolean match = false;
		if(m.find()) {
			match = true;
			match = match && varDecList(m.group(1));
			match = match && valList(m.group(2));
		}
		printMsg(match, "<v,ar_assign>", cmd, "variable assignment statement");
		return match;
	}
	
	private static boolean varDecList(String cmd) {
		String[] split = cmd.split(", ");
		boolean match = true;
		for(int i = 0; i < split.length; i++) {
			match = match && varDec(split[i]);
		}
		printMsg(match, "<var_dec_list>", cmd, "variable declaration list");
		return match;
	}
	
	private static boolean varDec(String cmd) {
		boolean match = false;
		Matcher m = type_var_dec.matcher(cmd);
		if(m.find()) {
			match = true;
			match = match && type(m.group(1));
			match = match && var(m.group(2));
		} else match = var(cmd);
		printMsg(match, "<var_dec>", cmd, "variable declaration");
		return match;
	}
	
	private static boolean type(String cmd) {
		Matcher m = type.matcher(cmd);
		boolean match = m.find();
		printMsg(match, "<type>", cmd, "type");
		return match;
	}
	
	private static boolean var(String cmd) {
		Matcher m = var.matcher(cmd);
		boolean match = m.find();
		printMsg(match, "<var>", cmd, "variable");
		return match;
	}
	
	private static boolean valList(String cmd) {
		String[] split = cmd.split(", ");
		boolean match = true;
		for(int i = 0; i < split.length; i++) {
			match = match && val(split[i]);
		}
		printMsg(match, "<val_list>", cmd, "value list");
		return match;
	}
	
	private static boolean val(String cmd) {
		Matcher m = intVal.matcher(cmd);
		boolean match = m.find();
		if(match) printMsg(match, "<int>", cmd, "integer");
		else {
			m = bool.matcher(cmd);
			match = m.find();
			if(match) printMsg(match, "<bool>", cmd, "boolean");
			else {
				m = var.matcher(cmd);
				match = m.find();
				if(match)
				printMsg(match, "<var>", cmd, "variable");
			}
		}
		printMsg(match, "<val>", cmd, "value");
		return match;
	}
	
	private static void printMsg(boolean match, String ntName, String cmd, String item) {
		if(match) System.out.println(ntName + ": " + cmd);
		else System.out.println("Failed to parse: {" + cmd + "} is not a valid " + item + ".");
	}
}


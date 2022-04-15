import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Test {

	public static void main(String[] args) {
		ArrayList<String> test = new ArrayList<String>();
		String t = "abc";
		test.add(t);
		
		String ttt = test.get(0);
		ttt += "}";
		//t.pop();
		
		System.out.println(test.get(0));
	}

}

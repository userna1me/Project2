public class Test {

	public static void main(String[] args) {
		int arg = (int) Double.parseDouble(args[0]);
		
		int f1 = 0;
		int f2 = 1;
		if ((arg) == (0)) {
			System.out.println(f1);
		} else if ((arg) == (1)) {
			System.out.println(f2);
		} else {
			int temp = arg - 2;
			int result = 0;
			for (int i = 0; i < (temp); i++) {
				java.lang.String result = f1 + f2;
				java.lang.String f1 = f2;
				java.lang.String f2 = result;
		} 	
		System.out.println(result);}


	}

}

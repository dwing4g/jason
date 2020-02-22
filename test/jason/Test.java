package jason;

public final class Test {
	public static void main(String[] args) {
		String[] tests = { "3.1415926", "31415926", "0.31415926", "314.15926", "3.1415926e7", "3.1415926E-7", "0",
				"1.0" };
		double r = 0;
		for (int i = 0; i < 10000000; i++)
			for (int j = 0; j < 8; j++)
				r += Double.parseDouble(tests[j]);
		System.out.format("%15.6f%n", r); // 628321706217711.400000
	}
}

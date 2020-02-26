package jason;

public final class TestNumberParser {
	public static void main(String[] args) {
		byte[][] tests = { "3.1415926 ".getBytes(), "31415926 ".getBytes(), "0.31415926 ".getBytes(),
				"314.15926 ".getBytes(), "3.1415926e7 ".getBytes(), "3.1415926E-7 ".getBytes(), "0 ".getBytes(),
				"1.0 ".getBytes() };
		Jason jason = new Jason();
		double r = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++)
				r += jason.buf(tests[j]).parseDouble();
		}
		System.out.format("%15.6f%n", r); // 628321706217711.400000
	}
}

package jason;

public final class TestNumberParser {
	public static void testReader() {
		byte[][] tests = { "3.1415926 ".getBytes(), "31415926 ".getBytes(), "0.31415926 ".getBytes(),
				"314.15926 ".getBytes(), "3.1415926e7 ".getBytes(), "3.1415926E-7 ".getBytes(), "0 ".getBytes(),
				"1.0 ".getBytes() };
		JasonReader jr = new JasonReader();
		double r = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++)
				r += jr.buf(tests[j]).parseDouble();
		}
		System.out.format("%15.6f%n", r); // 628321706217711.400000
	}

	public static void testWriter() {
		double[] tests = { 3.1415926, 31415926, 0.31415926, 314.15926, 3.1415926e7, 3.1415926E-7, 0, 1.0 };
		JasonWriter jw = new JasonWriter();
		int n = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
//				n += Double.toString(tests[j]).length();
				jw.clear().write(tests[j]);
				n += jw.size();
//				System.out.println(new String(jw.buf, 0, jw.pos));
			}
		}
		System.out.format("%d%n", n); // 660000000
	}

	public static void main(String[] args) {
		long t = System.nanoTime();
		testReader();
		System.out.println((System.nanoTime() - t) / 1_000_000 + " ms");
		t = System.nanoTime();
		testWriter();
		System.out.println((System.nanoTime() - t) / 1_000_000 + " ms");
	}
}

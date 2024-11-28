package jason;

import java.util.concurrent.ThreadLocalRandom;

public final class TestNumberParser {
	public static void testReader() {
		byte[][] tests = {"3.1234567 ".getBytes(), "31234567 ".getBytes(), "0.31234567 ".getBytes(),
				"312.34567 ".getBytes(), "3.1234567e7 ".getBytes(), "3.1234567E-7 ".getBytes(), "0 ".getBytes(),
				"1.0 ".getBytes()};
		JsonReader jr = new JsonReader();
		double r = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++)
				r += jr.buf(tests[j]).parseDouble();
		}
		System.out.format("%s.testReader: %15.6f\n", TestNumberParser.class.getSimpleName(), r); // 624694507922444.400000
	}

	public static void testWriter() {
		double[] tests = {3.1234567, 31234567, 0.31234567, 312.34567, 3.1234567e7, 3.1234567E-7, 0, 1.0};
		JsonWriter jw = new JsonWriter();
		int n = 0;
		for (int i = 0; i < 10_000_000; i++) {
			for (int j = 0; j < 8; j++) {
//				n += Double.toString(tests[j]).length();
				jw.clear().write(tests[j]);
				n += jw.size();
//				System.out.println(new String(jw.buf, 0, jw.pos));
			}
		}
		System.out.format("%s.testWriter: %d\n", TestNumberParser.class.getSimpleName(), n); // 660000000
	}

	public static void testRandomParser() {
		final JsonReader jr = JsonReader.local();
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < 10_000_000; i++) {
			long v;
			do
				v = r.nextLong();
			while ((v & 0x7ff0_0000_0000_0000L) == 0x7ff0_0000_0000_0000L);
			final double f = Double.longBitsToDouble(v);
			final double f2 = jr.buf(f + " ").parseDouble();
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testRandomParser[" + i + "]: " + f + " != " + f2);
		}
		System.out.println("testRandomParser OK!");
	}

	public static void main(String[] args) {
//		testRandomParser();

		long t = System.nanoTime();
		testReader();
		System.out.println(TestNumberParser.class.getSimpleName() + ".testReader: " +
				(System.nanoTime() - t) / 1_000_000 + " ms");
		t = System.nanoTime();
		testWriter();
		System.out.println(TestNumberParser.class.getSimpleName() + ".testWriter: " +
				(System.nanoTime() - t) / 1_000_000 + " ms");
	}
}

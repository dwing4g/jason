package jason;

import java.util.concurrent.ThreadLocalRandom;

public class TestWriter {
	static final JsonWriter jw = new JsonWriter();
	static final JsonReader jr = new JsonReader();
	static int count;

	static void testInt(int d) {
		jw.free().ensure(11);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		long d2 = jr.parseInt();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 11)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		count++;
	}

	static void testInt(int d, String s) {
		jw.free().ensure(11);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		long d2 = jr.parseInt();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 11)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		if (!ss.equals(s))
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' != '" + s + "'");
		count++;
	}

	static void testLong(long d) {
		if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE)
			testInt((int)d);
		jw.free().ensure(20);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		long d2 = jr.parseLong();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 20)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		count++;
	}

	static void testLong(long d, String s) {
		if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE)
			testInt((int)d);
		jw.free().ensure(20);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		long d2 = jr.parseLong();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 20)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		if (!ss.equals(s))
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' != '" + s + "'");
		count++;
	}

	static void testDouble(double d) {
		jw.free().ensure(25);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		double d2 = jr.parseDouble();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 25)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		count++;
	}

	static void testDouble(double d, String s) {
		jw.free().ensure(25);
		jw.write(d);
		String ss = jw.toString();
		jr.buf(jw.toBytes());
		double d2 = jr.parseDouble();
		if (d != d2)
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' => " + d2);
		if (jw.size() > 25)
			throw new IllegalStateException("too long size: " + d + " => '" + ss + "' => " + d2);
		if (!ss.equals(s))
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' != '" + s + "'");
		count++;
	}

	static void testDouble(int maxDecimalPlaces, double d, String s) {
		testDouble(d);
		jw.free().ensure(25);
		jw.write(d, maxDecimalPlaces);
		String ss = jw.toString();
		if (!ss.equals(s))
			throw new IllegalStateException("check err: " + d + " => '" + ss + "' != '" + s + "'");
		count++;
	}

	public static void testDoubleRange() {
		final JsonWriter jw = JsonWriter.local();
		double d = 0.01;
		for (int j = 0; j < 5; j++, d *= 10) {
			long dd = Double.doubleToRawLongBits(d);
			for (int i = 0; i < 1_000_000; i++) {
				final double f = Double.longBitsToDouble(dd + i);
				jw.clear().write(f);
				final String s = jw.toString();
				final double f2 = Double.parseDouble(s);
				if (f != f2)
					throw new AssertionError("testDoubleRange[" + j + "," + i + "]: " + f + " != " + f2 + ", " + s);
			}
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits(e << 52);
			jw.clear().write(f);
			final String s = jw.toString();
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleRange[e" + e + "]: " + f + " != " + f2 + ", " + s);
		}
		for (long e = 0; e < 2048; e++) {
			final double f = Double.doubleToRawLongBits((e << 52) + 1);
			jw.clear().write(f);
			final String s = jw.toString();
			final double f2 = Double.parseDouble(s);
			//noinspection ConstantValue
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleRange[e" + e + ":1]: " + f + " != " + f2 + ", " + s);
		}
		for (int i = 0; i < 1_000_000; i++) {
			final double f = Double.doubleToRawLongBits(i);
			jw.clear().write(f);
			final String s = jw.toString();
			final double f2 = Double.parseDouble(s);
			if (f != f2)
				throw new AssertionError("testDoubleRange[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testDoubleRange OK!");
	}

	public static void testDoubleRandom() {
		final JsonWriter jw = JsonWriter.local();
		final ThreadLocalRandom r = ThreadLocalRandom.current();
		for (int i = 0; i < 10_000_000; i++) {
			long v = r.nextLong();
			if ((v & 0x7ff0_0000_0000_0000L) == 0x7ff0_0000_0000_0000L) {
				v &= 0xfff8_0000_0000_0000L; // Infinity/-Infinity/NaN
				if (v == 0xfff8_0000_0000_0000L)
					v = 0x7ff8_0000_0000_0000L;
			}
			final double f = Double.longBitsToDouble(v);
			jw.clear().write(f);
			final String s = jw.toString();
			final double f2 = Double.parseDouble(s);
			if (f != f2 && !(Double.isNaN(f) && Double.isNaN(f2)))
				throw new AssertionError("testDoubleRandom[" + i + "]: " + f + " != " + f2 + ", " + s);
		}
		System.out.println("testDoubleRandom OK!");
	}

	public static void main(String[] args) {
		testLong(0);
		testLong(1);
		testLong(-1);
		testLong(9);
		testLong(-9);
		testLong(10);
		testLong(-10);
		testLong(11);
		testLong(-11);
		testLong(Integer.MAX_VALUE);
		testLong(-Integer.MAX_VALUE);
		testLong(Integer.MIN_VALUE);
		testLong(0x8000_0000L);
		testLong(Long.MAX_VALUE);
		testLong(-Long.MAX_VALUE);
		testLong(Long.MIN_VALUE);
		for (int i = 0; i < 32; i++) {
			testLong(0x5a5a_5a5a_5a5a_5a5aL >> i);
			testLong(0xa5a5_a5a5_a5a5_a5a5L >> i);
		}

		testDouble(0.0, "0.0");
		testDouble(-0.0, "-0.0");
		testDouble(1.0, "1.0");
		testDouble(-1.0, "-1.0");
		testDouble(1.2345, "1.2345");
		testDouble(1.2345678, "1.2345678");
		testDouble(0.123456789012, "0.123456789012");
		testDouble(1234567.8, "1234567.8");
		testDouble(-79.39773355813419, "-79.39773355813419");
		testDouble(-36.973846435546875, "-36.973846435546875");
		testDouble(0.000001, "0.000001");
		testDouble(0.0000001, "1e-7");
		testDouble(1e30, "1e30");
		testDouble(1.234567890123456e30, "1.234567890123456e30");
		testDouble(5e-324, "5e-324"); // Min subnormal positive double
		testDouble(2.225073858507201e-308, "2.225073858507201e-308"); // Max subnormal positive double
		testDouble(2.2250738585072014e-308, "2.2250738585072014e-308"); // Min normal positive double
		testDouble(1.7976931348623157e308, "1.7976931348623157e308"); // Max double

		testDouble(3, 0.0, "0.0");
		testDouble(1, 0.0, "0.0");
		testDouble(3, -0.0, "-0.0");
		testDouble(3, 1.0, "1.0");
		testDouble(3, -1.0, "-1.0");
		testDouble(3, 1.2345, "1.234");
		testDouble(2, 1.2345, "1.23");
		testDouble(1, 1.2345, "1.2");
		testDouble(3, 1.2345678, "1.234");
		testDouble(3, 1.0001, "1.0");
		testDouble(2, 1.0001, "1.0");
		testDouble(1, 1.0001, "1.0");
		testDouble(3, 0.123456789012, "0.123");
		testDouble(2, 0.123456789012, "0.12");
		testDouble(1, 0.123456789012, "0.1");
		testDouble(4, 0.0001, "0.0001");
		testDouble(3, 0.0001, "0.0");
		testDouble(2, 0.0001, "0.0");
		testDouble(1, 0.0001, "0.0");
		testDouble(3, 1234567.8, "1234567.8");
		testDouble(3, 1e30, "1e30");
		testDouble(3, 5e-324, "0.0"); // Min subnormal positive double
		testDouble(3, 2.225073858507201e-308, "0.0"); // Max subnormal positive double
		testDouble(3, 2.2250738585072014e-308, "0.0"); // Min normal positive double
		testDouble(3, 1.7976931348623157e308, "1.7976931348623157e308"); // Max double
		testDouble(5, -0.14000000000000001, "-0.14");
		testDouble(4, -0.14000000000000001, "-0.14");
		testDouble(3, -0.14000000000000001, "-0.14");
		testDouble(3, -0.10000000000000001, "-0.1");
		testDouble(2, -0.10000000000000001, "-0.1");
		testDouble(1, -0.10000000000000001, "-0.1");
		System.out.println(TestWriter.class.getSimpleName() + ": " + count + " tests OK!");

		testDoubleRange();
		testDoubleRandom();
	}
}

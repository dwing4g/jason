package jason;

public class TestWriter {
	static final JasonWriter jw = new JasonWriter();
	static final JasonReader jason = new JasonReader();
	static int count;

	static void testInt(int d) {
		jw.free().ensure(11);
		jw.write(d);
		String ss = jw.toString();
		jason.buf(jw.toBytes());
		long d2 = jason.parseInt();
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
		jason.buf(jw.toBytes());
		long d2 = jason.parseInt();
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
		jason.buf(jw.toBytes());
		long d2 = jason.parseLong();
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
		jason.buf(jw.toBytes());
		long d2 = jason.parseLong();
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
		jason.buf(jw.toBytes());
		double d2 = jason.parseDouble();
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
		jason.buf(jw.toBytes());
		double d2 = jason.parseDouble();
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

		System.out.println(count + " tests OK!");
	}
}

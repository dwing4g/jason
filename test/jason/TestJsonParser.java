package jason;

public final class TestJsonParser {
	public static class C {
		int a;
		String b;
		C c;
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		byte[] bytes = "{\"b\":\"xyz\", \"a\":123,\"c\":{\"a\":456,\"b\":\"abc\"}}".getBytes();
		long v = 0;
		long t = System.nanoTime();
		Jason jason = new Jason();
		for (int i = 0; i < 10_000_000; ++i) {
			C c = jason.buf(bytes).parse(C.class);
			v += c.a + c.c.a;
		}
		System.out.println(System.nanoTime() - t);
		System.out.println(v);
	}
}

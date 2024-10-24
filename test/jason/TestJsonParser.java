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
		JsonReader jr = new JsonReader();
		for (int i = 0; i < 10_000_000; ++i) {
			C c = jr.buf(bytes).parse(C.class);
			if (c == null)
				throw new RuntimeException();
			v += c.a + c.c.a;
		}
		System.out.println(TestJsonParser.class.getSimpleName() + ": " + v + ", " +
				(System.nanoTime() - t) / 1_000_000 + " ms");
	}
}

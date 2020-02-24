package jason;

public final class TestJsonParser {
	public static class C {
		int a;
		String b;
		C c;
	}

	public static void main(String[] args) throws InstantiationException {
		byte[] bytes = "{\"b\":\"xyz\", \"a\":123,\"c\":{\"a\":456,\"b\":\"abc\"}}".getBytes();
		long v = 0;
		long t = System.nanoTime();
		JsonParser jp = new JsonParser();
		for (int i = 0; i < 10_000_000; ++i) {
			C c = jp.buf(bytes).parseObj(C.class);
			v += c.a + c.c.a;
		}
		System.out.println(System.nanoTime() - t);
		System.out.println(v);
	}
}

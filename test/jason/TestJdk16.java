package jason;

public class TestJdk16 {
	record Record(int v, String s) {
	}

	public static void test1() throws ReflectiveOperationException {
		Record r = new Record(123, "abc");
		String json = JsonWriter.local().clear().write(r).toString();
		// System.out.println(json); // {"v":123,"s":"abc"}
		Record r2 = JsonReader.local().buf(json).parse(Record.class);
		Test.assertEquals(r, r2);
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		test1();
		System.out.println(TestJdk16.class.getSimpleName() + ": 1 test OK!");
	}
}

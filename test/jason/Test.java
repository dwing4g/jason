package jason;

public final class Test {
	static class A {
		int a;
	}

	static class B extends A {
		int b;
	}

	static class C {
		A a = new B();
	}

	public static void test1() throws ReflectiveOperationException {
		byte[] buf = "{a:[{x:1,y:2},{x:3,y:4},{x:5,y:6}]}".getBytes();
		Object obj = JasonReader.local().buf(buf).parse();
		if (obj == null)
			throw new RuntimeException();
		System.out.println(obj.toString().equals("{a=[{x=1, y=2}, {x=3, y=4}, {x=5, y=6}]}"));
	}

	public static void test2() throws ReflectiveOperationException {
		C c = JasonReader.local().buf("{a:{a:1,b:2}}".getBytes()).parse(C.class);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == B.class && c.a.a == 1 && ((B) c.a).b == 2);

		c.a = new A();
		c = JasonReader.local().buf("{a:{a:3,b:4}}".getBytes()).parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == A.class && c.a.a == 3);

		c.a = null;
		c = JasonReader.local().buf("{a:{a:5,b:6}}".getBytes()).parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == A.class && c.a.a == 5);

		Jason.getClassMeta(A.class).setParser((jason, __, ___) -> jason.parse(B.class));
		c.a = null;
		c = JasonReader.local().buf("{a:{a:7,b:8}}".getBytes()).parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == B.class && c.a.a == 7 && ((B) c.a).b == 8);
	}

	public static void test3() {
		C c = new C();
		c.a.a = 1;
		((B) c.a).b = -1;
		System.out.println(JasonWriter.local().clear().write(c).toString());
	}

	public static void test4() {
		C c = new C();
		c.a.a = 1;
		((B) c.a).b = -1;
		System.out.println(JasonWriter.local().clear().setFlags(JasonWriter.FLAG_PRETTY_FORMAT).write(c).toString());
	}

	public static void test5() {
		C c = new C();
		c.a.a = 1;
		((B) c.a).b = -1;
		System.out.println(JasonWriter.local().clear().setFlags(JasonWriter.FLAG_NO_QUOTE_KEY).write(c).toString());
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		test1();
		test2();
		test3();
		test4();
		test5();
	}
}

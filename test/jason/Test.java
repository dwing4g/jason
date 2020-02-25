package jason;

public final class Test {
	public static void test1() throws ReflectiveOperationException {
		byte[] buf = "{a:[{x:1,y:2},{x:3,y:4},{x:5,y:6}]}".getBytes();
		Object obj = new JsonParser(buf).parse();
		System.out.println(obj.toString().equals("{a=[{x=1, y=2}, {x=3, y=4}, {x=5, y=6}]}"));
	}

	public static void test2() throws ReflectiveOperationException {
		class A {
			int a;
		}
		class B extends A {
			int b;
		}
		class C {
			A a = new B();
		}

		C c = JsonParser.local().buf("{a:{a:1,b:2}}".getBytes()).parse(C.class);
		System.out.println(c.a.getClass() == B.class && c.a.a == 1 && ((B) c.a).b == 2);

		c.a = new A();
		c = JsonParser.local().buf("{a:{a:3,b:4}}".getBytes()).parse(c);
		System.out.println(c.a.getClass() == A.class && c.a.a == 3);

		c.a = null;
		c = JsonParser.local().buf("{a:{a:5,b:6}}".getBytes()).parse(c);
		System.out.println(c.a.getClass() == A.class && c.a.a == 5);

		JsonParser.getClassMeta(A.class).setIdentifier(jp -> JsonParser.getClassMeta(B.class));
		c.a = null;
		c = JsonParser.local().buf("{a:{a:7,b:8}}".getBytes()).parse(c);
		System.out.println(c.a.getClass() == B.class && c.a.a == 7 && ((B) c.a).b == 8);
		JsonParser.getClassMeta(C.class).setIdentifier(null);
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		test1();
		test2();
	}
}

package jason;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class Test {
	static class A {
		int a;
	}

	static class B extends A {
		int b;
	}

	static class C {
		A a = new B();
		C c;
		Map<A, Integer> m = new HashMap<>();
	}

	public static void test1() throws ReflectiveOperationException {
		byte[] buf = "{a:[{x:1,y:2},{x:3,y:4},{x:5,y:6}]}".getBytes();
		Object obj = JasonReader.local().buf(buf).parse();
		if (obj == null)
			throw new RuntimeException();
		System.out.println(obj.toString().equals("{a=[{x=1, y=2}, {x=3, y=4}, {x=5, y=6}]}"));
	}

	public static void test2() throws ReflectiveOperationException {
		C c = JasonReader.local().buf("{a:{a:1,b:2}}").parse(C.class);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == B.class && c.a.a == 1 && ((B)c.a).b == 2);

		c.a = new A();
		c = JasonReader.local().buf("{a:{a:3,b:4}}").parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == A.class && c.a.a == 3);

		c.a = null;
		c = JasonReader.local().buf("{a:{a:5,b:6}}").parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == A.class && c.a.a == 5);

		Jason.getClassMeta(A.class).setParser((jason, __, ___, ____) -> jason.parse(B.class));
		c.a = null;
		c = JasonReader.local().buf("{a:{a:7,b:8}}").parse(c);
		if (c == null)
			throw new RuntimeException();
		System.out.println(c.a.getClass() == B.class && c.a.a == 7 && ((B)c.a).b == 8);
	}

	public static void test3() {
		C c = new C();
		c.a.a = 1;
		((B)c.a).b = -1;
		System.out.println(JasonWriter.local().clear().write(c));
	}

	public static void test4() {
		C c = new C();
		c.a.a = 1;
		((B)c.a).b = -1;
		System.out.println(JasonWriter.local().clear().setFlags(JasonWriter.FLAG_PRETTY_FORMAT).write(c));
	}

	public static void test5() {
		C c = new C();
		c.a.a = 1;
		((B)c.a).b = -1;
		System.out.println(JasonWriter.local().clear().setFlags(JasonWriter.FLAG_NO_QUOTE_KEY).write(c));
	}

	public static void test6() {
		C c = new C();
		c.a.a = 1;
		((B)c.a).b = -1;
		System.out.println(JasonWriter.local().clear().setFlags(JasonWriter.FLAG_WRITE_NULL).write(c));
	}

	public static void test7() {
		System.out.println(System.getProperty("java.version"));
		System.out.println(Jason.getClassMeta(Inet4Address.class));
	}

	static class D {
		final HashMap<Integer, Integer> m = new HashMap<>();
	}

	public static void test8() throws ReflectiveOperationException {
		D d = new D();
		d.m.put(123, 456);
		String s = JasonWriter.local().clear().setFlags(0).write(d).toString();
		System.out.println(s);
		d.m.clear();
		JasonReader.local().buf("{\"m\":{123:456}}").parse(d);
		System.out.println(d.m);
		if (d.m.entrySet().iterator().next().getKey().getClass() != Integer.class)
			throw new RuntimeException();
	}

	static class E {
		int a;
		E e;
	}

	public static void test9() {
		E e = new E();
		e.a = 123;
		e.e = e;
		System.out.println(JasonWriter.local().clear().setDepthLimit(4).write(e));
	}

	abstract static class F1 {
		int f;
	}

	@SuppressWarnings({"serial", "RedundantSuppression"})
	static class F2 extends ArrayList<Object> {
		private F2() {
		}
	}

	static class G {
		final Set<Integer> set1 = new HashSet<>();
		HashSet<Integer> set2;
		Set<Integer> set3;
		Map<String, String> e1;
		F1 f1;
		F2 f2;
	}

	@SuppressWarnings("null")
	public static void test10() throws ReflectiveOperationException {
		G g = JasonReader.local().buf("{\"set1\":[123,456],\"set2\":[789],\"set3\":[],\"e1\":{\"1\":[]},\"f2\":[222]}")
				.parse(G.class);
		assert g != null;
		if (g.set1.getClass() != HashSet.class)
			throw new RuntimeException();
		if (g.set2 == null || g.set2.getClass() != HashSet.class)
			throw new RuntimeException();
		if (g.set3 == null || g.set3.getClass() != HashSet.class)
			throw new RuntimeException();
		if (((Number)g.f2.get(0)).intValue() != 222)
			throw new RuntimeException();
	}

	public static void test11() {
		int[] a = new int[]{1, 2, 3};
		String[] b = new String[]{"a", "b", "c"};
		List<Integer> c = new ArrayList<>();
		Collections.addAll(c, 1, 2, 3);
		List<String> d = new ArrayList<>();
		Collections.addAll(d, b);
		Map<Integer, String> e = new TreeMap<>();
		e.put(1, "a");
		e.put(2, "b");
		e.put(3, "c");
		System.out.println(JasonWriter.local().clear().write(a));
		System.out.println(JasonWriter.local().clear().write(b));
		System.out.println(JasonWriter.local().clear().write(c));
		System.out.println(JasonWriter.local().clear().write(d));
		System.out.println(JasonWriter.local().clear().write(e));
		JasonWriter.local().setPrettyFormat(true).setWrapElement(true);
		System.out.println(JasonWriter.local().clear().write(a));
		System.out.println(JasonWriter.local().clear().write(b));
		System.out.println(JasonWriter.local().clear().write(c));
		System.out.println(JasonWriter.local().clear().write(d));
		System.out.println(JasonWriter.local().clear().write(e));
	}

	public static void test12() {
		System.out.printf("%X%n", JasonWriter.umulHigh(0x8000_0000_0000_0001L, 0x8000_0000_0000_0000L));
		if (Integer.parseInt(System.getProperty("java.version").replaceFirst("^1\\.", "").replaceFirst("\\D.*", "")) > 8)
			System.out.printf("%X%n", JasonWriter.umulHigh9(0x8000_0000_0000_0001L, 0x8000_0000_0000_0000L));
	}

	public static void test13() throws ReflectiveOperationException {
		C c = new C();
		A a = new A();
		a.a = 123;
		c.m.put(a, 456);
		String j = JasonWriter.local().clear().write(c).toString();
		System.out.println(j);
		c.m.clear();
		JasonReader.local().buf(j).parse(c);
		Map.Entry<A, Integer> e = c.m.entrySet().iterator().next();
		System.out.println(e.getKey().a);
		System.out.println(e.getValue());
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
		test7();
		test8();
		test9();
		test10();
		test11();
		test12();
		test13();
	}
}

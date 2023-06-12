package jason;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TestJson5 {
	private static int checkCount = 0;

	private static void checkObj(String json5, String resJson) throws ReflectiveOperationException {
		final Map<String, Object> map = JsonReader.local().buf(json5).parseMap(new LinkedHashMap<>());
		final String res = JsonWriter.local().clear().setFlags(0x10_0000).setNoQuoteKey(true).write(map).toString();
		if (!res.equals(resJson))
			throw new AssertionError("unmatched: " + json5 + " <---> " + res);
		checkCount++;
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		checkObj("{'a':1}", "{a:1}");
		checkObj("{a:1}", "{a:1}");
		checkObj("{$_:1,_$:2,a\\u200C:3}", "{$_:1,_$:2,a\u200C:3}");
		checkObj("{中文key\\0:9}", "{中文key\\u0000:9}");
		checkObj("{\\u0061\\u0062:1,\\u0024\\u005F:2,\\u005F\\u0024:3}", "{ab:1,$_:2,_$:3}");
		checkObj("{abc:1,def:2,,,}", "{abc:1,def:2}");
		checkObj("{a:{b:2,},}", "{a:{b:2}}");
		checkObj("{a:+5,b:+1.23e,c:+1.23e+1,d:-.5,e:-5.,f:-0.,g:-.,h:.}", "{a:5,b:1.23,c:12.3,d:-0.5,e:-5.0,f:-0.0,g:-0.0,h:0.0}");
		checkObj("{a:Infinity,b:-infinity,c:Inf,d:NaN,e:nan}", "{a:Infinity,b:-Infinity,c:Infinity,d:NaN,e:NaN}");
		System.out.println(TestJson5.class.getSimpleName() + ": " + checkCount + " TESTS OK!");
	}
}

package jason;

import java.nio.charset.StandardCharsets;

public final class TestStringParser {
	public static void main(String[] args) throws ReflectiveOperationException {
		String[] strs = { "\"\"", "\"abc\"", "\"abcdefghijklmn\"", "\"\\\"\\\\\\/\\b\\f\\n\\r\\t\"",
				"\"\\u0123\\u4567\\u89ab\\ucdef\"", "\"opqrst\\r\\nuvwxyz\"", "\"《汉字》\"",
				"\"\\u0030\\'\\uABCD\\u00EF\"" };
		String[] chks = { "", "abc", "abcdefghijklmn", "\"\\/\b\f\n\r\t", "\u0123\u4567\u89ab\ucdef",
				"opqrst\r\nuvwxyz", "《汉字》", "\u0030'\uABCD\u00EF" };
		byte[][] tests = new byte[8][];
		for (int i = 0; i < 8; i++)
			tests[i] = strs[i].getBytes(StandardCharsets.UTF_8);

		JasonReader jason = new JasonReader();
		for (int j = 0; j < 8; j++) {
			String s = jason.buf(tests[j]).parseString();
			if (s == null)
				throw new RuntimeException();
			if (!s.equals(chks[j])) {
				System.err.println("ERROR! j=" + j + ':');
				for (int i = 0; i < s.length(); i++)
					System.out.format(" %4X", (int) s.charAt(i));
				System.out.println();
				for (int i = 0; i < chks[j].length(); i++)
					System.out.format(" %4X", (int) chks[j].charAt(i));
				System.out.println();
				return;
			}
		}
		long r = 0;
		for (int i = 0; i < 2_000_000; i++)
			for (int j = 0; j < 8; j++) {
				String s = jason.buf(tests[j]).parseString();
				if (s == null)
					throw new RuntimeException();
				r += s.length();
			}
		System.out.println(r); // 102000000
	}
}

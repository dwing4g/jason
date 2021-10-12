package jason;

public final class JasonToken {
	public static final int TYPE_SHIFT = 30;
	public static final int TYPE_PRIMITIVE = 0; // number, boolean(true/false) or null
	public static final int TYPE_STRING = 1 << TYPE_SHIFT;
	public static final int TYPE_ARRAY = 2 << TYPE_SHIFT;
	public static final int TYPE_OBJECT = 3 << TYPE_SHIFT;
	public static final int TYPE_MASK = 3 << TYPE_SHIFT;
	public static final int SIZE_MASK = ~TYPE_MASK;

	/**
	 * Reference: original C implement: https://github.com/zserge/jsmn
	 *
	 * @param in    json string (readonly byte array)
	 * @param inPos start offset from json array
	 * @param inLen length from json array offset
	 * @param out   output: {type|size, start, end, parent}*
	 * @return updated outPos(>=0) or -1-outPos(<0) for error
	 */
	public static int parse(final byte[] in, int inPos, final int inLen, final int[] out) {
		int outPos = 0, parentPos = -1;
		for (final int inEnd = inPos + inLen; inPos < inEnd; inPos++) {
			final int c = in[inPos];
			switch (c) {
			case '{':
			case '[':
				out[outPos] = c == '{' ? TYPE_OBJECT : TYPE_ARRAY;
				out[outPos + 1] = inPos;
				out[outPos + 2] = -1;
				out[outPos + 3] = parentPos;
				if (parentPos >= 0)
					out[parentPos]++;
				parentPos = outPos;
				outPos += 4;
				continue;
			case '}':
			case ']':
				final int type = c == '}' ? TYPE_OBJECT : TYPE_ARRAY;
				if (outPos <= 0)
					return -1;
				for (int pos = outPos - 4; ; pos = out[pos + 3]) {
					if (out[pos + 2] < 0) {
						if ((out[pos] & TYPE_MASK) != type)
							return -1 - outPos;
						out[pos + 2] = inPos + 1;
						parentPos = out[pos + 3];
						break;
					}
					if (out[pos + 3] < 0) {
						if ((out[pos] & TYPE_MASK) != type || parentPos < 0)
							return -1 - outPos;
						break;
					}
				}
				continue;
			case '\'':
			case '"':
				out[outPos] = TYPE_STRING;
				out[outPos + 1] = ++inPos;
				for (; inPos < inEnd; inPos++) {
					final int d = in[inPos];
					if (d == c)
						break;
					if (d == '\\' && inPos + 1 < inEnd)
						inPos++;
				}
				out[outPos + 2] = inPos;
				out[outPos + 3] = parentPos;
				outPos += 4;
				if (parentPos >= 0)
					out[parentPos]++;
				continue;
			case '\t':
			case '\r':
			case '\n':
			case ' ':
				continue;
			case ':':
				parentPos = outPos - 4;
				continue;
			case ',':
				if (parentPos >= 0 && out[parentPos] >= 0) // != ARRAY && != OBJECT
					parentPos = out[parentPos + 3];
				continue;
			}
			out[outPos] = TYPE_PRIMITIVE;
			out[outPos + 1] = inPos;
			end:
			for (; inPos < inEnd; inPos++) {
				switch (in[inPos]) {
				case '\t':
				case '\r':
				case '\n':
				case ' ':
				case ':':
				case ',':
				case ']':
				case '}':
					break end;
				}
			}
			out[outPos + 2] = inPos--;
			out[outPos + 3] = parentPos;
			outPos += 4;
			if (parentPos >= 0)
				out[parentPos]++;
		}
//		for (int pos = outPos - 4; pos >= 0; pos -= 4)
//			if (out[pos + 2] < 0)
//				return ERROR_PART;
		return outPos;
	}

	private JasonToken() {
	}

	public static void main(String[] args) {
		String json = "{a:12,\"b\":{\"c\":[3,4],\"d\":\"测试\"},'ef':{}}";
		final byte[] jsonBytes = json.getBytes();
		final int[] tokens = new int[1024];
		final int n = parse(jsonBytes, 0, jsonBytes.length, tokens);
		System.out.println(n + ": " + jsonBytes.length + "/" + json.length());
		for (int i = 0; i < n; i += 4) {
			System.out.printf("%2d: %d[%d]: %2d-%2d %2d: %s%n",
					i, tokens[i] >>> TYPE_SHIFT, tokens[i] & SIZE_MASK, tokens[i + 1], tokens[i + 2], tokens[i + 3],
					new String(jsonBytes, tokens[i + 1], tokens[i + 2] - tokens[i + 1]));
		}
		System.out.println("===");
	}
}

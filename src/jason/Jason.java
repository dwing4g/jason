package jason;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import sun.misc.Unsafe; //NOSONAR

// Compile with JDK9+; Run with JDK8+ (JDK9+ is recommended); Android is NOT supported
public final class Jason {
	private static final int TYPE_BOOLEAN = 1; // boolean Boolean
	private static final int TYPE_BYTE = 2; // byte Byte
	private static final int TYPE_SHORT = 3; // short Short
	private static final int TYPE_CHAR = 4; // char Character
	private static final int TYPE_INT = 5; // int Integer
	private static final int TYPE_LONG = 6; // long Long
	private static final int TYPE_FLOAT = 7; // float Float
	private static final int TYPE_DOUBLE = 8; // double Double
	private static final int TYPE_STRING = 9; // String
	private static final int TYPE_OBJECT = 10; // Object(null,Boolean,Integer,Long,Double,String,ArrayList<>,HashMap<>)
	private static final int TYPE_POS = 11; // Pos(pos)
	private static final int TYPE_CUSTOM = 12; // other custom type
	private static final int TYPE_WRAP_FLAG = 0x10; // wrap<1~8>
	private static final int TYPE_LIST_FLAG = 0x20; // Collection<1~12>
	private static final int TYPE_MAP_FLAG = 0x30; // Map<String, 1~12>

	private static final int KEY_HASH_MULTIPLIER = 0x100_0193; // 1677_7619

	private static final Double NEGATIVE_INFINITY = Double.valueOf(Double.NEGATIVE_INFINITY);
	private static final Double POSITIVE_INFINITY = Double.valueOf(Double.POSITIVE_INFINITY);

	private static final byte[] ESCAPE = { // @formatter:off
			' ', '!', '"', '#', '$', '%', '&','\'', '(', ')', '*', '+', ',', '-', '.', '/', // 0x2x
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', // 0x3x
			'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', // 0x4x
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[','\\', ']', '^', '_', // 0x5x
			'`', 'a','\b', 'c', 'd', 'e','\f', 'g', 'h', 'i', 'j', 'k', 'l', 'm','\n', 'o', // 0x6x
			'p', 'q','\r', 's','\t', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', '?', // 0x7x
	}; //@formatter:on

	private static final double[] EXP = { // 1e0...1e308: 309 * 8 bytes = 2472 bytes
			1e+0, 1e+1, 1e+2, 1e+3, 1e+4, 1e+5, 1e+6, 1e+7, 1e+8, 1e+9, 1e+10, 1e+11, 1e+12, 1e+13, 1e+14, 1e+15, 1e+16,
			1e+17, 1e+18, 1e+19, 1e+20, 1e+21, 1e+22, 1e+23, 1e+24, 1e+25, 1e+26, 1e+27, 1e+28, 1e+29, 1e+30, 1e+31,
			1e+32, 1e+33, 1e+34, 1e+35, 1e+36, 1e+37, 1e+38, 1e+39, 1e+40, 1e+41, 1e+42, 1e+43, 1e+44, 1e+45, 1e+46,
			1e+47, 1e+48, 1e+49, 1e+50, 1e+51, 1e+52, 1e+53, 1e+54, 1e+55, 1e+56, 1e+57, 1e+58, 1e+59, 1e+60, 1e+61,
			1e+62, 1e+63, 1e+64, 1e+65, 1e+66, 1e+67, 1e+68, 1e+69, 1e+70, 1e+71, 1e+72, 1e+73, 1e+74, 1e+75, 1e+76,
			1e+77, 1e+78, 1e+79, 1e+80, 1e+81, 1e+82, 1e+83, 1e+84, 1e+85, 1e+86, 1e+87, 1e+88, 1e+89, 1e+90, 1e+91,
			1e+92, 1e+93, 1e+94, 1e+95, 1e+96, 1e+97, 1e+98, 1e+99, 1e+100, 1e+101, 1e+102, 1e+103, 1e+104, 1e+105,
			1e+106, 1e+107, 1e+108, 1e+109, 1e+110, 1e+111, 1e+112, 1e+113, 1e+114, 1e+115, 1e+116, 1e+117, 1e+118,
			1e+119, 1e+120, 1e+121, 1e+122, 1e+123, 1e+124, 1e+125, 1e+126, 1e+127, 1e+128, 1e+129, 1e+130, 1e+131,
			1e+132, 1e+133, 1e+134, 1e+135, 1e+136, 1e+137, 1e+138, 1e+139, 1e+140, 1e+141, 1e+142, 1e+143, 1e+144,
			1e+145, 1e+146, 1e+147, 1e+148, 1e+149, 1e+150, 1e+151, 1e+152, 1e+153, 1e+154, 1e+155, 1e+156, 1e+157,
			1e+158, 1e+159, 1e+160, 1e+161, 1e+162, 1e+163, 1e+164, 1e+165, 1e+166, 1e+167, 1e+168, 1e+169, 1e+170,
			1e+171, 1e+172, 1e+173, 1e+174, 1e+175, 1e+176, 1e+177, 1e+178, 1e+179, 1e+180, 1e+181, 1e+182, 1e+183,
			1e+184, 1e+185, 1e+186, 1e+187, 1e+188, 1e+189, 1e+190, 1e+191, 1e+192, 1e+193, 1e+194, 1e+195, 1e+196,
			1e+197, 1e+198, 1e+199, 1e+200, 1e+201, 1e+202, 1e+203, 1e+204, 1e+205, 1e+206, 1e+207, 1e+208, 1e+209,
			1e+210, 1e+211, 1e+212, 1e+213, 1e+214, 1e+215, 1e+216, 1e+217, 1e+218, 1e+219, 1e+220, 1e+221, 1e+222,
			1e+223, 1e+224, 1e+225, 1e+226, 1e+227, 1e+228, 1e+229, 1e+230, 1e+231, 1e+232, 1e+233, 1e+234, 1e+235,
			1e+236, 1e+237, 1e+238, 1e+239, 1e+240, 1e+241, 1e+242, 1e+243, 1e+244, 1e+245, 1e+246, 1e+247, 1e+248,
			1e+249, 1e+250, 1e+251, 1e+252, 1e+253, 1e+254, 1e+255, 1e+256, 1e+257, 1e+258, 1e+259, 1e+260, 1e+261,
			1e+262, 1e+263, 1e+264, 1e+265, 1e+266, 1e+267, 1e+268, 1e+269, 1e+270, 1e+271, 1e+272, 1e+273, 1e+274,
			1e+275, 1e+276, 1e+277, 1e+278, 1e+279, 1e+280, 1e+281, 1e+282, 1e+283, 1e+284, 1e+285, 1e+286, 1e+287,
			1e+288, 1e+289, 1e+290, 1e+291, 1e+292, 1e+293, 1e+294, 1e+295, 1e+296, 1e+297, 1e+298, 1e+299, 1e+300,
			1e+301, 1e+302, 1e+303, 1e+304, 1e+305, 1e+306, 1e+307, 1e+308 };

	public interface Identifier {
		ClassMeta identify(Jason jason);
	}

	public static class Pos {
		public int pos;

		public Pos() {
		}

		public Pos(int p) {
			pos = p;
		}
	}

	static final class FieldMeta {
		final int type;
		final long offset;
		final String name; // field name
		final Class<?> klass; // TYPE_OBJ:fieldClass; TYPE_LIST_FLAG/TYPE_MAP_FLAG:subValueClass
		ClassMeta classMeta; // from klass, lazy assigned

		FieldMeta(int type, long offset, String name, Class<?> klass) {
			this.type = type;
			this.offset = offset;
			this.name = name;
			this.klass = klass;
		}
	}

	static class FieldMetaMap {
		private static final int PRIME2 = 0xbe1f14b1;
		private static final int PRIME3 = 0xb4b82e39;
		private static final float LOAD_FACTOR = 0.5f;
		private static final long ZERO_KEY = 0;
		private int mask; // [0,0x3fff_ffff]
		private long[] keyTable;
		private FieldMeta[] valueTable;
		private FieldMeta zeroValue;
		private boolean hasZeroValue;
		private int pushIterations; // [1,2,4,8,11,16,22,...,4096]
		private int hashShift; // [0,1,...30]
		private int size;
		private int capacity; // [1,2,4,8,...,0x4000_0000]
		private int tableSize; // capacity + [0,stashSize]
		private int threshold; // [1,0x4000_0000]

		FieldMetaMap() {
			int initialCapacity = 16;
			mask = initialCapacity - 1;
			pushIterations = Math.max(Math.min(initialCapacity, 8), (int) Math.sqrt(initialCapacity) >> 3);
			hashShift = 31 - Integer.numberOfTrailingZeros(initialCapacity);
			capacity = tableSize = initialCapacity;
			threshold = (int) Math.ceil(initialCapacity * LOAD_FACTOR);
			initialCapacity += (int) Math.ceil(Math.log(initialCapacity)) * 2;
			keyTable = new long[initialCapacity];
			valueTable = new FieldMeta[initialCapacity];
		}

		int hash2(long h64) {
			h64 *= PRIME2;
			int h = (int) (h64 ^ (h64 >> 32));
			return (h ^ (h >>> hashShift)) & mask;
		}

		int hash3(long h64) {
			h64 *= PRIME3;
			int h = (int) (h64 ^ (h64 >> 32));
			return (h ^ (h >>> hashShift)) & mask;
		}

		FieldMeta get(long key) {
			if (key == ZERO_KEY)
				return hasZeroValue ? zeroValue : null;
			long[] kt = keyTable;
			int idx = (int) key & mask;
			if (kt[idx] != key) {
				idx = hash2(key);
				if (kt[idx] != key) {
					idx = hash3(key);
					if (kt[idx] != key) {
						for (int i = capacity, n = tableSize; i < n; i++)
							if (kt[i] == key)
								return valueTable[i];
						return null;
					}
				}
			}
			return valueTable[idx];
		}

		FieldMeta put(long key, FieldMeta value) {
			if (key == ZERO_KEY) {
				FieldMeta oldValue = zeroValue;
				zeroValue = value;
				if (!hasZeroValue) {
					hasZeroValue = true;
					size++;
				}
				return oldValue;
			}

			long[] kt = keyTable;
			FieldMeta[] vt = valueTable;
			int idx1 = (int) key & mask;
			long key1 = kt[idx1];
			if (key1 == key) {
				FieldMeta oldValue = vt[idx1];
				vt[idx1] = value;
				return oldValue;
			}
			int idx2 = hash2(key);
			long key2 = kt[idx2];
			if (key2 == key) {
				FieldMeta oldValue = vt[idx2];
				vt[idx2] = value;
				return oldValue;
			}
			int idx3 = hash3(key);
			long key3 = kt[idx3];
			if (key3 == key) {
				FieldMeta oldValue = vt[idx3];
				vt[idx3] = value;
				return oldValue;
			}

			for (int i = capacity, n = tableSize; i < n; i++) {
				if (kt[i] == key) {
					FieldMeta oldValue = vt[i];
					vt[i] = value;
					return oldValue;
				}
			}

			if (key1 == ZERO_KEY) {
				kt[idx1] = key;
				vt[idx1] = value;
				size++;
				return null;
			}
			if (key2 == ZERO_KEY) {
				kt[idx2] = key;
				vt[idx2] = value;
				size++;
				return null;
			}
			if (key3 == ZERO_KEY) {
				kt[idx3] = key;
				vt[idx3] = value;
				size++;
				return null;
			}

			if (size >= threshold) {
				resize(capacity << 1);
				return put(key, value);
			}
			if (push(key, value, idx1, key1, idx2, key2, idx3, key3))
				size++;
			return null;
		}

		boolean push(long newKey, FieldMeta newValue, int idx1, long key1, int idx2, long key2, int idx3, long key3) {
			long[] kt = keyTable;
			FieldMeta[] vt = valueTable;
			int m = mask;
			long evictedKey;
			FieldMeta evictedValue;
			ThreadLocalRandom rand = ThreadLocalRandom.current();
			for (int i = 0, pis = pushIterations;;) {
				switch (rand.nextInt(3)) {
				case 0:
					evictedKey = key1;
					evictedValue = vt[idx1];
					kt[idx1] = newKey;
					vt[idx1] = newValue;
					break;
				case 1:
					evictedKey = key2;
					evictedValue = vt[idx2];
					kt[idx2] = newKey;
					vt[idx2] = newValue;
					break;
				default:
					evictedKey = key3;
					evictedValue = vt[idx3];
					kt[idx3] = newKey;
					vt[idx3] = newValue;
					break;
				}

				idx1 = (int) evictedKey & m;
				key1 = kt[idx1];
				if (key1 == ZERO_KEY) {
					kt[idx1] = evictedKey;
					vt[idx1] = evictedValue;
					return true;
				}
				idx2 = hash2(evictedKey);
				key2 = kt[idx2];
				if (key2 == ZERO_KEY) {
					kt[idx2] = evictedKey;
					vt[idx2] = evictedValue;
					return true;
				}
				idx3 = hash3(evictedKey);
				key3 = kt[idx3];
				if (key3 == ZERO_KEY) {
					kt[idx3] = evictedKey;
					vt[idx3] = evictedValue;
					return true;
				}
				if (++i == pis)
					break;
				newKey = evictedKey;
				newValue = evictedValue;
			}

			if (tableSize == kt.length) {
				resize(capacity << 1);
				put(evictedKey, evictedValue);
				return false;
			}
			int idx = tableSize++;
			kt[idx] = evictedKey;
			vt[idx] = evictedValue;
			return true;
		}

		void resize(int newCapacity) // [1,2,4,8,...,0x4000_0000]
		{
			int oldEndIndex = tableSize;
			mask = newCapacity - 1;
			pushIterations = Math.max(Math.min(newCapacity, 8), (int) Math.sqrt(newCapacity) >> 3);
			hashShift = 31 - Integer.numberOfTrailingZeros(newCapacity);
			capacity = tableSize = newCapacity;
			threshold = (int) Math.ceil(newCapacity * LOAD_FACTOR);
			newCapacity += (int) Math.ceil(Math.log(newCapacity)) * 2;
			long[] oldKeyTable = keyTable;
			FieldMeta[] oldValueTable = valueTable;
			long[] kt = new long[newCapacity];
			FieldMeta[] vt = new FieldMeta[newCapacity];
			keyTable = kt;
			valueTable = vt;

			if (size <= (hasZeroValue ? 1 : 0))
				return;
			for (int i = 0; i < oldEndIndex; i++) {
				long key = oldKeyTable[i];
				if (key == ZERO_KEY)
					continue;
				FieldMeta value = oldValueTable[i];
				int idx1 = (int) key & mask;
				long key1 = kt[idx1];
				if (key1 == ZERO_KEY) {
					kt[idx1] = key;
					vt[idx1] = value;
					continue;
				}
				int idx2 = hash2(key);
				long key2 = kt[idx2];
				if (key2 == ZERO_KEY) {
					kt[idx2] = key;
					vt[idx2] = value;
					continue;
				}
				int idx3 = hash3(key);
				long key3 = kt[idx3];
				if (key3 == ZERO_KEY) {
					kt[idx3] = key;
					vt[idx3] = value;
					continue;
				}
				push(key, value, idx1, key1, idx2, key2, idx3, key3);
				kt = keyTable;
				vt = valueTable;
			}
		}
	}

	public static final class ClassMeta extends FieldMetaMap {
		private static final HashMap<Class<?>, Integer> typeMap = new HashMap<>(32);
		final Class<?> klass;
		final Constructor<?> ctor;
		private final FieldMeta[] fieldMetas;
		Identifier identifier;

		static {
			typeMap.put(boolean.class, TYPE_BOOLEAN);
			typeMap.put(byte.class, TYPE_BYTE);
			typeMap.put(short.class, TYPE_SHORT);
			typeMap.put(char.class, TYPE_CHAR);
			typeMap.put(int.class, TYPE_INT);
			typeMap.put(long.class, TYPE_LONG);
			typeMap.put(float.class, TYPE_FLOAT);
			typeMap.put(double.class, TYPE_DOUBLE);
			typeMap.put(String.class, TYPE_STRING);
			typeMap.put(Object.class, TYPE_OBJECT);
			typeMap.put(Pos.class, TYPE_POS);
			typeMap.put(Boolean.class, TYPE_WRAP_FLAG + TYPE_BOOLEAN);
			typeMap.put(Byte.class, TYPE_WRAP_FLAG + TYPE_BYTE);
			typeMap.put(Short.class, TYPE_WRAP_FLAG + TYPE_SHORT);
			typeMap.put(Character.class, TYPE_WRAP_FLAG + TYPE_CHAR);
			typeMap.put(Integer.class, TYPE_WRAP_FLAG + TYPE_INT);
			typeMap.put(Long.class, TYPE_WRAP_FLAG + TYPE_LONG);
			typeMap.put(Float.class, TYPE_WRAP_FLAG + TYPE_FLOAT);
			typeMap.put(Double.class, TYPE_WRAP_FLAG + TYPE_DOUBLE);
		}

		ClassMeta(Class<?> klass) {
			this.klass = klass;
			Constructor<?> ct = null;
			for (Constructor<?> c : klass.getDeclaredConstructors()) {
				if (c.getParameterCount() == 0) {
					try {
						c.setAccessible(true);
						ct = c;
					} catch (Exception e) {
					}
					break;
				}
			}
			ctor = ct;
			ArrayList<Class<?>> classes = new ArrayList<>(2);
			ArrayList<FieldMeta> fieldMetaList = new ArrayList<>();
			for (; klass != Object.class; klass = klass.getSuperclass())
				classes.add(klass);
			for (int i = classes.size() - 1; i >= 0; i--) {
				klass = classes.get(i);
				for (Field field : klass.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()))
						continue;
					Class<?> fieldClass = field.getType();
					Integer v = typeMap.get(fieldClass);
					int type;
					if (v != null)
						type = v;
					else if (Collection.class.isAssignableFrom(fieldClass)) {
						Type geneType = field.getGenericType();
						if (geneType instanceof ParameterizedType) {
							Type[] geneTypes = ((ParameterizedType) geneType).getActualTypeArguments();
							if (geneTypes.length == 1 && (geneType = geneTypes[0]) instanceof Class) {
								v = typeMap.get(fieldClass = (Class<?>) geneType);
								type = TYPE_LIST_FLAG + (v != null ? v & 0xf : TYPE_CUSTOM);
							} else
								continue;
						} else
							continue;
					} else if (Map.class.isAssignableFrom(fieldClass)) {
						Type geneType = field.getGenericType();
						if (geneType instanceof ParameterizedType) {
							Type[] geneTypes = ((ParameterizedType) geneType).getActualTypeArguments();
							if (geneTypes.length == 2 && geneTypes[0] == String.class
									&& (geneType = geneTypes[1]) instanceof Class) {
								v = typeMap.get(fieldClass = (Class<?>) geneType);
								type = TYPE_MAP_FLAG + (v != null ? v & 0xf : TYPE_CUSTOM);
							} else
								continue;
						} else
							continue;
					} else
						type = TYPE_CUSTOM;
					String name = field.getName();
					FieldMeta fieldMeta = new FieldMeta(type, getUnsafe().objectFieldOffset(field), name, fieldClass);
					FieldMeta oldFieldMeta = put(getKeyHash(name), fieldMeta);
					if (oldFieldMeta != null)
						throw new IllegalStateException("conflicted field name: " + oldFieldMeta.name + " & " + name);
					fieldMetaList.add(fieldMeta);
				}
			}
			fieldMetas = fieldMetaList.toArray(new FieldMeta[fieldMetaList.size()]);
		}

		public void setIdentifier(Identifier iden) {
			identifier = iden;
		}

		ClassMeta identifyClassMeta(Jason jason) {
			return identifier != null ? identifier.identify(jason) : this;
		}

		int size() {
			return fieldMetas.length;
		}

		FieldMeta get(int idx) {
			return fieldMetas[idx];
		}
	}

	private static final Unsafe unsafe;
	private static final long STRING_VALUE_OFFSET;
	private static final ThreadLocal<Jason> jsonParserLocals = ThreadLocal.withInitial(Jason::new);
	private static final ConcurrentHashMap<Class<?>, ClassMeta> classMetas = new ConcurrentHashMap<>();
	private static String[] poolStrs;
	private static int poolSize = 1024;

	static {
		try {
			Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			unsafe = (Unsafe) theUnsafeField.get(null);
			Field valueField = String.class.getDeclaredField("value");
			STRING_VALUE_OFFSET = valueField.getType() == byte[].class ? unsafe.objectFieldOffset(valueField) : 0;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Unsafe getUnsafe() {
		return unsafe;
	}

	@SuppressWarnings("unchecked")
	public static <T> T allocObj(ClassMeta classMeta) throws ReflectiveOperationException {
		Constructor<?> ctor = classMeta.ctor;
		return (T) (ctor != null ? ctor.newInstance((Object[]) null) : unsafe.allocateInstance(classMeta.klass));
	}

	public static Jason local() {
		return jsonParserLocals.get();
	}

	public static void removeLocal() {
		jsonParserLocals.remove();
	}

	public static ClassMeta getClassMeta(Class<?> klass) {
		return classMetas.computeIfAbsent(klass, ClassMeta::new);
	}

	public static void resetStringPool(int size) {
		poolSize = 1 << (32 - Integer.numberOfLeadingZeros(size - 1));
		poolStrs = null;
	}

	public static void clearStringPool() {
		String[] ss = poolStrs;
		if (ss != null)
			Arrays.fill(ss, null);
	}

	static String intern(byte[] buf, int pos, int end) throws ReflectiveOperationException {
		int len = end - pos;
		String[] ss = poolStrs;
		if (ss == null)
			poolStrs = ss = new String[poolSize];
		int idx = (int) getKeyHash(buf, pos, end) & (ss.length - 1);
		String s = ss[idx];
		if (s != null) {
			if (STRING_VALUE_OFFSET != 0) { // JDK9+
				byte[] b = (byte[]) unsafe.getObject(s, STRING_VALUE_OFFSET);
				if (b.length == len && Arrays.equals(b, 0, len, buf, pos, end))
					return s;
			} else { // for JDK8-
				int n = s.length();
				if (n == len) {
					for (int i = 0;; i++) {
						if (i == n)
							return s;
						if (s.charAt(i) != (buf[pos + i] & 0xff))
							break;
					}
				}
			}
		}
		ss[idx] = s = newByteString(buf, pos, end);
		return s;
	}

	private byte[] buf;
	private int pos;
	private char[] tmp; // for parseString & parseStringNoQuot

	public Jason() {
	}

	public Jason(byte[] b) {
		buf = b;
	}

	public Jason(byte[] b, int p) {
		buf = b;
		pos = p;
	}

	public Jason reset() {
		buf = null;
		pos = 0;
		tmp = null;
		return this;
	}

	public byte[] buf() {
		return buf;
	}

	public Jason buf(byte[] b) {
		buf = b;
		pos = 0;
		return this;
	}

	public Jason buf(byte[] b, int p) {
		buf = b;
		pos = p;
		return this;
	}

	public int pos() {
		return pos;
	}

	public Jason pos(int p) {
		pos = p;
		return this;
	}

	public Jason pos(Pos p) {
		pos = p.pos;
		return this;
	}

	public char[] tmp() {
		return tmp;
	}

	public Jason tmp(char[] t) {
		tmp = t;
		return this;
	}

	public Jason tmp(int size) {
		if (tmp == null || tmp.length < size)
			tmp = new char[size];
		return this;
	}

	public Jason skip(int n) {
		pos += n;
		return this;
	}

	public boolean end() {
		return pos >= buf.length;
	}

	public int next() {
		for (int b;; pos++)
			if ((b = buf[pos] & 0xff) > ' ')
				return b;
	}

	public int skipNext() {
		for (int b;;)
			if ((b = buf[++pos] & 0xff) > ' ')
				return b;
	}

	public int skipVar() {
		for (int b, c;;) {
			if ((b = buf[pos]) == ',')
				for (;;)
					if ((b = buf[++pos] & 0xff) > ' ' && b != ',')
						return b;
			if ((c = b | 0x20) == '}') // ]:0x5D | 0x20 = }:0x7D
				return b;
			pos++;
			if (b < '"')
				continue;
			if (b == '"') {
				while ((b = buf[pos++]) != '"')
					if (b == '\\')
						pos++;
			} else if (c == '{') { // [:0x5B | 0x20 = {:0x7B
				for (int level = 0; (b = buf[pos++] | 0x20) != '}' || --level >= 0;) { // ]:0x5D | 0x20 = }:0x7D
					if (b == '"') {
						while ((b = buf[pos++]) != '"')
							if (b == '\\')
								pos++;
					} else if (b == '{') // [:0x5B | 0x20 = {:0x7B
						level++;
				}
			}
		}
	}

	public Object parse() throws ReflectiveOperationException {
		return parse(null, next());
	}

	@SuppressWarnings("unchecked")
	Object parse(Object obj, int b) throws ReflectiveOperationException {
		switch (b) { //@formatter:off
		case '{': return parseMap0(obj instanceof Map ? (Map<String, Object>) obj : null);
		case '[': return parseArray0(obj instanceof Collection ? (Collection<Object>) obj : null);
		case '"': return parseString(false);
		case '0': case '1': case '2': case '3': case '4': case '5':
		case '6': case '7': case '8': case '9': case '-': case '.': return parseNumber();
		case 'f': return false;
		case 't': return true;
		} //@formatter:on
		return null;
	}

	public Collection<Object> parseArray(Collection<Object> c) throws ReflectiveOperationException {
		return next() == '[' ? parseArray0(c) : c;
	}

	Collection<Object> parseArray0(Collection<Object> c) throws ReflectiveOperationException {
		if (c == null)
			c = new ArrayList<>();
		for (int b = skipNext(); b != ']'; b = skipVar())
			c.add(parse(null, b));
		pos++;
		return c;
	}

	public Map<String, Object> parseMap(Map<String, Object> m) throws ReflectiveOperationException {
		return next() == '{' ? parseMap0(m) : m;
	}

	Map<String, Object> parseMap0(Map<String, Object> m) throws ReflectiveOperationException {
		if (m == null)
			m = new HashMap<>();
		for (int b = skipNext(); b != '}'; b = skipVar()) {
			String k = parseStringKey(b);
			if ((b = next()) == ':')
				b = skipNext();
			m.put(k, parse(null, b));
		}
		pos++;
		return m;
	}

	public <T> T parse(Class<T> klass) throws ReflectiveOperationException {
		if (klass == null || next() != '{')
			return null;
		ClassMeta classMeta = getClassMeta(klass).identifyClassMeta(this);
		return parse0(allocObj(classMeta), classMeta);
	}

	public <T> T parse(ClassMeta classMeta) throws ReflectiveOperationException {
		if (classMeta == null || next() != '{')
			return null;
		classMeta = classMeta.identifyClassMeta(this);
		return parse0(allocObj(classMeta), classMeta);
	}

	public <T> T parse(T obj) throws ReflectiveOperationException {
		return obj == null || next() != '{' ? obj : parse0(obj, getClassMeta(obj.getClass()));
	}

	public <T> T parse(T obj, ClassMeta classMeta) throws ReflectiveOperationException {
		return obj == null || next() != '{' ? obj // fastest but dangerous if obj and classMeta are not matched
				: parse0(obj, classMeta != null ? classMeta : getClassMeta(obj.getClass()));
	}

	<T> T parse0(T obj, ClassMeta classMeta) throws ReflectiveOperationException {
		for (int b = skipNext(); b != '}'; b = skipVar()) {
			FieldMeta fm = classMeta.get(b == '"' ? parseKeyHash() : parseKeyHashNoQuot(b));
			if (fm == null)
				continue;
			if ((b = next()) == ':')
				b = skipNext();
			long offset = fm.offset;
			int type = fm.type;
			switch (type) {
			case TYPE_BOOLEAN:
				unsafe.putBoolean(obj, offset, b == 't');
				break;
			case TYPE_BYTE:
				unsafe.putByte(obj, offset, (byte) parseInt());
				break;
			case TYPE_SHORT:
				unsafe.putShort(obj, offset, (short) parseInt());
				break;
			case TYPE_CHAR:
				unsafe.putChar(obj, offset, (char) parseInt());
				break;
			case TYPE_INT:
				unsafe.putInt(obj, offset, parseInt());
				break;
			case TYPE_LONG:
				unsafe.putLong(obj, offset, parseLong());
				break;
			case TYPE_FLOAT:
				unsafe.putFloat(obj, offset, (float) parseDouble());
				break;
			case TYPE_DOUBLE:
				unsafe.putDouble(obj, offset, parseDouble());
				break;
			case TYPE_STRING:
				unsafe.putObject(obj, offset, parseString(false));
				break;
			case TYPE_OBJECT:
				unsafe.putObject(obj, offset, parse(unsafe.getObject(obj, offset), b));
				break;
			case TYPE_POS:
				Pos p = (Pos) unsafe.getObject(obj, offset);
				if (p == null)
					unsafe.putObject(obj, offset, new Pos(pos));
				else
					p.pos = pos;
				break;
			case TYPE_CUSTOM:
				if (b != '{')
					unsafe.putObject(obj, offset, null);
				else {
					Object subObj = unsafe.getObject(obj, offset);
					if (subObj != null) {
						Class<?> subClass = subObj.getClass();
						ClassMeta subClassMeta;
						if (subClass == fm.klass) {
							subClassMeta = fm.classMeta;
							if (subClassMeta == null)
								fm.classMeta = subClassMeta = getClassMeta(subClass);
						} else
							subClassMeta = getClassMeta(subClass);
						parse0(subObj, subClassMeta);
					} else {
						ClassMeta subClassMeta = fm.classMeta;
						if (subClassMeta == null)
							fm.classMeta = subClassMeta = getClassMeta(fm.klass);
						subClassMeta = subClassMeta.identifyClassMeta(this);
						unsafe.putObject(obj, offset, parse0(allocObj(subClassMeta), subClassMeta));
					}
				}
				break;
			case TYPE_WRAP_FLAG + TYPE_BOOLEAN:
				unsafe.putObject(obj, offset, b == 'n' ? null : b == 't');
				break;
			case TYPE_WRAP_FLAG + TYPE_BYTE:
				unsafe.putObject(obj, offset, b == 'n' ? null : (byte) parseInt());
				break;
			case TYPE_WRAP_FLAG + TYPE_SHORT:
				unsafe.putObject(obj, offset, b == 'n' ? null : (short) parseInt());
				break;
			case TYPE_WRAP_FLAG + TYPE_CHAR:
				unsafe.putObject(obj, offset, b == 'n' ? null : (char) parseInt());
				break;
			case TYPE_WRAP_FLAG + TYPE_INT:
				unsafe.putObject(obj, offset, b == 'n' ? null : parseInt());
				break;
			case TYPE_WRAP_FLAG + TYPE_LONG:
				unsafe.putObject(obj, offset, b == 'n' ? null : parseLong());
				break;
			case TYPE_WRAP_FLAG + TYPE_FLOAT:
				unsafe.putObject(obj, offset, b == 'n' ? null : (float) parseDouble());
				break;
			case TYPE_WRAP_FLAG + TYPE_DOUBLE:
				unsafe.putObject(obj, offset, b == 'n' ? null : parseDouble());
				break;
			default:
				int flag = type & 0xf0;
				if (flag == TYPE_LIST_FLAG) {
					if (b != '[') {
						unsafe.putObject(obj, offset, null);
						break;
					}
					@SuppressWarnings("unchecked")
					Collection<Object> c = (Collection<Object>) unsafe.getObject(obj, offset);
					if (c == null)
						unsafe.putObject(obj, offset, c = new ArrayList<>());
					else
						c.clear();
					b = skipNext();
					switch (type & 0xf) {
					case TYPE_BOOLEAN:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : b == 't');
						break;
					case TYPE_BYTE:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : (byte) parseInt());
						break;
					case TYPE_SHORT:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : (short) parseInt());
						break;
					case TYPE_CHAR:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : (char) parseInt());
						break;
					case TYPE_INT:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : parseInt());
						break;
					case TYPE_LONG:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : parseLong());
						break;
					case TYPE_FLOAT:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : (float) parseDouble());
						break;
					case TYPE_DOUBLE:
						for (; b != ']'; b = skipVar())
							c.add(b == 'n' ? null : parseDouble());
						break;
					case TYPE_STRING:
						for (; b != ']'; b = skipVar())
							c.add(parseString(false));
						break;
					case TYPE_OBJECT:
						for (; b != ']'; b = skipVar())
							c.add(parse(null, b));
						break;
					case TYPE_POS:
						for (; b != ']'; b = skipVar())
							c.add(new Pos(pos));
						break;
					case TYPE_CUSTOM:
						ClassMeta subClassMeta = fm.classMeta;
						if (subClassMeta == null)
							fm.classMeta = subClassMeta = getClassMeta(fm.klass);
						Identifier identifier = subClassMeta.identifier;
						if (identifier == null) {
							for (; b != ']'; b = skipVar())
								c.add(b != '{' ? null : parse0(allocObj(subClassMeta), subClassMeta));
						} else {
							for (; b != ']'; b = skipVar()) {
								if (b != '{')
									c.add(null);
								else {
									subClassMeta = identifier.identify(this);
									c.add(parse0(allocObj(subClassMeta), subClassMeta));
								}
							}
						}
						break;
					}
					pos++;
				} else if (flag == TYPE_MAP_FLAG) {
					if (b != '{') {
						unsafe.putObject(obj, offset, null);
						break;
					}
					@SuppressWarnings("unchecked")
					Map<String, Object> m = (Map<String, Object>) unsafe.getObject(obj, offset);
					if (m == null)
						unsafe.putObject(obj, offset, m = new HashMap<>());
					else
						m.clear();
					b = skipNext();
					switch (type & 0xf) {
					case TYPE_BOOLEAN:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : b == 't');
						}
						break;
					case TYPE_BYTE:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : (byte) parseInt());
						}
						break;
					case TYPE_SHORT:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : (short) parseInt());
						}
						break;
					case TYPE_CHAR:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : (char) parseInt());
						}
						break;
					case TYPE_INT:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : parseInt());
						}
						break;
					case TYPE_LONG:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : parseLong());
						}
						break;
					case TYPE_FLOAT:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : (float) parseDouble());
						}
						break;
					case TYPE_DOUBLE:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : parseDouble());
						}
						break;
					case TYPE_STRING:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, b == 'n' ? null : parseString(false));
						}
						break;
					case TYPE_OBJECT:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if ((b = next()) == ':')
								b = skipNext();
							m.put(k, parse(null, b));
						}
						break;
					case TYPE_POS:
						for (; b != '}'; b = skipVar()) {
							String k = parseStringKey(b);
							if (next() == ':')
								skipNext();
							m.put(k, new Pos(pos));
						}
						break;
					case TYPE_CUSTOM:
						ClassMeta subClassMeta = fm.classMeta;
						if (subClassMeta == null)
							fm.classMeta = subClassMeta = getClassMeta(fm.klass);
						Identifier identifier = subClassMeta.identifier;
						if (identifier == null) {
							for (; b != '}'; b = skipVar()) {
								String k = parseStringKey(b);
								if ((b = next()) == ':')
									b = skipNext();
								m.put(k, b == 'n' ? null : parse0(allocObj(subClassMeta), subClassMeta));
							}
						} else {
							for (; b != '}'; b = skipVar()) {
								String k = parseStringKey(b);
								if ((b = next()) == ':')
									b = skipNext();
								if (b != '{')
									m.put(k, null);
								else {
									subClassMeta = identifier.identify(this);
									m.put(k, parse0(allocObj(subClassMeta), subClassMeta));
								}
							}
						}
						break;
					}
					pos++;
				}
			}
		}
		pos++;
		return obj;
	}

	public static long getKeyHash(String str) {
		byte[] buf = str.getBytes(StandardCharsets.UTF_8);
		int n = buf.length;
		if (n <= 0)
			return 0;
		long h = buf[0];
		for (int i = 1; i < n; i++)
			h = h * KEY_HASH_MULTIPLIER + buf[i];
		return h;
	}

	public static long getKeyHash(byte[] buf, int pos, int end) {
		if (pos >= end)
			return 0;
		long h = buf[pos];
		while (++pos < end)
			h = h * KEY_HASH_MULTIPLIER + buf[pos];
		return h;
	}

	long parseKeyHash() {
		pos++; // skip the first '"'
		int b = buf[pos++];
		if (b == '"')
			return 0;
		for (long h = b;; h = h * KEY_HASH_MULTIPLIER + b) {
			if (b == '\\')
				h = h * KEY_HASH_MULTIPLIER + buf[pos++];
			if ((b = buf[pos++]) == '"')
				return h;
		}
	}

	long parseKeyHashNoQuot(int b) {
		if (b == ':')
			return 0;
		for (long h = b;; h = h * KEY_HASH_MULTIPLIER + b) {
			if (b == '\\')
				h = h * KEY_HASH_MULTIPLIER + buf[++pos];
			if (((b = buf[++pos]) & 0xff) <= ' ' || b == ':')
				return h;
		}
	}

	String parseStringKey(int b) throws ReflectiveOperationException {
		return b == '"' ? parseString(true) : parseStringNoQuot();
	}

	static int parseHex(int b) {
		return b <= '9' ? b - '0' : (b | 0x20) - ('a' - 10); // A~F:0x4X | 0x20 = a~z:0x6X
	}

	static String newByteString(byte[] buf, int pos, int end) throws ReflectiveOperationException {
		if (STRING_VALUE_OFFSET == 0) // for JDK8-
			return new String(buf, pos, end - pos, StandardCharsets.ISO_8859_1);
		String str = (String) unsafe.allocateInstance(String.class);
		unsafe.putObject(str, STRING_VALUE_OFFSET, Arrays.copyOfRange(buf, pos, end)); // for JDK9+
		return str;
	}

	public String parseString() throws ReflectiveOperationException {
		return parseString(false);
	}

	public String parseString(boolean intern) throws ReflectiveOperationException {
		final byte[] buffer = buf;
		int p = pos, b;
		if (buffer[p] != '"')
			return null;
		final int begin = ++p;
		for (;; p++) {
			if ((b = buffer[p]) == '"') { // lucky! finished the fast path
				pos = p + 1;
				return intern ? intern(buffer, begin, p) : newByteString(buffer, begin, p);
			}
			if ((b ^ '\\') < 1) // '\\' or multibyte char
				break; // jump to the slow path below
		}
		int len = p - begin, n = len, c, d;
		for (; (b = buffer[p++]) != '"'; len++)
			if (b == '\\' && buffer[p++] == 'u')
				p += 4;
		char[] t = tmp;
		if (t == null || t.length < len)
			tmp = t = new char[len];
		p = begin;
		for (int i = 0; i < n;)
			t[i++] = (char) (buffer[p++] & 0xff);
		for (;;) {
			if ((b = buffer[p++]) == '"') {
				pos = p;
				return new String(t, 0, n);
			}
			if (b == '\\') {
				if ((b = buffer[p++]) == 'u') {
					t[n++] = (char) ((parseHex(buffer[p]) << 12) + (parseHex(buffer[p + 1]) << 8)
							+ (parseHex(buffer[p + 2]) << 4) + parseHex(buffer[p + 3]));
					p += 4;
				} else
					t[n++] = (char) (b >= 0x20 ? ESCAPE[b - 0x20] : b & 0xff);
			} else if (b >= 0)
				t[n++] = (char) b;
			else if (b >= -0x20) {
				if ((c = buffer[p]) < -0x40 && (d = buffer[p + 1]) < -0x40) {
					t[n++] = (char) (((b & 0xf) << 12) + ((c & 0x3f) << 6) + (d & 0x3f));
					p += 2;
				} else
					t[n++] = (char) (b & 0xff); // ignore malformed utf-8
			} else if ((c = buffer[p]) < -0x40) {
				p++;
				t[n++] = (char) (((b & 0x1f) << 6) + (c & 0x3f));
			} else
				t[n++] = (char) (b & 0xff); // ignore malformed utf-8
		}
	}

	public String parseStringNoQuot() throws ReflectiveOperationException {
		final byte[] buffer = buf;
		int p = pos, b;
		final int begin = p;
		for (;; p++) {
			if (((b = buffer[p]) & 0xff) <= ' ' || b == ':') // lucky! finished the fast path
				return intern(buffer, begin, pos = p);
			if ((b ^ '\\') < 1) // '\\' or multibyte char
				break; // jump to the slow path below
		}
		int len = p - begin, n = len, c, d;
		for (; ((b = buffer[p++]) & 0xff) > ' ' && b != ':'; len++)
			if (b == '\\' && buffer[p++] == 'u')
				p += 4;
		char[] t = tmp;
		if (t == null || t.length < len)
			tmp = t = new char[len];
		p = begin;
		for (int i = 0; i < n;)
			t[i++] = (char) (buffer[p++] & 0xff);
		for (;;) {
			if ((b = buffer[p++] & 0xff) <= ' ' || b == ':') {
				pos = p;
				return new String(t, 0, n);
			}
			if (b == '\\') {
				if ((b = buffer[p++]) == 'u') {
					t[n++] = (char) ((parseHex(buffer[p]) << 12) + (parseHex(buffer[p + 1]) << 8)
							+ (parseHex(buffer[p + 2]) << 4) + parseHex(buffer[p + 3]));
					p += 4;
				} else
					t[n++] = (char) (b >= 0x20 ? ESCAPE[b - 0x20] : b);
			} else if (b < 0x80)
				t[n++] = (char) b;
			else if (b > 0xdf) {
				if ((c = buffer[p]) < -0x40 && (d = buffer[p + 1]) < -0x40) {
					t[n++] = (char) (((b & 0xf) << 12) + ((c & 0x3f) << 6) + (d & 0x3f));
					p += 2;
				} else
					t[n++] = (char) b; // ignore malformed utf-8
			} else if ((c = buffer[p]) < -0x40) {
				p++;
				t[n++] = (char) (((b & 0x1f) << 6) + (c & 0x3f));
			} else
				t[n++] = (char) b; // ignore malformed utf-8
		}
	}

	static double strtod(double d, int e) {
		return e >= 0 ? d * EXP[e] : (e < -308 ? 0 : d / EXP[-e]);
	}

	public int parseInt() {
		final byte[] buffer = buf;
		double d = 0;
		int p = pos, i = 0, n = 0, expFrac = 0, exp = 0, useDouble = 0, b, c;
		boolean minus = false, expMinus = false;

		try {
			b = buffer[p];
			if (b == '-') {
				minus = true;
				b = buffer[++p];
			}
			if (b == '0')
				b = buffer[++p];
			else if ((i = (b - '0') & 0xff) < 10) {
				while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10) {
					if (i >= 0xCCC_CCCC) { // 0xCCC_CCCC * 10 = 0x7FFF_FFF8
						d = i;
						useDouble = 2;
						do
							d = d * 10 + c;
						while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10);
						break;
					}
					i = i * 10 + c;
					n++;
				}
			}

			if (b == '.') {
				b = buffer[++p];
				if (useDouble == 0) {
					useDouble = 1; //NOSONAR
					for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
						if (i >= 0xCCC_CCCC)
							break;
						i = i * 10 + c;
						expFrac--;
						if (i != 0)
							n++;
					}
					d = i;
					useDouble = 2;
				}
				for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
					if (n < 17) {
						d = d * 10 + c;
						expFrac--;
						if (d > 0)
							n++;
					}
				}
			}

			if ((b | 0x20) == 'e') { // E:0x45 | 0x20 = e:0x65
				if (useDouble == 0) {
					d = i;
					useDouble = 2;
				}
				if ((b = buffer[++p]) == '+')
					b = buffer[++p];

				if (b == '-') {
					expMinus = true;
					b = buffer[++p];
				}
				if ((b = (b - '0') & 0xff) < 10) {
					exp = b;
					final int maxExp = expMinus ? (expFrac + 0x7FFF_FFF7) / 10 : 308 - expFrac;
					while ((b = (buffer[++p] - '0') & 0xff) < 10) {
						if ((exp = exp * 10 + b) > maxExp) {
							if (!expMinus)
								return minus ? Integer.MIN_VALUE : Integer.MAX_VALUE;
							do
								b = buffer[++p];
							while (((b - '0') & 0xff) < 10);
						}
					}
				}
			}
		} catch (IndexOutOfBoundsException __) {
		}
		pos = p;
		if (expMinus)
			exp = -exp;
		if (useDouble > 0) {
			if (useDouble == 1)
				d = i;
			exp += expFrac;
			d = exp < -308 ? strtod(strtod(d, -308), exp + 308) : strtod(d, exp);
			i = (int) (minus ? -d : d);
		} else if (minus)
			i = -i;
		return i;
	}

	public long parseLong() {
		final byte[] buffer = buf;
		double d = 0;
		long i = 0;
		int p = pos, n = 0, expFrac = 0, exp = 0, useDouble = 0, b, c;
		boolean minus = false, expMinus = false;

		try {
			b = buffer[p];
			if (b == '-') {
				minus = true;
				b = buffer[++p];
			}
			if (b == '0')
				b = buffer[++p];
			else if ((i = (b - '0') & 0xff) < 10) {
				while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10) {
					if (i >= 0xCCC_CCCC_CCCC_CCCCL) { // 0xCCC_CCCC_CCCC_CCCC * 10 = 0x7FFF_FFFF_FFFF_FFF8
						d = i;
						useDouble = 2;
						do
							d = d * 10 + c;
						while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10);
						break;
					}
					i = i * 10 + c;
					n++;
				}
			}

			if (b == '.') {
				b = buffer[++p];
				if (useDouble == 0) {
					useDouble = 1; //NOSONAR
					for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
						if (i > 0x1F_FFFF_FFFF_FFFFL) // 2^53 - 1 for fast path
							break;
						i = i * 10 + c;
						expFrac--;
						if (i != 0)
							n++;
					}
					d = i;
					useDouble = 2;
				}
				for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
					if (n < 17) {
						d = d * 10 + c;
						expFrac--;
						if (d > 0)
							n++;
					}
				}
			}

			if ((b | 0x20) == 'e') { // E:0x45 | 0x20 = e:0x65
				if (useDouble == 0) {
					d = i;
					useDouble = 2;
				}
				if ((b = buffer[++p]) == '+')
					b = buffer[++p];

				if (b == '-') {
					expMinus = true;
					b = buffer[++p];
				}
				if ((b = (b - '0') & 0xff) < 10) {
					exp = b;
					final int maxExp = expMinus ? (expFrac + 0x7FFF_FFF7) / 10 : 308 - expFrac;
					while ((b = (buffer[++p] - '0') & 0xff) < 10) {
						if ((exp = exp * 10 + b) > maxExp) {
							if (!expMinus)
								return minus ? Long.MIN_VALUE : Long.MAX_VALUE;
							do
								b = buffer[++p];
							while (((b - '0') & 0xff) < 10);
						}
					}
				}
			}
		} catch (IndexOutOfBoundsException __) {
		}
		pos = p;
		if (expMinus)
			exp = -exp;
		if (useDouble > 0) {
			if (useDouble == 1)
				d = i;
			exp += expFrac;
			d = exp < -308 ? strtod(strtod(d, -308), exp + 308) : strtod(d, exp);
			i = (long) (minus ? -d : d);
		} else if (minus)
			i = -i;
		return i;
	}

	public double parseDouble() {
		final byte[] buffer = buf;
		double d = 0;
		long i = 0;
		int p = pos, n = 0, expFrac = 0, exp = 0, useDouble = 0, b, c;
		boolean minus = false, expMinus = false;

		try {
			b = buffer[p];
			if (b == '-') {
				minus = true;
				b = buffer[++p];
			}
			if (b == '0')
				b = buffer[++p];
			else if ((i = (b - '0') & 0xff) < 10) {
				while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10) {
					if (i >= 0xCCC_CCCC_CCCC_CCCCL) { // 0xCCC_CCCC_CCCC_CCCC * 10 = 0x7FFF_FFFF_FFFF_FFF8
						d = i;
						useDouble = 2;
						do
							d = d * 10 + c;
						while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10);
						break;
					}
					i = i * 10 + c;
					n++;
				}
			}

			if (b == '.') {
				b = buffer[++p];
				if (useDouble == 0) {
					useDouble = 1; //NOSONAR
					for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
						if (i > 0x1F_FFFF_FFFF_FFFFL) // 2^53 - 1 for fast path
							break;
						i = i * 10 + c;
						expFrac--;
						if (i != 0)
							n++;
					}
					d = i;
					useDouble = 2;
				}
				for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
					if (n < 17) {
						d = d * 10 + c;
						expFrac--;
						if (d > 0)
							n++;
					}
				}
			}

			if ((b | 0x20) == 'e') { // E:0x45 | 0x20 = e:0x65
				if (useDouble == 0) {
					d = i;
					useDouble = 2;
				}
				if ((b = buffer[++p]) == '+')
					b = buffer[++p];

				if (b == '-') {
					expMinus = true;
					b = buffer[++p];
				}
				if ((b = (b - '0') & 0xff) < 10) {
					exp = b;
					final int maxExp = expMinus ? (expFrac + 0x7FFF_FFF7) / 10 : 308 - expFrac;
					while ((b = (buffer[++p] - '0') & 0xff) < 10) {
						if ((exp = exp * 10 + b) > maxExp) {
							if (!expMinus)
								return minus ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
							do
								b = buffer[++p];
							while (((b - '0') & 0xff) < 10);
						}
					}
				}
			}
		} catch (IndexOutOfBoundsException __) {
		}
		pos = p;
		if (expMinus)
			exp = -exp;
		if (useDouble > 0) {
			if (useDouble == 1)
				d = i;
			exp += expFrac;
			d = exp < -308 ? strtod(strtod(d, -308), exp + 308) : strtod(d, exp);
			if (minus)
				d = -d;
		} else
			d = minus ? -i : i;
		return d;
	}

	public Object parseNumber() {
		final byte[] buffer = buf;
		double d = 0;
		long i = 0;
		int p = pos, n = 0, expFrac = 0, exp = 0, useDouble = 0, b, c;
		boolean minus = false, expMinus = false;

		try {
			b = buffer[p];
			if (b == '-') {
				minus = true;
				b = buffer[++p];
			}
			if (b == '0')
				b = buffer[++p];
			else if ((i = (b - '0') & 0xff) < 10) {
				while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10) {
					if (i >= 0xCCC_CCCC_CCCC_CCCCL) { // 0xCCC_CCCC_CCCC_CCCC * 10 = 0x7FFF_FFFF_FFFF_FFF8
						d = i;
						useDouble = 2;
						do
							d = d * 10 + c;
						while ((c = ((b = buffer[++p]) - '0') & 0xff) < 10);
						break;
					}
					i = i * 10 + c;
					n++;
				}
			}

			if (b == '.') {
				b = buffer[++p];
				if (useDouble == 0) {
					useDouble = 1; //NOSONAR
					for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
						if (i > 0x1F_FFFF_FFFF_FFFFL) // 2^53 - 1 for fast path
							break;
						i = i * 10 + c;
						expFrac--;
						if (i != 0)
							n++;
					}
					d = i;
					useDouble = 2;
				}
				for (; (c = (b - '0') & 0xff) < 10; b = buffer[++p]) {
					if (n < 17) {
						d = d * 10 + c;
						expFrac--;
						if (d > 0)
							n++;
					}
				}
			}

			if ((b | 0x20) == 'e') { // E:0x45 | 0x20 = e:0x65
				if (useDouble == 0) {
					d = i;
					useDouble = 2;
				}
				if ((b = buffer[++p]) == '+')
					b = buffer[++p];

				if (b == '-') {
					expMinus = true;
					b = buffer[++p];
				}
				if ((b = (b - '0') & 0xff) < 10) {
					exp = b;
					final int maxExp = expMinus ? (expFrac + 0x7FFF_FFF7) / 10 : 308 - expFrac;
					while ((b = (buffer[++p] - '0') & 0xff) < 10) {
						if ((exp = exp * 10 + b) > maxExp) {
							if (!expMinus)
								return minus ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
							do
								b = buffer[++p];
							while (((b - '0') & 0xff) < 10);
						}
					}
				}
			}
		} catch (IndexOutOfBoundsException __) {
		}
		pos = p;
		if (expMinus)
			exp = -exp;
		if (useDouble > 0) {
			if (useDouble == 1)
				d = i;
			exp += expFrac;
			d = exp < -308 ? strtod(strtod(d, -308), exp + 308) : strtod(d, exp);
			return minus ? -d : d;
		}
		if (minus)
			return (i = -i) >= Integer.MIN_VALUE ? (int) i : i;
		return i <= Integer.MAX_VALUE ? (int) i : i;
	}
}

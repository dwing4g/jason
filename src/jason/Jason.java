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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

//Compile with JDK11+; Run with JDK8+ (JDK9+ is recommended); Android is NOT supported
public final class Jason {
	static final int TYPE_BOOLEAN = 1; // boolean Boolean
	static final int TYPE_BYTE = 2; // byte Byte
	static final int TYPE_SHORT = 3; // short Short
	static final int TYPE_CHAR = 4; // char Character
	static final int TYPE_INT = 5; // int Integer
	static final int TYPE_LONG = 6; // long Long
	static final int TYPE_FLOAT = 7; // float Float
	static final int TYPE_DOUBLE = 8; // double Double
	static final int TYPE_STRING = 9; // String
	static final int TYPE_OBJECT = 10; // Object(null,Boolean,Integer,Long,Double,String,ArrayList<Obj>,HashMap<Str,Obj>)
	static final int TYPE_POS = 11; // Pos(pos)
	static final int TYPE_CUSTOM = 12; // user custom type
	static final int TYPE_WRAP_FLAG = 0x10; // wrap<1~8>
	static final int TYPE_LIST_FLAG = 0x20; // Collection<1~12>
	static final int TYPE_MAP_FLAG = 0x30; // Map<String, 1~12>

	static final class FieldMeta {
		final int type;
		final long offset;
		final byte[] name; // field name
		final @NonNull Class<?> klass; // TYPE_OBJ:fieldClass; TYPE_LIST_FLAG/TYPE_MAP_FLAG:subValueClass
		@Nullable
		ClassMeta<?> classMeta; // from klass, lazy assigned

		FieldMeta(int type, long offset, @NonNull String name, @NonNull Class<?> klass) {
			this.type = type;
			this.offset = offset;
			this.name = name.getBytes(StandardCharsets.UTF_8);
			this.klass = klass;
		}
	}

	static final class FieldMetaNode {
		final int key;
		FieldMeta fieldMeta;
		final FieldMetaNode next;

		FieldMetaNode(int key, FieldMeta fieldMeta, FieldMetaNode next) {
			this.key = key;
			this.fieldMeta = fieldMeta;
			this.next = next;
		}
	}

	static class FieldMetaMap {
		private final int hashShift; // [0,1,...30]
		private final int mask; // [0,0x3fff_ffff]
		private final int[] keyTable;
		private final FieldMeta[] valueTable;
		final FieldMeta[] fieldMetas;
		private final int pushIterations; // [1,2,4,8,11,16,22,...,4096]
		private @Nullable FieldMetaNode stashHead;

		FieldMetaMap(int size) {
			hashShift = 32 - Integer.numberOfLeadingZeros(size * 2 - 1);
			int capacity = 1 << hashShift;
			mask = capacity - 1;
			keyTable = new int[capacity];
			valueTable = new FieldMeta[capacity];
			fieldMetas = new FieldMeta[size];
			pushIterations = Math.max(Math.min(capacity, 8), (int) Math.sqrt(capacity) >> 3);
		}

		int hash2(int h) {
			h *= 0xbe1f14b1;
			return (h ^ (h >> hashShift)) & mask;
		}

		int hash3(int h) {
			h *= 0xb4b82e39;
			return (h ^ (h >> hashShift)) & mask;
		}

		@Nullable
		FieldMeta get(int key) {
			if (key != 0) {
				int[] kt = keyTable;
				int idx;
				if (kt[idx = key & mask] == key || kt[idx = hash2(key)] == key || kt[idx = hash3(key)] == key)
					return valueTable[idx];
			}
			for (FieldMetaNode node = stashHead; node != null; node = node.next)
				if (node.key == key)
					return node.fieldMeta;
			return null;
		}

		@Nullable
		FieldMeta put(int key, @Nullable FieldMeta value) {
			if (key == 0) {
				for (FieldMetaNode node = stashHead; node != null; node = node.next) {
					if (node.key == key) {
						FieldMeta oldValue = node.fieldMeta;
						node.fieldMeta = value;
						return oldValue;
					}
				}
				stashHead = new FieldMetaNode(key, value, stashHead);
				return null;
			}

			int[] kt = keyTable;
			FieldMeta[] vt = valueTable;
			int idx1 = key & mask, key1 = kt[idx1];
			if (key1 == key) {
				FieldMeta oldValue = vt[idx1];
				vt[idx1] = value;
				return oldValue;
			}
			int idx2 = hash2(key), key2 = kt[idx2];
			if (key2 == key) {
				FieldMeta oldValue = vt[idx2];
				vt[idx2] = value;
				return oldValue;
			}
			int idx3 = hash3(key), key3 = kt[idx3];
			if (key3 == key) {
				FieldMeta oldValue = vt[idx3];
				vt[idx3] = value;
				return oldValue;
			}
			for (FieldMetaNode node = stashHead; node != null; node = node.next) {
				if (node.key == key) {
					FieldMeta oldValue = node.fieldMeta;
					node.fieldMeta = value;
					return oldValue;
				}
			}

			if (key1 == 0) {
				kt[idx1] = key;
				vt[idx1] = value;
				return null;
			}
			if (key2 == 0) {
				kt[idx2] = key;
				vt[idx2] = value;
				return null;
			}
			if (key3 == 0) {
				kt[idx3] = key;
				vt[idx3] = value;
				return null;
			}

			int m = mask, evictedKey;
			FieldMeta evictedValue;
			ThreadLocalRandom rand = ThreadLocalRandom.current();
			for (int pis = pushIterations;; key = evictedKey, value = evictedValue) {
				switch (rand.nextInt(3)) {
				case 0:
					evictedKey = key1;
					evictedValue = vt[idx1];
					kt[idx1] = key;
					vt[idx1] = value;
					break;
				case 1:
					evictedKey = key2;
					evictedValue = vt[idx2];
					kt[idx2] = key;
					vt[idx2] = value;
					break;
				default:
					evictedKey = key3;
					evictedValue = vt[idx3];
					kt[idx3] = key;
					vt[idx3] = value;
					break;
				}

				if ((key1 = kt[idx1 = evictedKey & m]) == 0) {
					kt[idx1] = evictedKey;
					vt[idx1] = evictedValue;
					return null;
				}
				if ((key2 = kt[idx2 = hash2(evictedKey)]) == 0) {
					kt[idx2] = evictedKey;
					vt[idx2] = evictedValue;
					return null;
				}
				if ((key3 = kt[idx3 = hash3(evictedKey)]) == 0) {
					kt[idx3] = evictedKey;
					vt[idx3] = evictedValue;
					return null;
				}
				if (--pis <= 0) {
					stashHead = new FieldMetaNode(evictedKey, evictedValue, stashHead);
					return null;
				}
			}
		}
	}

	public interface Parser<T> {
		@Nullable
		T parse(@NonNull JasonReader reader, @NonNull ClassMeta<T> classMeta, @Nullable T obj)
				throws ReflectiveOperationException;

		@SuppressWarnings("unchecked")
		default @Nullable T parse0(@NonNull JasonReader reader, @NonNull ClassMeta<?> classMeta, @Nullable Object obj)
				throws ReflectiveOperationException {
			return parse(reader, (ClassMeta<T>) classMeta, (T) obj);
		}
	}

	public interface Writer<T> {
		void write(@NonNull JasonWriter writer, @NonNull ClassMeta<T> classMeta, @Nullable T obj);

		@SuppressWarnings("unchecked")
		default void write0(@NonNull JasonWriter writer, @NonNull ClassMeta<?> classMeta, @Nullable Object obj) {
			write(writer, (ClassMeta<T>) classMeta, (T) obj);
		}
	}

	public static class Pos {
		public int pos;

		public Pos() {
		}

		public Pos(int p) {
			pos = p;
		}
	}

	public static final class ClassMeta<T> extends FieldMetaMap {
		private static final @NonNull HashMap<Class<?>, Integer> typeMap = new HashMap<>(32);
		final @NonNull Class<T> klass;
		final @Nullable Constructor<T> ctor;
		@Nullable
		Parser<T> parser; // user custom parser
		@Nullable
		Writer<T> writer; // user custom writer

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

		private static int getFieldCount(Class<?> klass) {
			int n = 0;
			for (Class<?> c = klass; c != Object.class; c = c.getSuperclass())
				for (Field field : c.getDeclaredFields())
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)
						n++;
			return n;
		}

		ClassMeta(final @NonNull Class<T> klass) {
			super(getFieldCount(klass));
			this.klass = klass;
			Constructor<T> ct = null;
			for (Constructor<?> c : klass.getDeclaredConstructors()) {
				if (c.getParameterCount() == 0) {
					try {
						c.setAccessible(true);
						@SuppressWarnings("unchecked")
						Constructor<T> ct0 = (Constructor<T>) c;
						ct = ct0;
					} catch (Exception e) {
					}
					break;
				}
			}
			ctor = ct;
			ArrayList<Class<? super T>> classes = new ArrayList<>(2);
			for (Class<? super T> c = klass; c != Object.class; c = c.getSuperclass())
				classes.add(c);
			for (int i = classes.size() - 1, j = 0; i >= 0; i--) {
				Class<? super T> c = classes.get(i);
				for (Field field : c.getDeclaredFields()) {
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
						continue;
					Class<?> fieldClass = ensureNonNull(field.getType());
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
					String name = ensureNonNull(field.getName());
					FieldMeta fieldMeta = new FieldMeta(type, getUnsafe().objectFieldOffset(field), name, fieldClass);
					FieldMeta oldMeta = put(getKeyHash(fieldMeta.name, 0, fieldMeta.name.length), fieldMeta);
					if (oldMeta != null) // bad luck! try to call setKeyHashMultiplier with another prime number
						throw new IllegalStateException("conflicted field names: " + name + " & "
								+ new String(oldMeta.name, StandardCharsets.UTF_8) + " in class: " + klass.getName());
					fieldMetas[j++] = fieldMeta;
				}
			}
		}

		static int getType(Class<?> klass) {
			Integer type = typeMap.get(klass);
			return type != null ? type : TYPE_CUSTOM;
		}

		public @Nullable Parser<T> getParser() {
			return parser;
		}

		public void setParser(@Nullable Parser<T> p) {
			parser = p;
		}

		@SuppressWarnings("unchecked")
		@Deprecated
		public void setParserUnsafe(@Nullable Parser<?> p) { // DANGEROUS! only for special purpose
			parser = (Parser<T>) p;
		}

		public @Nullable Writer<T> getWriter() {
			return writer;
		}

		public void setWriter(@Nullable Writer<T> w) {
			writer = w;
		}
	}

	static final @NonNull Unsafe unsafe;
	static final boolean BYTE_STRING;
	static final long STRING_VALUE_OFFSET;
	private static final @NonNull ConcurrentHashMap<Class<?>, ClassMeta<?>> classMetas = new ConcurrentHashMap<>();
	static final int keyHashMultiplier = 0x100_0193; //NOSONAR 1677_7619 can be changed to another prime number

	static {
		try {
			Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			unsafe = ensureNonNull((Unsafe) theUnsafeField.get(null));
			Field valueField = String.class.getDeclaredField("value");
			BYTE_STRING = valueField.getType() == byte[].class;
			STRING_VALUE_OFFSET = unsafe.objectFieldOffset(valueField);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("null")
	public static <T> @NonNull T ensureNonNull(@Nullable T obj) {
		return obj; //NOSONAR
	}

	public static @NonNull Unsafe getUnsafe() {
		return unsafe;
	}

	@SuppressWarnings({ "unchecked", "null" })
	public static <T> @NonNull ClassMeta<T> getClassMeta(@NonNull Class<T> klass) {
		return (ClassMeta<T>) classMetas.computeIfAbsent(klass, ClassMeta::new);
	}

//	public static void setKeyHashMultiplier(int multiplier) { // must be set before any other access
//		keyHashMultiplier = multiplier;
//	}

	public static int getKeyHash(byte[] buf, int pos, int end) {
		if (pos >= end)
			return 0;
		int h = buf[pos], m = keyHashMultiplier;
		while (++pos < end)
			h = h * m + buf[pos];
		return h;
	}

	static @NonNull String newByteString(byte[] buf, int pos, int end) throws ReflectiveOperationException {
		if (!BYTE_STRING) // for JDK8-
			return new String(buf, pos, end - pos, StandardCharsets.ISO_8859_1);
		@SuppressWarnings("null")
		@NonNull
		String str = (String) unsafe.allocateInstance(String.class);
		unsafe.putObject(str, STRING_VALUE_OFFSET, Arrays.copyOfRange(buf, pos, end)); // for JDK9+
		return str;
	}

	private Jason() {
	}
}

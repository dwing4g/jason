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
	static final int TYPE_LIST_FLAG = 0x20; // Collection<1~12> (parser only needs clear() & add(v))
	static final int TYPE_MAP_FLAG = 0x30; // Map<String, 1~12> (parser only needs clear() & put(k,v))

	static final class FieldMeta {
		final int type; // defined above
		final long offset; // for unsafe access
		final byte[] name; // field name
		final @NonNull Class<?> klass; // TYPE_CUSTOM:fieldClass; TYPE_LIST_FLAG/TYPE_MAP_FLAG:subValueClass
		final int hash; // for FieldMetaMap
		transient FieldMeta next; // for FieldMetaMap
		transient @Nullable ClassMeta<?> classMeta; // from klass, lazy assigned

		FieldMeta(int type, long offset, @NonNull String name, @NonNull Class<?> klass) {
			this.type = type;
			this.offset = offset;
			this.name = name.getBytes(StandardCharsets.UTF_8);
			this.klass = klass;
			this.hash = getKeyHash(this.name, 0, name.length());
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

	public static final class ClassMeta<T> {
		private static final @NonNull HashMap<Class<?>, Integer> typeMap = new HashMap<>(32);
		private final FieldMeta[] valueTable;
		final FieldMeta[] fieldMetas;
		final @NonNull Class<T> klass;
		final @Nullable Constructor<T> ctor;
		transient @Nullable Parser<T> parser; // user custom parser
		transient @Nullable Writer<T> writer; // user custom writer

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

		ClassMeta(final @NonNull Class<T> klass) {
			int size = 0;
			for (Class<?> c = klass; c != Object.class; c = c.getSuperclass())
				for (Field field : c.getDeclaredFields())
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)
						size++;
			valueTable = new FieldMeta[1 << (32 - Integer.numberOfLeadingZeros(size * 2 - 1))];
			fieldMetas = new FieldMeta[size];
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
					else if (Collection.class.isAssignableFrom(fieldClass)) { // Collection<?>
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
					} else if (Map.class.isAssignableFrom(fieldClass)) { // Map<String, ?>
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
					put(j++, new FieldMeta(type, getUnsafe().objectFieldOffset(field), name, fieldClass));
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

		@Nullable
		FieldMeta get(int hash) {
			for (FieldMeta fm = valueTable[hash & (valueTable.length - 1)]; fm != null; fm = fm.next)
				if (fm.hash == hash)
					return fm;
			return null;
		}

		void put(int idx, @NonNull FieldMeta fieldMeta) {
			fieldMeta.next = null;
			fieldMetas[idx] = fieldMeta;
			int hash = fieldMeta.hash;
			int i = hash & (valueTable.length - 1);
			FieldMeta fm = valueTable[i];
			if (fm == null) { // fast path
				valueTable[i] = fieldMeta;
				return;
			}
			for (;; fm = fm.next) {
				if (fm.hash == hash) // bad luck! try to call setKeyHashMultiplier with another prime number
					throw new IllegalStateException("conflicted field names: " + fieldMeta.name + " & "
							+ new String(fm.name, StandardCharsets.UTF_8) + " in class: " + fieldMeta.klass.getName());
				if (fm.next == null) {
					fm.next = fieldMeta;
					return;
				}
			}
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

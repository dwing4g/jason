package jason;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
	static final int TYPE_OBJECT = 10; // Object(null,Boolean,Integer,Long,Double,String,ArrayList<?>,HashMap<?,?>)
	static final int TYPE_POS = 11; // Pos(pos)
	static final int TYPE_CUSTOM = 12; // user custom type
	static final int TYPE_WRAP_FLAG = 0x10; // wrap<1~8>
	static final int TYPE_LIST_FLAG = 0x20; // Collection<1~12> (parser only needs clear() & add(v))
	static final int TYPE_MAP_FLAG = 0x30; // Map<String, 1~12> (parser only needs clear() & put(k,v))

	interface KeyReader {
		@NonNull
		Object parse(@NonNull JasonReader jr, int b) throws ReflectiveOperationException;
	}

	static final class FieldMeta {
		final int hash; // for FieldMetaMap
		final int type; // defined above
		final int offset; // for unsafe access
		final @NonNull Class<?> klass; // TYPE_CUSTOM:fieldClass; TYPE_LIST_FLAG/TYPE_MAP_FLAG:subValueClass
		transient @Nullable ClassMeta<?> classMeta; // from klass, lazy assigned
		transient @Nullable FieldMeta next; // for FieldMetaMap
		final byte[] name; // field name
		final @Nullable Constructor<?> ctor; // for TYPE_LIST_FLAG/TYPE_MAP_FLAG
		final @Nullable KeyReader keyParser; // for TYPE_MAP_FLAG

		FieldMeta(int type, int offset, @NonNull String name, @NonNull Class<?> klass, @Nullable Constructor<?> ctor,
				@Nullable KeyReader keyReader) {
			this.name = name.getBytes(StandardCharsets.UTF_8);
			this.hash = getKeyHash(this.name, 0, this.name.length);
			this.type = type;
			this.offset = offset;
			this.klass = klass;
			this.ctor = ctor;
			this.keyParser = keyReader;
		}

		@NonNull
		String getName() {
			return new String(name, StandardCharsets.UTF_8);
		}
	}

	public interface Parser<T> {
		@Nullable
		T parse(@NonNull JasonReader reader, @NonNull ClassMeta<T> classMeta, @Nullable T obj)
				throws ReflectiveOperationException;

		@SuppressWarnings("unchecked")
		default @Nullable T parse0(@NonNull JasonReader reader, @NonNull ClassMeta<?> classMeta, @Nullable Object obj)
				throws ReflectiveOperationException {
			return parse(reader, (ClassMeta<T>)classMeta, (T)obj);
		}
	}

	public interface Writer<T> {
		void write(@NonNull JasonWriter writer, @NonNull ClassMeta<T> classMeta, @Nullable T obj);

		@SuppressWarnings("unchecked")
		default void write0(@NonNull JasonWriter writer, @NonNull ClassMeta<?> classMeta, @Nullable Object obj) {
			write(writer, (ClassMeta<T>)classMeta, (T)obj);
		}
	}

	public static final class Pos {
		public int pos;

		public Pos() {
		}

		public Pos(int p) {
			pos = p;
		}
	}

	public static final class ClassMeta<T> {
		private static final @NonNull HashMap<Class<?>, Integer> typeMap = new HashMap<>(32);
		private static final @NonNull HashMap<Type, KeyReader> keyReaderMap = new HashMap<>(16);
		private final FieldMeta[] valueTable;
		final FieldMeta[] fieldMetas;
		final @NonNull Class<T> klass;
		final @Nullable Constructor<T> ctor;
		final boolean isAbstract;
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
			keyReaderMap.put(Boolean.class, JasonReader::parseBooleanKey);
			keyReaderMap.put(Byte.class, JasonReader::parseByteKey);
			keyReaderMap.put(Short.class, JasonReader::parseShortKey);
			keyReaderMap.put(Character.class, JasonReader::parseCharKey);
			keyReaderMap.put(Integer.class, JasonReader::parseIntegerKey);
			keyReaderMap.put(Long.class, JasonReader::parseLongKey);
			keyReaderMap.put(Float.class, JasonReader::parseFloatKey);
			keyReaderMap.put(Double.class, JasonReader::parseDoubleKey);
			keyReaderMap.put(String.class, JasonReader::parseStringKey);
			keyReaderMap.put(Object.class, JasonReader::parseStringKey);
		}

		static boolean isAbstract(@NonNull Class<?> klass) {
			return (klass.getModifiers() & (Modifier.INTERFACE | Modifier.ABSTRACT)) != 0;
		}

		@SuppressWarnings("unchecked")
		private static <T> @Nullable Constructor<T> getDefCtor(@NonNull Class<T> klass) {
			for (Constructor<?> c : klass.getDeclaredConstructors()) {
				if (c.getParameterCount() == 0) {
					try {
						setAccessible(c);
						return (Constructor<T>)c;
					} catch (Exception ignored) {
					}
				}
			}
			return null;
		}

		private static @Nullable Class<?> getCollectionSubClass(Type geneType) { // X<T>, X extends Y<T>, X implements Y<T>
			if (geneType instanceof ParameterizedType) {
				ParameterizedType paraType = (ParameterizedType)geneType;
				Class<?> rawClass = (Class<?>)paraType.getRawType();
				if (Collection.class.isAssignableFrom(rawClass)) {
					Type type = paraType.getActualTypeArguments()[0];
					if (type instanceof Class)
						return (Class<?>)type;
				}
			}
			if (geneType instanceof Class) {
				Class<?> klass = (Class<?>)geneType;
				for (Type subType : klass.getGenericInterfaces()) {
					Class<?> subClass = getCollectionSubClass(subType);
					if (subClass != null)
						return subClass;
				}
				return getCollectionSubClass(klass.getGenericSuperclass());
			}
			return null;
		}

		private static Type[] getMapSubClasses(Type geneType) { // X<K,V>, X extends Y<K,V>, X implements Y<K,V>
			if (geneType instanceof ParameterizedType) {
				ParameterizedType paraType = (ParameterizedType)geneType;
				if (Map.class.isAssignableFrom((Class<?>)paraType.getRawType())) {
					Type[] subTypes = paraType.getActualTypeArguments();
					if (subTypes.length == 2 && subTypes[0] instanceof Class && subTypes[1] instanceof Class)
						return subTypes;
				}
			}
			if (geneType instanceof Class) {
				Class<?> klass = (Class<?>)geneType;
				for (Type subType : klass.getGenericInterfaces()) {
					Type[] subTypes = getMapSubClasses(subType);
					if (subTypes != null)
						return subTypes;
				}
				return getMapSubClasses(klass.getGenericSuperclass());
			}
			return null;
		}

		ClassMeta(final @NonNull Class<T> klass) {
			int size = 0;
			for (Class<?> c = klass; c != null; c = c.getSuperclass())
				for (Field field : getDeclaredFields(c))
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)
						size++;
			valueTable = new FieldMeta[1 << (32 - Integer.numberOfLeadingZeros(size * 2 - 1))];
			fieldMetas = new FieldMeta[size];
			this.klass = klass;
			isAbstract = isAbstract(klass);
			ctor = getDefCtor(klass);
			ArrayList<Class<? super T>> classes = new ArrayList<>(2);
			for (Class<? super T> c = klass; c != null; c = c.getSuperclass())
				classes.add(c);
			for (int i = classes.size() - 1, j = 0; i >= 0; i--) {
				Class<? super T> c = classes.get(i);
				for (Field field : getDeclaredFields(c)) {
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
						continue;
					final String fieldName = field.getName();
					if (fieldName.startsWith("this$")) // closure field
						continue;
					Class<?> fieldClass = ensureNonNull(field.getType());
					Constructor<?> fieldCtor = null;
					KeyReader keyReader = null;
					Integer v = typeMap.get(fieldClass);
					int type;
					if (v != null)
						type = v;
					else if (Collection.class.isAssignableFrom(fieldClass)) { // Collection<?>
						if (!isAbstract(fieldClass))
							fieldCtor = getDefCtor(fieldClass);
						else if (fieldClass.isAssignableFrom(ArrayList.class)) // AbstractList,AbstractCollection,List,Collection
							fieldCtor = getDefCtor(ArrayList.class);
						else if (fieldClass.isAssignableFrom(HashSet.class)) // AbstractSet,Set
							fieldCtor = getDefCtor(HashSet.class);
						else if (fieldClass.isAssignableFrom(ArrayDeque.class)) // Deque
							fieldCtor = getDefCtor(ArrayDeque.class);
						else if (fieldClass.isAssignableFrom(TreeSet.class)) // NavigableSet
							fieldCtor = getDefCtor(TreeSet.class);
						else if (fieldClass.isAssignableFrom(LinkedList.class)) // AbstractSequentialList
							fieldCtor = getDefCtor(LinkedList.class);
						else if (fieldClass.isAssignableFrom(PriorityQueue.class)) // AbstractQueue
							fieldCtor = getDefCtor(PriorityQueue.class);
						Class<?> subClass = getCollectionSubClass(field.getGenericType());
						if (subClass != null) {
							v = typeMap.get(fieldClass = subClass);
							type = TYPE_LIST_FLAG + (v != null ? v & 0xf : TYPE_CUSTOM);
						} else
							type = TYPE_LIST_FLAG + TYPE_OBJECT;
					} else if (Map.class.isAssignableFrom(fieldClass)) { // Map<?,?>
						if (!isAbstract(fieldClass))
							fieldCtor = getDefCtor(fieldClass);
						else if (fieldClass.isAssignableFrom(HashMap.class)) // AbstractMap,Map
							fieldCtor = getDefCtor(HashMap.class);
						else if (fieldClass.isAssignableFrom(TreeMap.class)) // NavigableMap,SortedMap
							fieldCtor = getDefCtor(TreeMap.class);
						Type[] subTypes = getMapSubClasses(field.getGenericType());
						if (subTypes != null) {
							v = typeMap.get(fieldClass = ensureNonNull((Class<?>)subTypes[1]));
							type = TYPE_MAP_FLAG + (v != null ? v & 0xf : TYPE_CUSTOM);
							keyReader = keyReaderMap.get(subTypes[0]);
							if (keyReader == null)
								keyReader = JasonReader::parseStringKey;
						} else {
							type = TYPE_MAP_FLAG + TYPE_OBJECT;
							keyReader = JasonReader::parseStringKey;
						}
					} else
						type = TYPE_CUSTOM;
					long offset = unsafe.objectFieldOffset(field);
					if (offset != (int)offset)
						throw new IllegalStateException("unexpected offset(" + offset + ") from field: "
								+ fieldName + " in " + klass.getName());
					put(j++, new FieldMeta(type, (int)offset, ensureNonNull(fieldName), fieldClass, fieldCtor,
							keyReader));
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
			parser = (Parser<T>)p;
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
			for (;;) {
				if (fm.hash == hash) // bad luck! try to call setKeyHashMultiplier with another prime number
					throw new IllegalStateException("conflicted field names: " + fieldMeta.getName() + " & "
							+ fm.getName() + " in " + fieldMeta.klass.getName());
				FieldMeta next = fm.next;
				if (next == null) {
					fm.next = fieldMeta;
					return;
				}
				fm = next;
			}
		}
	}

	static final @NonNull Unsafe unsafe;
	static final @NonNull MethodHandle getDeclaredFields0MH;
	static final long OVERRIDE_OFFSET;
	static final long STRING_VALUE_OFFSET;
	static final boolean BYTE_STRING;
	private static final @NonNull ConcurrentHashMap<Class<?>, ClassMeta<?>> classMetas = new ConcurrentHashMap<>();
	static final int keyHashMultiplier = 0x100_0193; // 1677_7619 can be changed to another prime number

	static {
		try {
			Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			unsafe = ensureNonNull((Unsafe)theUnsafeField.get(null));
			for (long i = 8; ; i++) {
				theUnsafeField.setAccessible(true);
				if (unsafe.getBoolean(theUnsafeField, i)) {
					theUnsafeField.setAccessible(false);
					if (!unsafe.getBoolean(theUnsafeField, i)) {
						OVERRIDE_OFFSET = i;
						break;
					}
				}
				if (i == 32)
					throw new UnsupportedOperationException(System.getProperty("java.version"));
			}
			Method getDeclaredFields0Method = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
			setAccessible(getDeclaredFields0Method);
			getDeclaredFields0MH = ensureNonNull(MethodHandles.lookup().unreflect(getDeclaredFields0Method));
			Field valueField = getDeclaredField(String.class, "value");
			STRING_VALUE_OFFSET = unsafe.objectFieldOffset(Objects.requireNonNull(valueField));
			BYTE_STRING = valueField.getType() == byte[].class;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	static Field[] getDeclaredFields(Class<?> klass) {
		try {
			return (Field[])getDeclaredFields0MH.invokeExact(klass, false);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("SameParameterValue")
	static @Nullable Field getDeclaredField(Class<?> klass, String fieldName) {
		for (Field field : getDeclaredFields(klass))
			if (field.getName().equals(fieldName))
				return field;
		return null;
	}

	static void setAccessible(AccessibleObject ao) {
		unsafe.putBoolean(ao, OVERRIDE_OFFSET, true);
	}

	@SuppressWarnings("null")
	public static <T> @NonNull T ensureNonNull(@Nullable T obj) {
		assert obj != null;
		return obj;
	}

	public static @NonNull Unsafe getUnsafe() {
		return unsafe;
	}

	@SuppressWarnings({ "unchecked", "null" })
	public static <T> @NonNull ClassMeta<T> getClassMeta(@NonNull Class<T> klass) {
		return (ClassMeta<T>)classMetas.computeIfAbsent(klass, ClassMeta::new);
	}

	public static void clearClassMetas() {
		classMetas.clear();
	}

//	public static void setKeyHashMultiplier(int multiplier) { // must be set before any other access
//		keyHashMultiplier = multiplier;
//	}

	public static int getKeyHash(byte[] buf, int pos, int end) {
		if (pos >= end)
			return 0;
		//noinspection UnnecessaryLocalVariable
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
		String str = (String)unsafe.allocateInstance(String.class);
		unsafe.putObject(str, STRING_VALUE_OFFSET, Arrays.copyOfRange(buf, pos, end)); // for JDK9+
		return str;
	}

	private Jason() {
	}
}

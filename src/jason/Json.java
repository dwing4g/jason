package jason;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import sun.misc.Unsafe;

// Compile with JDK11+; Run with JDK8+ (JDK9+ is recommended); Android is NOT supported
public final class Json implements Cloneable {
	static final int TYPE_BOOLEAN = 1; // boolean, Boolean
	static final int TYPE_BYTE = 2; // byte, Byte
	static final int TYPE_SHORT = 3; // short, Short
	static final int TYPE_CHAR = 4; // char, Character
	static final int TYPE_INT = 5; // int, Integer
	static final int TYPE_LONG = 6; // long, Long
	static final int TYPE_FLOAT = 7; // float, Float
	static final int TYPE_DOUBLE = 8; // double, Double
	static final int TYPE_STRING = 9; // String
	static final int TYPE_OBJECT = 10; // Object(null,Boolean,Integer,Long,Double,String,ArrayList<?>,HashMap<?,?>)
	static final int TYPE_POS = 11; // Pos(pos)
	static final int TYPE_CUSTOM = 12; // user custom type
	static final int TYPE_WRAP_FLAG = 0x10; // wrap<1~8>
	static final int TYPE_LIST_FLAG = 0x20; // Collection<1~12> (parser only needs clear() & add(v))
	static final int TYPE_MAP_FLAG = 0x30; // Map<String, 1~12> (parser only needs clear() & put(k,v))

	public interface KeyReader {
		@NonNull
		Object parse(@NonNull JsonReader jr, int b) throws ReflectiveOperationException;
	}

	public interface Creator<T> {
		T create() throws ReflectiveOperationException;
	}

	public static final class FieldMeta {
		public final int hash; // for FieldMetaMap
		public final int type; // defined above
		public final int offset; // for unsafe access
		public final @NonNull Class<?> klass; // TYPE_CUSTOM:fieldClass; TYPE_LIST_FLAG/TYPE_MAP_FLAG:subValueClass
		transient @Nullable ClassMeta<?> classMeta; // from klass, lazy assigned
		transient @Nullable FieldMeta next; // for FieldMetaMap
		public final byte[] name; // field name
		public final @Nullable Creator<?> ctor; // for TYPE_LIST_FLAG/TYPE_MAP_FLAG
		public final @Nullable KeyReader keyParser; // for TYPE_MAP_FLAG
		public final @NonNull Field field;
		public final @Nullable Type[] paramTypes;

		FieldMeta(int type, int offset, @NonNull String name, @NonNull Class<?> klass, @Nullable Creator<?> ctor,
				  @Nullable KeyReader keyReader, @NonNull Field field) {
			this.name = name.getBytes(StandardCharsets.UTF_8);
			this.hash = getKeyHash(this.name, 0, this.name.length);
			this.type = type;
			this.offset = offset;
			this.klass = klass;
			this.ctor = ctor;
			this.keyParser = keyReader;
			this.field = field;
			Type geneType = field.getGenericType();
			paramTypes = geneType instanceof ParameterizedType
					? ((ParameterizedType)geneType).getActualTypeArguments() : null;
		}

		@NonNull
		public String getName() {
			return new String(name, StandardCharsets.UTF_8);
		}
	}

	public interface Parser<T> {
		@Nullable
		T parse(@NonNull JsonReader reader, @NonNull ClassMeta<T> classMeta, @Nullable FieldMeta fieldMeta,
				@Nullable T obj, @Nullable Object parent) throws ReflectiveOperationException;

		@SuppressWarnings("unchecked")
		default @Nullable T parse0(@NonNull JsonReader reader, @NonNull ClassMeta<?> classMeta,
								   @Nullable FieldMeta fieldMeta, @Nullable Object obj,
								   @Nullable Object parent) throws ReflectiveOperationException {
			return parse(reader, (ClassMeta<T>)classMeta, fieldMeta, (T)obj, parent);
		}
	}

	public interface Writer<T> {
		void write(@NonNull JsonWriter writer, @NonNull ClassMeta<T> classMeta, @Nullable T obj);

		// ensure +1
		@SuppressWarnings("unchecked")
		default void write0(@NonNull JsonWriter writer, @NonNull ClassMeta<?> classMeta, @Nullable Object obj) {
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

		final @NonNull Json json;
		final @NonNull Class<T> klass;
		final @NonNull Creator<T> ctor;
		private final FieldMeta[] valueTable;
		final FieldMeta[] fieldMetas;
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
			keyReaderMap.put(Boolean.class, JsonReader::parseBooleanKey);
			keyReaderMap.put(Byte.class, JsonReader::parseByteKey);
			keyReaderMap.put(Short.class, JsonReader::parseShortKey);
			keyReaderMap.put(Character.class, JsonReader::parseCharKey);
			keyReaderMap.put(Integer.class, JsonReader::parseIntegerKey);
			keyReaderMap.put(Long.class, JsonReader::parseLongKey);
			keyReaderMap.put(Float.class, JsonReader::parseFloatKey);
			keyReaderMap.put(Double.class, JsonReader::parseDoubleKey);
			keyReaderMap.put(String.class, JsonReader::parseStringKey);
			keyReaderMap.put(Object.class, JsonReader::parseStringKey);
		}

		static boolean isInKeyReaderMap(Class<?> klass) {
			return keyReaderMap.containsKey(klass);
		}

		public static KeyReader getKeyReader(Class<?> klass) {
			return keyReaderMap.get(klass);
		}

		static boolean isAbstract(@NonNull Class<?> klass) {
			return (klass.getModifiers() & (Modifier.INTERFACE | Modifier.ABSTRACT)) != 0;
		}

		@SuppressWarnings("unchecked")
		private static <T> @NonNull Creator<T> getDefCtor(@NonNull Class<T> klass) {
			for (Constructor<?> c : klass.getDeclaredConstructors()) {
				if (c.getParameterCount() == 0) {
					setAccessible(c);
					return () -> (T)c.newInstance((Object[])null);
				}
			}
			return () -> (T)unsafe.allocateInstance(klass);
		}

		private static @Nullable Class<?> getCollectionSubClass(Type geneType) { // X<T>, X extends Y<T>, X implements Y<T>
			if (geneType instanceof ParameterizedType) {
				//noinspection PatternVariableCanBeUsed
				ParameterizedType paraType = (ParameterizedType)geneType;
				Class<?> rawClass = (Class<?>)paraType.getRawType();
				if (Collection.class.isAssignableFrom(rawClass)) {
					Type type = paraType.getActualTypeArguments()[0];
					if (type instanceof Class)
						return (Class<?>)type;
				}
			}
			if (geneType instanceof Class) {
				//noinspection PatternVariableCanBeUsed
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
				//noinspection PatternVariableCanBeUsed
				ParameterizedType paraType = (ParameterizedType)geneType;
				if (Map.class.isAssignableFrom((Class<?>)paraType.getRawType())) {
					Type[] subTypes = paraType.getActualTypeArguments();
					if (subTypes.length == 2 && subTypes[0] instanceof Class && subTypes[1] instanceof Class)
						return subTypes;
				}
			}
			if (geneType instanceof Class) {
				//noinspection PatternVariableCanBeUsed
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

		ClassMeta(final @NonNull Json json, final @NonNull Class<T> klass) {
			this.json = json;
			this.klass = klass;
			ctor = getDefCtor(klass);
			int size = 0;
			for (Class<?> c = klass; c != null; c = c.getSuperclass())
				for (Field field : getDeclaredFields(c))
					if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0
							&& !field.getName().startsWith("this$"))
						size++;
			valueTable = new FieldMeta[1 << (32 - Integer.numberOfLeadingZeros(size * 2 - 1))];
			fieldMetas = new FieldMeta[size];
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
					Creator<?> fieldCtor = null;
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
							if (keyReader == null) {
								Class<?> keyClass = (Class<?>)subTypes[0];
								if (isAbstract(keyClass))
									throw new IllegalStateException("unsupported abstract key class for field: "
											+ fieldName + " in " + klass.getName());
								Creator<?> keyCtor = getDefCtor(keyClass);
								keyReader = (jr, b) -> {
									String keyStr = JsonReader.parseStringKey(jr, b);
									return ensureNonNull(new JsonReader().buf(keyStr).parse(json, keyCtor.create()));
								};
							}
						} else {
							type = TYPE_MAP_FLAG + TYPE_OBJECT;
							keyReader = JsonReader::parseStringKey;
						}
					} else
						type = TYPE_CUSTOM;
					long offset = objectFieldOffset(field);
					if (offset != (int)offset)
						throw new IllegalStateException("unexpected offset(" + offset + ") from field: "
								+ fieldName + " in " + klass.getName());
					final BiFunction<Class<?>, Field, String> fieldNameFilter = json.fieldNameFilter;
					final String fn = fieldNameFilter != null ? fieldNameFilter.apply(c, field) : fieldName;
					put(j++, new FieldMeta(type, (int)offset, fn != null ? fn : fieldName, fieldClass, fieldCtor,
							keyReader, field));
				}
			}
		}

		static int getType(Class<?> klass) {
			Integer type = typeMap.get(klass);
			return type != null ? type : TYPE_CUSTOM;
		}

		public @NonNull Creator<T> getCtor() {
			return ctor;
		}

		public @Nullable Parser<T> getParser() {
			return parser;
		}

		public void setParser(@Nullable Parser<T> p) {
			parser = p;
		}

		@SuppressWarnings("unchecked")
		@Deprecated // unsafe
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
			for (; ; ) {
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
	private static final @NonNull MethodHandle getDeclaredFields0MH;
	static final @NonNull MethodHandle stringCtorMH;
	private static final long OVERRIDE_OFFSET;
	static final long STRING_VALUE_OFFSET, STRING_CODE_OFFSET;
	static final boolean BYTE_STRING;
	static final int keyHashMultiplier = 0x100_0193; // 1677_7619 can be changed to another prime number
	public static final int javaVersion;
	public static final Json instance = new Json();

	private final @NonNull ConcurrentHashMap<Class<?>, ClassMeta<?>> classMetas = new ConcurrentHashMap<>();
	public BiFunction<Class<?>, Field, String> fieldNameFilter;

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public @NonNull Json clone() {
		Json json = new Json();
		json.classMetas.putAll(classMetas);
		json.fieldNameFilter = fieldNameFilter;
		return json;
	}

	static {
		try {
			Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			unsafe = ensureNonNull((Unsafe)theUnsafeField.get(null));
			for (long i = 8; ; i++) {
				if (unsafe.getBoolean(theUnsafeField, i)) {
					theUnsafeField.setAccessible(false);
					if (!unsafe.getBoolean(theUnsafeField, i)) {
						OVERRIDE_OFFSET = i;
						break;
					}
					theUnsafeField.setAccessible(true);
				}
				if (i == 32) // should be enough
					throw new UnsupportedOperationException(System.getProperty("java.version"));
			}
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			getDeclaredFields0MH = ensureNonNull(lookup.unreflect(setAccessible(
					Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class))));
			Field valueField = getDeclaredField(String.class, "value");
			STRING_VALUE_OFFSET = objectFieldOffset(Objects.requireNonNull(valueField));
			BYTE_STRING = valueField.getType() == byte[].class;
			STRING_CODE_OFFSET = BYTE_STRING ?
					objectFieldOffset(Objects.requireNonNull(getDeclaredField(String.class, "coder"))) : 0;
			//noinspection JavaReflectionMemberAccess
			stringCtorMH = ensureNonNull(lookup.unreflectConstructor(setAccessible(BYTE_STRING
					? String.class.getDeclaredConstructor(byte[].class, byte.class)
					: String.class.getDeclaredConstructor(char[].class, boolean.class))));
			javaVersion = (int)Float.parseFloat(System.getProperty("java.specification.version"));
		} catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static @NonNull Unsafe getUnsafe() {
		return unsafe;
	}

	@SuppressWarnings("deprecation")
	public static long objectFieldOffset(Field field) {
		return unsafe.objectFieldOffset(field);
	}

	static Field[] getDeclaredFields(Class<?> klass) {
		try {
			return (Field[])getDeclaredFields0MH.invokeExact(klass, false);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) { // MethodHandle.invoke
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

	public static <T extends AccessibleObject> @NonNull T setAccessible(@NonNull T ao) {
		unsafe.putBoolean(ao, OVERRIDE_OFFSET, true);
		return ao;
	}

	public static <T> @NonNull T ensureNonNull(@Nullable T obj) {
		assert obj != null;
		return obj;
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

	static @NonNull String newByteString(byte[] buf, int pos, int end) {
		if (!BYTE_STRING) // for JDK8-
			return new String(buf, pos, end - pos, StandardCharsets.ISO_8859_1);
		try {
			return (String)stringCtorMH.invokeExact(Arrays.copyOfRange(buf, pos, end), (byte)0); // for JDK9+
		} catch (Throwable e) { // MethodHandle.invoke
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> @NonNull ClassMeta<T> getClassMeta(@NonNull Class<T> klass) {
		ClassMeta<?> cm = classMetas.get(klass);
		if (cm == null)
			cm = classMetas.computeIfAbsent(klass, c -> new ClassMeta<>(this, c));
		return (ClassMeta<T>)cm;
	}

	public void clearClassMetas() {
		classMetas.clear();
	}

	public static <T> @Nullable T parse(@NonNull String jsonStr, @Nullable Class<T> klass) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr).parse(klass);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static <T> @Nullable T parse(byte @NonNull [] jsonStr, @Nullable Class<T> klass) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr).parse(klass);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static <T> @Nullable T parse(byte @NonNull [] jsonStr, int pos, @Nullable Class<T> klass) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr, pos).parse(klass);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static <T> @Nullable T parse(@NonNull String jsonStr, @Nullable T obj) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr).parse(obj);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static <T> @Nullable T parse(byte @NonNull [] jsonStr, @Nullable T obj) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr).parse(obj);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static <T> @Nullable T parse(byte @NonNull [] jsonStr, int pos, @Nullable T obj) {
		JsonReader jr = JsonReader.local();
		try {
			return jr.buf(jsonStr, pos).parse(obj);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		} finally {
			jr.reset();
		}
	}

	public static @NonNull String toCompactString(@Nullable Object obj) {
		JsonWriter jw = JsonWriter.local();
		try {
			return jw.clear().setFlagsAndDepthLimit(0, 16).write(obj).toString();
		} finally {
			jw.clear();
		}
	}

	public static byte @NonNull [] toCompactBytes(@Nullable Object obj) {
		JsonWriter jw = JsonWriter.local();
		try {
			return jw.clear().setFlagsAndDepthLimit(0, 16).write(obj).toBytes();
		} finally {
			jw.clear();
		}
	}
}

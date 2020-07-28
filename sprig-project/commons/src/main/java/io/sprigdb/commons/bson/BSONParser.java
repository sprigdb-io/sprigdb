package io.sprigdb.commons.bson;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class BSONParser {

	private KeySubstitutor keysub;

	public BSONParser() {

		this.keysub = new DefaultKeySubstitutor();
	}

	public BSONParser(KeySubstitutor keysub) {

		this.keysub = keysub;
	}

	public KeySubstitutor getKeySubstitutor() {

		return this.keysub;
	}

	public BSON parseObject(Object obj) {

		return new BSON(getRawBytes(this.keysub, obj));
	}

	public static <T> byte[] getBytes(KeySubstitutor substitutor, T[] array) {

		if (array == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < array.length; i++) {
			byte[] b = getRawBytes(substitutor, array[i]);
			bos.write(b, 0, b.length);
		}
		return getBytesWithType(BSON.ARRAY, bos.toByteArray());
	}

	public static <T> byte[] getBytes(KeySubstitutor substitutor, Collection<T> array) {

		if (array == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (T a : array) {
			byte[] b = getRawBytes(substitutor, a);
			bos.write(b, 0, b.length);
		}
		return getBytesWithType(BSON.ARRAY, bos.toByteArray());
	}

	private static byte[] getBytesWithType(byte type, byte[] bytes) {

		byte[] retBytes = new byte[bytes.length + 5];
		retBytes[0] = type;
		byte[] size = getBytes(bytes.length);
		System.arraycopy(size, 1, retBytes, 1, 4);
		if (bytes.length != 0)
			System.arraycopy(bytes, 0, retBytes, 5, bytes.length);
		return retBytes;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> byte[] getRawBytes(KeySubstitutor substitutor, T a) {

		if (a == null)
			return new byte[] { BSON.NULL };

		byte[] b = null;
		if (a instanceof Short) {
			b = getBytes((short) a);
		} else if (a instanceof Byte) {
			b = getBytes((byte) a);
		} else if (a instanceof Integer) {
			b = getBytes((int) a);
		} else if (a instanceof Long) {
			b = getBytes((long) a);
		} else if (a instanceof Float) {
			b = getBytes((float) a);
		} else if (a instanceof Double) {
			b = getBytes((double) a);
		} else if (a instanceof String) {
			b = getBytes((String) a);
		} else if (a instanceof Object[]) {
			b = getBytes(substitutor, (Object[]) a);
		} else if (a instanceof Collection) {
			b = getBytes(substitutor, (Collection) a);
		} else if (a instanceof Map) {
			b = getBytes(substitutor, (Map) a);
		} else if (a instanceof Boolean) {
			b = getBytes((boolean) a);
		} else if (a instanceof BigDecimal) {
			b = getBytes((BigDecimal) a);
		} else if (a instanceof BigInteger) {
			b = getBytes((BigInteger) a);
		}

		return b;
	}

	public static <K, V> byte[] getBytes(KeySubstitutor substitutor, Map<K, V> map) {

		if (map == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Entry<K, V> eachEntry : map.entrySet()) {
			byte[] b = substitutor.getBytesFromKey(eachEntry.getKey().toString());
			bos.write(b, 0, b.length);
			b = getRawBytes(substitutor, eachEntry.getValue());
			bos.write(b, 0, b.length);
		}

		return getBytesWithType(BSON.OBJECT, bos.toByteArray());
	}

	public static byte[] getBytes() {

		return new byte[] { BSON.NULL };
	}

	public static byte[] getBytes(String s) {

		if (s == null)
			return getBytes();

		byte[] b = s.getBytes();
		byte[] e = new byte[b.length + 5];

		System.arraycopy(getBytes(b.length), 1, e, 1, 4);
		e[0] = BSON.STRING;
		System.arraycopy(b, 0, e, 5, b.length);

		return e;
	}

	public static byte[] getBytes(boolean b) {

		return new byte[] { b ? BSON.BOOLEAN_TRUE : BSON.BOOLEAN_FALSE };
	}

	public static byte[] getBytes(int i) {

		byte[] b = new byte[5];
		b[0] = BSON.INTEGER;
		for (byte k = 1; k <= 4; k++) {
			b[k] = (byte) (i & 0xff);
			i >>>= 8;
		}
		return b;
	}

	public static byte[] getBytes(byte by) {

		byte[] b = new byte[2];
		b[0] = BSON.BYTE;
		b[1] = by;
		return b;
	}

	public static byte[] getBytes(short s) {

		byte[] b = new byte[3];
		b[0] = BSON.SHORT;
		b[1] = (byte) (s & 0xff);
		b[2] = (byte) ((s >>> 8) & 0xff);
		return b;
	}

	public static byte[] getBytes(long l) {

		byte[] b = new byte[9];
		b[0] = BSON.LONG;
		for (byte k = 1; k <= 8; k++) {
			b[k] = (byte) (l & 0xff);
			l >>>= 8;
		}
		return b;
	}

	public static byte[] getBytes(float f) {

		byte[] b = getBytes(Float.floatToRawIntBits(f));
		b[0] = BSON.FLOAT;
		return b;
	}

	public static byte[] getBytes(double d) {

		byte[] b = getBytes(Double.doubleToRawLongBits(d));
		b[0] = BSON.DOUBLE;
		return b;
	}

	public static byte[] getBytes(BigInteger i) {

		byte[] b = i.toByteArray();
		byte[] e = new byte[b.length + 5];

		System.arraycopy(getBytes(b.length), 1, e, 1, 4);
		e[0] = BSON.BIG_INTEGER;
		System.arraycopy(b, 0, e, 5, b.length);

		return e;
	}

	public static byte[] getBytes(BigDecimal d) {

		byte[] b = getBytes(d.toString());
		b[0] = BSON.BIG_DECIMAL;
		return b;
	}
}

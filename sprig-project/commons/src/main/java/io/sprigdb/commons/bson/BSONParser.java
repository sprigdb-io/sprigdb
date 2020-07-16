package io.sprigdb.commons.bson;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class BSONParser {

	public static BSON parseObject(Object obj) {

		return new BSON(getRawBytes(obj));
	}

	public static <T> byte[] getBytes(T[] array) {

		if (array == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < array.length; i++) {
			byte[] b = getRawBytes(array[i]);
			bos.write(b, 0, b.length);
		}
		return getBytesWithType(BSON.ARRAY, bos.toByteArray());
	}

	public static <T> byte[] getBytes(Collection<T> array) {

		if (array == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (T a : array) {
			byte[] b = getRawBytes(a);
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
	public static <T> byte[] getRawBytes(T a) {

		if (a == null)
			return new byte[] { BSON.NULL };

		byte[] b = null;
		if (a instanceof Integer) {
			b = getBytes((int) a);
		} else if (a instanceof Long) {
			b = getBytes((long) a);
		} else if (a instanceof Double) {
			b = getBytes((double) a);
		} else if (a instanceof Float) {
			b = getBytes((float) a);
		} else if (a instanceof String) {
			b = getBytes((String) a);
		} else if (a instanceof Object[]) {
			b = getBytes((Object[]) a);
		} else if (a instanceof Collection) {
			b = getBytes((Collection) a);
		} else if (a instanceof Map) {
			b = getBytes((Map) a);
		} else if (a instanceof Boolean) {
			b = getBytes((boolean) a);
		}

		return b;
	}

	public static <T> byte[] getBytes(Map<String, T> map) {

		if (map == null)
			return getBytes();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Entry<String, T> eachEntry : map.entrySet()) {
			byte[] b = getBytes(eachEntry.getKey());
			bos.write(b, 0, b.length);
			b = getRawBytes(eachEntry.getValue());
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

	private BSONParser() {
	}
}

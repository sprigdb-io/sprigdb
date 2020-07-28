package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.sprigdb.commons.util.ArraysUtil;

class BSONParserTest {

	KeySubstitutor substitutor = new DefaultKeySubstitutor();

	@Test
	void testParseObject() {

		Map<String, Object> map = Map.of("int", (Object) 32, "float", (Object) 45f, "h", (Object) "hello string");
		byte[] mapBytes = map.entrySet().stream().map(
				e -> ArraysUtil.concat(BSONParser.getBytes(e.getKey()), BSONParser.getRawBytes(substitutor, (Object) e.getValue())))
				.reduce(ArraysUtil::concat).orElse(null);
		byte[] finBytes = new byte[5];
		int length = mapBytes.length;
		finBytes[0] = BSON.OBJECT;
		finBytes[1] = (byte) (0xff & length);
		finBytes[2] = (byte) ((0xff00 & length) >>> 8);
		finBytes[3] = (byte) ((0xff0000 & length) >>> 16);
		finBytes[4] = (byte) ((0xff000000 & length) >>> 24);
		assertArrayEquals(ArraysUtil.concat(finBytes, mapBytes), BSONParser.getBytes(substitutor, map));
		assertArrayEquals(BSONParser.getBytes(), BSONParser.getBytes(substitutor, (Map<String, Object>) null));
		assertArrayEquals(BSONParser.getBytes(), BSONParser.getBytes(substitutor, (List<String>) null));
		assertArrayEquals(BSONParser.getBytes(), BSONParser.getBytes(substitutor, (Object[]) null));
	}

	@Test
	void testGetBytesTArray() {
		Object[] arr = new Object[] { 10, 20l, 40f, 55d, true, false, Boolean.TRUE, Boolean.FALSE, "hello" };
		byte[] colBytes = Stream.of(arr).map(e -> BSONParser.getRawBytes(substitutor, e)).reduce(ArraysUtil::concat).orElse(null);
		byte[] finBytes = new byte[5];
		int length = colBytes.length;
		finBytes[0] = BSON.ARRAY;
		finBytes[1] = (byte) (0xff & length);
		finBytes[2] = (byte) ((0xff00 & length) >>> 8);
		finBytes[3] = (byte) ((0xff0000 & length) >>> 16);
		finBytes[4] = (byte) ((0xff000000 & length) >>> 24);
		assertArrayEquals(ArraysUtil.concat(finBytes, colBytes), BSONParser.getBytes(substitutor, arr));
		assertArrayEquals(new byte[] { BSON.ARRAY, 0, 0, 0, 0 }, BSONParser.getBytes(substitutor, new Byte[0]));
	}

	@Test
	void testGetBytesCollectionOfT() {
		List<Object> list = List.of("int", (Object) 32, "float", (Object) 45f, "h", (Object) "hello string");
		byte[] colBytes = list.stream().map(e -> BSONParser.getRawBytes(substitutor, e)).reduce(ArraysUtil::concat)
				.orElse(null);
		byte[] finBytes = new byte[5];
		int length = colBytes.length;
		finBytes[0] = BSON.ARRAY;
		finBytes[1] = (byte) (0xff & length);
		finBytes[2] = (byte) ((0xff00 & length) >>> 8);
		finBytes[3] = (byte) ((0xff0000 & length) >>> 16);
		finBytes[4] = (byte) ((0xff000000 & length) >>> 24);
		assertArrayEquals(ArraysUtil.concat(finBytes, colBytes), BSONParser.getBytes(substitutor, list));
	}

	@Test
	void testGetRawBytes() {

		assertArrayEquals(BSONParser.getBytes(), BSONParser.getRawBytes(substitutor, null));
		assertArrayEquals(BSONParser.getBytes(23), BSONParser.getRawBytes(substitutor, 23));
		assertArrayEquals(BSONParser.getBytes(23l), BSONParser.getRawBytes(substitutor, 23l));
		assertArrayEquals(BSONParser.getBytes(23f), BSONParser.getRawBytes(substitutor, 23f));
		assertArrayEquals(BSONParser.getBytes(23d), BSONParser.getRawBytes(substitutor, 23d));
		assertArrayEquals(BSONParser.getBytes(true), BSONParser.getRawBytes(substitutor, true));
		assertArrayEquals(BSONParser.getBytes(false), BSONParser.getRawBytes(substitutor, false));
		assertArrayEquals(BSONParser.getBytes("Hello"), BSONParser.getRawBytes(substitutor, "Hello"));
		assertArrayEquals(BSONParser.getBytes(substitutor, new Object[] { 10, 20l, 40f, 55d, true, false, "hello" }),
				BSONParser.getRawBytes(substitutor, new Object[] { 10, 20l, 40f, 55d, true, false, "hello" }));
		assertArrayEquals(BSONParser.getBytes(substitutor, List.of(10, 20l, 40f, 55d, true, false, "hello")),
				BSONParser.getRawBytes(substitutor, List.of(10, 20l, 40f, 55d, true, false, "hello")));
		Map<String, Object> map = Map.of("int", (Object) 32, "float", (Object) 45f, "h", (Object) "hello string");
		assertArrayEquals(BSONParser.getBytes(substitutor, map), BSONParser.getRawBytes(substitutor, map));
	}

	@Test
	void testGetBytes() {

		assertArrayEquals(new byte[] { BSON.NULL }, BSONParser.getBytes());
	}

	@Test
	void testGetBytesString() {

		String strings[] = { "", "hello", null, "汉字/漢字", "平仮名", "漢字", "кто ты", "с кем ты разговариваешь",
				"ਤੁਸੀਂ ਕਿਸ ਨਾਲ ਗੱਲ ਕਰ ਰਹੇ ਹੋ", "ко то причаш", "cò ris a tha thu a ’bruidhinn", "مع من انت تتكلم",
				"сен ким менен сүйлөшүп жатасың", "با کی حرف میزنی" };

		for (String each : strings) {
			if (each == null) {
				assertArrayEquals(new byte[] { BSON.NULL }, BSONParser.getBytes(each));
				continue;
			}
			byte[] bytes = each.getBytes();
			byte[] finBytes = new byte[bytes.length + 5];
			finBytes[0] = BSON.STRING;
			int length = bytes.length;
			finBytes[1] = (byte) (0xff & length);
			finBytes[2] = (byte) ((0xff00 & length) >>> 8);
			finBytes[3] = (byte) ((0xff0000 & length) >>> 16);
			finBytes[4] = (byte) ((0xff000000 & length) >>> 24);
			for (int i = 0; i < bytes.length; i++)
				finBytes[i + 5] = bytes[i];
			assertArrayEquals(finBytes, BSONParser.getBytes(each));
		}
	}

	@Test
	void testGetBytesBoolean() {

		assertArrayEquals(new byte[] { BSON.BOOLEAN_TRUE }, BSONParser.getBytes(true));
		assertArrayEquals(new byte[] { BSON.BOOLEAN_FALSE }, BSONParser.getBytes(false));
	}

	@Test
	void testGetBytesInt() {

		int[] values = new int[] { 0, 10, 300, -300, Integer.MIN_VALUE, Integer.MAX_VALUE };

		for (int each : values) {
			assertArrayEquals(
					new byte[] { BSON.INTEGER, (byte) (0xff & each), (byte) ((0xff00 & each) >>> 8),
							(byte) ((0xff0000 & each) >>> 16), (byte) ((0xff000000 & each) >>> 24) },
					BSONParser.getBytes(each));
		}
	}

	@Test
	void testGetBytesLong() {

		long[] values = new long[] { 0, 10, 300, -300, Integer.MIN_VALUE, Integer.MAX_VALUE };

		for (long each : values) {
			assertArrayEquals(
					new byte[] { BSON.LONG, (byte) (0xff & each), (byte) ((0xff00 & each) >>> 8),
							(byte) ((0xff0000 & each) >>> 16), (byte) ((0xff000000 & each) >>> 24),
							(byte) ((0xff00000000l & each) >>> 32), (byte) ((0xff0000000000l & each) >>> 40),
							(byte) ((0xff000000000000l & each) >>> 48), (byte) ((0xff00000000000000l & each) >>> 56) },
					BSONParser.getBytes(each));
		}
	}

	@Test
	void testGetBytesFloat() {

		float[] values = { 0f, -0.2f, -5432.4f, 4533.f, Float.MAX_VALUE, Float.MIN_NORMAL, Float.NEGATIVE_INFINITY,
				Float.POSITIVE_INFINITY, Float.NaN, Float.MAX_EXPONENT, Float.MIN_EXPONENT };

		for (float eachFloat : values) {

			int each = Float.floatToRawIntBits(eachFloat);
			assertArrayEquals(
					new byte[] { BSON.FLOAT, (byte) (0xff & each), (byte) ((0xff00 & each) >>> 8),
							(byte) ((0xff0000 & each) >>> 16), (byte) ((0xff000000 & each) >>> 24) },
					BSONParser.getBytes(eachFloat));
		}
	}

	@Test
	void testGetBytesDouble() {
		double[] values = { 0d, -0.2d, -5432.4d, 4533.d, Double.MAX_VALUE, Double.MIN_NORMAL, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.NaN, Double.MAX_EXPONENT, Double.MIN_EXPONENT };

		for (double eachDouble : values) {

			long each = Double.doubleToLongBits(eachDouble);
			assertArrayEquals(
					new byte[] { BSON.DOUBLE, (byte) (0xff & each), (byte) ((0xff00 & each) >>> 8),
							(byte) ((0xff0000 & each) >>> 16), (byte) ((0xff000000 & each) >>> 24),
							(byte) ((0xff00000000l & each) >>> 32), (byte) ((0xff0000000000l & each) >>> 40),
							(byte) ((0xff000000000000l & each) >>> 48), (byte) ((0xff00000000000000l & each) >>> 56) },
					BSONParser.getBytes(eachDouble));
		}
	}

}

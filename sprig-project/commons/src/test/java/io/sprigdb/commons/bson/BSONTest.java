package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.sprigdb.commons.exceptions.BSONDisorientedException;
import io.sprigdb.commons.exceptions.BSONEmptyException;

class BSONTest {

	@Test
	void constuctorTest() {
		assertThrows(NullPointerException.class, () -> new BSON(null));
		assertThrows(NullPointerException.class, () -> new BSON(null, 0, 10));
		assertThrows(BSONEmptyException.class, () -> new BSON(new byte[0]));
		assertThrows(BSONEmptyException.class, () -> new BSON(new byte[0]));
		assertThrows(BSONEmptyException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, 0, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, 0, 16));
		assertThrows(IndexOutOfBoundsException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, -1, 5));
		assertThrows(IndexOutOfBoundsException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, 8, 5));
		assertThrows(IndexOutOfBoundsException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, 3, 3));
		assertThrows(IndexOutOfBoundsException.class, () -> new BSON(new byte[] { 01, 02, 00, 00, 00 }, 0, -3));
	}

	@Test
	void testIsNull() {
		assertTrue(BSONParser.parseObject(null).isNull());
		assertFalse(BSONParser.parseObject(123).isNull());
	}

	@Test
	void testGetType() {
		assertEquals(BSON.NULL, BSONParser.parseObject(null).getType());
		assertEquals(BSON.INTEGER, BSONParser.parseObject(123).getType());
		assertEquals(BSON.DOUBLE, BSONParser.parseObject(23d).getType());
		assertEquals(BSON.ARRAY, BSONParser.parseObject(new Object[0]).getType());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetValue() {

		assertEquals(2, (Integer) BSONParser.parseObject(2).getValue());
		assertEquals(-2, (Integer) BSONParser.parseObject(-2).getValue());
		assertEquals(Integer.MIN_VALUE, (Integer) BSONParser.parseObject(Integer.MIN_VALUE).getValue());
		assertEquals(Integer.MAX_VALUE, (Integer) BSONParser.parseObject(Integer.MAX_VALUE).getValue());
		assertEquals(2l, (Long) BSONParser.parseObject(2l).getValue());
		assertEquals(-2l, (Long) BSONParser.parseObject(-2l).getValue());
		assertEquals(Long.MIN_VALUE, (Long) BSONParser.parseObject(Long.MIN_VALUE).getValue());
		assertEquals(Long.MAX_VALUE, (Long) BSONParser.parseObject(Long.MAX_VALUE).getValue());
		assertEquals(2f, (Float) BSONParser.parseObject(2f).getValue());
		assertEquals(-1.999999f, (Float) BSONParser.parseObject(-1.999999f).getValue());
		assertEquals(Float.MAX_VALUE, (Float) BSONParser.parseObject(Float.MAX_VALUE).getValue());
		assertEquals(Float.MIN_VALUE, (Float) BSONParser.parseObject(Float.MIN_VALUE).getValue());
		assertEquals(Float.MIN_NORMAL, (Float) BSONParser.parseObject(Float.MIN_NORMAL).getValue());
		assertEquals(2d, (Double) BSONParser.parseObject(2d).getValue());
		assertEquals(-1.2999999999999999d, (Double) BSONParser.parseObject(-1.2999999999999999d).getValue());
		assertEquals(Double.MAX_VALUE, (Double) BSONParser.parseObject(Double.MAX_VALUE).getValue());
		assertEquals(Double.MIN_VALUE, (Double) BSONParser.parseObject(Double.MIN_VALUE).getValue());
		assertEquals(Double.MIN_NORMAL, (Double) BSONParser.parseObject(Double.MIN_NORMAL).getValue());
		assertEquals("", (String) BSONParser.parseObject("").getValue());
		assertEquals(null, (String) BSONParser.parseObject(null).getValue());
		assertEquals(null, (Long) BSONParser.parseObject(null).getValue());
		assertEquals("asdf", (String) BSONParser.parseObject("asdf").getValue());

		String strings[] = { "", "hello", "汉字/漢字", "平仮名", "漢字", "кто ты", "с кем ты разговариваешь",
				"ਤੁਸੀਂ ਕਿਸ ਨਾਲ ਗੱਲ ਕਰ ਰਹੇ ਹੋ", "ко то причаш", "cò ris a tha thu a ’bruidhinn", "مع من انت تتكلم",
				"сен ким менен сүйлөшүп жатасың", "با کی حرف میزنی" };

		for (String eachString : strings) {
			assertEquals(eachString, (String) BSONParser.parseObject(eachString).getValue());
		}

		assertEquals("HI", (String) ((List<BSON>) BSONParser.parseObject(List.of("HI")).getValue()).get(0).getValue());

		BSON listBSON = BSONParser.parseObject(strings);
		assertEquals(BSON.ARRAY, listBSON.getType());
		List<BSON> list = listBSON.getValue();
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i).getValue(), strings[i]);
		}

		List<Object> lst = List.of(1, Float.MIN_VALUE, "hi", Double.MAX_VALUE, Float.NaN, true, false, Long.MAX_VALUE);
		listBSON = BSONParser.parseObject(lst);
		list = listBSON.getValue();
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i).getValue(), lst.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetAsMap() {

		Map<String, Object> map2 = new HashMap<>(
				Map.of("hi", 123124, "hello", 989898l, "TrueValue", true, "FalseValue", Boolean.FALSE));
		map2.put("nullTest", null);
		Map<String, Object> map = Map.of("hiout", 3.5f, "helloout", 2.5d, "another", map2);

		BSON outMap = BSONParser.parseObject(map);
		assertEquals(BSON.OBJECT, outMap.getType());
		final Map<String, BSON> bMap = outMap.getAsMap();
		assertEquals(map.get("hiout"), bMap.get("hiout").getValue());
		assertEquals(map.get("helloout"), bMap.get("helloout").getValue());
		BSON inMap = bMap.get("another");
		final Map<String, BSON> ccMap = inMap.getAsMap();
		assertEquals(map2.get("hi"), ccMap.get("hi").getValue());
		assertEquals(map2.get("hello"), ccMap.get("hello").getValue());
		assertNull(ccMap.get("nullTest").getValue());
		assertTrue(() -> (Boolean) ccMap.get("TrueValue").getValue());
		assertFalse(() -> (Boolean) ccMap.get("FalseValue").getValue());

		assertTrue(((List<BSON>) BSONParser.parseObject(List.of()).getValue()).isEmpty());
		assertTrue(BSONParser.parseObject(Map.of()).getAsMap().isEmpty());

		List<Object> listofMap = new ArrayList<>();
		listofMap.add(null);
		listofMap.add(map2);
		BSON blom = BSONParser.parseObject(listofMap);
		List<BSON> blist = blom.getValue();
		assertTrue(blist.get(0).isNull());
		assertEquals(map2.get("hi"), blist.get(1).getAsMap().get("hi").getValue());
		
		BSON b = BSONParser.parseObject(null);
		b.deSerialize(new byte[] {0x09, 0x0D, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x6B, 0x08, 0x01, 0x00, 0x00, 0x00, 0x4F});
		assertThrows(BSONDisorientedException.class, () -> b.getAsMap());
		b.deSerialize(new byte[] {0x09, 0x0A, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x6B, 0x08, 0x01, 0x00, 0x00, 0x00, 0x4F});
		assertThrows(BSONDisorientedException.class, () -> b.getAsMap());
		b.deSerialize(new byte[] {0x09, 0x0C, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x6B, 0x08, 0x01, 0x00, 0x00, 0x00, 0x4F});
		assertDoesNotThrow(() -> b.getAsMap());
	}

	@Test
	void testEqualsObject() {

		BSON b = BSONParser.parseObject(23);
		assertEquals(b, b);
		BSON map = BSONParser.parseObject(Map.of("kiran", 23));
		BSON value = map.getAsMap().get("kiran");
		assertEquals(b, value);
		int ival = 3;
		ival += 4;
		assertNotEquals(b, ival);
		BSON a = BSONParser.parseObject(24);
		assertNotEquals(b, a);
		BSON c = BSONParser.parseObject(24l);
		assertNotEquals(b, c);
	}

	@Test
	void testSerialize() {
		BSON b = BSONParser.parseObject(24);
		assertArrayEquals(new byte[] { BSON.INTEGER, 24, 0, 0, 0 }, b.serialize());
	}

	@Test
	void testDeSerialize() {
		BSON b = BSONParser.parseObject(null);
		b.deSerialize(new byte[] { BSON.INTEGER, 24, 0, 0, 0 });
		assertEquals(24, (int) b.getValue());
		assertThrows(NullPointerException.class, () -> b.deSerialize(null));
		assertThrows(BSONEmptyException.class, () -> b.deSerialize(new byte[] {}));
	}

	@Test
	void testHashCode() {

		BSON b = BSONParser.parseObject("hello");
		assertEquals(213585471, b.hashCode());
	}

	@Test
	void testToString() {
		BSON b = BSONParser.parseObject("hello");
		assertEquals("BSON Object :\n" + "[ offset: 0, length: 10]\n"
				+ "08 05 00 00 00 68 65 6C 6C 6F                    - .....hello\n", b.toString());
	}
}

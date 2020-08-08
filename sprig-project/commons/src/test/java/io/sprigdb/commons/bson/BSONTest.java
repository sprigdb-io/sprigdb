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
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import io.sprigdb.commons.exceptions.BSONDisorientedException;
import io.sprigdb.commons.exceptions.BSONEmptyException;

class BSONTest {

	BSONParser bsonParser = new BSONParser();

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
		assertTrue(bsonParser.parseObject(null).isNull());
		assertFalse(bsonParser.parseObject(123).isNull());
	}

	@Test
	void testGetType() {
		assertEquals(BSON.NULL, bsonParser.parseObject(null).getType());
		assertEquals(BSON.INTEGER, bsonParser.parseObject(123).getType());
		assertEquals(BSON.DOUBLE, bsonParser.parseObject(23d).getType());
		assertEquals(BSON.ARRAY, bsonParser.parseObject(new Object[0]).getType());
		assertEquals(BSON.SHORT, bsonParser.parseObject((short) 23).getType());
		assertEquals(BSON.BYTE, bsonParser.parseObject((byte) 23).getType());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetValue() {

		assertEquals(2, (Integer) bsonParser.parseObject(2).getValue());
		assertEquals(-2, (Integer) bsonParser.parseObject(-2).getValue());
		assertEquals(Integer.MIN_VALUE, (Integer) bsonParser.parseObject(Integer.MIN_VALUE).getValue());
		assertEquals(Integer.MAX_VALUE, (Integer) bsonParser.parseObject(Integer.MAX_VALUE).getValue());
		assertEquals(2l, (Long) bsonParser.parseObject(2l).getValue());
		assertEquals(-2l, (Long) bsonParser.parseObject(-2l).getValue());
		assertEquals(Long.MIN_VALUE, (Long) bsonParser.parseObject(Long.MIN_VALUE).getValue());
		assertEquals(Long.MAX_VALUE, (Long) bsonParser.parseObject(Long.MAX_VALUE).getValue());
		assertEquals(2f, (Float) bsonParser.parseObject(2f).getValue());
		assertEquals(-1.999999f, (Float) bsonParser.parseObject(-1.999999f).getValue());
		assertEquals(Float.MAX_VALUE, (Float) bsonParser.parseObject(Float.MAX_VALUE).getValue());
		assertEquals(Float.MIN_VALUE, (Float) bsonParser.parseObject(Float.MIN_VALUE).getValue());
		assertEquals(Float.MIN_NORMAL, (Float) bsonParser.parseObject(Float.MIN_NORMAL).getValue());
		assertEquals(2d, (Double) bsonParser.parseObject(2d).getValue());
		assertEquals(-1.2999999999999999d, (Double) bsonParser.parseObject(-1.2999999999999999d).getValue());
		assertEquals(Double.MAX_VALUE, (Double) bsonParser.parseObject(Double.MAX_VALUE).getValue());
		assertEquals(Double.MIN_VALUE, (Double) bsonParser.parseObject(Double.MIN_VALUE).getValue());
		assertEquals(Double.MIN_NORMAL, (Double) bsonParser.parseObject(Double.MIN_NORMAL).getValue());
		assertEquals("", (String) bsonParser.parseObject("").getValue());
		assertEquals(null, (String) bsonParser.parseObject(null).getValue());
		assertEquals(null, (Long) bsonParser.parseObject(null).getValue());
		assertEquals("asdf", (String) bsonParser.parseObject("asdf").getValue());

		String strings[] = { "", "hello", "汉字/漢字", "平仮名", "漢字", "кто ты", "с кем ты разговариваешь",
				"ਤੁਸੀਂ ਕਿਸ ਨਾਲ ਗੱਲ ਕਰ ਰਹੇ ਹੋ", "ко то причаш", "cò ris a tha thu a ’bruidhinn", "مع من انت تتكلم",
				"сен ким менен сүйлөшүп жатасың", "با کی حرف میزنی" };

		for (String eachString : strings) {
			assertEquals(eachString, (String) bsonParser.parseObject(eachString).getValue());
		}

		assertEquals("HI", (String) ((List<BSON>) bsonParser.parseObject(List.of("HI")).getValue()).get(0).getValue());

		BSON listBSON = bsonParser.parseObject(strings);
		assertEquals(BSON.ARRAY, listBSON.getType());
		List<BSON> list = listBSON.getValue();
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i).getValue(), strings[i]);
		}

		List<Object> lst = List.of(1, Float.MIN_VALUE, "hi", Double.MAX_VALUE, Float.NaN, true, false, Long.MAX_VALUE,
				(byte) -25, (short) 25000);
		listBSON = bsonParser.parseObject(lst);
		list = listBSON.getValue();
		for (int i = 0; i < list.size(); i++) {
			assertEquals(lst.get(i), list.get(i).getValue());
		}

		Map<String, Object> map2 = new HashMap<>(
				Map.of("hi", 123124, "hello", 989898l, "TrueValue", true, "FalseValue", Boolean.FALSE));
		BSON bson = bsonParser.parseObject(map2);
		assertEquals(bson, bson.getValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetAsMap() {

		Map<String, Object> map2 = new HashMap<>(
				Map.of("hi", 123124, "hello", 989898l, "TrueValue", true, "FalseValue", Boolean.FALSE));
		map2.put("nullTest", null);
		Map<String, Object> map = Map.of("hiout", 3.5f, "helloout", 2.5d, "another", map2);

		BSON outMap = bsonParser.parseObject(map);
		assertEquals(BSON.OBJECT, outMap.getType());
		final Map<String, BSON> bMap = convertToStringKeyMap(outMap.getAsBSONMap());
		assertEquals(map.get("hiout"), bMap.get("hiout").getValue());
		assertEquals(map.get("helloout"), bMap.get("helloout").getValue());
		BSON inMap = bMap.get("another");
		final Map<String, BSON> ccMap = convertToStringKeyMap(inMap.getAsBSONMap());
		assertEquals(map2.get("hi"), ccMap.get("hi").getValue());
		assertEquals(map2.get("hello"), ccMap.get("hello").getValue());
		assertNull(ccMap.get("nullTest").getValue());
		assertTrue(() -> (Boolean) ccMap.get("TrueValue").getValue());
		assertFalse(() -> (Boolean) ccMap.get("FalseValue").getValue());

		assertTrue(((List<BSON>) bsonParser.parseObject(List.of()).getValue()).isEmpty());
		assertTrue(bsonParser.parseObject(Map.of()).getAsBSONMap().isEmpty());

		List<Object> listofMap = new ArrayList<>();
		listofMap.add(null);
		listofMap.add(map2);
		BSON blom = bsonParser.parseObject(listofMap);
		List<BSON> blist = blom.getValue();
		assertTrue(blist.get(0).isNull());
		assertEquals(map2.get("hi"), convertToStringKeyMap(blist.get(1).getAsBSONMap()).get("hi").getValue());

		BSON b = bsonParser.parseObject(null);
		b.deSerialize(new byte[] { 0x09, 0x0D, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x6B, 0x08, 0x01, 0x00,
				0x00, 0x00, 0x4F });
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> b.getAsBSONMap());
		b.deSerialize(new byte[] { 0x09, 0x0C, 0x00, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x6B, 0x08, 0x01, 0x00,
				0x00, 0x00, 0x4F });
		assertDoesNotThrow(() -> b.getAsBSONMap());

		BSON elist = bsonParser.parseObject(List.of(1, 2, 3));
		assertThrows(BSONDisorientedException.class, () -> elist.getAsBSONMap());
	}

	private Map<String, BSON> convertToStringKeyMap(Map<BSON, BSON> asMap) {
		Map<String, BSON> sMap = new HashMap<>();
		for (Entry<BSON, BSON> eachEntry : asMap.entrySet()) {
			sMap.put(eachEntry.getKey().getValue(), eachEntry.getValue());
		}
		return sMap;
	}

	@Test
	void testEqualsObject() {

		BSON b = bsonParser.parseObject(23);
		assertEquals(b, b);
		BSON map = bsonParser.parseObject(Map.of("kiran", 23));
		BSON value = convertToStringKeyMap(map.getAsBSONMap()).get("kiran");
		assertEquals(b, value);
		int ival = 3;
		ival += 4;
		assertNotEquals(b, ival);
		BSON a = bsonParser.parseObject(24);
		assertNotEquals(b, a);
		BSON c = bsonParser.parseObject(24l);
		assertNotEquals(b, c);
	}

	@Test
	void testSerialize() {
		BSON b = bsonParser.parseObject(24);
		assertArrayEquals(new byte[] { BSON.INTEGER, 24, 0, 0, 0 }, b.serialize());
	}

	@Test
	void testDeSerialize() {
		BSON b = bsonParser.parseObject(null);
		b.deSerialize(new byte[] { BSON.INTEGER, 24, 0, 0, 0 });
		assertEquals(24, (int) b.getValue());
		assertThrows(NullPointerException.class, () -> b.deSerialize(null));
		assertThrows(BSONEmptyException.class, () -> b.deSerialize(new byte[] {}));
	}

	@Test
	void testHashCode() {

		BSON b = bsonParser.parseObject("hello");
		assertEquals(675414029, b.hashCode());
	}

	@Test
	void testToString() {
		BSON b = bsonParser.parseObject("hello");
		assertEquals("BSON Object :\n" + "[ offset: 0, length: 10]\n"
				+ "08 05 00 00 00 68 65 6C 6C 6F                    - .....hello\n", b.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJSONString() {
		assertEquals("2", bsonParser.parseObject(2).toJSONString(bsonParser.getKeySubstitutor()));
		assertEquals("2.23213", bsonParser.parseObject(2.23213).toJSONString(bsonParser.getKeySubstitutor()));
		assertEquals("-23232", bsonParser.parseObject(-23232).toJSONString(bsonParser.getKeySubstitutor()));
		assertEquals("1.23123123223213E9", bsonParser.parseObject(1231231232.23213d).toJSONString(bsonParser.getKeySubstitutor()));

		Map<String, Object> map2 = new HashMap<>(
				Map.of("hi", 123124, "hello", 989898l, "TrueValue", true, "FalseValue", Boolean.FALSE));
		map2.put("nullTest", null);
		map2.put("arr", List.of(Boolean.TRUE, "Kevvu"));
		Map<String, Object> map = Map.of("hiout", 3.5f, "helloout", 2.5f, "another", map2);

		String string = bsonParser.parseObject(map).toJSONString(bsonParser.getKeySubstitutor());
		BSON bsonMap = new JSONParser().parseJSONString(string);

		Map<String, Object> outMap = bsonMap.getAsMap(new DefaultKeySubstitutor());
		assertEquals(map.get("hiout"), outMap.get("hiout"));
		assertEquals(map.get("helloout"), outMap.get("helloout"));

		Map<String, Object> outMap2 = (Map<String, Object>) outMap.get("another");
		assertEquals(map2.get("hi"), outMap2.get("hi"));
	}

	@Test
	void testGetAsList() {

		List<Object> l = List.of(1, 245, 45000, 3535l, "Probably", Map.of());
		assertEquals(l, bsonParser.parseObject(l).getAsList(new DefaultKeySubstitutor()));
	}
}

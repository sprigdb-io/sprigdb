package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.sprigdb.commons.bson.JSONParser.Marker;
import io.sprigdb.commons.exceptions.JSONParseException;

class JSONParserTest {

	BSONParser bsonParser = new BSONParser();
	JSONParser jsonParser = new JSONParser();

	@Test
	void testParseJSONString() {

		assertTrue(jsonParser.parseJSONString(null).isNull());
		assertTrue(jsonParser.parseJSONString("").isNull());
		assertTrue(jsonParser.parseJSONString("null").isNull());

		assertEquals((byte) 2, (byte) jsonParser.parseJSONString("2").getValue());
		assertEquals(2.5f, (float) jsonParser.parseJSONString("2.5").getValue());
		assertEquals(123123123132123123l, (long) jsonParser.parseJSONString("123123123132123123").getValue());
		assertEquals(1234567890123456789012345678901234567890.5d,
				(double) jsonParser.parseJSONString("1234567890123456789012345678901234567890.5").getValue());

		BSON b = jsonParser.parseJSONString("[2,3,4,5]");
		List<BSON> list = b.getValue();
		assertEquals((byte) 2, (byte) list.get(0).getValue());
		assertEquals((byte) 3, (byte) list.get(1).getValue());
		assertEquals((byte) 4, (byte) list.get(2).getValue());
		assertEquals((byte) 5, (byte) list.get(3).getValue());

		b = jsonParser.parseJSONString("{\"k1\" : -12, \"k2\": +34.4}");
		Map<BSON, BSON> map = b.getAsBSONMap();
		assertEquals((byte) -12, (byte) map.get(bsonParser.parseObject("k1")).getValue());
		assertEquals(34.4f, (float) map.get(bsonParser.parseObject("k2")).getValue());

		assertThrows(JSONParseException.class, () -> jsonParser.parseJSONString("[a]"));

		b = jsonParser.parseJSONString("[\"asdf\",4,5]");
		list = b.getValue();
		assertEquals("asdf", (String) list.get(0).getValue());
		assertEquals((byte) 4, (byte) list.get(1).getValue());
		assertEquals((byte) 5, (byte) list.get(2).getValue());
		b = jsonParser.parseJSONString("[\"asdf\",\n4\n,\n5]");
		list = b.getValue();
		assertEquals("asdf", (String) list.get(0).getValue());
		assertEquals((byte) 4, (byte) list.get(1).getValue());
		assertEquals((byte) 5, (byte) list.get(2).getValue());
		b = jsonParser.parseJSONString("[\"\",\n224\n]");
		list = b.getValue();
		assertEquals("", (String) list.get(0).getValue());
		assertEquals((short) 224, (short) list.get(1).getValue());
	}

	@Test
	void testGetValue() {

		assertEquals((short) 232,
				(short) jsonParser.getValue(new Marker(2, Marker.NUMBER), 5, ": 232,".toCharArray()).getValue());
		assertEquals(2323324234234l, (long) jsonParser
				.getValue(new Marker(5, Marker.NUMBER), 19, ":    2323324234234\n,".toCharArray()).getValue());
		assertTrue((boolean) jsonParser.getValue(new Marker(0, Marker.BOOLEAN), 5, "true\n}".toCharArray()).getValue());
		assertFalse((boolean) jsonParser.getValue(new Marker(0, Marker.BOOLEAN), 5, "false".toCharArray()).getValue());
		assertTrue(jsonParser.getValue(new Marker(0, Marker.NULL), 4, "null".toCharArray()).isNull());
		assertEquals(22.4f,
				(float) jsonParser.getValue(new Marker(2, Marker.REAL_NUMBER), 6, "  22.4;".toCharArray()).getValue());
		assertEquals(122.4f,
				(float) jsonParser.getValue(new Marker(1, Marker.REAL_NUMBER), 6, " 122.4;".toCharArray()).getValue());
		assertEquals(1234567890123456789012345678901234567890.4d,
				(double) jsonParser.getValue(new Marker(0, Marker.REAL_NUMBER), 42,
						"1234567890123456789012345678901234567890.4;".toCharArray()).getValue());

		final Marker marker1 = new Marker(0, Marker.BOOLEAN);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker1, 5, new char[] { 't', 'r', 'p', 'e', '\n', '}' }));

		final Marker marker2 = new Marker(0, Marker.NULL);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker2, 6, new char[] { 'n', 'i', 'n', 'u', '\n', '"' }));

		final Marker marker3 = new Marker(0, Marker.NUMBER);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker3, 4, new char[] { '0', '4', '5', 'n' }));

		final Marker marker4 = new Marker(0, Marker.REAL_NUMBER);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker4, 4, new char[] { '0', '4', '.', 'n' }));

		final Marker marker5 = new Marker(0, Marker.STRING);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker5, 3, new char[] { 'a', 'b', 'c', 'd' }));
	}

	@Test
	void testMakeBSONMapOfBSONBSON() {

		BSON k1 = bsonParser.parseObject("k1");
		BSON v1 = bsonParser.parseObject(12);
		BSON k2 = bsonParser.parseObject("k2");
		BSON v2 = bsonParser.parseObject("Kiran");

		Map<BSON, BSON> m = Map.of(k1, v1, k2, v2);
		BSON map = jsonParser.makeBSON(m);
		Map<BSON, BSON> m1 = map.getAsBSONMap();
		assertEquals(12, (int) m1.get(k1).getValue());
		assertEquals("Kiran", (String) m1.get(k2).getValue());
	}

	@Test
	void testMakeBSONListOfBSON() {

		BSON a = bsonParser.parseObject(12);
		BSON b = bsonParser.parseObject("kiran");
		BSON c = bsonParser.parseObject(23.4f);

		BSON bson = jsonParser.makeBSON(List.of(a, b, c));
		List<BSON> list = bson.getValue();
		assertEquals(12, (int) list.get(0).getValue());
		assertEquals("kiran", (String) list.get(1).getValue());
		assertEquals(23.4f, (float) list.get(2).getValue());
	}
}

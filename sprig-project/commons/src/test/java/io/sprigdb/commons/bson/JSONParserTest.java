package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

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
//		b = jsonParser.parseJSONString("[\"\",\n224\n]");
//		list = b.getValue();
//		assertEquals("", (String) list.get(0).getValue());
//		assertEquals((short) 224, (short) list.get(1).getValue());
	}
}

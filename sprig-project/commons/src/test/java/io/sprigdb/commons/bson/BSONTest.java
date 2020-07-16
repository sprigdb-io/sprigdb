package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

class BSONTest {

	@Test
	void testIsNull() {
		fail("Not yet implemented");
	}

	@Test
	void testGetType() {
		fail("Not yet implemented");
	}

	@Test
	void testGetBSONEntries() {
		fail("Not yet implemented");
	}

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

		System.out.println((String) ((List<BSON>)BSONParser.parseObject(List.of("HI")).getValue()).get(0).getValue());
	}

	@Test
	void testEqualsObject() {
		fail("Not yet implemented");
	}

	@Test
	void testSerialize() {
		fail("Not yet implemented");
	}

	@Test
	void testDeSerialize() {
		fail("Not yet implemented");
	}

}

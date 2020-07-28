package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DefaultKeySubstitutorTest {

	BSONParser parser = new BSONParser();

	@Test
	void test() {

		DefaultKeySubstitutor dk = new DefaultKeySubstitutor();
		assertEquals(parser.parseObject(""), dk.getBSONFromKey(""));
		assertEquals(parser.parseObject(null), dk.getBSONFromKey(null));
		assertEquals(parser.parseObject("adf"), dk.getBSONFromKey("adf"));
		assertNotEquals(parser.parseObject("adf"), dk.getBSONFromKey("asdf"));

		assertEquals("hello", dk.getKeyWithBytes(BSONParser.getBytes("hello")));
		assertEquals("", dk.getKeyWithBytes(BSONParser.getBytes("")));
	}

}

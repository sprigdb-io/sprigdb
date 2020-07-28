package io.sprigdb.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringUtilTest {

	@Test
	void test() {
	
		assertEquals("", StringUtil.trimInvisible(""));
		assertEquals("1", StringUtil.trimInvisible("1"));
		assertEquals("", StringUtil.trimInvisible("\n"));
		assertEquals("", StringUtil.trimInvisible("\n\t"));
		assertEquals("123", StringUtil.trimInvisible("\n123"));
		assertEquals("12\n3", StringUtil.trimInvisible("\n12\n3"));
		assertEquals("12\n3", StringUtil.trimInvisible("\n12\n3\n\t"));
		assertEquals("1", StringUtil.trimInvisible("1\n\t"));
		assertEquals("1\n\tw", StringUtil.trimInvisible("1\n\tw"));
		assertEquals("1\n\tw", StringUtil.trimInvisible("1\n\tw\r\b\n"));
	}
}

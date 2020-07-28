package io.sprigdb.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArraysUtilTest {

	@Test
	void testConcat() {

		assertArrayEquals(new byte[] {}, ArraysUtil.concat(new byte[] {}, new byte[] {}));
		assertArrayEquals(new byte[] { 1 }, ArraysUtil.concat(new byte[] { 1 }, new byte[] {}));
		assertArrayEquals(new byte[] { 1 }, ArraysUtil.concat(new byte[] {}, new byte[] { 1 }));
		assertArrayEquals(new byte[] { 1, 2 }, ArraysUtil.concat(new byte[] { 1 }, new byte[] { 2 }));
	}

}

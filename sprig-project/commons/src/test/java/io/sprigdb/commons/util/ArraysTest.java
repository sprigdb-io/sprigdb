package io.sprigdb.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArraysTest {

	@Test
	void testConcat() {

		assertArrayEquals(new byte[] {}, Arrays.concat(new byte[] {}, new byte[] {}));
		assertArrayEquals(new byte[] { 1 }, Arrays.concat(new byte[] { 1 }, new byte[] {}));
		assertArrayEquals(new byte[] { 1 }, Arrays.concat(new byte[] {}, new byte[] { 1 }));
		assertArrayEquals(new byte[] { 1, 2 }, Arrays.concat(new byte[] { 1 }, new byte[] { 2 }));
	}

}

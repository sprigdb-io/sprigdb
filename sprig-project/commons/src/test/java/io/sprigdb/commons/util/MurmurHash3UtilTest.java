package io.sprigdb.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MurmurHash3UtilTest {

	@Test
	void test() {

		assertEquals(333523692, MurmurHash3Util.hashBytes(new byte[] { 0, 0, 0 }, 0, 3));
		assertEquals(-84185705, MurmurHash3Util.hashBytes(new byte[] { 0, 0 }, 0, 2));

		assertThrows(IndexOutOfBoundsException.class, () -> MurmurHash3Util.hashBytes(new byte[] { 0, 0, 0 }, 0, 4));
		assertThrows(IndexOutOfBoundsException.class, () -> MurmurHash3Util.hashBytes(new byte[] { 0, 0, 0 }, 3, 3));
	}

}

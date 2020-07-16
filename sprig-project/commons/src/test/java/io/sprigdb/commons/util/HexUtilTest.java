package io.sprigdb.commons.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HexUtilTest {

	@Test
	void test() {
		assertEquals("02", HexUtil.hexByte((byte) 2));
		assertEquals("0x02", HexUtil.hexByte((byte) 2, true));
		assertEquals("0xF4", HexUtil.hexByte((byte) 244, true));
		String str = HexUtil.readableHexArray(new byte[] {0}, 0, 1, 1);
		assertEquals("00  - .\n", str);
		str = HexUtil.readableHexArray(new byte[] {0, 10, 98, 65}, 0, 4, 2);
		assertEquals("00 0A  - ..\n62 41  - bA\n", str);
		str = HexUtil.readableHexArray(new byte[] {0, 10, 98, 65}, 1, 3, 2);
		assertEquals("0A 62  - .b\n41     - A\n", str);
	}
}

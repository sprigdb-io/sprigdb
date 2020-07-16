package io.sprigdb.commons.util;

public class HexUtil {

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static String hexByte(byte b) {
		return hexByte(b, false);
	}

	public static String hexByte(byte b, boolean prefix) {

		return (prefix ? "0x" : "") + HEX_CHARS[b >>> 4] + HEX_CHARS[b & 0x0f];
	}

	private HexUtil() {
	}
}

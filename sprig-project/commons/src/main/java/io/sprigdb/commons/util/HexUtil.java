package io.sprigdb.commons.util;

public class HexUtil {

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static String hexByte(byte b) {

		return hexByte(b, false);
	}

	public static String hexByte(byte b, boolean prefix) {

		return (prefix ? "0x" : "") + HEX_CHARS[(b >>> 4) & 0x0f] + HEX_CHARS[b & 0x0f];
	}

	public static String readableHexArray(byte[] b, int offset, int length, int bytesInARow) {

		StringBuilder sb = new StringBuilder();
		int total = (offset + length);
		int rows = (int) Math.ceil(length / (double) bytesInARow);
		for (int i = 0; i < rows; i++) {
			StringBuilder vString = new StringBuilder();
			for (int j = offset + (i * bytesInARow); j < offset + ((i + 1) * bytesInARow); j++) {
				sb.append(j < total ? hexByte(b[j], false) : "  ").append(' ');
				if (j < total)
					vString.append(b[j] > 32 && b[j] < 127 ? (char) b[j] : '.');
			}
			sb.append(" - ").append(vString.toString()).append('\n');
		}

		return sb.toString();
	}

	private HexUtil() {
	}
}

package io.sprigdb.commons.util;

// https://sites.google.com/site/murmurhash/
//
// MurmurHash
// By Austin Appleby (aappleby (AT) gmail)

public class MurmurHash3Util {

	private static final int CHUNK_SIZE = 4;

	private static final int C1 = 0xcc9e2d51;
	private static final int C2 = 0x1b873593;

	public static int hashBytes(byte[] input, int off, int len) {

		if (off < 0 || (off + len) < off || (off + len) > input.length) {
			throw new IndexOutOfBoundsException();
		}

		int h1 = 7393123;
		int i;
		for (i = 0; i + CHUNK_SIZE <= len; i += CHUNK_SIZE) {
			int k1 = mixK1(getIntLittleEndian(input, off + i));
			h1 = mixH1(h1, k1);
		}

		int k1 = 0;
		for (int shift = 0; i < len; i++, shift += 8) {
			k1 ^= ((input[off + i]) & 0xFF) << shift;
		}
		h1 ^= mixK1(k1);
		return fmix(h1, len);
	}

	private static int getIntLittleEndian(byte[] input, int offset) {
		return fromBytes(input[offset + 3], input[offset + 2], input[offset + 1], input[offset]);
	}

	private static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
		return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
	}

	private static int mixK1(int k1) {
		k1 *= C1;
		k1 = Integer.rotateLeft(k1, 15);
		k1 *= C2;
		return k1;
	}

	private static int mixH1(int h1, int k1) {
		h1 ^= k1;
		h1 = Integer.rotateLeft(h1, 13);
		h1 = h1 * 5 + 0xe6546b64;
		return h1;
	}

	private static int fmix(int h1, int length) {
		h1 ^= length;
		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;
		return h1;
	}

	private MurmurHash3Util() {
		
	}
}

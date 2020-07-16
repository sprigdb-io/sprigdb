package io.sprigdb.commons.bson;

import static io.sprigdb.commons.bson.BSONValueExtractors.BOOLEAN_FALSE_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.BOOLEAN_TRUE_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.BSON_LIST_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.BSON_OBJECT_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.DOUBLE_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.FLOAT_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.INTEGER_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.LONG_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.NULL_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.STRING_EXTRACTOR;

import java.util.LinkedHashMap;
import java.util.Map;

import io.sprigdb.commons.exceptions.BSONDisorientedException;
import io.sprigdb.commons.exceptions.BSONEmptyException;
import io.sprigdb.commons.serializer.Marshallable;
import io.sprigdb.commons.util.HexUtil;

public class BSON implements Marshallable {

	private static final long serialVersionUID = -768651109633666630L;

	public static final byte INTEGER = 1;
	public static final byte LONG = 2;
	public static final byte FLOAT = 3;
	public static final byte DOUBLE = 4;
	public static final byte NULL = 5;
	public static final byte BOOLEAN_TRUE = 6;
	public static final byte BOOLEAN_FALSE = 7;
	public static final byte STRING = 8;
	public static final byte OBJECT = 9;
	public static final byte ARRAY = 10;

	private static final BSONExtractor<?>[] EXTRACTORS = new BSONExtractor[] { NULL_EXTRACTOR, INTEGER_EXTRACTOR,
			LONG_EXTRACTOR, FLOAT_EXTRACTOR, DOUBLE_EXTRACTOR, NULL_EXTRACTOR, BOOLEAN_TRUE_EXTRACTOR,
			BOOLEAN_FALSE_EXTRACTOR, STRING_EXTRACTOR, BSON_OBJECT_EXTRACTOR, BSON_LIST_EXTRACTOR };
	byte[] bs;
	private int offset;
	private int length;

	public BSON(byte[] bs, int offset, int length) {

		if (bs == null) {
			throw new NullPointerException();
		}

		if (bs.length == 0 || length == 0) {
			throw new BSONEmptyException();
		}

		this.bs = bs;
		this.offset = offset;
		this.length = length;

		if ((this.offset < 0) || (this.offset > bs.length) || (this.length < 0)
				|| ((this.offset + this.length) - this.bs.length > 0)) {
			throw new IndexOutOfBoundsException();
		}
	}

	public BSON(byte[] bs) {
		this(bs, 0, bs.length);
	}

	public boolean isNull() {
		return getType() == NULL;
	}

	public byte getType() {
		return bs[offset];
	}

	public Map<String, BSON> getAsMap() {

		Map<String, BSON> map = new LinkedHashMap<>();
		int off = this.offset;
		int limit = INTEGER_EXTRACTOR.getValue(this, off);
		off += 5;
		limit += off;

		if (limit != (offset + length))
			throw new BSONDisorientedException();

		String key;
		int len = 0;
		while (off < limit) {

			len = 5 + INTEGER_EXTRACTOR.getValue(this, off);
			key = STRING_EXTRACTOR.getValue(this, off);
			off += len;

			if (this.bs[off] == BSON.INTEGER || this.bs[off] == BSON.FLOAT) {
				map.put(key, new BSON(this.bs, off, 5));
				off += 5;
			} else if (this.bs[off] == BSON.LONG || this.bs[off] == BSON.DOUBLE) {
				map.put(key, new BSON(this.bs, off, 9));
				off += 9;
			} else if (this.bs[off] == BSON.NULL || this.bs[off] == BSON.BOOLEAN_FALSE
					|| this.bs[off] == BSON.BOOLEAN_TRUE) {
				map.put(key, new BSON(this.bs, off, 1));
				off += 1;
			} else {
				len = 5 + INTEGER_EXTRACTOR.getValue(this, off);
				map.put(key, new BSON(this.bs, off, len));
				off += len;
			}
		}

		return map;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder("BSON Object :\n[ offset: ").append(this.offset).append(", length: ")
				.append(this.length).append("]\n");
		sb.append(HexUtil.readableHexArray(this.bs, this.offset, this.length, 16));

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) EXTRACTORS[this.getType()].getValue(this, offset);
	}

	@Override
	public int hashCode() {

		int result = 1;
		for (int i = this.offset; i < (this.offset + this.length); i++) {

			result = 31 * result + (this.bs[i] & 0xff) ^ (this.bs[i] >>> 32);
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof BSON))
			return false;

		if (obj == this)
			return true;

		BSON b = (BSON) obj;
		if (this.length != b.length)
			return false;

		int i = this.offset;
		int j = b.offset;

		while (i < (this.offset + this.length) && b.bs[j++] == this.bs[i++])
			;

		return (i == this.offset + this.length) && (j == b.offset + b.length);
	}

	@Override
	public byte[] serialize() {
		byte[] b = new byte[this.length];
		System.arraycopy(this.bs, this.offset, b, 0, this.length);
		return b;
	}

	@Override
	public void deSerialize(byte[] bytes) {
		
		if (bytes == null) {
			throw new NullPointerException();
		}

		if (bytes.length == 0) {
			throw new BSONEmptyException();
		}
		
		this.bs = bytes;
		this.offset = 0;
		this.length = bytes.length;
	}
}

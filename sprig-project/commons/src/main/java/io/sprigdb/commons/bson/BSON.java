package io.sprigdb.commons.bson;

import static io.sprigdb.commons.bson.BSONValueExtractors.*;
import static io.sprigdb.commons.bson.BSONValueExtractors.BOOLEAN_TRUE_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.BSON_LIST_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.BSON_OBJECT_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.DOUBLE_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.FLOAT_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.INTEGER_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.LONG_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.NULL_EXTRACTOR;
import static io.sprigdb.commons.bson.BSONValueExtractors.STRING_EXTRACTOR;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	public static final byte BYTE = 11;
	public static final byte SHORT = 12;
	public static final byte BIG_INTEGER = 13;
	public static final byte BIG_DECIMAL = 14;

	// order has to match with numbers above.
	private static final BSONExtractor<?>[] EXTRACTORS = new BSONExtractor[] { NULL_EXTRACTOR, INTEGER_EXTRACTOR,
			LONG_EXTRACTOR, FLOAT_EXTRACTOR, DOUBLE_EXTRACTOR, NULL_EXTRACTOR, BOOLEAN_TRUE_EXTRACTOR,
			BOOLEAN_FALSE_EXTRACTOR, STRING_EXTRACTOR, BSON_OBJECT_EXTRACTOR, BSON_LIST_EXTRACTOR, BYTE_EXTRACTOR,
			SHORT_EXTRACTOR, BIG_INTEGER_EXTRACTOR, BIG_DECIMAL_EXTRACTOR };

	byte[] bs;
	int offset;
	int length;

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

	public Map<BSON, BSON> getAsBSONMap() {

		List<BSON> values = BSONValueExtractors.BSON_LIST_EXTRACTOR.getValue(this, this.offset);
		if (values.isEmpty())
			return Map.of();

		if (values.size() % 2 == 1) {
			throw new BSONDisorientedException();
		}

		Map<BSON, BSON> map = new HashMap<>();
		for (int i = 0; i < values.size() / 2; i++) {
			map.put(values.get(i * 2), values.get((i * 2) + 1));
		}

		return map;
	}

	public Map<String, Object> getAsMap(KeySubstitutor substitutor) {

		List<BSON> values = BSONValueExtractors.BSON_LIST_EXTRACTOR.getValue(this, this.offset);
		if (values.isEmpty())
			return Map.of();

		if (values.size() % 2 == 1) {
			throw new BSONDisorientedException();
		}

		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < values.size() / 2; i++) {

			BSON b = values.get((i * 2) + 1);
			Object o = null;

			if (b.getType() == ARRAY) {
				o = b.getAsList(substitutor);
			} else if (b.getType() == OBJECT) {
				o = b.getAsMap(substitutor);
			} else {
				o = EXTRACTORS[b.getType()].getValue(b, b.offset);
			}

			map.put(substitutor.getKeyWithBSON(values.get(i * 2)), o);
		}

		return map;
	}

	public List<Object> getAsList(KeySubstitutor substitutor) {

		List<BSON> values = BSONValueExtractors.BSON_LIST_EXTRACTOR.getValue(this, this.offset);
		if (values.isEmpty())
			return List.of();

		List<Object> list = new LinkedList<>();
		for (int i = 0; i < values.size(); i++) {

			BSON b = values.get(i);
			Object o = null;

			if (b.getType() == ARRAY) {
				o = b.getAsList(substitutor);
			} else if (b.getType() == OBJECT) {
				o = b.getAsMap(substitutor);
			} else {
				o = EXTRACTORS[b.getType()].getValue(b, b.offset);
			}
			list.add(o);
		}

		return list;
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

	public String toJSONString() {
		return recursiveJSONBuild(this).toString();
	}

	private StringBuilder recursiveJSONBuild(BSON obj) {

		switch (obj.getType()) {
		case INTEGER:
		case LONG:
		case FLOAT:
		case BYTE:
		case SHORT:
		case DOUBLE:
		case BIG_DECIMAL:
		case BIG_INTEGER:
			return new StringBuilder((EXTRACTORS[obj.getType()].getValue(obj, obj.offset)).toString());

		case BOOLEAN_FALSE:
			return new StringBuilder("false");

		case BOOLEAN_TRUE:
			return new StringBuilder("true");

		case STRING:
			return new StringBuilder("\"").append(((String) obj.getValue())).append("\"");

		case ARRAY:
			return toJSONArrayString(obj);

		case OBJECT:
			return toJSONObjectString(obj);

		default:
			return new StringBuilder("null");
		}
	}

	private StringBuilder toJSONObjectString(BSON obj) {

		StringBuilder sb = new StringBuilder("{");
		List<BSON> values = BSONValueExtractors.BSON_LIST_EXTRACTOR.getValue(obj, obj.offset);
		int lmt = values.size() / 2;
		for (int i = 0; i < lmt; i++) {
			sb.append(this.recursiveJSONBuild(values.get(i * 2))).append(':');
			BSON b = values.get((i * 2) + 1);
			if (b.getType() == OBJECT)
				sb.append(b.toJSONString());
			else
				sb.append(this.recursiveJSONBuild(b));
			if (i + 1 != lmt)
				sb.append(',');
		}
		sb.append('}');
		return sb;
	}

	private StringBuilder toJSONArrayString(BSON obj) {

		StringBuilder sb = new StringBuilder("[");
		List<BSON> lst = obj.getValue();
		for (int i = 0; i < lst.size(); i++) {
			sb.append(this.recursiveJSONBuild(lst.get(i)));
			if (i + 1 != lst.size())
				sb.append(',');
		}
		sb.append(']');
		return sb;
	}
}

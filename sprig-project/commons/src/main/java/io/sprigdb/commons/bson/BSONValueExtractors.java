package io.sprigdb.commons.bson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BSONValueExtractors {

	public static final BSONExtractor<Byte> BYTE_EXTRACTOR = (obj, off) -> (byte) (0xff & obj.bs[off + 1]);

	public static final BSONExtractor<Short> SHORT_EXTRACTOR = (obj, off) -> {

		short s = (short) (0xff & obj.bs[off + 2]);
		s <<= 8;
		s |= (short) (0xff & obj.bs[off + 1]);
		return s;
	};

	public static final BSONExtractor<Integer> INTEGER_EXTRACTOR = (obj, off) -> {

		int i = (0xff & obj.bs[off + 4]);
		for (byte k = 3; k > 0; k--) {
			i <<= 8;
			i |= (0xff & obj.bs[off + k]);
		}

		return i;
	};

	public static final BSONExtractor<Long> LONG_EXTRACTOR = (obj, off) -> {

		long l = (0xff & obj.bs[off + 8]);
		for (byte k = 7; k > 0; k--) {
			l <<= 8;
			l |= (0xff & obj.bs[off + k]);
		}

		return l;
	};

	public static final BSONExtractor<BigInteger> BIG_INTEGER_EXTRACTOR = (obj, off) -> new BigInteger(obj.bs, off + 5,
			(INTEGER_EXTRACTOR.getValue(obj, off)));

	public static final BSONExtractor<String> STRING_EXTRACTOR = (obj, off) -> new String(obj.bs, off + 5,
			(INTEGER_EXTRACTOR.getValue(obj, off)));

	public static final BSONExtractor<BigDecimal> BIG_DECIMAL_EXTRACTOR = (obj,
			off) -> new BigDecimal(STRING_EXTRACTOR.getValue(obj, off));

	public static final BSONExtractor<Float> FLOAT_EXTRACTOR = (obj, off) -> Float
			.intBitsToFloat(INTEGER_EXTRACTOR.getValue(obj, off));

	public static final BSONExtractor<Double> DOUBLE_EXTRACTOR = (obj, off) -> Double
			.longBitsToDouble(LONG_EXTRACTOR.getValue(obj, off));

	public static final BSONExtractor<Object> NULL_EXTRACTOR = (obj, off) -> null;

	public static final BSONExtractor<Boolean> BOOLEAN_TRUE_EXTRACTOR = (obj, off) -> Boolean.TRUE;

	public static final BSONExtractor<Boolean> BOOLEAN_FALSE_EXTRACTOR = (obj, off) -> Boolean.FALSE;

	public static final BSONExtractor<List<BSON>> BSON_LIST_EXTRACTOR = (obj, off) -> {

		List<BSON> list = new ArrayList<>();
		int limit = INTEGER_EXTRACTOR.getValue(obj, off);
		off += 5;
		limit += off;

		int length;
		while (off < limit) {

			if (obj.bs[off] == BSON.BYTE) {
				length = 2;
			} else if (obj.bs[off] == BSON.SHORT) {
				length = 3;
			} else if (obj.bs[off] == BSON.INTEGER || obj.bs[off] == BSON.FLOAT) {
				length = 5;
			} else if (obj.bs[off] == BSON.LONG || obj.bs[off] == BSON.DOUBLE) {
				length = 9;
			} else if (obj.bs[off] == BSON.NULL || obj.bs[off] == BSON.BOOLEAN_FALSE
					|| obj.bs[off] == BSON.BOOLEAN_TRUE) {
				length = 1;
			} else {
				length = 5 + INTEGER_EXTRACTOR.getValue(obj, off);
			}

			list.add(new BSON(obj.bs, off, length));
			off += length;
		}

		return list;
	};

	public static final BSONExtractor<BSON> BSON_OBJECT_EXTRACTOR = (obj, off) -> obj;

	private BSONValueExtractors() {
	}
}

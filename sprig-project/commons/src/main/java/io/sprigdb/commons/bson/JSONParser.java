package io.sprigdb.commons.bson;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.sprigdb.commons.exceptions.JSONParseException;
import io.sprigdb.commons.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class JSONParser {

	private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
	private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

	private static final BigDecimal DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE);
	private static final BigDecimal DOUBLE_MIN = BigDecimal.valueOf(Double.MIN_VALUE);

	private KeySubstitutor keysub;
	private BSONParser bsonParser;

	public JSONParser() {

		this(new DefaultKeySubstitutor());
	}

	public JSONParser(KeySubstitutor keysub) {
		this(new BSONParser(keysub));
	}

	public JSONParser(BSONParser bsonParser) {

		this.keysub = bsonParser.getKeySubstitutor();
		this.bsonParser = bsonParser;
	}

	public KeySubstitutor getKeySubstitutor() {

		return this.keysub;
	}

	public BSON parseJSONString(String json) {

		if (json == null || json.length() == 0) {
			return this.bsonParser.parseObject(null);
		}

		char[] c = json.toCharArray();

		int i = 0;
		LinkedList<Marker> markers = new LinkedList<>();
		markers.add(new Marker(0, Marker.BEGIN));

		int currentType;
		BSON lastB = null;
		LinkedList<Map<BSON, BSON>> mapList = new LinkedList<>();
		LinkedList<List<BSON>> listList = new LinkedList<>();
		LinkedList<BSON> lastBList = new LinkedList<>();

		while (i < c.length) {

			currentType = markers.peekLast().getType();

			if (c[i] == 't' || c[i] == 'f') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_VALUE_START
						|| currentType == Marker.ARRAY || currentType == Marker.ARRAY_VALUE_STARTED) {
					if (currentType == Marker.ARRAY)
						markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
					markers.add(new Marker(i, Marker.BOOLEAN));
				} else if (currentType != Marker.STRING) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if (c[i] == 'n') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_VALUE_START
						|| currentType == Marker.ARRAY || currentType == Marker.ARRAY_VALUE_STARTED) {
					if (currentType == Marker.ARRAY)
						markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
					markers.add(new Marker(i, Marker.NULL));
				} else if (currentType != Marker.STRING) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if ((c[i] >= '0' && c[i] <= '9') || c[i] == '-' || c[i] == '+') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_KEY_START
						|| currentType == Marker.OBJECT_VALUE_START || currentType == Marker.ARRAY
						|| currentType == Marker.ARRAY_VALUE_STARTED) {
					if (currentType == Marker.ARRAY)
						markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
					markers.add(new Marker(i, Marker.NUMBER));
				} else if (currentType != Marker.STRING && currentType != Marker.NUMBER
						&& currentType != Marker.REAL_NUMBER) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if (c[i] == '"') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_KEY_START
						|| currentType == Marker.OBJECT_VALUE_START || currentType == Marker.ARRAY
						|| currentType == Marker.ARRAY_VALUE_STARTED) {
					if (currentType == Marker.ARRAY)
						markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
					markers.add(new Marker(i, Marker.STRING));
				} else if (currentType == Marker.STRING) {

					char[] cArray = new char[i - markers.peekLast().getStart() - 1];
					System.arraycopy(c, markers.peekLast().getStart() + 1, cArray, 0,
							i - markers.peekLast().getStart() - 1);
					markers.removeLast();
					String value = new String(cArray);
					int lastType = markers.peekLast().getType();
					if (lastType == Marker.BEGIN) {
						lastB = this.bsonParser.parseObject(value);
					} else if (lastType == Marker.OBJECT_VALUE_START) {

						if (lastB == null) {
							throw new JSONParseException(
									"Exception at " + i + " unable to find the key for the value : " + value);
						}
						mapList.peekLast().put(lastB, this.bsonParser.parseObject(value));
					} else if (lastType == Marker.OBJECT_KEY_START) {
						lastB = this.keysub.getBSONFromKey(value);
						markers.add(new Marker(i, Marker.OBJECT_KEY_END));
					} else if (lastType == Marker.ARRAY_VALUE_STARTED) {
						lastB = this.bsonParser.parseObject(value);
						listList.peekLast().add(lastB);
					}
				}
			} else if (c[i] == ',') {

				if (currentType == Marker.BOOLEAN || currentType == Marker.NULL || currentType == Marker.NUMBER
						|| currentType == Marker.REAL_NUMBER) {
					BSON v = getValue(markers.pollLast(), i, c);
					byte lastType = markers.peekLast().getType();
					if (lastType == Marker.ARRAY_VALUE_STARTED) {
						listList.peekLast().add(v);
					} else if (lastType == Marker.OBJECT_VALUE_START) {
						mapList.peekLast().put(lastB, v);
						markers.pollLast();
					}
					lastB = null;
				} else if (currentType == Marker.BEGIN
						|| ((currentType == Marker.ARRAY_VALUE_STARTED || currentType == Marker.ARRAY)
								&& listList.peekLast().isEmpty())
						|| (currentType == Marker.OBJECT_KEY_START && mapList.peekLast().isEmpty())) {
					throw new JSONParseException(errorString(c[i], i));
				} else if (lastB == null && currentType == Marker.ARRAY_VALUE_STARTED) {
					throw new JSONParseException(errorString(c[i], i));
				} else {
					lastB = null;
				}
			} else if (c[i] == '{') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_VALUE_START) {
					mapList.add(new HashMap<>());
					if (currentType == Marker.OBJECT_VALUE_START) {
						lastBList.add(lastB);
						lastB = null;
					}
					markers.add(new Marker(i, Marker.OBJECT_KEY_START));
				} else if (currentType == Marker.ARRAY) {
					markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
				} else if (currentType != Marker.STRING) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if (c[i] == '}') {

				if (currentType == Marker.BOOLEAN || currentType == Marker.NULL || currentType == Marker.NUMBER
						|| currentType == Marker.REAL_NUMBER) {

					BSON value = getValue(markers.pollLast(), i, c);
					mapList.peekLast().put(lastB, value);
					currentType = markers.peekLast().getType();
				}
				if (currentType == Marker.OBJECT_VALUE_START) {

					lastB = makeBSON(mapList.pollLast());
					if (!mapList.isEmpty())
						mapList.getLast().put(lastBList.pollLast(), lastB);
					markers.pollLast(); // removes object value start
					markers.pollLast(); // removes object key start
					if (markers.size() != 1)
						markers.pollLast(); // removes object value start of parent
				} else if (currentType == Marker.OBJECT_KEY_START) {
					markers.pollLast();
					lastB = makeBSON(mapList.pollLast());
				}
			} else if (c[i] == '[') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_VALUE_START
						|| currentType == Marker.ARRAY || currentType == Marker.ARRAY_VALUE_STARTED) {
					if (currentType == Marker.OBJECT_VALUE_START) {
						lastBList.add(lastB);
					} else if (currentType == Marker.ARRAY) {
						markers.add(new Marker(i, Marker.ARRAY_VALUE_STARTED));
					}
					markers.add(new Marker(i, Marker.ARRAY));
					listList.add(new LinkedList<>());
					lastB = null;
				} else if (currentType != Marker.STRING) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if (c[i] == ']') {

				if (currentType == Marker.BEGIN || currentType == Marker.OBJECT_KEY_END
						|| currentType == Marker.OBJECT_KEY_START) {
					throw new JSONParseException(errorString(c[i], i));
				}
				if (currentType == Marker.BOOLEAN || currentType == Marker.NULL || currentType == Marker.NUMBER
						|| currentType == Marker.REAL_NUMBER) {
					lastB = getValue(markers.pollLast(), i, c);
					listList.peekLast().add(lastB);
					currentType = markers.peekLast().getType();
				}
				if (currentType == Marker.ARRAY_VALUE_STARTED || currentType == Marker.ARRAY) {
					lastB = makeBSON(listList.pollLast());
					if (currentType == Marker.ARRAY_VALUE_STARTED)
						markers.pollLast(); // removes the array started
					markers.pollLast();
					currentType = markers.peekLast().getType();
				}
				if (currentType == Marker.OBJECT_VALUE_START) {

					mapList.getLast().put(lastBList.pollLast(), lastB);
					markers.pollLast();
				} else if (currentType == Marker.ARRAY || currentType == Marker.ARRAY_VALUE_STARTED) {
					listList.getLast().add(lastB);
				}
			} else if (c[i] == ':') {
				if (currentType == Marker.OBJECT_KEY_END) {
					markers.pollLast(); // removes key end
					markers.add(new Marker(i, Marker.OBJECT_VALUE_START));
				} else if (currentType != Marker.STRING) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else if ((c[i] == '.' || c[i] == 'e' || c[i] == 'E')) {
				if (currentType == Marker.NUMBER || currentType == Marker.REAL_NUMBER)
					markers.peekLast().setType(Marker.REAL_NUMBER);
				else if ((currentType != Marker.STRING) && (c[i] != '.' && currentType != Marker.BOOLEAN)) {
					throw new JSONParseException(errorString(c[i], i));
				}
			} else {
				if ((currentType == Marker.ARRAY || currentType == Marker.OBJECT_KEY_START
						|| currentType == Marker.OBJECT_VALUE_START || currentType == Marker.BEGIN) && (c[i] > ' ')
						&& (c[i] != 65279)) {
					throw new JSONParseException(errorString(c[i], i));
				}
			}

			i++;
		}

		byte lastType = markers.peekLast().getType();
		if (lastType == Marker.BOOLEAN || lastType == Marker.NULL || lastType == Marker.NUMBER
				|| lastType == Marker.REAL_NUMBER) {
			lastB = getValue(markers.pollLast(), i, c);
		}

		if (markers.peekLast().getType() != Marker.BEGIN)
			throw new JSONParseException("Illformed JSON");

		return lastB;
	}

	private String errorString(char c, int i) {
		return "Unable to parse near : '" + c + "' at position : " + (i + 1);
	}

	protected BSON getValue(Marker marker, int i, char[] c) {

		char[] cArray = new char[i - marker.getStart()];
		System.arraycopy(c, marker.getStart(), cArray, 0, i - marker.getStart());
		String s = new String(StringUtil.trimInvisible(cArray));

		switch (marker.getType()) {

		case Marker.BOOLEAN:

			return getBooleanValue(s);
		case Marker.NULL:

			return getNullValue(s);
		case Marker.NUMBER:

			return getNumberValue(s);
		case Marker.REAL_NUMBER:

			return getRealNumberValue(s);
		default:

			throw new JSONParseException("Unexpected value : '" + s + "'");
		}
	}

	private BSON getRealNumberValue(String s) {
		try {

			BigDecimal bd = new BigDecimal(s);
			if (bd.compareTo(DOUBLE_MAX) > 0 || bd.compareTo(DOUBLE_MIN) < 0)
				return bsonParser.parseObject(bd);

			Double d = bd.doubleValue();
			float floatVal = d.floatValue();

			if (Math.abs(((double) d.floatValue()) - d) < 0.00005)
				return this.bsonParser.parseObject(floatVal);
			else
				return this.bsonParser.parseObject(d);
		} catch (Exception ex) {
			throw new JSONParseException("Unable to parse to a number : '" + s + "'", ex);
		}
	}

	private BSON getNumberValue(String s) {

		try {
			BigInteger bi = new BigInteger(s);
			if (bi.compareTo(LONG_MAX) > 0 || bi.compareTo(LONG_MIN) < 0)
				return this.bsonParser.parseObject(bi);

			Long l = bi.longValue();
			if (l >= -128l && l <= 127l)
				return this.bsonParser.parseObject(l.byteValue());
			else if (l >= -32768l && l <= 32767l)
				return this.bsonParser.parseObject(l.shortValue());
			else if (l >= -2147483648l && l <= 2147483647l)
				return this.bsonParser.parseObject(l.intValue());
			else
				return this.bsonParser.parseObject(l);
		} catch (Exception ex) {
			throw new JSONParseException("Unable to parse to a number : '" + s + "'");
		}
	}

	private BSON getNullValue(String s) {
		if (s.equals("null"))
			return this.bsonParser.parseObject(null);
		throw new JSONParseException("Expected null and found '" + s + "'");
	}

	private BSON getBooleanValue(String s) {
		Boolean b = null;
		if (s.equals("true"))
			b = Boolean.TRUE;
		else if (s.equals("false"))
			b = Boolean.FALSE;
		else
			throw new JSONParseException("Expected true or false and found '" + s + "'");
		return this.bsonParser.parseObject(b);
	}

	protected BSON makeBSON(Map<BSON, BSON> map) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Entry<BSON, BSON> eachEntry : map.entrySet()) {
			BSON v = eachEntry.getKey();
			bos.write(v.bs, v.offset, v.length);
			v = eachEntry.getValue();
			bos.write(v.bs, v.offset, v.length);
		}
		byte[] size = BSONParser.getBytes(bos.size());
		byte[] b = new byte[bos.size() + 5];
		b[0] = BSON.OBJECT;
		System.arraycopy(size, 1, b, 1, 4);
		System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());
		return new BSON(b);
	}

	protected BSON makeBSON(List<BSON> list) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (BSON v : list) {
			bos.write(v.bs, v.offset, v.length);
		}
		byte[] size = BSONParser.getBytes(bos.size());
		byte[] b = new byte[bos.size() + 5];
		b[0] = BSON.ARRAY;
		System.arraycopy(size, 1, b, 1, 4);
		System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());
		return new BSON(b);
	}

	@AllArgsConstructor
	@Data
	protected static class Marker {

		public static final byte BEGIN = 0;
		public static final byte NUMBER = 1;
		public static final byte STRING = 2;
		public static final byte BOOLEAN = 3;
		public static final byte NULL = 4;
		public static final byte OBJECT_KEY_START = 6;
		public static final byte OBJECT_KEY_END = 7;
		public static final byte OBJECT_VALUE_START = 8;
		public static final byte ARRAY = 9;
		public static final byte ARRAY_VALUE_STARTED = 10;
		public static final byte ARRAY_VALUE_ENDED = 11;
		public static final byte REAL_NUMBER = 12;

		private int start;
		private byte type;
	}
}

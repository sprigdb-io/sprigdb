package io.sprigdb.commons.bson;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

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
		return parseJSONString(c, 0, c.length);
	}

	public BSON parseJSONString(char[] c, int off, int length) {

		LinkedList<Marker> markers = new LinkedList<>();
		LinkedList<DataObject> values = new LinkedList<>();

		int i = 0;
		byte currentType;
		markers.push(new Marker(Marker.BEGIN, off));
		int totalLength = off + length;
		int loopCounter = 0;

		while (i < totalLength) {

			currentType = markers.peek().getType();

			switch (currentType) {

			case Marker.ARRAY:
			case Marker.OBJECT_VALUE_START:
			case Marker.OBJECT_KEY_START:
			case Marker.BEGIN:
				i = processBegin(c, i, markers, values);
				break;
			case Marker.NUMBER:
				i = processNumber(c, i, markers, values);
				break;
			case Marker.BOOLEAN:
				i = processBoolean(c, i, markers, values);
				break;
			case Marker.NULL:
				i = processNull(c, i, markers, values);
				break;
			case Marker.STRING:
				i = processString(c, i, markers, values);
				break;
			case Marker.OBJECT:
				i = processObject(c, i, markers);
				break;
			case Marker.OBJECT_KEY_END:
				i = processObjectKeyEnd(c, i, markers);
				break;
			case Marker.OBJECT_VALUE_END:
				i = processObjectValueEnd(c, i, markers, values);
				break;
			case Marker.ARRAY_END:
				i = processArrayEnd(i, markers, values);
				break;
			default:
				i++;
			}

			loopCounter++;
			if (loopCounter > length * 2) {

				throw new JSONParseException(errorString(c[i], i, null));
			}
		}

		if (markers.peek().getType() != Marker.BEGIN) {

			throw new JSONParseException("Illformed JSON");
		}

		return values.pop().getBson();
	}

	private int processArrayEnd(int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		LinkedList<BSON> list = new LinkedList<>();
		while (values.peek().getType() != DataObject.BREAK) {
			list.addFirst(values.pop().getBson());
		}
		values.pop();

		markers.pop();
		if (markers.peek().getType() != Marker.ARRAY) {
			throw new JSONParseException("Unable to parse an Array.");
		}
		markers.pop();

		for (BSON v : list) {
			bos.write(v.bs, v.offset, v.length);
		}
		byte[] size = BSONParser.getBytes(bos.size());
		byte[] b = new byte[bos.size() + 5];
		b[0] = BSON.ARRAY;
		System.arraycopy(size, 1, b, 1, 4);
		System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());

		boolean vs = markers.peek().getType() == Marker.OBJECT_VALUE_START;
		if (vs)
			markers.pop();
		values.push(new DataObject(DataObject.VALUE, new BSON(b)));
		if (vs)
			markers.push(new Marker(Marker.OBJECT_VALUE_END, i + 1));

		return i + 1;
	}

	private int processObject(char[] c, int i, LinkedList<Marker> markers) {

		int p = i;

		while (p < c.length && c[p] != '"') {

			if (c[p] > ' ')
				throw new JSONParseException("Unknown symbol found when expecting a '\"' at position : " + p);
			p++;
		}
		if (c[p] != '"') {
			throw new JSONParseException("Unknown symbol found when expecting a '\"' at position : " + p);
		}
		markers.push(new Marker(Marker.OBJECT_KEY_START, p + 1));
		markers.push(new Marker(Marker.STRING, p + 1));

		return p + 1;
	}

	private int processObjectKeyEnd(char[] c, int i, LinkedList<Marker> markers) {

		int p = i;
		while (p < c.length && (c[p] != ':')) {
			p++;
		}
		if (p == c.length)
			throw new JSONParseException("Unable to find the key value seperated ':'");
		markers.pop();
		markers.push(new Marker(Marker.OBJECT_VALUE_START, p + 1));

		return p + 1;
	}

	private int processObjectValueEnd(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		int p = i;
		while (p < c.length && c[p] != ',' && c[p] != '}') {
			p++;
		}
		if (p == c.length)
			throw new JSONParseException("Unable to find Object ending");
		markers.pop();
		if (c[p] == ',')
			markers.push(new Marker(Marker.OBJECT_KEY_START, p + 1));
		else {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataObject m = values.pop();
			DataObject k;
			while (m.getType() != DataObject.BREAK) {

				k = values.pop();
				bos.write(k.bson.bs, k.bson.offset, k.bson.length);
				bos.write(m.bson.bs, m.bson.offset, m.bson.length);
				m = values.pop();
			}

			byte[] size = BSONParser.getBytes(bos.size());
			byte[] b = new byte[bos.size() + 5];
			b[0] = BSON.OBJECT;
			System.arraycopy(size, 1, b, 1, 4);
			System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());

			markers.pop();
			values.push(new DataObject(DataObject.VALUE, new BSON(b)));
		}

		return p + 1;
	}

	private int processString(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		int p = i;

		while (p < c.length && (c[p] != '"' && c[p - 1] != '\\')) {

			if (c[p] == '\n') {
				throw new JSONParseException("String cannot have line breaks at : " + i);
			}
			p++;
		}

		char[] cArray = new char[p - i];
		System.arraycopy(c, i, cArray, 0, p - i);
		String s = new String(StringUtil.trimInvisible(cArray));

		markers.pop();

		if (markers.peek().getType() == Marker.ARRAY) {

			values.push(new DataObject(DataObject.VALUE, bsonParser.parseObject(s)));
			if (p + 1 < c.length && c[p + 1] == ',')
				return p + 2;
		} else {
			boolean isKeyOrValue = markers.peek().getType() == Marker.OBJECT_KEY_START
					|| markers.peek().getType() == Marker.OBJECT_VALUE_START;

			boolean isKey = markers.peek().getType() == Marker.OBJECT_KEY_START;

			if (isKeyOrValue)
				markers.pop();
			if (isKey) {
				values.push(new DataObject(DataObject.KEY, this.getKeySubstitutor().getBSONFromKey(s)));
				markers.push(new Marker(Marker.OBJECT_KEY_END, p + 1));
			} else {
				values.push(new DataObject(DataObject.VALUE, bsonParser.parseObject(s)));
			}
		}
		return p + 1;
	}

	private int processNull(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		int v = c.length - i;
		if (v < 4)
			throw new JSONParseException("Expected a null value");

		if (c[i + 1] == 'u' && c[i + 2] == 'l' && c[i + 3] == 'l') {
			markers.pop();
			boolean vs = markers.peek().getType() == Marker.OBJECT_VALUE_START;
			if (vs)
				markers.pop();
			values.push(new DataObject(DataObject.VALUE, bsonParser.parseObject(null)));
			if (vs)
				markers.push(new Marker(Marker.OBJECT_VALUE_END, i + 4));

			return i + 4;
		}

		throw new JSONParseException("Expected a null value");
	}

	private int processNumber(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		int p = i;
		boolean isReal = false;
		if (c[p] == '-' || c[p] == '+')
			++p;
		while (p < c.length && c[p] != ',' && c[p] != ']' && c[p] != '}') {

			if (!isReal && (c[p] == 'e' || c[p] == 'E' || c[p] == '.'))
				isReal = true;
			else if (c[p] < '0' || c[p] > '9')
				throw new JSONParseException("Unknown symbol while parsing number : '" + c[p] + "' at position : " + p);
			p++;
		}

		char[] cArray = new char[p - i];
		System.arraycopy(c, i, cArray, 0, p - i);
		String s = new String(StringUtil.trimInvisible(cArray));

		markers.pop();
		boolean vs = markers.peek().getType() == Marker.OBJECT_VALUE_START;
		if (vs)
			markers.pop();
		values.push(new DataObject(DataObject.VALUE, (isReal ? getRealNumberValue(s) : getNumberValue(s))));
		int index = p;
		if (index + 1 < c.length && (c[index] == '}' || markers.peek().getType() == Marker.ARRAY))
			++index;
		if (vs)
			markers.push(new Marker(Marker.OBJECT_VALUE_END, index));

		return index;
	}

	private int processBoolean(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		int v = c.length - i;
		if (v < 4 || (c[i] == 'f' && v < 5))
			throw new JSONParseException("Expected a boolean value " + (c[i] == 't' ? "true" : "false"));

		Boolean value;
		if (c[i] == 't') {
			if (c[i + 1] == 'r' && c[i + 2] == 'u' && c[i + 3] == 'e') {

				value = Boolean.TRUE;
			} else {
				throw new JSONParseException("Expected a boolean value true");
			}
		} else {
			if (c[i + 1] == 'a' && c[i + 2] == 'l' && c[i + 3] == 's' && c[i + 4] == 'e') {

				value = Boolean.FALSE;
			} else {
				throw new JSONParseException("Expected a boolean value true");
			}
		}

		markers.pop();
		boolean vs = markers.peek().getType() == Marker.OBJECT_VALUE_START;
		if (vs)
			markers.pop();
		values.push(new DataObject(DataObject.VALUE, bsonParser.parseObject(value)));
		int index = i + (value.booleanValue() ? 4 : 5);
		if (vs)
			markers.push(new Marker(Marker.OBJECT_VALUE_END, index));
		return index;
	}

	private int processBegin(char[] c, int i, LinkedList<Marker> markers, LinkedList<DataObject> values) {

		if (c[i] <= ' ') {
			return i + 1;
		}

		if ((c[i] >= '0' && c[i] <= '9') || c[i] == '-' || c[i] == '+') {
			markers.push(new Marker(Marker.NUMBER, i));
			return i;
		}

		switch (c[i]) {
		case 't':
		case 'f':
			markers.push(new Marker(Marker.BOOLEAN, i));
			return i;
		case 'n':
			markers.push(new Marker(Marker.NULL, i));
			return i;
		case '"':
			markers.push(new Marker(Marker.STRING, i + 1));
			return i + 1;
		case '{':
			values.push(new DataObject(DataObject.BREAK));
			markers.push(new Marker(Marker.OBJECT, i + 1));
			return i + 1;
		case '[':
			values.push(new DataObject(DataObject.BREAK));
			markers.push(new Marker(Marker.ARRAY, i + 1));
			return i + 1;
		case ']':
			markers.push(new Marker(Marker.ARRAY_END, i));
			return i;
		case ',':
			throw new JSONParseException(errorString(c[i], i, "Value cannot start with"));
		default:
			throw new JSONParseException(errorString(c[i], i, null));
		}
	}

	private String errorString(char c, int i, String message) {
		return (message == null ? "Unable to parse near" : message) + " : '" + c + "' at position : " + (i + 1);
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

	@AllArgsConstructor
	@Data
	protected static class DataObject {

		public static final byte BREAK = 0;
		public static final byte VALUE = 1;
		public static final byte KEY = 2;

		private byte type;
		private BSON bson;

		public DataObject(byte type) {
			this(type, null);
		}
	}

	@AllArgsConstructor
	@Data
	protected static class Marker {

		public static final byte BEGIN = 0;
		public static final byte NUMBER = 1;
		public static final byte STRING = 2;
		public static final byte BOOLEAN = 3;
		public static final byte NULL = 4;

		public static final byte OBJECT = 5;
		public static final byte OBJECT_KEY_START = 6;
		public static final byte OBJECT_KEY_END = 7;
		public static final byte OBJECT_VALUE_START = 8;
		public static final byte OBJECT_VALUE_END = 9;

		public static final byte ARRAY = 10;
//		public static final byte ARRAY_VALUE_START = 11;
		public static final byte ARRAY_END = 12;

		private byte type;
		private int start;
	}

}

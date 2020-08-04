package io.sprigdb.commons.bson;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.sprigdb.commons.exceptions.JSONParseException;
import io.sprigdb.commons.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class JSONParser {

	private static final String AT_POSITION = "' at position : ";

	private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
	private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

	private static final byte MARKER_NONE = -1;
	private static final byte MARKER_OBJECT = 1;
	private static final byte MARKER_ARRAY = 2;

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

		LinkedList<Byte> containerMarkers = new LinkedList<>();
		LinkedList<DataObject> values = new LinkedList<>();

		int i = off;
		byte currentContainer = MARKER_NONE;
		int totalLength = off + length;

		while (i < totalLength) {

			switch (c[i]) {

			case 't':
			case 'f':
				i = processBoolean(c, i, totalLength, currentContainer, values);
				break;

			case 'n':
				i = processNull(c, i, totalLength, currentContainer, values);
				break;

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '-':
			case '+':
				i = processNumber(c, i, totalLength, currentContainer, values);
				break;

			case '"':
				i = processString(c, i, totalLength, currentContainer, values);
				break;
			case '{':
				currentContainer = MARKER_OBJECT;
				containerMarkers.push(currentContainer);
				values.push(new DataObject());
				i = processObjectStart(c, i, totalLength);
				break;
			case '}':
				if (currentContainer != MARKER_OBJECT) {
					throw new JSONParseException("'}' found without object start '{'");
				}
				containerMarkers.pop();
				if (!containerMarkers.isEmpty())
					currentContainer = containerMarkers.peek();
				else
					currentContainer = MARKER_NONE;
				i = processObjectEnd(c, i, totalLength, currentContainer, values);
				break;
			case '[':
				currentContainer = MARKER_ARRAY;
				containerMarkers.push(currentContainer);
				values.push(new DataObject());
				i = checkForValue(c, i, totalLength);
				break;
			case ']':
				if (currentContainer != MARKER_ARRAY) {
					throw new JSONParseException("']' found without array start '['");
				}
				containerMarkers.pop();
				if (!containerMarkers.isEmpty())
					currentContainer = containerMarkers.peek();
				else
					currentContainer = MARKER_NONE;
				i = processArrayEnd(c, i, totalLength, currentContainer, values);
				break;
			default:
				if ((c[i] == '\t' || c[i] == '\n' || c[i] == ' ') || c[i] == 65279)
					i++;
				else
					throw new JSONParseException("Unknown symbol '" + c[i] + AT_POSITION + (i + 1));
			}
		}

		if (!containerMarkers.isEmpty()) {
			throw new JSONParseException(
					(containerMarkers.peek() == MARKER_ARRAY ? "Array" : "Object") + " is not closed in the end");
		}
		return values.pop().getBson();
	}

	private int processObjectStart(char[] c, int i, int totalLength) {

		int p = i + 1;
		while (p < totalLength && (c[p] == '\t' || c[p] == '\n' || c[p] == ' ') && c[p] != '"' && c[p] != '}') {
			++p;
		}

		if (p == totalLength || !(c[p] == '"' || c[p] == '}'))
			throw new JSONParseException("Expected a String at position : " + (p + 1));
		return i + 1;
	}

	private int processArrayEnd(char[] c, int i, int totalLength, byte previousContainer,
			LinkedList<DataObject> values) {

		LinkedList<BSON> list = new LinkedList<>();
		while (values.peek().getType() != DataObject.BREAK) {
			list.addFirst(values.pop().getBson());
		}
		values.pop();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (BSON v : list) {
			bos.write(v.bs, v.offset, v.length);
		}

		byte[] size = BSONParser.getBytes(bos.size());
		byte[] b = new byte[bos.size() + 5];
		b[0] = BSON.ARRAY;
		System.arraycopy(size, 1, b, 1, 4);
		System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());

		push(values, new DataObject(DataObject.VALUE, new BSON(b)));

		return checkForValue(c, i, totalLength, previousContainer);
	}

	private int processObjectEnd(char[] c, int i, int totalLength, byte previousContainer,
			LinkedList<DataObject> values) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataObject m = values.pop();
		DataObject k;
		Set<BSON> set = new HashSet<>();
		while (m.getType() != DataObject.BREAK) {

			k = values.pop();
			if (set.contains(k.bson)) {
				if (!values.isEmpty())
					m = values.pop();
				continue;
			}
			bos.write(k.bson.bs, k.bson.offset, k.bson.length);
			bos.write(m.bson.bs, m.bson.offset, m.bson.length);
			m = values.pop();
			set.add(k.bson);
		}

		byte[] size = BSONParser.getBytes(bos.size());
		byte[] b = new byte[bos.size() + 5];
		b[0] = BSON.OBJECT;
		System.arraycopy(size, 1, b, 1, 4);
		System.arraycopy(bos.toByteArray(), 0, b, 5, bos.size());

		push(values, new DataObject(DataObject.VALUE, new BSON(b)));

		return checkForValue(c, i, totalLength, previousContainer);
	}

	private int processString(char[] c, int i, int totalLength, byte currentContainer, LinkedList<DataObject> values) {

		int p = i + 1;

		while (p < totalLength) {

			if (c[p] == '"' && c[p - 1] != '\\')
				break;
			if (c[p] == '\n') {
				throw new JSONParseException("String cannot have line breaks at : " + (i + 1));
			}
			++p;
		}

		if (p == totalLength && (currentContainer != MARKER_NONE || p == (i + 1))) {
			throw new JSONParseException("String not terminated with '\"' at position : " + p);
		}

		char[] cArray = new char[p - (i + 1)];
		System.arraycopy(c, i + 1, cArray, 0, p - (i + 1));
		String s = new String(cArray);

		if (currentContainer == MARKER_NONE) {
			push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(s)));
			return p + 1;
		} else if (currentContainer == MARKER_OBJECT) {
			DataObject dob = values.peek();
			boolean isKey = dob.type == DataObject.BREAK || ((dob.getIndex() & 1) == 1);
			if (isKey)
				push(values, new DataObject(DataObject.KEY, this.keysub.getBSONFromKey(s)));
			else
				push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(s)));

			++p;
			if (isKey) {

				while (p < totalLength && c[p] != ':' && (c[p] == '\t' || c[p] == '\n' || c[p] == ' ')) {
					++p;
				}

				if (p < totalLength && c[p] == ':')
					return p + 1;

				throw new JSONParseException("Expected ':' at position : " + (p + 1));
			} else {

				while (p < totalLength && c[p] != ',' && c[p] != '}' && (c[p] == '\t' || c[p] == '\n' || c[p] == ' ')) {
					++p;
				}

				if (p < totalLength && c[p] == '}')
					return p;

				if (p < totalLength && c[p] == ',') {

					int q = p + 1;
					while (q < totalLength && c[q] != '"' && (c[q] == '\t' || c[q] == '\n' || c[q] == ' ')) {
						q++;
					}

					if (q < totalLength && c[q] == '"')
						return q;

					throw new JSONParseException("Expecting a string at position : " + (p + 1));
				} else {
					throw new JSONParseException("Expecting a ',' or '}' at position : " + (p + 1));
				}
			}
		} else {

			push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(s)));
			++p;
			while (p < totalLength && c[p] != ',' && c[p] != ']' && (c[p] == '\t' || c[p] == '\n' || c[p] == ' ')) {
				++p;
			}

			if (p == totalLength)
				throw new JSONParseException("Array not terminated");

			if (c[p] == ']')
				return p;

			if (c[p] == ',') {
				return checkForValue(c, p, totalLength);
			}
		}

		throw new JSONParseException("Unknown symbol found after position : 1");
	}

	private int processNumber(char[] c, int i, int totalLength, byte currentContainer, LinkedList<DataObject> values) {

		int p = i;
		boolean isReal = false;
		if (c[p] == '-' || c[p] == '+')
			++p;
		while (p < totalLength && c[p] != ',' && c[p] > ' ') {

			if (c[p] == 'e' || c[p] == 'E') {
				isReal = true;
				if (c[p - 1] == '.') {
					throw new JSONParseException("Exponent marker cannot be followed by '.' at position : " + (p + 1));
				}
			} else if (c[p] == '.') {
				isReal = true;
			} else if ((c[p] == ']' && currentContainer == MARKER_ARRAY)
					|| (c[p] == '}' && currentContainer == MARKER_OBJECT)) {
				break;
			} else if ((c[p] < '0' || c[p] > '9') && c[p] != '-' && c[p] != '+') {
				throw new JSONParseException("Unknown symbol while parsing number : '" + c[p] + AT_POSITION + (p + 1));
			}
			++p;
		}

		if (isReal && p < totalLength && c[p - 1] == '.') {
			throw new JSONParseException("Number cannot be terminated with '.' at position : " + (p + 1));
		}

		char[] cArray = new char[p - i];
		System.arraycopy(c, i, cArray, 0, p - i);
		String s = new String(StringUtil.trimInvisible(cArray));
		push(values, new DataObject(DataObject.VALUE, (isReal ? getRealNumberValue(s) : getNumberValue(s))));

		return nextToken(c, p, totalLength, currentContainer);
	}

	private int processNull(char[] c, int i, int totalLength, byte currentContainer, LinkedList<DataObject> values) {

		if (i + 4 > totalLength)
			throw new JSONParseException("Expected value 'null' at position : " + (i + 1));

		if (c[i + 1] == 'u' && c[i + 2] == 'l' && c[i + 3] == 'l') {
			push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(null)));
		} else {
			throw new JSONParseException("Expected value 'null' at position : " + (i + 1));
		}

		return nextToken(c, i + 4, totalLength, currentContainer);
	}

	private int processBoolean(char[] c, int i, int totalLength, byte currentContainer, LinkedList<DataObject> values) {

		int p = i + 4;
		if (c[i] == 't') {

			if (i + 4 > totalLength)
				throw new JSONParseException("Expected value 'true' at position : " + (i + 1));
			if (c[i + 1] == 'r' && c[i + 2] == 'u' && c[i + 3] == 'e') {
				push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(Boolean.TRUE)));
			} else {
				throw new JSONParseException("Expected value 'true' at position : " + (i + 1));
			}
		} else {
			if (i + 5 > totalLength)
				throw new JSONParseException("Expected value 'false' at position : " + (i + 1));
			if (c[i + 1] == 'a' && c[i + 2] == 'l' && c[i + 3] == 's' && c[i + 4] == 'e') {
				push(values, new DataObject(DataObject.VALUE, this.bsonParser.parseObject(Boolean.FALSE)));
			} else {
				throw new JSONParseException("Expected value 'false' at position : " + (i + 1));
			}
			++p;
		}

		return nextToken(c, p, totalLength, currentContainer);
	}

	private void push(LinkedList<DataObject> values, DataObject dataObject) {

		if (values.isEmpty()) {
			dataObject.setIndex(0);
		} else {
			DataObject lastDataObject = values.peek();
			if (lastDataObject.type == DataObject.BREAK)
				dataObject.setIndex(0);
			else
				dataObject.setIndex(lastDataObject.getIndex() + 1);
		}

		values.push(dataObject);
	}

	private int nextToken(char[] c, int i, int totalLength, byte currentContainer) {

		if (currentContainer == MARKER_NONE)
			return i;

		int p = i;

		while (p < totalLength && c[p] != ',' && c[p] != '}' && c[p] != ']'
				&& (c[p] == '\t' || c[p] == '\n' || c[p] == ' ')) {
			++p;
		}

		if (p == totalLength)
			throw new JSONParseException(
					"Expected closing '" + (currentContainer == MARKER_ARRAY ? ']' : '}') + AT_POSITION + (i + 1));

		if (c[p] == '}' || c[p] == ']')
			return p;

		if (c[p] == ',') {

			return checkForValue(c, p, totalLength);
		}

		throw new JSONParseException(
				"Expecting a ',' or '" + (currentContainer == MARKER_ARRAY ? ']' : '}') + AT_POSITION + (i + 1));
	}

	private int checkForValue(char[] c, int i, int totalLength, byte previousContainer) {

		int p = i + 1;
		while (p < totalLength && (c[p] == '\t' || c[p] == '\n' || c[p] == ' ')) {
			++p;
		}

		if (previousContainer == -1)
			return checkForValuePrevNothing(c, totalLength, p);
		else
			return checkForValuePrevObjectOrArray(c, totalLength, previousContainer, p);
	}

	private int checkForValuePrevObjectOrArray(char[] c, int totalLength, byte previousContainer, int p) {

		if (p == totalLength)
			throw new JSONParseException(
					(previousContainer == MARKER_ARRAY ? "Array" : "Object") + " is not terminated.");

		if (c[p] == ',')
			return p + 1;

		if (c[p] != ']' && previousContainer == MARKER_ARRAY)
			throw new JSONParseException("Expecting ']' but found : '" + c[p] + AT_POSITION + (p + 1));

		if (c[p] != '}' && previousContainer == MARKER_OBJECT)
			throw new JSONParseException("Expecting '}' but found : '" + c[p] + AT_POSITION + (p + 1));

		return p;
	}

	private int checkForValuePrevNothing(char[] c, int totalLength, int p) {
		if (p == totalLength)
			return p;
		else
			throw new JSONParseException("Unknown symbol '" + c[p] + AT_POSITION + (p + 1));
	}

	private int checkForValue(char[] c, int p, int totalLength) {

		boolean startsWithComma = c[p] == ',';
		int q = p + 1;
		boolean found = false;
		while (q < totalLength) {

			if ((c[q] == '\t' || c[q] == '\n' || c[q] == ' ')) {
				q++;
			} else {

				if (startsWithComma && c[q] == ']') {
					throw new JSONParseException("No value found at position : " + (q + 1));
				}

				if (c[q] == 't' || c[q] == 'f' || c[q] == 'n' || c[q] == '{' || c[q] == '[' || c[q] == '\"'
						|| c[q] == '+' || c[q] == '-' || c[q] == ']' || (c[q] >= '0' && c[q] <= '9')) {
					found = true;
				}
				break;
			}
		}

		if (found)
			return p + 1;

		throw new JSONParseException("Unknown/No value found at position : " + (q + 1));
	}

	private BSON getRealNumberValue(String s) {
		try {

			float f = Float.parseFloat(s);
			if (f != 0.0f && f != Float.POSITIVE_INFINITY && f != Float.NEGATIVE_INFINITY)
				return this.bsonParser.parseObject(f);

			Double d = Double.parseDouble(s);
			if (d != 0.0d && d != Double.POSITIVE_INFINITY && d != Double.NEGATIVE_INFINITY)
				return this.bsonParser.parseObject(d);

			BigDecimal bd = new BigDecimal(s);
			if (bd.equals(BigDecimal.valueOf(0.0d)))
				return this.bsonParser.parseObject(0.0f);

			return this.bsonParser.parseObject(bd);
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
		private int index = -1;

		public DataObject() {
			this(BREAK, null, -1);
		}

		public DataObject(byte type, BSON bson) {
			this(type, bson, -1);
		}

		public String toString() {
			if (type == BREAK)
				return "---------";

			StringBuilder sb = new StringBuilder(type == KEY ? "KKKKKK" : "VVVVVV");
			sb.append("(").append(index).append(")\n").append(bson.toString());

			return sb.toString();
		}
	}
}

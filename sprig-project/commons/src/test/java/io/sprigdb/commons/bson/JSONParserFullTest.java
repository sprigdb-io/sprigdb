package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;

// seriot.ch Parsing JSON is a Minefield http://www.seriot.ch/parsing_json.php
// http://seriot.ch/json/parsing.html#28
class JSONParserFullTest {

	//@formatter:off
	private static final Map<String, Definition> TEST_PARSING = makeMap(
			"test_parsing/i_number_double_huge_neg_exp.json", new Definition(List.of(new BigDecimal("123.456E-789"))),
			"test_parsing/i_number_huge_exp.json",new Definition("Unable to parse to a number : '0.4e00669999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999969999999006'"),
			"test_parsing/i_number_neg_int_huge_exp.json", new Definition(List.of(new BigDecimal("-1e+9999"))),
			"test_parsing/i_number_pos_double_huge_exp.json", new Definition(List.of(new BigDecimal("1.5e+9999"))),
			"test_parsing/i_number_real_neg_overflow.json", new Definition(List.of(new BigDecimal("-123123e100000"))),
			"test_parsing/i_number_real_pos_overflow.json", new Definition(List.of(new BigDecimal("123123e100000"))),
			"test_parsing/i_number_real_underflow.json", new Definition(List.of(new BigDecimal("123e-10000000"))),
			"test_parsing/i_number_too_big_neg_int.json", new Definition(List.of(new BigInteger("-123123123123123123123123123123"))),
			"test_parsing/i_number_too_big_pos_int.json", new Definition(List.of(new BigInteger("100000000000000000000"))),
			"test_parsing/i_number_very_big_negative_int.json", new Definition(List.of(new BigInteger("-237462374673276894279832749832423479823246327846"))),
			"test_parsing/i_object_key_lone_2nd_surrogate.json", new Definition(Map.of("\\uDFAA", Byte.valueOf((byte)0))),
			"test_parsing/i_string_1st_surrogate_but_2nd_missing.json", new Definition(List.of("\\uDADA")),
			"test_parsing/i_string_1st_valid_surrogate_2nd_invalid.json", new Definition(List.of("\\uD888\\u1234")),
			"test_parsing/i_string_incomplete_surrogate_and_escape_valid.json", new Definition(List.of("\\uD800\\n")),
			"test_parsing/i_string_incomplete_surrogate_pair.json", new Definition(List.of("\\uDd1ea")),
			"test_parsing/i_string_incomplete_surrogates_escape_valid.json", new Definition(List.of("\\uD800\\uD800\\n")),
			"test_parsing/i_string_invalid_lonely_surrogate.json", new Definition(List.of("\\ud800")),
			"test_parsing/i_string_invalid_surrogate.json", new Definition(List.of("\\ud800abc")),
			"test_parsing/i_string_invalid_utf-8.json", new Definition(List.of("�")),
			"test_parsing/i_string_inverted_surrogates_U+1D11E.json", new Definition(List.of("\\uDd1e\\uD834")),
			"test_parsing/i_string_iso_latin_1.json", new Definition(List.of("�")),
			"test_parsing/i_string_lone_second_surrogate.json", new Definition(List.of("\\uDFAA")),
			"test_parsing/i_string_lone_utf8_continuation_byte.json", new Definition(List.of("�")),
			"test_parsing/i_string_not_in_unicode_range.json", new Definition(List.of("����")),
			"test_parsing/i_string_overlong_sequence_2_bytes.json", new Definition(List.of("��")),
			"test_parsing/i_string_overlong_sequence_6_bytes_null.json", new Definition(List.of("������")),
			"test_parsing/i_string_overlong_sequence_6_bytes.json", new Definition(List.of("������")),
			"test_parsing/i_string_truncated-utf-8.json", new Definition(List.of("��")),
//			"test_parsing/i_string_utf16BE_no_BOM.json", new Definition(List.of( " � " )),
//			"test_parsing/i_string_utf16LE_no_BOM.json", new Definition(List.of("  �  "))
//			"test_parsing/i_string_UTF-16LE_with_BOM.json", new Definition("asdf")
			"test_parsing/i_string_UTF-8_invalid_sequence.json", new Definition(List.of("日ш�")),
			"test_parsing/i_string_UTF8_surrogate_U+D800.json", new Definition(List.of("�")),
			"test_parsing/i_structure_500_nested_arrays.json", new Definition(nestedEmptyList(500)),
			"test_parsing/i_structure_UTF-8_BOM_empty_object.json", new Definition(Map.of()),
			"test_parsing/n_array_1_true_without_comma.json", new Definition("Unable to parse near : 't' at position : 4"),
			"test_parsing/n_array_a_invalid_utf8.json", new Definition("Unable to parse near : 'a' at position : 2"),
			"test_parsing/n_array_colon_instead_of_comma.json", new Definition("Unable to parse near : ':' at position : 4"),
			"test_parsing/n_array_comma_after_close.json", new Definition("Unable to parse near : ',' at position : 5"),
			//Following case is not working.
			
			"test_parsing/n_array_comma_and_number.json", new Definition("Unable to parse near : ',' at position : 2"),
			"test_parsing/n_array_double_comma.json", new Definition("Unable to parse near : ',' at position : 4"),
			"test_parsing/n_array_double_extra_comma.json", new Definition("Unable to parse near : ',' at position : 5"),
			"test_parsing/n_array_extra_close.json", new Definition("Unable to parse near : ']' at position : 6"),
			"test_parsing/n_array_extra_comma.json", new Definition("Unable to parse near : ',' at position : 4"),
			"test_parsing/n_array_incomplete_invalid_value.json", new Definition("Unable to parse near : 'x' at position : 2"),
			"test_parsing/n_array_incomplete.json", new Definition("Illformed JSON"),
			"test_parsing/n_array_inner_array_no_comma.json", new Definition("Unable to parse near : '[' at position : 3"),
			"test_parsing/n_array_invalid_utf8.json", new Definition("Unable to parse near : '�' at position : 2"),
			"test_parsing/n_array_items_separated_by_semicolon.json", new Definition("Unable to parse near : ':' at position : 3"),
			"test_parsing/n_array_just_comma.json", new Definition("Unable to parse near : ',' at position : 2"),
			"test_parsing/n_array_just_minus.json", new Definition("Unable to parse to a number : '-'"),
			"test_parsing/n_array_missing_value.json", new Definition("Unable to parse near : ',' at position : 5"),
			"test_parsing/n_array_newlines_unclosed.json", new Definition("")
			);
	//@formatter:on
	BSONParser bsonParser = new BSONParser();
	JSONParser jsonParser = new JSONParser();

	@Test
	void testFiles() {

		TEST_PARSING.entrySet().stream().forEach(e -> testEachCase(e.getKey(), e.getValue()));
	}

	void testEachCase(String key, Definition def) {

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(key)))) {
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null)
				sb.append(line);
			String jsonText = sb.toString();
			try {
				BSON b = jsonParser.parseJSONString(jsonText);
//				System.out.println(b.toString());
				if (b.getType() == BSON.ARRAY) {
					assertEquals(def.getValue(), (Object) b.getAsList(jsonParser.getKeySubstitutor()));
				} else if (b.getType() == BSON.OBJECT) {
					assertTrue(mapEqual(def.getValue(), (Object) b.getAsMap(jsonParser.getKeySubstitutor())));
				} else {
					assertEquals(def.getValue(), b.getValue());
				}
			} catch (Exception ex) {
				if (def.isException()) {
					assertEquals(def.getValue(), ex.getMessage());
				} else {
					System.err.println(key);
					System.err.println(jsonText);
					System.err.println("--------------------\n");
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static List<Object> nestedEmptyList(int n) {

		List<Object> list = new ArrayList<>();

		for (int i = 1; i < n; i++) {
			List<Object> l = new ArrayList<>();
			l.add(list);
			list = l;
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	private boolean mapEqual(Object src, Object dest) {

		Map<String, Object> a = (Map<String, Object>) src;
		Map<String, Object> b = (Map<String, Object>) dest;

		assertEquals(a.keySet(), b.keySet());

		for (String key : a.keySet()) {

			if (a.get(key) instanceof Map && !mapEqual(a.get(key), b.get(key)))
				return false;
			assertEquals(a.get(key), b.get(key));
		}

		return true;
	}

	private static Map<String, Definition> makeMap(Object... objects) {

		Map<String, Definition> map = new LinkedHashMap<>();

		for (int i = 0; i < objects.length; i += 2) {
			map.put((String) objects[i], (Definition) objects[i + 1]);
		}

		return map;
	}

	@AllArgsConstructor
	@Data
	private static class Definition {

		boolean exception = false;
		Object value;

		public Definition(Object value) {
			this(false, value);
		}

		public Definition(String message) {
			this(true, message);
		}
	}
}

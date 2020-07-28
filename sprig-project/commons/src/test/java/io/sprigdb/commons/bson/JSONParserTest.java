package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.sprigdb.commons.bson.JSONParser.Marker;
import io.sprigdb.commons.exceptions.JSONParseException;

class JSONParserTest {

	BSONParser bsonParser = new BSONParser();
	JSONParser jsonParser = new JSONParser();

	@Test
	void testParseJSONString() {

		assertTrue(jsonParser.parseJSONString(null).isNull());
		assertTrue(jsonParser.parseJSONString("").isNull());
		assertTrue(jsonParser.parseJSONString("null").isNull());

		assertEquals((byte) 2, (byte) jsonParser.parseJSONString("2").getValue());
		assertEquals(2.5f, (float) jsonParser.parseJSONString("2.5").getValue());
		assertEquals(123123123132123123l, (long) jsonParser.parseJSONString("123123123132123123").getValue());
		assertEquals(1234567890123456789012345678901234567890.5d,
				(double) jsonParser.parseJSONString("1234567890123456789012345678901234567890.5").getValue());

		BSON b = jsonParser.parseJSONString("[2,3,4,5]");
		List<BSON> list = b.getValue();
		assertEquals((byte) 2, (byte) list.get(0).getValue());
		assertEquals((byte) 3, (byte) list.get(1).getValue());
		assertEquals((byte) 4, (byte) list.get(2).getValue());
		assertEquals((byte) 5, (byte) list.get(3).getValue());

		b = jsonParser.parseJSONString("{\"k1\" : -12, \"k2\": +34.4}");
		Map<BSON, BSON> map = b.getAsBSONMap();
		assertEquals((byte) -12, (byte) map.get(bsonParser.parseObject("k1")).getValue());
		assertEquals(34.4f, (float) map.get(bsonParser.parseObject("k2")).getValue());

		assertThrows(JSONParseException.class, () -> jsonParser.parseJSONString("[a]"));
		
		jsonParser.parseJSONString("[\"asdf\",4,5]");
		b = jsonParser.parseJSONString("[\"asdf\",\n4\n,\n5]");
	}

	@Test
	void testGetValue() {

		assertEquals((short) 232,
				(short) jsonParser.getValue(new Marker(2, Marker.NUMBER), 5, ": 232,".toCharArray()).getValue());
		assertEquals(2323324234234l, (long) jsonParser
				.getValue(new Marker(5, Marker.NUMBER), 19, ":    2323324234234\n,".toCharArray()).getValue());
		assertTrue((boolean) jsonParser.getValue(new Marker(0, Marker.BOOLEAN), 5, "true\n}".toCharArray()).getValue());
		assertFalse((boolean) jsonParser.getValue(new Marker(0, Marker.BOOLEAN), 5, "false".toCharArray()).getValue());
		assertTrue(jsonParser.getValue(new Marker(0, Marker.NULL), 4, "null".toCharArray()).isNull());
		assertEquals(22.4f,
				(float) jsonParser.getValue(new Marker(2, Marker.REAL_NUMBER), 6, "  22.4;".toCharArray()).getValue());
		assertEquals(122.4f,
				(float) jsonParser.getValue(new Marker(1, Marker.REAL_NUMBER), 6, " 122.4;".toCharArray()).getValue());
		assertEquals(1234567890123456789012345678901234567890.4d,
				(double) jsonParser.getValue(new Marker(0, Marker.REAL_NUMBER), 42,
						"1234567890123456789012345678901234567890.4;".toCharArray()).getValue());

		final Marker marker1 = new Marker(0, Marker.BOOLEAN);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker1, 5, new char[] { 't', 'r', 'p', 'e', '\n', '}' }));

		final Marker marker2 = new Marker(0, Marker.NULL);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker2, 6, new char[] { 'n', 'i', 'n', 'u', '\n', '"' }));

		final Marker marker3 = new Marker(0, Marker.NUMBER);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker3, 4, new char[] { '0', '4', '5', 'n' }));

		final Marker marker4 = new Marker(0, Marker.REAL_NUMBER);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker4, 4, new char[] { '0', '4', '.', 'n' }));

		final Marker marker5 = new Marker(0, Marker.STRING);
		assertThrows(JSONParseException.class,
				() -> jsonParser.getValue(marker5, 3, new char[] { 'a', 'b', 'c', 'd' }));
	}

	@Test
	void testMakeBSONMapOfBSONBSON() {

		BSON k1 = bsonParser.parseObject("k1");
		BSON v1 = bsonParser.parseObject(12);
		BSON k2 = bsonParser.parseObject("k2");
		BSON v2 = bsonParser.parseObject("Kiran");

		Map<BSON, BSON> m = Map.of(k1, v1, k2, v2);
		BSON map = jsonParser.makeBSON(m);
		Map<BSON, BSON> m1 = map.getAsBSONMap();
		assertEquals(12, (int) m1.get(k1).getValue());
		assertEquals("Kiran", (String) m1.get(k2).getValue());
	}

	@Test
	void testMakeBSONListOfBSON() {

		BSON a = bsonParser.parseObject(12);
		BSON b = bsonParser.parseObject("kiran");
		BSON c = bsonParser.parseObject(23.4f);

		BSON bson = jsonParser.makeBSON(List.of(a, b, c));
		List<BSON> list = bson.getValue();
		assertEquals(12, (int) list.get(0).getValue());
		assertEquals("kiran", (String) list.get(1).getValue());
		assertEquals(23.4f, (float) list.get(2).getValue());
	}

//	private static final List<String> TEST_TRANSFORM = Arrays.asList("test_transform/number_-9223372036854775808.json",
//			"test_transform/number_-9223372036854775809.json", "test_transform/number_1.0.json",
//			"test_transform/number_1.000000000000000005.json", "test_transform/number_1000000000000000.json",
//			"test_transform/number_10000000000000000999.json", "test_transform/number_1e-999.json",
//			"test_transform/number_1e6.json", "test_transform/number_9223372036854775807.json",
//			"test_transform/number_9223372036854775808.json", "test_transform/object_key_nfc_nfd.json",
//			"test_transform/object_key_nfd_nfc.json", "test_transform/object_same_key_different_values.json",
//			"test_transform/object_same_key_same_value.json", "test_transform/object_same_key_unclear_values.json",
//			"test_transform/string_1_escaped_invalid_codepoint.json", "test_transform/string_1_invalid_codepoint.json",
//			"test_transform/string_2_escaped_invalid_codepoints.json",
//			"test_transform/string_2_invalid_codepoints.json",
//			"test_transform/string_3_escaped_invalid_codepoints.json",
//			"test_transform/string_3_invalid_codepoints.json", "test_transform/string_with_escaped_NULL.json");
//	private static final List<String> TEST_PARSING = ("test_parsing/i_number_double_huge_neg_exp.json",
//			"test_parsing/i_number_huge_exp.json", "test_parsing/i_number_neg_int_huge_exp.json",
//			"test_parsing/i_number_pos_double_huge_exp.json", "test_parsing/i_number_real_neg_overflow.json",
//			"test_parsing/i_number_real_pos_overflow.json", "test_parsing/i_number_real_underflow.json",
//			"test_parsing/i_number_too_big_neg_int.json", "test_parsing/i_number_too_big_pos_int.json",
//			"test_parsing/i_number_very_big_negative_int.json", "test_parsing/i_object_key_lone_2nd_surrogate.json",
//			"test_parsing/i_string_1st_surrogate_but_2nd_missing.json",
//			"test_parsing/i_string_1st_valid_surrogate_2nd_invalid.json",
//			"test_parsing/i_string_UTF-16LE_with_BOM.json", "test_parsing/i_string_UTF-8_invalid_sequence.json",
//			"test_parsing/i_string_UTF8_surrogate_U+D800.json",
//			"test_parsing/i_string_incomplete_surrogate_and_escape_valid.json",
//			"test_parsing/i_string_incomplete_surrogate_pair.json",
//			"test_parsing/i_string_incomplete_surrogates_escape_valid.json",
//			"test_parsing/i_string_invalid_lonely_surrogate.json", "test_parsing/i_string_invalid_surrogate.json",
//			"test_parsing/i_string_invalid_utf-8.json", "test_parsing/i_string_inverted_surrogates_U+1D11E.json",
//			"test_parsing/i_string_iso_latin_1.json", "test_parsing/i_string_lone_second_surrogate.json",
//			"test_parsing/i_string_lone_utf8_continuation_byte.json", "test_parsing/i_string_not_in_unicode_range.json",
//			"test_parsing/i_string_overlong_sequence_2_bytes.json",
//			"test_parsing/i_string_overlong_sequence_6_bytes.json",
//			"test_parsing/i_string_overlong_sequence_6_bytes_null.json", "test_parsing/i_string_truncated-utf-8.json",
//			"test_parsing/i_string_utf16BE_no_BOM.json", "test_parsing/i_string_utf16LE_no_BOM.json",
//			"test_parsing/i_structure_500_nested_arrays.json", "test_parsing/i_structure_UTF-8_BOM_empty_object.json",
//			"test_parsing/n_array_1_true_without_comma.json", "test_parsing/n_array_a_invalid_utf8.json",
//			"test_parsing/n_array_colon_instead_of_comma.json", "test_parsing/n_array_comma_after_close.json",
//			"test_parsing/n_array_comma_and_number.json", "test_parsing/n_array_double_comma.json",
//			"test_parsing/n_array_double_extra_comma.json", "test_parsing/n_array_extra_close.json",
//			"test_parsing/n_array_extra_comma.json", "test_parsing/n_array_incomplete.json",
//			"test_parsing/n_array_incomplete_invalid_value.json", "test_parsing/n_array_inner_array_no_comma.json",
//			"test_parsing/n_array_invalid_utf8.json", "test_parsing/n_array_items_separated_by_semicolon.json",
//			"test_parsing/n_array_just_comma.json", "test_parsing/n_array_just_minus.json",
//			"test_parsing/n_array_missing_value.json", "test_parsing/n_array_newlines_unclosed.json",
//			"test_parsing/n_array_number_and_comma.json", "test_parsing/n_array_number_and_several_commas.json",
//			"test_parsing/n_array_spaces_vertical_tab_formfeed.json", "test_parsing/n_array_star_inside.json",
//			"test_parsing/n_array_unclosed.json", "test_parsing/n_array_unclosed_trailing_comma.json",
//			"test_parsing/n_array_unclosed_with_new_lines.json",
//			"test_parsing/n_array_unclosed_with_object_inside.json", "test_parsing/n_incomplete_false.json",
//			"test_parsing/n_incomplete_null.json", "test_parsing/n_incomplete_true.json",
//			"test_parsing/n_multidigit_number_then_00.json", "test_parsing/n_number_++.json",
//			"test_parsing/n_number_+1.json", "test_parsing/n_number_+Inf.json", "test_parsing/n_number_-01.json",
//			"test_parsing/n_number_-1.0..json", "test_parsing/n_number_-2..json", "test_parsing/n_number_-NaN.json",
//			"test_parsing/n_number_.-1.json", "test_parsing/n_number_.2e-3.json", "test_parsing/n_number_0.1.2.json",
//			"test_parsing/n_number_0.3e+.json", "test_parsing/n_number_0.3e.json", "test_parsing/n_number_0.e1.json",
//			"test_parsing/n_number_0_capital_E+.json", "test_parsing/n_number_0_capital_E.json",
//			"test_parsing/n_number_0e+.json", "test_parsing/n_number_0e.json", "test_parsing/n_number_1.0e+.json",
//			"test_parsing/n_number_1.0e-.json", "test_parsing/n_number_1.0e.json", "test_parsing/n_number_1_000.json",
//			"test_parsing/n_number_1eE2.json", "test_parsing/n_number_2.e+3.json", "test_parsing/n_number_2.e-3.json",
//			"test_parsing/n_number_2.e3.json", "test_parsing/n_number_9.e+.json", "test_parsing/n_number_Inf.json",
//			"test_parsing/n_number_NaN.json", "test_parsing/n_number_U+FF11_fullwidth_digit_one.json",
//			"test_parsing/n_number_expression.json", "test_parsing/n_number_hex_1_digit.json",
//			"test_parsing/n_number_hex_2_digits.json", "test_parsing/n_number_infinity.json",
//			"test_parsing/n_number_invalid+-.json", "test_parsing/n_number_invalid-negative-real.json",
//			"test_parsing/n_number_invalid-utf-8-in-bigger-int.json",
//			"test_parsing/n_number_invalid-utf-8-in-exponent.json", "test_parsing/n_number_invalid-utf-8-in-int.json",
//			"test_parsing/n_number_minus_infinity.json", "test_parsing/n_number_minus_sign_with_trailing_garbage.json",
//			"test_parsing/n_number_minus_space_1.json", "test_parsing/n_number_neg_int_starting_with_zero.json",
//			"test_parsing/n_number_neg_real_without_int_part.json",
//			"test_parsing/n_number_neg_with_garbage_at_end.json", "test_parsing/n_number_real_garbage_after_e.json",
//			"test_parsing/n_number_real_with_invalid_utf8_after_e.json",
//			"test_parsing/n_number_real_without_fractional_part.json", "test_parsing/n_number_starting_with_dot.json",
//			"test_parsing/n_number_with_alpha.json", "test_parsing/n_number_with_alpha_char.json",
//			"test_parsing/n_number_with_leading_zero.json", "test_parsing/n_object_bad_value.json",
//			"test_parsing/n_object_bracket_key.json", "test_parsing/n_object_comma_instead_of_colon.json",
//			"test_parsing/n_object_double_colon.json", "test_parsing/n_object_emoji.json",
//			"test_parsing/n_object_garbage_at_end.json", "test_parsing/n_object_key_with_single_quotes.json",
//			"test_parsing/n_object_lone_continuation_byte_in_key_and_trailing_comma.json",
//			"test_parsing/n_object_missing_colon.json", "test_parsing/n_object_missing_key.json",
//			"test_parsing/n_object_missing_semicolon.json", "test_parsing/n_object_missing_value.json",
//			"test_parsing/n_object_no-colon.json", "test_parsing/n_object_non_string_key.json",
//			"test_parsing/n_object_non_string_key_but_huge_number_instead.json",
//			"test_parsing/n_object_repeated_null_null.json", "test_parsing/n_object_several_trailing_commas.json",
//			"test_parsing/n_object_single_quote.json", "test_parsing/n_object_trailing_comma.json",
//			"test_parsing/n_object_trailing_comment.json", "test_parsing/n_object_trailing_comment_open.json",
//			"test_parsing/n_object_trailing_comment_slash_open.json",
//			"test_parsing/n_object_trailing_comment_slash_open_incomplete.json",
//			"test_parsing/n_object_two_commas_in_a_row.json", "test_parsing/n_object_unquoted_key.json",
//			"test_parsing/n_object_unterminated-value.json", "test_parsing/n_object_with_single_string.json",
//			"test_parsing/n_object_with_trailing_garbage.json", "test_parsing/n_single_space.json",
//			"test_parsing/n_string_1_surrogate_then_escape.json",
//			"test_parsing/n_string_1_surrogate_then_escape_u.json",
//			"test_parsing/n_string_1_surrogate_then_escape_u1.json",
//			"test_parsing/n_string_1_surrogate_then_escape_u1x.json",
//			"test_parsing/n_string_accentuated_char_no_quotes.json", "test_parsing/n_string_backslash_00.json",
//			"test_parsing/n_string_escape_x.json", "test_parsing/n_string_escaped_backslash_bad.json",
//			"test_parsing/n_string_escaped_ctrl_char_tab.json", "test_parsing/n_string_escaped_emoji.json",
//			"test_parsing/n_string_incomplete_escape.json", "test_parsing/n_string_incomplete_escaped_character.json",
//			"test_parsing/n_string_incomplete_surrogate.json",
//			"test_parsing/n_string_incomplete_surrogate_escape_invalid.json",
//			"test_parsing/n_string_invalid-utf-8-in-escape.json", "test_parsing/n_string_invalid_backslash_esc.json",
//			"test_parsing/n_string_invalid_unicode_escape.json", "test_parsing/n_string_invalid_utf8_after_escape.json",
//			"test_parsing/n_string_leading_uescaped_thinspace.json",
//			"test_parsing/n_string_no_quotes_with_bad_escape.json", "test_parsing/n_string_single_doublequote.json",
//			"test_parsing/n_string_single_quote.json", "test_parsing/n_string_single_string_no_double_quotes.json",
//			"test_parsing/n_string_start_escape_unclosed.json", "test_parsing/n_string_unescaped_crtl_char.json",
//			"test_parsing/n_string_unescaped_newline.json", "test_parsing/n_string_unescaped_tab.json",
//			"test_parsing/n_string_unicode_CapitalU.json", "test_parsing/n_string_with_trailing_garbage.json",
//			"test_parsing/n_structure_100000_opening_arrays.json", "test_parsing/n_structure_U+2060_word_joined.json",
//			"test_parsing/n_structure_UTF8_BOM_no_data.json", "test_parsing/n_structure_angle_bracket_..json",
//			"test_parsing/n_structure_angle_bracket_null.json", "test_parsing/n_structure_array_trailing_garbage.json",
//			"test_parsing/n_structure_array_with_extra_array_close.json",
//			"test_parsing/n_structure_array_with_unclosed_string.json",
//			"test_parsing/n_structure_ascii-unicode-identifier.json", "test_parsing/n_structure_capitalized_True.json",
//			"test_parsing/n_structure_close_unopened_array.json",
//			"test_parsing/n_structure_comma_instead_of_closing_brace.json",
//			"test_parsing/n_structure_double_array.json", "test_parsing/n_structure_end_array.json",
//			"test_parsing/n_structure_incomplete_UTF8_BOM.json", "test_parsing/n_structure_lone-invalid-utf-8.json",
//			"test_parsing/n_structure_lone-open-bracket.json", "test_parsing/n_structure_no_data.json",
//			"test_parsing/n_structure_null-byte-outside-string.json",
//			"test_parsing/n_structure_number_with_trailing_garbage.json",
//			"test_parsing/n_structure_object_followed_by_closing_object.json",
//			"test_parsing/n_structure_object_unclosed_no_value.json",
//			"test_parsing/n_structure_object_with_comment.json",
//			"test_parsing/n_structure_object_with_trailing_garbage.json",
//			"test_parsing/n_structure_open_array_apostrophe.json", "test_parsing/n_structure_open_array_comma.json",
//			"test_parsing/n_structure_open_array_object.json", "test_parsing/n_structure_open_array_open_object.json",
//			"test_parsing/n_structure_open_array_open_string.json", "test_parsing/n_structure_open_array_string.json",
//			"test_parsing/n_structure_open_object.json", "test_parsing/n_structure_open_object_close_array.json",
//			"test_parsing/n_structure_open_object_comma.json", "test_parsing/n_structure_open_object_open_array.json",
//			"test_parsing/n_structure_open_object_open_string.json",
//			"test_parsing/n_structure_open_object_string_with_apostrophes.json",
//			"test_parsing/n_structure_open_open.json", "test_parsing/n_structure_single_eacute.json",
//			"test_parsing/n_structure_single_star.json", "test_parsing/n_structure_trailing_#.json",
//			"test_parsing/n_structure_uescaped_LF_before_string.json", "test_parsing/n_structure_unclosed_array.json",
//			"test_parsing/n_structure_unclosed_array_partial_null.json",
//			"test_parsing/n_structure_unclosed_array_unfinished_false.json",
//			"test_parsing/n_structure_unclosed_array_unfinished_true.json",
//			"test_parsing/n_structure_unclosed_object.json", "test_parsing/n_structure_unicode-identifier.json",
//			"test_parsing/n_structure_whitespace_U+2060_word_joiner.json",
//			"test_parsing/n_structure_whitespace_formfeed.json", "test_parsing/y_array_arraysWithSpaces.json",
//			"test_parsing/y_array_empty-string.json", "test_parsing/y_array_empty.json",
//			"test_parsing/y_array_ending_with_newline.json", "test_parsing/y_array_false.json",
//			"test_parsing/y_array_heterogeneous.json", "test_parsing/y_array_null.json",
//			"test_parsing/y_array_with_1_and_newline.json", "test_parsing/y_array_with_leading_space.json",
//			"test_parsing/y_array_with_several_null.json", "test_parsing/y_array_with_trailing_space.json",
//			"test_parsing/y_number.json", "test_parsing/y_number_0e+1.json", "test_parsing/y_number_0e1.json",
//			"test_parsing/y_number_after_space.json", "test_parsing/y_number_double_close_to_zero.json",
//			"test_parsing/y_number_int_with_exp.json", "test_parsing/y_number_minus_zero.json",
//			"test_parsing/y_number_negative_int.json", "test_parsing/y_number_negative_one.json",
//			"test_parsing/y_number_negative_zero.json", "test_parsing/y_number_real_capital_e.json",
//			"test_parsing/y_number_real_capital_e_neg_exp.json", "test_parsing/y_number_real_capital_e_pos_exp.json",
//			"test_parsing/y_number_real_exponent.json", "test_parsing/y_number_real_fraction_exponent.json",
//			"test_parsing/y_number_real_neg_exp.json", "test_parsing/y_number_real_pos_exponent.json",
//			"test_parsing/y_number_simple_int.json", "test_parsing/y_number_simple_real.json",
//			"test_parsing/y_object.json", "test_parsing/y_object_basic.json",
//			"test_parsing/y_object_duplicated_key.json", "test_parsing/y_object_duplicated_key_and_value.json",
//			"test_parsing/y_object_empty.json", "test_parsing/y_object_empty_key.json",
//			"test_parsing/y_object_escaped_null_in_key.json", "test_parsing/y_object_extreme_numbers.json",
//			"test_parsing/y_object_long_strings.json", "test_parsing/y_object_simple.json",
//			"test_parsing/y_object_string_unicode.json", "test_parsing/y_object_with_newlines.json",
//			"test_parsing/y_string_1_2_3_bytes_UTF-8_sequences.json",
//			"test_parsing/y_string_accepted_surrogate_pair.json", "test_parsing/y_string_accepted_surrogate_pairs.json",
//			"test_parsing/y_string_allowed_escapes.json", "test_parsing/y_string_backslash_and_u_escaped_zero.json",
//			"test_parsing/y_string_backslash_doublequotes.json", "test_parsing/y_string_comments.json",
//			"test_parsing/y_string_double_escape_a.json", "test_parsing/y_string_double_escape_n.json",
//			"test_parsing/y_string_escaped_control_character.json", "test_parsing/y_string_escaped_noncharacter.json",
//			"test_parsing/y_string_in_array.json", "test_parsing/y_string_in_array_with_leading_space.json",
//			"test_parsing/y_string_last_surrogates_1_and_2.json", "test_parsing/y_string_nbsp_uescaped.json",
//			"test_parsing/y_string_nonCharacterInUTF-8_U+10FFFF.json",
//			"test_parsing/y_string_nonCharacterInUTF-8_U+FFFF.json", "test_parsing/y_string_null_escape.json",
//			"test_parsing/y_string_one-byte-utf-8.json", "test_parsing/y_string_pi.json",
//			"test_parsing/y_string_reservedCharacterInUTF-8_U+1BFFF.json", "test_parsing/y_string_simple_ascii.json",
//			"test_parsing/y_string_space.json", "test_parsing/y_string_surrogates_U+1D11E_MUSICAL_SYMBOL_G_CLEF.json",
//			"test_parsing/y_string_three-byte-utf-8.json", "test_parsing/y_string_two-byte-utf-8.json",
//			"test_parsing/y_string_u+2028_line_sep.json", "test_parsing/y_string_u+2029_par_sep.json",
//			"test_parsing/y_string_uEscape.json", "test_parsing/y_string_uescaped_newline.json",
//			"test_parsing/y_string_unescaped_char_delete.json", "test_parsing/y_string_unicode.json",
//			"test_parsing/y_string_unicodeEscapedBackslash.json", "test_parsing/y_string_unicode_2.json",
//			"test_parsing/y_string_unicode_U+10FFFE_nonchar.json", "test_parsing/y_string_unicode_U+1FFFE_nonchar.json",
//			"test_parsing/y_string_unicode_U+200B_ZERO_WIDTH_SPACE.json",
//			"test_parsing/y_string_unicode_U+2064_invisible_plus.json",
//			"test_parsing/y_string_unicode_U+FDD0_nonchar.json", "test_parsing/y_string_unicode_U+FFFE_nonchar.json",
//			"test_parsing/y_string_unicode_escaped_double_quote.json", "test_parsing/y_string_utf8.json",
//			"test_parsing/y_string_with_del_character.json", "test_parsing/y_structure_lonely_false.json",
//			"test_parsing/y_structure_lonely_int.json", "test_parsing/y_structure_lonely_negative_real.json",
//			"test_parsing/y_structure_lonely_null.json", "test_parsing/y_structure_lonely_string.json",
//			"test_parsing/y_structure_lonely_true.json", "test_parsing/y_structure_string_empty.json",
//			"test_parsing/y_structure_trailing_newline.json", "test_parsing/y_structure_true_in_array.json",
//			"test_parsing/y_structure_whitespace_array.json");

//	@Test
//	void testFromFile() {
//
//		assertFalse(TEST_PARSING.isEmpty());
//		assertFalse(TEST_TRANSFORM.isEmpty());
//
//		Stream.concat(TEST_PARSING.stream(), TEST_TRANSFORM.stream()).map(name -> {
//			try (BufferedReader br = new BufferedReader(
//					new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(name)))) {
//
//				String line;
//				StringBuffer sb = new StringBuffer();
//				while ((line = br.readLine()) != null)
//					sb.append(line);
//
//				return List.of(name, sb.toString());
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				throw new RuntimeException(ex);
//			}
//		}).forEach(this::compare);
//	}

//	void compare(List<String> json) {
//		try {
//
//			ObjectMapper objectMapper = new ObjectMapper();			
//			BSON bson = jsonParser.parseJSONString(json.get(1));
//			JsonNode jn = objectMapper.readTree(json.get(1));
//
//			boolean flag = compareNodeToBSON(jn, bson);
//			PrintStream stream = (flag ? System.out : System.err);
//			stream.println(json.get(0));
//			if (!flag) {
//				stream.println(json.get(1));
//				stream.println(jn);
//				stream.println(bson);
//			}
//			stream.println("-----------------------------------------\n");
//		} catch (Exception ex) {
//			System.err.println(json.get(0));
//			ex.printStackTrace();
//			System.err.println("-----------------------------------------\n");
////			throw new RuntimeException(ex);
//		}
//	}

//	boolean compareNodeToBSON(final JsonNode jn, final BSON bson) {
//
//		if (jn.getNodeType() == JsonNodeType.ARRAY) {
//
//			List<BSON> b = bson.getValue();
//			if (jn.size() != b.size())
//				return false;
//			boolean flag = true;
//			for (int i = 0; i < b.size() && flag; i++)
//				flag = flag && compareNodeToBSON(jn.get(i), b.get(i));
//			return flag;
//		} else if (jn.getNodeType() == JsonNodeType.OBJECT) {
//
//			Map<BSON, BSON> bb = bson.getAsBSONMap();
//			Map<String, BSON> b = bb.entrySet().stream().map(e -> new Map.Entry<String, BSON>() {
//				BSON v;
//
//				public String getKey() {
//					return e.getKey().getValue();
//				}
//
//				public BSON getValue() {
//					return v == null ? e.getValue() : v;
//				}
//
//				@Override
//				public BSON setValue(BSON value) {
//					v = value;
//					return v;
//				}
//			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//			Set<String> jnKeys = StreamSupport.stream(Spliterators.spliteratorUnknownSize(jn.fieldNames(), 0), false)
//					.collect(Collectors.toSet());
//			if (!jnKeys.equals(b.keySet()))
//				return false;
//
//			if (!b.entrySet().stream().allMatch(e -> compareNodeToBSON(jn.get(e.getKey()), e.getValue())))
//				return false;
//		} else if (jn.getNodeType() == JsonNodeType.BOOLEAN) {
//			if (!((Boolean) bson.getValue()).equals(jn.asBoolean()))
//				return false;
//		} else if (jn.getNodeType() == JsonNodeType.NULL) {
//			if (!bson.isNull())
//				return false;
//		} else if (jn.getNodeType() == JsonNodeType.NUMBER) {
//
//			if (bson.getType() == BSON.BYTE) {
//				if (!(Byte.valueOf("" + jn.asInt()).equals(bson.getValue())))
//					return false;
//			} else if (bson.getType() == BSON.INTEGER) {
//				if (!(Integer.valueOf(jn.asInt()).equals(bson.getValue())))
//					return false;
//			} else if (bson.getType() == BSON.SHORT) {
//				if (!(Short.valueOf("" + jn.asInt()).equals(bson.getValue())))
//					return false;
//			} else if (bson.getType() == BSON.LONG) {
//				if (!(Long.valueOf(jn.asLong()).equals(bson.getValue())))
//					return false;
//			} else if (bson.getType() == BSON.FLOAT) {
//				if (!(Float.valueOf(Double.valueOf(jn.asDouble()).floatValue()).equals(bson.getValue())))
//					return false;
//			} else if (bson.getType() == BSON.DOUBLE) {
//				if (!(Double.valueOf(jn.asDouble())).equals(bson.getValue()))
//					return false;
//			} else {
//				return false;
//			}
//
//		} else if (jn.getNodeType() == JsonNodeType.STRING) {
//			if (!jn.asText().equals(bson.getValue()))
//				return false;
//		}
//
//		return true;
//	}

//	boolean compareNodeToBSON(final JsonNode jn, final BSON bson) {
//
//		if (jn.getNodeType() == JsonNodeType.ARRAY) {
//
//			List<BSON> b = bson.getValue();
//			assertEquals(jn.size(), b.size());
//			for (int i = 0; i < b.size(); i++)
//				assertTrue(compareNodeToBSON(jn.get(i), b.get(i)));
//		} else if (jn.getNodeType() == JsonNodeType.OBJECT) {
//
//			Map<BSON, BSON> bb = bson.getAsBSONMap();
//			Map<String, BSON> b = bb.entrySet().stream().map(e -> new Map.Entry<String, BSON>() {
//				BSON v;
//
//				public String getKey() {
//					return e.getKey().getValue();
//				}
//
//				public BSON getValue() {
//					return v == null ? e.getValue() : v;
//				}
//
//				@Override
//				public BSON setValue(BSON value) {
//					v = value;
//					return v;
//				}
//			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//			Set<String> jnKeys = StreamSupport.stream(Spliterators.spliteratorUnknownSize(jn.fieldNames(), 0), false)
//					.collect(Collectors.toSet());
//			assertEquals(jnKeys, b.keySet());
//
//			assertTrue(b.entrySet().stream().allMatch(e -> compareNodeToBSON(jn.get(e.getKey()), e.getValue())));
//		} else if (jn.getNodeType() == JsonNodeType.BOOLEAN) {
//			assertEquals(jn.asBoolean(), bson.getValue());
//		} else if (jn.getNodeType() == JsonNodeType.NULL) {
//			assertTrue(bson.isNull());
//		} else if (jn.getNodeType() == JsonNodeType.NUMBER) {
//
//			if (bson.getType() == BSON.BYTE) {
//				assertEquals(Byte.valueOf("" + jn.asInt()), bson.getValue());
//			} else if (bson.getType() == BSON.INTEGER) {
//				assertEquals(Integer.valueOf(jn.asInt()), bson.getValue());
//			} else if (bson.getType() == BSON.SHORT) {
//				assertEquals(Short.valueOf("" + jn.asInt()), bson.getValue());
//			} else if (bson.getType() == BSON.LONG) {
//				assertEquals(Long.valueOf(jn.asLong()), bson.getValue());
//			} else if (bson.getType() == BSON.FLOAT) {
//				assertEquals(Float.valueOf(Double.valueOf(jn.asDouble()).floatValue()), bson.getValue());
//			} else if (bson.getType() == BSON.DOUBLE) {
//				assertEquals(Double.valueOf(jn.asDouble()), bson.getValue());
//			} else {
//				return false;
//			}
//
//		} else if (jn.getNodeType() == JsonNodeType.STRING) {
//			assertEquals(jn.asText(), bson.getValue());
//		}
//
//		return true;
//	}
}

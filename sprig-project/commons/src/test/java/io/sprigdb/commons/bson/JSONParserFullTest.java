package io.sprigdb.commons.bson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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
			"test_parsing/n_array_1_true_without_comma.json", new Definition("Expecting a ',' or ']' at position : 3"),
			"test_parsing/n_array_a_invalid_utf8.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_colon_instead_of_comma.json", new Definition("Unknown symbol found after position : 1"),
			"test_parsing/n_array_comma_after_close.json", new Definition("Unknown symbol ',' at position : 5"),
			"test_parsing/n_array_comma_and_number.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_double_comma.json", new Definition("Unknown/No value found at position : 4"),
			"test_parsing/n_array_double_extra_comma.json", new Definition("Unknown/No value found at position : 6"),
			"test_parsing/n_array_extra_close.json", new Definition("Unknown symbol ']' at position : 6"),
			"test_parsing/n_array_extra_comma.json", new Definition("No value found at position : 5"),
			"test_parsing/n_array_incomplete_invalid_value.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_incomplete.json", new Definition("Array not terminated"),
			"test_parsing/n_array_inner_array_no_comma.json", new Definition("Unknown symbol while parsing number : '[' at position : 3"),
			"test_parsing/n_array_invalid_utf8.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_items_separated_by_semicolon.json", new Definition("Unknown symbol while parsing number : ':' at position : 3"),
			"test_parsing/n_array_just_comma.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_just_minus.json", new Definition("Unable to parse to a number : '-'"),
			"test_parsing/n_array_missing_value.json", new Definition("Unknown/No value found at position : 5"),
			"test_parsing/n_array_newlines_unclosed.json", new Definition("Unknown/No value found at position : 12"),
			"test_parsing/n_array_number_and_comma.json", new Definition("No value found at position : 4"),
			"test_parsing/n_array_number_and_several_commas.json", new Definition("Unknown/No value found at position : 4"),
			"test_parsing/n_array_spaces_vertical_tab_formfeed.json", new Definition("Unknown symbol found after position : 1"),
			"test_parsing/n_array_star_inside.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_array_unclosed_trailing_comma.json", new Definition("Unknown/No value found at position : 4"),
			"test_parsing/n_array_unclosed_with_new_lines.json", new Definition("Expected closing ']' at position : 9"),
			"test_parsing/n_array_unclosed_with_object_inside.json", new Definition("Array is not terminated."),
			"test_parsing/n_array_unclosed.json", new Definition("Array not terminated"),
			"test_parsing/n_incomplete_false.json", new Definition("Expected value 'false' at position : 2"),
			"test_parsing/n_incomplete_null.json", new Definition("Expected value 'null' at position : 2"),
			"test_parsing/n_incomplete_true.json", new Definition("Expected value 'true' at position : 2"),
			"test_parsing/n_multidigit_number_then_00.json", new Definition("Unknown symbol ' ' at position : 4"),
			"test_parsing/n_number_++.json", new Definition("Unable to parse to a number : '++1234'"),
			"test_parsing/n_number_+1.json", new Definition(List.of(Byte.valueOf((byte)1))),
			"test_parsing/n_number_+Inf.json", new Definition("Unknown symbol while parsing number : 'I' at position : 3"),
			"test_parsing/n_number_-01.json", new Definition(List.of(Byte.valueOf((byte)-1))),
			"test_parsing/n_number_-1.0..json", new Definition("Number cannot be terminated with '.' at position : 7"),
			"test_parsing/n_number_-2..json", new Definition("Number cannot be terminated with '.' at position : 5"),
			"test_parsing/n_number_-NaN.json", new Definition("Unknown symbol while parsing number : 'N' at position : 3"),
			"test_parsing/n_number_.-1.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_.2e-3.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_0.1.2.json", new Definition("Unable to parse to a number : '0.1.2'"),
			"test_parsing/n_number_0.3e+.json", new Definition("Unable to parse to a number : '0.3e+'"),
			"test_parsing/n_number_0.3e.json", new Definition("Unable to parse to a number : '0.3e'"),
			"test_parsing/n_number_0.e1.json", new Definition("Exponent marker cannot be followed by '.' at position : 4"),
			"test_parsing/n_number_0_capital_E.json", new Definition("Unable to parse to a number : '0E'"),
			"test_parsing/n_number_0_capital_E+.json", new Definition("Unable to parse to a number : '0E+'"),
			"test_parsing/n_number_0e+.json", new Definition("Unable to parse to a number : '0e+'"),
			"test_parsing/n_number_0e.json", new Definition("Unable to parse to a number : '0e'"),
			"test_parsing/n_number_1.0e+.json", new Definition("Unable to parse to a number : '1.0e+'"),
			"test_parsing/n_number_1.0e-.json", new Definition("Unable to parse to a number : '1.0e-'"),
			"test_parsing/n_number_1.0e.json", new Definition("Unable to parse to a number : '1.0e'"),
			"test_parsing/n_number_1_000.json", new Definition("Expecting a ',' or ']' at position : 3"),
			"test_parsing/n_number_1eE2.json", new Definition("Unable to parse to a number : '1eE2'"),
			"test_parsing/n_number_2.e+3.json", new Definition("Exponent marker cannot be followed by '.' at position : 4"),
			"test_parsing/n_number_2.e-3.json", new Definition("Exponent marker cannot be followed by '.' at position : 4"),
			"test_parsing/n_number_2.e3.json", new Definition("Exponent marker cannot be followed by '.' at position : 4"),
			"test_parsing/n_number_9.e+.json", new Definition("Exponent marker cannot be followed by '.' at position : 4"),
			"test_parsing/n_number_Inf.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_NaN.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_U+FF11_fullwidth_digit_one.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_expression.json", new Definition("Unable to parse to a number : '1+2'"),
			"test_parsing/n_number_hex_1_digit.json", new Definition("Unknown symbol while parsing number : 'x' at position : 3"),
			"test_parsing/n_number_hex_2_digits.json", new Definition("Unknown symbol while parsing number : 'x' at position : 3"),
			"test_parsing/n_number_infinity.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_invalid+-.json", new Definition("Unable to parse to a number : '0e+-1'"),
			"test_parsing/n_number_invalid-negative-real.json", new Definition("Unknown symbol while parsing number : 'f' at position : 10"),
			"test_parsing/n_number_invalid-utf-8-in-bigger-int.json", new Definition("Unknown symbol while parsing number : '�' at position : 5"),
			"test_parsing/n_number_invalid-utf-8-in-exponent.json", new Definition("Unknown symbol while parsing number : '�' at position : 5"),
			"test_parsing/n_number_invalid-utf-8-in-int.json", new Definition("Unknown symbol while parsing number : '�' at position : 3"),
			"test_parsing/n_number_minus_infinity.json", new Definition("Unknown symbol while parsing number : 'I' at position : 3"),
			"test_parsing/n_number_minus_sign_with_trailing_garbage.json", new Definition("Unknown symbol while parsing number : 'f' at position : 3"),
			"test_parsing/n_number_minus_space_1.json", new Definition("Unable to parse to a number : '-'"),
			"test_parsing/n_number_neg_int_starting_with_zero.json", new Definition(List.of((byte)-12)),
			"test_parsing/n_number_neg_real_without_int_part.json", new Definition(List.of(-0.123f)),
			"test_parsing/n_number_neg_with_garbage_at_end.json", new Definition("Unknown symbol while parsing number : 'x' at position : 4"),
			"test_parsing/n_number_real_garbage_after_e.json", new Definition("Unknown symbol while parsing number : 'a' at position : 4"),
			"test_parsing/n_number_real_with_invalid_utf8_after_e.json", new Definition("Unknown symbol while parsing number : '�' at position : 4"),
			"test_parsing/n_number_real_without_fractional_part.json", new Definition("Number cannot be terminated with '.' at position : 4"),
			"test_parsing/n_number_starting_with_dot.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_number_with_alpha.json", new Definition("Unknown symbol while parsing number : 'a' at position : 5"),
			"test_parsing/n_number_with_alpha_char.json", new Definition("Unknown symbol while parsing number : 'H' at position : 20"),
			"test_parsing/n_number_with_leading_zero.json", new Definition(List.of((byte)12)),
			"test_parsing/n_object_bad_value.json", new Definition("Expected value 'true' at position : 7"),
			"test_parsing/n_object_bracket_key.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_comma_instead_of_colon.json", new Definition("Expected ':' at position : 5"),
			"test_parsing/n_object_double_colon.json", new Definition("Unknown symbol ':' at position : 6"),
			"test_parsing/n_object_emoji.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_garbage_at_end.json", new Definition("Expecting a ',' or '}' at position : 10"),
			"test_parsing/n_object_key_with_single_quotes.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_lone_continuation_byte_in_key_and_trailing_comma.json", new Definition("Expecting a string at position : 9"),
			"test_parsing/n_object_missing_colon.json", new Definition("Expected ':' at position : 6"),
			"test_parsing/n_object_missing_key.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_missing_semicolon.json", new Definition("Expected ':' at position : 6"),
			"test_parsing/n_object_missing_value.json", new Definition("Object is not closed in the end"),
			"test_parsing/n_object_no-colon.json", new Definition("Expected ':' at position : 5"),
			"test_parsing/n_object_non_string_key.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_non_string_key_but_huge_number_instead.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_repeated_null_null.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_several_trailing_commas.json", new Definition("Unknown/No value found at position : 9"),
			"test_parsing/n_object_single_quote.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_trailing_comma.json", new Definition("Unknown/No value found at position : 9"),
			"test_parsing/n_object_trailing_comment.json", new Definition("Unknown symbol '/' at position : 10"),
			"test_parsing/n_object_trailing_comment_open.json", new Definition("Unknown symbol '/' at position : 10"),
			"test_parsing/n_object_trailing_comment_slash_open.json", new Definition("Unknown symbol '/' at position : 10"),
			"test_parsing/n_object_trailing_comment_slash_open_incomplete.json", new Definition("Unknown symbol '/' at position : 10"),
			"test_parsing/n_object_two_commas_in_a_row.json", new Definition("Expecting a string at position : 9"),
			"test_parsing/n_object_unquoted_key.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_object_unterminated-value.json", new Definition("String not terminated with '\"' at position : 7"),
			"test_parsing/n_object_with_single_string.json", new Definition("Expected ':' at position : 22"),
			"test_parsing/n_object_with_trailing_garbage.json", new Definition("Unknown symbol '#' at position : 10"),
			"test_parsing/n_single_space.json", new Definition(null),
			"test_parsing/n_string_1_surrogate_then_escape.json", new Definition("String not terminated with '\"' at position : 11"),
			"test_parsing/n_string_1_surrogate_then_escape_u.json", new Definition(List.of("\\uD800\\u")),
			"test_parsing/n_string_1_surrogate_then_escape_u1.json", new Definition(List.of("\\uD800\\u1")),
			"test_parsing/n_string_1_surrogate_then_escape_u1x.json", new Definition(List.of("\\uD800\\u1x")),
			"test_parsing/n_string_accentuated_char_no_quotes.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_string_backslash_00.json", new Definition(List.of("\\ ")),
			"test_parsing/n_string_escape_x.json", new Definition(List.of("\\x00")),
			"test_parsing/n_string_escaped_backslash_bad.json", new Definition("String not terminated with '\"' at position : 7"),
			"test_parsing/n_string_escaped_ctrl_char_tab.json", new Definition(List.of("\\	")),
			"test_parsing/n_string_escaped_emoji.json", new Definition(List.of("\\🌀")),
			"test_parsing/n_string_incomplete_escape.json", new Definition("String not terminated with '\"' at position : 5"),
			"test_parsing/n_string_incomplete_escaped_character.json",new Definition(List.of("\\u00A")),
			"test_parsing/n_string_incomplete_surrogate.json", new Definition(List.of("\\uD834\\uDd")),
			"test_parsing/n_string_incomplete_surrogate_escape_invalid.json", new Definition(List.of("\\uD800\\uD800\\x")),
			"test_parsing/n_string_invalid-utf-8-in-escape.json", new Definition(List.of("\\u�")),
			"test_parsing/n_string_invalid_backslash_esc.json", new Definition(List.of("\\a")),
			"test_parsing/n_string_invalid_unicode_escape.json", new Definition(List.of("\\uqqqq")),
			"test_parsing/n_string_invalid_utf8_after_escape.json", new Definition(List.of("\\�")),
			"test_parsing/n_string_leading_uescaped_thinspace.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_string_no_quotes_with_bad_escape.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_string_single_doublequote.json", new Definition("String not terminated with '\"' at position : 1"),
			"test_parsing/n_string_single_quote.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_string_single_string_no_double_quotes.json", new Definition("Unknown symbol 'a' at position : 1"),
			"test_parsing/n_string_start_escape_unclosed.json", new Definition("String not terminated with '\"' at position : 3"),
			"test_parsing/n_string_unescaped_crtl_char.json", new Definition(List.of("a a")),
			"test_parsing/n_string_unescaped_newline.json", new Definition("String cannot have line breaks at : 2"),
			"test_parsing/n_string_unescaped_tab.json", new Definition(List.of("	")),
			"test_parsing/n_string_unicode_CapitalU.json", new Definition(false, "\\UA66D"),
			"test_parsing/n_string_with_trailing_garbage.json", new Definition("Unknown symbol 'x' at position : 3"),
			"test_parsing/n_structure_100000_opening_arrays.json", new Definition("Unknown/No value found at position : 100001"),
			"test_parsing/n_structure_U+2060_word_joined.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_UTF8_BOM_no_data.json", new Definition(null),
			"test_parsing/n_structure_angle_bracket_..json", new Definition("Unknown symbol '<' at position : 1"),
			"test_parsing/n_structure_angle_bracket_null.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_array_trailing_garbage.json", new Definition("Unknown symbol 'x' at position : 4"),
			"test_parsing/n_structure_array_with_extra_array_close.json", new Definition("Unknown symbol ']' at position : 4"),
			"test_parsing/n_structure_array_with_unclosed_string.json", new Definition("String not terminated with '\"' at position : 6"),
			"test_parsing/n_structure_ascii-unicode-identifier.json", new Definition("Unknown symbol 'a' at position : 1"),
			"test_parsing/n_structure_capitalized_True.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_close_unopened_array.json", new Definition("Unknown symbol while parsing number : ']' at position : 2"),
			"test_parsing/n_structure_comma_instead_of_closing_brace.json", new Definition("Unknown/No value found at position : 12"),
			"test_parsing/n_structure_double_array.json", new Definition("Unknown symbol '[' at position : 3"),
			"test_parsing/n_structure_end_array.json", new Definition("']' found without array start '['"),
			"test_parsing/n_structure_incomplete_UTF8_BOM.json", new Definition("Unknown symbol '�' at position : 1"),
			"test_parsing/n_structure_lone-invalid-utf-8.json", new Definition("Unknown symbol '�' at position : 1"),
			"test_parsing/n_structure_lone-open-bracket.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_no_data.json", new Definition(null),
			"test_parsing/n_structure_null-byte-outside-string.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_number_with_trailing_garbage.json", new Definition("Unknown symbol while parsing number : '@' at position : 2"),
			"test_parsing/n_structure_object_followed_by_closing_object.json", new Definition("Unknown symbol '}' at position : 3"),
			"test_parsing/n_structure_object_unclosed_no_value.json", new Definition("Object is not closed in the end"),
			"test_parsing/n_structure_object_with_comment.json", new Definition("Unknown symbol '/' at position : 6"),
			"test_parsing/n_structure_object_with_trailing_garbage.json", new Definition("Unknown symbol '\"' at position : 13"),
			"test_parsing/n_structure_open_array_apostrophe.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_open_array_comma.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_open_array_object.json", new Definition("Object is not closed in the end"),
			"test_parsing/n_structure_open_array_open_object.json", new Definition("Expected a String at position : 3"),
			"test_parsing/n_structure_open_array_open_string.json", new Definition("String not terminated with '\"' at position : 3"),
			"test_parsing/n_structure_open_array_string.json", new Definition("Array not terminated"),
			"test_parsing/n_structure_open_object.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_structure_open_object_close_array.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_structure_open_object_comma.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_structure_open_object_open_array.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_structure_open_object_open_string.json", new Definition("String not terminated with '\"' at position : 3"),
			"test_parsing/n_structure_open_object_string_with_apostrophes.json", new Definition("Expected a String at position : 2"),
			"test_parsing/n_structure_open_open.json", new Definition("Unknown symbol found after position : 1"),
			"test_parsing/n_structure_single_eacute.json", new Definition("Unknown symbol '�' at position : 1"),
			"test_parsing/n_structure_single_star.json", new Definition("Unknown symbol '*' at position : 1"),
			"test_parsing/n_structure_trailing_#.json", new Definition("Unknown symbol '#' at position : 10"),
			"test_parsing/n_structure_uescaped_LF_before_string.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_unclosed_array.json", new Definition("Expected closing ']' at position : 3"),
			"test_parsing/n_structure_unclosed_array_partial_null.json", new Definition("Expected value 'null' at position : 10"),
			"test_parsing/n_structure_unclosed_array_unfinished_false.json", new Definition("Expected value 'false' at position : 9"),
			"test_parsing/n_structure_unclosed_array_unfinished_true.json", new Definition("Expected value 'true' at position : 10"),
			"test_parsing/n_structure_unclosed_object.json", new Definition("Expecting a ',' or '}' at position : 13"),
			"test_parsing/n_structure_unicode-identifier.json", new Definition("Unknown symbol 'å' at position : 1"),
			"test_parsing/n_structure_whitespace_U+2060_word_joiner.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/n_structure_whitespace_formfeed.json", new Definition("Unknown/No value found at position : 2"),
			"test_parsing/y_array_arraysWithSpaces.json", new Definition(List.of(List.of())),
			"test_parsing/y_array_empty-string.json", new Definition(List.of("")),
			"test_parsing/y_array_empty.json", new Definition(List.of()),
			"test_parsing/y_array_ending_with_newline.json", new Definition(List.of("a")),
			"test_parsing/y_array_false.json", new Definition(List.of(false)),
			"test_parsing/y_array_heterogeneous.json", new Definition(Arrays.asList(null, Byte.valueOf((byte)1), "1", Map.of())),
			"test_parsing/y_array_null.json", new Definition(Arrays.asList(new Object[] { null })),
			"test_parsing/y_array_with_1_and_newline.json", new Definition(List.of(Byte.valueOf((byte)1))),
			"test_parsing/y_array_with_leading_space.json", new Definition(List.of(Byte.valueOf((byte)1))),
			"test_parsing/y_array_with_several_null.json", new Definition(Arrays.asList(new Object[] { Byte.valueOf((byte)1), null, null, null, Byte.valueOf((byte)2)})),
			"test_parsing/y_array_with_trailing_space.json", new Definition(List.of((byte) 2)),
			"test_parsing/y_number.json", new Definition(List.of(123e65)),
			"test_parsing/y_number_0e+1.json", new Definition(List.of(new BigDecimal("0E+1"))),
			"test_parsing/y_number_0e1.json", new Definition(List.of(new BigDecimal("0E+1"))),
			"test_parsing/y_number_after_space.json", new Definition(List.of((byte) 4)),
			"test_parsing/y_number_double_close_to_zero.json", new Definition(List.of(Double.parseDouble("-0.000000000000000000000000000000000000000000000000000000000000000000000000000001"))),
			"test_parsing/y_number_int_with_exp.json", new Definition(List.of(20E1f)),
			"test_parsing/y_number_minus_zero.json", new Definition(List.of((byte) 0)),
			"test_parsing/y_number_negative_int.json", new Definition(List.of((byte) -123)),
			"test_parsing/y_number_negative_one.json", new Definition(List.of((byte) -1)),
			"test_parsing/y_number_negative_zero.json", new Definition(List.of((byte) 0)),
			"test_parsing/y_number_real_capital_e.json", new Definition(List.of(1E22f)),
			"test_parsing/y_number_real_capital_e_neg_exp.json", new Definition(List.of(1E-2f)),
			"test_parsing/y_number_real_capital_e_pos_exp.json", new Definition(List.of(1E+2f)),
			"test_parsing/y_number_real_exponent.json", new Definition(List.of(123e45)),
			"test_parsing/y_number_real_fraction_exponent.json", new Definition(List.of(123.456e78)),
			"test_parsing/y_number_real_neg_exp.json", new Definition(List.of(1e-2f)),
			"test_parsing/y_number_real_pos_exponent.json", new Definition(List.of(1e+2f)),
			"test_parsing/y_number_simple_int.json", new Definition(List.of((byte)123)),
			"test_parsing/y_number_simple_real.json", new Definition(List.of(123.456789f)),
			"test_parsing/y_object.json", new Definition(Map.of("asd", "sdf", "dfg", "fgh")),
			"test_parsing/y_object_basic.json", new Definition(Map.of("asd", "sdf")),
			"test_parsing/y_object_duplicated_key.json", new Definition(Map.of("a", "c")),
			"test_parsing/y_object_duplicated_key_and_value.json", new Definition(Map.of("a", "b")),
			"test_parsing/y_object_empty.json", new Definition(Map.of()),
			"test_parsing/y_object_empty_key.json", new Definition(Map.of("", (byte)0)),
			"test_parsing/y_object_escaped_null_in_key.json", new Definition(Map.of("foo\\u0000bar", (byte)42)),
			"test_parsing/y_object_extreme_numbers.json", new Definition(Map.of("min", -1.0e+28f, "max", 1.0e+28f)),
			"test_parsing/y_object_long_strings.json", new Definition(Map.of("x", List.of(Map.of("id", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")), "id", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")),
			"test_parsing/y_object_simple.json", new Definition(Map.of("a", List.of())),
			"test_parsing/y_object_string_unicode.json", new Definition(Map.of("title", "\\u041f\\u043e\\u043b\\u0442\\u043e\\u0440\\u0430 \\u0417\\u0435\\u043c\\u043b\\u0435\\u043a\\u043e\\u043f\\u0430")),
			"test_parsing/y_object_with_newlines.json", new Definition(Map.of("a", "b")),
			"test_parsing/y_string_1_2_3_bytes_UTF-8_sequences.json", new Definition(List.of("\\u0060\\u012a\\u12AB")),
			"test_parsing/y_string_accepted_surrogate_pair.json", new Definition(List.of("\\uD801\\udc37")),
			"test_parsing/y_string_accepted_surrogate_pairs.json", new Definition(List.of("\\ud83d\\ude39\\ud83d\\udc8d")),
			"test_parsing/y_string_allowed_escapes.json", new Definition(List.of("\\\"\\\\\\/\\b\\f\\n\\r\\t")),
			"test_parsing/y_string_backslash_and_u_escaped_zero.json", new Definition(List.of("\\\\u0000")),
			"test_parsing/y_string_backslash_doublequotes.json", new Definition(List.of("\\\"")),
			"test_parsing/y_string_comments.json", new Definition(List.of("a/*b*/c/*d//e")),
			"test_parsing/y_string_double_escape_a.json", new Definition(List.of("\\\\a")),
			"test_parsing/y_string_double_escape_n.json", new Definition(List.of("\\\\n")),
			"test_parsing/y_string_escaped_control_character.json", new Definition(List.of("\\u0012")),
			"test_parsing/y_string_escaped_noncharacter.json", new Definition(List.of("\\uFFFF")),
			"test_parsing/y_string_in_array.json", new Definition(List.of("asd")),
			"test_parsing/y_string_in_array_with_leading_space.json", new Definition(List.of("asd")),
			"test_parsing/y_string_last_surrogates_1_and_2.json", new Definition(List.of("\\uDBFF\\uDFFF")),
			"test_parsing/y_string_nbsp_uescaped.json", new Definition(List.of("new\\u00A0line")),
			"test_parsing/y_string_nonCharacterInUTF-8_U+10FFFF.json", new Definition(List.of("􏿿")),
			"test_parsing/y_string_nonCharacterInUTF-8_U+FFFF.json", new Definition(List.of("￿")),
			"test_parsing/y_string_null_escape.json", new Definition(List.of("\\u0000")),
			"test_parsing/y_string_one-byte-utf-8.json", new Definition(List.of("\\u002c")),
			"test_parsing/y_string_pi.json", new Definition(List.of("π")),
			"test_parsing/y_string_reservedCharacterInUTF-8_U+1BFFF.json", new Definition(List.of("𛿿")),
			"test_parsing/y_string_simple_ascii.json", new Definition(List.of("asd ")),
			"test_parsing/y_string_space.json", new Definition(false, " "),
			"test_parsing/y_string_surrogates_U+1D11E_MUSICAL_SYMBOL_G_CLEF.json", new Definition(List.of("\\uD834\\uDd1e")),
			"test_parsing/y_string_three-byte-utf-8.json", new Definition(List.of("\\u0821")),
			"test_parsing/y_string_two-byte-utf-8.json", new Definition(List.of("\\u0123")),
			"test_parsing/y_string_u+2028_line_sep.json", new Definition(List.of(" ")),
			"test_parsing/y_string_u+2029_par_sep.json", new Definition(List.of(" ")),
			"test_parsing/y_string_uEscape.json", new Definition(List.of("\\u0061\\u30af\\u30EA\\u30b9")),
			"test_parsing/y_string_uescaped_newline.json", new Definition(List.of("new\\u000Aline")),
			"test_parsing/y_string_unescaped_char_delete.json", new Definition(List.of("")),
			"test_parsing/y_string_unicode.json", new Definition(List.of("\\uA66D")),
			"test_parsing/y_string_unicodeEscapedBackslash.json", new Definition(List.of("\\u005C")),
			"test_parsing/y_string_unicode_2.json", new Definition(List.of("⍂㈴⍂")),
			"test_parsing/y_string_unicode_U+10FFFE_nonchar.json", new Definition(List.of("\\uDBFF\\uDFFE")),
			"test_parsing/y_string_unicode_U+1FFFE_nonchar.json", new Definition(List.of("\\uD83F\\uDFFE")),
			"test_parsing/y_string_unicode_U+200B_ZERO_WIDTH_SPACE.json", new Definition(List.of("\\u200B")),
			"test_parsing/y_string_unicode_U+2064_invisible_plus.json", new Definition(List.of("\\u2064")),
			"test_parsing/y_string_unicode_U+FDD0_nonchar.json", new Definition(List.of("\\uFDD0")),
			"test_parsing/y_string_unicode_U+FFFE_nonchar.json", new Definition(List.of("\\uFFFE")),
			"test_parsing/y_string_unicode_escaped_double_quote.json", new Definition(List.of("\\u0022")),
			"test_parsing/y_string_utf8.json", new Definition(List.of("€𝄞")),
			"test_parsing/y_string_with_del_character.json", new Definition(List.of("aa")),
			"test_parsing/y_structure_lonely_false.json", new Definition(false),
			"test_parsing/y_structure_lonely_int.json", new Definition((byte)42),
			"test_parsing/y_structure_lonely_negative_real.json", new Definition(-0.1f),
			"test_parsing/y_structure_lonely_null.json", new Definition(null),
			"test_parsing/y_structure_lonely_string.json", new Definition(false, "asd"),
			"test_parsing/y_structure_lonely_true.json", new Definition(true),
			"test_parsing/y_structure_string_empty.json", new Definition(false, ""),
			"test_parsing/y_structure_trailing_newline.json", new Definition(List.of("a")),
			"test_parsing/y_structure_true_in_array.json", new Definition(List.of(true)),
			"test_parsing/y_structure_whitespace_array.json", new Definition(List.of()),
			
			"test_transform/number_-9223372036854775808.json", new Definition(List.of(-9223372036854775808l)),
			"test_transform/number_-9223372036854775809.json", new Definition(List.of(new BigInteger("-9223372036854775809"))),
			"test_transform/number_1.0.json", new Definition(List.of(1.0f)),
			"test_transform/number_1.000000000000000005.json", new Definition(List.of(1.000000000000000005f)),
			"test_transform/number_1000000000000000.json", new Definition(List.of(1000000000000000l)),
			"test_transform/number_10000000000000000999.json", new Definition(List.of(new BigInteger("10000000000000000999"))),
			"test_transform/number_1e-999.json", new Definition(List.of(new BigDecimal("1e-999"))),
			"test_transform/number_1e6.json", new Definition(List.of(1e6f)),
			"test_transform/number_9223372036854775807.json", new Definition(List.of(9223372036854775807l)),
			"test_transform/number_9223372036854775808.json", new Definition(List.of(new BigInteger("9223372036854775808"))),
			"test_transform/object_key_nfc_nfd.json", new Definition(Map.of("é", "NFC", "é", "NFD")),
			"test_transform/object_key_nfd_nfc.json", new Definition(Map.of("é", "NFD", "é", "NFC")),
			"test_transform/object_same_key_different_values.json", new Definition(Map.of("a", (byte)2)),
			"test_transform/object_same_key_same_value.json", new Definition(Map.of("a", (byte)1)),
			"test_transform/object_same_key_unclear_values.json", new Definition(Map.of("a", (byte)0)),
			"test_transform/string_1_escaped_invalid_codepoint.json", new Definition(List.of("\\uD800")),
			"test_transform/string_1_invalid_codepoint.json", new Definition(List.of("�")),
			"test_transform/string_2_escaped_invalid_codepoints.json", new Definition(List.of("\\uD800\\uD800")),
			"test_transform/string_2_invalid_codepoints.json", new Definition(List.of("��")),
			"test_transform/string_3_escaped_invalid_codepoints.json", new Definition(List.of("\\uD800\\uD800\\uD800")),
			"test_transform/string_3_invalid_codepoints.json", new Definition(List.of("���")),
			"test_transform/string_with_escaped_NULL.json", new Definition(List.of("A\\u0000B"))
			);
	//@formatter:on
	BSONParser bsonParser = new BSONParser();
	JSONParser jsonParser = new JSONParser();

	@Test
	void testFiles() {

		TEST_PARSING.entrySet().stream().forEach(e -> testEachCase(e.getKey(), e.getValue()));
	}

	void testEachCase(String key, Definition def) {

		try (BufferedInputStream bis = new BufferedInputStream(
				this.getClass().getClassLoader().getResourceAsStream(key))) {
			String jsonText = new String(bis.readAllBytes());
			try {
				BSON b = jsonParser.parseJSONString(jsonText);
				if (b.getType() == BSON.ARRAY) {
					List<Object> list = b.getAsList(jsonParser.getKeySubstitutor());
//					list.stream().forEach(e -> {
//						if (e == null)
//							System.out.println("null");
//						System.out.println(e.getClass());
//					});
					assertEquals(def.getValue(), list);
				} else if (b.getType() == BSON.OBJECT) {
					assertTrue(mapEqual(def.getValue(), (Object) b.getAsMap(jsonParser.getKeySubstitutor())));
				} else {
					assertEquals(def.getValue(), b.getValue());
				}
			} catch (Exception ex) {
				if (def.isException()) {
//					System.out.println(key);
					assertEquals(def.getValue(), ex.getMessage());
				} else {
					System.err.println(key);
					System.err.println(jsonText);
					System.err.println("--------------------\n");
					ex.printStackTrace();
					throw new RuntimeException(ex);
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

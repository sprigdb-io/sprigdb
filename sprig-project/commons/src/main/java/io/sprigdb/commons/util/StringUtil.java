package io.sprigdb.commons.util;

public class StringUtil {

	public static String trimInvisible(String s) {

		return new String(trimInvisible(s.toCharArray()));
	}

	public static char[] trimInvisible(char[] arr) {

		if (arr.length == 0)
			return arr;

		int start = -1;
		int end = -1;
		int i = 0;

		while (i < arr.length) {

			if (arr[i] > ' ') {
				if (start == -1)
					end = start = i;
				else
					end = i;
			}

			i++;
		}
		
		if (start == -1)
			return new char[0];
		
		if (end - start + 1 == arr.length)
			return arr;

		char[] newArr = new char[end - start + 1];
		System.arraycopy(arr, start, newArr, 0, end - start + 1);
		return newArr;
	}

	private StringUtil() {
	}
}

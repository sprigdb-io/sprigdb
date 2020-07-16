package io.sprigdb.commons.exceptions;

public class BSONDisorientedException extends RuntimeException {

	private static final long serialVersionUID = 2839688287010331261L;

	public BSONDisorientedException() {
		super("The bit pattern is misalinged and unable to read the BSON.");
	}
}

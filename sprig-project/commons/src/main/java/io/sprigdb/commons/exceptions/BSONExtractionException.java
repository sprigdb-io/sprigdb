package io.sprigdb.commons.exceptions;

public class BSONExtractionException extends RuntimeException {

	private static final long serialVersionUID = -2571961135536160026L;

	public BSONExtractionException() {
		super("Unknown marker to extract value from BSON.");
	}
}

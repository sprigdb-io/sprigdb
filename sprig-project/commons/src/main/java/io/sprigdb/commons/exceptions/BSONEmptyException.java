package io.sprigdb.commons.exceptions;

public class BSONEmptyException extends RuntimeException{

	private static final long serialVersionUID = -1640241193728348508L;

	public BSONEmptyException() {
		super("Empty BSON object.");
	}
}

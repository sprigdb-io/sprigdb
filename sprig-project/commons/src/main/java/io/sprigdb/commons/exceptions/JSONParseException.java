package io.sprigdb.commons.exceptions;

public class JSONParseException extends RuntimeException {

	private static final long serialVersionUID = -2142737268191172140L;

	public JSONParseException(String msg) {
		super(msg);
	}

	public JSONParseException(String msg, Throwable th) {
		super(msg, th);
	}
}

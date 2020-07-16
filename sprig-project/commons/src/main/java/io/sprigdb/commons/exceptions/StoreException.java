package io.sprigdb.commons.exceptions;

public class StoreException extends RuntimeException{

	private static final long serialVersionUID = -3501729919739182314L;

	public StoreException(String message) {
		super(message);
	}
	
	public StoreException(String message, Throwable exception) {
		super(message, exception);
	}
}

package io.sprigdb.commons.bson;

@FunctionalInterface
public interface BSONExtractor<T> {

	public T getValue(BSON obj, int offset);
}
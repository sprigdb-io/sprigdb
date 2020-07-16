package io.sprigdb.commons.serializer;

import java.io.Serializable;

public interface Marshallable extends Serializable{

	public byte[] serialize();

	public void deSerialize(byte[] bytes);
}

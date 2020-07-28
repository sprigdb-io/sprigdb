package io.sprigdb.commons.bson;

public interface KeySubstitutor {

	public BSON getBSONFromKey(String key);

	public byte[] getBytesFromKey(String key);

	public String getKeyWithBSON(BSON bson);

	public String getKeyWithBytes(byte[] b);
}

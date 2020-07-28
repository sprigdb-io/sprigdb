package io.sprigdb.commons.bson;

public class DefaultKeySubstitutor implements KeySubstitutor {

	@Override
	public BSON getBSONFromKey(String key) {
		return new BSON(this.getBytesFromKey(key));
	}

	@Override
	public byte[] getBytesFromKey(String key) {
		return BSONParser.getBytes(key);
	}

	@Override
	public String getKeyWithBSON(BSON bson) {
		return bson.getValue();
	}

	@Override
	public String getKeyWithBytes(byte[] b) {
		return this.getKeyWithBSON(new BSON(b));
	}
}
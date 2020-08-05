package io.sprigdb.commons.bson;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JSONTestFileDefinition {

	boolean exception = false;
	Object value;

	public JSONTestFileDefinition(Object value) {
		this(false, value);
	}

	public JSONTestFileDefinition(String message) {
		this(true, message);
	}
}
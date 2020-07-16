package io.sprigdb.btree.model;

import java.nio.file.Path;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class TreeParameters {

	private Path path;
	private int keySize = -1;
	private int valueSize = -1;
	private long pageSize = 5 * 1024 * 1024l;
	private long fileSize = 2 * 1024 * 1024 * 1024l;
	private boolean readOnly = false;
}

package io.sprigdb.btree.rightgrow;

import java.nio.MappedByteBuffer;

import io.sprigdb.btree.model.TreeParameters;
import io.sprigdb.commons.serializer.ComparableMarshallable;
import io.sprigdb.commons.serializer.Marshallable;
import io.sprigdb.commons.store.IStore;

public class RBTree<K extends ComparableMarshallable<K>, V extends Marshallable> implements IStore<K, V> {

	private TreeParameters params;
	
	private MappedByteBuffer header;
//	private R

	public RBTree(TreeParameters params) {
		
		this.params = params;
	}

	@Override
	public void put(K k, V v) {
		
		if (header == null) {
			openFile();
		}
		
	}
	
	@Override
	public V get(K k) {
		
		if (header == null) {
			openFile();
		}
		
		return null;
	}
	
	@Override
	public void sync() {
		
	}
	
	@Override
	public void close() {
		
	}

	private void openFile() {
		
		try {
			
		}catch (Exception ex) {
			
		}
	}
}

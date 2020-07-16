package io.sprigdb.commons.store;

public interface IStore<K, V> {

	public void put(K k, V v);

	public V get(K k);

	public void sync();

	public void close();
}

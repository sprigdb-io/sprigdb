package io.sprigdb.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class IndexedSet<K> extends ArrayList<K> {

	private static final long serialVersionUID = -754605802754239941L;

	public IndexedSet() {
		super();
	}

	public IndexedSet(int capacity) {
		super(capacity);
	}

	public IndexedSet(Collection<? extends K> c) {
		super(c);
	}

	@Override
	public void add(int index, K element) {

		int i = this.indexOf(element);
		if (i != -1)
			super.remove(i);
		super.add(index, element);
	}

	@Override
	public boolean add(K element) {

		int i = this.indexOf(element);
		if (i != -1)
			return false;
		return super.add(element);
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		return super.addAll(c.stream().filter(e -> !this.contains(e)).collect(Collectors.toList()));
	}

	@Override
	public boolean addAll(int index, Collection<? extends K> c) {
		return super.addAll(index, c.stream().filter(e -> !this.contains(e)).collect(Collectors.toList()));
	}

	@Override
	public K set(int index, K element) {

		int i = this.indexOf(element);
		if (i != -1)
			super.remove(i);

		if (index == this.size()) {
			super.add(element);
			return null;
		}

		return super.set(index, element);
	}
}

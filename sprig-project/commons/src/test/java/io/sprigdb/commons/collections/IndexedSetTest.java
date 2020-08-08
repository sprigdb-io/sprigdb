package io.sprigdb.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class IndexedSetTest {

	@Test
	void testAddAll() {

		IndexedSet<Integer> is = new IndexedSet<>(5);
		is.addAll(List.of(1, 2, 3, 4, 5));
		assertEquals(List.of(1, 2, 3, 4, 5), is);

		is.addAll(List.of(3, 4, 6));
		assertEquals(List.of(1, 2, 3, 4, 5, 6), is);

		is.clear();
		IndexedSet<Integer> is1 = new IndexedSet<>();
		List<Integer> sl = List.of(3, 4, 6);
		assertThrows(IndexOutOfBoundsException.class, () -> is1.addAll(2, sl));
		assertEquals(List.of(), is1);

		is1.addAll(List.of(1, 2, 3));
		is1.addAll(1, List.of(3, 1, 4, 5));
		assertEquals(List.of(1, 4, 5, 2, 3), is1);
	}

	@Test
	void testAdd() {

		IndexedSet<Integer> is = new IndexedSet<>(List.of(1, 2, 3));

		is.add(4);
		assertEquals(List.of(1, 2, 3, 4), is);

		is.add(3);
		assertEquals(List.of(1, 2, 3, 4), is);

		is.add(1, 3);
		assertEquals(List.of(1, 3, 2, 4), is);

		is.add(1, 5);
		assertEquals(List.of(1, 5, 3, 2, 4), is);
	}

	@Test
	void testSet() {

		IndexedSet<Integer> is = new IndexedSet<>(List.of(1, 2, 3));

		is.set(1, 5);
		assertEquals(List.of(1, 5, 3), is);
		
		is.set(1, 3);
		assertEquals(List.of(1, 3), is);
		
		is.set(1, 3);
		assertEquals(List.of(1, 3), is);
	}
}

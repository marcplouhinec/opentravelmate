package org.opentravelmate.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Guava's like immutable list.
 * Note: Guava's library has not been added to this project due to a bug with Dex (Java Heap space exception).
 * 
 * @author Marc Plouhinec
 */
public class ImmutableList<T> implements List<T>{
	
	private final List<T> innerList;
	
	private ImmutableList(List<T> innerList) {
		this.innerList = innerList;
	}

	@Override
	public boolean add(T object) {
		throw new IllegalStateException("Unable to add an element to an immutable list.");
	}

	@Override
	public void add(int location, T object) {
		throw new IllegalStateException("Unable to add an element to an immutable list.");
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new IllegalStateException("Unable to add an element to an immutable list.");
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		throw new IllegalStateException("Unable to add an element to an immutable list.");
	}

	@Override
	public void clear() {
		throw new IllegalStateException("Unable to clear an immutable list.");
	}

	@Override
	public boolean contains(Object object) {
		return innerList.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return innerList.containsAll(arg0);
	}

	@Override
	public T get(int location) {
		return innerList.get(location);
	}

	@Override
	public int indexOf(Object object) {
		return innerList.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		return innerList.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return innerList.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		return innerList.lastIndexOf(object);
	}

	@Override
	public ListIterator<T> listIterator() {
		return innerList.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int location) {
		return innerList.listIterator(location);
	}

	@Override
	public T remove(int location) {
		throw new IllegalStateException("Unable to remove an element from an immutable list.");
	}

	@Override
	public boolean remove(Object object) {throw new IllegalStateException("Unable to remove an element from an immutable list.");
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new IllegalStateException("Unable to remove an element from an immutable list.");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new IllegalStateException("Unable to remove an element from an immutable list.");
	}

	@Override
	public T set(int location, T object) {
		throw new IllegalStateException("Unable to set an element in an immutable list.");
	}

	@Override
	public int size() {
		return innerList.size();
	}

	@Override
	public List<T> subList(int start, int end) {
		return innerList.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		return innerList.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] array) {
		return innerList.toArray(array);
	}
	
	/**
	 * @return new builder
	 */
	public static <T> Builder<T> Builder() {
		return new Builder<T>();
	}
	
	/**
	 * Helper to create an immutable list.
	 */
	public static final class Builder<T> {
		private List<T> listToBuild = new LinkedList<T>();
		
		/**
		 * Add an element to the list.
		 * 
		 * @param object
		 * @return this
		 */
		public Builder<T> add(T object) {
			listToBuild.add(object);
			return this;
		}
		
		/**
		 * Add several elements to the list.
		 * 
		 * @param collection
		 * @return this
		 */
		public Builder<T> addAll(Collection<T> collection) {
			listToBuild.addAll(collection);
			return this;
		}
		
		/**
		 * Add the given element but one.
		 * 
		 * @param collection
		 * @param objectToNotAdd
		 * @return this
		 */
		public Builder<T> addAllBut(Collection<T> collection, T objectToNotAdd) {
			for (T element : collection) {
				if (element != null && !element.equals(objectToNotAdd)) {
					listToBuild.add(element);
				}
			}
			return this;
		}
		
		/**
		 * Build the immutable list.
		 * 
		 * @return immutable list
		 */
		public ImmutableList<T> build() {
			return new ImmutableList<T>(new ArrayList<T>(listToBuild));
		}
	}
}

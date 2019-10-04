package com.ccacic.financemanager.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A List wrapper that blocks modification of the backing List.
 * This effectively creates an unmodifiable List similar to the
 * wrapper provided by Collections with the notable difference
 * that this wrapper does not implement List. The benefit of this
 * is that List methods which modify the List are simply not present
 * in this wrapper, rather than causing them to fail with an
 * UnsupportedOperationException as Collections does. The drawback
 * is this Wrapper is not recognized as even a Collection, and thus
 * some common methods that would not violate the unmodifiability
 * of this List that can be performed with Collections do not work
 * with ReadOnlyList. Some static utility methods are provided to
 * reimplement these methods. Implements most methods from List
 * that do not modify the List
 * @author Cameron Cacic
 *
 * @param <E> the type of data stored in the List
 */
public class ReadOnlyList<E> implements Iterable<E> {
	
	/**
	 * Utility method to replace addAll between Collections
	 * @param collection the Collection to add to
	 * @param list the ReadOnlyList to add all values from
	 */
	public static <E> void addAll(Collection<E> collection, ReadOnlyList<E> list) {
		for (E obj: list) {
			collection.add(obj);
		}
	}

	private final List<E> list;
	
	@SuppressWarnings("unchecked")
	/*
	 * Creates a new ReadOnlyList that copies the passed ReadOnlyList's
	 * contents into a new backing list of the same class
	 * @param list the ReadOnlyList to copy
	 */
	public ReadOnlyList(ReadOnlyList<E> list) {
		
		List<E> tmp;
		try {
			tmp = list.list.getClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			tmp = new ArrayList<>();
		}
		addAll(tmp, list);
		this.list = tmp;
		
	}
	
	/**
	 * Creates a new ReadOnlyList around the passed List
	 * @param list the List to wrap
	 */
	public ReadOnlyList(List<E> list) {
		this.list = list;
	}
	
	/**
	 * Checks if the passed Object is contained in the backing List
	 * @param obj the Object to check for
	 * @return if the Object is contained in the backing List
	 */
	public boolean contains(E obj) {
		return list.contains(obj);
	}
	
	/**
	 * Checks if all the Objects in the passed Collection are
	 * in the backing List
	 * @param c the Collection of Objects to check for
	 * @return if all the Objects in the Collection are contained in
	 * the backing List
	 */
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}
	
	/**
	 * Returns the item at the passed index in the backing List
	 * @param index the index
	 * @return the item at the index in the backing List
	 */
	public E get(int index) {
		return list.get(index);
	}
	
	/**
	 * Finds the first index of the passed Object, or -1 if it is not
	 * present in the backing List
	 * @param obj the Object to search for
	 * @return the first index of the Object, or -1
	 */
	public int indexOf(E obj) {
		return list.indexOf(obj);
	}
	
	/**
	 * Checks if the backing List is empty
	 * @return if the baacking List is empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Returns an Iterator over the elements in the backing List.
	 * The remove method is not allowed as it would break the
	 * unmodifiable property of ReadOnlyList, and thus throws
	 * an UnsupportedOperationException
	 */
	public Iterator<E> iterator() {
		return new Iterator<>() {

			private final Iterator<E> iterator;

			{
				iterator = list.iterator();
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public E next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("ReadOnlyList iterators do not support removal");
			}

		};
	}
	
	/**
	 * Finds the last index of the passed Object in the backing List, or -1
	 * if the Object is not contained in the backing List
	 * @param obj the Object to search for
	 * @return the last index of the Object, or -1
	 */
	public int lastIndexOf(E obj) {
		return list.lastIndexOf(obj);
	}
	
	/**
	 * Wrapper around a ListIterator that throws UnsupportedOperationExceptions
	 * for remove and set since they would modify the backing List
	 * @author Cameron Cacic
	 */
	private class UnmodifiableListIterator implements ListIterator<E> {

		private final ListIterator<E> listIterator;
		
		UnmodifiableListIterator(ListIterator<E> listIterator) {
			this.listIterator = listIterator;
		}
		
		@Override
		public void add(E e) {
			listIterator.add(e);
		}

		@Override
		public boolean hasNext() {
			return listIterator.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return listIterator.hasPrevious();
		}

		@Override
		public E next() {
			return listIterator.next();
		}

		@Override
		public int nextIndex() {
			return listIterator.nextIndex();
		}

		@Override
		public E previous() {
			return listIterator.previous();
		}

		@Override
		public int previousIndex() {
			return listIterator.previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("ReadOnlyList iterators do not support removal");
		}

		@Override
		public void set(E e) {
			throw new UnsupportedOperationException("ReadOnlyList iterators do not support modification");
		}
		
	}
	
	/**
	 * Returns a ListIterator with modification operations disabled through
	 * UnsupportedOperationExceptions over the backing List
	 * @return the ListIterator
	 */
	public ListIterator<E> listIterator() {
		return new UnmodifiableListIterator(list.listIterator());
	}
	
	/**
	 * Returns a ListIterator with modification operations disabled through
	 * UnsupportedOperationExceptions over the backing List, starting at the
	 * passed index
	 * @param index the index to start at
	 * @return the ListIterator
	 */
	public ListIterator<E> listIterator(int index) {
		return new UnmodifiableListIterator(list.listIterator(index));
	}
	
	/**
	 * Returns the size of the backing List
	 * @return the size
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * Returns a new ReadOnlyList wrapped around a new sublist of the
	 * backing List obtained through the subList method, from the
	 * passed fromIndex, inclusive, to the passed toIndex, exclusive
	 * @param fromIndex the index to start the sublist at
	 * @param toIndex the index to end the sublist at (exclusive)
	 * @return the new ReadOnlyList wrapped on the sublist
	 */
	public ReadOnlyList<E> subReadOnlyList(int fromIndex, int toIndex) {
		return new ReadOnlyList<>(list.subList(fromIndex, toIndex));
	}
	
	/**
	 * Returns an array of the items in the backing List through its
	 * toArray method. This array does not back the backing List and
	 * can be modified freely without affecting the ReadOnlyList
	 * @return the backing List as an array
	 */
	public Object[] toArray() {
		return list.toArray();
	}
	
	/**
	 * Returns an array of the items in the backing List through its
	 * toArray method. This array does not back the backing List and
	 * can be modified freely without affecting the ReadOnlyList
	 * @param a the type of the array to return
	 * @return the backing List as an array
	 */
	public <T> T[] toArray(T[] a) {
		//noinspection SuspiciousToArrayCall
		return list.toArray(a);
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
		
}

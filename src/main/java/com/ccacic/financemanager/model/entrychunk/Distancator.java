package com.ccacic.financemanager.model.entrychunk;

import java.util.Comparator;

/**
 * A type of Comparator with an altered method contract for distance between two objects
 * @author Cameron Cacic
 *
 * @param <T> the type of object to compare
 */
public interface Distancator<T> extends Comparator<T> {

	/**
	 * Holds the same contract as compare() within Comparator, except there is the added
	 * dimension of a distance measure between the two objects. In other words, this method
	 * should return both the natural ordering of the objects through the signum of the return
	 * value and the distance between them through the magnitude of the return value
	 * @param o1 the first object
	 * @param o2 the second object
	 * @return the difference between the two objects
	 */
	int compare(T o1, T o2);
	
}

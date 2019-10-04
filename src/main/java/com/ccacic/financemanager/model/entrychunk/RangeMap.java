package com.ccacic.financemanager.model.entrychunk;

import java.util.*;

/**
 * Provides an implementation of Map that uses Ranges as keys. Instead of values being
 * associated with a singular key or a set of keys, values are associated with a Range
 * where any given key that falls within the Range will unlock the associated value.
 * As such, the key type used to define the Range must have natural ordering and
 * implement Comparable. 
 * 
 * Note that Set and Collection operations are currently unsupported (entrySet(),
 * keySet(), values()).
 * 
 * @author Cameron Cacic
 *
 * @param <K> the key type that defines the ranges
 * @param <V> the value stored at each range
 */
public class RangeMap<K extends Comparable<? super K>, V> implements Map<RangeMap.Range<K>, V> {
	
	/**
	 * Represents a range that exists between a high value and a low value.
	 * The values can be anything so long as they match and implement Comparable
	 * @author Cameron Cacic
	 *
	 * @param <K> the type of the Range, extends Comparable
	 */
	public static class Range<K extends Comparable<? super K>> {
		
		private final K low;
		private final K high;
		
		/**
		 * Creates a new Range with the passed low and high
		 * @param low the low value of the Range
		 * @param high the High value of the Range
		 */
		private Range(K low, K high) {
			this.low = low;
			this.high = high;
		}
		
		/**
		 * Returns the low value of the Range
		 * @return the low value
		 */
		K getLow() {
			return low;
		}
		
		/**
		 * Returns the high value of the Range
		 * @return the high value
		 */
		K getHigh() {
			return high;
		}
		
		/**
		 * Checks if the passed value falls within the Range, inclusive
		 * @param test the value to check
		 * @return if the value is within the Range
		 */
		public boolean inRange(K test) {
			int compLow = test.compareTo(low);
			int compHigh = test.compareTo(high);
			return compLow >= 0 && compHigh <= 0;
		}
		
	}
	
	/**
	 * Represents a binary tree node that holds a low Node reference,
	 * a high Node reference, and some data object
	 * @author Cameron Cacic
	 *
	 */
	private class Node {
		
		private K low;
		private K high;
		private V data;
		
		private Node left;
		private Node right;
		
		/**
		 * Creates a new Node with the passed low, high, and data values
		 * @param low the low Node this Node points to
		 * @param high the high Node this Node points to
		 * @param data the data contained by this Node
		 */
		Node(K low, K high, V data) {
			if (low.compareTo(high) > 0  || high.compareTo(low) < 0) {
				throw new IllegalArgumentException("Improper range, low: " + low + " high: " + high);
			}
			this.low = low;
			this.high = high;
			this.data = data;
			this.left = null;
			this.right = null;
		}
		
	}
	
	private Node root;
	private int size;
	
	/**
	 * Creates a new, empty RangeMap
	 */
	public RangeMap() {
		root = null;
		size = 0;
	}
	
	/**
	 * Converts the range given to a String
	 * @param low the lower bound
	 * @param high the upper bound
	 * @return the range as a String
	 */
	private String rangeToString(K low, K high) {
		return low + " <-> " + high;
	}
	
	/**
	 * Compares the given range to the given node's range. A range is less than another range if
	 * it's high bound is strictly less than the other range's low bound, greater than another range
	 * if it's low bound is strictly greater than the other range's high bound, and equal to another
	 * range if their low and high bounds are equal. Intersecting ranges are forbidden and will
	 * throw an IllegalArgumentException
	 * @param low the low bound of the range
	 * @param high the high bound of the range
	 * @param node the node to compare the range to
	 * @return -1, 0, 1 if the range is less than, equal to, or greater than the node's range
	 */
	private int compareToNode(K low, K high, Node node) {
		if (node == null) {
			return 0;
		}
		int lowComp = node.low.compareTo(low);
		int highComp = node.high.compareTo(high);
		if (lowComp == 0 && highComp == 0) {
			return 0;
		}
		int lowToHigh = node.low.compareTo(high);
		int highToLow = node.high.compareTo(low);
		if (highToLow < 0) {
			return 1;
		} else if (highToLow == 0) {
			throw new IllegalArgumentException("Lower bound of range intersects with upper bound of node: "
					+ rangeToString(node.low, node.high) + ", " + rangeToString(low, high));
		} else {
			if (lowToHigh < 0) {
				throw new IllegalArgumentException("Range intersects range of node: "
						+ rangeToString(low, high) + ", " + rangeToString(node.low, node.high));
			} else if (lowToHigh == 0) {
				throw new IllegalArgumentException("Upper bound of range intersects with lower bound of node: "
						+ rangeToString(low, high) + ", " + rangeToString(node.low, node.high));
			} else {
				return -1;
			}
		}
	}
	
	@Override
	public V put(Range<K> key, V value) {
		return put(key.getLow(), key.getHigh(), value);
	}
	
	/**
	 * Adds the data to the map at the given range. Throws an IllegalArgumentExpection
	 * if the range intersects with any ranges currently in the map
	 * @param low the lower bound of the range
	 * @param high the upper bound of the range
	 * @param data the data to add
	 * @return the data previously at that range, or null elsewise
	 */
	public V put(K low, K high, V data) {
		
		V oldData = null;
		if (root == null) {
			root = new Node(low, high, data);
		} else {
			
			Node onNode = root;
			Node newNode = new Node(low, high, data);
			
			boolean searching = true;
			while (searching) {
				int comp = compareToNode(low, high, onNode);
				if (comp == -1) {
					if (onNode.left == null) {
						onNode.left = newNode;
						searching = false;
					} else {
						onNode = onNode.left;
					}
				} else if (comp == 0) {
					oldData = onNode.data;
					onNode.data = data;
					searching = false;
				} else {
					if (onNode.right == null) {
						onNode.right = newNode;
						searching = false;
					} else {
						onNode = onNode.right;
					}
				}
			}
			
		}
		size++;
		return oldData;
	}
	
	/**
	 * Determines if the given key is in the range of the node, and if not if it's
	 * less than the range or greater than the range
	 * @param key the key
	 * @param node the node
	 * @return -1, 0, 1 if the key is less than the range, within the range, or greater
	 * than the range
	 */
	private int inNodeRange(K key, Node node) {
		int compLow = key.compareTo(node.low);
		int compHigh = key.compareTo(node.high);
		if (compLow < 0) {
			return -1;
		} else if (compLow == 0) {
			return 0;
		} else {
			if (compHigh < 0) {
				return 0;
			} else if (compHigh == 0) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	
	/**
	 * Returns the data stored at the range the passed key falls within
	 * @param key the key to search with
	 * @return the data stored at the range, or null if it doesn't exist
	 */
	private V get(K key) {

		Node onNode = root;
		while (true) {
			if (onNode == null) {
				return null;
			}
			int comp = inNodeRange(key, onNode);
			if (comp < 0) {
				onNode = onNode.left;
			} else if (comp == 0) {
				return onNode.data;
			} else {
				onNode = onNode.right;
			}
		}
		
	}
	
	/**
	 * Finds the nearest entry by distance to the given key. Uses natural ordering to determine the
	 * distance, unless a comparator is provided to draw a distance from. If the distances are equal,
	 * then the nearest entry that is less than the given key is returned. If the key maps to a range
	 * already in the map then that entry is returned since its distance to that entry is zero. Only
	 * returns null when the map is empty
	 * @param key the key to search with
	 * @param comparator the comparator to determine distance with (optional, can pass null)
	 * @return the nearest entry to the given key
	 */
	public V getNearestEntry(K key, Comparator<K> comparator) {
		
		if (root == null) {
			return null;
		}
		
		if (comparator == null) {
			comparator = Comparator.naturalOrder();
		}
		
		Stack<Node> nodeTrace = new Stack<>();
		Stack<Boolean> leftChildTrace = new Stack<>();
		Node onNode = root;
		while (true) {
			
			if (onNode == null) {
				
				Node closestLow;
				Node closestHigh;
				onNode = nodeTrace.pop();
				boolean leftChild = leftChildTrace.pop();
				if (leftChild) {
					
					closestHigh = onNode;
					while (leftChild && onNode != root) {
						onNode = nodeTrace.pop();
						leftChild = leftChildTrace.pop();
					}
					if (leftChild) {
						return closestHigh.data;
					}
					closestLow = onNode;
					
				} else {
					
					closestLow = onNode;
					while (!leftChild && onNode != root) {
						onNode = nodeTrace.pop();
						leftChild = leftChildTrace.pop();
					}
					if (!leftChild) {
						return closestLow.data;
					}
					closestHigh = onNode;
					
				}
				
				int distLow = Math.abs(comparator.compare(closestLow.high, key));
				int distHigh = Math.abs(comparator.compare(key, closestHigh.low));
				if (distLow < distHigh) {
					return closestLow.data;
				} else if (distLow > distHigh) {
					return closestHigh.data;
				} else {
					return closestLow.data;
				}
				
			}
			
			int comp = inNodeRange(key, onNode);
			if (comp < 0) {
				nodeTrace.push(onNode);
				leftChildTrace.push(true);
				onNode = onNode.left;
			} else if (comp == 0) {
				return onNode.data;
			} else {
				nodeTrace.push(onNode);
				leftChildTrace.push(false);
				onNode = onNode.right;
			}
			
		}
		
	}
	
	/**
	 * Finds and returns the Node corresponding to the passed key, or null if no Node
	 * exists mapped to that key
	 * @param key the key to search with
	 * @return the Node associated with that key
	 */
	private Node getNode(K key) {
		
		Node onNode = root;
		while (onNode != null) {
			int comp = inNodeRange(key, onNode);
			if (comp < 0) {
				onNode = onNode.left;
			} else if (comp == 0) {
				return onNode;
			} else {
				onNode = onNode.right;
			}
		}
		return null;
		
	}
	
	/**
	 * Finds the range mapped to the passed key
	 * @param key the key to search with
	 * @return the mapped range, or null if no such mapping exists
	 */
	public Range<K> getRange(K key) {
		Node node = getNode(key);
		if (node == null) {
			return null;
		}
		return new Range<>(node.low, node.high);
	}
	
	/**
	 * Expands the range of the mapping at the given old key using the new key. For example,
	 * if the new key falls below the range of the mapping at the old key, the new key will
	 * become the new lower bound of that mapping, and the new range of the mapping will be 
	 * returned. If the new key falls within the range before expansion, then nothing will
	 * change. If expanding the range causes an intersection with another mapping, then the
	 * expansion will be rejected and nothing will change. In that situation, a null range
	 * will be returned
	 * @param newKey the key to expand the range with
	 * @param oldKey the key to find the range with
	 * @return the new range at the key, or null if the expansion failed
	 */
	public Range<K> expandRange(K newKey, K oldKey) {
		
		Node toUpdate = getNode(oldKey);
		if (toUpdate == null) {
			return null;
		}
		
		V data = removeKey(oldKey);
		K newLow;
		K newHigh;
		if (newKey.compareTo(toUpdate.low) <= 0) {
			newLow = newKey;
			newHigh = toUpdate.high;
		} else if (newKey.compareTo(toUpdate.high) >= 0) {
			newLow = toUpdate.low;
			newHigh = newKey;
		} else {
			newLow = toUpdate.low;
			newHigh = toUpdate.high;
		}
		try {
			put(newLow, newHigh, toUpdate.data);
		} catch (IllegalArgumentException e) {
			put(toUpdate.low, toUpdate.high, toUpdate.data);
		}
		
		return new Range<>(newLow, newHigh);
		
	}
	
	/**
	 * Finds and returns the node containing the passed data
	 * @param data the data to search with
	 * @return the node containing the data
	 */
	private Node getNode(V data) {
		
		Stack<Node> stack = new Stack<>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node onNode = stack.pop();
			if (onNode != null) {
				if (Objects.equals(onNode.data, data)) {
					return onNode;
				}
				stack.push(onNode.right);
				stack.push(onNode.left);
			}
		}
		
		return null;
		
	}
	
	/**
	 * Returns true if the value is contained in the map
	 * @param data the data to search for
	 * @return if the value is contained in the map
	 */
	private boolean containsEntry(V data) {
		return getNode(data) != null;
	}
	
	/**
	 * Prepares the given node for deletion from the tree. Returns the node it
	 * should be replaced with, or null if there is no node to replace it with
	 * @param node the node to prepare for removal
	 * @return the node to replace the passed node with
	 */
	private Node removeNode(Node node) {
		if (node.left == null) {
			if (node.right == null) {
				return null;
			} else {
				return node.right;
			}
		} else {
			if (node.right == null) {
				return node.left;
			} else {
				Node prevNode = null;
				Node onNode = node.right;
				while (onNode.left != null) {
					prevNode = onNode;
					onNode = onNode.left;
				}
				if (prevNode == null) {
					onNode.left = node.left;
					return onNode;
				}
				prevNode.left = onNode.right;
				onNode.right = node.right;
				onNode.left = node.left;
				return onNode;
			}
		}
	}
	
	/**
	 * Removes the entry from the map containing the given data
	 * @param data the data to search for and remove
	 * @return true if the data was found and removed
	 */
	public boolean removeEntry(V data) {
		
		Stack<Node> stack = new Stack<>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node onNode = stack.pop();
			Node left = onNode.left;
			Node right = onNode.right;
			if (left != null) {
				if (Objects.equals(left.data, data)) {
					onNode.left = removeNode(left);
					size--;
					return true;
				} else {
					stack.push(left);
				}
			}
			if (right != null) {
				if (Objects.equals(right.data, data)) {
					onNode.right = removeNode(right);
					size--;
					return true;
				} else {
					stack.push(right);
				}
			}
		}
		return false;
		
	}
	
	/**
	 * Removes the entry from the map associated with the given key
	 * @param key the key to search for
	 * @return the value removed, or null if none found
	 */
	private V removeKey(K key) {
		
		if (root == null) {
			return null;
		}
		if (inNodeRange(key, root) == 0) {
			V data = root.data;
			root = removeNode(root);
			size--;
			return data;
		}
		
		Stack<Node> stack = new Stack<>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node onNode = stack.pop();
			int leftComp = onNode.left != null ? inNodeRange(key, onNode.left) : -1;
			int rightComp = onNode.right != null ? inNodeRange(key, onNode.right) : -1;
			if (leftComp == 0) {
				V data = onNode.left.data;
				onNode.left = removeNode(onNode.left);
				size--;
				return data;
			}
			if (rightComp == 0) {
				V data = onNode.right.data;
				onNode.right = removeNode(onNode.right);
				size--;
				return data;
			}
			int onComp = inNodeRange(key, onNode);
			if (onComp < 0 && onNode.left != null) {
				stack.push(onNode.left);
			} else if (onNode.right != null) {
				stack.push(onNode.right);
			}
		}
		
		return null;
		
	}
	
	@Override
	public void clear() {
		root = null;
		size = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		K castedKey = (K) key;
		return get(castedKey) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsValue(Object value) {
		V castedData = (V) value;
		return containsEntry(castedData);
	}

	@Override
	public Set<Entry<Range<K>, V>> entrySet() {
		throw new UnsupportedOperationException("To be implemented");
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		K castedKey = (K) key;
		return get(castedKey);
	}

	@Override
	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public Set<Range<K>> keySet() {
		throw new UnsupportedOperationException("To be implemented");
	}

	@Override
	public void putAll(Map<? extends Range<K>, ? extends V> m) {
		for (Range<K> range: m.keySet()) {
			put(range, m.get(range));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		K castedKey = (K) key;
		return removeKey(castedKey);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		
		Set<V> values = new HashSet<>(size);
		Stack<Node> stack = new Stack<>();
		stack.push(root);
		
		while (!stack.isEmpty()) {
			
			Node onNode = stack.pop();
			values.add(onNode.data);
			if (onNode.left != null) {
				stack.push(onNode.left);
			}
			if (onNode.right != null) {
				stack.push(onNode.right);
			}
			
		}
		
		return values;
		
	}
	
	/**
	 * Returns all the values in the map in range order from low to high. Not
	 * backed by the map; changes in the returned list will not be reflected
	 * in the map and vice versa
	 * @return map values ordered low to high
	 */
	public List<V> orderedValues() {
		return orderedValues(true);
	}
	
	/**
	 * Returns all the values in the map in range order based on the passed boolean. Not
	 * backed by the map; changes in the returned list will not be reflected
	 * in the map and vice versa
	 * @param ascending if the ordering should be ascending
	 * @return map values ordered based on ascending
	 */
	public List<V> orderedValues(boolean ascending) {
		
		List<V> values = new ArrayList<>(size);
		Stack<Node> stack = new Stack<>();
		Set<Node> visited = new HashSet<>();
		if (root != null) {
			stack.push(root);
		}
		
		if (ascending) {
			
			while (!stack.isEmpty()) {
				
				Node onNode = stack.peek();
				if (visited.contains(onNode)) {
					values.add(onNode.data);
					stack.pop();
					if (onNode.right != null) {
						stack.push(onNode.right);
					}
				} else {
					if (onNode.left != null) {
						stack.push(onNode.left);
						visited.add(onNode);
					} else {
						values.add(onNode.data);
						stack.pop();
						if (onNode.right != null) {
							stack.push(onNode.right);
						}
					}
				}
				
			}
			
		} else {
			
			while (!stack.isEmpty()) {
				
				Node onNode = stack.peek();
				if (visited.contains(onNode)) {
					values.add(onNode.data);
					stack.pop();
					if (onNode.left != null) {
						stack.push(onNode.left);
					}
				} else {
					if (onNode.right != null) {
						stack.push(onNode.right);
						visited.add(onNode);
					} else {
						values.add(onNode.data);
						stack.pop();
						if (onNode.left != null) {
							stack.push(onNode.left);
						}
					}
				}
				
			}
			
		}
		
		return values;
		
	}
	
}

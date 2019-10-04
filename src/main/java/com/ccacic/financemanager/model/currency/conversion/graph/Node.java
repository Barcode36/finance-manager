package com.ccacic.financemanager.model.currency.conversion.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Node in a Graph. Made package private so users of
 * Graph don't need to concern themselves with the Node implementation
 * and can concern themselves purely with their own data representation
 * @author Cameron Cacic
 *
 * @param <T> the data type stored at Nodes
 */
class Node<T> {
	
	private static final Map<Object, Node<?>> nodeMap = new HashMap<>();
	
	/**
	 * Method for creating new Nodes. If a Node has already been
	 * created for the passed data then that Node is returned; no
	 * new Node is created
	 * @param data the data to create a Node around
	 * @return the new Node or the existing Node
	 */
	@SuppressWarnings("unchecked")
	static <T> Node<T> getNode(T data) {
		if (nodeMap.containsKey(data)) {

			return (Node<T>) nodeMap.get(data);
		} else {
			return new Node<>(data);
		}
	}

	private final T data;
	private final Set<Edge<T>> edges;
	private final Map<Node<T>, Edge<T>> tailMap;
	
	/**
	 * Creates a new Node with the passed data
	 * @param data the data
	 */
	private Node(T data) {
		this.data = data;
		edges = new HashSet<>();
		tailMap = new HashMap<>();
		nodeMap.put(data, this);
	}
	
	/**
	 * Returns the data stored at this Node
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	
	/**
	 * Adds an Edge to the Node. Throws an IllegalArgumentException if
	 * the Node isn't the head of the passed Edge
	 * @param edge the Edge to add
	 * @throws IllegalArgumentException if the Node isn't the head of the passed Edge
	 */
	public void addEdge(Edge<T> edge) throws IllegalArgumentException {
		if (!edge.getNodeHead().equals(this)) {
			throw new IllegalArgumentException("Passed edge does not have this node as its head");
		}
		edges.add(edge);
		tailMap.put(edge.getNodeTail(), edge);
	}
	
	/**
	 * Gets all the Edges with this Node as their head
	 * @return a Set of Edges
	 */
	public Set<Edge<T>> getEdges() {
		return new HashSet<>(edges);
	}
	
	/**
	 * Gets the Edge with this Node as its head and the
	 * passed Node as its tail, or null if no such Edge
	 * exists
	 * @param tail the tail Node to search for
	 * @return the proper Edge
	 */
	public Edge<T> getEdgeByTail(Node<T> tail) {
		return tailMap.get(tail);
	}
	
	@Override
	public int hashCode() {
		return data != null ? data.hashCode() : 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return data == null;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		Node<T> node = (Node<T>) obj;
		return data.equals(node.getData());
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
	
}

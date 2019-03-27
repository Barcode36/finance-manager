package com.ccacic.financemanager.model.currency.conversion.graph;

/**
 * Represents a directed edge in a graph with a weight
 * @author Cameron Cacic
 *
 * @param <T> the data type of the Nodes in the Edge
 */
public class Edge<T> {

	private double weight;
	private Node<T> head;
	private Node<T> tail;
	
	/**
	 * Creates a new Edge. Package scope since Node is package scope
	 * @param head the head of the Edge
	 * @param tail the tail of the Edge
	 * @param weight the weight of the Edge
	 */
	Edge(Node<T> head, Node<T> tail, double weight) {
		this.head = head;
		this.tail = tail;
		this.weight = weight;
	}
	
	/**
	 * Returns the head
	 * @return the head
	 */
	Node<T> getNodeHead() {
		return head;
	}
	
	/**
	 * Returns the tail
	 * @return the tail
	 */
	Node<T> getNodeTail() {
		return tail;
	}
	
	/**
	 * Returns the head data
	 * @return the head data
	 */
	public T getHead() {
		return head.getData();
	}
	
	/**
	 * Returns the tail data
	 * @return the tail data
	 */
	public T getTail() {
		return tail.getData();
	}
	
	/**
	 * Returns the weight
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Sets the weight
	 * @param weight the new weight
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public int hashCode() {
		return head.hashCode() + tail.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		Edge<T> edge = (Edge<T>) obj;
		return head.equals(edge.getNodeHead()) && tail.equals(edge.getNodeTail());
	}
	
	@Override
	public String toString() {
		return head.getData() + " -( " + weight + " )-> " + tail.getData();
	}
	
}

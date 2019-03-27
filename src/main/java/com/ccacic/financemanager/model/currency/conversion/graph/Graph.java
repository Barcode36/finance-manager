package com.ccacic.financemanager.model.currency.conversion.graph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

/**
 * Graph representation for finding paths and finding the
 * product of the weights along a given path. Uses a modified
 * depth first search that employs a heuristic when determining
 * the order to add children to the queue. The default heuristic
 * is the degree of the node: higher degree nodes will be added
 * to the stack last
 * @author Cameron Cacic
 *
 * @param <T> the data type stored at nodes of the graph
 */
public class Graph<T> {
	
	/**
	 * Functional interface for obtaining a heuristic value
	 * @author Cameron Cacic
	 *
	 * @param <V> the type to obtain a heuristic value from
	 */
	private interface Heuristic<V> {
		
		/**
		 * Gets the heuristic value for the past instance
		 * @param val the passed instance
		 * @return the heuristic value
		 */
		public int getHeuristic(V val);
		
	}
	
	private Set<Node<T>> nodes;
	private Set<Edge<T>> edges;
	private Heuristic<Node<T>> heuristic;
	
	/**
	 * Creates a new, empty Graph with the default heuristic
	 */
	public Graph() {
		nodes = new HashSet<>();
		edges = new HashSet<>();
		heuristic = v -> v.getEdges().size();
	}
	
	/**
	 * Clears the Graph
	 */
	public void clear() {
		nodes.clear();
		edges.clear();
	}
	
	/**
	 * Returns a Set of all the Edges in the Graph
	 * @return a Set of all the Edges
	 */
	public Set<Edge<T>> getEdges() {
		return new HashSet<>(edges);
	}
	
	/**
	 * Adds the specified Edge to the Graph. If the nodes at the
	 * tail or head are not already in the graph then they will
	 * be added as new nodes
	 * @param head the head node of the Edge
	 * @param tail the tail node of the Edge
	 * @param weight the weight of the Edge
	 */
	public void addEdge(T head, T tail, double weight) {
		Node<T> headNode = Node.getNode(head);
		Node<T> tailNode = Node.getNode(tail);
		
		nodes.add(headNode);
		nodes.add(tailNode);
		
		Edge<T> edge = new Edge<>(headNode, tailNode, weight);
		headNode.addEdge(edge);
		edges.add(edge);
	}
	
	/**
	 * Checks if the passed node is in the Graph
	 * @param node the node to check for
	 * @return if the node is in the Graph
	 */
	public boolean containsNode(T node) {
		return nodes.contains(Node.getNode(node));
	}
	
	/**
	 * Checks if the specified Edge is in the Graph. Note that weight
	 * is not considered when checking equality between Edges so it is
	 * not a required parameter here
	 * @param head the head node of the Edge
	 * @param tail the tail node of the Edge
	 * @return if the Edge is in the Graph
	 */
	public boolean containsEdge(T head, T tail) {
		return edges.contains(new Edge<>(Node.getNode(head), Node.getNode(tail), 0.0));
	}
	
	/**
	 * Adds the passed node to the Graph
	 * @param node the node to add
	 * @throws IllegalArgumentException
	 */
	public void addNode(T node) {
		Node<T> newNode = Node.getNode(node);
		nodes.add(newNode);
	}
	
	/**
	 * Finds a path from the passed start node to the passed goal node. The search
	 * algorithm used is a modified depth first search that employs a heuristic
	 * to order the child nodes being added to the queue. The default heuristic used
	 * is such that the child node with the highest degree is added to the queue last.
	 * Throws an IllegalArgumentException if either the start or goal node is not
	 * part of the Graph
	 * @param start the node to start searching at
	 * @param goal the node to end the path at
	 * @return a path between the start and goal, or null if no path is found
	 * @throws IllegalArgumentException
	 */
	public List<Edge<T>> findPath(T start, T goal) throws IllegalArgumentException {
		
		if (!(containsNode(start) && containsNode(goal))) {
			return null;
		}
		
		Set<Node<T>> explored = new HashSet<>();
		Stack<Node<T>> stack = new Stack<>();
		LinkedList<Edge<T>> path = new LinkedList<>();
		Map<Node<T>, Integer> depthMap = new HashMap<>();
		Comparator<Node<T>> comparator = (o1, o2) -> {
			return heuristic.getHeuristic(o1) - heuristic.getHeuristic(o2);
		};
		
		Node<T> goalNode = Node.getNode(goal);
		Node<T> startNode = Node.getNode(start);
		
		if (!nodes.contains(goalNode)) {
			throw new IllegalArgumentException(goal + " is not a node in the graph");
		}
		if (!nodes.contains(startNode)) {
			throw new IllegalArgumentException(start + " is not a node in the graph");
		}
		
		stack.push(startNode);
		depthMap.put(startNode, 0);
		while (!stack.isEmpty()) {
			
			Node<T> onNode = stack.pop();
			int depth = depthMap.get(onNode) + 1;
			explored.add(onNode);
			
			PriorityQueue<Node<T>> priorityQueue = new PriorityQueue<>(comparator);
			for (Edge<T> edge: onNode.getEdges()) {
				if (!explored.contains(edge.getNodeTail())) {
					if (edge.getNodeTail().equals(goalNode)) {
						path.add(edge);
						return path;
					}
					priorityQueue.add(edge.getNodeTail());
					depthMap.put(edge.getNodeTail(), depth);
				}
			}
			
			if (!priorityQueue.isEmpty()) {
				while (!priorityQueue.isEmpty()) {
					stack.push(priorityQueue.poll());
				}
				path.add(onNode.getEdgeByTail(stack.peek()));
			} else {
				if (!stack.isEmpty()) {
					int reboundDepth = depthMap.get(stack.peek());
					path.subList(reboundDepth, path.size()).clear();
					Edge<T> reboundEdge = path.removeLast();
					reboundEdge = reboundEdge.getNodeHead().getEdgeByTail(stack.peek());
					path.addLast(reboundEdge);
				}
			}
			
		}
		return null;
		
	}
	
	/**
	 * Finds a path between the passed start node and passed goal node,
	 * then takes the product of all the weights of the edges in the path.
	 * Returns that product if a path is found, or 0.0 if no path is found.
	 * Throws an IllegalArgumentException if either the start or goal node
	 * is not present in the Graph
	 * @param start the starting node
	 * @param goal the goal node
	 * @return the product of the weights of the edges along the path
	 * between the start and goal node, or 0.0 if no path is found
	 * @throws IllegalArgumentException
	 */
	public double pathProduct(T start, T goal) throws IllegalArgumentException {
		List<Edge<T>> path = findPath(start, goal);
		if (path == null) {
			return 0.0;
		}
		double product = 1.0;
		for (Edge<T> edge: path) {
			product *= edge.getWeight();
		}
		return product;
	}
	
	@Override
	public String toString() {
		String str = "";
		/*for (Node<T> node: nodes) {
			str += node + "\n";
			for (Edge<T> edge: node.getEdges()) {
				str += "\t" + edge + "\n";
			}
		}
		return str;*/
		for (Edge<T> edge: edges) {
			str += edge + "\n";
		}
		return str;
	}

}

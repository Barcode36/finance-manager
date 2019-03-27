package com.ccacic.assetexchangewrapper.core.api;

import java.util.Comparator;

/**
 * Represents an order on an exchange
 * @author Cameron Cacic
 *
 */
public interface Order {

	/**
	 * Returns the quantity of the Order
	 * @return the quantity
	 */
	double getQuantity();
	
	/**
	 * Returns the price of the Order
	 * @return the price
	 */
	double getPrice();
	
	/**
	 * Returns the ID of the Order
	 * @return the ID
	 */
	String getID();

	/**
	 * Comparator for sorting Orders by quantity
	 */
	static final Comparator<Order> quantitySort = new Comparator<Order>() {
		
		@Override
		public int compare(Order o1, Order o2) {
			if (o1 == null && o2 == null) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			double diff = o1.getQuantity() - o2.getQuantity();
			if (diff < 0) {
				return -1;
			}
			if (diff > 0) {
				return 1;
			}
			return 0;
		}
		
	};
	
	/**
	 * Comparator for sorting Orders by price
	 */
	static final Comparator<Order> priceSort = new Comparator<Order>() {
		
		@Override
		public int compare(Order o1, Order o2) {
			if (o1 == null && o2 == null) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			double diff = o1.getPrice() - o2.getPrice();
			if (diff < 0) {
				return -1;
			}
			if (diff > 0) {
				return 1;
			}
			return 0;
		}
		
	};
}

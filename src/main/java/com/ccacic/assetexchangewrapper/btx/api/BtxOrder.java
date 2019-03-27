package com.ccacic.assetexchangewrapper.btx.api;

import com.ccacic.assetexchangewrapper.core.api.Order;

/**
 * An Order for a BtxMarket
 * @author Cameron Cacic
 *
 */
class BtxOrder implements Order {
	
	private double quantity;
	private double price;
	private String uuid;

	/**
	 * Creates a new BtxOrder
	 * @param quantity the quantity of the order
	 * @param price the price of the order
	 * @param uuid the ID of the order
	 */
	public BtxOrder(double quantity, double price, String uuid) {
		this.quantity = quantity;
		this.price = price;
		this.uuid = uuid;
	}

	@Override
	public double getQuantity() {
		return quantity;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public String getID() {
		return uuid;
	}
	
	@Override
	public String toString() {
		return "Quantity: " + quantity + " Price: " + price + " ID: " + uuid;
	}

}

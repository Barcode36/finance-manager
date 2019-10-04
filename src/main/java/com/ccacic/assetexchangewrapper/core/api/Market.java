package com.ccacic.assetexchangewrapper.core.api;

import java.util.List;

import com.ccacic.assetexchangewrapper.core.Bidding;

/**
 * Methods relevant to interacting with a market that allows placing orders
 * @author Cameron Cacic
 *
 */
public interface Market extends ReadOnlyMarket, Bidding {
	
	/**
	 * Gets the minimum trade size
	 * @return the minimum trade size
	 */
	double getMinTradeSize();
	
	/**
	 * Fill the passed Order as a buy
	 * @param order the Order to fill
	 * @return the Order tracking the fill order
     */
	Order fillBuyOrder(Order order);
	
	/**
	 * Fill the passed Order as a sell
	 * @param order the Order to fill
	 * @return the Order tracking the fill order
     */
	Order fillSellOrder(Order order);
	
	/**
	 * Create and execute an ask at the passed price and quantity
	 * @param price the price to ask
	 * @param quantity the quantity to ask
	 * @return an Order tracking the ask
     */
	Order ask(double price, double quantity);
	
	/**
	 * Create an execute a bid at the passed price and quantity
	 * @param price the price to bid
	 * @param quantity the quantity to bid
	 * @return an Order tracking the bid
     */
	Order bid(double price, double quantity);
	
	/**
	 * Attempts to cancel the passed Order
	 * @param order the Order to cancel
	 * @return if the cancel succeeded
     */
	boolean cancel(Order order);
	
	/**
	 * Gets all the current buy orders in the market
	 * @return a List of buy Orders
     */
	List<Order> getBuyOrders();
	
	/**
	 * Gets all the current sell orders in the market
	 * @return List of sell Orders
     */
	List<Order> getSellOrders();
	
}

package com.ccacic.assetexchangewrapper.core.api;

import java.io.IOException;
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
	 * @throws IOException
	 */
	Order fillBuyOrder(Order order) throws IOException;
	
	/**
	 * Fill the passed Order as a sell
	 * @param order the Order to fill
	 * @return the Order tracking the fill order
	 * @throws IOException
	 */
	Order fillSellOrder(Order order) throws IOException;
	
	/**
	 * Create and execute an ask at the passed price and quantity
	 * @param price the price to ask
	 * @param quantity the quantity to ask
	 * @return an Order tracking the ask
	 * @throws IOException
	 */
	Order ask(double price, double quantity) throws IOException;
	
	/**
	 * Create an execute a bid at the passed price and quantity
	 * @param price the price to bid
	 * @param quantity the quantity to bid
	 * @return an Order tracking the bid
	 * @throws IOException
	 */
	Order bid(double price, double quantity) throws IOException;
	
	/**
	 * Attempts to cancel the passed Order
	 * @param order the Order to cancel
	 * @return if the cancel succeeded
	 * @throws IOException
	 */
	boolean cancel(Order order) throws IOException;
	
	/**
	 * Gets all the current buy orders in the market
	 * @return a List of buy Orders
	 * @throws IOException
	 */
	List<Order> getBuyOrders() throws IOException;
	
	/**
	 * Gets all the current sell orders in the market
	 * @returna List of sell Orders
	 * @throws IOException
	 */
	List<Order> getSellOrders() throws IOException;
	
}

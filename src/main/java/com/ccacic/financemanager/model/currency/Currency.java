package com.ccacic.financemanager.model.currency;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.config.GeneralConfig;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Represents various metrics of value, typically refered to as currencies.
 * It is not limited exclusively to official currencies, however, as any
 * value metric can be represented as a Currency. This includes things like
 * stocks, which are not currencies in the traditional sense but are undoubtedly
 * a value metric, since goods and services could be priced in terms of shares
 * of a stock if the price of the stock in dollars is known. To put it simply,
 * if it represents a value that can be converted to represent another Currency,
 * then it is also a Currency. The details of converting between Currencies,
 * however, is actually external to a single Currency in isolation, and thus is
 * deferred to other classes within the currency package. The Currency class is
 * primarily responsible for enabling that deferral and for knowing how to
 * format a number into a String to be displayed to the user
 * @author Cameron Cacic
 *
 */
public class Currency {
	
	private static final Map<String, Currency> currencies = new HashMap<>();
	
	private static final String NAME = "name";
	private static final String SYMBOL = "symbol";
	private static final String TAGS = "tags";
	private static final String LEFT_SIDE = "left_side";
	private static final String SPACE = "space";
	private static final String DECIMAL_PLACES = "decimal_places";
	
	/**
	 * Creates a new Currency for the passed stock ticker. Due to the thousands
	 * of fluctuating stock tickers in the world, it is far simpler to 
	 * create a temporary new Currency for every newly encountered ticker
	 * than to try to compile and maintain a list of them. Follows a default
	 * stock formating scheme. Will always create a new Currency based on
	 * the passed ticker, regardless of if it exists in the real world
	 * @param ticker the ticker to create a new Currency for
	 * @return the newly created Currency
	 */
	public static Currency createNewStockCurrency(String ticker) {
		if (currencies.keySet().contains(ticker)) {
			return currencies.get(ticker);
		}
		ParamMap paramMap = new ParamMap();
		paramMap.put(SYMBOL, ticker);
		paramMap.put(NAME, ticker);
		paramMap.put(TAGS, new Tag[] {Tag.STOCK});
		paramMap.put(LEFT_SIDE, false + "");
		paramMap.put(SPACE, true + "");
		paramMap.put(DECIMAL_PLACES, 3 + "");
		Currency newStockCurr = new Currency(paramMap);
		currencies.put(ticker, newStockCurr);
		return newStockCurr;
	}
	
	/**
	 * Checks if the passed code matches a known, valid Currency.
	 * Codes that are valid but yet unknown to Currency return false
	 * @param currCode the code to check with
	 * @return if the code is known and valid
	 */
	public static boolean isValidCurrencyCode(String currCode) {
		return currencies.containsKey(currCode);
	}
	
	/**
	 * Gets a Currency instance that corresponds to the passed code.
	 * Throws an InvalidCurrencyCodeException if the passed code does
	 * not correspond to any known Currency
	 * @param currCode the code
	 * @return a Currency instance corresponding to the passed code
	 * @throws InvalidCurrencyCodeException if the passed code doesn't map to a Currency
	 */
	public static Currency getCurrency(String currCode) throws InvalidCurrencyCodeException {
		if (currencies.containsKey(currCode)) {
			return currencies.get(currCode);
		} else {
			throw new InvalidCurrencyCodeException(currCode + " is not recognized as a valid currency");
		}
	}
	
	/**
	 * Gets all the Currencies associated with the passed Tag
	 * @param filter the Tag to filter with
	 * @return a Set of Currencies
	 */
	public static Set<Currency> getAllCurrencies(Tag filter) {
		Set<Currency> set = new HashSet<>();
		for (Currency currency: currencies.values()) {
			if (currency.getTags().contains(filter)) {
				set.add(currency);
			}
		}
		return set;
	}
	
	/**
	 * Gets all the Currencies known to Currency
	 * @return a Set of Currencies
	 */
	public static Set<Currency> getAllCurrencies() {
		return new HashSet<>(currencies.values());
	}
	
	/**
	 * Gets all the codes that correspond to known Currencies
	 * @return a Set of codes
	 */
	public static Set<String> getAllCurrencyCodes() {
		return new HashSet<>(currencies.keySet());
	}
	
	/**
	 * Returns the default Currency for the current system
	 * @return the default Currency
	 */
	public static Currency getDefaultCurrency() {
		GeneralConfig genCon = GeneralConfig.getInstance();
		return currencies.get(genCon.getValue(GeneralConfig.DEFAULT_CURR));
	}
	
	/**
	 * Removes all formating from the passed String, leaving
	 * behind only the characters that relate to numerical value
	 * @param amnt the String to deformat
	 * @return the deformated String
	 */
	public static String deformat(String amnt) {
		if (amnt.length() == 0) {
			return "";
		}
		int frontIndex = 0;
		boolean isNegative = false;
		while (frontIndex < amnt.length() && !Character.isDigit(amnt.charAt(frontIndex))) {
			if (amnt.charAt(frontIndex) == '-') {
				isNegative = true;
			}
			frontIndex++;
		}
		if (frontIndex == amnt.length()) {
			return isNegative ? "-0" : "0";
		}
		int backIndex = amnt.length() - 1;
		while (!Character.isDigit(amnt.charAt(backIndex))) {
			if (amnt.charAt(backIndex) == '-') {
				isNegative = !isNegative; //allows typing '-' to toggle negativity
			}
			backIndex--;
		}
		String[] commaSplit = amnt.substring(frontIndex, backIndex + 1).split(",");
		StringBuilder reassembledBuilder = new StringBuilder();
		for (String s: commaSplit) {
			reassembledBuilder.append(s);
		}
		String reassembled = reassembledBuilder.toString();
		return isNegative ? "-" + reassembled : reassembled;
	}
	
	private final String code;
	private final String symbol;
	private final Set<Tag> tags;
	private final boolean leftSide;
	private final boolean space;
	private final int decPlaces;
	
	/**
	 * Creates a new Currency using the passed ParamMap to source parameters
	 * @param paramMap the ParamMap to source parameters from
	 */
	public Currency(ParamMap paramMap) {
		this.code = paramMap.get(NAME);
		this.symbol = paramMap.get(SYMBOL);
		this.tags = paramMap.getAsSet(TAGS, Tag::getTagByID);
		this.leftSide = paramMap.getAsBoolean(LEFT_SIDE);
		this.space = paramMap.getAsBoolean(SPACE);
		this.decPlaces = paramMap.getAsInt(DECIMAL_PLACES);
		
		currencies.put(this.code, this);
	}
	
	/**
	 * Returns the code of the Currency
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Gets the symbol of the Currency
	 * @return the symbol
	 */
	public String getSymbol() {
		String sym;
		if (symbol.equals(code)) {
			sym = symbol;
		} else {
			try {
				sym = new String(Character.toChars(Integer.parseInt(symbol, 16)));
			} catch (NumberFormatException e) {
				sym = symbol;
			}
		}
		
		if (this != getDefaultCurrency() && sym.equals(getDefaultCurrency().getSymbol())) {
			return getCode() + " " + sym;
		}
		return sym;
	}
	
	/**
	 * Returns the Tags associated with this Currency
	 * @return the associated Tags
	 */
	private Set<Tag> getTags() {
		return tags;
	}
	
	/**
	 * Encodes the Currency into a ParamMap
	 * @return the ParamMap representation of the Currency
	 */
	public ParamMap encode() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(LEFT_SIDE, leftSide + "");
		paramMap.put(NAME, code);
		paramMap.put(TAGS, tags);
		paramMap.put(SPACE, space + "");
		paramMap.put(SYMBOL, symbol);
		paramMap.put(DECIMAL_PLACES, decPlaces + "");
		return paramMap;
	}
	
	/**
	 * Returns the inputed value converted from the inputed currency to this currency,
	 * using the most recently fetched conversion rate between the two currencies. If
	 * no exchange rate between the two currencies can be obtained, then the value is
	 * returned unmodified.
	 * 
	 * Exmaple: if this currency is USD and a call is made with currency EUR and value
	 * 1.00, then 1.24 will be returned (demonstrating that the Euro to US dollar
	 * conversion rate is $1.24 for every Euro)
	 * @param curr the currency to convert from
	 * @param value the value to apply the conversion to
	 * @return the converted value
	 */
	public double convertCurrency(Currency curr, double value) {
		CurrencyExchangeFactory factory = CurrencyExchangeFactory.getInstance();
		return factory.getCurrencyConversionRate(curr, this) * value;
	}
	
	/**
	 * Returns the inputed value converted from the inputed currency to this currency,
	 * using the most recently fetched conversion rate between the two currencies from
	 * the provided exchange. If no such conversion exists on that exchange, or the
	 * exchange itself is down or doesn't exist, then an exchange or sequence of
	 * exchanges that can provide the requested conversion will be used. If no path to
	 * conversion can be found, then value is returned unmodified.
	 * 
	 * Exmaple: if this currency is USD and a call is made with currency EUR and value
	 * 1.00, then 1.24 will be returned (demonstrating that the Euro to US dollar
	 * conversion rate is $1.24 for every Euro)
	 * @param curr the currency to convert from
	 * @param value the value to apply the conversion to
	 * @param exchangeID the exchange to use for the value
	 * @return the converted value
	 */
	public double convertCurrency(Currency curr, double value, String exchangeID) {
		CurrencyExchangeFactory factory = CurrencyExchangeFactory.getInstance();
		return factory.getCurrencyConversionRate(curr, this, exchangeID) * value;
	}
	
	/**
	 * Formats the passed double with the Currency's format
	 * @param amnt the double to format
	 * @return the formated double
	 */
	public String format(double amnt) {
		DecimalFormat df = new DecimalFormat("#,##0." + "0".repeat(Math.max(0, decPlaces)));
		if (!leftSide) {
			return df.format(amnt) + (space ? " " : "") + getSymbol();
		} else {
			if (Double.doubleToRawLongBits(amnt) < 0) {
				amnt = -amnt;
				try {
					return "-" + getSymbol()
						+ (space ? " " : "") + df.format(amnt);
				} catch (NumberFormatException e) {
					return "-" + symbol + (space ? " " : "") + df.format(amnt);
				}
			} else {
				try {
					return getSymbol()
						+ (space ? " " : "") + df.format(amnt);
				} catch (NumberFormatException e) {
					return symbol + (space ? " " : "") + df.format(amnt);
				}
			}
		}
	}
	
	/**
	 * Formats the passed String with the Currency's format. Must be parsable
	 * to a double through Double.parseDouble
	 * @param amnt the double parsable String to format
	 * @return the formated String
	 */
	private String format(String amnt) {
		if (amnt.length() == 0) {
			return format(0);
		}
		return format(Double.parseDouble(deformat(amnt)));
	}
	
	/**
	 * Performs formatting on a String as it changes, using the passed old value as
	 * reference for how to incorporate the changes present in the passed new value
	 * into the proper format. Will throw a NumberFormatException if the passed new
	 * value does not contain any numbers, with a few edge case exceptions
	 * @param oldValue the old value of the String
	 * @param newValue the new value of the String
	 * @return the properly formatted version of newValue
	 * @throws NumberFormatException if the passed values do not contain any numbers
	 */
	public String liveFormat(String oldValue, String newValue) throws NumberFormatException {
		String deformOld = deformat(oldValue);
		String deformNew = deformat(newValue);
		if (deformOld.length() == 0) {
			return format(0);
		}
		if (deformOld.length() - deformNew.length() > 1) {
			return liveFormat("", newValue);
		}
		if (deformNew.equals("-")) {
			return format("-0");
		}
		String[] oldSplitStr = deformOld.split("\\.");
		String[] newSplitStr = deformNew.split("\\.");
		if (oldSplitStr.length == 1) {
			if (deformNew.length() == 1) {
				return format(Double.parseDouble("0." + "0".repeat(Math.max(0, decPlaces - 1)) + deformNew));
			}
		}
		if (oldSplitStr.length == 2 && newSplitStr.length == 2) {
			int leftDelta = newSplitStr[0].length() - oldSplitStr[0].length();
			int rightDelta = newSplitStr[1].length() - oldSplitStr[1].length();
			if (leftDelta != 0) {
				return format(Double.parseDouble(deformNew));
			}
			if (rightDelta < 0) {
				String shiftedDec = newSplitStr[0].substring(0, newSplitStr[0].length() + rightDelta) + "." 
					+ newSplitStr[0].substring(newSplitStr[0].length() + rightDelta) + newSplitStr[1];
				return format(Double.parseDouble(shiftedDec));
			}
			if (rightDelta > 0) {
				String shiftedDec = newSplitStr[0] + newSplitStr[1].substring(0, rightDelta) + "." + newSplitStr[1].substring(rightDelta);
				return format(Double.parseDouble(shiftedDec));
			}
		}
		return format(Double.parseDouble(deformNew));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Currency) {
			Currency curr = (Currency) obj;
			return this.code.equals(curr.code);
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return getCode();
	}
}

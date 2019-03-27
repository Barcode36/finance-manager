package com.ccacic.financemanager.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccacic.financemanager.exception.InvalidCurrencyCodeException;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.util.StringProcessing;

/**
 * A wrapper class around a String to String Map providing
 * utilities to facilitate representation of Objects as Strings.
 * These utilities include conversion of ParamMaps to and from
 * Strings in their entirety, a reserved key for class types,
 * and data getter methods that wrap the conversion of Strings to
 * various common types, such as ints and doubles
 * @author Cameron Cacic
 *
 */
public final class ParamMap {
	
	/**
	 * The reserved key for the type field. Data values can
	 * be stored at this key but they will be returned through
	 * the getType method
	 */
	public static final String TYPE_KEY = "class";
	
	/**
	 * Decodes the passed String into a ParamMap, assuming
	 * the representation is wrapped in curly brackets. This
	 * can be reversed using the instance encode method in
	 * the returned ParamMap, returning a String that may not
	 * be absolutely equal to the passed String but functionaly
	 * equivalent in that decode will create the exact same ParamMap
	 * from it each time
	 * @param str the String to decode
	 * @return the decoded String as a ParamMap
	 */
	public static ParamMap decode(String str) {
		return decode(str, true);
	}
	
	/**
	 * Decodes the passed String into a ParamMap. This 
	 * @param str the String to decode
	 * @param bracketed if the String is wrapped in curly brackets
	 * @return the decoded String as a ParamMap
	 */
	public static ParamMap decode(String str, boolean bracketed) {
		ParamMap paramMap = new ParamMap();
		if (str == null || str.indexOf('=') < 0) {
			return paramMap;
		}
		String pulled = bracketed ? StringProcessing.pullBracketSection(str, 0) : str;
		List<String> pairs = new ArrayList<>();
		
		int startIndex = 0;
		int endIndex = pulled.indexOf(',');
		while (endIndex > startIndex) {
			int brackIndex = pulled.indexOf('{', startIndex);
			String pair = "";
			if (brackIndex > startIndex && brackIndex < endIndex) {
				String section = StringProcessing.pullBracketSection(pulled, startIndex);
				pair = pulled.substring(startIndex, brackIndex) + "{" + section + "}";
				startIndex += pair.length() + 1;
			} else {
				pair = pulled.substring(startIndex, endIndex);
				startIndex = endIndex + 1;
			}
			pairs.add(pair);
			endIndex = pulled.indexOf(',', startIndex);
		}
		if (startIndex < pulled.length()) {
			pairs.add(pulled.substring(startIndex));
		}

		for (String pair: pairs) {
			String[] equalSplit = pair.split("=", 2);
			paramMap.put(equalSplit[0].trim(), equalSplit[1].trim());
		}
		return paramMap;
	}
	
	/**
	 * Decodes the passed String into a List of ParamMaps. ParamMap
	 * representations inside the List encoding should be wrapped in
	 * curly brackets, such that the format matches:
	 * "{~ParamMap~}, {~ParamMap~}"
	 * @param str the String to decode
	 * @return a List of decoded ParamMaps
	 */
	public static List<ParamMap> decodeList(String str) {
		List<ParamMap> list = new ArrayList<>();
		List<String> seperated = StringProcessing.pullBracketSections(str);
		for (String map: seperated) {
			list.add(decode(map, false));
		}
		return list;
	}
	
	/**
	 * A functional interface for converting Strings to instances
	 * of type T
	 * @author Cameron Cacic
	 *
	 * @param <T> the type to convert to
	 */
	public static interface Converter<T> {
		
		/**
		 * Converts the passed String to an instance of type T
		 * @param item the String to convert
		 * @return the converted instance of type T
		 */
		T convert(String item);
		
	}
	
	
	
	public Map<String, String> map;
	
	/**
	 * Creates a new, empty ParamMap
	 */
	public ParamMap() {
		map = new HashMap<>();
	}
	
	/**
	 * Creates a new ParamMap with all the fields
	 * of the passed ParamMap
	 * @param paramMap the ParamMap to pull fields from
	 */
	public ParamMap(ParamMap paramMap) {
		this.map = new HashMap<>();
		this.map.putAll(paramMap.map);
	}
	
	/**
	 * Puts the passed parameter at the passed key
	 * @param key the key to put the parameter at
	 * @param param the parameter to put
	 * @return the old value at the passed key, null if none existed beforehand
	 */
	public String put(String key, String param) {
		if (map.containsKey(key)) {
			Logger.getInstance().logInfo("Duplicate key: " + key + ", value is being overriden");
		}
		return map.put(key, param);
	}
	
	/**
	 * Puts the passed parameter at the passed key, converting it to a String
	 * using its toString method
	 * @param key the key to put the parameter at
	 * @param param the parameter to put
	 * @return the old value at the passed key, null if none existed beforehand
	 */
	public String put(String key, Object param) {
		if (map.containsKey(key)) {
			Logger.getInstance().logInfo("Duplicate key: " + key + ", value is being overriden");
		}
		return map.put(key, param.toString());
	}
	
	/**
	 * Puts all the fields in the passed ParamMap into this ParamMap
	 * @param paramMap the ParamMap to pull fields from
	 */
	public void putAll(ParamMap paramMap) {
		for (String key: paramMap.keySet()) {
			put(key, paramMap.get(key));
		}
	}
	
	/**
	 * Puts all the passed Objects in the passed array as a List
	 * representation at the passed key, converting the Objects
	 * into Strings using toString
	 * @param key the key to put the array at
	 * @param arr the array to put
	 */
	public void put(String key, Object[] arr) {
		put(key, Arrays.asList(arr));
	}
	
	/**
	 * Puts the contents of the passed Iterable as a List
	 * representation at the passed key, converting the Objects
	 * within the Iterable to Strings using toString
	 * @param key the key to put the contents at
	 * @param iter the Iterable to put
	 */
	public void put(String key, Iterable<?> iter) {
		Iterator<?> iterator = iter.iterator();
		String encoded = "{";
		while (iterator.hasNext()) {
			encoded += iterator.next();
			if (iterator.hasNext()) {
				encoded += ",";
			}
		}
		put(key, encoded + "}");
	}
	
	/**
	 * Returns the value stored at the passed key, null if no value
	 * exists at that key
	 * @param key the key to get the value with
	 * @return the value at the passed key
	 */
	public String get(String key) {
		return map.get(key);
	}
	
	/**
	 * Returns the value stored at the passed key as a double, 0.0 if no value
	 * exists at that key
	 * @param key the key to get the double with
	 * @return the double at the passed key
	 */
	public double getAsDouble(String key) {
		try {
			return Double.parseDouble(map.get(key));
		} catch (NumberFormatException | NullPointerException e) {
			return 0.0;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as an int, 0 if no value
	 * exists at that key
	 * @param key the key to get the int with
	 * @return the int at the passed key
	 */
	public int getAsInt(String key) {
		try {
			return Integer.parseInt(map.get(key));
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a long, 0 if no value
	 * exists at that key
	 * @param key the key to get the long with
	 * @return the long at the passed key
	 */
	public long getAsLong(String key) {
		try {
			return Long.parseLong(map.get(key));
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a float, 0.0 if no value
	 * exists at that key
	 * @param key the key to get the float with
	 * @return the float at the passed key
	 */
	public float getAsFloat(String key) {
		try {
			return Float.parseFloat(map.get(key));
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a boolean, false if no value
	 * exists at that key
	 * @param key the key to get the boolean with
	 * @return the boolean at the passed key
	 */
	public boolean getAsBoolean(String key) {
		try {
			return Boolean.parseBoolean(map.get(key));
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a Curreny, null if no value
	 * exists at that key. If the Currency stored at the key is not recognized,
	 * then the default Currency is returned
	 * @param key the key to get the Currency with
	 * @return the Currency at the passed key
	 */
	public Currency getAsCurrency(String key) {
		if (!map.containsKey(key)) {
			return null;
		}
		
		try {
			return Currency.getCurrency(map.get(key));
		} catch (InvalidCurrencyCodeException e) {
			return Currency.getDefaultCurrency();
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a LocalDateTime, null if no value
	 * exists at that key. Returns LocalDateTime.MIN if the value at the key fails to
	 * convert to a LocalDateTime
	 * @param key the key to get the Currency with
	 * @return the LocalDateTime at the passed key
	 */
	public LocalDateTime getAsLocalDateTime(String key) {
		if (!map.containsKey(key)) {
			return null;
		}
		
		try {
			return LocalDateTime.parse(map.get(key));
		} catch (DateTimeParseException e) {
			Logger.getInstance().logException(e);
			return LocalDateTime.MIN;
		}
	}
	
	/**
	 * Returns the value stored at the passed key as a bracketed String,
	 * removing the brackets from it, and null if no value exists at that key. 
	 * @param key the key to get the bracketed String with
	 * @return the bracketed String, unbracketed, at the passed key
	 */
	public String getAsBracketed(String key) {
		return StringProcessing.pullBracketSection(map.get(key), 0);
	}
	
	/**
	 * Returns the value stored at the passed key as a List of ParamMaps.
	 * Assumes the List encoding is bracketed:
	 * {{~ParamMap~}, {~ParamMap~}}
	 * @param key the key to get the List of ParamMaps with
	 * @return the List of ParamMaps at the passed key
	 */
	public List<ParamMap> getAsParamMaps(String key) {
		List<ParamMap> list = new ArrayList<>();
		String stripped = StringProcessing.pullBracketSection(map.get(key), 0);
		List<String> encodedParamMaps = StringProcessing.pullBracketSections(stripped);
		for (String encodedParamMap: encodedParamMaps) {
			list.add(decode(encodedParamMap, false));
		}
		return list;
	}
	
	/**
	 * Returns the value at the passed key as a List of Strings, or null if
	 * no value exists at the passed key
	 * @param key the key to get the List of Strings with
	 * @return the List of Strings at the passed key
	 */
	public List<String> getAsList(String key) {
		return getAsList(key, str -> str);
	}
	
	/**
	 * Returns the value at the passed key as a List of type T, or
	 * null if no value exists at the passed key. Uses the passed
	 * Converter to convert the Strings at the passed key to T objects
	 * @param key the key to get the List of type T with
	 * @param converter the converter to convert Strings to T objects with
	 * @return the List of T objects
	 */
	public <T> List<T> getAsList(String key, Converter<T> converter) {
		String bracketed = getAsBracketed(key);
		if (bracketed == null) {
			return null;
		}
		String[] items = bracketed.split(",");
		List<T> list = new ArrayList<>(items.length);
		for (String item: items) {
			list.add(converter.convert(item));
		}
		return list;
	}
	
	/**
	 * Returns the value at the passed key as a Set of type T, or
	 * null if no value exists at the passed key. Uses the passed
	 * Converter to convert the Strings at the passed key to T objects
	 * @param key the key to get the Set of type T with
	 * @param converter the converter to convert Strings to T objects with
	 * @return the Set of T objects
	 */
	public <T> Set<T> getAsSet(String key, Converter<T> converter) {
		String bracketed = getAsBracketed(key);
		if (bracketed == null) {
			return null;
		}
		String[] items = bracketed.split(",");
		Set<T> set = new HashSet<>(items.length);
		for (String item: items) {
			set.add(converter.convert(item));
		}
		return set;
	}
	
	/**
	 * Removes the value associated with the passed key
	 * @param key the key to find the value to remove with
	 * @return the value that was at the passed key
	 */
	public String remove(String key) {
		return map.remove(key);
	}
	
	/**
	 * Checks if the passed key has a value associated with it
	 * @param key the key to check
	 * @return if the passed key has an associated value
	 */
	public boolean contains(String key) {
		return map.containsKey(key);
	}
	
	/**
	 * Puts the passed type String at the reserved type key
	 * @param type the type to put
	 * @return old type value
	 */
	public String putType(String type) {
		return map.put(TYPE_KEY, type);
	}
	
	/**
	 * Returns the reserved value type
	 * @return the type
	 */
	public String getType() {
		return map.get(TYPE_KEY);
	}
	
	/**
	 * Returns a Set of all the keys with associated values
	 * @return the key set
	 */
	public Set<String> keySet() {
		return map.keySet();
	}
	
	/**
	 * Converts the ParamMap to a String representation. This
	 * can be reversed using the static decode method, creating
	 * a deep copy of this ParamMap
	 * @return a String representing this ParamMap
	 */
	public String encode() {
		return toString();
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}

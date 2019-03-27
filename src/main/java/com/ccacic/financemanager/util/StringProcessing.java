package com.ccacic.financemanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.ParamMap;

/**
 * A utility class for some advanced String manipulation. This class
 * is continually being cut down as its methods find more specialized
 * homes
 * @author Cameron Cacic
 *
 */
public class StringProcessing {
	
	/**
	 * Simple data storage class for storing a section of a String
	 * and the index it ends at in the String it was cut from
	 * @author Cameron Cacic
	 *
	 */
	private static class DetailedBracketSection {
		
		private final String section;
		private final int endCutIndex;
		
		/**
		 * Creates a new DetailedBracketSection
		 * @param section the cut String section
		 * @param endCutIndex the index the cut ends at
		 */
		public DetailedBracketSection(String section, int endCutIndex) {
			this.section = section;
			this.endCutIndex = endCutIndex;
		}
		
	}
	
	/**
	 * Pulls the first bracket section from the passed String starting from the
	 * passed index
	 * @param str the String to pull from
	 * @param beginIndex the index to start at
	 * @return a DetailedBracketSection containing the results
	 */
	private static DetailedBracketSection pullDetailedBracketSection(String str, int beginIndex) {
		if (str == null) {
			return null;
		}
		int brackIndex = str.indexOf("{", beginIndex);
		if (brackIndex == -1) {
			//throw new IllegalArgumentException("Missing opening { from index " + beginIndex + ": " + str);
			Logger.getInstance().logInfo("Missing opening { from index " + beginIndex + ": " + str);
			return new DetailedBracketSection(str, str.length());
		}
		char[] sequence = str.toCharArray();
		int unpaired = 0;
		int endIndex = brackIndex;
		do {
			if (sequence[endIndex] == '{') {
				unpaired++;
			} else if (sequence[endIndex] == '}') {
				unpaired--;
			}
			endIndex++;
		} while (endIndex < sequence.length && unpaired > 0);
		if (unpaired > 0) {
			//throw new IllegalArgumentException("Missing closing }");
			Logger.getInstance().logInfo("Missing closing } after index " + beginIndex + ": " + str);
			return new DetailedBracketSection(str, str.length());
		}
		String section = str.substring(brackIndex + 1, endIndex - 1);
		return new DetailedBracketSection(section, endIndex);
	}
	
	/**
	 * Pulls the first section from the passed String that's enclosed in curly braces,
	 * starting the search from the passed index
	 * @param str the String to pull the section from
	 * @param beginIndex the index to start searching from
	 * @return the first bracketed section
	 */
	public static String pullBracketSection(String str, int beginIndex) {
		DetailedBracketSection result = pullDetailedBracketSection(str, beginIndex);
		if (result == null) {
			return null;
		}
		return result.section;
	}
	
	/**
	 * Pulls all the top level bracket sections from the passed String.
	 * Example: passing the String "{ hello {world} my } first {program}"
	 * returns the List [" hello {world} my ", "program"]
	 * @param str the String to pull from
	 * @return a List of pulled bracket sections
	 */
	public static List<String> pullBracketSections(String str) {
		List<String> sectionList = new ArrayList<>();
		if (str == null) {
			return sectionList;
		}
		int index = 0;
		while (index < str.length()) {
			try {
				DetailedBracketSection pulledSection = pullDetailedBracketSection(str, index);
				index = pulledSection.endCutIndex;
				sectionList.add(pulledSection.section);
			} catch (IllegalArgumentException e) {
				index = Integer.MAX_VALUE;
			}
		}
		return sectionList;
	}
	
	/**
	 * Pulls the arguments from the passed String and returns them in field-value pairs.
	 * Example String to pull from: "field1=hello;field2=world;field3={list,of,values};"
	 * Returned array: [["field1", "field2", "field3"], ["hello", "world", "list,of,values"]]
	 * @param str the String to pull arguments from
	 * @return a 2D array of field-value pairs
	 */
	public static String[][] pullArgs(String str) {
		int startIndex = 0;
		int endIndex = str.indexOf(";");
		int brackIndex = str.indexOf("{");
		List<String> fields = new ArrayList<>();
		List<String> values = new ArrayList<>();
		while (endIndex > -1) {
			fields.add(str.substring(startIndex, str.indexOf('=', startIndex)).trim());
			if (brackIndex > -1 && endIndex > brackIndex) {
				String value = StringProcessing.pullBracketSection(str, startIndex);
				values.add(value.trim());
				startIndex = str.indexOf(';', brackIndex + value.length() + 1) + 1;
				brackIndex = str.indexOf('{', startIndex);
			} else {
				values.add(str.substring(str.indexOf('=', startIndex) + 1, endIndex).trim());
				startIndex = endIndex + 1;
			}
			endIndex = str.indexOf(';', startIndex);
		}
		String[][] rtnArr = new String[2][];
		rtnArr[0] = fields.toArray(new String[0]);
		rtnArr[1] = values.toArray(new String[0]);
		return rtnArr;
	}
	
	/**
	 * Pulls the arguments from the passed String and puts them in a ParamMap
	 * @param str the String to pull from
	 * @return the ParamMap of the arguments
	 */
	public static ParamMap pullParamMap(String str) {
		int startIndex = 0;
		int endIndex = str.indexOf(";");
		int brackIndex = str.indexOf("{");
		ParamMap paramMap = new ParamMap();
		String field = "";
		String value = "";
		while (endIndex > -1) {
			field = str.substring(startIndex, str.indexOf('=', startIndex)).trim();
			if (brackIndex > -1 && endIndex > brackIndex) {
				value = StringProcessing.pullBracketSection(str, startIndex);
				value = value.trim();
				startIndex = str.indexOf(';', brackIndex + value.length() + 1) + 1;
				brackIndex = str.indexOf('{', startIndex);
			} else {
				value = str.substring(str.indexOf('=', startIndex) + 1, endIndex).trim();
				startIndex = endIndex + 1;
			}
			endIndex = str.indexOf(';', startIndex);
			paramMap.put(field, value);
		}
		return paramMap;
	}
	
	/**
	 * Converts the passed String into a List of Strings
	 * @param str the String to convert
	 * @return a List of Strings
	 */
	public static List<String> decodeList(String str) {
		String pulled = pullBracketSection(str, 0);
		return new ArrayList<>(Arrays.asList(pulled.split(",")));
	}
}

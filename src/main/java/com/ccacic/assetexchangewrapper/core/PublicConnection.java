package com.ccacic.assetexchangewrapper.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Simplifies the process of making URL connections and HTTP requests
 * @author Cameron Cacic
 *
 */
public abstract class PublicConnection {
	
	/**
	 * The HTTP request types
	 * @author Cameron Cacic
	 *
	 */
	protected enum RequestType {
		GET,
		POST,
		HEAD,
		OPTION,
		PUT,
		DELETE,
		TRACE
	}
	
	private final String baseUrl;

	/**
	 * Creates a new PublicConnection to communicate on the passed url
	 * @param baseUrl the URL as a String
	 */
	protected PublicConnection(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	/**
	 * Performs an HTTP request and returns the result as a String
	 * @param request the extension to the base URL to perform the request on
	 * @param queries a Map of queries to include in the request; keys are query
	 * types and values are query values
	 * @param requestType the type of the request
	 * @param requestProperties a Map of property names to values to include with
	 * the requests
	 * @return the result of the request as a String
	 * @throws IOException if one occurs while fetching the data
	 */
	protected String executeRequest(String request, Map<String, String> queries, 
			RequestType requestType, Map<String, String> requestProperties
			) throws IOException {
		
		String urlString = baseUrl + request;
		if (queries != null && !queries.isEmpty()) {
			urlString += "?";
			Iterator<String> keyIterator = queries.keySet().iterator();
			StringBuilder urlStringBuilder = new StringBuilder(urlString);
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				urlStringBuilder.append(key).append("=").append(queries.get(key));
				if (keyIterator.hasNext()) {
					urlStringBuilder.append("&");
				}
			}
			urlString = urlStringBuilder.toString();
		}
		URL url = new URL(urlString);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		if (requestProperties != null) {
			for (String key: requestProperties.keySet()) {
				connection.setRequestProperty(key, requestProperties.get(key));
			}
		}
		if (requestType != null) {
			connection.setRequestMethod(requestType.name());
		}
		connection.connect();
		BufferedReader reader = 
				new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
		StringBuilder result = new StringBuilder();
		String input = reader.readLine();
		while (input != null) {
			result.append(input);
			input = reader.readLine();
		}
		
		connection.disconnect();
		reader.close();
		
		return result.toString();
	}
	
	/**
	 * Parses the given String to a JsonObject
	 * @param jString the String to parse
	 * @return the parsed JsonObject
	 */
	protected JsonObject parseJsonString(String jString) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(jString);
		return element.getAsJsonObject();
	}

}

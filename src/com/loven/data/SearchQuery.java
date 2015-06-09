package com.loven.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchQuery {

	private int numItems = 25;
	private long start = 1001;
	private String baseQuery = "http://api.walmartlabs.com/v1/search?format=json";
	private String query;
	private String apikey;
	private long totalItems = 2000;

	public SearchQuery(String query, String apikey) {
		super();
		this.query = query;
		this.apikey = apikey;
	}

	public boolean hasNext() {
		if(totalItems==-1){
			return true;
		}
		return totalItems-start > 0;
	}

	public Optional<JsonObject> next() throws IOException {
		
		String query = buildQuery();
		System.out.println(query);
		URL url = new URL(query);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		JsonParser parser = new JsonParser();
		JsonElement ele = parser.parse(br.readLine());
		conn.disconnect();
		if (ele instanceof JsonObject) {
			JsonObject result = (JsonObject) ele;
			if(totalItems == -1){
				totalItems = result.get("totalResults").getAsLong();
			}
			start = start+result.getAsJsonArray("items").size();
			return Optional.of(result);
		}
		return Optional.empty();
	}

	private String buildQuery() {
		return baseQuery + "&query=" + query + "&start=" + start + "&apikey="
				+ apikey + "&numItems="+numItems;
	}

}

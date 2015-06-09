package com.loven.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TrendingItems implements Runnable {

	private String baseQuery = "http://api.walmartlabs.com/v1/trends?format=json";
	private String apikey;
	private String query;
	private DB db;

	public TrendingItems(String apikey, DB db) {
		this.apikey = apikey;
		this.db = db;
		this.query = buildQuery();
	}

	private String buildQuery() {
		return baseQuery + "&apikey=" + apikey;
	}

	@Override
	public void run() {
		while (true) {
			try {
				URL url = new URL(query);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
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
					JsonArray items = result.getAsJsonArray("items");
					db.updateItems(items);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}

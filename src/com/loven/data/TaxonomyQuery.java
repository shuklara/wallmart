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

public class TaxonomyQuery {

	private String baseQuery = "http://api.walmartlabs.com/v1/taxonomy?format=json";
	private String apikey;

	public TaxonomyQuery(String apikey) {
		super();
		this.apikey = apikey;
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
		StringBuffer buff = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			buff.append(line);
			line = br.readLine();
		}
		JsonParser parser = new JsonParser();
		System.out.println(buff.toString());
		JsonElement ele = parser.parse(buff.toString());
		conn.disconnect();
		if (ele instanceof JsonObject) {
			JsonObject result = (JsonObject) ele;
			return Optional.of(result);
		}
		return Optional.empty();
	}

	private String buildQuery() {
		return baseQuery + "&apikey=" + apikey;
	}

}

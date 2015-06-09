package com.loven.data;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ProductDataPuller {

	private static final String API_KEY = "2m8deby4zfjte77t73e44r2v";

	public static void main(String[] args) throws UnknownHostException {
		DB db = new DB("root", "toor", "wallmart", "ds031681.mongolab.com",
				31681);
		new Thread(new Inventory(db, false)).start();
		// getProduct(db);
		// getTaxonomy(db);
		new Thread(new POSGenerator(db)).start();
		new Thread(new TrendingItems(API_KEY, db)).start();

	}

	public static void getTaxonomy(DB db) {
		TaxonomyQuery query = new TaxonomyQuery(API_KEY);
		try {

			Optional<JsonObject> next = query.next();
			JsonArray categories = next.get().getAsJsonArray("categories");
			db.saveTaxonomy(categories);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getProduct(DB db) {
		SearchQuery searchQuery = new SearchQuery("a+e+i+o+u+", API_KEY);
		try {
			while (searchQuery.hasNext()) {
				Optional<JsonObject> result = searchQuery.next();
				db.saveItems(result.get().getAsJsonArray("items"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

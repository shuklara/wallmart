package com.loven.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class DB {
	private static final String STORE_ID = "WALL709";
	private MongoClient mongoClient;
	private String database;
	private List<Integer> items = Collections.synchronizedList(new ArrayList<>(
			2000));
	private List<String> customers = Collections
			.synchronizedList(new ArrayList<>(500));

	public DB(String userName, String password, String database, String host,
			int port) throws UnknownHostException {
		this.database = database;
		MongoCredential credential = MongoCredential.createCredential(userName,
				database, password.toCharArray());
		mongoClient = new MongoClient(new ServerAddress(host, port),
				Arrays.asList(credential));

		init();
	}

	public List<Integer> getItems() {
		return items;
	}

	private synchronized void init() {
		DBCollection itemsCol = mongoClient.getDB(database).getCollection(
				"items");
		DBCursor cursor = itemsCol
				.find(new BasicDBObjectBuilder().get(),
						new BasicDBObjectBuilder().add("itemId", 1)
								.add("_id", 0).get());

		for (DBObject item : cursor) {
			items.add(Integer.parseInt(item.get("itemId").toString()));
		}

		DBCollection customersCol = mongoClient.getDB(database).getCollection(
				"items");
		cursor = customersCol.find(new BasicDBObjectBuilder().get(),
				new BasicDBObjectBuilder().add("_id", 1).get());

		for (DBObject customer : cursor) {
			customers.add(String.valueOf(customer.get("_id")));
		}
	}

	public synchronized void refresh() {
		init();
	}

	public void saveItems(JsonArray items) {
		DBCollection col = mongoClient.getDB(database).getCollection("items");
		for (JsonElement ele : items) {
			if (ele instanceof JsonObject) {
				DBObject obj = (DBObject) JSON.parse(ele.toString());
				col.insert(obj);
			}
		}
	}

	public void updateItems(JsonArray items) {
		DBCollection col = mongoClient.getDB(database).getCollection("items");
		for (JsonElement ele : items) {
			if (ele instanceof JsonObject) {
				DBObject obj = (DBObject) JSON.parse(ele.toString());
				obj.removeField("addToCartUrl");
				obj.removeField("affiliateAddToCartUrl");
				obj.removeField("productTrackingUrl");
				col.update(
						new BasicDBObjectBuilder().append("itemId",
								obj.get("itemId")).get(), obj, true, false);
			}
		}
		// init();
	}

	public void saveTaxonomy(JsonArray items) {
		DBCollection col = mongoClient.getDB(database).getCollection(
				"categories");
		for (JsonElement ele : items) {
			if (ele instanceof JsonObject) {
				DBObject obj = (DBObject) JSON.parse(ele.toString());
				col.insert(obj);
			}
		}
	}

	public void initCustomers() {
		DBCollection col = mongoClient.getDB(database).getCollection(
				"customers");
		URL resource = getClass().getClassLoader().getResource("customers.csv");
		try (BufferedReader br = new BufferedReader(new FileReader(
				resource.getPath()))) {
			String line = br.readLine();
			String[] headers = line.split(",");
			line = br.readLine();
			while (line != null) {

				BasicDBObjectBuilder builder = new BasicDBObjectBuilder();
				String[] values = line.split(",");
				for (int i = 0; i < headers.length; i++) {
					builder.add(headers[i].replace("\"", ""),
							values[i].replace("\"", ""));
				}
				col.insert(builder.get());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Order getRandomOrder() {
		String customersId = customers.get((int) Math.floor(Math.random()
				* (customers.size() - 1)));
		Order order = new Order(customersId, STORE_ID, PaymentMethod.CARD);

		int numberOfItems = (int) Math.ceil(Math.random() * 10);
		for (int i = 0; i < numberOfItems; i++) {
			Integer itemId = items.get((int) Math.floor(Math.random()
					* (items.size() - 1)));
			order.addItemOrder(getItemPOS(itemId));
		}
		updateInventory(order);
		return order;
	}

	private void updateInventory(Order order) {
		DBCollection inventory = mongoClient.getDB(database).getCollection(
				"inventories");
		for (ItemOrder item : order.getItems()) {
			inventory.update(
					new BasicDBObjectBuilder().append("itemId",
							+item.getItemId()).get(),
					new BasicDBObjectBuilder()
							.append("$inc",
									new BasicDBObjectBuilder().append(
											"availableQuantity",
											-item.getUnit()).get()).get());
			try {
				Inventory.queue.put(item.getItemId());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private ItemOrder getItemPOS(Integer itemId) {
		int units = (int) Math.ceil(Math.random() * 10);

		DBCollection itemsCol = mongoClient.getDB(database).getCollection(
				"items");

		DBObject item = itemsCol.findOne(new BasicDBObjectBuilder().add(
				"itemId", itemId).get());
		return new ItemOrder(itemId, Double.parseDouble(item.get("salePrice")
				.toString()), units);

	}

	public void initInventory() {

		DBCollection inventory = mongoClient.getDB(database).getCollection(
				"inventories");
		for (Integer id : items) {
			int itemId = id;
			int available = (int) Math.round(Math.random() * 100
					* Math.random() * 10);
			int reorderlevel = Math.max(100, (available / 10) * 10);
			int reorderQuantity = 2 * reorderlevel;
			DBObject inv = inventory.find(
					new BasicDBObjectBuilder().append("itemId", id).get())
					.one();
			if (inv == null) {
				inv = new BasicDBObjectBuilder().add("itemId", itemId)
						.add("availableQuantity", available)
						.add("reorderLevel", reorderlevel)
						.add("reorderQuantity", reorderQuantity).get();
				inventory.insert(inv);
			}

		}

	}

	public void checkForReorder(Integer id) {
		DBCollection inventory = mongoClient.getDB(database).getCollection(
				"inventories");
		DBObject idToSearch = new BasicDBObjectBuilder().append("itemId", id)
				.get();
		DBObject inv = inventory.find(idToSearch).one();
		if (Integer.parseInt(inv.get("availableQuantity").toString()) < Integer
				.parseInt(inv.get("reorderLevel").toString())) {
			DBObject reorder = new BasicDBObjectBuilder().append(
					"availableQuantity",
					+Integer.parseInt(inv.get("reorderLevel").toString()))
					.get();
			inventory.update(idToSearch,
					new BasicDBObjectBuilder().append("$inc", reorder).get());
		}
	}
}

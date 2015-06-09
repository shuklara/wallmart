package com.loven.data;

import java.util.ArrayList;
import java.util.List;

public class Order {

	private List<ItemOrder> items = new ArrayList<>();
	private String customerId;
	private String storeId;
	private PaymentMethod payMethod;
	private long date;
	private long time;

	public Order(String customerId, String storeId, PaymentMethod payMethod) {
		this.customerId = customerId;
		this.storeId = storeId;
		this.payMethod = payMethod;
		this.date = System.currentTimeMillis();
		this.time = System.currentTimeMillis();
	}

	public void addItemOrder(ItemOrder itemPOS) {
		getItems().add(itemPOS);

	}

	@Override
	public String toString() {
		String order = date + ";" + time + ";" + storeId + ";" + customerId
				+ ";" + payMethod + ";;";
		for (ItemOrder itemOrder : getItems()) {
			order = order + itemOrder + ";";
		}
		return order;
	}

	public List<ItemOrder> getItems() {
		return items;
	}


}

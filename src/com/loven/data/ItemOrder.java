package com.loven.data;

public class ItemOrder {
	private Integer itemId;
	private double salesPrice;
	private int unit;

	public ItemOrder(Integer itemId, double salesPrice, int unit) {
		this.itemId = itemId;
		this.salesPrice = salesPrice;
		this.unit = unit;
	}
	public Integer getItemId() {
		return itemId;
	}
	public double getSalesPrice() {
		return salesPrice;
	}
	public int getUnit() {
		return unit;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return itemId+","+unit+","+salesPrice;
	}

}

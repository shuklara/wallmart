package com.loven.data;


public class POSGenerator implements Runnable {

	private DB db;

	public POSGenerator(DB db) {
		this.db = db;

	}

	@Override
	public void run() {
		while (true) {
			Order randomOrder = db.getRandomOrder();
			KafkaProducer.send("POS", randomOrder.toString());
		}

	}

}

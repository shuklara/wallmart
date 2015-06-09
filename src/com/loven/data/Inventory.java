package com.loven.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Inventory implements Runnable {
	public static BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
	private DB db;

	public Inventory(DB db, boolean intFromScratch) {
		this.db = db;
		if (intFromScratch)
			db.initInventory();
	}

	@Override
	public void run() {
		while (true) {
			try {
				db.checkForReorder(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}

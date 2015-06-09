package com.loven.data;

public enum PaymentMethod {

	CASH{
		@Override
		public String toString() {
			return 1+"";
		}
	},
	CARD{
		@Override
		public String toString() {
			return 2+"";
		}
	}
}

package com.brailsoft.model;

import com.brailsoft.base.NotificationType;

public enum InventoryItemNotificationType implements NotificationType {
	Add("add"), Changed("changed"), Removed("removed"), Failed("failed");

	private String type;

	InventoryItemNotificationType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}

	@Override
	public String category() {
		return "inventoryitem";
	}
}

package com.brailsoft.model;

import com.brailsoft.base.NotificationType;

public enum PropertyNotificationType implements NotificationType {
	Add("add"), Changed("changed"), Removed("removed"), Failed("failed");

	private String type;

	PropertyNotificationType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}

	@Override
	public String category() {
		return "property";
	}
}

package com.brailsoft.model;

import com.brailsoft.base.NotificationType;

public enum MonitoredItemNotificationType implements NotificationType {
	Add("add"), Changed("changed"), Removed("removed");

	private String type;

	MonitoredItemNotificationType(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}

}
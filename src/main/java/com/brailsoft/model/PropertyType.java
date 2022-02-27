package com.brailsoft.model;

import com.brailsoft.base.AuditType;

public enum PropertyType implements AuditType {
	Added("added"), Changed("changed"), Deleted("deleted");

	private String type;

	PropertyType(String type) {
		this.type = type;
	}

	@Override
	public String type() {
		return type;
	}

}

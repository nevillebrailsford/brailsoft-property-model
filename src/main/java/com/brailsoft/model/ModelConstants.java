package com.brailsoft.model;

import com.brailsoft.storage.StorageConstants;

public interface ModelConstants extends StorageConstants {
	public static final String dateFormatForUI = "dd/MM/uuuu";
	public static final String dateFormatForStorage = "uuuu-MM-dd";

	public static final String PROPERTY_CATEGORY = "property";
	public static final String MONITORED_ITEM_CATEGORY = "monitoreditem";
	public static final String INVENTORY_ITEM_CATEGORY = "inventoryitem";

	public static final String PROPERTY_FILE = "property.dat";
	public static final String MODEL = "model";

}

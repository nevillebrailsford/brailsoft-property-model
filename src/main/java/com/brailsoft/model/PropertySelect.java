package com.brailsoft.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.brailsoft.base.ApplicationConfiguration;

public class PropertySelect {
	private static final String CLASS_NAME = PropertySelect.class.getName();
	private static final Logger LOGGER = ApplicationConfiguration.logger();

	private static final PropertyMonitor monitor = PropertyMonitor.instance();

	public synchronized static List<Property> withOverdueItems() {
		LOGGER.entering(CLASS_NAME, "withOverdueItems");
		List<Property> copyList = monitor.properties().stream().filter(property -> property.areItemsOverdue())
				.collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.exiting(CLASS_NAME, "withOverdueItems", copyList);
		return copyList;
	}

	public synchronized static List<Property> withOverdueNotices() {
		LOGGER.entering(CLASS_NAME, "withOverdueNotices");
		List<Property> copyList = monitor.properties().stream().filter(property -> property.areNoticesOverdue())
				.collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.exiting(CLASS_NAME, "withOverdueNotices", copyList);
		return copyList;
	}

	public synchronized static List<MonitoredItem> monitoredItemsFor(Property property) {
		LOGGER.entering(CLASS_NAME, "monitoredItemsFor", property);
		if (property == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertySelect: property was null");
			LOGGER.throwing(CLASS_NAME, "monitoredItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "monitoredItemsFor");
			throw exc;
		}
		if (monitor.findProperty(property) == null) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertySelect: property " + property + " not found");
			LOGGER.throwing(CLASS_NAME, "monitoredItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "monitoredItemsFor");
			throw exc;
		}
		List<MonitoredItem> copyList = monitor.findProperty(property).monitoredItems().stream()
				.map(item -> new MonitoredItem(item)).collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.exiting(CLASS_NAME, "monitoredItemsFor", copyList);
		return copyList;
	}

	public synchronized List<InventoryItem> inventoryItemsFor(Property property) {
		LOGGER.entering(CLASS_NAME, "inventoryItemsFor", property);
		if (property == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertySelect: property was null");
			LOGGER.throwing(CLASS_NAME, "inventoryItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "inventoryItemsFor");
			throw exc;
		}
		if (monitor.findProperty(property) == null) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertySelect: property " + property + " not found");
			LOGGER.throwing(CLASS_NAME, "inventoryItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "inventoryItemsFor");
			throw exc;
		}
		List<InventoryItem> copyList = monitor.findProperty(property).inventoryItems().stream()
				.map(item -> new InventoryItem(item)).sorted().collect(Collectors.toList());
		LOGGER.exiting(CLASS_NAME, "inventoryItemsFor", copyList);
		return copyList;
	}

	public synchronized static List<MonitoredItem> overdueItemsFor(LocalDate date) {
		LOGGER.entering(CLASS_NAME, "overdueItemsFor", date);
		if (date == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertySelect: date was null");
			LOGGER.throwing(CLASS_NAME, "overdueItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "overdueItemsFor");
			throw exc;
		}
		List<MonitoredItem> overdueList = monitor.getAllItems().stream()
				.filter(item -> item.timeForNextAction().equals(date)).collect(Collectors.toList());
		Collections.sort(overdueList);
		LOGGER.exiting(CLASS_NAME, "overdueItemsFor", overdueList);
		return overdueList;
	}

	public synchronized static List<MonitoredItem> notifiedItemsFor(LocalDate date) {
		LOGGER.entering(CLASS_NAME, "notifiedItemsFor", date);
		if (date == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertySelect: date was null");
			LOGGER.throwing(CLASS_NAME, "notifiedItemsFor", exc);
			LOGGER.exiting(CLASS_NAME, "notifiedItemsFor");
			throw exc;
		}
		List<MonitoredItem> notifiedList = monitor.getAllItems().stream()
				.filter(item -> item.timeForNextNotice().equals(date)).collect(Collectors.toList());
		Collections.sort(notifiedList);
		LOGGER.exiting(CLASS_NAME, "notifiedItemsFor", notifiedList);
		return notifiedList;
	}

	public synchronized static List<InventoryItem> inventoryFor(Property property) {
		LOGGER.entering(CLASS_NAME, "getInventoryFor", property);
		if (property == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "getInventoryFor", exc);
			LOGGER.exiting(CLASS_NAME, "getInventoryFor");
			throw exc;
		}
		if (monitor.findProperty(property) == null) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " not found");
			LOGGER.throwing(CLASS_NAME, "getInventoryFor", exc);
			LOGGER.exiting(CLASS_NAME, "getInventoryFor");
			throw exc;
		}
		List<InventoryItem> copyList = monitor.findProperty(property).inventoryItems().stream()
				.map(item -> new InventoryItem(item)).sorted().collect(Collectors.toList());
		LOGGER.exiting(CLASS_NAME, "getInventoryFor", copyList);
		return copyList;
	}

}

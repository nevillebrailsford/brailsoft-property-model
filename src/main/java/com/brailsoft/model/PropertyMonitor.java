package com.brailsoft.model;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.brailsoft.base.ApplicationConfiguration;
import com.brailsoft.base.AuditService;
import com.brailsoft.base.Notification;
import com.brailsoft.base.NotificationCentre;
import com.brailsoft.storage.Storage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PropertyMonitor {
	private static final String PROPERTY_FILE = "property.dat";
	private static final String MODEL = "model";
	private static final String CLASS_NAME = PropertyMonitor.class.getName();
	private static final Logger LOGGER = ApplicationConfiguration.logger();

	private static PropertyMonitor instance = null;

	private final ObservableList<Property> properties;

	public synchronized static PropertyMonitor instance() {
		LOGGER.entering(CLASS_NAME, "instance");
		if (instance == null) {
			instance = new PropertyMonitor();
		}
		LOGGER.exiting(CLASS_NAME, "instance", instance);
		return instance;
	}

	private PropertyMonitor() {
		properties = FXCollections.observableArrayList();
	}

	public synchronized void clear() {
		LOGGER.entering(CLASS_NAME, "clear");
		properties.clear();
		updateStorage();
		LOGGER.exiting(CLASS_NAME, "clear");
	}

	public synchronized void addProperty(Property newProperty) {
		LOGGER.entering(CLASS_NAME, "addProperty", newProperty);
		if (newProperty == null) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addProperty", exc);
			LOGGER.exiting(CLASS_NAME, "addProperty");
			throw exc;
		}
		if (properties.contains(newProperty)) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + newProperty + " already exists");
			LOGGER.throwing(CLASS_NAME, "addProperty", exc);
			LOGGER.exiting(CLASS_NAME, "addProperty");
			throw exc;
		}
		try {
			properties.add(newProperty);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.Property, newProperty.toString());
			updateStorage();
			Notification notification = new Notification(PropertyNotificationType.Add, this, newProperty);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "removeProperty", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "addProperty");
		}
	}

	public synchronized void removeProperty(Property oldProperty) {
		LOGGER.entering(CLASS_NAME, "removeProperty", oldProperty);
		if (oldProperty == null) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "removeProperty", exc);
			LOGGER.exiting(CLASS_NAME, "removeProperty");
			throw exc;
		}
		if (!properties.contains(oldProperty)) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + oldProperty + " was not known");
			LOGGER.throwing(CLASS_NAME, "removeProperty", exc);
			LOGGER.exiting(CLASS_NAME, "removeProperty");
			throw exc;
		}
		try {
			properties.remove(oldProperty);
			AuditService.writeAuditInformation(PropertyType.Removed, PropertyObject.Property, oldProperty.toString());
			updateStorage();
			Notification notification = new Notification(PropertyNotificationType.Removed, this, oldProperty);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(PropertyNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "removeProperty", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "removeProperty");
		}
	}

	public synchronized void addItem(MonitoredItem monitoredItem) {
		LOGGER.entering(CLASS_NAME, "addItem", monitoredItem);
		if (monitoredItem == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: monitoredItem was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		Property property = null;
		try {
			property = monitoredItem.owner();
		} catch (IllegalArgumentException e) {
			LOGGER.fine("PropertyMonitor: caught exception: " + e.getMessage());
		}
		if (property == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		try {
			findProperty(property).addItem(monitoredItem);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.MonitoredItem,
					monitoredItem.toString());
			updateStorage();
			Notification notification = new Notification(MonitoredItemNotificationType.Add, this, monitoredItem);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "addItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "addItem");
		}
	}

	public synchronized void replaceItem(MonitoredItem monitoredItem) {
		LOGGER.entering(CLASS_NAME, "replaceItem", monitoredItem);
		if (monitoredItem == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: monitoredItem was null");
			LOGGER.throwing(CLASS_NAME, "replaceItem", exc);
			LOGGER.exiting(CLASS_NAME, "replaceItem");
			throw exc;
		}
		Property property = null;
		try {
			property = monitoredItem.owner();
		} catch (IllegalArgumentException e) {
			LOGGER.fine("PropertyMonitor: caught exception: " + e.getMessage());
		}
		if (property == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "replaceItem", exc);
			LOGGER.exiting(CLASS_NAME, "replaceItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "replaceItem", exc);
			LOGGER.exiting(CLASS_NAME, "replaceItem");
			throw exc;
		}
		try {
			findProperty(property).replaceItem(monitoredItem);
			AuditService.writeAuditInformation(PropertyType.Changed, PropertyObject.MonitoredItem,
					monitoredItem.toString());
			updateStorage();
			Notification notification = new Notification(MonitoredItemNotificationType.Changed, this, monitoredItem);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "replaceItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "replaceItem");
		}
	}

	public synchronized void removeItem(MonitoredItem monitoredItem) {
		LOGGER.entering(CLASS_NAME, "removeItem", monitoredItem);
		if (monitoredItem == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: monitoredItem was null");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		Property property = null;
		try {
			property = monitoredItem.owner();
		} catch (IllegalArgumentException e) {
			LOGGER.fine("PropertyMonitor: caught exception: " + e.getMessage());
		}
		if (property == null) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		try {
			findProperty(property).removeItem(monitoredItem);
			AuditService.writeAuditInformation(PropertyType.Removed, PropertyObject.MonitoredItem,
					monitoredItem.toString());
			updateStorage();
			Notification notification = new Notification(MonitoredItemNotificationType.Removed, this, monitoredItem);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(MonitoredItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "removeItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "removeItem");
		}
	}

	public synchronized void addItem(InventoryItem inventoryItem) {
		LOGGER.entering(CLASS_NAME, "addItem", inventoryItem);
		if (inventoryItem == null) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: inventoryItem was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		Property property = null;
		try {
			property = inventoryItem.owner();
		} catch (IllegalArgumentException e) {
			LOGGER.fine("PropertyMonitor: caught exception: " + e.getMessage());
		}
		if (property == null) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		try {
			findProperty(property).addItem(inventoryItem);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.InventoryItem,
					inventoryItem.toString());
			updateStorage();
			Notification notification = new Notification(InventoryItemNotificationType.Add, this, inventoryItem);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "addItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "addItem");
		}
	}

	public synchronized void removeItem(InventoryItem inventoryItem) {
		LOGGER.entering(CLASS_NAME, "removeItem", inventoryItem);
		if (inventoryItem == null) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: inventoryItem was null");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		Property property = null;
		try {
			property = inventoryItem.owner();
		} catch (IllegalArgumentException e) {
			LOGGER.fine("PropertyMonitor: caught exception: " + e.getMessage());
		}
		if (property == null) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "removeItem", exc);
			LOGGER.exiting(CLASS_NAME, "removeItem");
			throw exc;
		}
		try {
			findProperty(property).removeItem(inventoryItem);
			AuditService.writeAuditInformation(PropertyType.Removed, PropertyObject.InventoryItem,
					inventoryItem.toString());
			updateStorage();
			Notification notification = new Notification(InventoryItemNotificationType.Removed, this, inventoryItem);
			NotificationCentre.broadcast(notification);
		} catch (Exception e) {
			Notification notification = new Notification(InventoryItemNotificationType.Failed, this);
			NotificationCentre.broadcast(notification);
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "removeItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "removeItem");
		}
	}

	public synchronized List<Property> properties() {
		LOGGER.entering(CLASS_NAME, "properties");
		List<Property> copyList = properties.stream().map(property -> new Property(property))
				.collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.exiting(CLASS_NAME, "properties", copyList);
		return copyList;
	}

	public synchronized List<MonitoredItem> monitoredItemsFor(Property property) {
		LOGGER.entering(CLASS_NAME, "monitoredItemsFor", property);
		Property p = findProperty(property);
		List<MonitoredItem> copyList = p.monitoredItems().stream().map(item -> new MonitoredItem(item))
				.collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.entering(CLASS_NAME, "monitoredItemsFor", copyList);
		return copyList;
	}

	public synchronized List<InventoryItem> inventoryItemsFor(Property property) {
		LOGGER.entering(CLASS_NAME, "inventoryItemsFor", property);
		Property p = findProperty(property);
		List<InventoryItem> copyList = p.inventoryItems().stream().map(item -> new InventoryItem(item))
				.collect(Collectors.toList());
		Collections.sort(copyList);
		LOGGER.entering(CLASS_NAME, "inventoryItemsFor", copyList);
		return copyList;
	}

	private void updateStorage() {
		LOGGER.entering(CLASS_NAME, "updateStorage");
		PropertyStore propertyStore = new PropertyStore();
		File modelDirectory = obtainModelDirectory();
		File dataFile = new File(modelDirectory, PROPERTY_FILE);
		propertyStore.setFileName(dataFile.getAbsolutePath());
		Storage storage = new Storage();
		storage.storeData(propertyStore);
		LOGGER.exiting(CLASS_NAME, "updateStorage");
	}

	private File obtainModelDirectory() {
		LOGGER.entering(CLASS_NAME, "obtainModelDirectory");
		File rootDirectory = ApplicationConfiguration.rootDirectory();
		File applicationDirectory = new File(rootDirectory,
				ApplicationConfiguration.applicationDecsriptor().applicationName());
		File modelDirectory = new File(applicationDirectory, MODEL);
		if (!modelDirectory.exists()) {
			LOGGER.fine("Model directory " + modelDirectory.getAbsolutePath() + " does not exist");
			if (!modelDirectory.mkdirs()) {
				LOGGER.warning("Unable to create model directory");
				modelDirectory = null;
			} else {
				LOGGER.fine("Created model directory " + modelDirectory.getAbsolutePath());
			}
		} else {
			LOGGER.fine("Model directory " + modelDirectory.getAbsolutePath() + " does exist");
		}
		LOGGER.exiting(CLASS_NAME, "obtainModelDirectory", modelDirectory);
		return modelDirectory;
	}

	synchronized Property findProperty(Property property) {
		LOGGER.entering(CLASS_NAME, "findProperty", property);
		Property found = null;
		for (Property p : properties) {
			if (p.equals(property)) {
				found = p;
				break;
			}
		}
		LOGGER.exiting(CLASS_NAME, "findProperty", found);
		return found;
	}

	synchronized List<MonitoredItem> getAllItems() {
		LOGGER.entering(CLASS_NAME, "getAllItems");
		List<MonitoredItem> allItems = properties().stream().flatMap(property -> property.monitoredItems().stream())
				.collect(Collectors.toList());
		Collections.sort(allItems);
		LOGGER.exiting(CLASS_NAME, "getAllItems", allItems);
		return allItems;
	}
}

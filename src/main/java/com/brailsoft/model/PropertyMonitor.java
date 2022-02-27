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
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addProperty", exc);
			LOGGER.exiting(CLASS_NAME, "addProperty");
			throw exc;
		}
		if (properties.contains(newProperty)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + newProperty + " already exists");
			LOGGER.throwing(CLASS_NAME, "addProperty", exc);
			LOGGER.exiting(CLASS_NAME, "addProperty");
			throw exc;
		}
		try {
			properties.add(newProperty);
			Notification<Property> notification = new Notification<>(PropertyNotificationType.Add, this, newProperty);
			NotificationCentre.broadcast(notification);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.Property, newProperty.toString());
			updateStorage();

		} finally {
			LOGGER.exiting(CLASS_NAME, "addProperty");
		}
	}

	public synchronized void replaceProperty(Property oldProperty, Property newProperty) {
		LOGGER.entering(CLASS_NAME, "replaceProperty", new Object[] { oldProperty, newProperty });
		if (oldProperty == null || newProperty == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "replaceProperty", exc);
			LOGGER.exiting(CLASS_NAME, "replaceProperty");
			throw exc;
		}
		if (!properties.contains(oldProperty)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + oldProperty + " was not known");
			LOGGER.throwing(CLASS_NAME, "replaceProperty", exc);
			LOGGER.exiting(CLASS_NAME, "replaceProperty");
			throw exc;
		}
		if (properties.contains(newProperty)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + newProperty + " already exists");
			LOGGER.throwing(CLASS_NAME, "replaceProperty", exc);
			LOGGER.exiting(CLASS_NAME, "replaceProperty");
			throw exc;
		}
		try {
			properties.remove(oldProperty);
			properties.add(newProperty);
			Notification<PropertyReplacement> notification = new Notification<>(PropertyNotificationType.Changed, this,
					new PropertyReplacement(oldProperty, newProperty));
			NotificationCentre.broadcast(notification);
			AuditService.writeAuditInformation(PropertyType.Changed, PropertyObject.Property, newProperty.toString());
			updateStorage();

		} catch (Exception e) {
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "replaceProperty", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "replaceProperty");
		}
	}

	public synchronized void removeProperty(Property oldProperty) {
		LOGGER.entering(CLASS_NAME, "removeProperty", oldProperty);
		if (oldProperty == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "removeProperty", exc);
			LOGGER.exiting(CLASS_NAME, "removeProperty");
			throw exc;
		}
		if (!properties.contains(oldProperty)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + oldProperty + " was not known");
			LOGGER.throwing(CLASS_NAME, "removeProperty", exc);
			LOGGER.exiting(CLASS_NAME, "removeProperty");
			throw exc;
		}
		try {
			properties.remove(oldProperty);
			Notification<Property> notification = new Notification<>(PropertyNotificationType.Removed, this,
					oldProperty);
			NotificationCentre.broadcast(notification);
			AuditService.writeAuditInformation(PropertyType.Deleted, PropertyObject.Property, oldProperty.toString());
			updateStorage();
		} catch (Exception e) {
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "removeProperty", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "removeProperty");
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

	public synchronized void addItem(MonitoredItem monitoredItem) {
		LOGGER.entering(CLASS_NAME, "addItem", monitoredItem);
		if (monitoredItem == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: monitoredItem was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		Property property = monitoredItem.owner();
		if (property == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		try {
			findProperty(property).addItem(monitoredItem);
			Notification<MonitoredItem> notification = new Notification<>(MonitoredItemNotificationType.Add, this,
					monitoredItem);
			NotificationCentre.broadcast(notification);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.MonitoredItem,
					monitoredItem.toString());
			updateStorage();
		} catch (Exception e) {
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "addItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "addItem");
		}
	}

	public synchronized void addItem(InventoryItem inventoryItem) {
		LOGGER.entering(CLASS_NAME, "addItem", inventoryItem);
		if (inventoryItem == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: inventoryItem was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		Property property = inventoryItem.owner();
		if (property == null) {
			IllegalArgumentException exc = new IllegalArgumentException("PropertyMonitor: property was null");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		if (!properties.contains(property)) {
			IllegalArgumentException exc = new IllegalArgumentException(
					"PropertyMonitor: property " + property + " was not known");
			LOGGER.throwing(CLASS_NAME, "addItem", exc);
			LOGGER.exiting(CLASS_NAME, "addItem");
			throw exc;
		}
		try {
			findProperty(property).addItem(inventoryItem);
			Notification<InventoryItem> notification = new Notification<>(InventoryItemNotificationType.Add, this,
					inventoryItem);
			NotificationCentre.broadcast(notification);
			AuditService.writeAuditInformation(PropertyType.Added, PropertyObject.InventoryItem,
					inventoryItem.toString());
			updateStorage();
		} catch (Exception e) {
			LOGGER.warning("Caught exception: " + e.getMessage());
			LOGGER.throwing(CLASS_NAME, "addItem", e);
			throw e;
		} finally {
			LOGGER.exiting(CLASS_NAME, "addItem");
		}
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
		File applicationDirectory = new File(rootDirectory, ApplicationConfiguration.application().applicationName());
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

	private synchronized Property findProperty(Property property) {
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

}

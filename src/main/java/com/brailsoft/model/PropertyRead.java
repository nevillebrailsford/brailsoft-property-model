package com.brailsoft.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.brailsoft.base.ApplicationConfiguration;
import com.brailsoft.storage.AbstractLoadData;

public class PropertyRead extends AbstractLoadData {
	private static final String CLASS_NAME = PropertyRead.class.getName();
	private static final Logger LOGGER = ApplicationConfiguration.logger();

	@Override
	public void readData() throws IOException {
		LOGGER.entering(CLASS_NAME, "readData");
		try (InputStream archive = new BufferedInputStream(new FileInputStream(fileName()))) {
			readDataFrom(archive);
		} catch (Exception e) {
			IOException exc = new IOException("PropertyRead: Exception occurred - " + e.getMessage(), e);
			LOGGER.throwing(CLASS_NAME, "readData", exc);
			throw exc;
		} finally {
			LOGGER.exiting(CLASS_NAME, "readData");
		}
	}

	private void readDataFrom(InputStream archive) throws Exception {
		LOGGER.entering(CLASS_NAME, "readDataFrom");
		Document document = buildDocument(archive);
		process(document);
		LOGGER.exiting(CLASS_NAME, "readDataFrom");
	}

	private Document buildDocument(InputStream archive) throws Exception {
		LOGGER.entering(CLASS_NAME, "buildDocument");
		Document document = null;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		XMLErrorHandler handler = new XMLErrorHandler();
		documentBuilder.setErrorHandler(handler);
		document = documentBuilder.parse(archive);
		handler.failFast();
		document.getDocumentElement().normalize();
		LOGGER.exiting(CLASS_NAME, "buildDocuments");
		return document;
	}

	private void process(Document document) {
		LOGGER.entering(CLASS_NAME, "process");
		NodeList list = document.getElementsByTagName(Constants.PROPERTY);
		for (int index = 0; index < list.getLength(); index++) {
			Node node = list.item(index);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element propertyElement = (Element) node;
				Property property = new Property(propertyElement);
				PropertyMonitor.instance().addProperty(property);
				updatePropertyWithMonitoredItems(property, propertyElement);
				updatePropertyWithInventoryItems(property, propertyElement);
			}
		}
		LOGGER.exiting(CLASS_NAME, "process");
	}

	private void updatePropertyWithMonitoredItems(Property property, Element propertyElement) {
		NodeList list = propertyElement.getElementsByTagName(Constants.ITEM);
		if (list == null || list.getLength() == 0) {
			return;
		}
		for (int index = 0; index < list.getLength(); index++) {
			Node node = list.item(index);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element itemElement = (Element) node;
				MonitoredItem monitoredItem = new MonitoredItem(itemElement);
				monitoredItem.setOwner(property);
				PropertyMonitor.instance().addItem(monitoredItem);
			}
		}
	}

	private void updatePropertyWithInventoryItems(Property property, Element propertyElement) {
		NodeList list = propertyElement.getElementsByTagName(Constants.INVENTORY);
		if (list == null || list.getLength() == 0) {
			return;
		}
		for (int index = 0; index < list.getLength(); index++) {
			Node node = list.item(index);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element itemElement = (Element) node;
				InventoryItem inventoryItem = new InventoryItem(itemElement);
				inventoryItem.setOwner(property);
				PropertyMonitor.instance().addItem(inventoryItem);
			}
		}
	}
}

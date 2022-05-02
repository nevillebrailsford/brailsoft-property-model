package com.brailsoft.model;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brailsoft.base.ApplicationConfiguration;
import com.brailsoft.storage.AbstractStoreData;

public class PropertyStore extends AbstractStoreData {
	private static final String CLASS_NAME = PropertyStore.class.getName();
	private static final Logger LOGGER = ApplicationConfiguration.logger();

	@Override
	public void storeData() throws IOException {
		LOGGER.entering(CLASS_NAME, "storeData");
		try (OutputStream archive = new BufferedOutputStream(new FileOutputStream(fileName()))) {
			writeDataTo(archive);
		} catch (Exception e) {
			IOException exc = new IOException("PropertyStore: Exception occurred - " + e.getMessage(), e);
			LOGGER.throwing(CLASS_NAME, "storeData", exc);
			throw exc;
		} finally {
			LOGGER.exiting(CLASS_NAME, "storeData");
		}

	}

	private void writeDataTo(OutputStream archive) throws IOException {
		LOGGER.entering(CLASS_NAME, "writeDataTo");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			writeDataTo(document);
			writeXML(document, archive);
		} catch (ParserConfigurationException e) {
			LOGGER.warning("Caught exception: " + e.getMessage());
			IOException exc = new IOException(e.getMessage());
			LOGGER.throwing(CLASS_NAME, "readDataFrom", exc);
			throw exc;
		} finally {
			LOGGER.exiting(CLASS_NAME, "writeDataTo");
		}
	}

	private void writeDataTo(Document document) {
		Element rootElement = document.createElement(XMLConstants.PROPERTIES);
		document.appendChild(rootElement);
		PropertyMonitor.instance().properties().stream().forEach(property -> {
			Element propertyElement = buildElementFor(property, document);
			rootElement.appendChild(propertyElement);
		});
	}

	private Element buildElementFor(Property property, Document document) {
		Element propertyElement = property.buildElement(document);
		for (int index = 0; index < property.monitoredItems().size(); index++) {
			propertyElement.appendChild(property.monitoredItems().get(index).buildElement(document));
		}
		for (int index = 0; index < property.inventoryItems().size(); index++) {
			propertyElement.appendChild(property.inventoryItems().get(index).buildElement(document));
		}
		return propertyElement;
	}

	private void writeXML(Document doc, OutputStream output) throws IOException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(output);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException(e.getMessage());
		}
	}

}

package com.brailsoft.model;

import java.util.ArrayList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLErrorHandler implements ErrorHandler {
	ArrayList<String> errors = new ArrayList<>();

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		System.err.println("WARNING: " + exception.toString());
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		errors.add(exception.toString());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw exception;
	}

	public void failFast() {
		if (errors.size() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : errors) {
			sb.append("ERROR: " + s);
		}
		errors.clear();
		throw new RuntimeException("Parsing failed: " + sb.toString());
	}
}

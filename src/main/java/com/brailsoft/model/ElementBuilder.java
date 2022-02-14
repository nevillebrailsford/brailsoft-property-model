package com.brailsoft.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementBuilder {
	public synchronized static Element build(String tag, String text, Document document) {
		Element result = document.createElement(tag);
		result.setTextContent(text);
		return result;
	}

}

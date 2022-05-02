package com.brailsoft.model;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Address implements Comparable<Address> {
	private StringProperty street = new SimpleStringProperty(this, "street", "");
	private StringProperty town = new SimpleStringProperty(this, "town", "");
	private StringProperty county = new SimpleStringProperty(this, "county", "");
	private ObjectProperty<PostCode> postcode = new SimpleObjectProperty<>(this, "postcode", null);

	private final StringBinding fulladdressBinding = new StringBinding() {
		{
			super.bind(street, town, county, postcode);
		}

		@Override
		protected String computeValue() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(street.get());
			stringBuilder.append(", ").append(town.get());
			stringBuilder.append(", ").append(county.get());
			stringBuilder.append(" ").append(postcode.get().toString());
			return stringBuilder.toString();
		}
	};

	private final ReadOnlyStringWrapper fulladdress = new ReadOnlyStringWrapper(this, "fulladdress");

	public final ReadOnlyStringProperty fulladdressProperty() {
		return fulladdress.getReadOnlyProperty();
	}

	public Address(PostCode postCode, String[] linesOfAddress) {
		if (postCode == null) {
			throw new IllegalArgumentException("Address: post code must be specified");
		}
		if (linesOfAddress == null) {
			throw new IllegalArgumentException("Address: lines of address must be specified");
		}
		if (linesOfAddress.length == 0) {
			throw new IllegalArgumentException("Address: lines of address must have contents");
		}
		if (linesOfAddress.length > 3) {
			throw new IllegalArgumentException("Address: too many lines of address");
		}
		this.postcode.set(postCode);
		this.street.set(linesOfAddress[0]);
		this.town.set(linesOfAddress[1]);
		this.county.set(linesOfAddress[2]);
		this.fulladdress.bind(fulladdressBinding);
	}

	public Address(Address that) {
		if (that == null) {
			throw new IllegalArgumentException("Address: address must be specified");
		}
		this.postcode.set(new PostCode(that.postcode.get()));
		this.street.set(that.street.get());
		this.town.set(that.town.get());
		this.county.set(that.county.get());
		this.fulladdress.bind(fulladdressBinding);
	}

	public Address(Element addressElement) {
		if (addressElement == null) {
			throw new IllegalArgumentException("Address: addressElement was null");
		}
		NodeList list = addressElement.getElementsByTagName(XMLConstants.LINE);
		String[] linesOfAddress = new String[list.getLength()];
		for (int index = 0; index < list.getLength(); index++) {
			linesOfAddress[index] = list.item(index).getTextContent();
		}
		this.postcode.set(new PostCode((Element) addressElement.getElementsByTagName(XMLConstants.POSTCODE).item(0)));
		this.street.set(linesOfAddress[0]);
		this.town.set(linesOfAddress[1]);
		this.county.set(linesOfAddress[2]);
		this.fulladdress.bind(fulladdressBinding);
	}

	public Element buildElement(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("Address: document was null");
		}
		Element result = document.createElement(XMLConstants.ADDRESS);
		result.appendChild(postCode().buildElement(document));
		for (int index = 0; index < linesOfAddress().length; index++) {
			result.appendChild(ElementBuilder.build(XMLConstants.LINE, linesOfAddress()[index], document));
		}
		return result;

	}

	public String[] linesOfAddress() {
		String[] lines = new String[3];
		lines[0] = street.get();
		lines[1] = town.get();
		lines[2] = county.get();
		return lines;
	}

	public PostCode postCode() {
		return new PostCode(postcode.get());
	}

	public ObjectProperty<PostCode> postCodeProperty() {
		return postcode;
	}

	public StringProperty streetProperty() {
		return street;
	}

	public StringProperty townProperty() {
		return town;
	}

	public StringProperty countyProperty() {
		return county;
	}

	@Override
	public int compareTo(Address that) {
		return this.toString().compareTo(that.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(street.get(), town.get(), county.get(), postcode.get());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address that = (Address) obj;
		return Objects.equals(street.get(), that.street.get()) && Objects.equals(this.town.get(), that.town.get())
				&& Objects.equals(this.county.get(), that.county.get())
				&& Objects.equals(postcode.get(), that.postcode.get());
	}

	@Override
	public String toString() {
		return fulladdress.get();
	}

}

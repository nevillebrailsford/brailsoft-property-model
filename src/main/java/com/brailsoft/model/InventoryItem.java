package com.brailsoft.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InventoryItem implements Comparable<InventoryItem> {
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(ModelConstants.dateFormatForUI);
	private DateTimeFormatter storageFormatter = DateTimeFormatter.ofPattern(ModelConstants.dateFormatForStorage);

	private StringProperty description = new SimpleStringProperty(this, "description", "");
	private StringProperty manufacturer = new SimpleStringProperty(this, "manufacturuer", "");
	private StringProperty model = new SimpleStringProperty(this, "model", "");
	private StringProperty serialNumber = new SimpleStringProperty(this, "serialNumber", "");
	private StringProperty supplier = new SimpleStringProperty(this, "supplier", "");
	private StringProperty purchaseDate = new SimpleStringProperty(this, "puchaseDate", null);
	private ObjectProperty<Property> owner = new SimpleObjectProperty<>(this, "owner", null);

	public InventoryItem(String description, String manufacturer, String model, String serialNumber, String supplier,
			LocalDate purchaseDateAsDate) {
		if (description == null || description.isBlank() || description.isEmpty()) {
			throw new IllegalArgumentException("InventoryItem: description was missing");
		}
		if (manufacturer == null) {
			throw new IllegalArgumentException("InventoryItem: manufacturer was null");
		}
		if (model == null) {
			throw new IllegalArgumentException("InventoryItem: model was null");
		}
		if (serialNumber == null) {
			throw new IllegalArgumentException("InventoryItem: serialNumber was null");
		}
		if (supplier == null) {
			throw new IllegalArgumentException("InventoryItem: supplier was null");
		}
		String purchaseDate;
		if (purchaseDateAsDate == null) {
			purchaseDate = "";
		} else {
			purchaseDate = purchaseDateAsDate.format(dateFormatter);
		}
		initialize(description, manufacturer, model, serialNumber, supplier, purchaseDate);
	}

	public InventoryItem(InventoryItem that) {
		if (that == null) {
			throw new IllegalArgumentException("InventoryItem: item was null");
		}
		this.description.set(that.description.get());
		this.manufacturer.set(that.manufacturer.get());
		this.model.set(that.model.get());
		this.serialNumber.set(that.serialNumber.get());
		this.supplier.set(that.supplier.get());
		this.purchaseDate.set(that.purchaseDate.get());
		if (that.owner.get() != null) {
			this.owner.set(new Property(that.owner.get()));
		} else {
			this.owner.set(null);
		}
	}

	public InventoryItem(Element itemElement) {
		if (itemElement == null) {
			throw new IllegalArgumentException("InventoryItem: itemElement was null");
		}
		String description = itemElement.getElementsByTagName(Constants.DESCRIPTION).item(0).getTextContent();
		String manufacturer = textContent(itemElement, Constants.MANUFACTURER);
		String model = textContent(itemElement, Constants.MODEL);
		String serialNumber = textContent(itemElement, Constants.SERIAL_NUMBER);
		String supplier = textContent(itemElement, Constants.SUPPLIER);
		String purchaseDate = textContent(itemElement, Constants.PURCHASE_DATE);
		if (!purchaseDate.isEmpty()) {
			LocalDate lDate = LocalDate.parse(purchaseDate, storageFormatter);
			purchaseDate = lDate.format(dateFormatter);
		}

		initialize(description, manufacturer, model, serialNumber, supplier, purchaseDate);
	}

	private String textContent(Element itemElement, String tag) {
		String result = "";
		NodeList list = itemElement.getElementsByTagName(tag);
		if (list.getLength() == 1) {
			result = list.item(0).getTextContent();
		}
		return result;
	}

	public Element buildElement(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("InventoryItem: document was null");
		}
		Element result = document.createElement(Constants.INVENTORY);
		result.appendChild(ElementBuilder.build(Constants.DESCRIPTION, description(), document));
		if (!manufacturer().isEmpty()) {
			result.appendChild(ElementBuilder.build(Constants.MANUFACTURER, manufacturer(), document));
		}
		if (!model().isEmpty()) {
			result.appendChild(ElementBuilder.build(Constants.MODEL, model(), document));
		}
		if (!serialNumber().isEmpty()) {
			result.appendChild(ElementBuilder.build(Constants.SERIAL_NUMBER, serialNumber(), document));
		}
		if (!supplier().isEmpty()) {
			result.appendChild(ElementBuilder.build(Constants.SUPPLIER, supplier(), document));
		}
		if (!purchaseDate().isEmpty()) {
			LocalDate lDate = LocalDate.parse(purchaseDate(), dateFormatter);
			String dateForStorage = lDate.format(storageFormatter);
			result.appendChild(ElementBuilder.build(Constants.PURCHASE_DATE, dateForStorage, document));
		}
		return result;
	}

	private void initialize(String description, String manufacturer, String model, String serialNumber, String supplier,
			String purchaseDate) {
		this.description.set(description);
		this.manufacturer.set(manufacturer);
		this.model.set(model);
		this.serialNumber.set(serialNumber);
		this.supplier.set(supplier);
		this.purchaseDate.set(purchaseDate);
		this.owner.set(null);
	}

	public String description() {
		return description.get();
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public String manufacturer() {
		return manufacturer.get();
	}

	public StringProperty manufacturerProperty() {
		return manufacturer;
	}

	public String model() {
		return model.get();
	}

	public StringProperty modelProperty() {
		return model;
	}

	public String serialNumber() {
		return serialNumber.get();
	}

	public StringProperty serialNumberProperty() {
		return serialNumber;
	}

	public String supplier() {
		return supplier.get();
	}

	public StringProperty supplierProperty() {
		return supplier;
	}

	public String purchaseDate() {
		return purchaseDate.get();
	}

	public StringProperty purchaseDateProperty() {
		return purchaseDate;
	}

	public Property owner() {
		return owner.get();
	}

	public void setOwner(Property property) {
		this.owner.set(new Property(property));
	}

	public ObjectProperty<Property> ownerProperty() {
		return owner;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description.get());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InventoryItem other = (InventoryItem) obj;
		return Objects.equals(description.get(), other.description.get());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (description.get() != null) {
			builder.append(description.get());
			builder.append(", ");
		}
		if (manufacturer.get() != null) {
			builder.append(manufacturer.get());
			builder.append(", ");
		}
		if (model.get() != null) {
			builder.append(model.get());
			builder.append(", ");
		}
		if (serialNumber.get() != null) {
			builder.append(serialNumber.get());
		}
		return builder.toString();
	}

	@Override
	public int compareTo(InventoryItem that) {
		int result = this.manufacturer.get().compareTo(that.manufacturer.get());
		if (result == 0) {
			result = this.model.get().compareTo(that.model.get());
			if (result == 0) {
				result = this.serialNumber.get().compareTo(that.serialNumber.get());
			}
		}
		return result;
	}

}

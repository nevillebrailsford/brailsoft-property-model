module brailsoft.model {
	exports com.brailsoft.model;

	requires brailsoft.base;
	requires brailsoft.storage;
	requires java.logging;
	requires java.xml;
	requires javafx.base;

	opens com.brailsoft.model;
}
module brailsoft.model {
	exports com.brailsoft.model;

	requires brailsoft.base;
	requires brailsoft.storage;
	requires java.logging;
	requires transitive java.xml;
	requires transitive javafx.base;

	opens com.brailsoft.model;
}
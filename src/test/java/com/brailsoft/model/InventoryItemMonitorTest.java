package com.brailsoft.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.LocalDate;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.brailsoft.base.Application;
import com.brailsoft.base.ApplicationConfiguration;
import com.brailsoft.base.LogConfigurer;
import com.brailsoft.base.Notification;
import com.brailsoft.base.NotificationCentre;
import com.brailsoft.base.NotificationListener;
import com.brailsoft.storage.StorageNotificationType;
import com.brailsoft.storage.StoreState;

class InventoryItemMonitorTest {
	private static final PostCode postCode1 = new PostCode("CW3 9ST");
	private static final PostCode postCode2 = new PostCode("CW3 9SU");
	private static final String LINE1 = "99 The Street";
	private static final String LINE2 = "The Town";
	private static final String LINE3 = "The County";
	private static final String[] linesOfAddress = new String[] { LINE1, LINE2, LINE3 };
	private static final Address address1 = new Address(postCode1, linesOfAddress);
	private static final Property property1 = new Property(address1);
	private static final Address address2 = new Address(postCode2, linesOfAddress);
	private static final Property property2 = new Property(address2);
	private LocalDate startTest;
	private MonitoredItem testItem;
	private MonitoredItem overdueItem;
	private MonitoredItem noticeDueItem;
	private InventoryItem testInventory;

	private Object waitForIO = new Object();
	private boolean addedItem = false;
	private boolean removedItem = false;
	private boolean changedItem = false;

	NotificationListener listener = new NotificationListener() {
		@Override
		public void notify(Notification notification) {
			assertTrue(notification.subject().isPresent());
			if (notification.notificationType() instanceof StorageNotificationType) {
				handleStorage(notification);
			} else if (notification.notificationType() instanceof InventoryItemNotificationType) {
				handleMonitoredItem(notification);
			}
		}
	};

	@TempDir
	File rootDirectory;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		startTest = LocalDate.now();
		testInventory = new InventoryItem("inventory1", "manufacturer1", "model1", "serialnumber1", "supplier1",
				LocalDate.now());
		testInventory.setOwner(property1);
		resetFlags();
		Application app = new Application("test") {
			@Override
			public Level level() {
				return Level.OFF;
			}
		};
		ApplicationConfiguration.registerApplication(app, rootDirectory.getAbsolutePath());
		LogConfigurer.setUp();
		NotificationCentre.addListener(listener);
	}

	@AfterEach
	void tearDown() throws Exception {
		synchronized (waitForIO) {
			PropertyMonitor.instance().clear();
			waitForIO.wait();
		}
		NotificationCentre.removeListener(listener);
		LogConfigurer.shutdown();
		ApplicationConfiguration.clear();
	}

	@Test
	void test() {
		assertNotNull(PropertyMonitor.instance());
	}

	@Test
	void testAddItem() throws InterruptedException {
		synchronized (waitForIO) {
			PropertyMonitor.instance().addProperty(property1);
			waitForIO.wait();
			assertEquals(1, PropertyMonitor.instance().properties().size());
		}
		synchronized (waitForIO) {
			assertFalse(addedItem);
			PropertyMonitor.instance().addItem(testInventory);
			waitForIO.wait();
			assertTrue(addedItem);
			assertEquals(1, PropertyMonitor.instance().properties().get(0).inventoryItems().size());
		}
	}

	private void resetFlags() {
		addedItem = false;
		removedItem = false;
		changedItem = false;
	}

	private void handleMonitoredItem(Notification notification) {
		InventoryItemNotificationType type = (InventoryItemNotificationType) notification.notificationType();
		switch (type) {
			case Add -> {
				addItem();
			}
			case Changed -> {
				changeItem();
			}
			case Removed -> {
				removeItem();
			}
		}
	}

	private void handleStorage(Notification notification) {
		StorageNotificationType type = (StorageNotificationType) notification.notificationType();
		switch (type) {
			case Store -> {
				StoreState state = (StoreState) notification.subject().get();
				switch (state) {
					case Complete -> storeData();
					case Failed -> storeData();
					case Started -> ignore();
				}
			}
			case Load -> ignore();
		}
	}

	private void ignore() {
	}

	private void storeData() {
		synchronized (waitForIO) {
			waitForIO.notifyAll();
		}
	}

	private void addItem() {
		addedItem = true;
	}

	private void removeItem() {
		removedItem = true;
	}

	private void changeItem() {
		changedItem = true;
	}
}

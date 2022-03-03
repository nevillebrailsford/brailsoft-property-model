package com.brailsoft.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	private static final String LINE1 = "99 The Street";
	private static final String LINE2 = "The Town";
	private static final String LINE3 = "The County";
	private static final String[] linesOfAddress = new String[] { LINE1, LINE2, LINE3 };
	private static final Address address1 = new Address(postCode1, linesOfAddress);
	private InventoryItem testInventory;
	private InventoryItem testInventory2;

	private Property property1 = null;
	private Object waitForIO = new Object();
	private boolean addedItem = false;
	private boolean removedItem = false;
	private boolean failedIO = false;

	NotificationListener listener = new NotificationListener() {
		@Override
		public void notify(Notification notification) {
			if (notification.notificationType() instanceof StorageNotificationType) {
				assertTrue(notification.subject().isPresent());
				handleStorage(notification);
			} else if (notification.notificationType() instanceof InventoryItemNotificationType) {
				if (notification.notificationType() != InventoryItemNotificationType.Failed) {
					assertTrue(notification.subject().isPresent());
				}
				handleInventoryItem(notification);
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
		property1 = new Property(address1);
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

	@Test
	void testRemoveItem() throws InterruptedException {
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
			PropertyMonitor.instance().removeItem(testInventory);
			waitForIO.wait();
			assertTrue(removedItem);
			assertEquals(0, PropertyMonitor.instance().properties().get(0).inventoryItems().size());
		}
	}

	@Test
	void testAddUnknownOwner() throws InterruptedException {
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().addItem(testInventory);
			});
			assertEquals("PropertyMonitor: property 99 The Street, The Town, The County CW3 9ST was not known",
					exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testAddNullItem() throws InterruptedException {
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().addItem((InventoryItem) null);
			});
			assertEquals("PropertyMonitor: inventoryItem was null", exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testAddNullOwner() throws InterruptedException {
		synchronized (waitForIO) {
			testInventory2 = new InventoryItem("inventory1", "manufacturer1", "model1", "serialnumber1", "supplier1",
					LocalDate.now());
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().addItem(testInventory2);
			});
			assertEquals("PropertyMonitor: property was null", exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testAddDuplicateItem() throws InterruptedException {
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
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().addItem(testInventory);
			});
			assertEquals("Property: item inventory1, manufacturer1, model1, serialnumber1 already exists",
					exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testRemoveUnknownOwner() throws InterruptedException {
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().removeItem(testInventory);
			});
			assertEquals("PropertyMonitor: property 99 The Street, The Town, The County CW3 9ST was not known",
					exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testRemoveNullItem() throws InterruptedException {
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().removeItem((InventoryItem) null);
			});
			assertEquals("PropertyMonitor: inventoryItem was null", exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testRemoveNullOwner() throws InterruptedException {
		testInventory2 = new InventoryItem("inventory1", "manufacturer1", "model1", "serialnumber1", "supplier1",
				LocalDate.now());
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().removeItem(testInventory2);
			});
			assertEquals("PropertyMonitor: property was null", exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	@Test
	void testRemoveUnknownItem() throws InterruptedException {
		synchronized (waitForIO) {
			PropertyMonitor.instance().addProperty(property1);
			waitForIO.wait();
			assertEquals(1, PropertyMonitor.instance().properties().size());
		}
		synchronized (waitForIO) {
			Exception exc = assertThrows(IllegalArgumentException.class, () -> {
				PropertyMonitor.instance().removeItem(testInventory);
			});
			assertEquals("Property: item inventory1, manufacturer1, model1, serialnumber1 not found", exc.getMessage());
			waitForIO.wait();
		}
		assertTrue(failedIO);
	}

	private void resetFlags() {
		addedItem = false;
		removedItem = false;
		failedIO = false;
	}

	private void handleInventoryItem(Notification notification) {
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
			case Failed -> {
				failed();
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

	private void failed() {
		synchronized (waitForIO) {
			failedIO = true;
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
	}
}

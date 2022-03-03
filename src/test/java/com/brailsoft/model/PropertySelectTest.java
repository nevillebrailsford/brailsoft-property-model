package com.brailsoft.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class PropertySelectTest {

	private static final PostCode postCode1 = new PostCode("CW3 9ST");
	private static final String LINE1 = "99 The Street";
	private static final String LINE2 = "The Town";
	private static final String LINE3 = "The County";
	private static final String[] linesOfAddress = new String[] { LINE1, LINE2, LINE3 };
	private static final Address address1 = new Address(postCode1, linesOfAddress);

	private LocalDate startTest;
	private Object waitForIO = new Object();

	private Property property1 = new Property(address1);
	private MonitoredItem testItem;
	private InventoryItem testInventory;

	NotificationListener listener = new NotificationListener() {
		@Override
		public void notify(Notification notification) {
			if (notification.notificationType() instanceof StorageNotificationType) {
				assertTrue(notification.subject().isPresent());
				handleStorage(notification);
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
		property1 = new Property(address1);
		testItem = new MonitoredItem("item1", Period.YEARLY, 1, startTest, 1, Period.WEEKLY);
		testItem.setOwner(property1);
		testInventory = new InventoryItem("inventory1", "manufacturer1", "model1", "serialnumber1", "supplier1",
				LocalDate.now());
		testInventory.setOwner(property1);
		Application app = new Application("test") {
			@Override
			public Level level() {
				return Level.OFF;
			}
		};
		ApplicationConfiguration.registerApplication(app, rootDirectory.getAbsolutePath());
		LogConfigurer.setUp();
		NotificationCentre.addListener(listener);
		addObjects();
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
	void testMonitoredItems() {
		assertEquals(1, PropertySelect.monitoredItemsFor(property1).size());
	}

	@Test
	void testInventoryItems() {
		assertEquals(1, PropertySelect.inventoryFor(property1).size());
	}

	@Test
	void testOverdueItemsForToday() {
		assertEquals(0, PropertySelect.overdueItemsFor(startTest).size());
	}

	@Test
	void testOverdueItemsForYearAhead() {
		assertEquals(1, PropertySelect.overdueItemsFor(startTest.plusYears(1)).size());
	}

	@Test
	void testOverdueItemsForYearAheadLessOneDay() {
		assertEquals(0, PropertySelect.overdueItemsFor(startTest.plusYears(1).minusDays(1)).size());
	}

	@Test
	void testOverdueItemsForYearAheadPlusOneDay() {
		assertEquals(0, PropertySelect.overdueItemsFor(startTest.plusYears(1).plusDays(1)).size());
	}

	@Test
	void testNotifiedItemsForYearAhead() {
		assertEquals(1, PropertySelect.notifiedItemsFor(startTest.plusYears(1).minusWeeks(1)).size());
	}

	@Test
	void testNotifiedItemsForYearAheadLessOneDay() {
		assertEquals(0, PropertySelect.notifiedItemsFor(startTest.plusYears(1).minusWeeks(1).minusDays(1)).size());
	}

	@Test
	void testNotifiedItemsForYearAheadPlusOneDay() {
		assertEquals(0, PropertySelect.notifiedItemsFor(startTest.plusYears(1).minusWeeks(1).plusDays(1)).size());
	}

	private void addObjects() throws InterruptedException {
		synchronized (waitForIO) {
			PropertyMonitor.instance().addProperty(property1);
			waitForIO.wait();
		}
		synchronized (waitForIO) {
			PropertyMonitor.instance().addItem(testItem);
			waitForIO.wait();
		}
		synchronized (waitForIO) {
			PropertyMonitor.instance().addItem(testInventory);
			waitForIO.wait();
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

}

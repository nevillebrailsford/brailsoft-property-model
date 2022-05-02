package com.brailsoft.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MonitoredItem implements Comparable<MonitoredItem> {

	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(ModelConstants.dateFormatForUI);

	private StringProperty description = new SimpleStringProperty(this, "description", "");
	private ObjectProperty<LocalDate> lastActionPerformed = new SimpleObjectProperty<>(this, "lastActionPerformed",
			null);
	private ObjectProperty<LocalDate> timeForNextAction = new SimpleObjectProperty<>(this, "timeForNextAction", null);
	private ObjectProperty<LocalDate> timeForNextNotice = new SimpleObjectProperty<>(this, "timeForNextNotice", null);
	private ObjectProperty<Period> periodForNextAction = new SimpleObjectProperty<>(this, "periodForNextAction", null);;
	private IntegerProperty noticeEvery = new SimpleIntegerProperty(this, "noticeEvery", 0);;
	private ObjectProperty<Period> periodForNextNotice = new SimpleObjectProperty<>(this, "periodForNextNotice", null);;
	private IntegerProperty advanceNotice = new SimpleIntegerProperty(this, "advanceNotice", 0);
	private ObjectProperty<Property> owner = new SimpleObjectProperty<>(this, "owner", null);
	private ObjectProperty<LocalDate> emailSentOn = new SimpleObjectProperty<>(this, "emailSentOn", null);

	private final StringBinding lastActionBinding = new StringBinding() {
		{
			super.bind(lastActionPerformed);
		}

		@Override
		protected String computeValue() {
			return lastActionPerformed.get().format(dateFormatter);
		}
	};
	private final StringBinding nextActionBinding = new StringBinding() {
		{
			super.bind(timeForNextAction);
		}

		@Override
		protected String computeValue() {
			return timeForNextAction.get().format(dateFormatter);
		}
	};

	private final StringBinding nextNoticeBinding = new StringBinding() {
		{
			super.bind(timeForNextNotice);
		}

		@Override
		protected String computeValue() {
			return timeForNextNotice.get().format(dateFormatter);
		}
	};

	private final StringBinding emailSentOnBinding = new StringBinding() {
		{
			super.bind(emailSentOn);
		}

		@Override
		protected String computeValue() {
			if (emailSentOn.get() == null) {
				return "";
			} else {
				return emailSentOn.get().format(dateFormatter);
			}
		}
	};

	private final ReadOnlyStringWrapper lastAction = new ReadOnlyStringWrapper(this, "lastAction");
	private final ReadOnlyStringWrapper nextAction = new ReadOnlyStringWrapper(this, "nextAction");
	private final ReadOnlyStringWrapper nextNotice = new ReadOnlyStringWrapper(this, "nextNotice");

	private final ReadOnlyStringWrapper emailSent = new ReadOnlyStringWrapper(this, "emailSent");

	public ReadOnlyStringProperty lastActionProperty() {
		return lastAction.getReadOnlyProperty();
	}

	public ReadOnlyStringProperty nextActionProperty() {
		return nextAction.getReadOnlyProperty();
	}

	public ReadOnlyStringProperty nextNoticeProperty() {
		return nextNotice.getReadOnlyProperty();
	}

	public ReadOnlyStringProperty emailSentOnProperty() {
		return emailSent.getReadOnlyProperty();
	}

	public MonitoredItem(String description, Period periodForNextAction, int noticeEvery, LocalDate lastActioned,
			int advanceNotice, Period periodForNextNotice) {
		if (description == null || description.isBlank() || description.isEmpty()) {
			throw new IllegalArgumentException("MonitoredItem: description not specified");
		}
		if (periodForNextAction == null) {
			throw new IllegalArgumentException("MonitoredItem: period was null");
		}
		if (noticeEvery < 1) {
			throw new IllegalArgumentException("MonitoredItem: noticeEvery less than 1");
		}
		if (advanceNotice < 1) {
			throw new IllegalArgumentException("MonitoredItem: advanceNotice less than 1");
		}
		if (lastActioned == null) {
			throw new IllegalArgumentException("MonitoredItem: lastActioned was null");
		}
		if (periodForNextNotice == null) {
			throw new IllegalArgumentException("MonitoredItem: periodForNextNotice was null");
		}
		initialize(description, lastActioned, periodForNextAction, noticeEvery, advanceNotice, periodForNextNotice,
				null);
	}

	public MonitoredItem(MonitoredItem that) {
		if (that == null) {
			throw new IllegalArgumentException("MonitoredItem: item was null");
		}
		this.description.set(that.description.get());
		this.periodForNextAction.set(that.periodForNextAction.get());
		this.noticeEvery.set(that.noticeEvery.get());
		this.advanceNotice.set(that.advanceNotice.get());
		this.periodForNextNotice.set(that.periodForNextNotice.get());
		this.lastActionPerformed.set(that.lastActionPerformed.get());
		this.timeForNextAction.set(that.timeForNextAction.get());
		this.timeForNextNotice.set(that.timeForNextNotice.get());
		this.emailSentOn.set(that.emailSentOn.get());
		if (that.owner.get() != null) {
			this.owner.set(new Property(that.owner.get()));
		} else {
			this.owner.set(null);
		}
		initializeBindings();
	}

	public MonitoredItem(Element itemElement) {
		if (itemElement == null) {
			throw new IllegalArgumentException("MonitoredItem: itemElement was null");
		}
		String description = itemElement.getElementsByTagName(XMLConstants.DESCRIPTION).item(0).getTextContent();
		String speriodForNextAction = itemElement.getElementsByTagName(XMLConstants.PERIOD_FOR_NEXT_ACTION).item(0)
				.getTextContent();
		String snoticeEvery = itemElement.getElementsByTagName(XMLConstants.NOTICE_EVERY).item(0).getTextContent();
		String slastActioned = itemElement.getElementsByTagName(XMLConstants.LAST_ACTIONED).item(0).getTextContent();
		String sadvanceNotice = itemElement.getElementsByTagName(XMLConstants.ADVANCE_NOTICE).item(0).getTextContent();
		String speriodForNextNotice = itemElement.getElementsByTagName(XMLConstants.PERIOD_FOR_NEXT_NOTICE).item(0)
				.getTextContent();
		String sEmailSentOn = null;
		if (itemElement.getElementsByTagName(XMLConstants.EMAIL_SENT_ON).getLength() > 0) {
			sEmailSentOn = itemElement.getElementsByTagName(XMLConstants.EMAIL_SENT_ON).item(0).getTextContent();
		}

		LocalDate lastActioned = LocalDate.parse(slastActioned);
		Period periodForNextAction = Period.valueOf(speriodForNextAction);
		int noticeEvery = Integer.parseInt(snoticeEvery);
		int advanceNotice = Integer.parseInt(sadvanceNotice);
		Period periodForNextNotice = Period.valueOf(speriodForNextNotice);
		LocalDate emailSentOn = null;
		if (sEmailSentOn != null) {
			emailSentOn = LocalDate.parse(sEmailSentOn);
		}

		initialize(description, lastActioned, periodForNextAction, noticeEvery, advanceNotice, periodForNextNotice,
				emailSentOn);
	}

	public Element buildElement(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("MonitoredItem: document was null");
		}
		Element result = document.createElement(XMLConstants.ITEM);
		result.appendChild(ElementBuilder.build(XMLConstants.DESCRIPTION, description(), document));
		result.appendChild(
				ElementBuilder.build(XMLConstants.PERIOD_FOR_NEXT_ACTION, periodForNextAction().toString(), document));
		result.appendChild(ElementBuilder.build(XMLConstants.NOTICE_EVERY, Integer.toString(noticeEvery()), document));
		result.appendChild(
				ElementBuilder.build(XMLConstants.LAST_ACTIONED, getLastActionPerformed().toString(), document));
		result.appendChild(ElementBuilder.build(XMLConstants.ADVANCE_NOTICE, Integer.toString(advanceNotice()), document));
		result.appendChild(
				ElementBuilder.build(XMLConstants.PERIOD_FOR_NEXT_NOTICE, periodForNextNotice().toString(), document));
		if (emailSentOn() != null) {
			result.appendChild(ElementBuilder.build(XMLConstants.EMAIL_SENT_ON, emailSentOn().toString(), document));
		}
		return result;

	}

	private void initialize(String description, LocalDate lastActioned, Period periodForNextAction, int noticeEvery,
			int advanceNotice, Period periodForNextNotice, LocalDate emailSentOn) {
		this.description.set(description);
		this.periodForNextAction.set(periodForNextAction);
		this.noticeEvery.set(noticeEvery);
		this.advanceNotice.set(advanceNotice);
		this.periodForNextNotice.set(periodForNextNotice);
		this.lastActionPerformed.set(lastActioned);
		this.timeForNextAction.set(calculateTimeForNextAction(periodForNextAction, noticeEvery, lastActioned));
		this.timeForNextNotice
				.set(calculateTimeForNextNotice(periodForNextNotice, advanceNotice, this.timeForNextAction.get()));
		this.owner.set(null);
		this.emailSentOn.set(emailSentOn);
		initializeBindings();
	}

	private void initializeBindings() {
		this.lastAction.bind(lastActionBinding);
		this.nextAction.bind(nextActionBinding);
		this.nextNotice.bind(nextNoticeBinding);
		this.emailSent.bind(emailSentOnBinding);
	}

	public Property owner() {
		return new Property(owner.get());
	}

	public void setOwner(Property owner) {
		this.owner.set(new Property(owner));
	}

	public ObjectProperty<Property> ownerProperty() {
		return owner;
	}

	public String description() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public Period periodForNextAction() {
		return periodForNextAction.get();
	}

	public void setPeriodForNextAction(Period periodForNextAction) {
		this.periodForNextAction.set(periodForNextAction);
		recalculateTimes();
	}

	public ObjectProperty<Period> periodForNextActionProperty() {
		return periodForNextAction;
	}

	public int noticeEvery() {
		return noticeEvery.get();
	}

	public void setNoticeEvery(int noticeEvery) {
		this.noticeEvery.set(noticeEvery);
		recalculateTimes();
	}

	public IntegerProperty noticeEveryProperty() {
		return noticeEvery;
	}

	public int advanceNotice() {
		return advanceNotice.get();
	}

	public void setAdvanceNotice(int advanceNotice) {
		this.advanceNotice.set(advanceNotice);
		recalculateNextNotice();
	}

	public IntegerProperty advanceNoticeProperty() {
		return advanceNotice;
	}

	public LocalDate getLastActionPerformed() {
		return lastActionPerformed.get();
	}

	public LocalDate timeForNextAction() {
		return timeForNextAction.get();
	}

	public LocalDate timeForNextNotice() {
		return timeForNextNotice.get();
	}

	public Period periodForNextNotice() {
		return periodForNextNotice.get();
	}

	public void setPeriodForNextNotice(Period periodForNextNotice) {
		this.periodForNextNotice.set(periodForNextNotice);
		recalculateNextNotice();
	}

	public ObjectProperty<Period> periodForNextNoticeProperty() {
		return periodForNextNotice;
	}

	public void actionPerformed(LocalDate when) {
		this.lastActionPerformed.set(when);
		recalculateTimes();
	}

	public ObjectProperty<LocalDate> lastActionPerformedProperty() {
		return lastActionPerformed;
	}

	public void setEmailSentOn(LocalDate when) {
		this.emailSentOn.set(when);
	}

	public LocalDate emailSentOn() {
		return emailSentOn.get();
	}

	public boolean overdue() {
		return overdue(LocalDate.now());
	}

	public boolean overdue(LocalDate today) {
		if (today.isAfter(timeForNextAction.get())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean noticeDue() {
		return noticeDue(LocalDate.now());
	}

	public boolean noticeDue(LocalDate today) {
		if (today.isAfter(timeForNextNotice.get())) {
			return true;
		} else {
			return false;
		}
	}

	private LocalDate calculateTimeForNextNotice(Period periodForNextNotice, int advanceNotice,
			LocalDate timeForNextAction) {
		var result = switch (periodForNextNotice) {
			case WEEKLY -> reduceTimeStampByWeeks(advanceNotice, timeForNextAction);
			case MONTHLY -> reduceTimeStampByMonths(advanceNotice, timeForNextAction);
			case YEARLY -> reduceTimeStampByYears(advanceNotice, timeForNextAction);
		};
		return result;
	}

	private LocalDate calculateTimeForNextAction(Period period, int noticeEvery, LocalDate lastActioned) {
		var result = switch (period) {
			case WEEKLY -> increaesTimeStampByWeeks(noticeEvery, lastActioned);
			case MONTHLY -> increaseTimeStampByMonths(noticeEvery, lastActioned);
			case YEARLY -> increaseTimeStampByYears(noticeEvery, lastActioned);
		};
		return result;
	}

	private void recalculateTimes() {
		recalculateNextAction();
		recalculateNextNotice();
	}

	private void recalculateNextAction() {
		timeForNextAction.set(
				calculateTimeForNextAction(periodForNextAction.get(), noticeEvery.get(), lastActionPerformed.get()));
	}

	private void recalculateNextNotice() {
		timeForNextNotice.set(
				calculateTimeForNextNotice(periodForNextNotice.get(), advanceNotice.get(), timeForNextAction.get()));
	}

	private LocalDate increaesTimeStampByWeeks(int noticeEvery, LocalDate lastActioned) {
		return lastActioned.plusWeeks(noticeEvery);
	}

	private LocalDate increaseTimeStampByMonths(int noticeEvery, LocalDate lastActioned) {
		return lastActioned.plusMonths(noticeEvery);
	}

	private LocalDate increaseTimeStampByYears(int noticeEvery, LocalDate lastActioned) {
		return lastActioned.plusYears(noticeEvery);
	}

	private LocalDate reduceTimeStampByWeeks(int advanceNotice, LocalDate nextAction) {
		return nextAction.minusWeeks(advanceNotice);
	}

	private LocalDate reduceTimeStampByMonths(int advanceNotice, LocalDate nextAction) {
		return nextAction.minusMonths(advanceNotice);
	}

	private LocalDate reduceTimeStampByYears(int advanceNotice, LocalDate nextAction) {
		return nextAction.minusYears(advanceNotice);
	}

	@Override
	public int compareTo(MonitoredItem that) {
		return this.timeForNextAction().compareTo(that.timeForNextAction());
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
		MonitoredItem other = (MonitoredItem) obj;
		return Objects.equals(description.get(), other.description.get());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (description.get() != null) {
			builder.append(description.get());
		}
		return builder.toString();
	}

}

package calendarapp.factory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class EditInput {
  private String property;              // e.g. "name", "startdatetime"
  private String eventName;            // name of the event to be edited
  private ZonedDateTime fromStart;     // original start datetime (optional for repeatuntil, etc.)
  private ZonedDateTime fromEnd;       // original end datetime   (optional for repeatuntil, etc.)
  private String newValue;             // new value for the property
  private boolean isRecurring;
  private String originalName;
  private ZonedDateTime fromDateTime;
  private ZonedDateTime toDateTime;
  private String newName;
  private LocalDateTime newStart;
  private LocalDateTime newEnd;
  private String newDescription;
  private String newLocation;

  public EditInput() {}

  // Full constructor (used when fromStart/fromEnd are needed)
  public EditInput(String property, String eventName, ZonedDateTime fromStart, ZonedDateTime fromEnd, String newValue, boolean isRecurring) {
    this.property = property;
    this.eventName = eventName;
    this.fromStart = fromStart;
    this.fromEnd = fromEnd;
    this.newValue = newValue;
    this.isRecurring = isRecurring;
  }

  // Minimal constructor (used for repeatuntil, repeattimes, repeatingdays)
  public EditInput(String property, String eventName, String newValue, boolean isRecurring) {
    this(property, eventName, null, null, newValue, isRecurring);
  }

  public String getProperty() {
    return property;
  }

  public String getEventName() {
    return eventName;
  }

  public ZonedDateTime getFromStart() {
    return fromStart;
  }

  public ZonedDateTime getFromEnd() {
    return fromEnd;
  }

  public String getNewValue() {
    return newValue;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setFromStart(ZonedDateTime fromStart) {
    this.fromStart = fromStart;
  }

  public void setFromEnd(ZonedDateTime fromEnd) {
    this.fromEnd = fromEnd;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public void setRecurring(boolean recurring) {
    isRecurring = recurring;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public void setFromDateTime(ZonedDateTime fromDateTime) {
    this.fromDateTime = fromDateTime;
  }

  public void setToDateTime(ZonedDateTime toDateTime) {
    this.toDateTime = toDateTime;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public void setNewStart(LocalDateTime newStart) {
    this.newStart = newStart;
  }

  public void setNewEnd(LocalDateTime newEnd) {
    this.newEnd = newEnd;
  }

  public void setNewDescription(String newDescription) {
    this.newDescription = newDescription;
  }

  public void setNewLocation(String newLocation) {
    this.newLocation = newLocation;
  }

}

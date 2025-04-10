package calendarapp.factory;

import java.time.ZonedDateTime;

public class EditInput {
  private String property;
  private String eventName;
  private ZonedDateTime fromStart;
  private ZonedDateTime fromEnd;
  private String newValue;
  private boolean isRecurring;


  public EditInput(String property, String eventName, ZonedDateTime fromStart,
                   ZonedDateTime fromEnd, String newValue, boolean isRecurring) {
    this.property = property;
    this.eventName = eventName;
    this.fromStart = fromStart;
    this.fromEnd = fromEnd;
    this.newValue = newValue;
    this.isRecurring = isRecurring;
  }

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

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public void setRecurring(boolean recurring) {
    isRecurring = recurring;
  }

}

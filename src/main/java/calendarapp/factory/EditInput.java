package calendarapp.factory;

import java.time.ZonedDateTime;

/**
 * Represents the input required to edit an event in the calendar system.
 * This class encapsulates the details of an event being modified, such as the
 * event's name, the property being changed, the start and end times, the new
 * value for the property, and whether the event is recurring.
 */
public class EditInput {
  private String property;
  private String eventName;
  private ZonedDateTime fromStart;
  private ZonedDateTime fromEnd;
  private String newValue;
  private boolean isRecurring;

  /**
   * Constructs an EditInput object with the specified event details.
   *
   * @param property    the property of the event to be edited
   * @param eventName   the name of the event
   * @param fromStart   the start time of the event (null if not time-based)
   * @param fromEnd     the end time of the event (null if not time-based)
   * @param newValue    the new value for the event property
   * @param isRecurring indicates if the event is recurring
   */
  public EditInput(String property, String eventName, ZonedDateTime fromStart,
                   ZonedDateTime fromEnd, String newValue, boolean isRecurring) {
    this.property = property;
    this.eventName = eventName;
    this.fromStart = fromStart;
    this.fromEnd = fromEnd;
    this.newValue = newValue;
    this.isRecurring = isRecurring;
  }

  /**
   * Constructs an EditInput object for properties not related to time.
   *
   * @param property    the property to be edited
   * @param eventName   the name of the event
   * @param newValue    the new value for the event property
   * @param isRecurring indicates if the event is recurring
   */
  public EditInput(String property, String eventName, String newValue, boolean isRecurring) {
    this(property, eventName, null, null, newValue, isRecurring);
  }

  /**
   * Gets the property being edited.
   *
   * @return the property to be edited
   */
  public String getProperty() {
    return property;
  }

  /**
   * Gets the name of the event being edited.
   *
   * @return the event's name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time, or null if not time-based
   */
  public ZonedDateTime getFromStart() {
    return fromStart;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time, or null if not time-based
   */
  public ZonedDateTime getFromEnd() {
    return fromEnd;
  }

  /**
   * Gets the new value to be applied to the property.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Checks if the event is recurring.
   *
   * @return true if recurring, false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Sets the property to be edited.
   *
   * @param property the property to be edited
   */
  public void setProperty(String property) {
    this.property = property;
  }

  /**
   * Sets the event name.
   *
   * @param eventName the event's name
   */
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }


  /**
   * Sets the new value for the event property.
   *
   * @param newValue the new value for the property
   */
  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  /**
   * Sets whether the event is recurring.
   *
   * @param recurring true if the event is recurring, false otherwise
   */
  public void setRecurring(boolean recurring) {
    isRecurring = recurring;
  }

}

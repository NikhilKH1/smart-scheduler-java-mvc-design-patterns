package calendarapp.controller;

import java.time.temporal.Temporal;

/**
 * Represents the properties related to a recurring event.
 * This class holds information about whether the event is recurring,
 * the weekdays on which it occurs, the repeat count, the repeat until date,
 * and an index value for identifying the recurring event.
 */
public class ParsedRecurringEvent {

  protected boolean isRecurring;
  protected String weekdays;
  protected int repeatCount;
  protected Temporal repeatUntil;
  protected int index;

  /**
   * Default constructor for initializing the RecurringResult instance.
   * Initializes the fields for isRecurring, weekdays, repeatCount, repeatUntil, and index.
   */
  public ParsedRecurringEvent() {
    this.isRecurring = false;
    this.weekdays = null;
    this.repeatCount = -1;
    this.repeatUntil = null;
    this.index = -1;
  }

  /**
   * Sets whether the event is recurring.
   *
   * @param recurring true if the event is recurring; false otherwise
   */
  public void setRecurring(boolean recurring) {
    this.isRecurring = recurring;
  }

  /**
   * Sets the weekdays on which the event occurs.
   *
   * @param weekdays a string representing the weekdays on which the event occurs
   */
  public void setWeekdays(String weekdays) {
    this.weekdays = weekdays;
  }

  /**
   * Sets the number of times the event should repeat.
   *
   * @param repeatCount the number of times the event should repeat
   */
  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * Sets the date until which the event should repeat.
   *
   * @param repeatUntil a Temporal object representing the repeat until date
   */
  public void setRepeatUntil(Temporal repeatUntil) {
    this.repeatUntil = repeatUntil;
  }

  /**
   * Sets the index value for identifying the recurring event.
   *
   * @param index the index value for the recurring event
   */
  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * Returns whether the event is recurring.
   *
   * @return true if the event is recurring; false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Returns the weekdays on which the event occurs.
   *
   * @return a string representing the weekdays on which the event occurs
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times the event should repeat.
   *
   * @return the number of times the event should repeat
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Returns the date until which the event should repeat.
   *
   * @return a Temporal object representing the repeat until date
   */
  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Returns the index value of the recurring event.
   *
   * @return the index value for identifying the recurring event
   */
  public int getIndex() {
    return index;
  }
}

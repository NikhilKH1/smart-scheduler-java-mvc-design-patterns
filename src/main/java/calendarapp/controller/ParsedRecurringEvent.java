package calendarapp.controller;

import java.time.temporal.Temporal;

/**
 * Represents the properties related to a recurring event.
 * This class holds information about whether the event is recurring,
 * the weekdays on which it occurs, the repeat count, the repeat until date,
 * and an index value for identifying the recurring event.
 */
public class ParsedRecurringEvent {

  protected boolean isRecurring = false;
  protected String weekdays = "";
  protected int repeatCount = 0;
  protected Temporal repeatUntil = null;
  protected int index;

  /**
   * Default constructor for initializing the RecurringResult instance.
   * Initializes the fields for isRecurring, weekdays, repeatCount, repeatUntil, and index.
   */
  public ParsedRecurringEvent() {
    this.isRecurring = false;
    this.weekdays = "";
    this.repeatCount = 0;
    this.repeatUntil = null;
    this.index = -1;
  }

  /**
   * Checks if the event is recurring.
   *
   * @return true if the event is recurring, otherwise false.
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Gets the weekdays on which the event repeats.
   *
   * @return a string representing the weekdays (e.g., "MTWRF" for Monday to Friday).
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Gets the repeat count for the recurring event.
   *
   * @return the number of times the event should repeat.
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Gets the date and time until which the recurring event repeats.
   *
   * @return the repeat until date, or null if there is no end date.
   */
  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Gets the index of the recurring event.
   *
   * @return the index of the event, used to identify it in a list or sequence.
   */
  public int getIndex() {
    return index;
  }
}

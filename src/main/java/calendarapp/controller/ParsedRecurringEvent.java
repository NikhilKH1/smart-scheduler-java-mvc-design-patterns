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
    this.weekdays = "";
    this.repeatCount = 0;
    this.repeatUntil = null;
    this.index = -1;
  }

  public void setRecurring(boolean recurring) {
    this.isRecurring = recurring;
  }

  public void setWeekdays(String weekdays) {
    this.weekdays = weekdays;
  }

  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  public void setRepeatUntil(Temporal repeatUntil) {
    this.repeatUntil = repeatUntil;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public String getWeekdays() {
    return weekdays;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  public int getIndex() {
    return index;
  }
}

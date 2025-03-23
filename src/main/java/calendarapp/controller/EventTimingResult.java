package calendarapp.controller;

import java.time.ZonedDateTime;

/**
 * Represents the timing details of an event.
 * This class holds information about the start time, end time,
 * whether the event is all-day, and an index value for identifying
 * the event in a list or sequence.
 */
public class EventTimingResult {

  ZonedDateTime start;
  ZonedDateTime end;
  boolean isAllDay;
  int index;

  /**
   * Default constructor for initializing the EventTimingResult instance.
   * Initializes the start, end, isAllDay, and index with default values.
   */
  public EventTimingResult() {
    this.start = start;
    this.end = end;
    this.isAllDay = isAllDay;
    this.index = index;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time as a {@link ZonedDateTime} object.
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time as a {@link ZonedDateTime} object.
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Checks whether the event is an all-day event.
   *
   * @return true if the event is an all-day event, false otherwise.
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Gets the index of the event.
   *
   * @return the index of the event, used to identify the event in a list or sequence.
   */
  public int getIndex() {
    return index;
  }
}

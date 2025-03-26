package calendarapp.controller;

import java.time.temporal.Temporal;

/**
 * Represents the timing details of an event.
 * This class holds information about the start time, end time,
 * whether the event is all-day, and an index value for identifying
 * the event in a list or sequence.
 */
public class ParsedEventTiming {

  Temporal start;
  Temporal end;
  boolean isAllDay;
  int index;

  /**
   * Default constructor initializing fields to null/default.
   */
  public ParsedEventTiming() {
    this.start = null;
    this.end = null;
    this.isAllDay = false;
    this.index = -1;
  }

  /**
   * Sets the start time.
   *
   * @param start the start time as a Temporal object
   */
  public void setStart(Temporal start) {
    this.start = start;
  }

  /**
   * Sets the end time.
   *
   * @param end the end time as a Temporal object
   */
  public void setEnd(Temporal end) {
    this.end = end;
  }

  /**
   * Sets whether the event is all day.
   *
   * @param isAllDay true if all-day, false otherwise
   */
  public void setAllDay(boolean isAllDay) {
    this.isAllDay = isAllDay;
  }

  /**
   * Sets the index.
   *
   * @param index the index
   */
  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * Gets the start time.
   *
   * @return the start time as a Temporal
   */
  public Temporal getStart() {
    return start;
  }

  /**
   * Gets the end time.
   *
   * @return the end time as a Temporal
   */
  public Temporal getEnd() {
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

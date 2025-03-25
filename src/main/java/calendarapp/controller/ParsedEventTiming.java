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
   * @param start the start time as a {@link Temporal} object
   */
  public void setStart(Temporal start) {
    this.start = start;
  }

  /**
   * Sets the end time.
   *
   * @param end the end time as a {@link Temporal} object
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
   * @return the start time as a {@link Temporal}
   */
  public Temporal getStart() {
    return start;
  }

  /**
   * Gets the end time.
   *
   * @return the end time as a {@link Temporal}
   */
  public Temporal getEnd() {
    return end;
  }

  /**
   * Returns whether it's all day.
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Gets index.
   */
  public int getIndex() {
    return index;
  }
}

package calendarapp.controller;

import java.time.LocalDateTime;

public class EventTimingResult {
  LocalDateTime start;
  LocalDateTime end;
  boolean isAllDay;
  int index;

  public EventTimingResult() {
    this.start = start;
    this.end = end;
    this.isAllDay = isAllDay;
    this.index = index;
  }

  public LocalDateTime getStart() { return start; }
  public LocalDateTime getEnd() { return end; }
  public boolean isAllDay() { return isAllDay; }
  public int getIndex() { return index; }
}

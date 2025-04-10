package calendarapp.model.event;

import java.time.ZonedDateTime;

public interface ReadOnlyCalendarEvent {
  String getSubject();
  String getDescription();
  String getLocation();
  ZonedDateTime getStartDateTime();
  ZonedDateTime getEndDateTime();

  boolean isRecurring();
  String getWeekdays();
  ZonedDateTime RepeatUntil();
  Integer getRepeatCount();
  boolean isPublic();

  boolean isAllDay();
}

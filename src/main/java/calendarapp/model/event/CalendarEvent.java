package calendarapp.model.event;

import java.time.LocalDateTime;

public interface CalendarEvent {
  String getSubject();
  LocalDateTime getStartDateTime();
  LocalDateTime getEndDateTime();
  String getDescription();
  String getLocation();
  boolean isAllDay();
  boolean isPublic();
  }
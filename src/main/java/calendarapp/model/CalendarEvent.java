package calendarapp.model;

import java.time.LocalDateTime;

public interface CalendarEvent {
  String getSubject();
  LocalDateTime getStartDateTime();
  LocalDateTime getEndDateTime();


}

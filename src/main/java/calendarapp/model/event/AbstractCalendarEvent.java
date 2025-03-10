package calendarapp.model.event;

import java.time.LocalDateTime;

public abstract class AbstractCalendarEvent implements CalendarEvent {
  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;

  @Override
  public String getSubject() {
    return subject;
  }
  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }
  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }
}

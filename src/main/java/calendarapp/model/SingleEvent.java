package calendarapp.model;

import java.time.LocalDateTime;

public class SingleEvent implements CalendarEvent{
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;


  public SingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

}

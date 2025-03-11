package calendarapp.model.event;

import java.time.LocalDateTime;

public class SingleEvent extends AbstractCalendarEvent {

  private final String seriesId;

  public SingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String description, String location, boolean isPublic, boolean isAllDay, String seriesId) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
    this.seriesId = seriesId;
  }

  public String getSeriesId() {
    return seriesId;
  }

}

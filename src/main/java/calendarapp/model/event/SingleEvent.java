package calendarapp.model.event;

import java.time.LocalDateTime;

public class SingleEvent extends AbstractCalendarEvent {
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;
  private final String seriesId; // null if not recurring

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

  public String getDescription() {
    return description;
  }
  public String getLocation() {
    return location;
  }
  public boolean isPublic() {
    return isPublic;
  }
  public boolean isAllDay() {
    return isAllDay;
  }
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("- ").append(subject)
            .append(": ").append(startDateTime)
            .append(" to ").append(endDateTime);
    if (description != null && !description.isEmpty()) {
      sb.append(" | Description: ").append(description);
    }
    if (location != null && !location.isEmpty()) {
      sb.append(" | Location: ").append(location);
    }
    return sb.toString();
  }
}

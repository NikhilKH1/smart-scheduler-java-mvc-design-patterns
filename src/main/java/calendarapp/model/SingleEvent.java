package calendarapp.model;

import java.time.LocalDateTime;

public class SingleEvent implements CalendarEvent {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;

  public SingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String description, String location, boolean isPublic, boolean isAllDay) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
  }

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
    if (!isPublic) {
      sb.append(" | Visibility: Private");
    }
    if (isAllDay) {
      sb.append(" | (All Day Event)");
    }
    return sb.toString();
  }
}

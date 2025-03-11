package calendarapp.model.event;

import java.time.LocalDateTime;

public abstract class AbstractCalendarEvent implements CalendarEvent {
  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;
  protected String description;
  protected String location;
  protected boolean isPublic;
  protected boolean isAllDay;

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

    if (isPublic) {
      sb.append(" | ").append("Public");
    }
    else
      sb.append(" | ").append("Private");

    if (isAllDay) {
      sb.append(" | All Day Event");
    }

    return sb.toString();
  }
}

package calendarapp.model;

import java.time.LocalDateTime;

public class CreateEventCommand implements Command {
  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;

  public CreateEventCommand(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                            boolean autoDecline, String description, String location,
                            boolean isPublic, boolean isAllDay) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.autoDecline = autoDecline;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
  }

  public String getEventName() {
    return eventName;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public boolean isAutoDecline() {
    return autoDecline;
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
    return "CreateEventCommand{" +
            "eventName='" + eventName + '\'' +
            ", startDateTime=" + startDateTime +
            ", endDateTime=" + endDateTime +
            ", autoDecline=" + autoDecline +
            ", description='" + description + '\'' +
            ", location='" + location + '\'' +
            ", isPublic=" + isPublic +
            ", isAllDay=" + isAllDay +
            '}';
  }
}

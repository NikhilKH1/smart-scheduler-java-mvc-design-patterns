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
  private final boolean isRecurring;
  private final String weekdays;
  private final int repeatCount;
  private final LocalDateTime repeatUntil;

  public CreateEventCommand(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime,
                            boolean autoDecline, String description, String location,
                            boolean isPublic, boolean isAllDay,
                            boolean isRecurring, String weekdays, int repeatCount,
                            LocalDateTime repeatUntil) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.autoDecline = autoDecline;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
    this.isRecurring = isRecurring;
    this.weekdays = weekdays;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
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

  public boolean isRecurring() {
    return isRecurring;
  }

  public String getWeekdays() {
    return weekdays;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
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
            ", isRecurring=" + isRecurring +
            ", weekdays='" + weekdays + '\'' +
            ", repeatCount=" + repeatCount +
            ", repeatUntil=" + repeatUntil +
            '}';
  }
}
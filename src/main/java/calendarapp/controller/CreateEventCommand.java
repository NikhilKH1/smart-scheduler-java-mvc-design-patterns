package calendarapp.controller;

import java.time.LocalDateTime;

public class CreateEventCommand implements Command {
  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean autoDecline;


  public CreateEventCommand(String eventName, LocalDateTime startDateTime, LocalDateTime endDateTime, boolean autoDecline) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.autoDecline = autoDecline;
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

  @Override
  public String toString() {
    return "CreateEventCommand{" +
            "eventName='" + eventName + '\'' +
            ", startDateTime=" + startDateTime +
            ", endDateTime=" + endDateTime +
            ", autoDecline=" + autoDecline +
            '}';
  }
}



package calendarapp.model.commands;

import java.time.LocalDateTime;

public class QueryRangeDateTimeCommand implements Command {
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  public QueryRangeDateTimeCommand(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }
}

package calendarapp.model;

import java.time.LocalDateTime;

public class QueryRangeDateTimeCommand implements Command {
  private final LocalDateTime start;
  private final LocalDateTime end;

  public QueryRangeDateTimeCommand(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end = end;
  }

  public LocalDateTime getStartDateTime() {
    return start;
  }

  public LocalDateTime getEndDateTime() {
    return end;
  }
}

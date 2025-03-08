package calendarapp.model;

import java.time.LocalDateTime;

public class BusyQueryCommand implements Command {
  private final LocalDateTime queryTime;

  public BusyQueryCommand(LocalDateTime queryTime) {
    this.queryTime = queryTime;
  }

  public LocalDateTime getQueryTime() {
    return queryTime;
  }
}

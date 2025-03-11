package calendarapp.model.commands;

import java.time.LocalDateTime;

/**
 * Command to query whether the calendar is busy at a specified date and time.
 */
public class BusyQueryCommand implements Command {
  private final LocalDateTime queryTime;

  /**
   * Constructs a BusyQueryCommand with the given query time.
   *
   * @param queryTime the date and time at which to check for calendar conflicts
   */
  public BusyQueryCommand(LocalDateTime queryTime) {
    this.queryTime = queryTime;
  }

  /**
   * Returns the date and time for the busy query.
   *
   * @return the query time
   */
  public LocalDateTime getQueryTime() {
    return queryTime;
  }
}

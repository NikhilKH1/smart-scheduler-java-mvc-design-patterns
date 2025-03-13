package calendarapp.model.commands;

import java.time.LocalDateTime;

/**
 * Command to query calendar events within a specific date and time range.
 */
public class QueryRangeDateTimeCommand implements Command {
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;

  /**
   * Constructs a QueryRangeDateTimeCommand with the specified start and end date/time.
   *
   * @param startDateTime the beginning of the query range
   * @param endDateTime   the end of the query range
   */
  public QueryRangeDateTimeCommand(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  /**
   * Returns the start date and time of the query range.
   *
   * @return the start date and time
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the query range.
   *
   * @return the end date and time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }
}

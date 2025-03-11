package calendarapp.model.commands;

import java.time.LocalDate;

/**
 * Command to query calendar events for a specific date.
 */
public class QueryByDateCommand implements Command {
  private final LocalDate queryDate;

  /**
   * Constructs a QueryByDateCommand with the specified date.
   *
   * @param queryDate the date for which to retrieve calendar events
   */
  public QueryByDateCommand(LocalDate queryDate) {
    this.queryDate = queryDate;
  }

  /**
   * Returns the query date.
   *
   * @return the date for which events are being queried
   */
  public LocalDate getQueryDate() {
    return queryDate;
  }
}

package calendarapp.model;

import java.time.LocalDate;

public class QueryByDateCommand implements Command {
  private final LocalDate queryDate;

  public QueryByDateCommand(LocalDate queryDate) {
    this.queryDate = queryDate;
  }

  public LocalDate getQueryDate() {
    return queryDate;
  }
}

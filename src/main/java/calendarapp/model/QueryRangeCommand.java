package calendarapp.model;

import java.time.LocalDate;

public class QueryRangeCommand implements Command {
  private final LocalDate startDate;
  private final LocalDate endDate;

  public QueryRangeCommand(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }
}

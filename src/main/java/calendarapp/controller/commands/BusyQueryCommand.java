package calendarapp.controller.commands;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.ZoneId;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Command to query whether the calendar is busy at a specified date and time.
 */
public class BusyQueryCommand implements ICalendarModelCommand {
  private final Temporal queryTime;

  /**
   * Constructs a BusyQueryCommand with the given query time.
   *
   * @param queryTime the date and time at which to check for calendar conflicts
   */
  public BusyQueryCommand(Temporal queryTime) {
    this.queryTime = queryTime;
  }

  /**
   * Executes the busy query command by checking if the calendar has a conflict
   * at the specified query time, and displaying the result using the view.
   *
   * @param model the calendar model used for checking conflicts
   * @param view  the calendar view for displaying messages
   * @return true after executing the query
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    ZonedDateTime zonedQueryTime;
    if (queryTime instanceof ZonedDateTime) {
      zonedQueryTime = (ZonedDateTime) queryTime;
    } else {
      ZoneId zone = model.getTimezone();
      zonedQueryTime = ZonedDateTime.from(queryTime).withZoneSameInstant(zone);
    }

    boolean busy = model.isBusyAt(zonedQueryTime);
    if (busy) {
      view.displayMessage("Busy at " + zonedQueryTime);
    } else {
      view.displayMessage("Available at " + zonedQueryTime);
    }
    return true;
  }

  /**
   * Returns the date and time for the busy query.
   *
   * @return the query time
   */
  public Temporal getQueryTime() {
    return queryTime;
  }

}

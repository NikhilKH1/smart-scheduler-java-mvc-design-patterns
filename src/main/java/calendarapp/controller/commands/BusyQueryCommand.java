package calendarapp.controller.commands;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Command to query whether the calendar is busy at a specified date and time.
 */
public class BusyQueryCommand implements ICalendarModelCommand {
  private final ZonedDateTime queryTime;

  /**
   * Constructs a BusyQueryCommand with the given query time.
   *
   * @param queryTime the date and time at which to check for calendar conflicts
   */
  public BusyQueryCommand(ZonedDateTime queryTime) {
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
    boolean busy = model.isBusyAt(queryTime);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    String formattedTime = queryTime.format(formatter);

    if (busy) {
      view.displayMessage("Busy at " + formattedTime);
    } else {
      view.displayMessage("Available at " + formattedTime);
    }
    return true;
  }

  /**
   * Returns the date and time for the busy query.
   *
   * @return the query time
   */
  public ZonedDateTime getQueryTime() {
    return queryTime;
  }
}

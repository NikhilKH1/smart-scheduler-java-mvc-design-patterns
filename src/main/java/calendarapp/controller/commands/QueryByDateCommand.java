package calendarapp.controller.commands;

import java.time.LocalDate;
import java.util.List;

import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.view.ICalendarView;

/**
 * Command to query calendar events for a specific date.
 */
public class QueryByDateCommand implements CalendarModelCommand {
  private final LocalDate queryDate;
  /**
   * Processes a query-by-date command. It retrieves events on the specified date and displays
   * them in the view.
   *
   * @param model the calendar model used for checking conflicts
   * @param view the calendar view for displaying messages
   * @return true after processing the command
   */
  @Override
  public boolean execute(CalendarModel model, ICalendarView view) {
    List<CalendarEvent> events = model.getEventsOnDate(queryDate);
    if (events.isEmpty()) {
      view.displayMessage("No events found on " + queryDate);
    } else {
      view.displayMessage("Events on " + queryDate + ":");
      view.displayEvents(events);
    }
    return true;
  }

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

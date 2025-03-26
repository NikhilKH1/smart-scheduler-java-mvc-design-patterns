package calendarapp.controller.commands;

import java.time.temporal.Temporal;
import java.util.List;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.view.ICalendarView;

/**
 * Command to query calendar events within a specific date and time range.
 */
public class QueryRangeDateTimeCommand implements ICalendarModelCommand {
  private final Temporal startDateTime;
  private final Temporal endDateTime;

  /**
   * Constructs a QueryRangeDateTimeCommand with the specified start and end date/time.
   *
   * @param startDateTime the beginning of the query range
   * @param endDateTime   the end of the query range
   */
  public QueryRangeDateTimeCommand(Temporal startDateTime, Temporal endDateTime) {
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  /**
   * Processes a query range command. It retrieves events between the specified start and
   * end date-time and displays them in the view.
   *
   * @param model the calendar model used for checking conflicts
   * @param view  the calendar view for displaying messages
   * @return true after executing the query
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    List<ICalendarEvent> events = model.getEventsBetween(startDateTime, endDateTime);
    if (events.isEmpty()) {
      view.displayMessage("No events found from " + startDateTime + " to " + endDateTime);
    } else {
      view.displayMessage("Events from " + startDateTime + " to " + endDateTime + ":");
      view.displayEvents(events);
    }
    return true;
  }

  /**
   * Returns the start date and time of the query range.
   *
   * @return the start date and time
   */
  public Temporal getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the query range.
   *
   * @return the end date and time
   */
  public Temporal getEndDateTime() {
    return endDateTime;
  }
}

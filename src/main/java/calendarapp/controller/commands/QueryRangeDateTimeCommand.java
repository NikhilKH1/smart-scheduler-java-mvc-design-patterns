package calendarapp.controller.commands;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;

/**
 * Command to query calendar events within a specific date and time range.
 */
public class QueryRangeDateTimeCommand implements ICalendarModelCommand {
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;

  /**
   * Constructs a QueryRangeDateTimeCommand with the specified start and end date/time.
   *
   * @param startDateTime the beginning of the query range
   * @param endDateTime   the end of the query range
   */
  public QueryRangeDateTimeCommand(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
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
    List<ReadOnlyCalendarEvent> events = model.getEventsBetween(startDateTime, endDateTime);
    if (events.isEmpty()) {
      if (!(view instanceof calendarapp.view.CalendarGUIView)) {
        view.displayMessage("No events found from " + startDateTime + " to " + endDateTime);
      } else {
        view.displayEvents(events);
      }
    } else {
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

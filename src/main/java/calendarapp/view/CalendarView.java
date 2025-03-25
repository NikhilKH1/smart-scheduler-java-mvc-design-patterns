package calendarapp.view;

import calendarapp.model.event.ICalendarEvent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This class implements the view for the calendar application.
 * It provides methods to display a list of events, normal messages, and error messages.
 */
public class CalendarView implements ICalendarView {

  private static final DateTimeFormatter FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

  /**
   * Displays the provided list of calendar events.
   * If the list is empty, a message indicating no events were found is printed.
   *
   * @param events the list of calendar events to display
   */
  @Override
  public void displayEvents(List<ICalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (ICalendarEvent event : events) {
      System.out.println(formatEventDetails(event));
    }
  }

  /**
   * Formats an event's details for display.
   *
   * @param event the event to format
   * @return a formatted string representing the event
   */
  private String formatEventDetails(ICalendarEvent event) {
    StringBuilder sb = new StringBuilder();

    ZonedDateTime start = ZonedDateTime.from(event.getStartDateTime());
    ZonedDateTime end = ZonedDateTime.from(event.getEndDateTime());

    sb.append("- ").append(event.getSubject())
            .append(": ").append(start.format(FORMATTER))
            .append(" to ").append(end.format(FORMATTER));

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append(" | Description: ").append(event.getDescription());
    }
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" | Location: ").append(event.getLocation());
    }

    if (event.isPublic()) {
      sb.append(" | Public");
    }

    if (event.isAllDay()) {
      sb.append(" | All Day Event");
    }

    return sb.toString();
  }

  /**
   * Displays a normal message to the standard output.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message to the error output.
   *
   * @param errorMessage the error message to display
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println(errorMessage);
  }
}

package calendarapp.view;

import calendarapp.model.event.CalendarEvent;

import java.util.List;

/**
 * This class implements the view for the calendar application.
 * It provides methods to display a list of events, normal messages, and error messages.
 */
public class CalendarView implements ICalendarView {

  /**
   * Displays the provided list of calendar events.
   * If the list is empty, a message indicating no events were found is printed.
   *
   * @param events the list of calendar events to display
   */
  @Override
  public void displayEvents(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (CalendarEvent event : events) {
      System.out.println(event.toString());
    }
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

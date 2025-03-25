package calendarapp.view;

import calendarapp.model.event.ICalendarEvent;

import java.util.List;

/**
 * This interface defines the methods required for a calendar view.
 * Implementations of this interface provide ways to display events, messages, and errors.
 */
public interface ICalendarView {

  /**
   * Displays the provided list of calendar events.
   *
   * @param events the list of calendar events to display
   */
  public void displayEvents(List<ICalendarEvent> events);

  /**
   * Displays a normal message.
   *
   * @param message the message to display
   */
  public void displayMessage(String message);

  /**
   * Displays an error message.
   *
   * @param errorMessage the error message to display
   */
  public void displayError(String errorMessage);
}

package calendarapp.view;

import calendarapp.model.event.ReadOnlyCalendarEvent;
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
  void displayEvents(List<ReadOnlyCalendarEvent> events);

  /**
   * Displays a normal message.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message.
   *
   * @param errorMessage the error message to display
   */
  void displayError(String errorMessage);

  /**
   * Runs the main loop of the view (for CLI/headless views).
   */
  void run();

  /**
   * Injects the input stream (Readable) for receiving commands.
   * @param in the input source
   */
  default void setInput(Readable in) {
  }

  /**
   * Injects the output stream (Appendable) for writing responses.
   * @param out the output destination
   */
  default void setOutput(Appendable out) {
  }
}

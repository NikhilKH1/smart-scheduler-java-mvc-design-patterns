package calendarapp.controller;

/**
 * The ICalendarController interface defines the method required to process a single command input.
 */
public interface ICalendarController {
  /**
   * Processes a command input.
   *
   * @param commandInput the command text to process
   * @return true if the command was processed successfully; false otherwise
   */
  boolean processCommand(String commandInput);
}

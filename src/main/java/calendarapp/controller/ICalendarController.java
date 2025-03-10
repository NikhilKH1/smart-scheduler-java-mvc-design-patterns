package calendarapp.controller;

public interface ICalendarController {
  /**
   * Processes a single command input.
   * @param commandInput The command text.
   * @return true if the command was processed successfully; false otherwise.
   */
  boolean processCommand(String commandInput);
}

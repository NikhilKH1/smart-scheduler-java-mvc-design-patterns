package calendarapp.controller;

import calendarapp.view.ICalendarView;

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
  public boolean processCommand(String commandInput);

  void run(String[] args);

  void setView(ICalendarView view);
}

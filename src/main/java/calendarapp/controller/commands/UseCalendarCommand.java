package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Command to set a specific calendar as the active calendar.
 */
public class UseCalendarCommand implements ICalendarManagerCommand {
  private final String calendarName;

  /**
   * Constructs a UseCalendarCommand to activate the calendar with the specified name.
   *
   * @param calendarName the name of the calendar to activate
   * @throws IllegalArgumentException if the calendar name is null or empty
   */
  public UseCalendarCommand(String calendarName) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    this.calendarName = calendarName;
  }

  /**
   * Executes the command to set the specified calendar as the active calendar.
   * The command attempts to find the calendar by name and set it as active.
   * If the calendar exists, it displays the events of that calendar; if the calendar is empty,
   * it shows a message stating so.
   * If the calendar cannot be found, an error message is displayed.
   *
   * @param calendarManager the calendar manager that manages multiple calendars
   * @param view            the view used to display messages and events
   * @return true if the calendar was successfully set as active, false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    boolean success = calendarManager.useCalendar(calendarName);
    if (success) {
      view.displayMessage("Using calendar: " + calendarName);
      if (calendarManager.getActiveCalendar().getEvents().isEmpty()) {
        view.displayMessage("No events found in calendar " + calendarName);
      } else {
        view.displayEvents(calendarManager.getActiveCalendar().getEvents());
      }
    } else {
      view.displayError("Calendar not found: " + calendarName);
    }
    return success;
  }
}

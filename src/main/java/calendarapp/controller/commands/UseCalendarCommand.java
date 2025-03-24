package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

public class UseCalendarCommand implements ICalendarManagerCommand {
  private final String calendarName;

  public UseCalendarCommand(String calendarName) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    this.calendarName = calendarName;
  }

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

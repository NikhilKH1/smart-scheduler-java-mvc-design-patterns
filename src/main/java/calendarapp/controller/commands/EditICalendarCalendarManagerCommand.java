package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

public class EditICalendarCalendarManagerCommand implements ICalendarManagerCommand {
  private final String calendarName;
  private final String property;
  private final String newValue;

  public EditICalendarCalendarManagerCommand(String calendarName, String property, String newValue) {
    if (calendarName == null || property == null || newValue == null ||
            calendarName.trim().isEmpty() || property.trim().isEmpty() || newValue.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name, property, or value cannot be empty.");
    }
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    try {
      boolean success = calendarManager.editCalendar(calendarName, property, newValue);
      if (success) {
        view.displayMessage("Calendar updated: " + calendarName);
      }
      return success;
    } catch (Exception e) {
      view.displayError("Edit calendar failed: " + e.getMessage());
      return false;
    }
  }
}

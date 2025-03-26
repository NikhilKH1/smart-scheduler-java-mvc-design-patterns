package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Command to edit a calendar's properties.
 */
public class EditCalendarCommand implements ICalendarManagerCommand {
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param calendarName the name of the calendar to edit
   * @param property     the property to modify
   * @param newValue     the new value for the property
   * @throws IllegalArgumentException if any parameter is null or empty
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    if (calendarName == null || property == null || newValue == null ||
            calendarName.trim().isEmpty() || property.trim().isEmpty()
            || newValue.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name, property, or value cannot be empty.");
    }
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  /**
   * Edits the specified property of a calendar.
   * If the edit is successful, a success message is displayed.
   * If an error occurs, an error message is displayed.
   *
   * @param calendarManager the calendar manager used to modify the calendar
   * @param view            the view used to display messages
   * @return true if the edit was successful, false otherwise
   */
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

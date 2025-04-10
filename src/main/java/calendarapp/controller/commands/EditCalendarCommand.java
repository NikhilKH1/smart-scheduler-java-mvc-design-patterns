package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Command to edit properties of a calendar such as name or description.
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
   * @throws IllegalArgumentException if any parameter is null or blank
   */
  public EditCalendarCommand(String calendarName, String property, String newValue) {
    if (isNullOrBlank(calendarName) || isNullOrBlank(property) || isNullOrBlank(newValue)) {
      throw new IllegalArgumentException("Calendar name, property, or new value cannot "
              + "be null or blank.");
    }
    this.calendarName = calendarName;
    this.property = property;
    this.newValue = newValue;
  }

  /**
   * Executes the command to edit a calendar's property (such as name or description)
   * in the calendar manager. Displays appropriate messages in the view based on the result.
   *
   * @param calendarManager the calendar manager to perform the edit operation on
   * @param view            the view to display success or error messages
   * @return true if the property was successfully updated; false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    try {
      boolean success = calendarManager.editCalendar(calendarName, property, newValue);
      if (success) {
        view.displayMessage("Calendar updated: " + calendarName);
      } else {
        view.displayError("No calendar found or property change failed for: "
                + calendarName);
      }
      return success;
    } catch (Exception e) {
      view.displayError("Failed to edit calendar '" + calendarName + "': "
              + e.getMessage());
      return false;
    }
  }

  /**
   * Utility method to check if a string is null or blank.
   *
   * @param str the string to check
   * @return true if the string is null, empty, or consists only of whitespace;
   *         false otherwise
   */
  private boolean isNullOrBlank(String str) {
    return str == null || str.trim().isEmpty();
  }
}

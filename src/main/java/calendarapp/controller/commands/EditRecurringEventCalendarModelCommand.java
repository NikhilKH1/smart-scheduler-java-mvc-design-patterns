package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Command to edit properties of a recurring event.
 * This command is used to update recurring-specific properties such as repeat count,
 * repeat-until date, repeating days, description, or location.
 */
public class EditRecurringEventCalendarModelCommand implements ICalendarModelCommand {
  private final String property;
  private final String eventName;
  private final String newValue;

  /**
   * Processes an edit recurring event command. It updates the recurring event properties.
   *
   * @param model the calendar model used for checking conflicts
   * @param view  the calendar view for displaying messages
   * @return true after executing the query
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = model.editRecurringEvent(eventName, property, newValue);
    if (success) {
      view.displayMessage("Recurring event modified successfully.");
    } else {
      view.displayError("Failed to modify recurring event.");
    }
    return success;
  }


  /**
   * Constructs an EditRecurringEventCommand with the specified recurring property update.
   *
   * @param property  the recurring property to update
   * @param eventName the name of the recurring event
   * @param newValue  the new value for the recurring property
   */
  public EditRecurringEventCalendarModelCommand(String property, String eventName, String newValue) {
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
  }

  /**
   * Returns the recurring property to be updated.
   *
   * @return the property name
   */
  public String getProperty() {
    return property;
  }

  /**
   * Returns the name of the recurring event.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Returns the new value for the recurring property.
   *
   * @return the new property value
   */
  public String getNewValue() {
    return newValue;
  }
}

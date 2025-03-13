package calendarapp.model.commands;

/**
 * Command to edit properties of a recurring event.
 * This command is used to update recurring-specific properties such as repeat count,
 * repeat-until date, repeating days, description, or location.
 */
public class EditRecurringEventCommand implements Command {
  private final String property;
  private final String eventName;
  private final String newValue;

  /**
   * Constructs an EditRecurringEventCommand with the specified recurring property update.
   *
   * @param property  the recurring property to update
   * @param eventName the name of the recurring event
   * @param newValue  the new value for the recurring property
   */
  public EditRecurringEventCommand(String property, String eventName, String newValue) {
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

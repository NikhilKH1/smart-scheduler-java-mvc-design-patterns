package calendarapp.model.commands;

public class EditRecurringEventCommand implements Command {
  private final String property;
  private final String eventName;
  private final String newValue;

  public EditRecurringEventCommand(String property, String eventName, String newValue) {
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
  }

  public String getProperty() {
    return property;
  }

  public String getEventName() {
    return eventName;
  }

  public String getNewValue() {
    return newValue;
  }
}

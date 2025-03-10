package calendarapp.model.commands;

import java.time.LocalDateTime;

public class EditEventCommand implements Command {
  public enum EditMode {
    SINGLE,
    FROM,
    ALL
  }

  private final EditMode mode;
  private final String property;
  private final String eventName;
  private final LocalDateTime originalStart;
  private final LocalDateTime originalEnd;
  private final LocalDateTime filterDateTime;
  private final String newValue;

  // Constructor for editing a single event (SINGLE mode)
  public EditEventCommand(String property, String eventName, LocalDateTime originalStart, LocalDateTime originalEnd, String newValue) {
    this.mode = EditMode.SINGLE;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = originalStart;
    this.originalEnd = originalEnd;
    this.newValue = newValue;
    this.filterDateTime = null;
  }

  // Constructor for editing events from a certain time onward (FROM mode)
  public EditEventCommand(String property, String eventName, LocalDateTime filterDateTime, String newValue) {
    this.mode = EditMode.FROM;
    this.property = property;
    this.eventName = eventName;
    this.filterDateTime = filterDateTime;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
  }

  // Constructor for editing all events with the same event name (ALL mode)
  public EditEventCommand(String property, String eventName, String newValue) {
    this.mode = EditMode.ALL;
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
    this.filterDateTime = null;
  }

  public EditMode getMode() {
    return mode;
  }

  public String getProperty() {
    return property;
  }

  public String getEventName() {
    return eventName;
  }

  public LocalDateTime getOriginalStart() {
    return originalStart;
  }

  public LocalDateTime getOriginalEnd() {
    return originalEnd;
  }

  public LocalDateTime getFilterDateTime() {
    return filterDateTime;
  }

  public String getNewValue() {
    return newValue;
  }
}

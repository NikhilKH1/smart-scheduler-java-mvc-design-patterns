package calendarapp.model;

import java.time.LocalDateTime;

public class EditEventCommand implements Command {
  private final EditMode mode;
  private final String property;
  private final String eventName;
  // For SINGLE mode:
  private final LocalDateTime originalStart;
  private final LocalDateTime originalEnd;
  // For FROM mode:
  private final LocalDateTime filterDateTime;
  // For all modes:
  private final String newValue;

  // Constructor for SINGLE mode.
  public EditEventCommand(String property, String eventName, LocalDateTime originalStart, LocalDateTime originalEnd, String newValue) {
    this.mode = EditMode.SINGLE;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = originalStart;
    this.originalEnd = originalEnd;
    this.filterDateTime = null;
    this.newValue = newValue;
  }

  // Constructor for FROM mode.
  public EditEventCommand(String property, String eventName, LocalDateTime filterDateTime, String newValue, boolean dummy) {
    this.mode = EditMode.FROM;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = null;
    this.originalEnd = null;
    this.filterDateTime = filterDateTime;
    this.newValue = newValue;
  }

  // Constructor for ALL mode.
  public EditEventCommand(String property, String eventName, String newValue) {
    this.mode = EditMode.ALL;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = null;
    this.originalEnd = null;
    this.filterDateTime = null;
    this.newValue = newValue;
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

package calendarapp.model.commands;

import java.time.LocalDateTime;

/**
 * Command to edit an existing calendar event.
 * This command supports editing a single occurrence, all occurrences from a
 * specific date and time, or all occurrences of an event.
 */
public class EditEventCommand implements Command {
  /**
   * Enum representing the mode of editing.
   */
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

  /**
   * Constructs an EditEventCommand to edit a single occurrence of an event.
   *
   * @param property      the property to update
   * @param eventName     the name of the event
   * @param originalStart the original start date and time of the event
   * @param originalEnd   the original end date and time of the event
   * @param newValue      the new value for the specified property
   */
  public EditEventCommand(String property, String eventName, LocalDateTime originalStart,
                          LocalDateTime originalEnd, String newValue) {
    this.mode = EditMode.SINGLE;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = originalStart;
    this.originalEnd = originalEnd;
    this.newValue = newValue;
    this.filterDateTime = null;
  }

  /**
   * Constructs an EditEventCommand to edit all events from a specific date and time onward.
   *
   * @param property       the property to update
   * @param eventName      the name of the event
   * @param filterDateTime the date and time from which the edit should be applied
   * @param newValue       the new value for the specified property
   */
  public EditEventCommand(String property, String eventName, LocalDateTime filterDateTime,
                          String newValue) {
    this.mode = EditMode.FROM;
    this.property = property;
    this.eventName = eventName;
    this.filterDateTime = filterDateTime;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
  }

  /**
   * Constructs an EditEventCommand to edit all occurrences of an event.
   *
   * @param property  the property to update
   * @param eventName the name of the event
   * @param newValue  the new value for the specified property
   */
  public EditEventCommand(String property, String eventName, String newValue) {
    this.mode = EditMode.ALL;
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
    this.filterDateTime = null;
  }

  /**
   * Returns the mode in which the event is to be edited.
   *
   * @return the edit mode (SINGLE, FROM, or ALL)
   */
  public EditMode getMode() {
    return mode;
  }

  /**
   * Returns the property that is to be updated.
   *
   * @return the property name
   */
  public String getProperty() {
    return property;
  }

  /**
   * Returns the name of the event to be edited.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Returns the original start date and time of the event (for single occurrence edits).
   *
   * @return the original start date and time, or null if not applicable
   */
  public LocalDateTime getOriginalStart() {
    return originalStart;
  }

  /**
   * Returns the original end date and time of the event (for single occurrence edits).
   *
   * @return the original end date and time, or null if not applicable
   */
  public LocalDateTime getOriginalEnd() {
    return originalEnd;
  }

  /**
   * Returns the filter date and time used for editing events from a specific point onward.
   *
   * @return the filter date and time, or null if not applicable
   */
  public LocalDateTime getFilterDateTime() {
    return filterDateTime;
  }

  /**
   * Returns the new value for the updated property.
   *
   * @return the new property value
   */
  public String getNewValue() {
    return newValue;
  }
}

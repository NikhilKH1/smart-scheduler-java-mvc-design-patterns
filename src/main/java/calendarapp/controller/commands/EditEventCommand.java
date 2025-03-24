package calendarapp.controller.commands;

import java.time.ZonedDateTime;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Command to edit an existing calendar event.
 * This command supports editing a single occurrence, all occurrences from a
 * specific date and time, or all occurrences of an event.
 */
public class EditEventCommand implements ICalendarModelCommand {
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
  private final ZonedDateTime originalStart;
  private final ZonedDateTime originalEnd;
  private final ZonedDateTime filterDateTime;
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
  public EditEventCommand(String property, String eventName, ZonedDateTime originalStart,
                          ZonedDateTime originalEnd, String newValue) {
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
  public EditEventCommand(String property, String eventName, ZonedDateTime filterDateTime,
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
   * Processes an edit event command. Depending on the mode specified (SINGLE, FROM, or ALL),
   * it attempts to edit a single event, events from a specific date-time, or all events matching
   * the event name.
   *
   * @param model the calendar model used for checking conflicts
   * @param view the calendar view for displaying messages
   * @return true if the event(s) were edited successfully; false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = false;
    String propertyLower = property.toLowerCase();

    try {
      switch (mode) {
        case SINGLE:
          String processedValue = newValue;
          if (propertyLower.equals("startdatetime") || propertyLower.equals("enddatetime")) {
            ZonedDateTime zonedNewValue = ZonedDateTime.of(java.time.LocalDateTime.parse(newValue),
                    model.getTimezone());
            processedValue = zonedNewValue.toString();
          }
          success = model.editSingleEvent(property, eventName, originalStart, originalEnd, processedValue);
          break;
        case FROM:
          processedValue = newValue;
          if (propertyLower.equals("startdatetime") || propertyLower.equals("enddatetime")) {
            ZonedDateTime zonedNewValue = ZonedDateTime.of(java.time.LocalDateTime.parse(newValue),
                    model.getTimezone());
            processedValue = zonedNewValue.toString();
          }
          success = model.editEventsFrom(property, eventName, filterDateTime, processedValue);
          break;
        case ALL:
          success = model.editEventsAll(property, eventName, newValue);
          break;
      }
    } catch (Exception e) {
      view.displayError("Error while editing event(s): " + e.getMessage());
      return false;
    }

    if (success) {
      view.displayMessage("Event(s) edited successfully");
    } else {
      view.displayError("Failed to edit event(s)");
    }
    return success;
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
  public ZonedDateTime getOriginalStart() {
    return originalStart;
  }

  /**
   * Returns the original end date and time of the event (for single occurrence edits).
   *
   * @return the original end date and time, or null if not applicable
   */
  public ZonedDateTime getOriginalEnd() {
    return originalEnd;
  }

  /**
   * Returns the filter date and time used for editing events from a specific point onward.
   *
   * @return the filter date and time, or null if not applicable
   */
  public ZonedDateTime getFilterDateTime() {
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
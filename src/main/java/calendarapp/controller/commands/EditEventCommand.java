package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;

/**
 * Command to edit an existing calendar event.
 * Supports editing a single occurrence, all occurrences from a specific date-time,
 * or all occurrences of an event.
 */
public class EditEventCommand implements ICalendarModelCommand {

  /**
   * Represents the edit mode: editing a single occurrence,
   * all occurrences from a point onward, or all occurrences.
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
   * Constructs a command to edit a single event occurrence.
   *
   * @param property      the property to be edited (e.g., startDateTime, location)
   * @param eventName     the name of the event
   * @param originalStart the original start time of the event
   * @param originalEnd   the original end time of the event
   * @param newValue      the new value for the property
   */
  public EditEventCommand(String property, String eventName,
                          ZonedDateTime originalStart, ZonedDateTime originalEnd,
                          String newValue) {
    this.mode = EditMode.SINGLE;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = originalStart;
    this.originalEnd = originalEnd;
    this.newValue = newValue;
    this.filterDateTime = null;
  }

  /**
   * Constructs a command to edit all occurrences of an event from a specific date and time.
   *
   * @param property       the property to be edited
   * @param eventName      the name of the event
   * @param filterDateTime the date and time from which edits should apply
   * @param newValue       the new value for the property
   */
  public EditEventCommand(String property, String eventName,
                          ZonedDateTime filterDateTime, String newValue) {
    this.mode = EditMode.FROM;
    this.property = property;
    this.eventName = eventName;
    this.filterDateTime = filterDateTime;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
  }

  /**
   * Constructs a command to edit all occurrences of an event.
   *
   * @param property  the property to be edited
   * @param eventName the name of the event
   * @param newValue  the new value for the property
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
   * Executes the command using the provided calendar model and view.
   * Applies the appropriate editing logic based on the selected mode.
   *
   * @param model the calendar model
   * @param view  the calendar view for displaying messages or errors
   * @return true if the edit was successful, false otherwise
   */

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = false;
    ZoneId zone = model.getTimezone();
    String propLower = property.toLowerCase();

    try {
      switch (mode) {
        case SINGLE:
          success = model.editSingleEvent(
                  property,
                  eventName,
                  originalStart.withZoneSameInstant(zone),
                  originalEnd.withZoneSameInstant(zone),
                  convertValueIfDateTime(propLower, newValue, zone)
          );
          break;

        case FROM:
          success = model.editEventsFrom(
                  property,
                  eventName,
                  filterDateTime.withZoneSameInstant(zone),
                  convertValueIfDateTime(propLower, newValue, zone)
          );
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
   * Converts the value to a date-time string if the property being edited is a date-time.
   *
   * @param property the name of the property
   * @param value    the value to be converted
   * @param zone     the time zone for conversion
   * @return the converted value if it's a date-time property, otherwise the original value
   */
  private String convertValueIfDateTime(String property, String value, ZoneId zone) {
    if ("startdatetime".equals(property) || "enddatetime".equals(property)) {
      return ZonedDateTime.of(LocalDateTime.parse(value), zone).toString();
    }
    return value;
  }

  /**
   * Returns the edit mode used in this command.
   *
   * @return the edit mode
   */
  public EditMode getMode() {
    return mode;
  }

  /**
   * Returns the property to be edited.
   *
   * @return the property name
   */
  public String getProperty() {
    return property;
  }

  /**
   * Returns the name of the event being edited.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Returns the original start time of the event occurrence.
   *
   * @return the original start time, or null if not applicable
   */
  public ZonedDateTime getOriginalStart() {
    return originalStart;
  }

  /**
   * Returns the original end time of the event occurrence.
   *
   * @return the original end time, or null if not applicable
   */
  public ZonedDateTime getOriginalEnd() {
    return originalEnd;
  }

  /**
   * Returns the filter date-time used for 'FROM' mode editing.
   *
   * @return the filter date-time, or null if not applicable
   */
  public ZonedDateTime getFilterDateTime() {
    return filterDateTime;
  }

  /**
   * Returns the new value to be applied to the property.
   *
   * @return the new property value
   */
  public String getNewValue() {
    return newValue;
  }
}

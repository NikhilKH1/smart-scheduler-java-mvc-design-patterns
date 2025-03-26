package calendarapp.controller.commands;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Command to edit an existing calendar event.
 * Supports editing a single occurrence, all occurrences from a specific date-time,
 * or all occurrences of an event.
 */
public class EditEventCommand implements ICalendarModelCommand {

  public enum EditMode {
    SINGLE,
    FROM,
    ALL
  }

  private final EditMode mode;
  private final String property;
  private final String eventName;
  private final Temporal originalStart;
  private final Temporal originalEnd;
  private final Temporal filterDateTime;
  private final String newValue;

  /**
   * Constructs an EditEventCommand in SINGLE mode.
   *
   * @param property      the property to edit
   * @param eventName     the name of the event
   * @param originalStart the original start date and time of the event
   * @param originalEnd   the original end date and time of the event
   * @param newValue      the new value for the property
   */
  public EditEventCommand(String property, String eventName, Temporal originalStart,
                          Temporal originalEnd, String newValue) {
    this.mode = EditMode.SINGLE;
    this.property = property;
    this.eventName = eventName;
    this.originalStart = originalStart;
    this.originalEnd = originalEnd;
    this.newValue = newValue;
    this.filterDateTime = null;
  }

  /**
   * Constructs an EditEventCommand in FROM mode.
   *
   * @param property       the property to edit
   * @param eventName      the name of the event
   * @param filterDateTime the date-time from which edits should be applied
   * @param newValue       the new value for the property
   */
  public EditEventCommand(String property, String eventName, Temporal filterDateTime,
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
   * Constructs an EditEventCommand in ALL mode.
   *
   * @param property  the property to edit
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
   * Executes the event editing command.
   * Based on the mode, it edits a single occurrence, all occurrences from a specific time,
   * or all occurrences of the event.
   *
   * @param model the calendar model where the event is edited
   * @param view  the view used to display messages
   * @return true if the event was edited successfully, false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = false;
    String propertyLower = property.toLowerCase();

    try {
      switch (mode) {
        case SINGLE:
          String processedSingleValue = processTemporalValue(propertyLower, newValue,
                  model.getTimezone());
          success = model.editSingleEvent(property, eventName,
                  convertToZonedDateTime(originalStart, model.getTimezone()),
                  convertToZonedDateTime(originalEnd, model.getTimezone()),
                  processedSingleValue);
          break;
        case FROM:
          String processedFromValue = processTemporalValue(propertyLower, newValue,
                  model.getTimezone());
          success = model.editEventsFrom(property, eventName,
                  convertToZonedDateTime(filterDateTime, model.getTimezone()),
                  processedFromValue);
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
   * Converts a Temporal value to ZonedDateTime based on the specified timezone.
   *
   * @param temporal the temporal value to convert
   * @param zone     the timezone to apply
   * @return the converted ZonedDateTime
   * @throws IllegalArgumentException if the Temporal type is unsupported
   */
  private ZonedDateTime convertToZonedDateTime(Temporal temporal, ZoneId zone) {
    if (temporal == null) {
      return null;
    }
    if (temporal instanceof ZonedDateTime) {
      return (ZonedDateTime) temporal;
    }
    if (temporal instanceof LocalDateTime) {
      return ZonedDateTime.of((LocalDateTime) temporal, zone);
    }
    throw new IllegalArgumentException("Unsupported Temporal type");
  }

  /**
   * Processes a temporal value when updating startdatetime or enddatetime properties.
   *
   * @param property the property being modified
   * @param value    the new value for the property
   * @param zone     the timezone to apply
   * @return the processed temporal value as a string
   */
  private String processTemporalValue(String property, String value, ZoneId zone) {
    if (property.equals("startdatetime") || property.equals("enddatetime")) {
      ZonedDateTime zonedValue = ZonedDateTime.of(LocalDateTime.parse(value), zone);
      return zonedValue.toString();
    }
    return value;
  }

  /**
   * Returns the mode of the edit operation.
   *
   * @return the edit mode
   */
  public EditMode getMode() {
    return mode;
  }

  /**
   * Returns the property being edited.
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
   * Returns the original start date-time of the event.
   *
   * @return the original start date-time
   */
  public Temporal getOriginalStart() {
    return originalStart;
  }

  /**
   * Returns the original end date-time of the event.
   *
   * @return the original end date-time
   */
  public Temporal getOriginalEnd() {
    return originalEnd;
  }

  /**
   * Returns the filter date-time used in FROM mode.
   *
   * @return the filter date-time
   */
  public Temporal getFilterDateTime() {
    return filterDateTime;
  }

  /**
   * Returns the new value for the property being updated.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }
}

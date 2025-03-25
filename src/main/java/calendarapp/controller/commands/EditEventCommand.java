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

  // SINGLE mode constructor
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

  // FROM mode constructor
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

  // ALL mode constructor
  public EditEventCommand(String property, String eventName, String newValue) {
    this.mode = EditMode.ALL;
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
    this.originalStart = null;
    this.originalEnd = null;
    this.filterDateTime = null;
  }

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = false;
    String propertyLower = property.toLowerCase();

    try {
      switch (mode) {
        case SINGLE:
          String processedSingleValue = processTemporalValue(propertyLower, newValue, model.getTimezone());
          success = model.editSingleEvent(property, eventName,
                  convertToZonedDateTime(originalStart, model.getTimezone()),
                  convertToZonedDateTime(originalEnd, model.getTimezone()),
                  processedSingleValue);
          break;
        case FROM:
          String processedFromValue = processTemporalValue(propertyLower, newValue, model.getTimezone());
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

  // Converts input Temporal to ZonedDateTime based on calendar timezone
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

  // If startdatetime/enddatetime, process ZonedDateTime string
  private String processTemporalValue(String property, String value, ZoneId zone) {
    if (property.equals("startdatetime") || property.equals("enddatetime")) {
      ZonedDateTime zonedValue = ZonedDateTime.of(LocalDateTime.parse(value), zone);
      return zonedValue.toString();
    }
    return value;
  }

  // Getters
  public EditMode getMode() {
    return mode;
  }

  public String getProperty() {
    return property;
  }

  public String getEventName() {
    return eventName;
  }

  public Temporal getOriginalStart() {
    return originalStart;
  }

  public Temporal getOriginalEnd() {
    return originalEnd;
  }

  public Temporal getFilterDateTime() {
    return filterDateTime;
  }

  public String getNewValue() {
    return newValue;
  }
}

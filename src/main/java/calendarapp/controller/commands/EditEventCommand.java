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

  private String convertValueIfDateTime(String property, String value, ZoneId zone) {
    if ("startdatetime".equals(property) || "enddatetime".equals(property)) {
      return ZonedDateTime.of(LocalDateTime.parse(value), zone).toString();
    }
    return value;
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

  public ZonedDateTime getOriginalStart() {
    return originalStart;
  }

  public ZonedDateTime getOriginalEnd() {
    return originalEnd;
  }

  public ZonedDateTime getFilterDateTime() {
    return filterDateTime;
  }

  public String getNewValue() {
    return newValue;
  }
}

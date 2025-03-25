package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

/**
 * Command to edit a recurring event's property like repeating days, repeat count, or until date.
 */
public class EditRecurringEventCommand implements ICalendarModelCommand {

  private final String property;
  private final String eventName;
  private final String newValue;
  private final Temporal temporalValue;

  public EditRecurringEventCommand(String property, String eventName, String newValue) {
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
    this.temporalValue = null;
  }

  public EditRecurringEventCommand(String property, String eventName, Temporal temporalValue) {
    this.property = property;
    this.eventName = eventName;
    this.temporalValue = temporalValue;
    this.newValue = null;
  }

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success = false;
    try {

      if (property.equalsIgnoreCase("repeatuntil") && temporalValue != null) {
        ZonedDateTime zonedDateTime = convertToZonedDateTime(temporalValue, model.getTimezone());
        success = model.editRecurringEvent(eventName, property, zonedDateTime.toString());
      } else if (property.equalsIgnoreCase("repeattimes") || property.equalsIgnoreCase("repeatingdays")) {
        // Send directly as string value
        success = model.editRecurringEvent(eventName, property, newValue);
      } else {
        // Fallback for other fields like description/location
        success = model.editRecurringEvent(eventName, property, newValue);
      }
    } catch (Exception e) {
      view.displayError("Execution Error: " + e.getMessage());
      return false;
    }

    if (success) {
      view.displayMessage("Recurring event modified successfully.");
    } else {
      view.displayError("Failed to modify recurring event.");
    }
    return success;
  }


  private ZonedDateTime convertToZonedDateTime(Temporal temporal, ZoneId zoneId) {
    if (temporal instanceof ZonedDateTime) {
      return (ZonedDateTime) temporal;
    } else {
      return ZonedDateTime.from(temporal).withZoneSameInstant(zoneId);
    }
  }
}

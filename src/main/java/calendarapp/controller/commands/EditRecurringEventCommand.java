package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

/**
 * Command to edit a recurring event's property such as repeating days, repeat count, or until date.
 */
public class EditRecurringEventCommand implements ICalendarModelCommand {

  private final String property;
  private final String eventName;
  private final String newValue;
  private final Temporal temporalValue;

  /**
   * Constructs an EditRecurringEventCommand for properties that require a string value.
   *
   * @param property  the property to be modified
   * @param eventName the name of the event
   * @param newValue  the new value for the property
   */
  public EditRecurringEventCommand(String property, String eventName, String newValue) {
    this.property = property;
    this.eventName = eventName;
    this.newValue = newValue;
    this.temporalValue = null;
  }

  /**
   * Constructs an EditRecurringEventCommand for properties that require a temporal value.
   *
   * @param property      the property to be modified
   * @param eventName     the name of the event
   * @param temporalValue the new temporal value for the property
   */
  public EditRecurringEventCommand(String property, String eventName, Temporal temporalValue) {
    this.property = property;
    this.eventName = eventName;
    this.temporalValue = temporalValue;
    this.newValue = null;
  }

  /**
   * Executes the command to edit a recurring event in the calendar.
   *
   * @param model the calendar model where the event exists
   * @param view  the view used to display messages
   * @return true if the event was successfully modified, false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    boolean success;
    try {
      if (property.equalsIgnoreCase("repeatuntil") && temporalValue != null) {
        ZonedDateTime adjusted = toZonedDateTime(temporalValue, model.getTimezone());
        success = model.editRecurringEvent(eventName, property, adjusted.toString());
      } else {
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

  /**
   * Converts a temporal value to a ZonedDateTime based on the given timezone.
   *
   * @param input the temporal value to convert
   * @param zone  the timezone to apply
   * @return the converted ZonedDateTime
   */
  private ZonedDateTime toZonedDateTime(Temporal input, ZoneId zone) {
    if (input instanceof ZonedDateTime) {
      return ((ZonedDateTime) input).withZoneSameInstant(zone);
    }
    if (input instanceof LocalDateTime) {
      return ZonedDateTime.of((LocalDateTime) input, zone);
    }
    throw new IllegalArgumentException("Unsupported temporal type for repeatuntil.");
  }
}

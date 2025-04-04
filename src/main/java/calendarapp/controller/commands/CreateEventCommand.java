package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.temporal.Temporal;

/**
 * Command to create a new calendar event.
 * Supports both single and recurring events.
 */
public class CreateEventCommand implements ICalendarModelCommand {
  private final String eventName;
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;
  private final boolean isRecurring;
  private final String weekdays;
  private final int repeatCount;
  private final Temporal repeatUntil;

  /**
   * Constructs a CreateEventCommand with the specified event details.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param autoDecline   whether conflicting events should be automatically declined
   * @param description   the description of the event
   * @param location      the location of the event
   * @param isPublic      whether the event is public
   * @param isAllDay      whether the event lasts all day
   * @param isRecurring   whether the event is recurring
   * @param weekdays      the days of the week on which the event recurs
   * @param repeatCount   the number of times the event repeats
   * @param repeatUntil   the date until which the event repeats (nullable, may be LocalDate or ZonedDateTime)
   */
  public CreateEventCommand(String eventName,
                            ZonedDateTime startDateTime,
                            ZonedDateTime endDateTime,
                            boolean autoDecline,
                            String description,
                            String location,
                            boolean isPublic,
                            boolean isAllDay,
                            boolean isRecurring,
                            String weekdays,
                            int repeatCount,
                            Temporal repeatUntil) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.autoDecline = autoDecline;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
    this.isRecurring = isRecurring;
    this.weekdays = weekdays;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
  }

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      boolean success;
      ZoneId targetZone = model.getTimezone();

      ZonedDateTime adjustedStart = startDateTime.withZoneSameInstant(targetZone);
      ZonedDateTime adjustedEnd = endDateTime.withZoneSameInstant(targetZone);
      ZonedDateTime adjustedRepeatUntil = null;

      if (repeatUntil instanceof ZonedDateTime) {
        adjustedRepeatUntil = ((ZonedDateTime) repeatUntil).withZoneSameInstant(targetZone);
      } else if (repeatUntil instanceof LocalDate) {
        adjustedRepeatUntil = ((LocalDate) repeatUntil).atStartOfDay(targetZone);
      }

      if (isRecurring) {
        RecurringEvent recurringEvent = new RecurringEvent(
                eventName, adjustedStart, adjustedEnd, weekdays, repeatCount,
                adjustedRepeatUntil, description, location, isPublic, isAllDay
        );
        success = model.addRecurringEvent(recurringEvent, autoDecline);
      } else {
        SingleEvent event = new SingleEvent(
                eventName, adjustedStart, adjustedEnd, description,
                location, isPublic, isAllDay, null
        );
        success = model.addEvent(event, autoDecline);
      }

      if (success) {
        view.displayMessage("Event created successfully");
      } else {
        view.displayError("Event creation failed due to conflict");
      }
      return success;
    } catch (IllegalArgumentException ex) {
      view.displayError(ex.getMessage());
      return false;
    }
  }
}

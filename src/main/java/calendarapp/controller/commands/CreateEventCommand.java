package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

/**
 * Command to create a new calendar event.
 * This command holds all details required for creating both single and recurring events.
 */
public class CreateEventCommand implements ICalendarModelCommand {
  private final String eventName;
  private final Temporal startDateTime;
  private final Temporal endDateTime;
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
   * @param repeatUntil   the date until which the event repeats
   */
  public CreateEventCommand(String eventName, Temporal startDateTime,
                            Temporal endDateTime,
                            boolean autoDecline, String description, String location,
                            boolean isPublic, boolean isAllDay,
                            boolean isRecurring, String weekdays, int repeatCount,
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

  /**
   * Returns the name of the event.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return the start date and time
   */
  public Temporal getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time
   */
  public Temporal getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns whether the event should automatically decline conflicts.
   *
   * @return true if conflicts should be declined, false otherwise
   */
  public boolean isAutoDecline() {
    return autoDecline;
  }

  /**
   * Returns the description of the event.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the location of the event.
   *
   * @return the event location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns whether the event is public.
   *
   * @return true if the event is public, false otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Returns whether the event is an all-day event.
   *
   * @return true if the event is all-day, false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Returns whether the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Returns the weekdays on which the event recurs.
   *
   * @return the weekdays of recurrence
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times the event repeats.
   *
   * @return the repeat count
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Returns the date until which the event repeats.
   *
   * @return the repeat-until date
   */
  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Creates a single or recurring event in the calendar.
   * The event is adjusted to match the calendar's timezone.
   * A success message is displayed if the event is created successfully.
   * If a conflict occurs, an error message is displayed.
   *
   * @param model the calendar model where the event is added
   * @param view  the view used to display messages
   * @return true if the event was created successfully, false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      boolean success;
      ZoneId targetZone = model.getTimezone();
      ZonedDateTime adjustedStart = ZonedDateTime.from(startDateTime)
              .withZoneSameInstant(targetZone);
      ZonedDateTime adjustedEnd = ZonedDateTime.from(endDateTime)
              .withZoneSameInstant(targetZone);
      ZonedDateTime adjustedRepeatUntil = (repeatUntil != null)
              ? ZonedDateTime.from(repeatUntil).withZoneSameInstant(targetZone) : null;

      if (isRecurring) {
        RecurringEvent recurringEvent = new RecurringEvent(eventName, adjustedStart,
                adjustedEnd, weekdays, repeatCount, adjustedRepeatUntil,
                description, location, isPublic, isAllDay);
        success = model.addRecurringEvent(recurringEvent, autoDecline);
      } else {
        SingleEvent event = new SingleEvent(eventName, adjustedStart, adjustedEnd,
                description, location, isPublic, isAllDay, null);
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

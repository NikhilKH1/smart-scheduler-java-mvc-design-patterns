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
   * @param repeatUntil   the date until which the event repeats
   */
  public CreateEventCommand(String eventName, ZonedDateTime startDateTime,
                            ZonedDateTime endDateTime, boolean autoDecline,
                            String description, String location, boolean isPublic,
                            boolean isAllDay, boolean isRecurring, String weekdays,
                            int repeatCount, Temporal repeatUntil) {
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
   * Gets the name of the event.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date and time as a ZonedDateTime
   */
  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date and time as a ZonedDateTime
   */
  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns whether conflicting events should be automatically declined.
   *
   * @return true if conflicts are to be automatically declined; false otherwise
   */
  public boolean isAutoDecline() {
    return autoDecline;
  }

  /**
   * Gets the description of the event.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the event location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns whether the event is public.
   *
   * @return true if the event is public; false if private
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Returns whether the event lasts all day.
   *
   * @return true if the event is all-day; false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Returns whether the event is recurring.
   *
   * @return true if the event is recurring; false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Gets the weekdays on which the event recurs.
   *
   * @return a string representing the recurring weekdays (e.g., "MO,TU,WE")
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Gets the number of times the event should repeat.
   *
   * @return the repeat count
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Gets the date until which the event should repeat.
   *
   * @return a Temporal object representing the repeat-until date (could be ZonedDateTime or LocalDate)
   */
  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Executes the command to create a new event in the given calendar model.
   * Handles both single and recurring events, adjusting for the calendar's timezone.
   * Displays a success message if the event is created or an error if it fails.
   *
   * @param model the calendar model to add the event to
   * @param view  the view for displaying messages
   * @return true if the event was created successfully, false otherwise
   */
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
        RecurringEvent recurringEvent = new RecurringEvent(eventName, adjustedStart,
                adjustedEnd, weekdays, repeatCount, adjustedRepeatUntil, description,
                location, isPublic, isAllDay
        );
        success = model.addRecurringEvent(recurringEvent, autoDecline);
      } else {
        SingleEvent event = new SingleEvent(eventName, adjustedStart, adjustedEnd, description,
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

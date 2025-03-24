package calendarapp.controller.commands;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

/**
 * Command to create a new calendar event.
 * This command holds all details required for creating both single and recurring events.
 */
public class CreateEventCommand implements CalendarModelCommand {
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
  private final ZonedDateTime repeatUntil;

  /**
   * Constructs a CreateEventCommand with the specified event details.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param autoDecline   true if conflicts should be automatically declined
   * @param description   the event description
   * @param location      the event location
   * @param isPublic      true if the event is public
   * @param isAllDay      true if the event lasts all day
   * @param isRecurring   true if the event is recurring
   * @param weekdays      the days on which the event recurs
   * @param repeatCount   the number of repetitions for recurring events
   * @param repeatUntil   the date and time until which the event repeats
   */
  public CreateEventCommand(String eventName, ZonedDateTime startDateTime,
                            ZonedDateTime endDateTime,
                            boolean autoDecline, String description, String location,
                            boolean isPublic, boolean isAllDay,
                            boolean isRecurring, String weekdays, int repeatCount,
                            ZonedDateTime repeatUntil) {
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

  public String getEventName() {
    return eventName;
  }

  public ZonedDateTime getStartDateTime() {
    return startDateTime;
  }

  public ZonedDateTime getEndDateTime() {
    return endDateTime;
  }

  public boolean isAutoDecline() {
    return autoDecline;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isAllDay() {
    return isAllDay;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public String getWeekdays() {
    return weekdays;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public ZonedDateTime getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Processes the create event command. Depending on whether the event is recurring, it creates
   * either a recurring event or a single event and attempts to add it to the calendar model.
   *
   * @param model the calendar model used for checking conflicts
   * @param view  the calendar view for displaying messages
   * @return true if the event was created successfully; false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      boolean success;
      ZoneId targetZone = model.getTimezone();

      ZonedDateTime adjustedStart = startDateTime.withZoneSameInstant(targetZone);
      ZonedDateTime adjustedEnd = endDateTime.withZoneSameInstant(targetZone);
      ZonedDateTime adjustedRepeatUntil = (repeatUntil != null) ? repeatUntil.withZoneSameInstant(targetZone) : null;

      if (isRecurring) {
        RecurringEvent recurringEvent = new RecurringEvent(eventName, adjustedStart,
                adjustedEnd, weekdays, repeatCount, adjustedRepeatUntil, description, location, isPublic, isAllDay);
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

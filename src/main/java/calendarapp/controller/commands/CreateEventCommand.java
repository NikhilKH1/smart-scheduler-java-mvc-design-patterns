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
 * Supports both single and recurring events.
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

  public String getEventName() {
    return eventName;
  }

  public Temporal getStartDateTime() {
    return startDateTime;
  }

  public Temporal getEndDateTime() {
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

  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      boolean success;
      ZoneId targetZone = model.getTimezone();

      // Adjusting the start/end/repeatUntil Temporal values to targetZone
      ZonedDateTime adjustedStart = ZonedDateTime.from(startDateTime).withZoneSameInstant(targetZone);
      ZonedDateTime adjustedEnd = ZonedDateTime.from(endDateTime).withZoneSameInstant(targetZone);
      ZonedDateTime adjustedRepeatUntil = (repeatUntil != null) ? ZonedDateTime.from(repeatUntil).withZoneSameInstant(targetZone) : null;

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

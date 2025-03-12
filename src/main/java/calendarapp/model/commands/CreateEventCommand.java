package calendarapp.model.commands;

import java.time.LocalDateTime;

/**
 * Command to create a new calendar event.
 * This command holds all details required for creating both single and recurring events.
 */
public class CreateEventCommand implements Command {
  private final String eventName;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final boolean autoDecline;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;
  private final boolean isRecurring;
  private final String weekdays;
  private final int repeatCount;
  private final LocalDateTime repeatUntil;

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
  public CreateEventCommand(String eventName, LocalDateTime startDateTime,
                            LocalDateTime endDateTime,
                            boolean autoDecline, String description, String location,
                            boolean isPublic, boolean isAllDay,
                            boolean isRecurring, String weekdays, int repeatCount,
                            LocalDateTime repeatUntil) {
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
   * Returns the event name.
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
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Indicates whether the event should be automatically declined in case of a conflict.
   *
   * @return true if auto-decline is enabled; false otherwise
   */
  public boolean isAutoDecline() {
    return autoDecline;
  }

  /**
   * Returns the event description.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the event location.
   *
   * @return the event location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Indicates whether the event is public.
   *
   * @return true if the event is public; false otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Indicates whether the event lasts all day.
   *
   * @return true if it is an all-day event; false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Indicates whether the event is recurring.
   *
   * @return true if the event is recurring; false otherwise
   */
  public boolean isRecurring() {
    return isRecurring;
  }

  /**
   * Returns the weekdays on which a recurring event occurs.
   *
   * @return a string representing the weekdays
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of repetitions for a recurring event.
   *
   * @return the repeat count
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Returns the date and time until which a recurring event should repeat.
   *
   * @return the repeat-until date and time
   */
  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
  }
}

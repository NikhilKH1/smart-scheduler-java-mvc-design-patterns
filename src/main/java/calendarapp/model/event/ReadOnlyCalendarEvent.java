package calendarapp.model.event;

import java.time.ZonedDateTime;

/**
 * The ReadOnlyCalendarEvent interface defines methods to access the details
 * of a calendar event. It provides read-only access to event properties such as
 * subject, description, location, date and time, recurrence details, and public visibility.
 */
public interface ReadOnlyCalendarEvent {

  /**
   * Returns the subject/title of the event.
   *
   * @return the subject of the event
   */
  public String getSubject();

  /**
   * Returns the description of the event.
   *
   * @return the description of the event
   */
  public String getDescription();

  /**
   * Returns the location where the event is taking place.
   *
   * @return the location of the event
   */
  public String getLocation();

  /**
   * Returns the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  public ZonedDateTime getStartDateTime();

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time of the event
   */
  public ZonedDateTime getEndDateTime();

  /**
   * Returns whether the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  public boolean isRecurring();

  /**
   * Returns the weekdays on which the event repeats, represented as a string.
   *
   * @return a string representing the weekdays on which the event repeats
   */
  public String getWeekdays();

  /**
   * Returns the date until which the event repeats. This method is only relevant
   * if the event is recurring.
   *
   * @return the date until which the event repeats, or null if not applicable
   */
  public ZonedDateTime repeatUntil();

  /**
   * Returns the number of times the event will repeat. This method is only relevant
   * if the event is recurring.
   *
   * @return the repeat count, or null if not applicable
   */
  public Integer getRepeatCount();

  /**
   * Returns whether the event is marked as public.
   *
   * @return true if the event is public, false otherwise
   */
  public boolean isPublic();

  /**
   * Returns whether the event is an all-day event.
   *
   * @return true if the event is an all-day event,  false otherwise
   */
  public boolean isAllDay();
}

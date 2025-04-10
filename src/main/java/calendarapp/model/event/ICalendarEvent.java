package calendarapp.model.event;

import java.time.ZonedDateTime;

/**
 * This interface represents a calendar event.
 * It provides methods to retrieve event details such as subject, start and end date/time,
 * description, location, whether the event lasts all day, and its public status.
 */
public interface ICalendarEvent extends ReadOnlyCalendarEvent {

  /**
   * Returns the subject of the event.
   *
   * @return the subject of the event
   */
  public String getSubject();

  /**
   * Returns the start date and time of the event.
   *
   * @return the start date and time as ZonedDateTime
   */
  public ZonedDateTime getStartDateTime();

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time as ZonedDateTime
   */
  public ZonedDateTime getEndDateTime();

  /**
   * Returns the description of the event.
   *
   * @return the event description
   */
  public String getDescription();

  /**
   * Returns the location of the event.
   *
   * @return the event location
   */
  public String getLocation();

  /**
   * Indicates whether the event lasts all day.
   *
   * @return true if the event is an all-day event, false otherwise
   */
  public boolean isAllDay();

  /**
   * Indicates whether the event is public.
   *
   * @return true if the event is public, false otherwise
   */
  public boolean isPublic();

  /**
   * Returns a copy of the event with the specified property updated.
   *
   * @param property the property name to update
   * @param newValue the new value of the property
   * @return a new instance of the event with the updated property
   */
  public ICalendarEvent withUpdatedProperty(String property, String newValue);

}

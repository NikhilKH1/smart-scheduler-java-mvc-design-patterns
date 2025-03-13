package calendarapp.model.event;

import java.time.LocalDateTime;

/**
 * This abstract class provides a common implementation for calendar events.
 * It holds properties such as subject, start and end date/time, description, location,
 * and flags for public and all-day events.
 */
public abstract class AbstractCalendarEvent implements CalendarEvent {
  protected String subject;
  protected LocalDateTime startDateTime;
  protected LocalDateTime endDateTime;
  protected String description;
  protected String location;
  protected boolean isPublic;
  protected boolean isAllDay;

  /**
   * Returns the subject of the event.
   *
   * @return the event subject
   */
  @Override
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return the event start date and time
   */
  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the event end date and time
   */
  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns the description of the event.
   *
   * @return the event description
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Returns the location of the event.
   *
   * @return the event location
   */
  @Override
  public String getLocation() {
    return location;
  }

  /**
   * Indicates whether the event is public.
   *
   * @return true if the event is public; false otherwise
   */
  @Override
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Indicates whether the event is an all-day event.
   *
   * @return true if the event lasts all day; false otherwise
   */
  @Override
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Returns a basic string representation of the event (used for debugging/logging).
   * The full display logic is handled separately in CalendarView.
   *
   * @return a string representation of the event
   */
  @Override
  public String toString() {
    return subject + " (" + startDateTime + " - " + endDateTime + ")";
  }
}

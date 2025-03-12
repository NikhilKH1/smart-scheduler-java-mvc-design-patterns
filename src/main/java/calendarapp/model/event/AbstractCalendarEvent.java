package calendarapp.model.event;

import java.time.LocalDateTime;

/**
 * This abstract class provides a common implementation for calendar events.
 * It holds properties such as subject, start and end date/time, description, location,
 * and flags for public and all-day events. It also provides a standard string representation.
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
   * Indicates whether the event is public.
   *
   * @return true if the event is public; false otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Indicates whether the event is an all-day event.
   *
   * @return true if the event lasts all day; false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Returns a string representation of the event including subject, date/time, description,
   * location, public status, and all-day flag.
   *
   * @return a string representing the event
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("- ").append(subject)
            .append(": ").append(startDateTime)
            .append(" to ").append(endDateTime);

    if (description != null && !description.isEmpty()) {
      sb.append(" | Description: ").append(description);
    }
    if (location != null && !location.isEmpty()) {
      sb.append(" | Location: ").append(location);
    }

    if (isPublic) {
      sb.append(" | Public");
    } else {
      sb.append(" | Private");
    }

    if (isAllDay) {
      sb.append(" | All Day Event");
    }

    return sb.toString();
  }
}

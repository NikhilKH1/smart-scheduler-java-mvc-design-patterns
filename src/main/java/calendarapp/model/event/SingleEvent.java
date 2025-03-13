package calendarapp.model.event;

import java.time.LocalDateTime;

/**
 * This class represents a single (non-recurring) calendar event.
 * A single event contains the standard event details and may include a series identifier
 * if it is part of a recurring event series.
 */
public class SingleEvent extends AbstractCalendarEvent {

  private final String seriesId;

  /**
   * Constructs a single event with the specified details.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param description   the event description
   * @param location      the event location
   * @param isPublic      true if the event is public; false otherwise
   * @param isAllDay      true if the event lasts all day; false otherwise
   * @param seriesId      an identifier linking the event to a recurring series,
   *                      or null if not applicable
   */
  public SingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                     String description, String location, boolean isPublic,
                     boolean isAllDay, String seriesId) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
    this.seriesId = seriesId;
  }

  /**
   * Returns the series identifier for this event if it is part of a recurring series.
   *
   * @return the series identifier, or null if not applicable
   */
  public String getSeriesId() {
    return seriesId;
  }
}

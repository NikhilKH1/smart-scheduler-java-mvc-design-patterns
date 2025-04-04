package calendarapp.model.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Represents a single (non-recurring) calendar event.
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
  public SingleEvent(String subject, ZonedDateTime startDateTime, ZonedDateTime endDateTime,
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

  /**
   * Returns a new SingleEvent updated with the specified property.
   *
   * @param property The property to update.
   * @param newValue The new value for the property.
   * @return A new SingleEvent instance with the updated property.
   * @throws IllegalArgumentException if the property is unsupported
   */
  public SingleEvent withUpdatedProperty(String property, String newValue) {
    String newSubject = this.subject;
    String newDescription = this.description;
    String newLocation = this.location;
    ZonedDateTime newStart = this.startDateTime;
    ZonedDateTime newEnd = this.endDateTime;
    boolean newIsPublic = this.isPublic;

    switch (property.toLowerCase().trim()) {
      case "name":
        newSubject = newValue;
        break;
      case "description":
        newDescription = newValue;
        break;
      case "location":
        newLocation = newValue;
        break;
      case "startdatetime":
        newStart = ZonedDateTime.parse(newValue);
        break;
      case "enddatetime":
        newEnd = ZonedDateTime.parse(newValue);
        break;
      case "startdate":
        newStart = LocalDate.parse(newValue).atTime(newStart.toLocalTime())
                .atZone(newStart.getZone());
        break;
      case "enddate":
        newEnd = LocalDate.parse(newValue).atTime(newEnd.toLocalTime())
                .atZone(newEnd.getZone());
        break;
      case "starttime":
        newStart = newStart.toLocalDate().atTime(LocalTime.parse(newValue))
                .atZone(newStart.getZone());
        break;
      case "endtime":
        newEnd = newEnd.toLocalDate().atTime(LocalTime.parse(newValue))
                .atZone(newEnd.getZone());
        break;
      case "public":
        newIsPublic = Boolean.parseBoolean(newValue);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }

    return new SingleEvent(newSubject, newStart, newEnd, newDescription,
            newLocation, newIsPublic, this.isAllDay, this.seriesId);
  }
}
package calendarapp.utils;

import calendarapp.model.event.SingleEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utility class that provides helper methods to update calendar events.
 */
public class ModelHelper {

  /**
   * Creates an updated SingleEvent based on the old event
   * and the property that needs to be updated.
   * The method parses the new value and applies the change to the corresponding property.
   *
   * @param oldEvent the original event to be updated
   * @param property the property to update (e.g., name, description, location etc.)
   * @param newValue the new value for the specified property
   * @return a new SingleEvent instance with the updated value
   * @throws IllegalArgumentException if the property is unsupported
   */
  public static SingleEvent createUpdatedEvent(SingleEvent oldEvent,
                                               String property, String newValue) {
    String newSubject = oldEvent.getSubject();
    String newDescription = oldEvent.getDescription();
    String newLocation = oldEvent.getLocation();
    LocalDateTime newStart = oldEvent.getStartDateTime();
    LocalDateTime newEnd = oldEvent.getEndDateTime();
    boolean newIsPublic = oldEvent.isPublic();

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
        newStart = LocalDateTime.parse(newValue);
        break;
      case "enddatetime":
        newEnd = LocalDateTime.parse(newValue);
        break;
      case "startdate":
        newStart = LocalDate.parse(newValue).atTime(newStart.toLocalTime());
        break;
      case "enddate":
        newEnd = LocalDate.parse(newValue).atTime(newEnd.toLocalTime());
        break;
      case "starttime":
        newStart = newStart.toLocalDate().atTime(LocalTime.parse(newValue));
        break;
      case "endtime":
        newEnd = newEnd.toLocalDate().atTime(LocalTime.parse(newValue));
        break;
      case "public":
        newIsPublic = Boolean.parseBoolean(newValue);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
    return new SingleEvent(newSubject, newStart, newEnd, newDescription,
            newLocation, newIsPublic, oldEvent.isAllDay(), oldEvent.getSeriesId());
  }
}

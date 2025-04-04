package calendarapp.model;

import calendarapp.model.event.ICalendarEvent;

import java.time.ZonedDateTime;

/**
 * This utility class provides a method to determine if two calendar events conflict.
 * Two events are considered to conflict if one starts before the other ends and
 * ends after the other starts.
 */
public class ConflictChecker {

  /**
   * Checks if two calendar events conflict.
   *
   * @param e1 the first calendar event
   * @param e2 the second calendar event
   * @return true if the events conflict; false otherwise
   */
  public static boolean hasConflict(ICalendarEvent e1, ICalendarEvent e2) {
    ZonedDateTime start1 = e1.getStartDateTime();
    ZonedDateTime end1 = e1.getEndDateTime();
    ZonedDateTime start2 = e2.getStartDateTime();
    ZonedDateTime end2 = e2.getEndDateTime();

    return start1.isBefore(end2)
            && !end1.isEqual(start2)
            && end1.isAfter(start2);
  }

  /**
   * Checks if a conflict exists between the new event and other events in the calendar,
   * excluding the specified old event.
   *
   * @param oldEvent the event that is being replaced or updated
   * @param newEvent the event to be checked for conflicts with other events
   * @param events the list of all events in the calendar
   * @return true if a conflict is found with any other event, false otherwise
   */
  public static boolean hasConflictExcept(ICalendarEvent oldEvent, ICalendarEvent newEvent, Iterable<ICalendarEvent> events) {
    for (ICalendarEvent existing : events) {
      if (!existing.equals(oldEvent) && hasConflict(existing, newEvent)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a conflict exists between a recurring event and other events in the calendar,
   * excluding the specified event identified by its subject.
   *
   * @param eventName the name of the recurring event being checked
   * @param newEvent  the event to be checked for conflicts with other events
   * @param events    the list of all events in the calendar
   * @return true if a conflict is found with any other event, false otherwise
   */
  public static boolean hasConflictExceptRecurring(String eventName, ICalendarEvent newEvent, Iterable<ICalendarEvent> events) {
    for (ICalendarEvent existing : events) {
      if (!existing.getSubject().equals(eventName)
              && hasConflict(existing, newEvent)) {
        return true;
      }
    }
    return false;
  }
}

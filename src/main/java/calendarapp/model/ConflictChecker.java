package calendarapp.model;

import calendarapp.model.event.ICalendarEvent;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

/**
 * This utility class provides a method to determine if two calendar events conflict.
 * Two events are considered to conflict if one starts before the other ends and
 * ends after the other starts.
 */
public class ConflictChecker {

  /**
   * Converts a Temporal to ZonedDateTime.
   */
  private static ZonedDateTime toZonedDateTime(Temporal temporal) {
    return ZonedDateTime.from(temporal);
  }

  /**
   * Checks if two calendar events conflict.
   *
   * @param e1 the first calendar event
   * @param e2 the second calendar event
   * @return true if the events conflict; false otherwise
   */
  public static boolean hasConflict(ICalendarEvent e1, ICalendarEvent e2) {
    ZonedDateTime start1 = toZonedDateTime(e1.getStartDateTime());
    ZonedDateTime end1 = toZonedDateTime(e1.getEndDateTime());
    ZonedDateTime start2 = toZonedDateTime(e2.getStartDateTime());
    ZonedDateTime end2 = toZonedDateTime(e2.getEndDateTime());

    return start1.isBefore(end2)
            && !end1.isEqual(start2)
            && end1.isAfter(start2);
  }
}

package calendarapp.model;

import calendarapp.model.event.CalendarEvent;

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
  public static boolean hasConflict(CalendarEvent e1, CalendarEvent e2) {
    return e1.getStartDateTime().isBefore(e2.getEndDateTime())
            && !e1.getEndDateTime().isEqual(e2.getStartDateTime())
            && e1.getEndDateTime().isAfter(e2.getStartDateTime());
  }
}

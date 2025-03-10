package calendarapp.model;

import calendarapp.model.event.CalendarEvent;

public class ConflictChecker {
  /**
   * Two events conflict if one starts before the other ends and ends after the other starts.
   */
  public static boolean hasConflict(CalendarEvent e1, CalendarEvent e2) {
    return e1.getStartDateTime().isBefore(e2.getEndDateTime())
            && e1.getEndDateTime().isAfter(e2.getStartDateTime());
  }
}

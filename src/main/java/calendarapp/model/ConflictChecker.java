package calendarapp.model;

public class ConflictChecker {

  public static boolean hasConflict(CalendarEvent e1, CalendarEvent e2) {
    return e1.getStartDateTime().isBefore(e2.getEndDateTime())
            && e1.getEndDateTime().isAfter(e2.getStartDateTime());
  }
}

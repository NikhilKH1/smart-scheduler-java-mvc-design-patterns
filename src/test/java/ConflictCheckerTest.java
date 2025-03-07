
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

import calendarapp.model.CalendarEvent;
import calendarapp.model.ConflictChecker;
import calendarapp.model.SingleEvent;

public class ConflictCheckerTest {

  @Test
  public void testNoConflict() {
    CalendarEvent e1 = new SingleEvent(
            "Event1",
            LocalDateTime.of(2025, 5, 10, 10, 0),
            LocalDateTime.of(2025, 5, 10, 11, 0),
            "",
            "",
            true,
            false
    );
    CalendarEvent e2 = new SingleEvent(
            "Event2",
            LocalDateTime.of(2025, 5, 10, 11, 0),
            LocalDateTime.of(2025, 5, 10, 12, 0),
            "",
            "",
            true,
            false
    );
    // e1 ends exactly when e2 starts -> no conflict
    assertFalse(ConflictChecker.hasConflict(e1, e2));
  }

  @Test
  public void testOverlapConflict() {
    CalendarEvent e1 = new SingleEvent(
            "Event1",
            LocalDateTime.of(2025, 5, 10, 10, 0),
            LocalDateTime.of(2025, 5, 10, 11, 0),
            "",
            "",
            true,
            false
    );
    CalendarEvent e2 = new SingleEvent(
            "Event2",
            LocalDateTime.of(2025, 5, 10, 10, 30),
            LocalDateTime.of(2025, 5, 10, 11, 30),
            "",
            "",
            true,
            false
    );
    // e2 starts before e1 ends -> conflict
    assertTrue(ConflictChecker.hasConflict(e1, e2));
  }

  @Test
  public void testAllDayEventConflict() {
    // If an event is all day, it might span entire day
    CalendarEvent allDay = new SingleEvent(
            "AllDayEvent",
            LocalDateTime.of(2025, 6, 1, 0, 0),
            LocalDateTime.of(2025, 6, 1, 23, 59),
            "",
            "",
            true,
            true
    );
    CalendarEvent partialDay = new SingleEvent(
            "Partial",
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0),
            "",
            "",
            true,
            false
    );
    assertTrue(ConflictChecker.hasConflict(allDay, partialDay));
  }
}

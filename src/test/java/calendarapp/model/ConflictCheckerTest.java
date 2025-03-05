package calendarapp.model;

import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Test;
import calendarapp.model.SingleEvent;

public class ConflictCheckerTest {

  @Test
  public void testNoConflictWhenEventsTouchButDoNotOverlap() {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 10, 8, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 10, 9, 0);
    CalendarEvent event1 = new SingleEvent("Event1", start1, end1);

    LocalDateTime start2 = LocalDateTime.of(2025, 3, 10, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 10, 10, 0);
    CalendarEvent event2 = new SingleEvent("Event2", start2, end2);

    assertFalse("Events that touch but do not overlap should not conflict.",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testConflictWhenEventsOverlap() {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 10, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 10, 10, 0);
    CalendarEvent event1 = new SingleEvent("Event1", start1, end1);

    LocalDateTime start2 = LocalDateTime.of(2025, 3, 10, 9, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 10, 10, 30);
    CalendarEvent event2 = new SingleEvent("Event2", start2, end2);

    assertTrue("Events that overlap should conflict.",
            ConflictChecker.hasConflict(event1, event2));
  }
}

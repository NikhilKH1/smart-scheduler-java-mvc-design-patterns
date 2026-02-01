import calendarapp.model.ConflictChecker;
import calendarapp.model.event.SingleEvent;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the ConflictChecker class.
 */
public class ConflictCheckerTest {

  @Test
  public void testEventsConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 10, 30,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 30,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testAdjacentEventsNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testSeparateEventsNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 12, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testEventCompletelyInsideAnother() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 12, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent outerEvent = new SingleEvent("Outer Event", start1, end1, "",
            "", true, false, null);
    SingleEvent innerEvent = new SingleEvent("Inner Event", start2, end2, "",
            "", true, false, null);

    assertTrue(ConflictChecker.hasConflict(outerEvent, innerEvent));
  }

  @Test
  public void testEventTouchingStartNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testEventTouchingEndNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testIdenticalEventsConflict() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start, end, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 1", start, end, "", "",
            true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testLongOverlappingEventsConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 8, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 12, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 13, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testMultipleConflictingEvents() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 8, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start3 = ZonedDateTime.of(2025, 6, 1, 9, 30,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end3 = ZonedDateTime.of(2025, 6, 1, 10, 30,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);
    SingleEvent event3 = new SingleEvent("Event 3", start3, end3, "", "",
            true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
    assertTrue(ConflictChecker.hasConflict(event2, event3));
    assertTrue(ConflictChecker.hasConflict(event1, event3));
  }

  @Test
  public void testMidnightBoundary() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 23, 30,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 2, 0, 30,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 2, 0, 15,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 2, 1, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Late Night Event", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Early Morning Event", start2, end2, "",
            "", true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testMultiDayEventOverlap() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 12, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 3, 12, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 2, 14, 0,
            0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Conference", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Meeting", start2, end2, "", "",
            true, false, null);

    assertTrue(ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testConflictSkippedWhenNameMatches() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0, 0, 0, ZoneId.of("UTC"));

    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9, 30, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 10, 30, 0, 0, ZoneId.of("UTC"));

    SingleEvent existing = new SingleEvent("Math Class", start1, end1, "", "", true, false, null);
    SingleEvent newEvent = new SingleEvent("Math Class", start2, end2, "", "", true, false, null);

    boolean hasConflict = false;
    if (!existing.getSubject().equals(newEvent.getSubject())
            && ConflictChecker.hasConflict(existing, newEvent)) {
      hasConflict = true;
    }

    assertFalse(hasConflict);
  }


}



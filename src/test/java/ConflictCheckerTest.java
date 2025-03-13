

import calendarapp.model.ConflictChecker;
import calendarapp.model.event.SingleEvent;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the ConflictChecker class.
 */
public class ConflictCheckerTest {

  @Test
  public void testEventsConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 30);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertTrue("These events should conflict", ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse("These events should not conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testAdjacentEventsNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse("Adjacent events should NOT conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testSeparateEventsNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 12, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse("Non-overlapping events should NOT conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testEventCompletelyInsideAnother() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 12, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent outerEvent = new SingleEvent("Outer Event", start1, end1, "",
            "", true, false, null);
    SingleEvent innerEvent = new SingleEvent("Inner Event", start2, end2, "",
            "", true, false, null);

    assertTrue("Event inside another should conflict",
            ConflictChecker.hasConflict(outerEvent, innerEvent));
  }

  @Test
  public void testEventTouchingStartNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse("Events that just touch should NOT conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testEventTouchingEndNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 10, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertFalse("Events that just touch at the end should NOT conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testIdenticalEventsConflict() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start, end, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 1", start, end, "", "",
            true, false, null);

    assertTrue("Identical events should conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testLongOverlappingEventsConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 8, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 12, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 13, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);

    assertTrue("Long overlapping events should conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testMultipleConflictingEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 8, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime start3 = LocalDateTime.of(2025, 6, 1, 9, 30);
    LocalDateTime end3 = LocalDateTime.of(2025, 6, 1, 10, 30);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null);
    SingleEvent event3 = new SingleEvent("Event 3", start3, end3, "", "",
            true, false, null);

    assertTrue("Events 1 and 2 should conflict",
            ConflictChecker.hasConflict(event1, event2));
    assertTrue("Events 2 and 3 should conflict",
            ConflictChecker.hasConflict(event2, event3));
    assertTrue("Events 1 and 3 should conflict",
            ConflictChecker.hasConflict(event1, event3));
  }

  @Test
  public void testMidnightBoundary() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 23, 30);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 2, 0, 30);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 2, 0, 15);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 2, 1, 0);

    SingleEvent event1 = new SingleEvent("Late Night Event", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Early Morning Event", start2, end2, "",
            "", true, false, null);

    assertTrue("Events crossing midnight should conflict",
            ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testMultiDayEventOverlap() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 12, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 3, 12, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 2, 14, 0);

    SingleEvent event1 = new SingleEvent("Conference", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Meeting", start2, end2, "",
            "", true, false, null);

    assertTrue("Multi-day events should detect conflicts correctly",
            ConflictChecker.hasConflict(event1, event2));
  }


}



import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the RecurringEvent class.
 */
public class RecurringEventTest {

  @Test
  public void testCreateRecurringEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    RecurringEvent event = new RecurringEvent("Daily Standup", start, end, "MTWRF",
            5, null, "Scrum meeting", "Office", true,
            false);

    assertEquals("Daily Standup", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertEquals("MTWRF", event.getWeekdays());
    assertEquals(5, event.getRepeatCount());
    assertNull(event.getRepeatUntil());
    assertEquals("Scrum meeting", event.getDescription());
    assertEquals("Office", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
  }

  @Test
  public void testRecurringEventWithEndDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime repeatUntil = LocalDateTime.of(2025, 6, 10, 0, 0);

    RecurringEvent event = new RecurringEvent("Yoga Class", start, end, "MWF",
            0, repeatUntil, "Morning exercise", "Gym", false, false);

    assertEquals("MWF", event.getWeekdays());
    assertEquals(repeatUntil, event.getRepeatUntil());
  }

  @Test
  public void testGenerateOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    RecurringEvent event = new RecurringEvent("Weekly Sync", start, end, "W",
            3, null, "Check-in", "Conference Room",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series-123");

    assertEquals(3, occurrences.size());
    assertEquals("Weekly Sync", occurrences.get(0).getSubject());
    assertEquals("series-123", occurrences.get(0).getSeriesId());
  }

  @Test
  public void testGenerateOccurrencesWithEndDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime repeatUntil = LocalDateTime.of(2025, 6, 5, 0, 0);

    RecurringEvent event = new RecurringEvent("Daily Workout", start, end, "MTWRF",
            0, repeatUntil, "Morning fitness", "Gym", false,
            false);

    List<SingleEvent> occurrences = event.generateOccurrences("series-456");

    assertFalse(occurrences.isEmpty());
    assertTrue(occurrences.get(0).getStartDateTime().isBefore(repeatUntil));
    assertEquals("series-456", occurrences.get(0).getSeriesId());
  }

  @Test
  public void testToStringFormat() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    RecurringEvent event = new RecurringEvent("Project Meeting", start, end, "T",
            4, null, "Weekly status", "Office",
            true, false);

    assertTrue(event.getDescription().contains("Weekly status"));
    assertEquals("Project Meeting", event.getSubject());
  }
}

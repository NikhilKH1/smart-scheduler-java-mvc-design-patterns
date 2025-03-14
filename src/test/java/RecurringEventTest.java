

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.time.DayOfWeek;

/**
 * JUnit tests for the RecurringEvent class.
 */
public class RecurringEventTest {

  @Test
  public void testCreateRecurringEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    RecurringEvent event = new RecurringEvent("Daily Standup", start, end, "MTWRF",
            5, null, "Scrum meeting", "Office",
            true, false);

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
    LocalDateTime repeatUntil = LocalDateTime.of(2025, 6, 10,
            0, 0);

    RecurringEvent event = new RecurringEvent("Yoga Class", start, end, "MWF",
            0, repeatUntil, "Morning exercise", "Gym", false,
            false);

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
    LocalDateTime repeatUntil = LocalDateTime.of(2025, 6, 5,
            0, 0);

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

  @Test
  public void testGenerateOccurrencesEmptyWeekdays() {
    RecurringEvent event = new RecurringEvent("Test",
            LocalDateTime.of(2025, 6, 2, 9, 0),
            LocalDateTime.of(2025, 6, 2, 10, 0),
            "", 3, null, "desc", "loc",
            true, false);
    List<SingleEvent> occurrences = event.generateOccurrences("series1");
    assertEquals(0, occurrences.size());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatCount() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    RecurringEvent event = new RecurringEvent("Test", start, end, "M",
            2, null, "desc", "loc", true, false);
    List<SingleEvent> occurrences = event.generateOccurrences("series1");
    assertEquals(2, occurrences.size());
    assertEquals(start, occurrences.get(0).getStartDateTime());
    assertEquals(start.plusDays(7), occurrences.get(1).getStartDateTime());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntil() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime repeatUntil = start.plusDays(3);
    RecurringEvent event = new RecurringEvent("Test", start, end, "M",
            5, repeatUntil, "desc", "loc", true, false);
    List<SingleEvent> occurrences = event.generateOccurrences("series1");
    assertEquals(1, occurrences.size());
    assertEquals(start, occurrences.get(0).getStartDateTime());
  }

  @Test
  public void testGenerateOccurrencesWhenConditionFalseThenTrue() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 3, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 10, 0);
    RecurringEvent event = new RecurringEvent("Test", start, end, "M",
            1, null, "desc", "loc", true, false);
    List<SingleEvent> occurrences = event.generateOccurrences("series1");
    assertEquals(1, occurrences.size());
    assertEquals(LocalDateTime.of(2025, 6, 9, 9, 0),
            occurrences.get(0).getStartDateTime());
  }

  @Test
  public void testGetDayChar() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar", DayOfWeek.class);
    method.setAccessible(true);
    RecurringEvent dummy = new RecurringEvent("dummy", LocalDateTime.now(),
            LocalDateTime.now(), "MTWRFSU", 1, null, "",
            "", true, false);
    assertEquals('M', method.invoke(dummy, DayOfWeek.MONDAY));
    assertEquals('T', method.invoke(dummy, DayOfWeek.TUESDAY));
    assertEquals('W', method.invoke(dummy, DayOfWeek.WEDNESDAY));
    assertEquals('R', method.invoke(dummy, DayOfWeek.THURSDAY));
    assertEquals('F', method.invoke(dummy, DayOfWeek.FRIDAY));
    assertEquals('S', method.invoke(dummy, DayOfWeek.SATURDAY));
    assertEquals('U', method.invoke(dummy, DayOfWeek.SUNDAY));
  }

  @Test
  public void testGetDayCharNull() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar", DayOfWeek.class);
    method.setAccessible(true);
    RecurringEvent dummy = new RecurringEvent("dummy", LocalDateTime.now(),
            LocalDateTime.now(), "MTWRFSU", 1, null,
            "", "", true, false);
    try {
      method.invoke(dummy, (Object) null);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof NullPointerException);
    }
  }
}

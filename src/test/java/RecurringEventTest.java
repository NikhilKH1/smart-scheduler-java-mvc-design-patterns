import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

/**
 * JUnit test class for RecurringEvent class.
 */
public class RecurringEventTest {

  private RecurringEvent baseEvent;
  private final ZoneId zone = ZoneId.of("UTC");

  @Before
  public void setUp() {
    baseEvent = new RecurringEvent("Original",
            ZonedDateTime.of(2025, 6, 1, 9, 0,
                    0, 0, zone),
            ZonedDateTime.of(2025, 6, 1, 10, 0,
                    0, 0, zone),
            "MW", 5, null, "Description",
            "Location", true, false);
  }

  @Test
  public void testUpdateSubject() {
    RecurringEvent updated = baseEvent.
            withUpdatedProperty("subject", "Updated Title");
    assertEquals("Updated Title", updated.getSubject());
    assertEquals(baseEvent.getStartDateTime(), updated.getStartDateTime());
  }

  @Test
  public void testGetDayCharThrowsOnNull() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar", DayOfWeek.class);
    method.setAccessible(true);

    RecurringEvent dummy = new RecurringEvent("Dummy",
            ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
            "MW", 1, null, "desc",
            "loc", true, false);

    try {
      method.invoke(dummy, new Object[]{null});
      fail("Expected InvocationTargetException");
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof NullPointerException);
    }
  }

  @Test
  public void testGetDayCharThrowsOnNullnew() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar", DayOfWeek.class);
    method.setAccessible(true);

    RecurringEvent dummy = new RecurringEvent("Dummy",
            ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
            "MW", 1, null, "desc", "loc",
            true, false);

    try {
      method.invoke(dummy, new Object[]{null});
      fail("Expected InvocationTargetException caused by IllegalArgumentException");
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof NullPointerException);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUpdatedPropertyInvalidRepeatTimesThrows() {
    RecurringEvent dummy = new RecurringEvent("Dummy",
            ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
            "MW", 1, null, "desc", "loc",
            true, false);

    dummy.withUpdatedProperty("repeattimes", "notanumber");
  }

  @Test
  public void testUpdateStartDateTime() {
    String newStart = "2025-06-03T08:00:00Z";
    RecurringEvent updated = baseEvent.withUpdatedProperty("startdatetime", newStart);
    assertEquals(ZonedDateTime.parse(newStart), updated.getStartDateTime());
    assertEquals(baseEvent.getEndDateTime(), updated.getEndDateTime());
  }

  @Test
  public void testUpdateEndDateTime() {
    String newEnd = "2025-06-03T11:00:00Z";
    RecurringEvent updated = baseEvent.withUpdatedProperty("enddatetime", newEnd);
    assertEquals(ZonedDateTime.parse(newEnd), updated.getEndDateTime());
    assertEquals(baseEvent.getStartDateTime(), updated.getStartDateTime());
  }

  @Test
  public void testUpdateToPublic() {
    RecurringEvent privateEvent = new RecurringEvent("Test",
            baseEvent.getStartDateTime(),
            baseEvent.getEndDateTime(),
            "MW", 5, null,
            "desc", "loc", false, false);

    RecurringEvent updated = privateEvent.withUpdatedProperty("public", "true");
    assertTrue(updated.isPublic());
  }

  @Test
  public void testUpdateToPrivate() {
    RecurringEvent updated = baseEvent.withUpdatedProperty("private", "true");
    assertFalse(updated.isPublic());
  }

  @Test
  public void testGenerateOccurrencesReturnsCorrectList() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3, 9,
            0, 0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, zone);
    RecurringEvent event = new RecurringEvent("Test", start, end, "T",
            2, null, "desc", "loc", true,
            false);

    List<SingleEvent> actual = event.generateOccurrences("seriesABC");

    assertEquals(2, actual.size());
    assertEquals("Test", actual.get(0).getSubject());
    assertEquals("seriesABC", actual.get(0).getSeriesId());
    assertEquals(start, actual.get(0).getStartDateTime());
    assertEquals(end, actual.get(0).getEndDateTime());
  }

  @Test
  public void testGenerateOccurrencesRepeatCountZeroAndNoRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3, 9,
            0, 0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, zone);

    RecurringEvent event = new RecurringEvent("Zero Repeat", start, end,
            "T", 0, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertTrue("Expected no occurrences when repeatCount=0 and no repeatUntil",
            occurrences.isEmpty());
  }


  @Test
  public void testCreateRecurringEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, zone);

    RecurringEvent event = new RecurringEvent(
            "Daily Standup", start, end, "MTWRF", 5, null,
            "Scrum meeting", "Office", true, false
    );

    assertEquals("Daily Standup", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertEquals("MTWRF", event.getWeekdays());
    assertNull(event.repeatUntil());
    assertEquals("Scrum meeting", event.getDescription());
    assertEquals("Office", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
  }

  @Test
  public void testRecurringEventWithEndDate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, zone);
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 6, 10, 0,
            0, 0, 0, zone);

    RecurringEvent event = new RecurringEvent(
            "Yoga Class", start, end, "MWF", 0, repeatUntil,
            "Morning exercise", "Gym", false, false
    );

    assertEquals("MWF", event.getWeekdays());
    assertEquals(repeatUntil, event.repeatUntil());
  }

  @Test
  public void testGenerateOccurrences() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 4, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 4, 10, 0,
            0, 0, zone);

    RecurringEvent event = new RecurringEvent(
            "Weekly Sync", start, end, "W", 3, null,
            "Check-in", "Conference Room", true, false
    );

    List<SingleEvent> occurrences = event.generateOccurrences("series-123");

    assertEquals(3, occurrences.size());
    assertEquals("Weekly Sync", occurrences.get(0).getSubject());
    assertEquals("series-123", occurrences.get(0).getSeriesId());
  }

  @Test
  public void testGenerateOccurrencesWithEndDate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 3, 10, 0,
            0, 0, zone);
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 6, 6, 0,
            0, 0, 0, zone);

    RecurringEvent event = new RecurringEvent(
            "Daily Workout", start, end, "MTWRF", 0, repeatUntil,
            "Morning fitness", "Gym", false, false
    );

    List<SingleEvent> occurrences = event.generateOccurrences("series-456");

    assertFalse(occurrences.isEmpty());
    for (SingleEvent se : occurrences) {
      assertTrue(ZonedDateTime.from(se.getStartDateTime()).isBefore(repeatUntil));
      assertEquals("series-456", se.getSeriesId());
    }
  }

  @Test
  public void testToStringFormat() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, zone);

    RecurringEvent event = new RecurringEvent(
            "Project Meeting", start, end, "T", 4, null,
            "Weekly status", "Office", true, false
    );

    assertTrue(event.getDescription().contains("Weekly status"));
    assertEquals("Project Meeting", event.getSubject());
  }

  @Test
  public void testGenerateOccurrencesEmptyWeekdays() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, zone);

    RecurringEvent event = new RecurringEvent("Test", start, end,
            "", 3, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");
    assertEquals(0, occurrences.size());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatCount() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, zone);

    RecurringEvent event = new RecurringEvent("Test", start, end,
            "M", 2, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals(2, occurrences.size());
    assertEquals(start, occurrences.get(0).getStartDateTime());
    assertEquals(start.plusDays(7), occurrences.get(1).getStartDateTime());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, zone);
    ZonedDateTime repeatUntil = start.plusDays(3);

    RecurringEvent event = new RecurringEvent("Test", start, end,
            "M", 5, repeatUntil, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals(1, occurrences.size());
    assertEquals(start, occurrences.get(0).getStartDateTime());
  }

  @Test
  public void testGenerateOccurrencesWhenConditionFalseThenTrue() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3, 9,
            0, 0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, zone);

    RecurringEvent event = new RecurringEvent("Test", start, end,
            "M", 1, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals(1, occurrences.size());
    assertEquals(start.plusDays(6), occurrences.get(0).getStartDateTime());
  }

  @Test
  public void testGetDayChar() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar", DayOfWeek.class);
    method.setAccessible(true);

    RecurringEvent dummy = new RecurringEvent("dummy",
            ZonedDateTime.now(zone), ZonedDateTime.now(zone),
            "MTWRFSU", 1, null, "", "",
            true, false);

    assertEquals('M', method.invoke(dummy, DayOfWeek.MONDAY));
    assertEquals('T', method.invoke(dummy, DayOfWeek.TUESDAY));
    assertEquals('W', method.invoke(dummy, DayOfWeek.WEDNESDAY));
    assertEquals('R', method.invoke(dummy, DayOfWeek.THURSDAY));
    assertEquals('F', method.invoke(dummy, DayOfWeek.FRIDAY));
    assertEquals('S', method.invoke(dummy, DayOfWeek.SATURDAY));
    assertEquals('U', method.invoke(dummy, DayOfWeek.SUNDAY));
  }

  @Test
  public void testGenerateOccurrencesEmptyWeekdaysReturnsEmptyList() {
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 2, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 2, 10, 0, 0,
                    0, zone),
            "", 3, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertTrue("Expected occurrences to be an empty list", occurrences.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesNonEmptyWeekdays() {
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 3, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 3, 10, 0, 0,
                    0, zone),
            "T", 2, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertFalse("Expected at least one occurrence", occurrences.isEmpty());
    assertEquals(2, occurrences.size());
  }

  @Test
  public void testGenerateOccurrencesRepeatCountBoundary() {
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 3, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 3, 10, 0, 0,
                    0, zone),
            "T", 3, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals("Expected exactly 3 occurrences", 3, occurrences.size());
  }

  @Test
  public void testGenerateOccurrencesRepeatCountExceeds() {
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 3, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 3, 10, 0, 0,
                    0, zone),
            "T", 5, null, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals("Expected exactly 5 occurrences", 5, occurrences.size());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntilAndNoLimit() {
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 6, 8, 0,
            0, 0, 0, zone);
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 3, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 3, 10, 0, 0,
                    0, zone),
            "T", 0, repeatUntil, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertFalse(occurrences.isEmpty());
    assertTrue(ZonedDateTime.from(occurrences.get(occurrences.size() - 1).getStartDateTime())
            .isBefore(ZonedDateTime.from(repeatUntil)));

  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntilStopsCorrectly() {
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 6, 4, 23,
            59, 59, 0, zone);
    RecurringEvent event = new RecurringEvent("Test",
            ZonedDateTime.of(2025, 6, 2, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 2, 10, 0, 0,
                    0, zone),
            "MTW", 0, repeatUntil, "desc", "loc",
            true, false);

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertFalse(occurrences.isEmpty());
    assertTrue(ZonedDateTime.from(occurrences.get(occurrences.size() - 1).getStartDateTime())
            .isBefore(ZonedDateTime.from(repeatUntil)));

  }

  @Test
  public void testGenerateOccurrencesWithEmptyWeekdaysReturnsEmptyList() {
    RecurringEvent event = new RecurringEvent(
            "No Repeat",
            ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 1, 10, 0, 0,
                    0, zone),
            "", 5, null, "desc", "loc",
            true, false
    );

    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertTrue("Occurrences should be empty when weekdays are empty",
            occurrences.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatCountLimitsOccurrences() {
    RecurringEvent event = new RecurringEvent(
            "Limited Repeat",
            ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
                    0, zone),
            ZonedDateTime.of(2025, 6, 1, 10, 0, 0,
                    0, zone),
            "M", 3, null, "desc", "loc",
            true, false
    );


    List<SingleEvent> occurrences = event.generateOccurrences("series1");

    assertEquals("Should only generate exactly 3 occurrences", 3,
            occurrences.size());
  }

  @Test
  public void testGetDayCharNull() throws Exception {
    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar",
            DayOfWeek.class);
    method.setAccessible(true);

    RecurringEvent dummy = new RecurringEvent("dummy",
            ZonedDateTime.now(zone), ZonedDateTime.now(zone),
            "MTWRFSU", 1, null, "", "",
            true, false);

    try {
      method.invoke(dummy, (Object) null);
      fail("Expected InvocationTargetException caused by IllegalArgumentException");
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      assertTrue(cause instanceof NullPointerException);
    }
  }

  @Test
  public void testGenerateOccurrencesWithEmptyWeekdays() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T09:00:00+00:00");
    ZonedDateTime end = ZonedDateTime.parse("2025-04-01T10:00:00+00:00");

    RecurringEvent event = new RecurringEvent(
            "NoDays", start, end, "", 5, null,
            "desc", "loc", true, false);

    List<SingleEvent> result = event.generateOccurrences("series-1");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesWithZeroRepeatCountAndNoUntil() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T09:00:00+00:00");
    ZonedDateTime end = ZonedDateTime.parse("2025-04-01T10:00:00+00:00");

    RecurringEvent event = new RecurringEvent(
            "NoRepeats", start, end, "M", 0, null,
            "desc", "loc", true, false);

    List<SingleEvent> result = event.generateOccurrences("series-2");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntilOnly() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T09:00:00+00:00");
    ZonedDateTime end = ZonedDateTime.parse("2025-04-01T10:00:00+00:00");
    ZonedDateTime until = ZonedDateTime.parse("2025-04-05T10:00:00+00:00");

    RecurringEvent event = new RecurringEvent(
            "WithUntil", start, end, "TRF", 0, until,
            "desc", "loc", true, false);

    List<SingleEvent> result = event.generateOccurrences("series-3");
    assertEquals(3, result.size());
    assertEquals("WithUntil", result.get(0).getSubject());
  }

  @Test
  public void testGenerateOccurrencesStopsAfterRepeatCount() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T09:00:00+00:00");
    ZonedDateTime end = ZonedDateTime.parse("2025-04-01T10:00:00+00:00");

    RecurringEvent event = new RecurringEvent(
            "LimitedRepeats", start, end, "T", 3, null,
            "desc", "loc", true, false);

    List<SingleEvent> result = event.generateOccurrences("series-4");
    assertEquals(3, result.size());
  }

  @Test
  public void testGetDayCharAllDays() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T09:00:00+00:00");
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent("AllDays", start, end, "MTWRFSU",
            1, null,
            "desc", "loc", true, false);

    assertEquals(1, event.generateOccurrences("series-5").size());
  }

  @Test
  public void testGetDayCharThrowsOnNull2() throws Exception {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "Test", start, end, "M", 1, null,
            "desc", "loc", true, false);

    Method method = RecurringEvent.class.getDeclaredMethod("getDayChar",
            java.time.DayOfWeek.class);
    method.setAccessible(true);

    try {
      method.invoke(event, new Object[]{null});
      fail("Expected IllegalArgumentException to be thrown");
    } catch (java.lang.reflect.InvocationTargetException e) {
      Throwable cause = e.getCause();
      assertEquals(NullPointerException.class, cause.getClass());

    }
  }

  @Test
  public void testGenerateOccurrencesWhenWeekdaysIsEmpty() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "EmptyWeekdayTest", start, end,
            "", 5, null,
            "desc", "loc", true, false
    );

    List<?> occurrences = event.generateOccurrences("series1");
    assertTrue(occurrences.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesWithNoRepeatCountAndNoRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "NoRepeatTest", start, end,
            "MW", 0, null,
            "desc", "loc", true, false
    );

    List<?> occurrences = event.generateOccurrences("series2");
    assertTrue(occurrences.isEmpty());
  }

  @Test
  public void testGenerateOccurrencesStopsAtRepeatCount() {
    ZonedDateTime start = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "RepeatCountTest", start, end,
            "T", 2, null,
            "desc", "loc", true, false
    );

    List<?> occurrences = event.generateOccurrences("series3");
    assertEquals(2, occurrences.size());
  }


}

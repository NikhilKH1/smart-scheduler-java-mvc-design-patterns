import calendarapp.model.event.SingleEvent;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the SingleEvent class.
 */
public class SingleEventTest {

  private final ZoneId zone = ZoneId.of("UTC");

  private SingleEvent createSampleEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, zone);
    return new SingleEvent("Meeting", start, end,
            "Team meeting", "Room A", true, false, null);
  }

  @Test
  public void testCreateSingleEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, zone);

    SingleEvent event = new SingleEvent("Meeting", start, end, "Team discussion",
            "Room 101", true, false, null);

    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertEquals("Team discussion", event.getDescription());
    assertEquals("Room 101", event.getLocation());
    assertTrue(event.isPublic());
    assertFalse(event.isAllDay());
    assertNull(event.getSeriesId());
  }

  @Test
  public void testAllDayEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 0, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 23, 59,
            0, 0, zone);

    SingleEvent event = new SingleEvent("Holiday", start, end, "National holiday",
            "Home", true, true, null);

    assertTrue(event.isAllDay());
  }

  @Test
  public void testPrivateEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 12, 0,
            0, 0, zone);
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 13, 0,
            0, 0, zone);

    SingleEvent event = new SingleEvent("Lunch", start, end, "Personal time",
            "Cafeteria", false, false, null);

    assertFalse(event.isPublic());
  }

  @Test
  public void testUpdateName() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("name", "Planning");

    assertEquals("Planning", updated.getSubject());
    assertEquals(original.getStartDateTime(), updated.getStartDateTime());
  }

  @Test
  public void testUpdateDescription() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("description",
            "Updated desc");

    assertEquals("Updated desc", updated.getDescription());
  }

  @Test
  public void testUpdateLocation() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("location", "Room B");

    assertEquals("Room B", updated.getLocation());
  }

  @Test
  public void testUpdateStartDateTime() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("startdatetime",
            "2025-06-01T08:30Z");

    assertEquals(ZonedDateTime.parse("2025-06-01T08:30Z"), updated.getStartDateTime());
  }

  @Test
  public void testUpdateEndDateTime() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("enddatetime",
            "2025-06-01T11:00Z");

    assertEquals(ZonedDateTime.parse("2025-06-01T11:00Z"), updated.getEndDateTime());
  }

  @Test
  public void testUpdateStartDate() {
    SingleEvent original = createSampleEvent();
    String newDate = "2025-06-02";
    SingleEvent updated = original.withUpdatedProperty("startdate", newDate);

    ZonedDateTime expectedStart = LocalDate.parse(newDate)
            .atTime(ZonedDateTime.from(original.getStartDateTime()).toLocalTime())
            .atZone(zone);

    assertEquals(expectedStart, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEndDate() {
    SingleEvent original = createSampleEvent();
    String newDate = "2025-06-02";
    SingleEvent updated = original.withUpdatedProperty("enddate", newDate);

    ZonedDateTime expectedEnd = LocalDate.parse(newDate)
            .atTime(ZonedDateTime.from(original.getEndDateTime()).toLocalTime())
            .atZone(zone);

    assertEquals(expectedEnd, updated.getEndDateTime());
  }


  @Test
  public void testUpdateStartTime() {
    SingleEvent original = createSampleEvent();
    String newTime = "08:15";
    SingleEvent updated = original.withUpdatedProperty("starttime", newTime);

    ZonedDateTime expected = ZonedDateTime.from(original.getStartDateTime()).toLocalDate()
            .atTime(LocalTime.parse(newTime))
            .atZone(zone);

    assertEquals(expected, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEndTime() {
    SingleEvent original = createSampleEvent();
    String newTime = "11:15";
    SingleEvent updated = original.withUpdatedProperty("endtime", newTime);

    ZonedDateTime expected = ZonedDateTime.from(original.getEndDateTime()).toLocalDate()
            .atTime(LocalTime.parse(newTime))
            .atZone(zone);

    assertEquals(expected, updated.getEndDateTime());
  }

  @Test
  public void testUpdatePublicFlag() {
    SingleEvent original = createSampleEvent();
    SingleEvent updated = original.withUpdatedProperty("public", "false");

    assertFalse(updated.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateUnsupportedPropertyThrows() {
    SingleEvent original = createSampleEvent();
    original.withUpdatedProperty("unsupported", "value");
  }
}
import org.junit.Test;

import java.time.LocalDateTime;

import calendarapp.model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the SingleEvent class.
 */
public class SingleEventTest {

  @Test
  public void testCreateSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

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
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 23, 59);

    SingleEvent event = new SingleEvent("Holiday", start, end, "National holiday",
            "Home", true, true, null);

    assertTrue(event.isAllDay());
  }

  @Test
  public void testPrivateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 12, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 13, 0);

    SingleEvent event = new SingleEvent("Lunch", start, end, "Personal time",
            "Cafeteria", false, false, null);

    assertFalse(event.isPublic());
  }

}

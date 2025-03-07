
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

import calendarapp.model.SingleEvent;

public class SingleEventTest {
  private SingleEvent singleEvent;

  @Before
  public void setUp() {
    // Create a basic single event
    singleEvent = new SingleEvent(
            "Team Meeting",
            LocalDateTime.of(2025, 5, 10, 10, 0),
            LocalDateTime.of(2025, 5, 10, 11, 0),
            "Discuss project updates",
            "Conference Room A",
            true,
            false
    );
  }

  @Test
  public void testGetSubject() {
    assertEquals("Team Meeting", singleEvent.getSubject());
  }

  @Test
  public void testGetStartDateTime() {
    assertEquals(LocalDateTime.of(2025, 5, 10, 10, 0), singleEvent.getStartDateTime());
  }

  @Test
  public void testGetEndDateTime() {
    assertEquals(LocalDateTime.of(2025, 5, 10, 11, 0), singleEvent.getEndDateTime());
  }

  @Test
  public void testGetDescription() {
    assertEquals("Discuss project updates", singleEvent.getDescription());
  }

  @Test
  public void testGetLocation() {
    assertEquals("Conference Room A", singleEvent.getLocation());
  }

  @Test
  public void testIsPublic() {
    assertTrue(singleEvent.isPublic());
  }

  @Test
  public void testIsAllDay() {
    assertFalse(singleEvent.isAllDay());
  }

  @Test
  public void testAllDayEvent() {
    // Create an all-day event
    SingleEvent allDay = new SingleEvent(
            "Holiday",
            LocalDateTime.of(2025, 5, 15, 0, 0),
            LocalDateTime.of(2025, 5, 15, 23, 59),
            "",
            "",
            false,
            true
    );
    assertTrue(allDay.isAllDay());
  }
}

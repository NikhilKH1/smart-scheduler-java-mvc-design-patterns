import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import calendarapp.factory.EventInput;

public class EventInputTest {

  @Test
  public void testDefaultConstructor() {
    EventInput eventInput = new EventInput();
    assertNull(eventInput.getSubject());
    assertNull(eventInput.getStart());
    assertNull(eventInput.getEnd());
    assertNull(eventInput.getRepeatingDays());
    assertNull(eventInput.getRepeatTimes());
    assertNull(eventInput.getRepeatUntil());
    assertNull(eventInput.getDescription());
    assertNull(eventInput.getLocation());
    assertFalse(eventInput.isRecurring());
  }

  @Test
  public void testSettersAndGetters() {
    EventInput eventInput = new EventInput();
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0, 0, 0, ZoneId.systemDefault());
    eventInput.setSubject("Meeting");
    eventInput.setStart(start);
    eventInput.setEnd(end);
    eventInput.setRepeatingDays("Mon,Wed,Fri");
    eventInput.setRepeatTimes(10);
    eventInput.setRepeatUntil(ZonedDateTime.of(2025, 6, 30, 9, 0, 0, 0, ZoneId.systemDefault()));
    eventInput.setDescription("Team meeting for project updates");
    eventInput.setLocation("Room 101");
    eventInput.setRecurring(true);

    assertEquals("Meeting", eventInput.getSubject());
    assertEquals(start, eventInput.getStart());
    assertEquals(end, eventInput.getEnd());
    assertEquals("Mon,Wed,Fri", eventInput.getRepeatingDays());
    assertEquals(Integer.valueOf(10), eventInput.getRepeatTimes());
    assertEquals(ZonedDateTime.of(2025, 6, 30, 9, 0, 0, 0, ZoneId.systemDefault()), eventInput.getRepeatUntil());
    assertEquals("Team meeting for project updates", eventInput.getDescription());
    assertEquals("Room 101", eventInput.getLocation());
    assertTrue(eventInput.isRecurring());
  }

  @Test
  public void testSetRecurringFalse() {
    EventInput eventInput = new EventInput();
    eventInput.setRecurring(false);
    assertFalse(eventInput.isRecurring());
  }

  @Test
  public void testSetRepeatingDays() {
    EventInput eventInput = new EventInput();
    eventInput.setRepeatingDays("Mon,Tue,Thu");
    assertEquals("Mon,Tue,Thu", eventInput.getRepeatingDays());
  }

  @Test
  public void testSetRepeatTimes() {
    EventInput eventInput = new EventInput();
    eventInput.setRepeatTimes(5);
    assertEquals(Integer.valueOf(5), eventInput.getRepeatTimes());
  }

  @Test
  public void testSetRepeatUntil() {
    EventInput eventInput = new EventInput();
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 12, 31, 9, 0, 0, 0, ZoneId.systemDefault());
    eventInput.setRepeatUntil(repeatUntil);
    assertEquals(repeatUntil, eventInput.getRepeatUntil());
  }

  @Test
  public void testSetDescription() {
    EventInput eventInput = new EventInput();
    eventInput.setDescription("Weekly team check-in");
    assertEquals("Weekly team check-in", eventInput.getDescription());
  }

  @Test
  public void testSetLocation() {
    EventInput eventInput = new EventInput();
    eventInput.setLocation("Conference Room A");
    assertEquals("Conference Room A", eventInput.getLocation());
  }

  @Test
  public void testSetStartAndEnd() {
    EventInput eventInput = new EventInput();
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0, 0, 0, ZoneId.systemDefault());
    eventInput.setStart(start);
    eventInput.setEnd(end);
    assertEquals(start, eventInput.getStart());
    assertEquals(end, eventInput.getEnd());
  }

  @Test
  public void testNoRecurringEvent() {
    EventInput eventInput = new EventInput();
    eventInput.setSubject("One-time Event");
    eventInput.setRecurring(false);
    assertEquals("One-time Event", eventInput.getSubject());
    assertFalse(eventInput.isRecurring());
  }
}

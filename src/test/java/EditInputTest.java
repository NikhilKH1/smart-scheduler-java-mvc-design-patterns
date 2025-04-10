import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import calendarapp.factory.EditInput;

public class EditInputTest {

  @Test
  public void testConstructorWithTime() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    EditInput input = new EditInput("repeatingdays", "Meeting", start, end, "MTWRF", true);

    assertEquals("repeatingdays", input.getProperty());
    assertEquals("Meeting", input.getEventName());
    assertEquals(start, input.getFromStart());
    assertEquals(end, input.getFromEnd());
    assertEquals("MTWRF", input.getNewValue());
    assertTrue(input.isRecurring());
  }

  @Test
  public void testConstructorWithoutTime() {
    EditInput input = new EditInput("repeattimes", "Meeting", "10", true);

    assertEquals("repeattimes", input.getProperty());
    assertEquals("Meeting", input.getEventName());
    assertNull(input.getFromStart());
    assertNull(input.getFromEnd());
    assertEquals("10", input.getNewValue());
    assertTrue(input.isRecurring());
  }

  @Test
  public void testGetters() {

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    EditInput input = new EditInput("repeatuntil", "Meeting", start, end, "2025-06-30", true);

    assertEquals("repeatuntil", input.getProperty());
    assertEquals("Meeting", input.getEventName());
    assertEquals(start, input.getFromStart());
    assertEquals(end, input.getFromEnd());
    assertEquals("2025-06-30", input.getNewValue());
    assertTrue(input.isRecurring());
  }

  @Test
  public void testSetters() {
    EditInput input = new EditInput("repeatingdays", "Meeting", null, null, "MTWRF", true);

    input.setProperty("repeattimes");
    input.setEventName("Work Meeting");
    input.setNewValue("5");
    input.setRecurring(false);

    assertEquals("repeattimes", input.getProperty());
    assertEquals("Work Meeting", input.getEventName());
    assertNull(input.getFromStart());
    assertNull(input.getFromEnd());
    assertEquals("5", input.getNewValue());
    assertFalse(input.isRecurring());
  }

  @Test
  public void testConstructorAndSettersCombined() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    EditInput input = new EditInput("repeatuntil", "Meeting", start, end, "2025-06-30", true);

    input.setProperty("repeatingdays");
    input.setEventName("Team Meeting");
    input.setNewValue("MTWRF");
    input.setRecurring(false);

    assertEquals("repeatingdays", input.getProperty());
    assertEquals("Team Meeting", input.getEventName());
    assertEquals(start, input.getFromStart());
    assertEquals(end, input.getFromEnd());
    assertEquals("MTWRF", input.getNewValue());
    assertFalse(input.isRecurring());
  }
}

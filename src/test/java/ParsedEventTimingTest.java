import calendarapp.controller.ParsedEventTiming;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import static org.junit.Assert.*;

public class ParsedEventTimingTest {

  private ParsedEventTiming timing;

  @Before
  public void setUp() {
    timing = new ParsedEventTiming();
  }

  @Test
  public void testDefaultConstructor() {
    assertNull("Start time should be null by default", timing.getStart());
    assertNull("End time should be null by default", timing.getEnd());
    assertFalse("isAllDay should be false by default", timing.isAllDay());
    assertEquals("Index should be -1 by default", -1, timing.getIndex());
  }

  @Test
  public void testSetAndGetStart() {
    Temporal start = LocalDateTime.of(2025, 5, 10, 14, 30);
    timing.setStart(start);
    assertEquals("Start time should be set correctly", start, timing.getStart());

    // Edge case: Setting to null
    timing.setStart(null);
    assertNull("Start time should be null after setting null", timing.getStart());
  }

  @Test
  public void testSetAndGetEnd() {
    Temporal end = LocalDateTime.of(2025, 5, 10, 16, 0);
    timing.setEnd(end);
    assertEquals("End time should be set correctly", end, timing.getEnd());

    // Edge case: Setting to null
    timing.setEnd(null);
    assertNull("End time should be null after setting null", timing.getEnd());
  }

  @Test
  public void testSetAndGetIsAllDay() {
    timing.setAllDay(true);
    assertTrue("isAllDay should be true when set to true", timing.isAllDay());

    timing.setAllDay(false);
    assertFalse("isAllDay should be false when set to false", timing.isAllDay());
  }

  @Test
  public void testSetAndGetIndex() {
    timing.setIndex(5);
    assertEquals("Index should be set to 5", 5, timing.getIndex());

    // Edge case: Negative index
    timing.setIndex(-3);
    assertEquals("Index should be set to -3", -3, timing.getIndex());

    // Edge case: Zero index
    timing.setIndex(0);
    assertEquals("Index should be set to 0", 0, timing.getIndex());
  }
}

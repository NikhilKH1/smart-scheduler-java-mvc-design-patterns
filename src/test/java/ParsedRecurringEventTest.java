import calendarapp.controller.ParsedRecurringEvent;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.temporal.Temporal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for ParsedRecurringEvent.
 */
public class ParsedRecurringEventTest {

  private TestParsedRecurringEvent recurringEvent;

  /**
   * Subclass to access protected fields.
   */
  private static class TestParsedRecurringEvent extends ParsedRecurringEvent {

    @Override
    public void setRecurring(boolean recurring) {
      this.isRecurring = recurring;
    }

    @Override
    public void setWeekdays(String days) {
      this.weekdays = days;
    }

    @Override
    public void setRepeatCount(int count) {
      this.repeatCount = count;
    }

    @Override
    public void setRepeatUntil(Temporal until) {
      this.repeatUntil = until;
    }

    @Override
    public void setIndex(int index) {
      this.index = index;
    }
  }

  @Before
  public void setUp() {
    recurringEvent = new TestParsedRecurringEvent();
  }

  @Test
  public void testDefaultConstructor() {
    assertFalse("isRecurring should be false by default", recurringEvent.isRecurring());
    assertEquals("Weekdays should be empty string by default", "",
            recurringEvent.getWeekdays());
    assertEquals("Repeat count should be 0 by default", 0,
            recurringEvent.getRepeatCount());
    assertNull("RepeatUntil should be null by default",
            recurringEvent.getRepeatUntil());
    assertEquals("Index should be -1 by default", -1, recurringEvent.getIndex());
  }

  @Test
  public void testSetAndGetIsRecurring() {
    recurringEvent.setRecurring(true);
    assertTrue("isRecurring should be true when set to true",
            recurringEvent.isRecurring());

    recurringEvent.setRecurring(false);
    assertFalse("isRecurring should be false when set to false",
            recurringEvent.isRecurring());
  }

  @Test
  public void testSetAndGetWeekdays() {
    recurringEvent.setWeekdays("MTWRF");
    assertEquals("Weekdays should be set correctly", "MTWRF",
            recurringEvent.getWeekdays());
    recurringEvent.setWeekdays("");
    assertEquals("Weekdays should be empty when set to empty", "",
            recurringEvent.getWeekdays());
  }

  @Test
  public void testSetAndGetRepeatCount() {
    recurringEvent.setRepeatCount(5);
    assertEquals("Repeat count should be set correctly", 5,
            recurringEvent.getRepeatCount());
    recurringEvent.setRepeatCount(-1);
    assertEquals("Repeat count should be -1 when set to -1", -1,
            recurringEvent.getRepeatCount());

    recurringEvent.setRepeatCount(0);
    assertEquals("Repeat count should be 0 when set to 0", 0,
            recurringEvent.getRepeatCount());
  }

  @Test
  public void testSetAndGetRepeatUntil() {
    Temporal repeatUntilDate = LocalDate.of(2025, 12, 31);
    recurringEvent.setRepeatUntil(repeatUntilDate);
    assertEquals("RepeatUntil should be set correctly", repeatUntilDate,
            recurringEvent.getRepeatUntil());

    recurringEvent.setRepeatUntil(null);
    assertNull("RepeatUntil should be null after setting null",
            recurringEvent.getRepeatUntil());
  }

  @Test
  public void testSetAndGetIndex() {
    recurringEvent.setIndex(5);
    assertEquals("Index should be set to 5", 5, recurringEvent.getIndex());

    recurringEvent.setIndex(-3);
    assertEquals("Index should be set to -3", -3, recurringEvent.getIndex());

    recurringEvent.setIndex(0);
    assertEquals("Index should be set to 0", 0, recurringEvent.getIndex());
  }
}
import calendarapp.controller.RecurringResult;

import org.junit.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the RecurringResult class.
 */
public class RecurringResultTest {

  @Test
  public void testDefaultConstructor() {
    RecurringResult result = new RecurringResult();

    assertFalse("Default isRecurring should be false", result.isRecurring());
    assertEquals("Default weekdays should be empty", "", result.getWeekdays());
    assertEquals("Default repeatCount should be 0", 0, result.getRepeatCount());
    assertNull("Default repeatUntil should be null", result.getRepeatUntil());
    assertEquals("Default index should be 0", 0, result.getIndex());
  }

  @Test
  public void testSetAndGetFieldsUsingReflection() throws Exception {
    RecurringResult result = new RecurringResult();

    setField(result, "isRecurring", true);
    setField(result, "weekdays", "MTWRF");
    setField(result, "repeatCount", 10);
    setField(result, "repeatUntil", LocalDateTime.of(2025, 8, 15,
            12, 0));
    setField(result, "index", 7);

    assertTrue(result.isRecurring());
    assertEquals("MTWRF", result.getWeekdays());
    assertEquals(10, result.getRepeatCount());
    assertEquals(LocalDateTime.of(2025, 8, 15, 12, 0),
            result.getRepeatUntil());
    assertEquals(7, result.getIndex());
  }

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }
}

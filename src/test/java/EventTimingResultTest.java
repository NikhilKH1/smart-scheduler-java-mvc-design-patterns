import calendarapp.controller.EventTimingResult;
import org.junit.Test;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

/**
 * JUnit tests for the EventTimingResult class.
 */
public class EventTimingResultTest {

  @Test
  public void testDefaultConstructor() {
    EventTimingResult result = new EventTimingResult();

    assertNull("Default start should be null", result.getStart());
    assertNull("Default end should be null", result.getEnd());
    assertFalse("Default isAllDay should be false", result.isAllDay());
    assertEquals("Default index should be 0", 0, result.getIndex());
  }

  @Test
  public void testSetAndGetFieldsUsingReflection() throws Exception {
    EventTimingResult result = new EventTimingResult();

    LocalDateTime startTime = LocalDateTime.of(2025, 6, 10, 9, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 6, 10, 17, 0);

    setField(result, "start", startTime);
    setField(result, "end", endTime);
    setField(result, "isAllDay", true);
    setField(result, "index", 3);

    assertEquals(startTime, result.getStart());
    assertEquals(endTime, result.getEnd());
    assertTrue(result.isAllDay());
    assertEquals(3, result.getIndex());
  }

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }
}

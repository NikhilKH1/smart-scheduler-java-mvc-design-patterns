import calendarapp.controller.PropertiesResult;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * JUnit tests for the PropertiesResult class.
 */
public class PropertiesResultTest {

  @Test
  public void testDefaultConstructor() throws Exception {
    PropertiesResult result = new PropertiesResult();

    assertNull("Default description should be null", result.getDescription());
    assertNull("Default location should be null", result.getLocation());
    assertFalse("Default isPublic should be false", result.isPublic());
    assertEquals("Default index should be 0", 0, result.getIndex());
  }

  @Test
  public void testSetAndGetFieldsUsingReflection() throws Exception {
    PropertiesResult result = new PropertiesResult();

    setField(result, "description", "Team Meeting");
    setField(result, "location", "Room A");
    setField(result, "isPublic", true);
    setField(result, "index", 5);

    assertEquals("Team Meeting", result.getDescription());
    assertEquals("Room A", result.getLocation());
    assertTrue(result.isPublic());
    assertEquals(5, result.getIndex());
  }

  private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }
}

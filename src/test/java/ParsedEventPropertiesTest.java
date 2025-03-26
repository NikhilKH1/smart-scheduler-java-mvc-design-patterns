import org.junit.Before;
import org.junit.Test;

import calendarapp.controller.ParsedEventProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * JUnit test class for ParsedEventProperties.
 */
public class ParsedEventPropertiesTest {

  private TestParsedEventProperties properties;

  /**
   * Subclass to access protected fields.
   */
  private static class TestParsedEventProperties extends ParsedEventProperties {
    void setDescription(String desc) {
      this.description = desc;
    }

    void setLocation(String loc) {
      this.location = loc;
    }

    void setIsPublic(boolean isPublic) {
      this.isPublic = isPublic;
    }

    void setIndex(int index) {
      this.index = index;
    }
  }

  @Before
  public void setUp() {
    properties = new TestParsedEventProperties();
  }

  @Test
  public void testDefaultConstructor() {
    assertNull("Description should be null by default", properties.getDescription());
    assertNull("Location should be null by default", properties.getLocation());
    assertFalse("isPublic should be false by default", properties.isPublic());
    assertEquals("Index should be 0 by default", 0, properties.getIndex());
  }

  @Test
  public void testSetAndGetDescription() {
    properties.setDescription("Meeting with client");
    assertEquals("Meeting with client", properties.getDescription());
  }

  @Test
  public void testSetAndGetLocation() {
    properties.setLocation("Conference Room A");
    assertEquals("Conference Room A", properties.getLocation());
  }

  @Test
  public void testSetAndGetIsPublic() {
    properties.setIsPublic(true);
    assertTrue(properties.isPublic());

    properties.setIsPublic(false);
    assertFalse(properties.isPublic());
  }

  @Test
  public void testSetAndGetIndex() {
    properties.setIndex(5);
    assertEquals(5, properties.getIndex());

    properties.setIndex(-1);
    assertEquals(-1, properties.getIndex());
  }
}



import calendarapp.model.event.SingleEvent;
import calendarapp.utils.ModelHelper;

import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class ModelHelperTest {

  @Test
  public void testUpdateEventDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Meeting", start, end, "Old Description", "Office", true, false, null);

    SingleEvent updatedEvent = ModelHelper.createUpdatedEvent(event, "description", "New Description");

    assertNotNull(updatedEvent);
    assertEquals("New Description", updatedEvent.getDescription());
  }

  @Test
  public void testUpdateEventLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Meeting", start, end, "Discussion", "Old Office", true, false, null);

    SingleEvent updatedEvent = ModelHelper.createUpdatedEvent(event, "location", "New Conference Room");

    assertNotNull(updatedEvent);
    assertEquals("New Conference Room", updatedEvent.getLocation());
  }

  @Test
  public void testInvalidPropertyUpdate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Meeting", start, end, "Discussion", "Office", true, false, null);

    try {
      ModelHelper.createUpdatedEvent(event, "invalidProperty", "New Value");
      fail("Should throw IllegalArgumentException for invalid property");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().contains("invalidProperty"));
    }
  }
}

import calendarapp.model.event.SingleEvent;
import calendarapp.utils.ModelHelper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
  public void testUpdateEventName() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("OldName", start, end, "Desc", "Loc", true, false, null);

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "name", "NewName");

    assertEquals("NewName", updated.getSubject());
    assertEquals("Desc", updated.getDescription());
    assertEquals("Loc", updated.getLocation());
  }

  @Test
  public void testUpdateEventStartDateTime() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newStart = "2025-06-01T08:30";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "startdatetime", newStart);

    assertEquals(LocalDateTime.parse(newStart), updated.getStartDateTime());
    assertEquals(end, updated.getEndDateTime());
  }

  @Test
  public void testUpdateEventEndDateTime() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newEnd = "2025-06-01T10:30";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "enddatetime", newEnd);

    assertEquals(LocalDateTime.parse(newEnd), updated.getEndDateTime());
    assertEquals(start, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEventStartDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 15);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newDate = "2025-06-02";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "startdate", newDate);
    LocalDateTime expectedStart = LocalDate.parse(newDate).atTime(start.toLocalTime());

    assertEquals(expectedStart, updated.getStartDateTime());
    assertEquals(end, updated.getEndDateTime());
  }

  @Test
  public void testUpdateEventEndDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 30);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newDate = "2025-06-02";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "enddate", newDate);
    LocalDateTime expectedEnd = LocalDate.parse(newDate).atTime(end.toLocalTime());

    assertEquals(expectedEnd, updated.getEndDateTime());
    assertEquals(start, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEventStartTime() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newTime = "08:45";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "starttime", newTime);
    LocalDateTime expectedStart = start.toLocalDate().atTime(LocalTime.parse(newTime));

    assertEquals(expectedStart, updated.getStartDateTime());
    assertEquals(end, updated.getEndDateTime());
  }

  @Test
  public void testUpdateEventEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    String newTime = "10:30";

    SingleEvent updated = ModelHelper.createUpdatedEvent(event, "endtime", newTime);
    LocalDateTime expectedEnd = end.toLocalDate().atTime(LocalTime.parse(newTime));

    assertEquals(expectedEnd, updated.getEndDateTime());
    assertEquals(start, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEventPublic() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent eventTrue = new SingleEvent("Event", start, end, "Desc", "Loc", true, false, null);
    SingleEvent eventFalse = new SingleEvent("Event", start, end, "Desc", "Loc", false, false, null);

    // Change true -> false
    SingleEvent updatedFalse = ModelHelper.createUpdatedEvent(eventTrue, "public", "false");
    assertFalse(updatedFalse.isPublic());

    // Change false -> true
    SingleEvent updatedTrue = ModelHelper.createUpdatedEvent(eventFalse, "public", "true");
    assertTrue(updatedTrue.isPublic());
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
      assertTrue(e.getMessage().contains("invalidProperty"));
    }
  }
}

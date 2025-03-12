import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarModelTest {
  private CalendarModel model;

  @Before
  public void setUp() {
    model = new CalendarModel();
  }

  @Test
  public void testAddNonOverlappingEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 30);

    boolean added1 = model.addEvent(new SingleEvent("Event A", start1, end1, "", "", true, false, null), false);
    boolean added2 = model.addEvent(new SingleEvent("Event B", start2, end2, "", "", true, false, null), false);

    assertTrue("First event should be added successfully", added1);
    assertTrue("Second event should be added successfully", added2);
  }

  @Test
  public void testEventConflictDetection() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 9, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 10, 30);

    model.addEvent(new SingleEvent("Meeting A", start1, end1, "", "", true, false, null), true);
    boolean added = model.addEvent(new SingleEvent("Meeting B", start2, end2, "", "", true, false, null), true);

    assertFalse("Overlapping event should not be added", added);
  }

  @Test
  public void testAddRecurringEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    String weekdays = "MTWRF";

    RecurringEvent recurringEvent = new RecurringEvent(
            "Daily Standup", start, end, weekdays, 5, null, "Daily meeting", "Office", true, false
    );

    boolean added = model.addRecurringEvent(recurringEvent, false);
    assertTrue("Recurring event should be added successfully", added);
  }

  @Test
  public void testRecurringEventGeneration() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    String weekdays = "MTW";

    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting", start, end, weekdays, 3, null, "Discussion", "Room 101", true, false
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences("123-series");
    assertEquals("Recurring event should generate exactly 3 occurrences", 3, occurrences.size());
  }


  @Test
  public void testGetEventsOnDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    model.addEvent(new SingleEvent("Meeting", start, end, "", "", true, false, null), false);

    List<CalendarEvent> events = model.getEventsOnDate(LocalDate.of(2025, 6, 1));
    assertEquals("Should return 1 event on this date", 1, events.size());
  }

  @Test
  public void testGetEventsBetween() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    model.addEvent(new SingleEvent("Workshop", start, end, "", "", true, false, null), false);

    List<CalendarEvent> events = model.getEventsBetween(
            LocalDateTime.of(2025, 6, 1, 8, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0)
    );

    assertEquals("Should return 1 event in range", 1, events.size());
  }

  @Test
  public void testIsBusyAt() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    model.addEvent(new SingleEvent("Interview", start, end, "", "", true, false, null), false);

    assertTrue("Should be busy at 9:30 AM", model.isBusyAt(LocalDateTime.of(2025, 6, 1, 9, 30)));
    assertFalse("Should not be busy at 8:30 AM", model.isBusyAt(LocalDateTime.of(2025, 6, 1, 8, 30)));
  }

  @Test
  public void testEditSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Conference", start, end, "Tech Talk", "Hall A", true, false, null);
    model.addEvent(event, false);

    boolean edited = model.editSingleEvent("description", "Conference", start, end, "Updated Tech Talk");
    assertTrue("Event should be edited", edited);

    CalendarEvent updatedEvent = model.getEvents().get(0);
    assertEquals("Description should be updated", "Updated Tech Talk", updatedEvent.getDescription());
  }

  @Test
  public void testAddConflictingEventWithoutAutoDecline() {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 8, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 8, 11, 0);

    model.addEvent(new SingleEvent("Team Meeting", start1, end1, "", "", true, false, null), false);
    boolean added = model.addEvent(new SingleEvent("Darshan", start2, end2, "Darshan is a shade", "CKM", true, false, null), false);

    assertTrue("Conflicting event should be added when autoDecline is false", added);
    assertEquals("Both events should exist in the calendar", 2, model.getEvents().size());
  }

  @Test
  public void testAddConflictingEventWithAutoDecline() {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 8, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 8, 11, 0);

    model.addEvent(new SingleEvent("Team Meeting", start1, end1, "", "", true, false, null), false);
    boolean added = model.addEvent(new SingleEvent("Darshan", start2, end2, "Darshan is a shade", "CKM", true, false, null), true);

    assertFalse("Conflicting event should not be added when autoDecline is true", added);
    assertEquals("Only the first event should exist in the calendar", 1, model.getEvents().size());
  }

}

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The Calendar Manager JUnit test for reviewing the functionalities
 * of the CalendarManager class
 */
public class CalendarManagerTest {

  private CalendarManager manager;

  @Before
  public void setUp() {
    manager = new CalendarManager();
  }

  @Test
  public void testAddCalendarSuccessfully() {
    boolean added = manager.addCalendar("Work", ZoneId.of("Asia/Kolkata"));
    assertTrue(added);
    assertNotNull(manager.getCalendar("Work"));
  }

  @Test
  public void testAddCalendarDuplicateNameFails() {
    manager.addCalendar("Work", ZoneId.of("Asia/Kolkata"));
    boolean result = manager.addCalendar("Work", ZoneId.of("Europe/Paris"));
    assertFalse(result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistentCalendarThrowsException() {
    manager.editCalendar("Unknown", "name", "NewName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarToDuplicateNameFails() {
    manager.addCalendar("A", ZoneId.of("Asia/Kolkata"));
    manager.addCalendar("B", ZoneId.of("Asia/Tokyo"));
    manager.editCalendar("A", "name", "B");
  }

  @Test
  public void testEditCalendarNameSuccessfully() {
    manager.addCalendar("Old", ZoneId.of("Asia/Kolkata"));
    boolean result = manager.editCalendar("Old", "name", "New");
    assertTrue(result);
    assertNull(manager.getCalendar("Old"));
    assertNotNull(manager.getCalendar("New"));
  }

  @Test
  public void testEditCalendarTimezoneSuccessfully() {
    manager.addCalendar("MyCal", ZoneId.of("Asia/Kolkata"));
    boolean result = manager.editCalendar("MyCal", "timezone",
            "Europe/Paris");
    assertTrue(result);
    assertEquals(ZoneId.of("Europe/Paris"),
            manager.getCalendar("MyCal").getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidTimezoneThrows() {
    manager.addCalendar("Cal", ZoneId.of("Asia/Kolkata"));
    manager.editCalendar("Cal", "timezone", "Invalid/Zone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarUnsupportedProperty() {
    manager.addCalendar("Cal", ZoneId.of("Asia/Kolkata"));
    manager.editCalendar("Cal", "color", "Red");
  }

  @Test
  public void testUseCalendarSuccessfully() {
    manager.addCalendar("Personal", ZoneId.of("Asia/Kolkata"));
    boolean result = manager.useCalendar("Personal");
    assertTrue(result);
    assertEquals("Personal", manager.getActiveCalendar().getName());
  }

  @Test
  public void testUseNonExistentCalendarFails() {
    boolean result = manager.useCalendar("NonExistent");
    assertFalse(result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopySingleEventFailsIfCalendarNotFound() {
    manager.copySingleEvent("Meeting",
            ZonedDateTime.now(),
            "TargetCal",
            ZonedDateTime.now().plusHours(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateFailsIfCalendarNotFound() {
    manager.copyEventsOnDate(LocalDate.now(), "Target",
            LocalDate.now().plusDays(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetweenFailsIfCalendarNotFound() {
    manager.copyEventsBetween(LocalDate.now(), LocalDate.now().plusDays(1),
            "Target", LocalDate.now().plusDays(2));
  }

  @Test
  public void testGetCalendarWithQuotes() {
    manager.addCalendar("QuotedCal", ZoneId.of("America/New_York"));
    CalendarModel cal = manager.getCalendar("\"QuotedCal\"");
    assertNotNull(cal);
    assertEquals("QuotedCal", cal.getName());
  }

  @Test
  public void testCopySingleEventBetweenCalendars() {
    manager.addCalendar("Source", ZoneId.of("UTC"));
    manager.addCalendar("Target", ZoneId.of("America/New_York"));
    manager.useCalendar("Source");

    CalendarModel source = manager.getCalendar("Source");
    CalendarModel target = manager.getCalendar("Target");

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    source.addEvent(new SingleEvent("Lecture", start, end, "", "",
            true, false, null), false);

    boolean result = manager.copySingleEvent("Lecture", start,
            "Target",
            ZonedDateTime.of(2025, 6, 2, 9, 0, 0,
                    0, ZoneId.of("America/New_York")));

    assertTrue(result);
    assertEquals(1, target.getEvents().size());
  }

  @Test
  public void testCopyEventsOnDateToAnotherCalendar() {
    manager.addCalendar("Semester2024", ZoneId.of("UTC"));
    manager.addCalendar("Semester2025", ZoneId.of("UTC"));
    manager.useCalendar("Semester2024");

    CalendarModel source = manager.getCalendar("Semester2024");
    LocalDate sourceDate = LocalDate.of(2025, 6, 1);
    source.addEvent(new SingleEvent("Exam",
            ZonedDateTime.of(2025, 6, 1, 10, 0,
                    0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 6, 1, 11, 0,
                    0, 0, ZoneId.of("UTC")),
            "Finals", "Room 101", true, false,
            null), false);

    boolean result = manager.copyEventsOnDate(sourceDate, "Semester2025",
            LocalDate.of(2025, 8, 1));

    assertTrue(result);
    assertEquals(1, manager.getCalendar("Semester2025").getEvents().size());
  }

  @Test
  public void testCopyEventsBetweenCalendarsWithTimezoneConversion() {
    manager.addCalendar("Fall2024", ZoneId.of("Asia/Kolkata"));
    manager.addCalendar("Spring2025", ZoneId.of("America/New_York"));
    manager.useCalendar("Fall2024");

    CalendarModel source = manager.getCalendar("Fall2024");

    source.addEvent(new SingleEvent("Lecture",
            ZonedDateTime.of(2024, 9, 5, 10, 0,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2024, 9, 5, 11, 0,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            "Intro", "Room A", true, false,
            null), false);

    boolean copied = manager.copyEventsBetween(
            LocalDate.of(2024, 9, 5),
            LocalDate.of(2024, 9, 5),
            "Spring2025",
            LocalDate.of(2025, 1, 8)
    );

    assertTrue(copied);
    List<ICalendarEvent> springEvents = manager.getCalendar("Spring2025").getEvents();
    assertEquals(1, springEvents.size());
    assertEquals("Lecture", springEvents.get(0).getSubject());
  }

  @Test
  public void testAddConflictingEventsInSameCalendarFails() {
    manager.addCalendar("ConflictsCal", ZoneId.of("UTC"));
    manager.useCalendar("ConflictsCal");

    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9, 30,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 10, 30,
            0, 0, ZoneId.of("UTC"));

    CalendarModel model = manager.getActiveCalendar();
    assertTrue(model.addEvent(new SingleEvent("Meeting A", start1, end1, "",
            "", true, false, null), false));
    assertFalse(model.addEvent(new SingleEvent("Meeting B", start2, end2, "",
            "", true, false, null), false));
  }

  @Test
  public void testRecurringEventConflictsWithExistingSingleEventFails() {
    manager.addCalendar("ConflictRecurring", ZoneId.of("UTC"));
    manager.useCalendar("ConflictRecurring");

    CalendarModel model = manager.getActiveCalendar();
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, ZoneId.of("UTC"));

    assertTrue(model.addEvent(new SingleEvent("Blocked Slot", start, end, "",
            "", true, false, null), false));

    RecurringEvent re = new RecurringEvent("Standup", start, end, "MW",
            3, null, "", "", true, false);
    assertFalse(model.addRecurringEvent(re, false));
  }

  @Test
  public void testEditEventCreatesConflictFails() {
    manager.addCalendar("EditConflict", ZoneId.of("UTC"));
    manager.useCalendar("EditConflict");

    CalendarModel model = manager.getActiveCalendar();
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 3, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 3, 11,
            0, 0, 0, ZoneId.of("UTC"));

    SingleEvent event1 = new SingleEvent("Event1", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event2", start2, end2, "",
            "", true, false, null);

    assertTrue(model.addEvent(event1, false));
    assertTrue(model.addEvent(event2, false));

    SingleEvent updated = new SingleEvent("Event1", start2, end2, "Updated",
            "", true, false, null);
    assertFalse(model.editEvent(event1, updated));
  }

  @Test
  public void testAddConflictingSingleEventFails() {
    manager.addCalendar("Work", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("Work");
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 11,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));
    manager.getActiveCalendar().addEvent(new SingleEvent("A", start, end,
            "", "", true, false, null), false);

    ZonedDateTime newStart = ZonedDateTime.of(2025, 6, 1,
            10, 30, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime newEnd = ZonedDateTime.of(2025, 6, 1, 11,
            30, 0, 0, ZoneId.of("Asia/Kolkata"));
    boolean result = manager.getActiveCalendar().addEvent(
            new SingleEvent("B", newStart, newEnd, "", "",
                    true, false, null), false);

    assertFalse("Conflict should prevent second event", result);
  }

  @Test
  public void testEditEventThatCausesConflictFails() {
    manager.addCalendar("Work", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("Work");
    CalendarModel model = manager.getActiveCalendar();

    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 11,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 11,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 12,
            0, 0, 0, ZoneId.of("Asia/Kolkata"));

    SingleEvent event1 = new SingleEvent("Event1", start1, end1, "",
            "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event2", start2, end2, "",
            "", true, false, null);

    assertTrue(model.addEvent(event1, false));
    assertTrue(model.addEvent(event2, false));
    SingleEvent updatedEvent = new SingleEvent("Event1",
            ZonedDateTime.of(2025, 6, 1, 10, 30,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 6, 1, 11, 30,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            "", "", true, false, null);

    boolean edited = model.editEvent(event1, updatedEvent);
    assertFalse("Edit should fail due to conflict with Event2", edited);
  }

  @Test
  public void testRecurringEventConflictsWithSingleEventFails() {
    manager.addCalendar("Work", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("Work");
    CalendarModel model = manager.getActiveCalendar();

    RecurringEvent recurring = new RecurringEvent("Repeat",
            ZonedDateTime.of(2025, 6, 2, 9, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 6, 2, 10, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            "M", 3, null, "", "",
            true, false);

    assertTrue(model.addRecurringEvent(recurring, false));

    ZonedDateTime conflictStart = ZonedDateTime.of(2025, 6, 9,
            9, 30, 0, 0, ZoneId.of("Asia/Kolkata"));
    ZonedDateTime conflictEnd = ZonedDateTime.of(2025, 6, 9,
            10, 30, 0, 0, ZoneId.of("Asia/Kolkata"));

    SingleEvent conflict = new SingleEvent("Overlap", conflictStart, conflictEnd,
            "", "", true, false, null);
    assertFalse("Should reject conflict with recurring occurrence",
            model.addEvent(conflict, false));
  }

  @Test
  public void testRecurringEventConflictsWithRecurringEventFails() {
    manager.addCalendar("Class", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("Class");
    CalendarModel model = manager.getActiveCalendar();

    RecurringEvent first = new RecurringEvent("Yoga",
            ZonedDateTime.of(2025, 6, 2, 7, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 6, 2, 8, 0, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            "MWF", 5, null, "Morning", "Gym",
            true, false);

    assertTrue(model.addRecurringEvent(first, false));
    RecurringEvent conflict = new RecurringEvent("HIIT",
            ZonedDateTime.of(2025, 6, 2, 7, 30, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2025, 6, 2, 8, 30, 0,
                    0, ZoneId.of("Asia/Kolkata")),
            "MWF", 5, null, "Conflict", "Gym",
            true, false);

    assertFalse("Recurring event should conflict with existing recurring",
            model.addRecurringEvent(conflict, false));
  }

  @Test
  public void testCalendarTimezoneChangeUpdatesEventTimes() {
    manager.addCalendar("TZCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TZCal");

    ZonedDateTime originalStart = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0,
            ZoneId.of("America/New_York"));
    ZonedDateTime originalEnd = originalStart.plusHours(1);

    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            originalStart, originalEnd, "Planning", "Room 1", true,
            false, null), false);

    manager.editCalendar("TZCal", "timezone", "Europe/Paris");

    List<ICalendarEvent> updatedEvents = manager.getActiveCalendar().getEvents();
    assertEquals(1, updatedEvents.size());

    assertEquals("Europe/Paris",
            ZonedDateTime.from(updatedEvents.get(0).getStartDateTime()).getZone().getId());
  }

  @Test
  public void testCopiedEventAdjustsToTargetCalendarTimezone() {
    manager.addCalendar("Source", ZoneId.of("America/New_York"));
    manager.addCalendar("Target", ZoneId.of("Asia/Tokyo"));

    manager.useCalendar("Source");

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 6, 2, 8,
            0, 0, 0,
            ZoneId.of("America/New_York"));
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);

    manager.getActiveCalendar().addEvent(
            new SingleEvent("Call", sourceStart, sourceEnd,
                    "Client Call", "Zoom", true, false,
                    null), false);

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 6, 5, 9,
            0, 0, 0,
            ZoneId.of("Asia/Tokyo"));

    boolean result = manager.copySingleEvent("Call", sourceStart,
            "Target", targetStart);
    assertTrue(result);

    List<ICalendarEvent> eventsInTarget = manager.getCalendar("Target").getEvents();
    assertEquals(1, eventsInTarget.size());
    assertEquals("Call", eventsInTarget.get(0).getSubject());
    assertEquals(targetStart, eventsInTarget.get(0).getStartDateTime());
    assertEquals("Asia/Tokyo",
            ZonedDateTime.from(eventsInTarget.get(0).getStartDateTime()).getZone().getId());
  }

  @Test
  public void testEditEventsFromOnlyAppliesToLaterEvents() {
    manager.addCalendar("Cal", ZoneId.of("UTC"));
    manager.useCalendar("Cal");

    ZonedDateTime early = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime mid = ZonedDateTime.of(2025, 6, 2, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime late = ZonedDateTime.of(2025, 6, 3, 9,
            0, 0, 0, ZoneId.of("UTC"));

    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            early, early.plusHours(1), "A", "Room", true,
            false, null), false);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            mid, mid.plusHours(1), "B", "Room", true,
            false, null), false);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            late, late.plusHours(1), "C", "Room", true,
            false, null), false);

    boolean result = manager.getActiveCalendar().editEventsFrom("description",
            "Meeting", mid, "Updated");

    assertTrue(result);
    List<ICalendarEvent> events = manager.getActiveCalendar().getEvents();
    for (ICalendarEvent e : events) {
      if (e.getStartDateTime().equals(early)) {
        assertEquals("A", e.getDescription());
      } else {
        assertEquals("Updated", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsAllUpdatesAllMatchingEvents() {
    manager.addCalendar("AllEditCal", ZoneId.of("UTC"));
    manager.useCalendar("AllEditCal");

    for (int i = 1; i <= 3; i++) {
      ZonedDateTime start = ZonedDateTime.of(2025, 6, i, 10, 0, 0,
              0, ZoneId.of("UTC"));
      ZonedDateTime end = start.plusHours(1);
      manager.getActiveCalendar().addEvent(
              new SingleEvent("Workshop", start, end, "Old Desc", "Loc",
                      true, false, null),
              false);
    }

    boolean result = manager.getActiveCalendar().editEventsAll("description",
            "Workshop", "New Desc");
    assertTrue(result);

    for (ICalendarEvent e : manager.getActiveCalendar().getEvents()) {
      assertEquals("New Desc", e.getDescription());
    }
  }

  @Test
  public void testEditEventsFromNoMatchReturnsFalse() {
    manager.addCalendar("NoMatchCal", ZoneId.of("UTC"));
    manager.useCalendar("NoMatchCal");

    ZonedDateTime dt = ZonedDateTime.of(2025, 6, 10, 10, 0,
            0, 0, ZoneId.of("UTC"));
    boolean result = manager.getActiveCalendar().editEventsFrom("location",
            "NonExistent", dt, "New Loc");
    assertFalse(result);
  }

  @Test
  public void testEditEventsAllNoMatchReturnsFalse() {
    manager.addCalendar("NoMatchAll", ZoneId.of("UTC"));
    manager.useCalendar("NoMatchAll");

    boolean result = manager.getActiveCalendar().editEventsAll("location",
            "GhostEvent", "Nowhere");
    assertFalse(result);
  }

  @Test
  public void testConflictOnCreateEventFails() {
    manager.addCalendar("ConflictsCal", ZoneId.of("UTC"));
    manager.useCalendar("ConflictsCal");

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = start.plusHours(1);

    SingleEvent first = new SingleEvent("Event A", start, end, "",
            "", true, false, null);
    SingleEvent conflict = new SingleEvent("Event B", start.plusMinutes(30),
            end.plusMinutes(30), "", "", true, false,
            null);

    assertTrue(manager.getActiveCalendar().addEvent(first, false));
    assertFalse(manager.getActiveCalendar().addEvent(conflict, false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateEventDetectionThrows() {
    manager.addCalendar("DupCal", ZoneId.of("UTC"));
    manager.useCalendar("DupCal");

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = start.plusHours(1);

    SingleEvent e1 = new SingleEvent("Standup", start, end, "", "",
            true, false, null);
    SingleEvent e2 = new SingleEvent("Standup", start, end, "", "",
            true, false, null);

    manager.getActiveCalendar().addEvent(e1, false);
    manager.getActiveCalendar().addEvent(e2, false);
  }

  @Test
  public void testValidEditsOnSingleEvent() {
    manager.addCalendar("EditPropsCal", ZoneId.of("UTC"));
    manager.useCalendar("EditPropsCal");

    ZonedDateTime startA = ZonedDateTime.of(2025, 6, 3, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime endA = startA.plusHours(1);

    ZonedDateTime startB = ZonedDateTime.of(2025, 6, 3, 12,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime endB = startB.plusHours(1);

    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting A", startA, endA,
            "", "", true, false, null),
            false);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting B", startB, endB,
                    "Old Desc", "Room 1", true, false,
                    null),
            false);

    boolean updatedDesc = manager.getActiveCalendar().editSingleEvent("description",
            "Meeting B", startB, endB, "Updated Desc");
    assertTrue(updatedDesc);

    boolean updatedLoc = manager.getActiveCalendar().editSingleEvent("location",
            "Meeting B", startB, endB, "Room 2");
    assertTrue(updatedLoc);
  }


}


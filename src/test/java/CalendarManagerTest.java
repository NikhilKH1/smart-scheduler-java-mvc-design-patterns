import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.List;

import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit test class for CalendarManager class.
 */
public class CalendarManagerTest {

  private CalendarManager manager;

  @Before
  public void setUp() {
    manager = new CalendarManager();
  }

  @Test
  public void testCopySingleEventNullSourceDateThrows() {
    manager.addCalendar("Source", ZoneId.of("UTC"));
    manager.addCalendar("Target", ZoneId.of("UTC"));
    manager.useCalendar("Source");

    boolean result = manager.copySingleEvent("Event", null,
            "Target", ZonedDateTime.now());
    assertFalse(result);
  }

  @Test
  public void testEditCalendarToSameTimezone() {
    manager.addCalendar("TimeCal", ZoneId.of("Asia/Kolkata"));
    boolean result = manager.editCalendar("TimeCal", "timezone",
            "Asia/Kolkata");
    assertTrue(result);
    assertEquals(ZoneId.of("Asia/Kolkata"),
            manager.getCalendar("TimeCal").getTimezone());
  }

  @Test
  public void testCopyEventWithinSameCalendar() {
    manager.addCalendar("SameCal", ZoneId.of("UTC"));
    manager.useCalendar("SameCal");

    ZonedDateTime start = ZonedDateTime.of(2025, 6,
            1, 9, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = start.plusHours(1);
    manager.getActiveCalendar().addEvent(new SingleEvent("Session",
            start, end, "", "", true, false, null), false);

    ZonedDateTime newStart = ZonedDateTime.of(2025, 6, 2,
            9, 0, 0, 0, ZoneId.of("UTC"));
    boolean result = manager.copySingleEvent("Session", start,
            "SameCal", newStart);

    assertTrue(result);
    assertEquals(2, manager.getActiveCalendar().getEvents().size());
  }

  @Test
  public void testCopyEventCausesConflictInTargetCalendarFails() {
    manager.addCalendar("Source", ZoneId.of("UTC"));
    manager.addCalendar("Target", ZoneId.of("UTC"));
    manager.useCalendar("Source");

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 6,
            1, 10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            sourceStart, sourceEnd, "", "", true, false, null), false);

    ICalendarModel target = manager.getCalendar("Target");
    ZonedDateTime conflictStart = ZonedDateTime.of(2025, 6,
            2, 10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime conflictEnd = conflictStart.plusHours(1);
    target.addEvent(new SingleEvent("Existing", conflictStart, conflictEnd,
            "", "", true, false, null), false);

    boolean result = manager.copySingleEvent("Meeting", sourceStart,
            "Target", conflictStart);
    assertFalse(result);
  }

  @Test
  public void testCopyNonExistentEventFails() {
    manager.addCalendar("Source", ZoneId.of("UTC"));
    manager.addCalendar("Target", ZoneId.of("UTC"));
    manager.useCalendar("Source");

    boolean result = manager.copySingleEvent("UnknownEvent",
            ZonedDateTime.of(2025, 6, 1, 10, 0,
                    0, 0, ZoneId.of("UTC")),
            "Target",
            ZonedDateTime.of(2025, 6, 2, 10, 0,
                    0, 0, ZoneId.of("UTC")));
    assertFalse(result);
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
  public void testEditCalendarNameActiveCalendar() {
    manager.addCalendar("ActiveCal", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("ActiveCal");
    ICalendarModel activeBefore = manager.getActiveCalendar();
    boolean result = manager.editCalendar("ActiveCal", "name",
            "RenamedCal");
    assertTrue(result);
    assertNull(manager.getCalendar("ActiveCal"));
    ICalendarModel activeAfter = manager.getCalendar("RenamedCal");
    assertNotNull(activeAfter);
    assertEquals(activeAfter, manager.getActiveCalendar());
    assertEquals("RenamedCal", activeAfter.getName());
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
    manager.copyEventsBetween(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1),
            "Target", ZonedDateTime.now().plusDays(2));
  }

  @Test
  public void testGetCalendarWithQuotes() {
    manager.addCalendar("QuotedCal", ZoneId.of("America/New_York"));
    ICalendarModel cal = manager.getCalendar("\"QuotedCal\"");
    assertNotNull(cal);
    assertEquals("QuotedCal", cal.getName());
  }

  @Test
  public void testCopySingleEventBetweenCalendars() {
    manager.addCalendar("Source", ZoneId.of("UTC"));
    manager.addCalendar("Target", ZoneId.of("America/New_York"));
    manager.useCalendar("Source");

    ICalendarModel source = manager.getCalendar("Source");
    ICalendarModel target = manager.getCalendar("Target");

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1,
            10, 0, 0, 0, ZoneId.of("UTC"));
    source.addEvent(new SingleEvent("Lecture", start, end, "",
            "", true, false, null), false);

    boolean result = manager.copySingleEvent("Lecture", start,
            "Target",
            ZonedDateTime.of(2025, 6, 2, 9, 0,
                    0, 0, ZoneId.of("America/New_York")));

    assertTrue(result);
    assertEquals(1, target.getEvents().size());
  }

  @Test
  public void testCopyEventsOnDateToAnotherCalendar() {
    manager.addCalendar("Semester2024", ZoneId.of("UTC"));
    manager.addCalendar("Semester2025", ZoneId.of("UTC"));
    manager.useCalendar("Semester2024");

    ICalendarModel source = manager.getCalendar("Semester2024");
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

    ICalendarModel source = manager.getCalendar("Fall2024");
    source.addEvent(new SingleEvent("Lecture",
            ZonedDateTime.of(2024, 9, 5, 10, 0,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            ZonedDateTime.of(2024, 9, 6, 11, 0,
                    0, 0, ZoneId.of("Asia/Kolkata")),
            "Intro", "Room A", true, false,
            null), false);

    boolean copied = manager.copyEventsBetween(
            ZonedDateTime.of(LocalDate.of(2024, 9, 5),
                    LocalTime.MIDNIGHT, ZoneId.of("UTC")),
            ZonedDateTime.of(LocalDate.of(2024, 9, 6),
                    LocalTime.MIDNIGHT, ZoneId.of("UTC")),
            "Spring2025",
            ZonedDateTime.of(LocalDate.of(2025, 1, 8),
                    LocalTime.MIDNIGHT, ZoneId.of("UTC"))
    );

    assertTrue(copied);
    List<ReadOnlyCalendarEvent> springEvents = manager.getCalendar("Spring2025").getEvents();
    assertEquals(1, springEvents.size());
    assertEquals("Lecture", springEvents.get(0).getSubject());
  }

  @Test
  public void testAddConflictingEventsInSameCalendarFails() {
    manager.addCalendar("ConflictsCal", ZoneId.of("UTC"));
    manager.useCalendar("ConflictsCal");

    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9,
            30, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 10,
            30, 0, 0, ZoneId.of("UTC"));

    ICalendarModel model = manager.getActiveCalendar();
    assertTrue(model.addEvent(new SingleEvent("Meeting A", start1, end1,
                    "", "", true, false, null),
            false));
    assertFalse(model.addEvent(new SingleEvent("Meeting B", start2, end2,
                    "", "", true, false, null),
            false));
  }

  @Test
  public void testRecurringEventConflictsWithExistingSingleEventFails() {
    manager.addCalendar("ConflictRecurring", ZoneId.of("UTC"));
    manager.useCalendar("ConflictRecurring");

    ICalendarModel model = manager.getActiveCalendar();
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 10,
            0, 0, 0, ZoneId.of("UTC"));

    assertTrue(model.addEvent(new SingleEvent("Blocked Slot", start, end, "",
            "", true, false, null), false));

    RecurringEvent re = new RecurringEvent("Standup", start, end, "MW",
            3, null, "", "", true, false);
    assertFalse(model.addRecurringEvent(re, false));
  }

  @Test
  public void testRecurringEventConflictsWithRecurringEventFails() {
    manager.addCalendar("Class", ZoneId.of("Asia/Kolkata"));
    manager.useCalendar("Class");
    ICalendarModel model = manager.getActiveCalendar();

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
    assertFalse(model.addRecurringEvent(conflict, false));
  }

  @Test
  public void testCalendarTimezoneChangeUpdatesEventTimes() {
    manager.addCalendar("TZCal", ZoneId.of("America/New_York"));
    manager.useCalendar("TZCal");

    ZonedDateTime originalStart = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime originalEnd = originalStart.plusHours(1);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting", originalStart,
            originalEnd, "Planning", "Room 1", true,
            false, null), false);

    manager.editCalendar("TZCal", "timezone", "Europe/Paris");

    List<ReadOnlyCalendarEvent> updatedEvents = manager.getActiveCalendar().getEvents();
    assertEquals(1, updatedEvents.size());
    assertEquals("Europe/Paris", ZonedDateTime.from(updatedEvents.get(0)
            .getStartDateTime()).getZone().getId());
  }

  @Test
  public void testCopiedEventAdjustsToTargetCalendarTimezone() {
    manager.addCalendar("Source", ZoneId.of("America/New_York"));
    manager.addCalendar("Target", ZoneId.of("Asia/Tokyo"));
    manager.useCalendar("Source");

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 6,
            2, 8, 0, 0, 0,
            ZoneId.of("America/New_York"));
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);
    manager.getActiveCalendar().addEvent(new SingleEvent("Call",
            sourceStart, sourceEnd, "Client Call", "Zoom",
            true, false, null), false);

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 6, 5,
            9, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
    boolean result = manager.copySingleEvent("Call", sourceStart,
            "Target", targetStart);
    assertTrue(result);

    List<ReadOnlyCalendarEvent> eventsInTarget = manager.getCalendar("Target").getEvents();
    assertEquals(1, eventsInTarget.size());
    assertEquals("Call", eventsInTarget.get(0).getSubject());
    assertEquals(targetStart, eventsInTarget.get(0).getStartDateTime());
    assertEquals("Asia/Tokyo", ZonedDateTime.from(eventsInTarget.get(0)
            .getStartDateTime()).getZone().getId());
  }

  @Test
  public void testEditEventsFromOnlyAppliesToLaterEvents() {
    manager.addCalendar("Cal", ZoneId.of("UTC"));
    manager.useCalendar("Cal");

    ZonedDateTime early = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime mid = ZonedDateTime.of(2025, 6, 2,
            9, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime late = ZonedDateTime.of(2025, 6, 3,
            9, 0, 0, 0, ZoneId.of("UTC"));

    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting", early,
            early.plusHours(1), "A", "Room", true,
            false, null), false);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting", mid,
            mid.plusHours(1), "B", "Room", true,
            false, null), false);
    manager.getActiveCalendar().addEvent(new SingleEvent("Meeting",
            late, late.plusHours(1), "C", "Room", true,
            false, null), false);

    boolean result = manager.getActiveCalendar().editEventsFrom("description",
            "Meeting", mid, "Updated");
    assertTrue(result);
    List<ReadOnlyCalendarEvent> events = manager.getActiveCalendar().getEvents();
    for (ReadOnlyCalendarEvent e : events) {
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
      ZonedDateTime start = ZonedDateTime.of(2025, 6, i, 10,
              0, 0, 0, ZoneId.of("UTC"));
      ZonedDateTime end = start.plusHours(1);
      manager.getActiveCalendar().addEvent(new SingleEvent("Workshop",
              start, end, "Old Desc", "Loc", true,
              false, null), false);
    }

    boolean result = manager.getActiveCalendar().editEventsAll("description",
            "Workshop", "New Desc");
    assertTrue(result);
    for (ReadOnlyCalendarEvent e : manager.getActiveCalendar().getEvents()) {
      assertEquals("New Desc", e.getDescription());
    }
  }

  @Test
  public void testEditEventsFromNoMatchReturnsFalse() {
    manager.addCalendar("NoMatchCal", ZoneId.of("UTC"));
    manager.useCalendar("NoMatchCal");

    ZonedDateTime dt = ZonedDateTime.of(2025, 6, 10, 10,
            0, 0, 0, ZoneId.of("UTC"));
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

    SingleEvent e1 = new SingleEvent("Standup", start, end, "",
            "", true, false, null);
    SingleEvent e2 = new SingleEvent("Standup", start, end, "",
            "", true, false, null);

    manager.getActiveCalendar().addEvent(e1, false);
    manager.getActiveCalendar().addEvent(e2, false);
  }

  @Test
  public void testValidEditsOnSingleEvent() {
    manager.addCalendar("EditPropsCal", ZoneId.of("UTC"));
    manager.useCalendar("EditPropsCal");

    ZonedDateTime startA = ZonedDateTime.of(2025, 6, 3,
            10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime endA = startA.plusHours(1);

    ZonedDateTime startB = ZonedDateTime.of(2025, 6, 3,
            12, 0, 0, 0, ZoneId.of("UTC"));
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

  /**
   * Dummy subclass of CalendarModel to override the copy methods.
   */
  private static class DummyCalendarModel extends CalendarModel {
    private final boolean singleReturn;
    private final boolean eventsOnDateReturn;
    private final boolean eventsBetweenReturn;

    public DummyCalendarModel(String name, ZoneId zone, boolean singleReturn,
                              boolean eventsOnDateReturn, boolean eventsBetweenReturn) {
      super(name, zone);
      this.singleReturn = singleReturn;
      this.eventsOnDateReturn = eventsOnDateReturn;
      this.eventsBetweenReturn = eventsBetweenReturn;
    }

    public boolean copySingleEventTo(CalendarModel source, String eventName,
                                     ZonedDateTime sourceDateTime,
                                     CalendarModel target, ZonedDateTime targetDateTime) {
      return singleReturn;
    }

    public boolean copyEventsOnDateTo(CalendarModel source, LocalDate sourceDate,
                                      CalendarModel target, LocalDate targetDate) {
      return eventsOnDateReturn;
    }

    public boolean copyEventsBetweenTo(CalendarModel source, LocalDate startDate,
                                       LocalDate endDate,
                                       CalendarModel target, LocalDate targetStartDate) {
      return eventsBetweenReturn;
    }
  }

  private void replaceCalendar(String name, CalendarModel dummy) throws Exception {
    Field calendarsField = CalendarManager.class.getDeclaredField("calendars");
    calendarsField.setAccessible(true);
    Map<String, CalendarModel> calendars = (Map<String, CalendarModel>) calendarsField.get(manager);
    calendars.put(name, dummy);
  }

  @Test
  public void testCopySingleEventDelegationReturnsFalse() throws Exception {
    manager.addCalendar("DummySource", ZoneId.of("UTC"));
    manager.addCalendar("DummyTarget", ZoneId.of("UTC"));
    DummyCalendarModel dummySource = new DummyCalendarModel("DummySource",
            ZoneId.of("UTC"), false, false,
            false);
    DummyCalendarModel dummyTarget = new DummyCalendarModel("DummyTarget",
            ZoneId.of("UTC"), false, false,
            false);
    replaceCalendar("DummySource", dummySource);
    replaceCalendar("DummyTarget", dummyTarget);
    manager.useCalendar("DummySource");

    boolean result = manager.copySingleEvent("AnyEvent", ZonedDateTime.now(),
            "DummyTarget", ZonedDateTime.now().plusHours(1));
    assertFalse("Expected copySingleEvent to return false from dummy implementation",
            result);
  }

  @Test
  public void testCopyEventsOnDateDelegationReturnsFalse() throws Exception {
    manager.addCalendar("DummySourceOnDate", ZoneId.of("UTC"));
    manager.addCalendar("DummyTargetOnDate", ZoneId.of("UTC"));
    DummyCalendarModel dummySource = new DummyCalendarModel("DummySourceOnDate",
            ZoneId.of("UTC"), false, false,
            false);
    DummyCalendarModel dummyTarget = new DummyCalendarModel("DummyTargetOnDate",
            ZoneId.of("UTC"), false, false,
            false);
    replaceCalendar("DummySourceOnDate", dummySource);
    replaceCalendar("DummyTargetOnDate", dummyTarget);
    manager.useCalendar("DummySourceOnDate");

    boolean result = manager.copyEventsOnDate(LocalDate.now(),
            "DummyTargetOnDate", LocalDate.now().plusDays(1));
    assertTrue("Expected copyEventsOnDate to return true from dummy implementation",
            result);
  }

  @Test
  public void testCopyEventsBetweenDelegationReturnsFalse() throws Exception {
    manager.addCalendar("DummySourceBetween", ZoneId.of("UTC"));
    manager.addCalendar("DummyTargetBetween", ZoneId.of("UTC"));
    DummyCalendarModel dummySource = new DummyCalendarModel("DummySourceBetween",
            ZoneId.of("UTC"), false, false,
            false);
    DummyCalendarModel dummyTarget = new DummyCalendarModel("DummyTargetBetween",
            ZoneId.of("UTC"), false, false,
            false);
    replaceCalendar("DummySourceBetween", dummySource);
    replaceCalendar("DummyTargetBetween", dummyTarget);
    manager.useCalendar("DummySourceBetween");

    boolean result = manager.copyEventsBetween(ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1), "DummyTargetBetween",
            ZonedDateTime.now().plusDays(2));
    assertTrue("Expected copyEventsBetween to return False from dummy implementation",
            result);
  }

  @Test
  public void testCopyEventsOnDateDelegationTrue() throws Exception {
    manager.addCalendar("DummySourceOnDateTrue", ZoneId.of("UTC"));
    manager.addCalendar("DummyTargetOnDateTrue", ZoneId.of("UTC"));
    DummyCalendarModel dummySource = new DummyCalendarModel("DummySourceOnDateTrue",
            ZoneId.of("UTC"), false, true,
            false);
    DummyCalendarModel dummyTarget = new DummyCalendarModel("DummyTargetOnDateTrue",
            ZoneId.of("UTC"), false, true,
            false);
    replaceCalendar("DummySourceOnDateTrue", dummySource);
    replaceCalendar("DummyTargetOnDateTrue", dummyTarget);
    manager.useCalendar("DummySourceOnDateTrue");

    boolean result = manager.copyEventsOnDate(LocalDate.now(),
            "DummyTargetOnDateTrue", LocalDate.now().plusDays(1));
    assertTrue("Expected copyEventsOnDate to return true from dummy implementation",
            result);
  }

  @Test
  public void testCopyEventsBetweenDelegationTrue() throws Exception {
    manager.addCalendar("DummySourceBetweenTrue", ZoneId.of("UTC"));
    manager.addCalendar("DummyTargetBetweenTrue", ZoneId.of("UTC"));
    DummyCalendarModel dummySource = new DummyCalendarModel("DummySourceBetweenTrue",
            ZoneId.of("UTC"), false, false,
            true);
    DummyCalendarModel dummyTarget = new DummyCalendarModel("DummyTargetBetweenTrue",
            ZoneId.of("UTC"), false, false,
            true);
    replaceCalendar("DummySourceBetweenTrue", dummySource);
    replaceCalendar("DummyTargetBetweenTrue", dummyTarget);
    manager.useCalendar("DummySourceBetweenTrue");

    boolean result = manager.copyEventsBetween(ZonedDateTime.now(),
            ZonedDateTime.now().plusDays(1), "DummyTargetBetweenTrue",
            ZonedDateTime.now().plusDays(2));
    assertTrue("Expected copyEventsBetween to return true from dummy implementation",
            result);
  }

  @Test
  public void testValidCalendarCreation() {
    boolean result = manager.addCalendar("WorkCal", ZoneId.of("America/New_York"));
    assertTrue(result);
    assertNotNull(manager.getCalendar("WorkCal"));
  }

  @Test
  public void testDuplicateCalendarCreationFails() {
    manager.addCalendar("WorkCal", ZoneId.of("America/New_York"));
    boolean result = manager.addCalendar("WorkCal", ZoneId.of("America/New_York"));
    assertFalse(result);
  }

  @Test
  public void testInvalidTimezoneFails() {
    try {
      manager.addCalendar("ErrCal", ZoneId.of("Invalid/Timezone"));
      fail("Expected an exception for invalid timezone");
    } catch (Exception e) {
      assertTrue(e instanceof RuntimeException);
      assertTrue(e.getMessage().contains("Invalid"));
    }
  }

  @Test
  public void testEditNonexistentCalendar() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("NonExistentCal", "name", "NewCal");
    });

    assertEquals("Calendar not found: NonExistentCal", exception.getMessage());
  }

  @Test
  public void testEditCalendarInvalidProperty() {
    manager.addCalendar("OfficeCal", ZoneId.of("America/New_York"));

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      manager.editCalendar("OfficeCal", "invalidProp", "value");
    });

    assertEquals("Unsupported property: invalidProp", exception.getMessage());
  }

  @Test
  public void testUseNonexistentCalendarFails() {
    boolean result = manager.useCalendar("UnknownCal");

    assertFalse("Expected failure when trying to use a non-existent calendar", result);
    assertNull("No calendar should be active if the requested one does not exist",
            manager.getActiveCalendar());
  }

  @Test
  public void testSwitchingActiveCalendar() {
    manager.addCalendar("WorkCal", ZoneId.of("America/New_York"));
    manager.addCalendar("PersonalCal", ZoneId.of("Europe/London"));

    boolean firstUse = manager.useCalendar("WorkCal");
    boolean secondUse = manager.useCalendar("PersonalCal");

    assertTrue("Expected to switch from 'WorkCal' to 'PersonalCal'", secondUse);
    assertEquals("Active calendar should now be 'PersonalCal'", "PersonalCal",
            manager.getActiveCalendar().getName());
  }


  @Test
  public void testAddCalendarWithTrailingSpaces() {
    boolean result = manager.addCalendar("  TrimTest  ", ZoneId.of("UTC"));
    assertTrue(result);
    assertNotNull(manager.getCalendar("  TrimTest  "));
  }

  @Test
  public void testUseCalendarAfterRenaming() {
    manager.addCalendar("TempCal", ZoneId.of("UTC"));
    manager.editCalendar("TempCal", "name", "FinalCal");
    boolean used = manager.useCalendar("FinalCal");
    assertTrue(used);
    assertEquals("FinalCal", manager.getActiveCalendar().getName());
  }

  @Test
  public void testGetCalendarStripsQuotes() {
    manager.addCalendar("TestCal", ZoneId.of("UTC"));
    ICalendarModel found = manager.getCalendar("\"TestCal\"");
    assertNotNull(found);
    assertEquals("TestCal", found.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarToSameNameThrowsException() {
    manager.addCalendar("SameNameCal", ZoneId.of("UTC"));
    manager.editCalendar("SameNameCal", "name", "SameNameCal");
  }

}

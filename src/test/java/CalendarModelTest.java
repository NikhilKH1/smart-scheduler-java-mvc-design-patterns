import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.model.event.ICalendarEvent;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.zone.ZoneRulesException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CalendarModelTest {

  private CalendarModel model;


  @Before
  public void setUp() {
    model = new CalendarModel("TestCal", ZoneId.of("Asia/Kolkata"));
  }

  @Test
  public void testAddSingleEventSuccess() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);

    assertTrue(model.addEvent(event, true));
    assertEquals(1, model.getEvents().size());
  }

  @Test
  public void testAddConflictEventFails() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 3, 25,
            10, 0, 0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    SingleEvent event1 = new SingleEvent("Meeting1", start1, end1, "",
            "", true, false, null);

    ZonedDateTime start2 = ZonedDateTime.of(2025, 3, 25, 10,
            30, 0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);
    SingleEvent event2 = new SingleEvent("Meeting2", start2, end2, "",
            "", true, false, null);

    assertTrue(model.addEvent(event1, true));
    assertFalse(model.addEvent(event2, true));
  }

  @Test
  public void testEditEventsAllNonConflict() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    model.addEvent(new SingleEvent("Lecture", start, end, "", "",
            true, false, null), true);
    model.addEvent(new SingleEvent("Lecture", start.plusDays(1), end.plusDays(1),
            "", "", true, false, null), true);

    assertTrue(model.editEventsAll("description", "Lecture",
            "UpdatedDesc"));
    for (ICalendarEvent e : model.getEvents()) {
      assertEquals("UpdatedDesc", e.getDescription());
    }
  }

  @Test
  public void testIsBusyAt() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(2);
    model.addEvent(new SingleEvent("Blocker", start, end, "", "",
            true, false, null), true);

    ZonedDateTime check = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    assertTrue(model.isBusyAt(check));

    ZonedDateTime free = ZonedDateTime.of(2025, 3, 25, 12,
            0, 0, 0, model.getTimezone());
    assertFalse(model.isBusyAt(free));
  }

  @Test
  public void testUpdateTimezoneAdjustsEventTimes() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    model.addEvent(new SingleEvent("ConvertTZ", start, end, "", "",
            true, false, null), true);

    model.updateTimezone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), model.getTimezone());

    for (ICalendarEvent e : model.getEvents()) {
      assertEquals(ZoneId.of("America/New_York"),
              ZonedDateTime.from(e.getStartDateTime()).getZone());
    }
  }

  @Test
  public void testTimezoneUpdate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("TimeZoneTest", start, end, "",
            "", true, false, null);
    model.addEvent(event, true);

    model.updateTimezone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), model.getTimezone());

    ZonedDateTime newStart = ZonedDateTime.from(model.getEvents().get(0).getStartDateTime());
    assertEquals(ZoneId.of("America/New_York"), newStart.getZone());
  }

  @Test
  public void testCalendarInitialization() {
    assertEquals("TestCal", model.getName());
    assertEquals(ZoneId.of("Asia/Kolkata"), model.getTimezone());
    assertTrue(model.getEvents().isEmpty());
  }

  @Test
  public void testAddSingleEventSuccessfully() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Meeting", start, end, "Team meeting",
            "Office", true, false, null);
    assertTrue(model.addEvent(event, true));
    assertEquals(1, model.getEvents().size());
  }

  @Test
  public void testAddEventConflictFails() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event1 = new SingleEvent("Meeting1", start, end, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Meeting2", start.plusMinutes(30),
            end.plusHours(1), "", "", true, false, null);

    assertTrue(model.addEvent(event1, true));
    assertFalse("Second event should fail due to conflict",
            model.addEvent(event2, true));
    assertEquals(1, model.getEvents().size());
  }

  @Test
  public void testEditEventNoConflict() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Lecture", start, end, "",
            "", true, false, null);

    model.addEvent(event, true);
    assertTrue(model.editEventsAll("description", "Lecture",
            "Updated Description"));

    for (ICalendarEvent e : model.getEvents()) {
      assertEquals("Updated Description", e.getDescription());
    }
  }

  @Test
  public void testEditEventCreatingConflictFails() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime start2 = ZonedDateTime.of(2025, 3, 25, 12,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);

    model.addEvent(new SingleEvent("Meeting1", start1, end1, "", "",
            true, false, null), true);
    model.addEvent(new SingleEvent("Meeting2", start2, end2, "", "",
            true, false, null), true);
    assertFalse("Editing should fail due to conflict",
            model.editEventsFrom("startdatetime", "Meeting2", start2,
                    "2025-03-25T10:30+05:30[Asia/Kolkata]"));
  }

  @Test
  public void testIsBusyAtCheck() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(2);
    model.addEvent(new SingleEvent("Block", start, end, "", "",
            true, false, null), true);

    ZonedDateTime busyCheck = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime freeCheck = ZonedDateTime.of(2025, 3, 25, 12,
            0, 0, 0, model.getTimezone());

    assertTrue(model.isBusyAt(busyCheck));
    assertFalse(model.isBusyAt(freeCheck));
  }

  @Test
  public void testGenerateOccurrencesWithRepeatCount() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Class", start, end, "MTW",
            3, null, "Desc", "Loc", true,
            false);

    List<SingleEvent> occurrences = recurring.generateOccurrences("series-1");
    assertEquals(3, occurrences.size());

    for (SingleEvent e : occurrences) {
      assertEquals("Class", e.getSubject());
      assertEquals("series-1", e.getSeriesId());
      assertEquals("Desc", e.getDescription());
    }
  }

  @Test
  public void testGenerateOccurrencesWithRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    ZonedDateTime repeatUntil = start.plusDays(5);

    RecurringEvent recurring = new RecurringEvent("Webinar", start, end, "MWF",
            0, repeatUntil, "Topic", "Online", true,
            false);
    List<SingleEvent> occurrences = recurring.generateOccurrences("series-2");

    assertFalse(occurrences.isEmpty());
    for (SingleEvent e : occurrences) {
      ZonedDateTime occurrenceDate = ZonedDateTime.from(e.getStartDateTime());
      assertFalse(occurrenceDate.isAfter(repeatUntil));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUpdatedPropertyInvalidRepeatCount() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Session", start, end, "T",
            5, null, "", "", true, false);

    recurring.withUpdatedProperty("repeattimes", "-1");
  }

  @Test(expected = DateTimeParseException.class)
  public void testWithUpdatedPropertyInvalidRepeatUntilFormat() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Session", start, end, "T",
            5, null, "", "", true, false);

    recurring.withUpdatedProperty("repeatuntil", "not-a-date");
  }

  @Test
  public void testWithUpdatedPropertyChangesCorrectly() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent original = new RecurringEvent("Session", start, end, "M",
            5, null, "Desc", "Room", true,
            false);

    RecurringEvent updated = original.withUpdatedProperty("description",
            "Updated Desc");
    assertEquals("Updated Desc", updated.getDescription());
    assertEquals("Room", updated.getLocation());

    RecurringEvent updatedRepeat = updated.withUpdatedProperty("repeatingdays",
            "MW");
    assertEquals("MW", updatedRepeat.getWeekdays());
  }

  @Test
  public void testConflictWithSingleEventFails() {
    ZonedDateTime s1 = ZonedDateTime.of(2025, 3, 25, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime e1 = s1.plusHours(2);
    model.addEvent(new SingleEvent("One", s1, e1, "", "",
            true, false, null), true);

    ZonedDateTime s2 = ZonedDateTime.of(2025, 3, 25, 11,
            0, 0, 0, model.getTimezone());
    ZonedDateTime e2 = s2.plusHours(1);
    assertFalse(model.addEvent(new SingleEvent("Two", s2, e2, "",
            "", true, false, null), true));
  }


  @Test
  public void testCopySingleEventToNewDateSameCalendar() {
    ZonedDateTime original = ZonedDateTime.of(2025, 3, 25, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = original.plusHours(1);
    model.addEvent(new SingleEvent("CopyMe", original, end, "", "",
            true, false, null), true);

    ZonedDateTime target = ZonedDateTime.of(2025, 3, 30, 9,
            0, 0, 0, model.getTimezone());
    assertTrue(model.copySingleEventTo(model, "CopyMe", original, model, target));
    assertEquals(2, model.getEvents().size());
  }

  @Test
  public void testIsBusyAtReturnsTrueAndFalse() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(2);
    model.addEvent(new SingleEvent("Busy", start, end, "", "",
            true, false, null), true);

    ZonedDateTime check1 = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime check2 = ZonedDateTime.of(2025, 3, 10, 12,
            0, 0, 0, model.getTimezone());

    assertTrue(model.isBusyAt(check1));
    assertFalse(model.isBusyAt(check2));
  }

  @Test
  public void testUpdateTimezoneShiftsEventTimesCorrectly() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, model.getTimezone());
    model.addEvent(new SingleEvent("TimeTest", start, start.plusHours(1),
            "", "", true, false, null), true);

    model.updateTimezone(ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), model.getTimezone());

    for (ICalendarEvent event : model.getEvents()) {
      ZonedDateTime eventStart = ZonedDateTime.from(event.getStartDateTime());
      assertEquals("America/New_York", eventStart.getZone().toString());
    }
  }

  @Test
  public void testCopyEventsBetweenDatesSuccessfully() {
    ZonedDateTime base = ZonedDateTime.of(2025, 3, 1, 10,
            0, 0, 0, model.getTimezone());
    model.addEvent(new SingleEvent("E1", base, base.plusHours(1), "",
            "", true, false, null), true);
    model.addEvent(new SingleEvent("E2", base.plusDays(1), base.plusDays(1).plusHours(1),
            "", "", true, false, null), true);

    CalendarModel target = new CalendarModel("TargetCal",
            ZoneId.of("America/New_York"));
    assertTrue(model.copyEventsBetweenTo(model, LocalDate.of(2025, 3,
                    1), LocalDate.of(2025, 3, 2), target,
            LocalDate.of(2025, 3, 5)));
    assertEquals(2, target.getEvents().size());
  }


  @Test
  public void testEditEventsFromSpecificDateOnlyAffectsLaterEvents() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Class", start, end, "MTW",
            3, null, "Initial Desc", "", true,
            false);
    model.addRecurringEvent(recurring, true);

    ZonedDateTime editFrom = ZonedDateTime.of(2025, 3, 11, 10,
            0, 0, 0, model.getTimezone());
    assertTrue(model.editEventsFrom("description", "Class", editFrom,
            "Updated Desc"));

    for (ICalendarEvent e : model.getEvents()) {
      ZonedDateTime eventStart = ZonedDateTime.from(e.getStartDateTime());
      if (!eventStart.isBefore(editFrom)) {
        assertEquals("Updated Desc", e.getDescription());
      } else {
        assertEquals("Initial Desc", e.getDescription());
      }
    }
  }

  @Test
  public void testAddRecurringEventConflictFails() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("DBMS Class", start, end, "MTW",
            3, null, "", "", true, false);
    assertTrue(model.addRecurringEvent(recurring, true));

    ZonedDateTime conflictStart = ZonedDateTime.of(2025, 3, 10, 10,
            30, 0, 0, model.getTimezone());
    ZonedDateTime conflictEnd = conflictStart.plusHours(1);
    SingleEvent conflict = new SingleEvent("Conflict", conflictStart, conflictEnd,
            "", "", true, false, null);

    assertFalse(model.addEvent(conflict, true));
  }

  @Test
  public void testEditRecurringEventRepeatingDaysSuccess() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Lecture", start, end, "MTW",
            3, null, "", "", true, false);
    assertTrue(model.addRecurringEvent(recurring, true));
    assertTrue(model.editRecurringEvent("Lecture", "repeattimes",
            "5"));
    assertEquals(5, model.getEvents().size());
  }

  @Test
  public void testEditRecurringEventConflictFails() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Class", start, end, "MTW",
            3, null, "", "", true, false);
    assertTrue(model.addRecurringEvent(recurring, true));

    ZonedDateTime longMeetingStart = ZonedDateTime.of(2025, 3, 11, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime longMeetingEnd = longMeetingStart.plusHours(2);
    SingleEvent overlap = new SingleEvent("Overlap", longMeetingStart, longMeetingEnd,
            "", "", true, false, null);
    assertFalse(model.addEvent(overlap, true));

    assertFalse(model.editEventsAll("startdatetime", "Class",
            "2025-03-11T09:30+05:30[Asia/Kolkata]"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventDuplicate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event1 = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(event1, false);

    SingleEvent event2 = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(event2, false);
  }

  @Test
  public void testAddEventNoConflict() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    boolean added = model.addEvent(event, false);
    assertTrue(added);
  }

  @Test
  public void testAddEventConflictAutoDecline() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);

    SingleEvent event1 = new SingleEvent("Meeting1", start, end, "Desc",
            "Loc", true, false, null);
    assertTrue(model.addEvent(event1, false));

    ZonedDateTime conflictStart = start.plusMinutes(30);
    ZonedDateTime conflictEnd = end.plusMinutes(30);
    SingleEvent event2 = new SingleEvent("Meeting2", conflictStart, conflictEnd,
            "Desc", "Loc", true, false, null);

    boolean added = model.addEvent(event2, true);
    assertFalse(added);
  }

  @Test
  public void testAddRecurringEventNoConflict() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Recurring", start, end, "M",
            2, null, "Desc", "Loc", true,
            false);

    boolean added = model.addRecurringEvent(recurring, false);
    assertTrue(added);

    List<ICalendarEvent> events = model.getEvents();
    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventDuplicateOccurrence() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("RecurringDup", start, end, "M",
            2, null, "Desc", "Loc", true,
            false);

    assertTrue(model.addRecurringEvent(recurring, false));
    model.addRecurringEvent(recurring, false);
  }

  @Test
  public void testAddRecurringEventConflictAutoDecline() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent conflict = new SingleEvent("Conflict", start, end, "Desc",
            "Loc", true, false, null);
    assertTrue(model.addEvent(conflict, false));

    RecurringEvent recurring = new RecurringEvent("RecurringConflict", start, end,
            "T", 2, null, "Desc", "Loc",
            true, false);
    boolean added = model.addRecurringEvent(recurring, true);

    assertFalse(added);
  }

  @Test
  public void testGetEventsOnDate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    assertTrue(model.addEvent(event, false));

    List<ICalendarEvent> events = model.getEventsOnDate(LocalDate.of(2025, 6,
            1));
    assertEquals(1, events.size());
  }

  @Test
  public void testGetEventsBetween() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Workshop", start, end, "", "",
            true, false, null);
    assertTrue(model.addEvent(event, false));

    ZonedDateTime from = ZonedDateTime.of(2025, 6, 1, 8, 0,
            0, 0, model.getTimezone());
    ZonedDateTime to = ZonedDateTime.of(2025, 6, 1, 11, 0,
            0, 0, model.getTimezone());

    List<ICalendarEvent> events = model.getEventsBetween(from, to);
    assertEquals(1, events.size());
  }

  @Test
  public void testIsBusyAt2() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Interview", start, end, "", "",
            true, false, null);
    model.addEvent(event, false);

    ZonedDateTime checkBusy = ZonedDateTime.of(2025, 6, 1, 9,
            30, 0, 0, model.getTimezone());
    ZonedDateTime checkFree = ZonedDateTime.of(2025, 6, 1, 8,
            30, 0, 0, model.getTimezone());

    assertTrue(model.isBusyAt(checkBusy));
    assertFalse(model.isBusyAt(checkFree));
  }

  @Test
  public void testEditEventNoConflictNew() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 7, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    SingleEvent oldEvent = new SingleEvent("EditTest", start1, end1, "Desc",
            "Loc", true, false, null);
    model.addEvent(oldEvent, false);

    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 7, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);
    SingleEvent newEvent = new SingleEvent("EditTest", start2, end2, "NewDesc",
            "NewLoc", true, false, null);

    boolean edited = model.editEvent(oldEvent, newEvent);
    assertTrue(edited);

    List<ICalendarEvent> events = model.getEvents();
    assertTrue(events.contains(newEvent));
    assertFalse(events.contains(oldEvent));
  }

  @Test
  public void testEditEventConflictDirect() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 20, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    SingleEvent event1 = new SingleEvent("EditTest", start1, end1, "Desc",
            "Loc", true, false, null);
    model.addEvent(event1, false);

    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 20, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);
    SingleEvent event2 = new SingleEvent("Other", start2, end2, "Desc",
            "Loc", true, false, null);
    model.addEvent(event2, false);

    SingleEvent newEvent = new SingleEvent("EditTest", start2, end2, "NewDesc",
            "NewLoc", true, false, null);
    boolean edited = model.editEvent(event1, newEvent);
    assertFalse(edited);

    List<ICalendarEvent> events = model.getEvents();
    assertTrue(events.contains(event1));
  }

  @Test
  public void testEditSingleEventNotFound() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 9, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    boolean edited = model.editSingleEvent("description", "Nonexistent",
            start, end, "New");
    assertFalse(edited);
  }

  @Test
  public void testEditEventsFromNotFound() {
    ZonedDateTime from = ZonedDateTime.of(2025, 6, 10, 9, 0,
            0, 0, model.getTimezone());
    boolean edited = model.editEventsFrom("description", "Nonexistent", from,
            "New");
    assertFalse(edited);
  }

  @Test
  public void testEditEventsAllNotFound() {
    boolean edited = model.editEventsAll("description", "Nonexistent",
            "New");
    assertFalse(edited);
  }

  @Test
  public void testEditSingleEventUpdate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 11, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("EditSingle", start, end, "Old",
            "Loc", true, false, null);
    assertTrue(model.addEvent(event, false));

    boolean edited = model.editSingleEvent("description", "EditSingle",
            start, end, "New");
    assertTrue(edited);

    for (ICalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditSingle")) {
        assertEquals("New", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsFromUpdate() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 13, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("EditFrom", start, end, "Old",
            "Loc", true, false, null);
    assertTrue(model.addEvent(event, false));

    boolean edited = model.editEventsFrom("description", "EditFrom", start,
            "New");
    assertTrue(edited);

    for (ICalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditFrom")) {
        assertEquals("New", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsAllUpdate() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 14, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 14, 11, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);

    SingleEvent event1 = new SingleEvent("EditAll", start1, end1, "Old1",
            "Loc", true, false, null);
    SingleEvent event2 = new SingleEvent("EditAll", start2, end2, "Old2",
            "Loc", true, false, null);

    assertTrue(model.addEvent(event1, false));
    assertTrue(model.addEvent(event2, false));

    boolean edited = model.editEventsAll("description", "EditAll",
            "Updated");
    assertTrue(edited);

    for (ICalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditAll")) {
        assertEquals("Updated", e.getDescription());
      }
    }
  }

  @Test
  public void testEditRecurringEventValid() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 15, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("RecurringEdit", start, end,
            "F", 2, null, "Desc", "Loc",
            true, false);

    assertTrue(model.addRecurringEvent(recurring, false));
    assertTrue(model.editRecurringEvent("RecurringEdit", "description",
            "Updated"));

    for (ICalendarEvent ev : model.getEvents()) {
      if (ev instanceof SingleEvent && ev.getSubject().equals("RecurringEdit")) {
        assertEquals("Updated", ev.getDescription());
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventInvalidProperty() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 7,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent event = new RecurringEvent("Yoga", start, end, "MWF",
            5, null, "Session", "Studio", true,
            false);
    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "repeattimes", "0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventUnsupportedProperty() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 8, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent event = new RecurringEvent("Yoga", start, end, "MTW",
            5, null, "", "", true, false);
    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "invalidproperty", "value");
  }

  @Test
  public void testEditRecurringEventDescriptionSuccessfully() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 7,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "Yoga", start, end,
            "MWF", 5, null,
            "Morning session", "Gym", true, false);

    assertTrue(model.addRecurringEvent(event, false));

    boolean edited = model.editRecurringEvent("Yoga", "description",
            "Evening session");
    assertTrue(edited);

    List<ICalendarEvent> events = model.getEvents();
    for (ICalendarEvent e : events) {
      if (e.getSubject().equals("Yoga")) {
        assertEquals("Evening session", e.getDescription());
      }
    }
  }

  @Test
  public void testEditRecurringEventRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 7, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 6, 30, 7,
            0, 0, 0, model.getTimezone());

    RecurringEvent event = new RecurringEvent(
            "Pilates", start, end,
            "TR", 0, repeatUntil,
            "Session", "Studio", true, false);

    assertTrue(model.addRecurringEvent(event, false));

    boolean edited = model.editRecurringEvent("Pilates", "repeatuntil",
            "2025-07-01T07:00+05:30[Asia/Kolkata]");
    assertTrue(edited);
  }

  @Test
  public void testEditRecurringEventRepeatingDays() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 18, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent event = new RecurringEvent(
            "Dance Class", start, end,
            "MT", 4, null,
            "Evening class", "Dance Studio", true, false);

    assertTrue(model.addRecurringEvent(event, false));

    boolean edited = model.editRecurringEvent("Dance Class", "repeatingdays",
            "WRF");
    assertTrue(edited);
  }

  @Test
  public void testEditRecurringEventLocation() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 15, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent event = new RecurringEvent("Training", start, end,
            "WRF", 3, null, "Sessions",
            "Room 101", true, false);
    model.addRecurringEvent(event, false);

    boolean edited = model.editRecurringEvent("Training", "location",
            "Room 202");
    assertTrue(edited);
    for (ICalendarEvent ev : model.getEvents()) {
      assertEquals("Room 202", ev.getLocation());
    }
  }

  @Test
  public void testEditRecurringEventAndCheckOccurrences() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent event = new RecurringEvent("Sprint Planning", start, end,
            "MTWRF", 5, null, "Daily Planning",
            "Office", true, false);
    model.addRecurringEvent(event, false);

    model.editRecurringEvent("Sprint Planning", "description",
            "Updated Daily Planning");
    List<ICalendarEvent> events = model.getEvents();
    assertEquals(5, events.size());
    assertEquals("Updated Daily Planning", events.get(0).getDescription());
  }


  @Test
  public void testAddSingleEventSuccessfullyNew() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    assertTrue(model.addEvent(event, true));
    assertEquals(1, model.getEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddDuplicateSingleEventFails() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event1 = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    SingleEvent event2 = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    model.addEvent(event1, false);
    model.addEvent(event2, false);
  }

  @Test
  public void testEditEventDescriptionSuccess() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent oldEvent = new SingleEvent("Team Meeting", start, end, "",
            "", true, false, null);
    model.addEvent(oldEvent, true);

    SingleEvent updated = oldEvent.withUpdatedProperty("description",
            "Updated Team Meeting");
    boolean edited = model.editEvent(oldEvent, updated);

    assertTrue(edited);
    assertTrue(model.getEvents().contains(updated));
    assertFalse(model.getEvents().contains(oldEvent));
    assertEquals("Updated Team Meeting", updated.getDescription());
  }

  @Test
  public void testEditEventConflictWithAutoDeclineFails() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 2, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    model.addEvent(new SingleEvent("Project Meeting", start1, end1, "",
            "", true, false, null), true);

    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 2, 11, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);
    model.addEvent(new SingleEvent("Client Meeting", start2, end2, "",
            "", true, false, null), true);

    SingleEvent original = new SingleEvent("Project Meeting", start1, end1,
            "", "", true, false, null);
    SingleEvent updated = new SingleEvent("Project Meeting", ZonedDateTime.of(2025,
            6, 2, 10, 30, 0, 0,
            model.getTimezone()),
            ZonedDateTime.of(2025, 6, 2, 11, 30, 0,
                    0, model.getTimezone()),
            "", "", true, false, null);

    boolean result = model.editEvent(original, updated);
    assertFalse(result);
  }

  @Test
  public void testEditNonExistingEventFails() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    boolean result = model.editSingleEvent("description", "NonExistentEvent",
            start, end, "Updated");
    assertFalse(result);
  }

  @Test
  public void testAddRecurringEventSuccessfully() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusMinutes(30);
    RecurringEvent recurring = new RecurringEvent("Standup", start, end, "MTWRF",
            3, null, "", "", true, false);
    boolean added = model.addRecurringEvent(recurring, true);
    assertTrue(added);
    assertEquals(3, model.getEvents().size());
  }

  @Test
  public void testEditRecurringEventRepeatTimesModelOnly() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusMinutes(30);
    RecurringEvent recurring = new RecurringEvent("Scrum", start, end, "MTWRF",
            5, null, "", "", true, false);
    assertTrue(model.addRecurringEvent(recurring, true));

    boolean edited = model.editRecurringEvent("Scrum", "repeattimes",
            "3");
    assertTrue(edited);
    assertEquals(3, model.getEvents().size());
  }

  @Test
  public void testQueryMultipleEventsOnSameDate() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 5, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 5, 11, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);

    model.addEvent(new SingleEvent("Event 1", start1, end1, "", "",
            true, false, null), true);
    model.addEvent(new SingleEvent("Event 2", start2, end2, "", "",
            true, false, null), true);

    List<ICalendarEvent> events = model.getEventsOnDate(start1.toLocalDate());
    assertEquals(2, events.size());
  }

  @Test
  public void testShowAvailableStatusSlot() {
    ZonedDateTime freeSlot = ZonedDateTime.of(2025, 6, 10, 15,
            0, 0, 0, model.getTimezone());
    assertFalse(model.isBusyAt(freeSlot));
  }

  @Test
  public void testEditNonexistentTimeSlotModelOnly() {
    ZonedDateTime correctStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime correctEnd = correctStart.plusHours(1);
    model.addEvent(new SingleEvent("Meeting", correctStart, correctEnd, "",
            "", true, false, null), true);

    ZonedDateTime wrongStart = ZonedDateTime.of(2025, 6, 2, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime wrongEnd = wrongStart.plusHours(1);

    boolean result = model.editSingleEvent("description", "Meeting",
            wrongStart, wrongEnd, "Updated");
    assertFalse(result);
  }

  @Test
  public void testEventEndTimeBeforeStartTimeModelOnly() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 11,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, model.getTimezone());
    SingleEvent event = new SingleEvent("Backwards Time", start, end, "",
            "", true, false, null);

    assertTrue(model.addEvent(event, true));
  }


  @Test
  public void testEditRecurringEventInvalidCommandFormatAtModelLevel() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 6, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent event = new RecurringEvent("Bootcamp", start, end, "MTW",
            4, null, "", "", true, false);
    assertTrue(model.addRecurringEvent(event, true));

    try {
      model.editRecurringEvent("Bootcamp", "time", "2025-06-01T07:00");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Unknown recurring event property"));
    }
  }

  @Test
  public void testBusySlotCheckViaModel() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    model.addEvent(new SingleEvent("Interview", start, end, "", "",
            true, false, null), true);

    ZonedDateTime busy = ZonedDateTime.of(2025, 6, 1, 9, 30,
            0, 0, model.getTimezone());
    ZonedDateTime free = ZonedDateTime.of(2025, 6, 1, 8, 0,
            0, 0, model.getTimezone());

    assertTrue(model.isBusyAt(busy));
    assertFalse(model.isBusyAt(free));
  }

  @Test
  public void testAdjacentEventsNoConflict() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1, 8,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end1 = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, model.getTimezone());

    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end2 = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, model.getTimezone());

    SingleEvent event1 = new SingleEvent("Morning Meeting", start1, end1,
            "", "", true, false, null);
    SingleEvent event2 = new SingleEvent("Next Meeting", start2, end2,
            "", "", true, false, null);

    assertTrue(model.addEvent(event1, true));
    assertTrue(model.addEvent(event2, true));
  }

  @Test
  public void testCreateAllDayEvent() {
    ZonedDateTime start = ZonedDateTime.of(2025, 7, 1, 0,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = ZonedDateTime.of(2025, 7, 1, 23,
            59, 0, 0, model.getTimezone());

    SingleEvent allDay = new SingleEvent("All Day Conference", start, end,
            "", "", true, true, null);
    boolean added = model.addEvent(allDay, true);
    assertTrue(added);
    assertEquals(1, model.getEvents().size());
    assertTrue(model.getEvents().get(0).isAllDay());
  }

  @Test
  public void testRecurringEventWithRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 8, 1, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    ZonedDateTime repeatUntil = ZonedDateTime.of(2025, 8, 31,
            10, 0, 0, 0, model.getTimezone());

    RecurringEvent seminar = new RecurringEvent("Seminar", start, end,
            "MW", 0, repeatUntil, "", "",
            true, false);
    assertTrue(model.addRecurringEvent(seminar, true));

    assertFalse(model.getEvents().isEmpty());
    for (ICalendarEvent e : model.getEvents()) {
      assertEquals("Seminar", e.getSubject());
    }
  }

  @Test
  public void testEditSingleEventLocationSuccessfully() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 12,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    SingleEvent event = new SingleEvent("Training Session", start, end, "",
            "Room A", true, false, null);
    model.addEvent(event, true);

    boolean edited = model.editSingleEvent("location", "Training Session",
            start, end, "Room B");
    assertTrue(edited);

    for (ICalendarEvent e : model.getEvents()) {
      assertEquals("Room B", e.getLocation());
    }
  }


  @Test
  public void testEditCalendarNameSuccessfully() {
    model.setName("OfficeCal");
    assertEquals("OfficeCal", model.getName());
  }

  @Test
  public void testEditCalendarTimezoneSuccessfully() {
    model.updateTimezone(ZoneId.of("Europe/Paris"));
    assertEquals(ZoneId.of("Europe/Paris"), model.getTimezone());
  }

  @Test
  public void testUpdateTimezoneAdjustsEventTimesNext() {
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 25, 10, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    model.addEvent(new SingleEvent("Test", start, end, "", "",
            true, false, null), false);

    model.updateTimezone(ZoneId.of("Europe/Paris"));
    assertEquals(ZoneId.of("Europe/Paris"), model.getTimezone());

    ZonedDateTime eventStart = (ZonedDateTime) model.getEvents().get(0).getStartDateTime();
    assertEquals(ZoneId.of("Europe/Paris"), eventStart.getZone());
  }


  @Test
  public void testCopySingleEventWithinSameCalendar() {
    CalendarModel model = new CalendarModel("Main", ZoneId.of("America/New_York"));
    ZonedDateTime originalStart = ZonedDateTime.of(2025, 3, 9, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime originalEnd = originalStart.plusHours(1);
    ICalendarEvent event = new SingleEvent("Lecture", originalStart, originalEnd,
            "CS5010", "Room A", true, false, null);
    assertTrue(model.addEvent(event, false));

    ZonedDateTime newStart = ZonedDateTime.of(2025, 3, 16, 10,
            0, 0, 0, model.getTimezone());
    assertTrue(model.copySingleEventTo(model, "Lecture",
            originalStart, model, newStart));
    assertEquals(2, model.getEvents().size());
  }

  @Test
  public void testCopySingleEventToDifferentCalendarWithTimezone() {
    CalendarModel source = new CalendarModel("Fall2024",
            ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel("Spring2025",
            ZoneId.of("Asia/Kolkata"));

    ZonedDateTime originalStart = ZonedDateTime.of(2025, 3, 9,
            10, 0, 0, 0, source.getTimezone());
    ZonedDateTime originalEnd = originalStart.plusHours(1);
    ICalendarEvent event = new SingleEvent("Lecture", originalStart, originalEnd,
            "CS5010", "Room A", true, false, null);
    assertTrue(source.addEvent(event, false));

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 3, 9, 9,
            0, 0, 0, target.getTimezone());
    assertTrue(target.copySingleEventTo(source, "Lecture", originalStart,
            target, targetStart));

    assertEquals(1, target.getEvents().size());
    ICalendarEvent copied = target.getEvents().get(0);
    assertEquals("Lecture", copied.getSubject());
    assertEquals(targetStart, copied.getStartDateTime());
    assertEquals(targetStart.plusHours(1), copied.getEndDateTime());
  }

  @Test
  public void testCopyEventsOnDateToAnotherCalendar() {
    CalendarModel source = new CalendarModel("Fall", ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel("Spring", ZoneId.of("Europe/Paris"));

    ZonedDateTime event1Start = ZonedDateTime.of(2025, 3, 9, 10,
            0, 0, 0, source.getTimezone());
    ZonedDateTime event2Start = ZonedDateTime.of(2025, 3, 9, 14,
            0, 0, 0, source.getTimezone());

    assertTrue(source.addEvent(new SingleEvent("Lecture A", event1Start,
            event1Start.plusHours(1), "", "", true, false,
            null), false));
    assertTrue(source.addEvent(new SingleEvent("Lecture B", event2Start,
            event2Start.plusHours(1), "", "", true, false,
            null), false));

    assertTrue(target.copyEventsOnDateTo(source, event1Start.toLocalDate(), target,
            ZonedDateTime.of(2025, 4, 1, 0, 0, 0,
                    0, target.getTimezone()).toLocalDate()));

    assertEquals(2, target.getEvents().size());
  }

  @Test
  public void testCopyEventsBetweenDatesToAnotherCalendar() {
    CalendarModel source = new CalendarModel("Fall", ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel("Spring", ZoneId.of("Asia/Kolkata"));

    ZonedDateTime startA = ZonedDateTime.of(2024, 9, 5, 10, 0,
            0, 0, source.getTimezone());
    ZonedDateTime startB = ZonedDateTime.of(2024, 12, 18, 14,
            0, 0, 0, source.getTimezone());

    source.addEvent(new SingleEvent("Lecture A", startA, startA.plusHours(1),
            "", "", true, false, null), false);
    source.addEvent(new SingleEvent("Lecture B", startB, startB.plusHours(2),
            "", "", true, false, null), false);

    boolean copied = target.copyEventsBetweenTo(
            source,
            LocalDate.of(2024, 9, 5),
            LocalDate.of(2024, 12, 18),
            target,
            LocalDate.of(2025, 1, 8)
    );
    assertTrue(copied);
    assertEquals(2, target.getEvents().size());
  }

  @Test
  public void testEditCalendarNameSuccessfullyMain() {
    CalendarModel calendar = new CalendarModel("WorkCal",
            ZoneId.of("America/New_York"));
    calendar.setName("OfficeCal");
    assertEquals("OfficeCal", calendar.getName());
  }

  @Test
  public void testEditCalendarTimezoneSuccessfullyMain() {
    CalendarModel calendar = new CalendarModel("Global",
            ZoneId.of("America/New_York"));
    assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());

    calendar.setTimezone(ZoneId.of("Europe/Paris"));
    assertEquals(ZoneId.of("Europe/Paris"), calendar.getTimezone());
  }

  @Test
  public void testUpdateTimezoneAdjustsEventTimesCorrectly() {
    CalendarModel calendar = new CalendarModel("TestCal",
            ZoneId.of("America/New_York"));
    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime end = start.plusHours(1);

    calendar.addEvent(new SingleEvent("Meeting", start, end,
                    "Sync", "NYC", true, false, null),
            false);

    calendar.updateTimezone(ZoneId.of("Asia/Kolkata"));
    assertEquals(ZoneId.of("Asia/Kolkata"), calendar.getTimezone());

    ICalendarEvent updatedEvent = calendar.getEvents().get(0);
    assertEquals("Meeting", updatedEvent.getSubject());

    assertEquals(start.toInstant(),
            ZonedDateTime.from(updatedEvent.getStartDateTime()).toInstant());
    assertEquals("Asia/Kolkata",
            ZonedDateTime.from(updatedEvent.getStartDateTime()).getZone().getId());
  }

  @Test
  public void testUpdateTimezoneWithRecurringEvents() {
    CalendarModel calendar = new CalendarModel("RepeatCal",
            ZoneId.of("America/New_York"));
    ZonedDateTime start = ZonedDateTime.of(2025, 6,
            1, 10, 0, 0, 0, calendar.getTimezone());
    ZonedDateTime end = start.plusHours(1);

    RecurringEvent recurring = new RecurringEvent("Standup", start, end,
            "MW", 2, null, "Daily sync",
            "NYC", true, false);
    assertTrue(calendar.addRecurringEvent(recurring, false));

    calendar.updateTimezone(ZoneId.of("Europe/Paris"));

    assertEquals(ZoneId.of("Europe/Paris"), calendar.getTimezone());

    for (ICalendarEvent ev : calendar.getEvents()) {
      assertEquals("Europe/Paris",
              ZonedDateTime.from(ev.getStartDateTime()).getZone().getId());
    }
  }

  @Test
  public void testAddConflictingEventsShouldBeRejected() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 1,
            9, 30, 0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);

    assertTrue(model.addEvent(new SingleEvent("Event1", start1, end1,
                    "", "", true, false, null),
            false));
    assertFalse(model.addEvent(new SingleEvent("Event2", start2, end2,
                    "", "", true, false, null),
            false));
  }

  @Test
  public void testAddRecurringEventConflictsWithSingleEvent() {
    ZonedDateTime singleStart = ZonedDateTime.of(2025, 6, 3,
            10, 0, 0, 0, model.getTimezone());
    ZonedDateTime singleEnd = singleStart.plusHours(1);
    assertTrue(model.addEvent(new SingleEvent("Existing", singleStart, singleEnd,
                    "", "", true, false, null),
            false));

    ZonedDateTime recurringStart = ZonedDateTime.of(2025, 6, 3,
            10, 0, 0, 0, model.getTimezone());
    ZonedDateTime recurringEnd = recurringStart.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("ConflictRecurring",
            recurringStart, recurringEnd, "T", 3, null,
            "", "", true, false);

    assertFalse(model.addRecurringEvent(recurring, false));
  }

  @Test
  public void testEditEventWithConflictFails() {
    ZonedDateTime start1 = ZonedDateTime.of(2025, 6, 5, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end1 = start1.plusHours(1);
    ZonedDateTime start2 = ZonedDateTime.of(2025, 6, 5, 11,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end2 = start2.plusHours(1);

    SingleEvent e1 = new SingleEvent("Event1", start1, end1, "", "",
            true, false, null);
    SingleEvent e2 = new SingleEvent("Event2", start2, end2, "", "",
            true, false, null);

    model.addEvent(e1, false);
    model.addEvent(e2, false);

    SingleEvent e1Updated = new SingleEvent("Event1", start2, end2, "",
            "", true, false, null);
    assertFalse(model.editEvent(e1, e1Updated));
  }

  @Test
  public void testEditEventsFromUpdatesCorrectEvents() {
    ZonedDateTime early = ZonedDateTime.of(2025, 6, 6, 8,
            0, 0, 0, model.getTimezone());
    ZonedDateTime mid = ZonedDateTime.of(2025, 6, 6, 10,
            0, 0, 0, model.getTimezone());
    ZonedDateTime late = ZonedDateTime.of(2025, 6, 6, 12,
            0, 0, 0, model.getTimezone());

    model.addEvent(new SingleEvent("BatchEdit", early, early.plusHours(1),
                    "Old", "", true, false, null),
            false);
    model.addEvent(new SingleEvent("BatchEdit", mid, mid.plusHours(1),
                    "Old", "", true, false, null),
            false);
    model.addEvent(new SingleEvent("BatchEdit", late, late.plusHours(1),
                    "Old", "", true, false, null),
            false);

    boolean result = model.editEventsFrom("description", "BatchEdit", mid,
            "Updated");
    assertTrue(result);

    for (ICalendarEvent e : model.getEvents()) {
      if (!ZonedDateTime.from(e.getStartDateTime()).isBefore(mid)) {
        assertEquals("Updated", e.getDescription());
      } else {
        assertEquals("Old", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsAllSuccessfully() {
    ZonedDateTime s1 = ZonedDateTime.of(2025, 6, 10, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime s2 = s1.plusHours(1);
    ZonedDateTime s3 = ZonedDateTime.of(2025, 6, 10, 11, 0,
            0, 0, model.getTimezone());
    ZonedDateTime s4 = s3.plusHours(1);

    model.addEvent(new SingleEvent("EditAll", s1, s2, "Desc", "",
            true, false, null), false);
    model.addEvent(new SingleEvent("EditAll", s3, s4, "Desc", "",
            true, false, null), false);

    boolean result = model.editEventsAll("description", "EditAll",
            "UpdatedDesc");
    assertTrue(result);

    for (ICalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditAll")) {
        assertEquals("UpdatedDesc", e.getDescription());
      }
    }
  }

  @Test
  public void testEditSingleEventSuccess() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 11, 14,
            0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    model.addEvent(new SingleEvent("EditMe", start, end, "Initial",
            "", true, false, null), false);

    boolean result = model.editSingleEvent("description", "EditMe",
            start, end, "Changed");
    assertTrue(result);

    ICalendarEvent updated = model.getEvents().stream()
            .filter(e -> e.getSubject().equals("EditMe"))
            .findFirst()
            .orElse(null);
    assertNotNull(updated);
    assertEquals("Changed", updated.getDescription());
  }

  @Test
  public void testCopySingleEventWithinSameCalendarMain() {
    CalendarModel source = new CalendarModel("WorkCal",
            ZoneId.of("America/New_York"));
    CalendarModel target = source;

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, source.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    source.addEvent(new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null), false);

    ZonedDateTime newStart = ZonedDateTime.of(2025, 6, 8, 9,
            0, 0, 0, target.getTimezone());
    boolean copied = target.copySingleEventTo(source, "Meeting", start,
            target, newStart);
    assertTrue(copied);
    assertEquals(2, target.getEvents().size());
  }

  @Test
  public void testCopySingleEventAcrossCalendarsWithTimezoneShift() {
    CalendarModel source = new CalendarModel("IndiaCal", ZoneId.of("Asia/Kolkata"));
    CalendarModel target = new CalendarModel("USCal", ZoneId.of("America/New_York"));

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 6, 1,
            10, 0, 0, 0, source.getTimezone());
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);
    source.addEvent(new SingleEvent("Discussion", sourceStart, sourceEnd,
                    "Timezones", "Zoom", true, false,
                    null),
            false);

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 6, 2, 9,
            0, 0, 0, target.getTimezone());
    boolean copied = target.copySingleEventTo(source, "Discussion", sourceStart,
            target, targetStart);
    assertTrue(copied);

    ICalendarEvent copiedEvent = target.getEvents().get(0);
    assertEquals(targetStart, copiedEvent.getStartDateTime());
    assertEquals("Timezones", copiedEvent.getDescription());
  }

  @Test
  public void testCopyEventsOnDateToAnotherCalendar2() {
    CalendarModel source = new CalendarModel("TeachingCal",
            ZoneId.of("Europe/Paris"));
    CalendarModel target = new CalendarModel("ClonedCal", ZoneId.of("Asia/Kolkata"));

    ZonedDateTime event1 = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, source.getTimezone());
    ZonedDateTime event2 = ZonedDateTime.of(2025, 6, 1, 14, 0,
            0, 0, source.getTimezone());
    source.addEvent(new SingleEvent("Lecture 1", event1, event1.plusHours(1),
            "", "", true, false, null), false);
    source.addEvent(new SingleEvent("Lecture 2", event2, event2.plusHours(1),
            "", "", true, false, null), false);

    ZonedDateTime targetDate = ZonedDateTime.of(2025, 7, 1, 0,
            0, 0, 0, target.getTimezone());
    boolean copied = target.copyEventsOnDateTo(source, event1.toLocalDate(), target,
            targetDate.toLocalDate());
    assertTrue(copied);
    assertEquals(2, target.getEvents().size());
  }

  @Test
  public void testCopyEventsBetweenDatesAcrossCalendars() {
    CalendarModel source = new CalendarModel("Fall2024",
            ZoneId.of("America/Chicago"));
    CalendarModel target = new CalendarModel("Spring2025",
            ZoneId.of("America/New_York"));

    ZonedDateTime sep5 = ZonedDateTime.of(2024, 9, 5, 10,
            0, 0, 0, source.getTimezone());
    ZonedDateTime nov20 = ZonedDateTime.of(2024, 11, 20, 11,
            0, 0, 0, source.getTimezone());
    ZonedDateTime dec18 = ZonedDateTime.of(2024, 12, 18, 13,
            0, 0, 0, source.getTimezone());

    source.addEvent(new SingleEvent("Intro", sep5, sep5.plusHours(1), "",
            "", true, false, null), false);
    source.addEvent(new SingleEvent("Midterm", nov20, nov20.plusHours(1), "",
            "", true, false, null), false);
    source.addEvent(new SingleEvent("Review", dec18, dec18.plusHours(1), "",
            "", true, false, null), false);

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 1, 8, 0,
            0, 0, 0, target.getTimezone());
    boolean copied = target.copyEventsBetweenTo(source, sep5.toLocalDate(),
            dec18.toLocalDate(), target, targetStart.toLocalDate());
    assertTrue(copied);
    assertEquals(3, target.getEvents().size());
  }

  @Test
  public void testCopyEventFailsDueToConflict() {
    CalendarModel source = new CalendarModel("TeamCal",
            ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel("ConflictCal",
            ZoneId.of("America/New_York"));

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 15, 10, 0,
            0, 0, source.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    source.addEvent(new SingleEvent("Planning", start, end, "Team Sync",
            "Boardroom", true, false, null), false);

    ZonedDateTime conflictStart = ZonedDateTime.of(2025, 6, 16, 10,
            0, 0, 0, target.getTimezone());
    target.addEvent(new SingleEvent("Occupied", conflictStart, conflictStart.plusHours(1),
                    "", "", true, false, null),
            false);

    boolean copied = target.copySingleEventTo(source, "Planning",
            start, target, conflictStart);
    assertFalse(copied);
    assertEquals(1, target.getEvents().size());
  }

  @Test
  public void testUpdateCalendarTimezoneAdjustsEventsCorrectly() {
    CalendarModel model = new CalendarModel("TestCal", ZoneId.of("America/New_York"));

    ZonedDateTime start = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime end = start.plusHours(1);
    ICalendarEvent event = new SingleEvent("Meeting", start, end, "Desc",
            "Room", true, false, null);

    assertTrue(model.addEvent(event, false));

    model.updateTimezone(ZoneId.of("Europe/Paris"));

    ICalendarEvent updatedEvent = model.getEvents().get(0);
    assertEquals(ZoneId.of("Europe/Paris"),
            ZonedDateTime.from(updatedEvent.getStartDateTime()).getZone());
    assertEquals(ZoneId.of("Europe/Paris"),
            ZonedDateTime.from(updatedEvent.getEndDateTime()).getZone());
  }

  @Test
  public void testRecurringEventTimezoneUpdateMaintainsDuration() {
    CalendarModel model = new CalendarModel("TZCal", ZoneId.of("America/New_York"));

    ZonedDateTime start = ZonedDateTime.of(2025, 3, 11, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Yoga", start, end, "T",
            2, null, "Stretching", "Studio", true,
            false);

    assertTrue(model.addRecurringEvent(recurring, false));
    List<ICalendarEvent> beforeUpdate = model.getEvents();

    model.updateTimezone(ZoneId.of("Asia/Kolkata"));
    List<ICalendarEvent> afterUpdate = model.getEvents();

    assertEquals(beforeUpdate.size(), afterUpdate.size());

    for (int i = 0; i < beforeUpdate.size(); i++) {
      Duration durationBefore = Duration.between(
              ZonedDateTime.from(beforeUpdate.get(i).getStartDateTime()),
              ZonedDateTime.from(beforeUpdate.get(i).getEndDateTime()));
      Duration durationAfter = Duration.between(
              ZonedDateTime.from(afterUpdate.get(i).getStartDateTime()),
              ZonedDateTime.from(afterUpdate.get(i).getEndDateTime()));
      assertEquals(durationBefore, durationAfter);
      assertEquals(ZoneId.of("Asia/Kolkata"),
              ZonedDateTime.from(afterUpdate.get(i).getStartDateTime()).getZone());
    }
  }

  @Test
  public void testTimezoneConversionWhenCopyingAcrossCalendars() {
    CalendarModel source = new CalendarModel("Source", ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel("Target", ZoneId.of("Asia/Kolkata"));

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, source.getTimezone());
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);
    ICalendarEvent original = new SingleEvent("Standup", sourceStart, sourceEnd,
            "Daily sync", "Zoom", true, false, null);

    assertTrue(source.addEvent(original, false));

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 6, 10,
            10, 0, 0, 0, target.getTimezone());

    boolean copied = target.copySingleEventTo(source, "Standup", sourceStart,
            target, targetStart);
    assertTrue(copied);

    List<ICalendarEvent> eventsInTarget = target.getEvents();
    assertEquals(1, eventsInTarget.size());

    ICalendarEvent copiedEvent = eventsInTarget.get(0);
    assertEquals("Standup", copiedEvent.getSubject());
    assertEquals(targetStart, copiedEvent.getStartDateTime());
    assertEquals(targetStart.plusHours(1), copiedEvent.getEndDateTime());
    assertEquals(ZoneId.of("Asia/Kolkata"),
            ZonedDateTime.from(copiedEvent.getStartDateTime()).getZone());
  }

  @Test
  public void testAddDuplicateCalendarNameFails() {
    CalendarManager manager = new CalendarManager();
    assertTrue(manager.addCalendar("Work", ZoneId.of("America/New_York")));
    assertFalse(manager.addCalendar("Work", ZoneId.of("Europe/Paris")));
  }

  @Test(expected = ZoneRulesException.class)
  public void testCreateCalendarInvalidTimezone() {
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("InvalidTZ", ZoneId.of("Invalid/Zone"));
  }

  @Test
  public void testEmptyCalendarHasNoEvents() {
    CalendarModel model = new CalendarModel("Empty", ZoneId.of("UTC"));
    assertEquals(0, model.getEvents().size());
  }

  @Test
  public void testCopyEventNonexistentFails() {
    CalendarModel cal1 = new CalendarModel("Source", ZoneId.of("UTC"));
    CalendarModel cal2 = new CalendarModel("Target", ZoneId.of("UTC"));

    ZonedDateTime sourceTime = ZonedDateTime.of(2025, 3, 10, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime targetTime = ZonedDateTime.of(2025, 3, 11, 9,
            0, 0, 0, ZoneId.of("UTC"));

    boolean copied = cal2.copySingleEventTo(cal1, "Nonexistent", sourceTime,
            cal2, targetTime);
    assertFalse(copied);
  }

  @Test
  public void testCopyWithConflictInTargetCalendar() {
    CalendarModel source = new CalendarModel("Source", ZoneId.of("UTC"));
    CalendarModel target = new CalendarModel("Target", ZoneId.of("UTC"));

    ZonedDateTime sourceStart = ZonedDateTime.of(2025, 3, 10, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime sourceEnd = sourceStart.plusHours(1);

    ZonedDateTime targetStart = ZonedDateTime.of(2025, 3, 11, 10,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime targetEnd = targetStart.plusHours(1);

    source.addEvent(new SingleEvent("Meeting", sourceStart, sourceEnd, "",
            "", true, false, null), false);
    target.addEvent(new SingleEvent("Conflict", targetStart, targetEnd, "",
            "", true, false, null), false);

    boolean result = target.copySingleEventTo(source, "Meeting", sourceStart,
            target, targetStart);
    assertFalse(result);
  }

  @Test
  public void testUpdateCalendarTimezoneShiftsEventTimesCorrectly() {
    CalendarModel model = new CalendarModel("WorkCal", ZoneId.of("America/New_York"));
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
            0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    ICalendarEvent event = new SingleEvent("Meeting", start, end, "Desc",
            "Office", true, false, null);
    model.addEvent(event, false);

    model.updateTimezone(ZoneId.of("Europe/Paris"));

    ICalendarEvent updatedEvent = model.getEvents().get(0);
    assertEquals(ZoneId.of("Europe/Paris"),
            ZonedDateTime.from(updatedEvent.getStartDateTime()).getZone());
    assertEquals(15, ZonedDateTime.from(updatedEvent.getStartDateTime()).getHour());
  }

  @Test
  public void testCopyEventAcrossTimezonesPreservesDuration() {
    CalendarModel source = new CalendarModel("SourceCal", ZoneId.of("Asia/Kolkata"));
    CalendarModel target = new CalendarModel("TargetCal",
            ZoneId.of("America/New_York"));

    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1,
            10, 0, 0, 0, source.getTimezone());
    ZonedDateTime end = start.plusHours(2);
    ICalendarEvent event = new SingleEvent("Training", start, end,
            "Session", "Room A", true, false, null);
    source.addEvent(event, false);

    Temporal sourceTime = start;
    Temporal targetTime = ZonedDateTime.of(2025, 6, 5, 9,
            0, 0, 0, target.getTimezone());

    boolean copied = target.copySingleEventTo(source, "Training",
            sourceTime, target, targetTime);
    assertTrue(copied);

    ICalendarEvent copiedEvent = target.getEvents().get(0);
    assertEquals(ZoneId.of("America/New_York"),
            ZonedDateTime.from(copiedEvent.getStartDateTime()).getZone());
    assertEquals(2, Duration.between(copiedEvent.getStartDateTime(),
            copiedEvent.getEndDateTime()).toHours());
  }

  @Test
  public void testTimezoneShiftUpdatesRecurringEvents() {
    CalendarModel model = new CalendarModel("LectureCal",
            ZoneId.of("America/New_York"));
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 3,
            9, 0, 0, 0, model.getTimezone());
    ZonedDateTime end = start.plusHours(1);
    RecurringEvent recurring = new RecurringEvent("Lecture", start, end,
            "T", 2, null, "CS", "Room",
            true, false);
    assertTrue(model.addRecurringEvent(recurring, false));

    model.updateTimezone(ZoneId.of("Europe/Paris"));

    List<ICalendarEvent> events = model.getEvents();
    for (ICalendarEvent ev : events) {
      assertEquals(ZoneId.of("Europe/Paris"),
              ZonedDateTime.from(ev.getStartDateTime()).getZone());
    }
  }

}

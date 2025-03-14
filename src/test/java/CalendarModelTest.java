import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the CalendarModel class.
 */
public class CalendarModelTest {
  private CalendarModel model;
  private TestCalendarView view;
  private CalendarController controller;

  @Before
  public void setUp() {
    model = new CalendarModel();
    CommandParser parser = new CommandParser(model);
    view = new TestCalendarView();
    controller = new CalendarController(model, view, parser);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEventDuplicate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event1 = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(event1, false);
    CalendarEvent event2 = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(event2, false);
  }

  @Test
  public void testAddEventNoConflict() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Meeting", start, end, "Desc",
            "Loc", true, false, null);
    boolean added = model.addEvent(event, false);
    assertTrue(added);
  }

  @Test
  public void testAddEventConflictAutoDecline() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event1 = new SingleEvent("Meeting1", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(event1, false);
    CalendarEvent event2 = new SingleEvent("Meeting2", start.plusMinutes(30),
            end.plusMinutes(30), "Desc", "Loc",
            true, false, null);
    boolean added = model.addEvent(event2, true);
    assertFalse(added);
  }

  @Test
  public void testAddRecurringEventNoConflict() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    RecurringEvent recurring = new RecurringEvent("Recurring", start, end, "M",
            2, null, "Desc", "Loc", true,
            false);
    boolean added = model.addRecurringEvent(recurring, false);
    assertTrue(added);
    List<CalendarEvent> events = model.getEvents();
    assertTrue(events.size() >= 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddRecurringEventDuplicateOccurrence() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
    RecurringEvent recurring = new RecurringEvent("RecurringDup", start, end,
            "M", 2, null, "Desc", "Loc",
            true, false);
    model.addRecurringEvent(recurring, false);
    model.addRecurringEvent(recurring, false);
  }

  @Test
  public void testAddRecurringEventConflictAutoDecline() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 3, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 10, 0);
    CalendarEvent conflicting = new SingleEvent("Conflict", start, end, "Desc",
            "Loc", true, false, null);
    model.addEvent(conflicting, false);
    RecurringEvent recurring = new RecurringEvent("RecurringConflict", start, end,
            "T", 2, null, "Desc", "Loc",
            true, false);
    boolean added = model.addRecurringEvent(recurring, true);
    assertFalse(added);
  }

  @Test
  public void testGetEventsOnDate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    model.addEvent(event, false);
    List<CalendarEvent> events = model.getEventsOnDate(LocalDate.of(2025, 6,
            1));
    assertEquals(1, events.size());
  }

  @Test
  public void testGetEventsOnDateNoMatch() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Meeting", start, end, "", "",
            true, false, null);
    model.addEvent(event, false);
    List<CalendarEvent> events = model.getEventsOnDate(LocalDate.of(2025,
            5, 31));
    assertEquals(0, events.size());
  }

  @Test
  public void testGetEventsBetween() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Workshop", start, end, "",
            "", true, false, null);
    model.addEvent(event, false);
    List<CalendarEvent> events = model.getEventsBetween(LocalDateTime.of(2025, 6,
            1, 8, 0), LocalDateTime.of(2025, 6,
            1, 11, 0));
    assertEquals(1, events.size());
  }

  @Test
  public void testIsBusyAt() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Interview", start, end, "",
            "", true, false, null);
    model.addEvent(event, false);
    assertTrue(model.isBusyAt(LocalDateTime.of(2025, 6, 1, 9,
            30)));
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 6, 1, 8,
            30)));
  }

  @Test
  public void testEditEventNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 7, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 7, 10, 0);
    SingleEvent oldEvent = new SingleEvent("EditTest", start1, end1, "Desc",
            "Loc", true, false, null);
    model.addEvent(oldEvent, false);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 7, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 7, 11, 0);
    SingleEvent newEvent = new SingleEvent("EditTest", start2, end2, "NewDesc",
            "NewLoc", true, false, null);
    boolean edited = model.editEvent(oldEvent, newEvent);
    assertTrue(edited);
    List<CalendarEvent> evs = model.getEvents();
    assertTrue(evs.contains(newEvent));
    assertFalse(evs.contains(oldEvent));
  }

  @Test
  public void testEditEventConflictDirect() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 20, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 20, 10, 0);
    SingleEvent event1 = new SingleEvent("EditTest", start1, end1, "Desc",
            "Loc", true, false, null);
    model.addEvent(event1, false);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 20, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 20, 11, 0);
    SingleEvent event2 = new SingleEvent("Other", start2, end2, "Desc",
            "Loc", true, false, null);
    model.addEvent(event2, false);
    SingleEvent newEvent = new SingleEvent("EditTest", start2, end2, "NewDesc",
            "NewLoc", true, false, null);
    boolean edited = model.editEvent(event1, newEvent);
    assertFalse(edited);
    List<CalendarEvent> evs = model.getEvents();
    assertTrue(evs.contains(event1));
  }

  @Test
  public void testEditSingleEventNotFound() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 9, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 9, 10, 0);
    boolean edited = model.editSingleEvent("description", "Nonexistent", start,
            end, "New");
    assertFalse(edited);
  }

  @Test
  public void testEditEventsFromNotFound() {
    LocalDateTime from = LocalDateTime.of(2025, 6, 10, 9, 0);
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
    LocalDateTime start = LocalDateTime.of(2025, 6, 11, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 11, 10, 0);
    SingleEvent event = new SingleEvent("EditSingle", start, end, "Old",
            "Loc", true, false, null);
    model.addEvent(event, false);
    boolean edited = model.editSingleEvent("description", "EditSingle",
            start, end, "New");
    assertTrue(edited);
    for (CalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditSingle")) {
        assertEquals("New", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsFromUpdate() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 13, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 13, 10, 0);
    SingleEvent event = new SingleEvent("EditFrom", start, end, "Old",
            "Loc", true, false, null);
    model.addEvent(event, false);
    boolean edited = model.editEventsFrom("description", "EditFrom",
            start, "New");
    assertTrue(edited);
    for (CalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditFrom")) {
        assertEquals("New", e.getDescription());
      }
    }
  }

  @Test
  public void testEditEventsAllUpdate() {
    SingleEvent event1 = new SingleEvent("EditAll", LocalDateTime.of(2025,
            6, 14, 9, 0), LocalDateTime.of(2025, 6,
            14, 10, 0), "Old1", "Loc", true,
            false, null);
    SingleEvent event2 = new SingleEvent("EditAll", LocalDateTime.of(2025,
            6, 14, 11, 0), LocalDateTime.of(2025, 6,
            14, 12, 0), "Old2", "Loc", true,
            false, null);
    model.addEvent(event1, false);
    model.addEvent(event2, false);
    boolean edited = model.editEventsAll("description", "EditAll",
            "Updated");
    assertTrue(edited);
    for (CalendarEvent e : model.getEvents()) {
      if (e.getSubject().equals("EditAll")) {
        assertEquals("Updated", e.getDescription());
      }
    }
  }

  @Test
  public void testEditRecurringEventValid() throws Exception {
    LocalDateTime start = LocalDateTime.of(2025, 6, 15, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 15, 10, 0);
    RecurringEvent recurring = new RecurringEvent("RecurringEdit", start, end,
            "F", 2, null, "Desc", "Loc",
            true,
            false);
    model.addRecurringEvent(recurring, false);
    model.editRecurringEvent("RecurringEdit", "description",
            "Updated");
    Field field = model.getClass().getDeclaredField("recurringMap");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, RecurringEvent> recurringMap = (java.util.Map<String, RecurringEvent>)
            field.get(model);
    RecurringEvent updatedRecurring = recurringMap.get("RecurringEdit");
    assertEquals("Updated", updatedRecurring.getDescription());
    for (CalendarEvent ev : model.getEvents()) {
      if (ev instanceof SingleEvent && ev.getSubject().equals("RecurringEdit")) {
        assertEquals("Updated", ev.getDescription());
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventInvalidProperty() {
    RecurringEvent event = new RecurringEvent("Yoga", LocalDateTime.of(2025,
            6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "MWF", 5, null, "Session", "Studio",
            true, false);
    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "repeattimes", "0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventUnsupportedProperty() {
    RecurringEvent event = new RecurringEvent("Yoga", LocalDateTime.of(2025, 6,
            1, 8, 0),
            LocalDateTime.of(2025, 6, 1, 9, 0), "MTW",
            5, null, "", "", true, false);
    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "invalidproperty", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventNotFound() {
    model.editRecurringEvent("NonExistent", "description", "Updated");
  }

  @Test
  public void testEditRecurringEventDescriptionSuccessfully() {
    RecurringEvent event = new RecurringEvent("Yoga", LocalDateTime.of(2025, 6,
            1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0), "MWF",
            5, null, "Morning session", "Gym",
            true, false);
    model.addRecurringEvent(event, false);
    boolean edited = model.editRecurringEvent("Yoga", "description",
            "Evening session");
    assertTrue(edited);
    List<CalendarEvent> events = model.getEvents();
    assertEquals("Evening session", events.get(0).getDescription());
  }

  @Test
  public void testEditRecurringEventRepeatUntil() {
    RecurringEvent event = new RecurringEvent("Pilates", LocalDateTime.of(2025,
            6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "TR", 0, LocalDateTime.of(2025, 6, 30,
            7, 0),
            "Session", "Studio", true, false);
    model.addRecurringEvent(event, false);
    boolean edited = model.editRecurringEvent("Pilates", "repeatuntil",
            "2025-07-01T07:00");
    assertTrue(edited);
  }

  @Test
  public void testEditRecurringEventRepeatingDays() {
    RecurringEvent event = new RecurringEvent("Dance Class",
            LocalDateTime.of(2025, 6, 1, 18, 0),
            LocalDateTime.of(2025, 6, 1, 19, 0),
            "MT", 4, null, "Evening class",
            "Dance Studio", true, false);
    model.addRecurringEvent(event, false);
    boolean edited = model.editRecurringEvent("Dance Class",
            "repeatingdays", "WRF");
    assertTrue(edited);
  }

  @Test
  public void testEditRecurringEventLocation() {
    RecurringEvent event = new RecurringEvent("Training",
            LocalDateTime.of(2025, 6, 2, 15, 0),
            LocalDateTime.of(2025, 6, 2, 16, 0),
            "WRF", 3, null, "Sessions",
            "Room 101", true, false);
    model.addRecurringEvent(event, false);
    boolean edited = model.editRecurringEvent("Training",
            "location", "Room 202");
    assertTrue(edited);
  }

  @Test
  public void testEditRecurringEventAndCheckOccurrences() {
    RecurringEvent event = new RecurringEvent("Sprint Planning",
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0),
            "MTWRF", 5, null, "Daily Planning",
            "Office", true, false);
    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Sprint Planning", "description",
            "Updated Daily Planning");
    List<CalendarEvent> events = model.getEvents();
    assertEquals(5, events.size());
    assertEquals("Updated Daily Planning", events.get(0).getDescription());
  }

  @Test
  public void testAddSingleEventViaController() {
    assertTrue(controller.processCommand("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00"));
    assertEquals(1, model.getEvents().size());
  }

  @Test
  public void testAddDuplicateSingleEventViaController() {
    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00 "
            + "to 2025-06-01T10:00");
    boolean result = controller.processCommand("create event \"Meeting\" from"
            + " 2025-06-01T09:00 to 2025-06-01T10:00");
    assertFalse(result);
    assertEquals("Duplicate event: subject, start and end are identical.",
            view.getLastMessage());
  }

  @Test
  public void testEditEventSuccessViaController() {
    controller.processCommand("create event \"Team Meeting\" from 2025-06-01T09:00"
            + " to 2025-06-01T10:00");
    boolean result = controller.processCommand("edit event description "
            + "\"Team Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 with"
            + " \"Updated Team Meeting\"");
    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventConflictWithAutoDeclineViaController() {
    controller.processCommand("create event --autoDecline \"Project Meeting\" "
            + "from 2025-06-02T10:00 to 2025-06-02T11:00");
    controller.processCommand("create event --autoDecline \"Client Meeting\" "
            + "from 2025-06-02T11:00 to 2025-06-02T12:00");
    boolean result = controller.processCommand("edit event \"Project Meeting\" "
            + "from 2025-06-02T10:00 to 2025-06-02T11:00 with \"2025-06-02T10:30\"");
    assertFalse(result);
    assertEquals("Parsing Error: Incomplete edit event command. Expected format: "
                    + "edit event <property> <eventName> from <start> to <end>"
                    + " with <NewPropertyValue>",
            view.getLastMessage());
  }

  @Test
  public void testEditNonExistingEventViaController() {
    boolean result = controller.processCommand("edit event description "
            + "\"NonExistentEvent\" from 2025-06-01T10:00 to 2025-06-01T11:00 with \"Updated\"");
    assertFalse(result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testAddRecurringEventSuccessMainViaController() {
    boolean result = controller.processCommand("create event \"Standup\" "
            + "from 2025-06-01T10:00 to 2025-06-01T10:30 repeats MTWRF for 3 times");
    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testEditRecurringEventRepeatTimesViaController() {
    controller.processCommand("create event \"Scrum\" from 2025-06-01T09:00 "
            + "to 2025-06-01T09:30 repeats MTWRF for 5 times");
    boolean edited = model.editRecurringEvent("Scrum", "repeattimes",
            "3");
    assertTrue(edited);
    assertEquals(3, model.getEvents().size());
  }

  @Test
  public void testQueryMultipleEventsOnDate() {
    controller.processCommand("create event \"Event 1\" "
            + "from 2025-06-05T09:00 to 2025-06-05T10:00");
    controller.processCommand("create event \"Event 2\" "
            + "from 2025-06-05T11:00 to 2025-06-05T12:00");
    assertTrue(controller.processCommand("print events on 2025-06-05"));
    assertTrue(view.getLastMessage().contains("Displaying 2 events"));
  }

  @Test
  public void testShowAvailableStatus() {
    assertTrue(controller.processCommand("show status on 2025-06-10T15:00"));
    assertEquals("Available at 2025-06-10T15:00", view.getLastMessage());
  }


  @Test
  public void testEditNonexistentTimeSlot() {
    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00 "
            + "to 2025-06-01T10:00");
    assertFalse(controller.processCommand("edit event description \"Meeting\" "
            + "from 2025-06-02T09:00 to 2025-06-02T10:00 with \"Updated\""));
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testEventEndTimeBeforeStartTime() {
    assertFalse(controller.processCommand("create event \"Backwards Time\" "
            + "from 2025-06-01T11:00 to 2025-06-01T10:00"));
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testIncorrectShowStatusTimeFormat() {
    assertFalse(controller.processCommand("show status on 2025-06-01 10:00"));
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testEditRecurringEventUnsupportedCommandFormat() {
    controller.processCommand("create event \"Bootcamp\" from 2025-06-01T06:00 "
            + "to 2025-06-01T07:00 repeats MTW for 4 times");
    assertFalse(controller.processCommand("edit events time \"Bootcamp\" with "
            + "\"2025-06-01T07:00\""));
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testRecurringEventMissingRepeatDetails() {
    assertFalse(controller.processCommand("create event \"Ambiguous Recurrence\""
            + " from 2025-06-01T08:00 to 2025-06-01T09:00 repeats MTW"));
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testCheckBusySlotViaController() {
    controller.processCommand("create event \"Interview\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue(model.isBusyAt(LocalDateTime.of(2025, 6, 1,
            9, 30)));
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 6, 1,
            8, 0)));
  }

  @Test
  public void testAdjacentEventsNoConflictViaController() {
    controller.processCommand("create event \"Morning Meeting\" from "
            + "2025-06-01T08:00 to 2025-06-01T09:00");
    boolean result = controller.processCommand("create event \"Next Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue(result);
  }

  @Test
  public void testCreateAllDayEvent() {
    assertTrue(controller.processCommand("create event \"All Day Conference\""
            + " from 2025-07-01T00:00 to 2025-07-01T23:59"));
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testRecurringEventWithRepeatUntil() {
    assertTrue(controller.processCommand("create event \"Seminar\" "
            + "from 2025-08-01T10:00 to 2025-08-01T11:00 repeats MW until 2025-08-31T10:00"));
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testEditSingleEventLocationSuccessfully() {
    controller.processCommand("create event \"Training Session\" from 20"
            + "25-06-01T12:00 to 2025-06-01T13:00 location \"Room A\"");
    assertTrue(controller.processCommand("edit event location "
            + "\"Training Session\" from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Room B\""));
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }


  private static class TestCalendarView implements ICalendarView {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String error) {
      messages.add(error);
    }

    @Override
    public void displayEvents(List<CalendarEvent> events) {
      messages.add("Displaying " + events.size() + " events");
    }

    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
  }
}

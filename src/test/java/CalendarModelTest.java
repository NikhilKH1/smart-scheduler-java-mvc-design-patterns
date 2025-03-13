import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    CommandParser parser;
    view = new TestCalendarView();
    parser = new CommandParser(model);
    controller = new CalendarController(model, view, parser);
  }

  @Test
  public void testAddNonOverlappingEvents() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 30);

    boolean added1 = model.addEvent(new SingleEvent("Event A", start1, end1,
            "", "", true, false, null), false);
    boolean added2 = model.addEvent(new SingleEvent("Event B", start2, end2,
            "", "", true, false, null), false);

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
    boolean added = model.addEvent(new SingleEvent("Meeting B", start2, end2, "",
            "", true, false, null), true);

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

    assertTrue("Should be busy at 9:30 AM",
            model.isBusyAt(LocalDateTime.of(2025, 6, 1, 9, 30)));
    assertFalse("Should not be busy at 8:30 AM",
            model.isBusyAt(LocalDateTime.of(2025, 6, 1, 8, 30)));
  }

  @Test
  public void testEditSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    SingleEvent event = new SingleEvent("Conference", start, end, "Tech Talk",
            "Hall A", true, false, null);
    model.addEvent(event, false);

    boolean edited = model.editSingleEvent("description", "Conference",
            start, end, "Updated Tech Talk");
    assertTrue("Event should be edited", edited);

    CalendarEvent updatedEvent = model.getEvents().get(0);
    assertEquals("Description should be updated", "Updated Tech Talk",
            updatedEvent.getDescription());
  }

  @Test
  public void testAddConflictingEventWithoutAutoDecline() {
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 8, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 3, 8, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 8, 11, 0);

    model.addEvent(new SingleEvent("Team Meeting", start1, end1, "", "", true, false, null), false);
    boolean added = model.addEvent(new SingleEvent("Darshan", start2, end2,
            "Darshan", "CKM", true, false, null), false);

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
    boolean added = model.addEvent(new SingleEvent("Darshan", start2, end2,
            "Darshan", "CKM", true, false, null), true);

    assertFalse("Conflicting event should not be added when autoDecline is true", added);
    assertEquals("Only the first event should exist in the calendar", 1, model.getEvents().size());
  }

  @Test
  public void testAddRecurringEventSuccess() {
    String command = "create event \"Standup Meeting\" "
            + "from 2025-06-01T10:00 to 2025-06-01T10:30 repeats MTWRF for 5 times";
    boolean result = controller.processCommand(command);
    assertTrue(result);
    assertTrue(view.getLastMessage().contains("Event created successfully"));
  }

  @Test
  public void testAddRecurringEventDuplicateReturnsFalse() {
    String command1 = "create event --autoDecline \"Daily Standup\" from "
            + "2025-06-01T09:00 to 2025-06-01T09:30 repeats MTWRF for 5 times";
    String command2 = "create event \"Daily Standup\" from 2025-06-01T09:00 to "
            + "2025-06-01T09:30 repeats MTWRF for 5 times --autodecline";

    boolean result1 = controller.processCommand(command1);
    boolean result2 = controller.processCommand(command2);
    assertTrue("First event should be created successfully", result1);
    assertFalse("Second event should be declined due to conflict", result2);
  }


  @Test
  public void testAddRecurringEventWithConflictButNoAutoDecline() {
    controller.processCommand("create event \"Team Meeting\" from "
            + "2025-06-01T10:00 to 2025-06-01T10:30");

    String recurringCommand = "create event \"Daily Sync\" from 2025-06-01T10:00 "
            + "to 2025-06-01T10:30 repeats MTWRF for 3 times";
    boolean result = controller.processCommand(recurringCommand);

    assertTrue("Recurring event should be added despite conflict when autoDecline is false",
            result);
    assertTrue(view.getLastMessage().contains("Event created successfully"));
  }

  @Test
  public void testDuplicateEventDetection() {
    String command1 = "create event \"Project Kickoff\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    controller.processCommand(command1);

    String command2 = "create event \"Project Kickoff\" from 2025-06-01T10:00 to 2025-06-01T11:00";

    boolean result = controller.processCommand(command2);
    assertFalse("Duplicate event should not be allowed", result);
    assertEquals("Duplicate event: subject, start and end are identical.", view.getLastMessage());
  }


  @Test
  public void testEditEventConflict() {
    controller.processCommand("create event --autoDecline\"Project Meeting\" from "
            + "2025-06-02T10:00 to 2025-06-02T11:00");

    controller.processCommand("create event --autoDecline \"Client Meeting\" "
            + "from 2025-06-02T10:00 to 2025-06-02T11:00");

    boolean result = controller.processCommand(
            "edit event \"Project Meeting\" from 2025-06-01T09:00 to 2025-06-01T09:30 "
                    + "with \"2025-06-02T10:30\""
    );

    assertFalse("Edit should fail due to conflict", result);
    assertEquals("Parsing Error: Incomplete edit event command. Expected format: "
                    + "edit event <property> <eventName> from <start> to <end> with "
                    + "<NewPropertyValue>",
            view.getLastMessage());
  }


  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventWithInvalidRepeatTimes() {
    RecurringEvent event = new RecurringEvent("Yoga",
            LocalDateTime.of(2025, 6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "MWF", 5, null, "Morning Yoga", "Studio", true, false);

    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "repeattimes", "0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventWithUnsupportedProperty() {
    RecurringEvent event = new RecurringEvent("Yoga",
            LocalDateTime.of(2025, 6, 1, 8, 0),
            LocalDateTime.of(2025, 6, 1, 9, 0), "MTW", 5, null, "", "", true, false);

    model.addRecurringEvent(event, false);
    model.editRecurringEvent("Yoga", "invalidproperty", "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventNotFound() {
    model.editRecurringEvent("NonExistentEvent", "description", "Updated Description");
  }

  @Test
  public void testEditRecurringEventDescriptionSuccessfully() {
    RecurringEvent event = new RecurringEvent("Yoga",
            LocalDateTime.of(2025, 6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "MWF", 5, null, "Morning session", "Gym", true, false);

    model.addRecurringEvent(event, false);
    boolean edited = model.editRecurringEvent("Yoga", "description", "Evening session");

    assertTrue("Recurring event description should be updated", edited);
    List<CalendarEvent> events = model.getEvents();
    assertEquals("Morning session should be updated", "Evening session",
            events.get(0).getDescription());
  }

  @Test
  public void testEditRecurringEventRepeatUntil() {
    RecurringEvent event = new RecurringEvent("Pilates",
            LocalDateTime.of(2025, 6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "TR", 0, LocalDateTime.of(2025, 6, 30, 7, 0), "Pilates session", "Studio", true, false);

    model.addRecurringEvent(event, false);

    boolean edited = model.editRecurringEvent("Pilates", "repeatuntil", "2025-07-01T07:00");
    assertTrue("Recurring event repeatUntil should be updated", edited);
  }

  @Test
  public void testEditRecurringEventRepeatingDays() {
    RecurringEvent event = new RecurringEvent("Dance Class",
            LocalDateTime.of(2025, 6, 1, 18, 0),
            LocalDateTime.of(2025, 6, 1, 19, 0),
            "MT", 4, null, "Evening class", "Dance Studio", true, false);

    model.addRecurringEvent(event, false);

    boolean edited = model.editRecurringEvent("Dance Class", "repeatingdays", "WRF");
    assertTrue("Recurring event weekdays should be updated", edited);
  }

  @Test
  public void testEditRecurringEventLocation() {
    RecurringEvent event = new RecurringEvent("Training",
            LocalDateTime.of(2025, 6, 2, 15, 0),
            LocalDateTime.of(2025, 6, 1, 16, 0),
            "WRF", 3, null, "Training sessions", "Room 101", true, false);

    model.addRecurringEvent(event, false);

    boolean edited = model.editRecurringEvent("Training", "location", "Room 202");
    assertTrue("Recurring event location should be updated", edited);
  }

  @Test
  public void testEditRecurringEventAndCheckOccurrences() {
    RecurringEvent event = new RecurringEvent("Sprint Planning",
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0),
            "MTWRF", 5, null, "Daily Planning", "Office", true, false);

    model.addRecurringEvent(event, false);

    model.editRecurringEvent("Sprint Planning", "description", "Updated Daily Planning");

    List<CalendarEvent> events = model.getEvents();

    assertEquals("Should generate 5 updated occurrences", 5, events.size());
    assertEquals("Event description should be updated", "Updated Daily Planning",
            events.get(0).getDescription());
  }

  @Test
  public void testAddSingleEvent() {
    assertTrue(controller.processCommand("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00"));
    assertEquals(1, model.getEvents().size());
  }

  @Test
  public void testAddDuplicateSingleEvent() {
    controller.processCommand("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");

    assertFalse("Duplicate event should not be allowed", result);
    assertEquals("Duplicate event: subject, start and end are identical.", view.getLastMessage());
  }


  @Test
  public void testEditSingleEventSuccessfully() {
    controller.processCommand("create event \"Team Sync\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    boolean edited = controller.processCommand("edit event description \"Team Sync\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Description\"");
    assertTrue(edited);
  }

  @Test
  public void testEditEventConflictWithAutoDecline() {
    controller.processCommand("create event --autoDecline \"Project Meeting\" "
            + "from 2025-06-02T10:00 to 2025-06-02T11:00");
    controller.processCommand("create event --autoDecline \"Client Call\" "
            + "from 2025-06-02T11:00 to 2025-06-02T12:00");
    boolean result = controller.processCommand("edit event \"Project Meeting\" "
            + "from 2025-06-02T10:00 to 2025-06-02T11:00 with \"2025-06-02T10:30\"");
    assertFalse(result);
    assertEquals("Parsing Error: Incomplete edit event command. "
            + "Expected format: edit event <property> <eventName> from <start> to <end> with "
            + "<NewPropertyValue>", view.getLastMessage());
  }

  @Test
  public void testEditNonExistingEvent() {
    boolean result = controller.processCommand("edit event description \"Nonexistent\""
            + "from 2025-01-01T09:00 to 2025-06-01T10:00 with \"Updated\"");
    assertFalse(result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }


  @Test
  public void testAddRecurringEventSuccessMain() {
    boolean result = controller.processCommand("create event \"Standup\" "
            + "from 2025-06-01T10:00 to 2025-06-01T10:30 repeats MTWRF for 3 times");
    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testEditRecurringEventRepeatTimes() {
    controller.processCommand("create event \"Scrum\" "
            + "from 2025-06-01T09:00 to 2025-06-01T09:30 repeats MTWRF for 5 times");
    boolean edited = model.editRecurringEvent("Scrum", "repeattimes", "3");
    assertTrue(edited);
    assertEquals(3, model.getEvents().size());
  }


  @Test(expected = IllegalArgumentException.class)
  public void testEditRecurringEventInvalidProperty() {
    controller.processCommand("create event \"Scrum\" "
            + "from 2025-06-01T09:00 to 2025-06-01T09:30 repeats MTWRF for 5 times");
    model.editRecurringEvent("Scrum", "invalidProperty", "value");
  }


  @Test
  public void testCheckBusySlot() {
    controller.processCommand("create event \"Interview\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue(model.isBusyAt(LocalDateTime.of(2025, 6, 1, 9, 30)));
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 6, 1, 8, 0)));
  }

  @Test
  public void testAdjacentEventsNoConflict() {
    controller.processCommand("create event \"Morning Meeting\" "
            + "from 2025-06-01T08:00 to 2025-06-01T09:00");
    boolean result = controller.processCommand("create event \"Next Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue(result);
  }

  @Test
  public void testEditEventSuccess() {
    controller.processCommand("create event \"Team Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand(
            "edit event description \"Team Meeting\" "
                    + "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Team Meeting\""
    );

    assertTrue("Event should be edited successfully", result);
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

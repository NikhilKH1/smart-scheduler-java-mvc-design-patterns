import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarModel;
import calendarapp.model.commands.Command;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * JUnit tests for the CalendarController class.
 */
public class CalendarControllerTest {

  private CalendarController controller;
  private CalendarModel model;
  private TestCalendarView view;

  @Before
  public void setUp() {
    model = new CalendarModel();
    view = new TestCalendarView();
    CommandParser parser;
    parser = new CommandParser(model);
    controller = new CalendarController(model, view, parser);
  }

  @Test
  public void testProcessCreateEvent() {
    String command = "create event \"Team Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertTrue("Processing create event should return true", result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventWithConflict() {
    String command1 =
            "create event --autoDecline \"Meeting A\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    String command2 =
            "create event --autoDecline \"Meeting B\" from 2025-06-01T10:30 to 2025-06-01T11:30";

    controller.processCommand(command1);
    boolean result = controller.processCommand(command2);

    System.out.println(view.getLastMessage());
    assertFalse("Processing conflicting event should return false", result);
    assertEquals("Event creation failed due to conflict", view.getLastMessage());
  }

  @Test
  public void testProcessQueryByDate() {
    String command = "print events on 2025-06-01";

    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    model.addEvent(new SingleEvent("Meeting", start, end, "", "",
            true, false, null), false);

    assertFalse("Model should contain at least one event",
            model.getEventsOnDate(LocalDate.of(2025, 6, 1)).isEmpty());
    boolean result = controller.processCommand(command);
    assertTrue("Querying events by date should return true", result);
    assertTrue("View should contain event details",
            view.getLastMessage().contains("Displaying 1 events")
                    || view.getLastMessage().contains("Events on 2025-06-01:"));
  }

  @Test
  public void testProcessQueryEmptyDate() {
    String command = "print events on 2025-07-01";
    boolean result = controller.processCommand(command);

    assertTrue("Querying empty date should return true", result);
    assertEquals("No events found on 2025-07-01", view.getLastMessage());
  }

  @Test
  public void testProcessEditEvent() {
    String commandCreate =
            "create event \"Morning Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    controller.processCommand(commandCreate);

    String commandEdit =
            "edit event description \"Morning Meeting\" from 2025-06-01T09:00 to "
                    + "2025-06-01T10:00 with \"Updated Meeting\"";
    boolean result = controller.processCommand(commandEdit);

    assertTrue("Editing event should return true", result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessBusyQuery() {
    String commandCreate = "create event \"Work Call\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    controller.processCommand(commandCreate);

    String commandBusy = "show status on 2025-06-01T10:30";
    boolean result = controller.processCommand(commandBusy);

    assertTrue("Querying busy status should return true", result);
    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
  }


  @Test
  public void testProcessInvalidCommand() {
    String command = "invalid command example";
    boolean result = controller.processCommand(command);

    assertFalse("Processing invalid command should return false", result);
    assertEquals("Parsing Error: Unknown command: invalid", view.getLastMessage());
  }

  @Test
  public void testHandleParsingError() {
    String command = "create event from 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertFalse("Parsing error should return false", result);
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testProcessRecurringEvent() {
    String command = "create event \"Daily Standup\" from 2025-06-01T08:00 to "
            + "2025-06-01T09:00 repeats MTWRF until 2025-06-30T23:59";
    boolean result = controller.processCommand(command);

    assertTrue("Recurring event should be processed", result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessFreeQuery() {
    String command = "show status on 2025-06-01T10:00";
    boolean result = controller.processCommand(command);

    assertTrue("Querying free status should return true", result);
    assertEquals("Available at 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testProcessNullCommand() {
    boolean result = controller.processCommand(null);

    assertFalse("Null command should fail", result);
    assertEquals("Parsing Error: Command cannot be null", view.getLastMessage());
  }

  @Test
  public void testProcessOverlappingEventsQuery() {
    String command1 = "create event \"Event A\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    String command2 = "create event \"Event B\" from 2025-06-01T09:30 to 2025-06-01T10:30";

    controller.processCommand(command1);
    controller.processCommand(command2);

    assertFalse("Model should contain events", model.getEvents().isEmpty());

    boolean result = controller.processCommand("print events from "
            + "2025-06-01T09:00 to 2025-06-01T11:00");

    System.out.println("Last Message: " + view.getLastMessage());

    assertTrue("Querying overlapping events should return true", result);
    assertTrue("View should contain overlapping event details",
            view.getLastMessage().contains("Displaying")
                    || view.getLastMessage().contains("Events from"));
  }

  @Test
  public void testProcessEditNonExistentEvent() {
    String commandEdit = "edit event description \"Nonexistent Event\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Description\"";
    boolean result = controller.processCommand(commandEdit);

    assertFalse("Editing a non-existent event should return false", result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testProcessInvalidCommandStructure() {
    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertFalse("Processing invalid command structure should return false", result);
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testProcessQueryPastDate() {
    String command = "print events on 2020-06-01";
    boolean result = controller.processCommand(command);

    assertTrue("Querying past date should return true", result);
    assertEquals("No events found on 2020-06-01", view.getLastMessage());
  }

  @Test
  public void testProcessQueryFutureDate() {
    String command = "print events on 2030-12-31";
    boolean result = controller.processCommand(command);

    assertTrue("Querying future date should return true", result);
    assertEquals("No events found on 2030-12-31", view.getLastMessage());
  }


  @Test
  public void testProcessEditEventIncorrectTime() {
    String commandCreate = "create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    controller.processCommand(commandCreate);

    String commandEdit = "edit event description \"Meeting\" "
            + "from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Updated Time\"";
    boolean result = controller.processCommand(commandEdit);

    assertFalse("Editing event with incorrect time should return false", result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testProcessQueryByDateWithNoEvents() {
    String command = "print events on 2025-07-01";
    boolean result = controller.processCommand(command);

    assertTrue(result);
    assertEquals("No events found on 2025-07-01", view.getLastMessage());
  }

  @Test
  public void testProcessQueryRangeWithNoEvents() {
    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
    boolean result = controller.processCommand(command);

    assertTrue(result);
    assertEquals("No events found from 2025-06-01T08:00 to 2025-06-01T10:00",
            view.getLastMessage());
  }

  @Test
  public void testProcessBusyQueryWhenNoEventsExist() {
    String command = "show status on 2025-06-01T10:30";
    boolean result = controller.processCommand(command);

    assertTrue(result);
    assertEquals("Available at 2025-06-01T10:30", view.getLastMessage());
  }

  @Test
  public void testProcessEditNonExistentRecurringEvent() {
    String commandEdit = "edit events repeatuntil \"NonexistentEvent\" with 2025-07-31T23:59";
    boolean result = controller.processCommand(commandEdit);

    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessUnknownCommand() {
    String command = "some unknown command";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Unknown command: some", view.getLastMessage());
  }

  @Test
  public void testProcessEmptyCommand() {
    boolean result = controller.processCommand("");

    assertFalse(result);
    assertEquals("Command is null", view.getLastMessage());
  }

  @Test
  public void testCommandHandlersReturnFalse() {
    boolean result1 = controller.processCommand("edit event description"
            + " \"Fake Event\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated\"");
    boolean result2 = controller.processCommand("edit events repeatuntil"
            + " \"NonexistentEvent\" with 2025-07-31T23:59");

    assertFalse(result1);
    assertFalse(result2);
  }

  @Test
  public void testDisplayMessageCalls() {
    String command = "create event \"Test Event\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    controller.processCommand(command);

    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testDisplayErrorCalls() {
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"Nonexistent\" with 2025-07-31T23:59");

    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailure_NotFound() {
    String editCommand = "edit events repeatuntil \"NonExistentEvent\" with 2025-07-31T23:59";
    boolean result = controller.processCommand(editCommand);

    assertFalse("Editing non-existent recurring event should return false", result);
    System.out.println(view.getLastMessage());
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailure_InvalidProperty() {
    String createCommand = "create event \"Team Sync\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59";
    controller.processCommand(createCommand);

    assertFalse("Recurring event should be present", model.getEvents().isEmpty());

    String editCommand = "edit events invalidproperty \"Team Sync\" with NewValue";
    boolean result = controller.processCommand(editCommand);

    assertFalse("Editing with invalid property should return false", result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testBusyQueryCommandHandler() {
    controller.processCommand("create event \"BusyEvent\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");

    boolean result = controller.processCommand("show status on 2025-06-01T10:00");
    assertTrue("Should handle BusyQueryCommand through commandHandlers", result);
    assertEquals("Busy at 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testEditEventFromMode() {

    controller.processCommand("create event \"FromModeTest\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00 "
                    + "with \"NewLocation\""
    );

    assertTrue("EditEventCommand with FROM mode should succeed", result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testUnknownOrUnimplementedCommand() {
    Command fakeCommand = new Command() {
    };
    boolean result = controller.processCommand(String.valueOf(fakeCommand));
    System.out.println(view.getLastMessage());
    assertFalse("Unknown or unimplemented command should return false", result);
    assertTrue(view.getLastMessage().contains("Unknown command: calendarcontrollertest"));
  }

  @Test
  public void testEditEventFromMode2() {
    controller.processCommand("create event \"FromModeTest\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");
    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00"
                    + " with \"NewLocation\""
    );
    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessQueryRangeNoEvents() {
    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
    boolean result = controller.processCommand(command);
    assertTrue(result);
    assertTrue(view.getLastMessage().contains("No events found from"));
  }

  @Test
  public void testEditEventAllMode() {
    controller.processCommand("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    String editCommand = "edit events description \"Meeting\" \"Updated Description\"";
    boolean result = controller.processCommand(editCommand);
    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventSuccess() {
    controller.processCommand("create event \"WeeklyStandup\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    boolean result = controller.processCommand("edit events "
            + "repeatuntil \"WeeklyStandup\" 2025-07-31T23:59");
    assertTrue(result);
    System.out.println(view.getLastMessage());
    assertEquals("Recurring event modified successfully.", view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailure() {
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"NonExistent\" with 2025-07-31T23:59");
    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailureMain() {
    controller.processCommand("create event \"WeeklyStandup\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");

    try {
      controller.processCommand("edit events repeatuntil"
              +  " \"NonExistent\" \"2025-07-31T23:59\"");
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException e) {
      assertEquals("Recurring event not found: NonExistent", e.getMessage());
    }
  }


  @Test(expected = DateTimeParseException.class)
  public void testProcessCreateEventWithExceptionHandling() {
    String command = "create event \"FaultyEvent\" from INVALID_DATE to 2025-06-01T11:00";
    controller.processCommand(command);
  }

  @Test
  public void testProcessCreateEventMissingWeekdays() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 "
            + "to 2025-06-01T11:00 repeats";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Missing weekdays after 'repeats'",
            view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventInvalidRepeatCount() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF for -5 times";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Repeat count must be a positive number",
            view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingTimesKeyword() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF for 5";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Expected 'times' after repeat count",
            view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingForOrUntil() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Expected 'for' or 'until' after weekdays",
            view.getLastMessage());
  }

  @Test
  public void testDuplicateEventCreationFailure() {
    String command1 = "create event \"Test Event\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    String command2 = "create event \"Test Event\" from 2025-06-01T10:00 to 2025-06-01T11:00";

    boolean result1 = controller.processCommand(command1);
    boolean result2 = controller.processCommand(command2);

    assertTrue("First event creation should succeed", result1);
    assertFalse("Duplicate event creation should return false", result2);
    assertEquals("Duplicate event: subject, start and end are identical.",
            view.getLastMessage());
  }

  @Test
  public void testEditNonExistentEventFailure() {
    String command = "edit event description \"NonExistentEvent\" from 2025-06-01T10:00 "
            + "to 2025-06-01T11:00 with \"New Description\"";
    boolean result = controller.processCommand(command);

    assertFalse("Editing a non-existent event should return false", result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testInvalidCommandStructureFailure() {
    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertFalse("Invalid command structure should return false", result);
    assertTrue("Expected parsing error message",
            view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testUnknownCommandFailure() {
    String command = "unknown command test";
    boolean result = controller.processCommand(command);

    assertFalse("Unknown command should return false", result);
    assertTrue("Expected error message about unknown command",
            view.getLastMessage().contains("Unknown"));
  }

  @Test
  public void testQueryByDateCommandAlwaysTrue() {
    boolean result = controller.processCommand("print events on 2025-06-01");
    assertTrue("Query by date command should return true", result);
    assertEquals("No events found on 2025-06-01",
            view.getLastMessage());
  }

  @Test
  public void testQueryRangeCommandAlwaysTrue() {
    controller.processCommand("create event \"RangeTest\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
    boolean result = controller.processCommand("print events "
            + "from 2025-06-01T08:00 to 2025-06-01T11:00");
    assertTrue("Query range command should return true", result);
    String msg = view.getLastMessage();
    assertTrue("View message should indicate events",
            msg.contains("Events from") || msg.contains("Displaying"));
  }


  @Test
  public void testBusyQueryCommandAlwaysTrue() {
    controller.processCommand("create event \"BusyTest\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");
    boolean result = controller.processCommand("show status on 2025-06-01T10:30");
    assertTrue("Busy query command should return true", result);
    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
  }

  @Test
  public void testCreateSingleEvent() {
    controller.processCommand("create event \"Nisha\" from 2025-03-09T10:00 "
            + "to 2025-03-09T11:00");
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventName() {
    controller.processCommand("create event \"Nisha\" from 2025-03-09T10:00 "
            + "to 2025-03-09T11:00");
    controller.processCommand("edit event name \"Nisha\" from 2025-03-09T10:00 to "
            + "2025-03-09T11:00 with \"Edited Name\"");
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventStartEndDateTime() {
    controller.processCommand("create event \"Edited Name\" from 2025-03-09T10:00 "
            + "to 2025-03-09T11:00");
    controller.processCommand("edit event startdatetime \"Edited Name\" "
            + "from 2025-03-09T10:00 to 2025-03-09T11:00 with 2025-03-08T10:00");
    assertEquals("Event(s) edited successfully", view.getLastMessage());

    controller.processCommand("edit event enddatetime \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-09T11:00 with 2025-03-08T11:00");
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventDescriptionAndLocation() {
    controller.processCommand("create event \"RecurringTest\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    controller.processCommand("edit event description \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"Adding Description\"");
    assertEquals("Failed to edit event(s)", view.getLastMessage());

    controller.processCommand("edit event location \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"New Location\"");
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }


  @Test
  public void testEditRecurringEventCommandAlwaysTrue() {
    controller.processCommand("create event \"RecurringTest\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    boolean result = controller.processCommand("edit events repeatuntil"
            + " \"RecurringTest\" 2025-07-31T23:59");
    assertTrue("Editing recurring event should return true", result);
    assertEquals("Recurring event modified successfully.", view.getLastMessage());
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

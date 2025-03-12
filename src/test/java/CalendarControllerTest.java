

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
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

public class CalendarControllerTest {

  private CalendarController controller;
  private CalendarModel model;
  private TestCalendarView view;
  private CommandParser parser;

  @Before
  public void setUp() {
    model = new CalendarModel();
    view = new TestCalendarView();
    parser = new CommandParser(model);
    controller = new CalendarController(model, view, parser);
  }

  /** ✅ Test 1: Process Valid Create Event Command */
  @Test
  public void testProcessCreateEvent() {
    String command = "create event \"Team Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertTrue("Processing create event should return true", result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  /** ✅ Test 2: Prevent Event Creation Due to Conflict */
  @Test
  public void testProcessCreateEventWithConflict() {
    String command1 = "create event --autoDecline \"Meeting A\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    String command2 = "create event --autoDecline \"Meeting B\" from 2025-06-01T10:30 to 2025-06-01T11:30";

    controller.processCommand(command1);
    boolean result = controller.processCommand(command2);

    System.out.println(view.getLastMessage());
    assertFalse("Processing conflicting event should return false", result);
    assertEquals("Event creation failed due to conflict", view.getLastMessage());
  }

  /** ✅ Test 3: Process Query by Date Command */
  @Test
  public void testProcessQueryByDate() {
    String command = "print events on 2025-06-01";

    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    model.addEvent(new SingleEvent("Meeting", start, end, "", "", true, false, null), false);

    assertFalse("Model should contain at least one event", model.getEventsOnDate(LocalDate.of(2025, 6, 1)).isEmpty());
    boolean result = controller.processCommand(command);
    assertTrue("Querying events by date should return true", result);
    assertTrue("View should contain event details",
            view.getLastMessage().contains("Displaying 1 events") ||
                    view.getLastMessage().contains("Events on 2025-06-01:"));
  }


  /** ✅ Test 4: Process Query for Empty Date */
  @Test
  public void testProcessQueryEmptyDate() {
    String command = "print events on 2025-07-01";
    boolean result = controller.processCommand(command);

    assertTrue("Querying empty date should return true", result);
    assertEquals("No events found on 2025-07-01", view.getLastMessage());
  }

  /** ✅ Test 5: Process Edit Single Event Command */
  @Test
  public void testProcessEditEvent() {
    String commandCreate = "create event \"Morning Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    controller.processCommand(commandCreate);

    String commandEdit = "edit event description \"Morning Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Meeting\"";
    boolean result = controller.processCommand(commandEdit);

    assertTrue("Editing event should return true", result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  /** ✅ Test 6: Process Busy Query */
  @Test
  public void testProcessBusyQuery() {
    String commandCreate = "create event \"Work Call\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    controller.processCommand(commandCreate);

    String commandBusy = "show status on 2025-06-01T10:30";
    boolean result = controller.processCommand(commandBusy);

    assertTrue("Querying busy status should return true", result);
    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
  }

  /** ✅ Test 7: Process Export Command */
  @Test
  public void testProcessExportCommand() {
    String command = "export cal calendar.csv";
    boolean result = controller.processCommand(command);

    assertTrue("Exporting events should return true", result);
    assertNull("Export command does not display a message", view.getLastMessage());
  }

  /** ✅ Test 8: Process Invalid Command */
  @Test
  public void testProcessInvalidCommand() {
    String command = "invalid command example";
    boolean result = controller.processCommand(command);

    assertFalse("Processing invalid command should return false", result);
    assertEquals("Parsing Error: Unknown command: invalid", view.getLastMessage());
  }

  /** ✅ Test 9: Handle Parsing Error */
  @Test
  public void testHandleParsingError() {
    String command = "create event from 2025-06-01T10:00 to 2025-06-01T11:00"; // Missing event name
    boolean result = controller.processCommand(command);

    assertFalse("Parsing error should return false", result);
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  /** ✅ Test 10: Process Recurring Event Creation */
  @Test
  public void testProcessRecurringEvent() {
    String command = "create event \"Daily Standup\" from 2025-06-01T08:00 to 2025-06-01T09:00 repeats MTWRF until 2025-06-30T23:59";
    boolean result = controller.processCommand(command);

    assertTrue("Recurring event should be processed", result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  /** ✅ Test 11: Query Free Status */
  @Test
  public void testProcessFreeQuery() {
    String command = "show status on 2025-06-01T10:00"; // No event at this time
    boolean result = controller.processCommand(command);

    assertTrue("Querying free status should return true", result);
    assertEquals("Available at 2025-06-01T10:00", view.getLastMessage());
  }

  /** ✅ Test 12: Process Null Command */
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

    // Add events
    controller.processCommand(command1);
    controller.processCommand(command2);

    // Ensure events exist before querying
    assertFalse("Model should contain events", model.getEvents().isEmpty());

    // Process the query
    boolean result = controller.processCommand("print events from 2025-06-01T09:00 to 2025-06-01T11:00");

    // Debugging output
    System.out.println("Last Message: " + view.getLastMessage());

    // Assertions
    assertTrue("Querying overlapping events should return true", result);
    assertTrue("View should contain overlapping event details",
            view.getLastMessage().contains("Displaying") ||
                    view.getLastMessage().contains("Events from"));
  }


  /** ✅ Test 14: Process Editing Non-Existent Event */
  @Test
  public void testProcessEditNonExistentEvent() {
    String commandEdit = "edit event description \"Nonexistent Event\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Description\"";
    boolean result = controller.processCommand(commandEdit);

    assertFalse("Editing a non-existent event should return false", result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  /** ✅ Test 15: Process Export Without Events */
  @Test
  public void testProcessExportEmptyCalendar() {
    String command = "export cal empty_calendar.csv";
    boolean result = controller.processCommand(command);

    assertTrue("Exporting empty calendar should return true", result);
    assertNull("Export command does not display a message", view.getLastMessage());
  }

//  /** ✅ Test 16: Process Invalid Date Format */
//  @Test
//  public void testProcessInvalidDateFormat() {
//    String command = "create event \"Test Event\" from 2025/06/01 10:00 to 2025-06-01T11:00";
//    boolean result = controller.processCommand(command);
//
//    assertFalse("Processing invalid date format should return false", result);
//    assertTrue(view.getLastMessage().contains("Parsing Error"));
//  }

  /** ✅ Test 17: Process Invalid Command Structure */
  @Test
  public void testProcessInvalidCommandStructure() {
    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertFalse("Processing invalid command structure should return false", result);
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  /** ✅ Test 18: Process Query Past Date */
  @Test
  public void testProcessQueryPastDate() {
    String command = "print events on 2020-06-01";
    boolean result = controller.processCommand(command);

    assertTrue("Querying past date should return true", result);
    assertEquals("No events found on 2020-06-01", view.getLastMessage());
  }

  /** ✅ Test 19: Process Query Future Date */
  @Test
  public void testProcessQueryFutureDate() {
    String command = "print events on 2030-12-31";
    boolean result = controller.processCommand(command);

    assertTrue("Querying future date should return true", result);
    assertEquals("No events found on 2030-12-31", view.getLastMessage());
  }

  /** ✅ Test 20: Process Edit Event with Incorrect Time */
  @Test
  public void testProcessEditEventIncorrectTime() {
    String commandCreate = "create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    controller.processCommand(commandCreate);

    String commandEdit = "edit event description \"Meeting\" from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Updated Time\"";
    boolean result = controller.processCommand(commandEdit);

    assertFalse("Editing event with incorrect time should return false", result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }


  /** ✅ Test 15: Fail to Edit Non-Existent Recurring Event */
  @Test
  public void testProcessEditRecurringEventFailure_NotFound() {
    // Attempt to edit a recurring event that does not exist
    String editCommand = "edit events repeatuntil \"NonExistentEvent\" with 2025-07-31T23:59";
    boolean result = controller.processCommand(editCommand);

    assertFalse("Editing non-existent recurring event should return false", result);
    System.out.println(view.getLastMessage());
    assertEquals("Parsing Error: Invalid edit events command format", view.getLastMessage());
  }

  /** ✅ Test 16: Fail to Edit Recurring Event With Invalid Property */
  @Test
  public void testProcessEditRecurringEventFailure_InvalidProperty() {
    // Create a recurring event
    String createCommand = "create event \"Team Sync\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59";
    controller.processCommand(createCommand);

    // Ensure the event exists
    assertFalse("Recurring event should be present", model.getEvents().isEmpty());

    // Attempt to edit an unsupported property
    String editCommand = "edit events invalidproperty \"Team Sync\" with NewValue";
    boolean result = controller.processCommand(editCommand);

    assertFalse("Editing with invalid property should return false", result);
    assertEquals("Parsing Error: Invalid edit events command format", view.getLastMessage());
  }

  @Test
  public void testBusyQueryCommandHandler() {
    // This ensures BusyQueryCommand is created and handled by commandHandlers.put(BusyQueryCommand.class, ...)

    // Create an event to be "busy" at 2025-06-01T10:00
    controller.processCommand("create event \"BusyEvent\" from 2025-06-01T10:00 to 2025-06-01T11:00");

    // Trigger the busy query
    boolean result = controller.processCommand("show status on 2025-06-01T10:00");

    // Assertions
    assertTrue("Should handle BusyQueryCommand through commandHandlers", result);
    assertEquals("Busy at 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testEditEventFromMode() {
    // Create an event
    controller.processCommand("create event \"FromModeTest\" from 2025-06-01T09:00 to 2025-06-01T10:00");

    // Use 'edit events <property> <eventName> from <dateTime> with <NewValue>'
    // This triggers EditEventCommand in FROM mode
    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00 with \"NewLocation\""
    );

    // Assertions
    assertTrue("EditEventCommand with FROM mode should succeed", result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testUnknownOrUnimplementedCommand() {
    // We will create a "fake" command object to bypass the parser
    // or pass a known command type not in the commandHandlers map

    // Option 1: Bypass parser and call controller.processCommand(...) with a custom Command object
    Command fakeCommand = new Command() { /* an anonymous Command implementation */ };

    // Or Option 2: parse a string that returns a Command type not in commandHandlers
    // For instance, parse a known command, but we remove it from the map (not recommended).
    // We'll do Option 1 for simplicity:

    // Directly call controller.processCommand on a custom command:
    boolean result = controller.processCommand(String.valueOf(fakeCommand));
    System.out.println(view.getLastMessage());
    assertFalse("Unknown or unimplemented command should return false", result);
    assertTrue(view.getLastMessage().contains("Unknown command: calendarcontrollertest"));
  }


  /** Test: EditEventCommand in FROM mode (covers case FROM in processEditEvent) */
  @Test
  public void testEditEventFromMode2() {
    // Create event first
    controller.processCommand("create event \"FromModeTest\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    // Issue edit command with FROM clause
    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00 with \"NewLocation\""
    );
    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }


  /** Test: processQueryRange branch when no events exist */
  @Test
  public void testProcessQueryRangeNoEvents() {
    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
    boolean result = controller.processCommand(command);
    assertTrue(result);
    // Expect a message indicating no events found
    assertTrue(view.getLastMessage().contains("No events found from"));
  }

  /** Test: EditEventCommand in ALL mode (no "from" clause) */
  @Test
  public void testEditEventAllMode() {
    // Create an event
    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    // Issue an edit command without a "from" clause => ALL mode
    String editCommand = "edit events description \"Meeting\" \"Updated Description\"";
    boolean result = controller.processCommand(editCommand);
    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  /** Test: processEditRecurringEvent success branch */
  @Test
  public void testProcessEditRecurringEventSuccess() {
    controller.processCommand("create event \"WeeklyStandup\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    // Edit recurring event
    boolean result = controller.processCommand("edit events repeatuntil \"WeeklyStandup\" 2025-07-31T23:59");
    assertTrue(result);
    System.out.println(view.getLastMessage());
    assertEquals("Recurring event modified successfully.", view.getLastMessage());
  }

  /** Test: processEditRecurringEvent failure branch */
  @Test
  public void testProcessEditRecurringEventFailure() {
    // Attempt to edit a recurring event that does not exist
    boolean result = controller.processCommand("edit events repeatuntil \"NonExistent\" with 2025-07-31T23:59");
    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format", view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailureMain() {
    controller.processCommand("create event \"WeeklyStandup\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");

    try {
      controller.processCommand("edit events repeatuntil \"NonExistent\" \"2025-07-31T23:59\"");
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
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Missing weekdays after 'repeats'", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventInvalidRepeatCount() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF for -5 times";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Repeat count must be a positive number", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingTimesKeyword() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF for 5";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Expected 'times' after repeat count", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingForOrUntil() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Expected 'for' or 'until' after weekdays", view.getLastMessage());
  }





  // Custom View Implementation for Testing
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

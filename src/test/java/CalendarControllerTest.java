//import calendarapp.controller.CalendarController;
//import calendarapp.controller.CommandParser;
//import calendarapp.model.CalendarModel;
//import calendarapp.controller.commands.Command;
//import calendarapp.model.event.CalendarEvent;
//import calendarapp.model.event.SingleEvent;
//import calendarapp.view.ICalendarView;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeParseException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//
///**
// * JUnit tests for the CalendarController class.
// */
//public class CalendarControllerTest {
//
//  private CalendarController controller;
//  private CalendarModel model;
//  private TestCalendarView view;
//
//  @Before
//  public void setUp() {
//    model = new CalendarModel();
//    view = new TestCalendarView();
//    CommandParser parser;
//    parser = new CommandParser(model);
//    controller = new CalendarController(model, view, parser);
//  }
//

//
//  @Test
//  public void testProcessEditEventIncorrectTime() {
//    String commandCreate = "create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
//    controller.processCommand(commandCreate);
//
//    String commandEdit = "edit event description \"Meeting\" "
//            + "from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Updated Time\"";
//    boolean result = controller.processCommand(commandEdit);
//
//    assertFalse("Editing event with incorrect time should return false", result);
//    assertEquals("Failed to edit event(s)", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessQueryByDateWithNoEvents() {
//    String command = "print events on 2025-07-01";
//    boolean result = controller.processCommand(command);
//
//    assertTrue(result);
//    assertEquals("No events found on 2025-07-01", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessQueryRangeWithNoEvents() {
//    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
//    boolean result = controller.processCommand(command);
//
//    assertTrue(result);
//    assertEquals("No events found from 2025-06-01T08:00 to 2025-06-01T10:00",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessBusyQueryWhenNoEventsExist() {
//    String command = "show status on 2025-06-01T10:30";
//    boolean result = controller.processCommand(command);
//
//    assertTrue(result);
//    assertEquals("Available at 2025-06-01T10:30", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditNonExistentRecurringEvent() {
//    String commandEdit = "edit events repeatuntil \"NonexistentEvent\" with 2025-07-31T23:59";
//    boolean result = controller.processCommand(commandEdit);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Invalid edit events command format",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessUnknownCommand() {
//    String command = "some unknown command";
//    boolean result = controller.processCommand(command);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Unknown command: some", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEmptyCommand() {
//    boolean result = controller.processCommand("");
//
//    assertFalse(result);
//    assertEquals("Command parsing returned null", view.getLastMessage());
//  }
//
//  @Test
//  public void testCommandHandlersReturnFalse() {
//    boolean result1 = controller.processCommand("edit event description"
//            + " \"Fake Event\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated\"");
//    boolean result2 = controller.processCommand("edit events repeatuntil"
//            + " \"NonexistentEvent\" with 2025-07-31T23:59");
//
//    assertFalse(result1);
//    assertFalse(result2);
//  }
//
//  @Test
//  public void testDisplayMessageCalls() {
//    String command = "create event \"Test Event\" from 2025-06-01T09:00 to 2025-06-01T10:00";
//    controller.processCommand(command);
//
//    assertEquals("Event created successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testDisplayErrorCalls() {
//    boolean result = controller.processCommand("edit events repeatuntil "
//            + "\"Nonexistent\" with 2025-07-31T23:59");
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Invalid edit events command format",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditRecurringEventFailure_NotFound() {
//    String editCommand = "edit events repeatuntil \"NonExistentEvent\" with 2025-07-31T23:59";
//    boolean result = controller.processCommand(editCommand);
//
//    assertFalse("Editing non-existent recurring event should return false", result);
//    System.out.println(view.getLastMessage());
//    assertEquals("Parsing Error: Invalid edit events command format",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditRecurringEventFailure_InvalidProperty() {
//    String createCommand = "create event \"Team Sync\" from 2025-06-01T10:00 to "
//            + "2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59";
//    controller.processCommand(createCommand);
//
//    assertFalse("Recurring event should be present", model.getEvents().isEmpty());
//
//    String editCommand = "edit events invalidproperty \"Team Sync\" with NewValue";
//    boolean result = controller.processCommand(editCommand);
//
//    assertFalse("Editing with invalid property should return false", result);
//    assertEquals("Parsing Error: Invalid edit events command format",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testBusyQueryCommandHandler() {
//    controller.processCommand("create event \"BusyEvent\" from "
//            + "2025-06-01T10:00 to 2025-06-01T11:00");
//
//    boolean result = controller.processCommand("show status on 2025-06-01T10:00");
//    assertTrue("Should handle BusyQueryCommand through commandHandlers", result);
//    assertEquals("Busy at 2025-06-01T10:00", view.getLastMessage());
//  }
//
//  @Test
//  public void testEditEventFromMode() {
//
//    controller.processCommand("create event \"FromModeTest\" from "
//            + "2025-06-01T09:00 to 2025-06-01T10:00");
//
//    boolean result = controller.processCommand(
//            "edit events location \"FromModeTest\" from 2025-06-01T09:00 "
//                    + "with \"NewLocation\""
//    );
//
//    assertTrue("EditEventCommand with FROM mode should succeed", result);
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//
//  @Test
//  public void testEditEventFromMode2() {
//    controller.processCommand("create event \"FromModeTest\" from "
//            + "2025-06-01T09:00 to 2025-06-01T10:00");
//    boolean result = controller.processCommand(
//            "edit events location \"FromModeTest\" from 2025-06-01T09:00"
//                    + " with \"NewLocation\""
//    );
//    assertTrue(result);
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessQueryRangeNoEvents() {
//    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
//    boolean result = controller.processCommand(command);
//    assertTrue(result);
//    assertTrue(view.getLastMessage().contains("No events found from"));
//  }
//
//  @Test
//  public void testEditEventAllMode() {
//    controller.processCommand("create event \"Meeting\" "
//            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
//    String editCommand = "edit events description \"Meeting\" \"Updated Description\"";
//    boolean result = controller.processCommand(editCommand);
//    assertTrue(result);
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditRecurringEventSuccess() {
//    controller.processCommand("create event \"WeeklyStandup\" "
//            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
//    boolean result = controller.processCommand("edit events "
//            + "repeatuntil \"WeeklyStandup\" 2025-07-31T23:59");
//    assertTrue(result);
//    System.out.println(view.getLastMessage());
//    assertEquals("Recurring event modified successfully.", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditRecurringEventFailure() {
//    boolean result = controller.processCommand("edit events repeatuntil "
//            + "\"NonExistent\" with 2025-07-31T23:59");
//    assertFalse(result);
//    assertEquals("Parsing Error: Invalid edit events command format",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEditRecurringEventFailureMain() {
//    boolean result = controller.processCommand("create event \"WeeklyStandup\" "
//            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
//    assertTrue(result);
//  }
//
//
//  @Test
//  public void testProcessCreateEventWithInvalidDateInput() {
//    String command = "create event \"FaultyEvent\" from INVALID_DATE to 2025-06-01T11:00";
//
//    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//    System.setErr(new PrintStream(errContent));
//
//    boolean result = controller.processCommand(command);
//
//    assertFalse("Expected command to fail due to invalid date", result);
//  }
//
//
//  @Test
//  public void testProcessCreateEventMissingWeekdays() {
//    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 "
//            + "to 2025-06-01T11:00 repeats";
//    boolean result = controller.processCommand(command);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Missing weekdays after 'repeats'",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessCreateEventInvalidRepeatCount() {
//    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
//            + "2025-06-01T11:00 repeats MTWRF for -5 times";
//    boolean result = controller.processCommand(command);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Repeat count must be a positive number",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessCreateEventMissingTimesKeyword() {
//    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
//            + "2025-06-01T11:00 repeats MTWRF for 5";
//    boolean result = controller.processCommand(command);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Expected 'times' after repeat count",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessCreateEventMissingForOrUntil() {
//    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
//            + "2025-06-01T11:00 repeats MTWRF";
//    boolean result = controller.processCommand(command);
//
//    assertFalse(result);
//    assertEquals("Parsing Error: Expected 'for' or 'until' after weekdays",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testDuplicateEventCreationFailure() {
//    String command1 = "create event \"Test Event\" from 2025-06-01T10:00 to 2025-06-01T11:00";
//    String command2 = "create event \"Test Event\" from 2025-06-01T10:00 to 2025-06-01T11:00";
//
//    boolean result1 = controller.processCommand(command1);
//    boolean result2 = controller.processCommand(command2);
//
//    assertTrue("First event creation should succeed", result1);
//    assertFalse("Duplicate event creation should return false", result2);
//    assertEquals("Duplicate event: subject, start and end are identical.",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testEditNonExistentEventFailure() {
//    String command = "edit event description \"NonExistentEvent\" from 2025-06-01T10:00 "
//            + "to 2025-06-01T11:00 with \"New Description\"";
//    boolean result = controller.processCommand(command);
//
//    assertFalse("Editing a non-existent event should return false", result);
//    assertEquals("Failed to edit event(s)", view.getLastMessage());
//  }
//
//  @Test
//  public void testInvalidCommandStructureFailure() {
//    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
//    boolean result = controller.processCommand(command);
//
//    assertFalse("Invalid command structure should return false", result);
//    assertTrue("Expected parsing error message",
//            view.getLastMessage().contains("Parsing Error"));
//  }
//
//  @Test
//  public void testUnknownCommandFailure() {
//    String command = "unknown command test";
//    boolean result = controller.processCommand(command);
//
//    assertFalse("Unknown command should return false", result);
//    assertTrue("Expected error message about unknown command",
//            view.getLastMessage().contains("Unknown"));
//  }
//
//  @Test
//  public void testQueryByDateCommandAlwaysTrue() {
//    boolean result = controller.processCommand("print events on 2025-06-01");
//    assertTrue("Query by date command should return true", result);
//    assertEquals("No events found on 2025-06-01",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testQueryRangeCommandAlwaysTrue() {
//    controller.processCommand("create event \"RangeTest\" "
//            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
//    boolean result = controller.processCommand("print events "
//            + "from 2025-06-01T08:00 to 2025-06-01T11:00");
//    assertTrue("Query range command should return true", result);
//    String msg = view.getLastMessage();
//    assertTrue("View message should indicate events",
//            msg.contains("Events from") || msg.contains("Displaying"));
//  }
//
//
//  @Test
//  public void testBusyQueryCommandAlwaysTrue() {
//    controller.processCommand("create event \"BusyTest\" from "
//            + "2025-06-01T10:00 to 2025-06-01T11:00");
//    boolean result = controller.processCommand("show status on 2025-06-01T10:30");
//    assertTrue("Busy query command should return true", result);
//    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
//  }
//
//  @Test
//  public void testCreateSingleEvent() {
//    controller.processCommand("create event \"Nisha\" from 2025-03-09T10:00 "
//            + "to 2025-03-09T11:00");
//    assertEquals("Event created successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testEditEventName() {
//    controller.processCommand("create event \"Nisha\" from 2025-03-09T10:00 "
//            + "to 2025-03-09T11:00");
//    controller.processCommand("edit event name \"Nisha\" from 2025-03-09T10:00 to "
//            + "2025-03-09T11:00 with \"Edited Name\"");
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testEditEventStartEndDateTime() {
//    controller.processCommand("create event \"Edited Name\" from 2025-03-09T10:00 "
//            + "to 2025-03-09T11:00");
//    controller.processCommand("edit event startdatetime \"Edited Name\" "
//            + "from 2025-03-09T10:00 to 2025-03-09T11:00 with 2025-03-08T10:00");
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//
//    controller.processCommand("edit event enddatetime \"Edited Name\" from "
//            + "2025-03-08T10:00 to 2025-03-09T11:00 with 2025-03-08T11:00");
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testEditEventDescriptionAndLocation() {
//    controller.processCommand("create event \"RecurringTest\" "
//            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
//    controller.processCommand("edit event description \"Edited Name\" from "
//            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"Adding Description\"");
//    assertEquals("Failed to edit event(s)", view.getLastMessage());
//
//    controller.processCommand("edit event location \"Edited Name\" from "
//            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"New Location\"");
//    assertEquals("Failed to edit event(s)", view.getLastMessage());
//  }
//
//
//  @Test
//  public void testEditRecurringEventCommandAlwaysTrue() {
//    controller.processCommand("create event \"RecurringTest\" "
//            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
//    boolean result = controller.processCommand("edit events repeatuntil"
//            + " \"RecurringTest\" 2025-07-31T23:59");
//    assertTrue("Editing recurring event should return true", result);
//    assertEquals("Recurring event modified successfully.", view.getLastMessage());
//  }
//
//  private static class TestCalendarView implements ICalendarView {
//    private final List<String> messages = new ArrayList<>();
//
//    @Override
//    public void displayMessage(String message) {
//      messages.add(message);
//    }
//
//    @Override
//    public void displayError(String error) {
//      messages.add(error);
//    }
//
//    @Override
//    public void displayEvents(List<CalendarEvent> events) {
//      messages.add("Displaying " + events.size() + " events");
//    }
//
//    public String getLastMessage() {
//      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
//    }
//  }
//}

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CalendarControllerTest {

  private CalendarController controller;
  private CalendarManager manager;
  private TestView view;

  @Before
  public void setUp() {
    manager = new CalendarManager();
    view = new TestView();
    view.clearMessages();
    CommandParser parser = new CommandParser(manager);
    controller = new CalendarController(manager, view, parser);

    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");
  }

  @After
  public void tearDown() {
    view.clearMessages();
  }


  @Test
  public void testProcessCreateEvent() {
    String command = "create event \"Team Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventWithConflict() {
    controller.processCommand("create event --autoDecline \"Meeting A\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00");
    boolean result = controller.processCommand("create event --autoDecline "
            + "\"Meeting B\" from 2025-06-01T10:30 to 2025-06-01T11:30");

    assertFalse(result);
    assertEquals("No events found in calendar default", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventWithoutConflict() {
    controller.processCommand("create event \"Meeting A\" from 2025-06-01T08:00 "
            + "to 2025-06-01T09:00");
    boolean result = controller.processCommand("create event \"Meeting B\" "
            + "from 2025-06-01T09:15 to 2025-06-01T10:15");

    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessQueryByDate() {
    controller.processCommand("create event \"Check-in\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand("print events on 2025-06-01");

    assertTrue(result);
    assertTrue(view.getLastMessage().contains("Events on 2025-06-01:")
            || view.getLastMessage().contains("Displaying 1 events"));
  }

  @Test
  public void testProcessQueryEmptyDate() {
    boolean result = controller.processCommand("print events on 2025-12-31");

    assertTrue(result);
    assertEquals("No events found on 2025-12-31", view.getLastMessage());
  }

  @Test
  public void testProcessEditEvent() {
    controller.processCommand("create event \"Morning Meeting\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand("edit event description "
            + "\"Morning Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Meeting\"");

    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessBusyQuery() {
    controller.processCommand("create event \"Work Call\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");

    boolean result = controller.processCommand("show status on 2025-06-01T10:30");

    assertTrue(result);
    assertEquals("Busy at 2025-06-01T10:30Z[UTC]", view.getLastMessage());
  }


  @Test
  public void testProcessRecurringEvent() {
    boolean result = controller.processCommand("create event \"Daily Standup\" "
            + "from 2025-06-01T08:00 to 2025-06-01T09:00 repeats MTWRF until 2025-06-30T23:59");

    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessFreeQuery() {
    boolean result = controller.processCommand("show status on 2025-06-01T10:00");

    assertTrue(result);
    assertEquals("Available at 2025-06-01T10:00Z[UTC]", view.getLastMessage());
  }

  @Test
  public void testProcessNullCommand() {
    boolean result = controller.processCommand(null);

    assertFalse(result);
    assertEquals("Parsing Error: Command cannot be null or empty", view.getLastError());
  }

  @Test
  public void testProcessOverlappingEventsQuery() {
    controller.processCommand("create event \"Event A\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    controller.processCommand("create event \"Event B\" from 2025-06-01T09:30 to 2025-06-01T10:30");

    boolean result = controller.processCommand("print events from 2025-06-01T09:00 to 2025-06-01T11:00");

    assertTrue(result);
    assertTrue(view.getLastMessage().contains("Displaying")
            || view.getLastMessage().contains("Events from")
            || view.getLastMessage().contains("Events on"));
  }

  @Test
  public void testProcessEditNonExistentEvent() {
    String commandEdit = "edit event description \"Nonexistent Event\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Description\"";
    boolean result = controller.processCommand(commandEdit);

    assertFalse(result);
    assertEquals("No events found in calendar default", view.getLastMessage());
  }

  @Test
  public void testProcessInvalidCommandStructure() {
    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);
    assertFalse(result);
    assertEquals("No events found in calendar default", view.getLastMessage());
  }

  @Test
  public void testProcessQueryPastDate() {
    boolean result = controller.processCommand("print events on 2020-06-01");

    assertTrue(result);
    assertEquals("No events found on 2020-06-01", view.getLastMessage());
  }

  @Test
  public void testProcessQueryFutureDate() {
    boolean result = controller.processCommand("print events on 2030-12-31");

    assertTrue(result);
    assertEquals("No events found on 2030-12-31", view.getLastMessage());
  }

  @Test
  public void testProcessEditEventIncorrectTime() {
    controller.processCommand("create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00");
    boolean result = controller.processCommand("edit event description \"Meeting\" " +
            "from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Updated Time\"");

    assertFalse(result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testProcessQueryByDateWithNoEvents() {
    boolean result = controller.processCommand("print events on 2025-07-01");
    assertTrue(result);
    assertEquals("No events found on 2025-07-01", view.getLastMessage());
  }

  @Test
  public void testProcessQueryRangeWithNoEvents() {
    boolean result = controller.processCommand("print events from 2025-06-01T08:00 to 2025-06-01T10:00");
    assertTrue(result);
    assertEquals("No events found from 2025-06-01T08:00 to 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testProcessBusyQueryWhenNoEventsExist() {
    boolean result = controller.processCommand("show status on 2025-06-01T10:30");
    assertTrue(result);
    assertEquals("Available at 2025-06-01T10:30Z[UTC]", view.getLastMessage());
  }

  @Test
  public void testProcessEditNonExistentRecurringEvent() {
    boolean result = controller.processCommand("edit events repeatuntil \"NonexistentEvent\" with 2025-07-31T23:59");
    assertFalse(result);
    assertEquals("Failed to edit recurring event", view.getLastMessage());
  }

  @Test
  public void testProcessUnknownCommand() {
    boolean result = controller.processCommand("some unknown command");
    assertFalse(result);
    assertEquals("Parsing Error: Unknown command: some", view.getLastMessage());
  }

  @Test
  public void testProcessEmptyCommand() {
    boolean result = controller.processCommand("");
    assertFalse(result);
    assertEquals("Parsing Error: Command cannot be null", view.getLastMessage());
  }

  @Test
  public void testCommandHandlersReturnFalse() {
    boolean result1 = controller.processCommand("edit event description \"Fake Event\" " +
            "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated\"");
    boolean result2 = controller.processCommand("edit events repeatuntil \"NonexistentEvent\" with 2025-07-31T23:59");

    assertFalse(result1);
    assertFalse(result2);
  }

  @Test
  public void testDisplayMessageCalls() {
    controller.processCommand("create event \"Test Event\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testDisplayErrorCalls() {
    boolean result = controller.processCommand("edit events repeatuntil \"Nonexistent\" with 2025-07-31T23:59");
    assertFalse(result);
    assertEquals("Failed to edit recurring event", view.getLastMessage());
  }




  private static class TestView implements ICalendarView {
    private String lastMessage;
    private String lastError;

    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayError(String errorMessage) {
      this.lastError = errorMessage;
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      if (events == null || events.isEmpty()) {
        this.lastMessage = "No events found";
      } else {
        this.lastMessage = "Displaying " + events.size() + " events";
      }
    }


    public String getLastMessage() {
      return lastMessage;
    }

    public String getLastError() {
      return lastError;
    }

    public void clearMessages() {
      this.lastMessage = null;
      this.lastError = null;
    }
  }
}


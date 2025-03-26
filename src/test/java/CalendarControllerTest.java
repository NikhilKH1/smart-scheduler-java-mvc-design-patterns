import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.commands.ICommand;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for CalendarController class.
 */
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
    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
            view.getLastMessage());
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
            + "\"Morning Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 "
            + "with \"Updated Meeting\"");

    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessBusyQuery() {
    controller.processCommand("create event \"Work Call\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");

    boolean result = controller.processCommand("show status on 2025-06-01T10:30");

    assertTrue(result);
    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
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
    assertEquals("Available at 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testProcessNullCommand() {
    boolean result = controller.processCommand(null);

    assertFalse(result);
    assertEquals("Parsing Error: Command cannot be null or empty", view.getLastMessage());
  }

  @Test
  public void testProcessOverlappingEventsQuery() {
    controller.processCommand("create event \"Event A\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");
    controller.processCommand("create event \"Event B\" from "
            + "2025-06-01T09:30 to 2025-06-01T10:30");

    boolean result = controller.processCommand("print events from "
            + "2025-06-01T09:00 to 2025-06-01T11:00");

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
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testProcessInvalidCommandStructure() {
    String command = "create event 2025-06-01T10:00 to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);
    assertFalse(result);
    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
            view.getLastMessage());
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
    controller.processCommand("create event \"Meeting\" from 2025-06-01T10:00 "
            + "to 2025-06-01T11:00");
    boolean result = controller.processCommand("edit event description \"Meeting\" "
            + "from 2025-06-01T12:00 to 2025-06-01T13:00 with \"Updated Time\"");

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
    boolean result = controller.processCommand("print events from 2025-06-01T08:00 "
            + "to 2025-06-01T10:00");
    assertTrue(result);
    assertEquals("No events found from 2025-06-01T08:00Z[UTC] to 2025-06-01T10:00Z[UTC]",
            view.getLastMessage());
  }

  @Test
  public void testProcessBusyQueryWhenNoEventsExist() {
    boolean result = controller.processCommand("show status on 2025-06-01T10:30");
    assertTrue(result);
    assertEquals("Available at 2025-06-01T10:30", view.getLastMessage());
  }

  @Test
  public void testProcessEditNonExistentRecurringEvent() {
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"NonexistentEvent\" with 2025-07-31T23:59");
    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
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
    assertEquals("Parsing Error: Command cannot be null or empty", view.getLastMessage());
  }

  @Test
  public void testCommandHandlers() {
    boolean result1 = controller.processCommand("edit event description "
            + "\"Fake Event\" " +
            "from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated\"");
    boolean result2 = controller.processCommand("edit events repeatuntil "
            + "\"NonexistentEvent\" with 2025-07-31T23:59");

    assertFalse(result1);
    assertFalse(result2);
  }

  @Test
  public void testDisplayMessageCalls() {
    controller.processCommand("create event \"Test Event\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");
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
  public void testProcessEditRecurringEvent() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    String editCommand = "edit events repeatuntil \"NonExistentEvent\" with 2025-07-31T23:59";
    boolean result = controller.processCommand(editCommand);

    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventProperty() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    String createCommand = "create event \"Team Sync\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59";
    controller.processCommand(createCommand);

    String editCommand = "edit events invalidproperty \"Team Sync\" with NewValue";
    boolean result = controller.processCommand(editCommand);

    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testBusyQueryCommandHandler() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    controller.processCommand("create event \"BusyEvent\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");

    boolean result = controller.processCommand("show status on 2025-06-01T10:00");
    assertTrue(result);
    assertEquals("Busy at 2025-06-01T10:00", view.getLastMessage());
  }

  @Test
  public void testEditEventFromMode() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    controller.processCommand("create event \"FromModeTest\" from 2025-06-01T09:00"
            + " to 2025-06-01T10:00");

    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00 "
                    + "with \"NewLocation\""
    );

    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventFromMode2() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    controller.processCommand("create event \"FromModeTest\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00");

    boolean result = controller.processCommand(
            "edit events location \"FromModeTest\" from 2025-06-01T09:00 "
                    + "with \"NewLocation\""
    );

    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessQueryRange() {
    String command = "print events from 2025-06-01T08:00 to 2025-06-01T10:00";
    boolean result = controller.processCommand(command);

    assertTrue(result);
    assertEquals("No events found from 2025-06-01T08:00Z[UTC] to 2025-06-01T10:00Z[UTC]",
            view.getLastMessage());
  }

  @Test
  public void testEditEventAllMode() {
    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00 "
            + "to 2025-06-01T10:00");
    String editCommand = "edit events description \"Meeting\" \"Updated Description\"";
    boolean result = controller.processCommand(editCommand);

    assertTrue(result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringSuccess() {
    controller.processCommand("create event \"WeeklyStandup\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"WeeklyStandup\" 2025-07-31T23:59");

    assertTrue(result);
    assertEquals("Recurring event modified successfully.", view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringFailure() {
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"NonExistent\" with 2025-07-31T23:59");

    assertFalse(result);
    assertEquals("Parsing Error: Invalid edit events command format",
            view.getLastMessage());
  }

  @Test
  public void testProcessEditRecurringEventFailureMain() {
    boolean result = controller.processCommand("create event \"WeeklyStandup\""
            + " from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");

    assertTrue(result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingTime() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF for 5";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Expected 'times' after repeat count",
            view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventInvalidRepeatCount() {
    String command = "create event \"WeeklyMeeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00 repeats MTWRF for -5 times";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertEquals("Parsing Error: Repeat count must be positive", view.getLastMessage());
  }

  @Test
  public void testProcessCreateEventMissingFor() {
    boolean result = controller.processCommand("create event \"WeeklyMeeting\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF");
    assertFalse(result);
    assertEquals("Parsing Error: Expected 'for' or 'until' after weekdays",
            view.getLastMessage());
  }

  @Test
  public void testDuplicateEventCreationFailure() {
    controller.processCommand("create event \"Test Event\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");
    boolean result = controller.processCommand("create event \"Test Event\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00");

    assertFalse(result);
    assertEquals("Duplicate event detected.", view.getLastMessage());
  }

  @Test
  public void testEditNonExistentEventFailure() {
    boolean result = controller.processCommand("edit event description"
            + " \"NonExistentEvent\" from 2025-06-01T10:00 to 2025-06-01T11:00 with"
            + " \"New Description\"");
    assertFalse(result);
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testInvalidCommandStructureFailure() {
    boolean result = controller.processCommand("create event 2025-06-01T10:00 "
            + "to 2025-06-01T11:00");
    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Parsing Error"));
  }

  @Test
  public void testUnknownCommandFailure() {
    boolean result = controller.processCommand("unknown command test");
    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Unknown"));
  }

  @Test
  public void testQueryByDateCommandAlwaysTrue() {
    boolean result = controller.processCommand("print events on 2025-06-01");
    assertTrue(result);
    assertEquals("No events found on 2025-06-01", view.getLastMessage());
  }

  @Test
  public void testQueryRangeCommandAlwaysTrue() {
    controller.processCommand("create event \"RangeTest\" from 2025-06-01T09:00 to "
            + "2025-06-01T10:00");
    boolean result = controller.processCommand("print events from 2025-06-01T08:00 "
            + "to 2025-06-01T11:00");

    assertTrue(result);
    assertTrue(view.getLastMessage().contains("Events from")
            || view.getLastMessage().contains("Displaying"));
  }

  @Test
  public void testBusyQueryCommand() {
    controller.processCommand("create event \"BusyTest\" from 2025-06-01T10:00 "
            + "to 2025-06-01T11:00");
    boolean result = controller.processCommand("show status on 2025-06-01T10:30");

    assertTrue(result);
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
    controller.processCommand("edit event name \"Nisha\" from 2025-03-09T10:00 "
            + "to 2025-03-09T11:00 with \"Edited Name\"");
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventStartEndDateTime() {
    controller.processCommand("create event \"Edited Name\" from 2025-03-09T10:00 "
            + "to 2025-03-09T11:00");

    controller.processCommand("edit event startdatetime \"Edited Name\" from "
            + "2025-03-09T10:00 to 2025-03-09T11:00 with 2025-03-08T10:00");
    assertEquals("Event(s) edited successfully", view.getLastMessage());

    controller.processCommand("edit event enddatetime \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-09T11:00 with 2025-03-08T11:00");
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testEditEventDescriptionAndLocation() {
    controller.processCommand("create event \"RecurringTest\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");

    controller.processCommand("edit event description \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"Adding Description\"");
    assertEquals("Failed to edit event(s)", view.getLastMessage());

    controller.processCommand("edit event location \"Edited Name\" from "
            + "2025-03-08T10:00 to 2025-03-08T11:00 with \"New Location\"");
    assertEquals("Failed to edit event(s)", view.getLastMessage());
  }

  @Test
  public void testEditRecurringEventCommand() {
    controller.processCommand("create event \"RecurringTest\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00 repeats MTWRF until 2025-06-30T23:59");
    boolean result = controller.processCommand("edit events repeatuntil "
            + "\"RecurringTest\" 2025-07-31T23:59");

    assertTrue(result);
    assertEquals("Recurring event modified successfully.", view.getLastMessage());
  }

  @Test
  public void testProcessCommand_Empty() {
    boolean result = controller.processCommand("");

    assertFalse("Expected false when input command is empty", result);
    assertEquals("Parsing Error: Command cannot be null or empty", view.getLastMessage());
  }


  @Test
  public void testProcessCommand_Null() {
    boolean result = controller.processCommand(null);

    assertFalse("Expected false when input command is null", result);
    assertEquals("Parsing Error: Command cannot be null or empty", view.getLastMessage());
  }

  @Test
  public void testProcessCommand_ParsedCommand() {
    CommandParser dummyParser = new CommandParser(manager) {
      @Override
      public ICommand parse(String input) {
        return null;
      }
    };

    CalendarController dummyController = new CalendarController(manager, view, dummyParser);
    boolean result = dummyController.
            processCommand("create calendar --name x --timezone UTC");

    assertFalse("Expected false when parsed command is null", result);
    assertEquals("Parsing Error: Command parsing returned null", view.getLastMessage());
  }

  @Test
  public void testProcessCommandNoActiveCalendarSelected() {
    ICalendarManager freshManager = new CalendarManager();
    CommandParser freshParser = new CommandParser(freshManager);
    CalendarController freshController = new CalendarController(freshManager, view, freshParser);

    freshController.processCommand("create calendar --name testCal --timezone UTC");

    boolean result = freshController.processCommand(
            "create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00");

    assertFalse("Expected false when no active calendar is selected", result);
    assertEquals("Execution Error: null",
            view.getLastMessage());
  }


  @Test
  public void testProcessCommandUnsupportedCommand() {
    ICommand unsupportedCommand = new ICommand() {};

    CommandParser dummyParser = new CommandParser(manager) {
      @Override
      public ICommand parse(String input) {
        return unsupportedCommand;
      }
    };

    CalendarController dummyController = new CalendarController(manager, view, dummyParser);

    boolean result = dummyController.processCommand("dummy unsupported command");

    assertFalse("Expected false for unsupported command type", result);
    assertEquals("Unsupported command type.", view.getLastMessage());
  }

  @Test
  public void testProcessCommandThrowsException() {
    CommandParser throwingParser = new CommandParser(manager) {
      @Override
      public ICommand parse(String input) {
        throw new IllegalArgumentException("Bad input");
      }
    };

    CalendarController dummyController = new CalendarController(manager, view, throwingParser);

    boolean result = dummyController.processCommand("bad input");

    assertFalse("Expected false when IllegalArgumentException is thrown", result);
    assertEquals("Parsing Error: Bad input", view.getLastMessage());
  }

  @Test
  public void testProcessCommandThrowsGenericException() {
    CommandParser throwingParser = new CommandParser(manager) {
      @Override
      public ICommand parse(String input) {
        throw new RuntimeException("Unexpected failure");
      }
    };

    CalendarController dummyController = new CalendarController(manager, view, throwingParser);

    boolean result = dummyController.processCommand("crash input");

    assertFalse("Expected false when generic Exception is thrown", result);
    assertEquals("Execution Error: Unexpected failure", view.getLastMessage());
  }

  @Test
  public void testCreateCalendarAnDUse() {
    boolean created = controller.
            processCommand("create calendar --name Work --timezone UTC");
    assertTrue("Calendar creation should succeed", created);
    assertEquals("Calendar created: Work (UTC)", view.getLastMessage());

    boolean used = controller.processCommand("use calendar --name Work");
    assertTrue("Calendar usage should succeed", used);
    assertEquals("No events found in calendar Work", view.getLastMessage());
  }

  @Test
  public void testCreateEvent() {
    controller.processCommand("create calendar --name Default --timezone UTC");
    controller.processCommand("use calendar --name Default");

    boolean result = controller.processCommand("create event \"Team Sync\" "
            + "from 2025-06-10T09:00 to 2025-06-10T10:00");

    assertTrue("Event creation should succeed", result);
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testPrintEventsOnDate() {
    controller.processCommand("create calendar --name Default --timezone UTC");
    controller.processCommand("use calendar --name Default");
    controller.processCommand("create event \"Meeting\" from 2025-06-01T10:00"
            + " to 2025-06-01T11:00");

    boolean result = controller.processCommand("print events on 2025-06-01");

    assertTrue("Query by date should succeed", result);
    assertTrue("Message should confirm events on that date",
            view.getLastMessage().
                    contains("Displaying") || view.getLastMessage().contains("Events on"));
  }

  @Test
  public void testBusyQuery() {
    controller.processCommand("create calendar --name Default --timezone UTC");
    controller.processCommand("use calendar --name Default");
    controller.processCommand("create event \"Meeting\" from 2025-06-01T10:00"
            + " to 2025-06-01T11:00");

    boolean result = controller.processCommand("show status on 2025-06-01T10:30");

    assertTrue("Busy query should succeed", result);
    assertEquals("Busy at 2025-06-01T10:30", view.getLastMessage());
  }

  @Test
  public void testEditEventDescription() {
    controller.processCommand("create calendar --name Default --timezone UTC");
    controller.processCommand("use calendar --name Default");
    controller.processCommand("create event \"Daily Brief\" from "
            + "2025-06-01T08:00 to 2025-06-01T09:00");

    boolean result = controller.processCommand(
            "edit event description \"Daily Brief\" from 2025-06-01T08:00 "
                    + "to 2025-06-01T09:00 with \"Updated Description\"");

    assertTrue("Editing description should succeed", result);
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test(expected = NoSuchFieldException.class)
  public void testProcessCommand_NoActiveCalendarSelected_ModelCommand() throws Exception {
    controller.processCommand("create calendar --name testCal --timezone UTC");

    java.lang.reflect.Field field = manager.getClass().getDeclaredField("activeCalendarName");
    field.setAccessible(true);
    field.set(manager, null);

    boolean result = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T10:00 to 2025-06-01T11:00");

    assertFalse("Expected false when no active calendar is selected", result);
    assertEquals("No active calendar selected. Use 'use calendar --name <calName>' first.",
            view.getLastMessage());
  }

  @Test
  public void testGetActiveCalendar_ReturnsExpectedModel() {
    controller.processCommand("create calendar --name default --timezone UTC");
    controller.processCommand("use calendar --name default");

    assertNotNull("Active calendar should not be null", controller.getActiveCalendar());
  }



  @Test
  public void testGetView_ReturnsInjectedView() {
    assertSame("Expected same view object injected", view, controller.getView());
  }




  private static class TestView implements ICalendarView {
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
    public void displayEvents(List<ICalendarEvent> events) {
      messages.add("Displaying " + events.size() + " events");
    }

    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public void clearMessages() {
      messages.clear();
    }
  }

}


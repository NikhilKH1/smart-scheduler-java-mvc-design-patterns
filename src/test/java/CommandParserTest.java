//import calendarapp.controller.CalendarController;
//import calendarapp.controller.CommandParser;
//import calendarapp.model.CalendarModel;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import calendarapp.controller.commands.BusyQueryCommand;
//import calendarapp.controller.commands.Command;
//import calendarapp.controller.commands.CreateEventCommand;
//import calendarapp.controller.commands.EditEventCommand;
//import calendarapp.controller.commands.EditRecurringEventCommand;
//import calendarapp.controller.commands.ExportCalendarCommand;
//import calendarapp.controller.commands.QueryByDateCommand;
//import calendarapp.controller.commands.QueryRangeDateTimeCommand;
//import calendarapp.model.event.CalendarEvent;
//import calendarapp.view.ICalendarView;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * JUnit tests for the CommandParser class.
// */
//public class CommandParserTest {
//  private CalendarController controller;
//  private TestCalendarViewParser view;
//  private CommandParser parser;
//
//  @Before
//  public void setUp() {
//    CalendarModel model = new CalendarModel();
//    view = new TestCalendarViewParser();
//    parser = new CommandParser(model);
//    controller = new CalendarController(model, view, parser);
//  }
//
//
//
//  @Test
//  public void testParseEditSingleEventCommandIncomplete() {
//    String command = "edit event description \"Meeting\" from 2025-06-01T09:00";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for incomplete edit event command");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Incomplete edit event command. Expected format:"
//                      + " edit event <property> <eventName> from <start> to <end> with "
//                      + "<NewPropertyValue>",
//              e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandMissingFromKeyword() {
//    String command = "edit event description \"Meeting\" 2025-06-01T09:00 to "
//            + "2025-06-01T10:00 with \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'from' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Incomplete edit event command. Expected format: "
//                      + "edit event <property> <eventName> from <start> to <end> with "
//                      + "<NewPropertyValue>",
//              e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandMissingToKeyword() {
//    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
//            + "something 2025-06-01T10:00 with \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'to' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Expected 'to' after start date/time", e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandMissingFromKeywordMain() {
//    String command = "edit event description \"Meeting\" something 2025-06-01T09:00 "
//            + "to 2025-06-01T10:00 with \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'from' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Expected 'from' after event name", e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandMissingWithKeywordMain() {
//    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
//            + "to 2025-06-01T10:00 without \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'from' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Expected 'with' after end date/time", e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandMissingWithKeyword() {
//    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'with' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Incomplete edit event command. Expected format:"
//                      + " edit event <property> <eventName> from <start> to <end> with "
//                      + "<NewPropertyValue>",
//              e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditSingleEventCommandValid() {
//    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
//            + "to 2025-06-01T10:00 with \"Updated Description\"";
//    Command parsedCommand = parser.parse(command);
//    assertTrue("Should parse as EditEventCommand",
//            parsedCommand instanceof EditEventCommand);
//
//    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
//    assertEquals("description", editCmd.getProperty());
//    assertEquals("Meeting", editCmd.getEventName());
//    assertEquals(LocalDateTime.of(2025, 6, 1, 9, 0),
//            editCmd.getOriginalStart());
//    assertEquals(LocalDateTime.of(2025, 6, 1, 10, 0),
//            editCmd.getOriginalEnd());
//    assertEquals("Updated Description", editCmd.getNewValue());
//  }
//
//  @Test
//  public void testParseEditEventsCommandMissingFromKeyword() {
//    String command = "edit events description \"Meeting\" nothing 2025-06-01T09:00 "
//            + "with \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'from' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Expected 'from' after event name", e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditEventsCommandMissingWithKeyword() {
//    String command = "edit events description \"Meeting\" from 2025-06-01T09:00 "
//            + "something \"Updated Description\"";
//    try {
//      parser.parse(command);
//      fail("Expected IllegalArgumentException for missing 'with' keyword");
//    } catch (IllegalArgumentException e) {
//      assertEquals("Expected 'with' after date/time", e.getMessage());
//    }
//  }
//
//  @Test
//  public void testParseEditRecurringEventCommand() {
//    String command = "edit events repeatuntil \"WeeklyStandup\" \"2025-07-31T23:59\"";
//    Command parsedCommand = parser.parse(command);
//    assertTrue(parsedCommand instanceof EditRecurringEventCommand);
//    EditRecurringEventCommand editCmd = (EditRecurringEventCommand) parsedCommand;
//    assertEquals("repeatuntil", editCmd.getProperty());
//    assertEquals("WeeklyStandup", editCmd.getEventName());
//    assertEquals("2025-07-31T23:59", editCmd.getNewValue());
//  }
//
//  @Test
//  public void testParseEditEventsCommandNonRecurring() {
//    String command = "edit events description \"Meeting\" \"Updated Description\"";
//    Command parsedCommand = parser.parse(command);
//    assertTrue(parsedCommand instanceof EditEventCommand);
//    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
//    assertEquals("description", editCmd.getProperty());
//    assertEquals("Meeting", editCmd.getEventName());
//    assertEquals("Updated Description", editCmd.getNewValue());
//  }
//
//
//  @Test
//  public void testCreateEventMissingEventKeyword() {
//    String command = "create something";
//    controller.processCommand(command);
//    assertEquals("Parsing Error: Expected 'event' after create", view.getLastMessage());
//  }
//
//  @Test
//  public void testCreateEventMissingEventName() {
//    String command = "create event";
//    controller.processCommand(command);
//    assertEquals("Parsing Error: Missing event name", view.getLastMessage());
//  }
//
//  @Test
//  public void testCreateEventMissingFromOrOn() {
//    String command = "create event \"Test\"";
//    controller.processCommand(command);
//    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
//            view.getLastMessage());
//  }
//
//  @Test
//  public void testRecurringEventExceeds24Hours() {
//    String command = "create event \"Test\" from 2025-06-01T10:00 to 2025-06-02T11:00 "
//            + "repeats MTWRF for 3 times";
//    controller.processCommand(command);
//    assertEquals("Parsing Error: Recurring event must end within "
//            + "24 hours of the start time.", view.getLastMessage());
//  }
//
//
//  private static class TestCalendarViewParser implements ICalendarView {
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
import calendarapp.controller.commands.*;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;
import calendarapp.model.ICalendarModel;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;

public class CommandParserTest {

  private CommandParser parser;
  private ICalendarManager calendarManager;
  private TestCalendarView view;
  private CalendarController controller;

  @Before
  public void setUp() {
    calendarManager = new CalendarManager();
    parser = new CommandParser(calendarManager);
    view = new TestCalendarView();
    controller = new CalendarController(calendarManager, view, parser);
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");
  }

  @Test
  public void testParseCreateEventCommand() {
    ICommand cmd = parser.parse
            ("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  @Test
  public void testParseQueryByDateCommand() {
    ICommand cmd = parser.parse("print events on 2025-06-01");
    assertTrue(cmd instanceof QueryByDateCommand);
    assertEquals(LocalDate.of(2025, 6, 1),
            ((QueryByDateCommand) cmd).getQueryDate());
  }

  @Test
  public void testParseQueryRangeCommand() {
    ICommand cmd = parser.parse("print events from 2025-06-01T09:00 to 2025-06-01T11:00");
    assertTrue(cmd instanceof QueryRangeDateTimeCommand);
  }

  @Test
  public void testParseShowStatusCommand() {
    ICommand cmd = parser.parse("show status on 2025-06-01T10:00");
    assertTrue(cmd instanceof BusyQueryCommand);
  }

  @Test
  public void testParseExportCalendarCommand() {
    ICommand cmd = parser.parse("export cal testCal.csv");
    assertTrue(cmd instanceof ExportCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUnknownCommandThrowsException() {
    parser.parse("some invalid command");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEmptyCommandThrowsException() {
    parser.parse("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseNullCommandThrowsException() {
    parser.parse(null);
  }

  @Test
  public void testParseCreateEvent() {
    ICommand parsedCommand = parser.parse
            ("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00");
    assertTrue("Should parse as CreateEventCommand",
            parsedCommand instanceof CreateEventCommand);

    CreateEventCommand cmd = (CreateEventCommand) parsedCommand;
    assertEquals("Meeting", cmd.getEventName());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
            0, ZoneId.of("UTC")), cmd.getStartDateTime());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 0, 0,
            0, ZoneId.of("UTC")), cmd.getEndDateTime());
  }

  @Test
  public void testParsePrintEventsOnDate() {
    ICommand parsedCommand = parser.parse("print events on 2025-06-01");
    assertTrue(parsedCommand instanceof QueryByDateCommand);

    QueryByDateCommand cmd = (QueryByDateCommand) parsedCommand;
    assertEquals(LocalDate.of(2025, 6, 1), cmd.getQueryDate());
  }

  @Test
  public void testParsePrintEventsInRange() {
    ICommand parsedCommand = parser.parse
            ("print events from 2025-06-01T09:00 to 2025-06-01T10:30");
    assertTrue(parsedCommand instanceof QueryRangeDateTimeCommand);

    QueryRangeDateTimeCommand cmd = (QueryRangeDateTimeCommand) parsedCommand;
    assertEquals(ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
            0, ZoneId.of("UTC")), cmd.getStartDateTime());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 30, 0,
            0, ZoneId.of("UTC")), cmd.getEndDateTime());
  }

  @Test
  public void testParseShowStatus() {
    ICommand parsedCommand = parser.parse("show status on 2025-06-01T10:00");
    assertTrue(parsedCommand instanceof BusyQueryCommand);

    BusyQueryCommand cmd = (BusyQueryCommand) parsedCommand;
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 0, 0,
            0, ZoneId.of("UTC")), cmd.getQueryTime());
  }

  @Test
  public void testParseEditSingleEvent_Assignment5() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-01T10:00 with \"Updated Description\"";
    ICommand parsedCommand = parser.parse(command);
    assertTrue(parsedCommand instanceof EditEventCommand);

    EditEventCommand cmd = (EditEventCommand) parsedCommand;

    ZonedDateTime expectedStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime expectedEnd = ZonedDateTime.of(2025, 6, 1, 10,
            0, 0, 0, ZoneId.of("UTC"));

    assertEquals("Meeting", cmd.getEventName());
    assertEquals("description", cmd.getProperty());
    assertEquals("Updated Description", cmd.getNewValue());
    assertEquals(expectedStart, (ZonedDateTime) cmd.getOriginalStart());
    assertEquals(expectedEnd, (ZonedDateTime) cmd.getOriginalEnd());
  }

//  @Test
//  public void testParseEditRecurringEvent() {
//    String command = "edit events repeatuntil \"Team Sync\" 2025-12-31T00:00";
//    ICommand parsedCommand = parser.parse(command);
//
//    assertTrue(parsedCommand instanceof EditRecurringEventCommand);
//    EditRecurringEventCommand editCmd = (EditRecurringEventCommand) parsedCommand;
//
//    assertEquals("Team Sync", editCmd.getEventName());
//    assertEquals("repeatuntil", editCmd.getProperty());
//    assertEquals(ZonedDateTime.of(2025, 12, 31, 0, 0, 0, 0, ZoneId.of("UTC")),
//            (ZonedDateTime) editCmd.getNewRepeatUntil());
//  }

//  @Test
//  public void testParseExportCommand() {
//    String command = "export cal events.csv";
//    ICommand parsedCommand = parser.parse(command);
//
//    assertTrue(parsedCommand instanceof ExportCalendarCommand);
//    ExportCalendarCommand exportCmd = (ExportCalendarCommand) parsedCommand;
//
//    assertEquals("events.csv", exportCmd.getClass());
//  }


  @Test
  public void testParseUnknownCommand() {
    String command = "unknowncommand";

    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for unknown command");
    } catch (IllegalArgumentException e) {
      assertEquals("Unknown command: unknowncommand", e.getMessage());
    }
  }

  @Test
  public void testParseCreateEventMissingDateHandledGracefully() {
    String command = "create event \"Team Lunch\" from";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    String errorMsg = view.getLastMessage();
    assertTrue(errorMsg.contains("Execution Error: Index 4 out of bounds for length 4"));
  }

  @Test
  public void testProcessCreateEventWithInvalidDateHandledGracefully() {
    String command = "create event \"FaultyEvent\" from INVALID_DATE to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    String errorMsg = view.getLastMessage();
    assertTrue(errorMsg.
            contains("Execution Error: Text 'INVALID_DATE' could not be parsed at index 0"));
  }

  @Test
  public void testParseMalformedCommand() {
    String command = "create event from 2025-06-01T09:00 to 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for malformed command");
    } catch (IllegalArgumentException e) {
      String msg = e.getMessage().toLowerCase();
      assertTrue(msg.contains("expected") || msg.contains("event name"));
    }
  }

  @Test
  public void testParsePrintEventsMissingDate() {
    String command = "print events on";

    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Expected date after 'on'"));
    }
  }

  @Test
  public void testParsePrintEventsInvalidDateFormat() {
    String command = "print events on 06-01-2025";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect date format");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid date format"));
    }
  }

  @Test
  public void testParseShowStatusMissingDate() {
    String command = "show status on";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("usage: show status on"));
    }
  }

  @Test
  public void testParseEditRecurringEventInvalidFormat() {
    String command = "edit events repeatuntil \"Daily Scrum\"";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing new value");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit events command format"));
    }
  }

  @Test
  public void testParseExportCommandMissingFileName() {
    String command = "export cal";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing file name");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid export format"));
    }
  }

  @Test
  public void testParseExportCommandNonCSVFile() {
    String command = "export cal events.txt";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect file extension");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains(".csv extension"));
    }
  }

  @Test
  public void testParseEmptyCommand() {
    boolean result = controller.processCommand("");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("parsing error"));
  }

  @Test
  public void testParseNullCommand() {
    try {
      parser.parse(null);
      fail("Should throw IllegalArgumentException for null command");
    } catch (IllegalArgumentException e) {
      assertEquals("Command cannot be null or empty", e.getMessage());
    }
  }

  @Test
  public void testParseRecurringEventExceeds24Hours() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");

    String command = "create event \"Long Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-02T10:00 repeats MTWRF until 2025-06-30T23:59";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Recurring event must end within 24 hours"));
  }

  @Test
  public void testParseEditEventsMissingKeywords() {
    String command = "edit events location";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for incomplete edit events command");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Incomplete edit command"));
    }
  }

  @Test
  public void testParseExportCommandInvalidFormat() {
    String command = "export cal";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid export format");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid export format"));
    }
  }

  @Test
  public void testParseExportCommandMissingCalKeyword() {
    String command = "export something events.csv";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'cal' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid export format"));
    }
  }

  @Test
  public void testParseExportCommandInvalidFileExtension() {
    String command = "export cal events.txt";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid file extension");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains(".csv extension"));
    }
  }

//  @Test
//  public void testParseExportCommandValid() {
//    String command = "export cal events.csv";
//    ICommand parsedCommand = parser.parse(command);
//
//    assertTrue(parsedCommand instanceof ExportCalendarCommand);
//    ExportCalendarCommand cmd = (ExportCalendarCommand) parsedCommand;
//    assertEquals("events.csv", cmd.getFileName());
//  }


  @Test
  public void testParsePrintCommandIncomplete() {
    String command = "print";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete print command");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid print command format"));
    }
  }

  @Test
  public void testParsePrintCommandMissingEventsKeyword() {
    String command = "print something on 2025-06-01";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'events' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid print command format"));
    }
  }

  @Test
  public void testParsePrintCommandOnWithoutDate() {
    String command = "print events on";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing date after 'on'");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected date after 'on'", e.getMessage());
    }
  }

  @Test
  public void testParsePrintCommandOnValid() {
    String command = "print events on 2025-06-01";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof QueryByDateCommand);
    QueryByDateCommand cmd = (QueryByDateCommand) parsedCommand;
    assertEquals(LocalDate.of(2025, 6, 1), cmd.getQueryDate());
  }

  @Test
  public void testParsePrintCommandFromWithoutRange() {
    String command = "print events from 2025-06-01T09:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete range");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("expected format"));
    }
  }

  @Test
  public void testParsePrintCommandFromWithoutToKeyword() {
    String command = "print events from 2025-06-01T09:00 something 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'to' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("expected format"));
    }
  }

  @Test
  public void testParsePrintCommandFromValidRange() {
    controller.processCommand("create calendar --name cal1 --timezone UTC");
    controller.processCommand("use calendar --name cal1");

    String command = "print events from 2025-06-01T09:00 to 2025-06-01T10:30";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof QueryRangeDateTimeCommand);
    QueryRangeDateTimeCommand cmd = (QueryRangeDateTimeCommand) parsedCommand;
    assertEquals(ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.of("UTC")), cmd.getStartDateTime());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 30, 0, 0, ZoneId.of("UTC")), cmd.getEndDateTime());
  }

  @Test
  public void testParsePrintCommandInvalidSpecifier() {
    String command = "print events tomorrow";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid print specifier");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid print command"));
    }
  }







  /**
   * A minimal test view implementation for CommandParser testing.
   */
  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = "";

    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayError(String error) {
      this.lastMessage = error;
    }

    @Override
    public void displayEvents(java.util.List<calendarapp.model.event.ICalendarEvent> events) {
      if (events == null || events.isEmpty()) {
        this.lastMessage = "No events found";
      } else {
        this.lastMessage = "Displaying " + events.size() + " events";
      }
    }

    public String getLastMessage() {
      return lastMessage;
    }
  }
}


import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarModel;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendarapp.controller.commands.BusyQueryCommand;
import calendarapp.controller.commands.Command;
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.controller.commands.EditEventCommand;
import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.controller.commands.QueryByDateCommand;
import calendarapp.controller.commands.QueryRangeDateTimeCommand;
import calendarapp.model.event.CalendarEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit tests for the CommandParser class.
 */
public class CommandParserTest {
  private CalendarController controller;
  private TestCalendarViewParser view;
  private CommandParser parser;

  @Before
  public void setUp() {
    CalendarModel model = new CalendarModel();
    view = new TestCalendarViewParser();
    parser = new CommandParser(model);
    controller = new CalendarController(model, view, parser);
  }

  @Test
  public void testParseCreateEvent() {
    String command = "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as CreateEventCommand",
            parsedCommand instanceof CreateEventCommand);

    CreateEventCommand createCmd = (CreateEventCommand) parsedCommand;
    assertEquals("Event name should match", "Meeting", createCmd.getEventName());
    assertEquals("Start time should match", LocalDateTime.of(2025, 6,
            1, 9, 0), createCmd.getStartDateTime());
    assertEquals("End time should match", LocalDateTime.of(2025, 6,
            1, 10, 0), createCmd.getEndDateTime());
  }

  @Test
  public void testParsePrintEventsOnDate() {
    String command = "print events on 2025-06-01";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryByDateCommand",
            parsedCommand instanceof QueryByDateCommand);

    QueryByDateCommand queryCmd = (QueryByDateCommand) parsedCommand;
    assertEquals("Query date should match", LocalDate.of(2025,
            6, 1), queryCmd.getQueryDate());
  }

  @Test
  public void testParsePrintEventsInRange() {
    String command = "print events from 2025-06-01T09:00 to 2025-06-01T10:30";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryRangeDateTimeCommand",
            parsedCommand instanceof QueryRangeDateTimeCommand);

    QueryRangeDateTimeCommand rangeCmd = (QueryRangeDateTimeCommand) parsedCommand;
    assertEquals("Start time should match", LocalDateTime.of(2025,
            6, 1, 9, 0), rangeCmd.getStartDateTime());
    assertEquals("End time should match", LocalDateTime.of(2025,
            6, 1, 10, 30), rangeCmd.getEndDateTime());
  }

  @Test
  public void testParseShowStatus() {
    String command = "show status on 2025-06-01T10:00";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as BusyQueryCommand",
            parsedCommand instanceof BusyQueryCommand);

    BusyQueryCommand busyCmd = (BusyQueryCommand) parsedCommand;
    assertEquals("Query time should match", LocalDateTime.of(2025,
            6, 1, 10, 0), busyCmd.getQueryTime());
  }

  @Test
  public void testParseEditSingleEvent() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-01T10:00 with \"Updated Description\"";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as EditEventCommand",
            parsedCommand instanceof EditEventCommand);

    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
    assertEquals("Event name should match", "Meeting",
            editCmd.getEventName());
    assertEquals("Property should be 'description'", "description",
            editCmd.getProperty());
    assertEquals("New value should be 'Updated Description'",
            "Updated Description", editCmd.getNewValue());
  }

  @Test
  public void testParseEditRecurringEvent() {
    String command = "edit events repeatuntil \"Team Sync\" 2025-12-31T00:00";

    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as EditRecurringEventCommand",
            parsedCommand instanceof EditRecurringEventCommand);

    EditRecurringEventCommand editCmd = (EditRecurringEventCommand) parsedCommand;
    assertEquals("Event name should match", "Team Sync",
            editCmd.getEventName());
    assertEquals("Property should be 'repeatuntil'", "repeatuntil",
            editCmd.getProperty());
    assertEquals("New value should be '2025-12-31T00:00'", "2025-12-31T00:00",
            editCmd.getNewValue());
  }

  @Test
  public void testParseExportCommand() {
    String command = "export cal events.csv";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as ExportCalendarCommand",
            parsedCommand instanceof ExportCalendarCommand);

    ExportCalendarCommand exportCmd = (ExportCalendarCommand) parsedCommand;
    assertEquals("File name should match", "events.csv",
            exportCmd.getFileName());
  }

  @Test
  public void testParseUnknownCommand() {
    String command = "unknowncommand";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for unknown command");
    } catch (IllegalArgumentException e) {
      assertEquals("Unknown command: unknowncommand",
              e.getMessage());
    }
  }

  @Test
  public void testParseMalformedCommand() {
    String command = "create event from 2025-06-01T09:00 to 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for malformed command");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should contain 'Expected 'from'"
                      + " or 'on' after event name'",
              e.getMessage().contains("Expected 'from' or 'on' after event name"));
    }
  }


  @Test
  public void testParseCreateEventMissingDateHandledGracefully() {
    String command = "create event \"Team Lunch\" from";
    boolean result = controller.processCommand(command);
    assertFalse(result);
  }

  @Test
  public void testProcessCreateEventWithInvalidDateHandledGracefully() {
    String command = "create event \"FaultyEvent\" from INVALID_DATE to 2025-06-01T11:00";
    boolean result = controller.processCommand(command);
    assertFalse(result);
  }



  @Test
  public void testParsePrintEventsMissingDate() {
    String command = "print events on";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing date",
              e.getMessage().contains("Expected date after 'on'"));
    }
  }

  @Test
  public void testParsePrintEventsInvalidDateFormat() {
    String command = "print events on 06-01-2025";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect date format");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format",
              e.getMessage().contains("Invalid date format"));
    }
  }

  @Test
  public void testParseShowStatusMissingDate() {
    String command = "show status on";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing date",
              e.getMessage().contains("Usage: show status on <dateTime>"));
    }
  }


  @Test
  public void testParseEditRecurringEventInvalidFormat() {
    String command = "edit events repeatuntil \"Daily Scrum\"";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing new value");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format",
              e.getMessage().contains("Invalid edit events command format"));
    }
  }

  @Test
  public void testParseExportCommandMissingFileName() {
    String command = "export cal";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing file name");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing file name",
              e.getMessage().contains("Invalid export command format"));
    }
  }

  @Test
  public void testParseExportCommandNonCSVFile() {
    String command = "export cal events.txt";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect file extension");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format",
              e.getMessage().contains("Invalid file name. Must be a CSV file"));
    }
  }

  @Test
  public void testParseEmptyCommand() {
    String command = "";
    Command parsedCommand = parser.parse(command);
    assertNull("Empty command should return null", parsedCommand);
  }

  @Test
  public void testParseNullCommand() {
    try {
      parser.parse(null);
      fail("Should throw IllegalArgumentException for null command");
    } catch (IllegalArgumentException e) {
      assertEquals("Command cannot be null", e.getMessage());
    }
  }

  @Test
  public void testParseRecurringEventExceeds24Hours() {
    String command = "create event \"Long Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-02T10:00 repeats MTWRF until 2025-06-30T23:59";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException because event exceeds 24 hours");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate event exceeds 24 hours",
              e.getMessage().contains("Recurring event must end within "
                      + "24 hours of the start time."));
    }
  }

  @Test
  public void testParseEditEventsMissingKeywords() {
    String command = "edit events location";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for incomplete edit events command");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing keywords",
              e.getMessage().contains("Incomplete edit command"));
    }
  }

  @Test
  public void testParseExportCommandInvalidFormat() {
    String command = "export cal";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid export format");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid export command format. Expected: export cal fileName.csv",
              e.getMessage());
    }
  }

  @Test
  public void testParseExportCommandMissingCalKeyword() {
    String command = "export something events.csv";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'cal' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid export command. Expected 'cal' after export.",
              e.getMessage());
    }
  }

  @Test
  public void testParseExportCommandInvalidFileExtension() {
    String command = "export cal events.txt";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid file extension");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid file name. Must be a CSV file ending with .csv",
              e.getMessage());
    }
  }

  @Test
  public void testParseExportCommandValid() {
    String command = "export cal events.csv";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as ExportCalendarCommand",
            parsedCommand instanceof ExportCalendarCommand);

    ExportCalendarCommand exportCmd = (ExportCalendarCommand) parsedCommand;
    assertEquals("events.csv", exportCmd.getFileName());
  }

  @Test
  public void testParsePrintCommandIncomplete() {
    String command = "print";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete print command");
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete print events command. Usage: print events on/from "
              + "<date/time>", e.getMessage());
    }
  }

  @Test
  public void testParsePrintCommandMissingEventsKeyword() {
    String command = "print something on 2025-06-01";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'events' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'events' after print", e.getMessage());
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
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryByDateCommand",
            parsedCommand instanceof QueryByDateCommand);

    QueryByDateCommand queryCmd = (QueryByDateCommand) parsedCommand;
    assertEquals(LocalDate.of(2025, 6, 1), queryCmd.getQueryDate());
  }

  @Test
  public void testParsePrintCommandFromWithoutRange() {
    String command = "print events from 2025-06-01T09:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete range");
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete print events range command. "
              + "Usage: print events from <start> to <end>", e.getMessage());
    }
  }

  @Test
  public void testParsePrintCommandFromWithoutToKeyword() {
    String command = "print events from 2025-06-01T09:00 something 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'to' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'to' after start date/time", e.getMessage());
    }
  }

  @Test
  public void testParsePrintCommandFromValidRange() {
    String command = "print events from 2025-06-01T09:00 to 2025-06-01T10:30";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryRangeDateTimeCommand",
            parsedCommand instanceof QueryRangeDateTimeCommand);

    QueryRangeDateTimeCommand rangeCmd = (QueryRangeDateTimeCommand) parsedCommand;
    assertEquals(LocalDateTime.of(2025, 6, 1,
            9, 0), rangeCmd.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 6, 1,
            10, 30), rangeCmd.getEndDateTime());
  }

  @Test
  public void testParsePrintCommandInvalidSpecifier() {
    String command = "print events tomorrow";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for invalid print specifier");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'on' or 'from' after 'print events'", e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandIncomplete() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete edit event command");
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete edit event command. Expected format:"
                      + " edit event <property> <eventName> from <start> to <end> with "
                      + "<NewPropertyValue>",
              e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingFromKeyword() {
    String command = "edit event description \"Meeting\" 2025-06-01T09:00 to "
            + "2025-06-01T10:00 with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete edit event command. Expected format: "
                      + "edit event <property> <eventName> from <start> to <end> with "
                      + "<NewPropertyValue>",
              e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingToKeyword() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
            + "something 2025-06-01T10:00 with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'to' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'to' after start date/time", e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingFromKeywordMain() {
    String command = "edit event description \"Meeting\" something 2025-06-01T09:00 "
            + "to 2025-06-01T10:00 with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'from' after event name", e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingWithKeywordMain() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
            + "to 2025-06-01T10:00 without \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'with' after end date/time", e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingWithKeyword() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'with' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete edit event command. Expected format:"
                      + " edit event <property> <eventName> from <start> to <end> with "
                      + "<NewPropertyValue>",
              e.getMessage());
    }
  }

  @Test
  public void testParseEditSingleEventCommandValid() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 "
            + "to 2025-06-01T10:00 with \"Updated Description\"";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as EditEventCommand",
            parsedCommand instanceof EditEventCommand);

    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
    assertEquals("description", editCmd.getProperty());
    assertEquals("Meeting", editCmd.getEventName());
    assertEquals(LocalDateTime.of(2025, 6, 1, 9, 0),
            editCmd.getOriginalStart());
    assertEquals(LocalDateTime.of(2025, 6, 1, 10, 0),
            editCmd.getOriginalEnd());
    assertEquals("Updated Description", editCmd.getNewValue());
  }

  @Test
  public void testParseEditEventsCommandMissingFromKeyword() {
    String command = "edit events description \"Meeting\" nothing 2025-06-01T09:00 "
            + "with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'from' after event name", e.getMessage());
    }
  }

  @Test
  public void testParseEditEventsCommandMissingWithKeyword() {
    String command = "edit events description \"Meeting\" from 2025-06-01T09:00 "
            + "something \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'with' keyword");
    } catch (IllegalArgumentException e) {
      assertEquals("Expected 'with' after date/time", e.getMessage());
    }
  }

  @Test
  public void testParseEditRecurringEventCommand() {
    String command = "edit events repeatuntil \"WeeklyStandup\" \"2025-07-31T23:59\"";
    Command parsedCommand = parser.parse(command);
    assertTrue(parsedCommand instanceof EditRecurringEventCommand);
    EditRecurringEventCommand editCmd = (EditRecurringEventCommand) parsedCommand;
    assertEquals("repeatuntil", editCmd.getProperty());
    assertEquals("WeeklyStandup", editCmd.getEventName());
    assertEquals("2025-07-31T23:59", editCmd.getNewValue());
  }

  @Test
  public void testParseEditEventsCommandNonRecurring() {
    String command = "edit events description \"Meeting\" \"Updated Description\"";
    Command parsedCommand = parser.parse(command);
    assertTrue(parsedCommand instanceof EditEventCommand);
    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
    assertEquals("description", editCmd.getProperty());
    assertEquals("Meeting", editCmd.getEventName());
    assertEquals("Updated Description", editCmd.getNewValue());
  }


  @Test
  public void testCreateEventMissingEventKeyword() {
    String command = "create something";
    controller.processCommand(command);
    assertEquals("Parsing Error: Expected 'event' after create", view.getLastMessage());
  }

  @Test
  public void testCreateEventMissingEventName() {
    String command = "create event";
    controller.processCommand(command);
    assertEquals("Parsing Error: Missing event name", view.getLastMessage());
  }

  @Test
  public void testCreateEventMissingFromOrOn() {
    String command = "create event \"Test\"";
    controller.processCommand(command);
    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
            view.getLastMessage());
  }

  @Test
  public void testRecurringEventExceeds24Hours() {
    String command = "create event \"Test\" from 2025-06-01T10:00 to 2025-06-02T11:00 "
            + "repeats MTWRF for 3 times";
    controller.processCommand(command);
    assertEquals("Parsing Error: Recurring event must end within "
            + "24 hours of the start time.", view.getLastMessage());
  }


  private static class TestCalendarViewParser implements ICalendarView {
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

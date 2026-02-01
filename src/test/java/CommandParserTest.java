import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.commands.BusyQueryCommand;
import calendarapp.controller.commands.CopyEventsBetweenDatesCommand;
import calendarapp.controller.commands.CopyEventsOnDateCommand;
import calendarapp.controller.commands.CopySingleEventCommand;
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.controller.commands.EditCalendarCommand;
import calendarapp.controller.commands.EditEventCommand;
import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.controller.commands.ICommand;
import calendarapp.controller.commands.ImportCalendarCommand;
import calendarapp.controller.commands.QueryByDateCommand;
import calendarapp.controller.commands.QueryRangeDateTimeCommand;
import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit tests for the CommandParser class.
 */
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

  @Test(expected = IllegalArgumentException.class)
  public void testParseImportCommandJustEnoughTokens() {
    parser.parse("import cal \"\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyCommandBoundary() {
    parser.parse("copy");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateBoundary() {
    parser.parse("copy events on 2025-06-01 --target calendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateCommandBoundary() {
    parser.parse("create");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCommandBoundary() {
    parser.parse("edit");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCommandBoundary() {
    parser.parse("use");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCreateEventBoundary() {
    parser.parse("create event");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseRecurringSectionBoundaryWeekdaysMissing() {
    parser.parse("create event \"Test Event\" from 2025-06-01T09:00 repeats");
  }

  @Test
  public void testParseRecurringSectionBoundaryEmptyWeekdays() {
    ICommand command = parser.parse("create event \"Test Event\" from 2025-06-01T09:00 "
            + "repeats \"\" until 2025-06-30");
    assertNotNull(command);
  }



  @Test(expected = DateTimeParseException.class)
  public void testEditRecurringEventEmptyNewValue() {
    parser.parse("edit events repeatuntil \"Standup\" \"\"");
  }

  @Test(expected = DateTimeParseException.class)
  public void testEditRecurringEventRepeatUntilMissingT() {
    parser.parse("edit events repeatuntil \"Standup\" \"2025-07-31 23:59\"");
  }


  @Test
  public void testEditRecurringEventEmptyNewValue_ManualCheck() {
    try {
      parser.parse("edit events repeatuntil \"Standup\" \"\"");
      fail("Expected DateTimeParseException for empty date string");
    } catch (DateTimeParseException e) {
      assertTrue(e.getMessage().contains("could not be parsed"));
    }
  }


  @Test
  public void testParseCommandWithExtraWhitespace() {
    ICommand cmd = parser.parse("  create   event   \"Meeting\"  from  2025-04-01T10:00  "
            + "to  2025-04-01T11:00  ");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  @Test
  public void testParseImportCommandValid() {
    ICommand cmd = parser.parse("import cal \"calendar.csv\"");
    assertTrue(cmd instanceof ImportCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseImportCommandMissingCalKeyword() {
    parser.parse("import file.csv");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseImportCommandMissingFilePath() {
    parser.parse("import cal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseImportCommandInvalidFileExtension() {
    parser.parse("import cal file.txt");
  }

  @Test
  public void testParseImportCommandWithSpacesInFilePath() {
    ICommand cmd = parser.parse("import cal \"my calendar file.csv\"");
    assertTrue(cmd instanceof ImportCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsInvalidSpecifier() {
    parser.parse("copy events during 2025-06-01");
  }


  @Test
  public void testParseImportCommandWithoutQuotes() {
    ICommand cmd = parser.parse("import cal calendar.csv");
    assertTrue(cmd instanceof ImportCalendarCommand);
  }







  @Test
  public void testParseCreateEventCommand() {
    ICommand cmd = parser.parse("create event \"Meeting\" from"
            + " 2025-06-01T09:00 to 2025-06-01T10:00");
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

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopySingleEventInvalidFormat() {
    parser.parse("copy event \"Meeting\" on 2025-06-01T09:00 --target TargetCal");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateInvalidMissingTarget() {
    parser.parse("copy events on 2025-06-01 --target");
  }


  @Test(expected = NullPointerException.class)
  public void testParseCopyEventsOnDateValid() {
    String command = "copy events on 2025-06-01 --target \"PersonalCal\" to 2025-06-02";
    ICommand parsedCommand = parser.parse(command);
    assertTrue(parsedCommand instanceof CopyEventsOnDateCommand);
  }

  @Test(expected = NullPointerException.class)
  public void testParseCopyEventsBetweenValid() {
    String command = "copy events between 2025-06-01 and 2025-06-10 --target "
            + "\"NewCal\" to 2025-07-01";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof CopyEventsBetweenDatesCommand);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenInvalidFormat() {
    parser.parse("copy events between 2025-06-01 and --target "
            + "\"TargetCal\" to 2025-07-01");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyCommandUnknownType() {
    parser.parse("copy unknown");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyCommandIncomplete() {
    parser.parse("copy");
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
    ICommand parsedCommand = parser.parse("create event \"Meeting\" "
            + "from 2025-06-01T09:00 to 2025-06-01T10:00");
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
    ICommand parsedCommand = parser.parse("print events from"
            + " 2025-06-01T09:00 to 2025-06-01T10:30");
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
  public void testParseExportCommand() throws Exception {
    String command = "export cal events.csv";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof ExportCalendarCommand);
    ExportCalendarCommand exportCmd = (ExportCalendarCommand) parsedCommand;

    Field filePathField = exportCmd.getClass().getDeclaredField("filePath");
    filePathField.setAccessible(true);
    String filePath = (String) filePathField.get(exportCmd);

    assertEquals("events.csv", filePath);
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
    assertEquals(ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
            0, ZoneId.of("UTC")), cmd.getStartDateTime());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 30, 0,
            0, ZoneId.of("UTC")), cmd.getEndDateTime());
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

  @Test
  public void testParseEditSingleEventCommandIncomplete() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for incomplete edit event command");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingFromKeyword() {
    String command = "edit event description \"Meeting\" 2025-06-01T09:00 to 2025-06-01T10:00 "
            + "with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingToKeyword() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 something "
            + "2025-06-01T10:00 with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'to' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingFromKeywordMain() {
    String command = "edit event description \"Meeting\" something 2025-06-01T09:00 to "
            + "2025-06-01T10:00 with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingWithKeywordMain() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-01T10:00 without \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'with' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandMissingWithKeyword() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'with' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit event command format"));
    }
  }

  @Test
  public void testParseEditSingleEventCommandValid() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");

    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to "
            + "2025-06-01T10:00 with \"Updated Description\"";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof EditEventCommand);
    EditEventCommand cmd = (EditEventCommand) parsedCommand;
    assertEquals("description", cmd.getProperty());
    assertEquals("Meeting", cmd.getEventName());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 9, 0, 0,
            0, ZoneId.of("UTC")), cmd.getOriginalStart());
    assertEquals(ZonedDateTime.of(2025, 6, 1, 10, 0, 0,
            0, ZoneId.of("UTC")), cmd.getOriginalEnd());
    assertEquals("Updated Description", cmd.getNewValue());
  }

  @Test
  public void testParseEditEventsCommandMissingFromKeyword() {
    String command = "edit events description \"Meeting\" nothing 2025-06-01T09:00 "
            + "with \"Updated Description\"";
    try {
      parser.parse(command);
      fail("Expected IllegalArgumentException for missing 'from' keyword");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit events command format"));
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
      assertTrue(e.getMessage().toLowerCase().contains("invalid edit events command format"));
    }
  }


  @Test
  public void testParseEditRecurringEventCommand() throws Exception {
    String command = "edit events repeatuntil \"WeeklyStandup\" \"2025-07-31T23:59\"";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof EditRecurringEventCommand);
    EditRecurringEventCommand cmd = (EditRecurringEventCommand) parsedCommand;

    Field propertyField = cmd.getClass().getDeclaredField("property");
    Field nameField = cmd.getClass().getDeclaredField("eventName");
    Field newValField = cmd.getClass().getDeclaredField("newValue");

    propertyField.setAccessible(true);
    nameField.setAccessible(true);
    newValField.setAccessible(true);

    String property = (String) propertyField.get(cmd);
    String name = (String) nameField.get(cmd);
    String newValue = (String) newValField.get(cmd);

    assertEquals("repeatuntil", property);
    assertEquals("WeeklyStandup", name);
  }


  @Test
  public void testParseEditEventsCommandNonRecurring() {
    String command = "edit events description \"Meeting\" \"Updated Description\"";
    ICommand parsedCommand = parser.parse(command);

    assertTrue(parsedCommand instanceof EditEventCommand);
    EditEventCommand cmd = (EditEventCommand) parsedCommand;
    assertEquals("description", cmd.getProperty());
    assertEquals("Meeting", cmd.getEventName());
    assertEquals("Updated Description", cmd.getNewValue());
  }

  @Test
  public void testCreateEventMissingEventKeyword() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");

    String command = "create something";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("expected 'event' after create"));
  }

  @Test
  public void testCreateEventMissingEventName() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");

    String command = "create event";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("missing event name"));
  }

  @Test
  public void testCreateEventMissingFromOrOn() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    controller.processCommand("use calendar --name testCal");

    String command = "create event \"Test\"";
    boolean result = controller.processCommand(command);

    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Execution Error: Index 3 out "
            + "of bounds for length 3"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopySingleEventTooFewTokens() {
    parser.parse("copy event \"Meeting\" on 2025-04-01T10:00 --target");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopySingleEventWrongOnKeyword() {
    parser.parse("copy event \"Meeting\" when 2025-04-01T10:00 --target "
            + "\"WorkCal\" to 2025-04-02T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopySingleEventWrongTargetKeyword() {
    parser.parse("copy event \"Meeting\" on 2025-04-01T10:00 --wrong "
            + "\"WorkCal\" to 2025-04-02T11:00");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopySingleEventWrongToKeyword() {
    parser.parse("copy event \"Meeting\" on 2025-04-01T10:00 --target "
            + "\"WorkCal\" into 2025-04-02T11:00");
  }

  @Test
  public void testCreateEventParsesDescriptionLocationPublic() {
    String input = "create event \"Team Sync\" from 2025-04-01T10:00 to "
            + "2025-04-01T11:00 description \"Sprint planning\" location \"Room 101\" public";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof CreateEventCommand);
  }

  @Test
  public void testCreateEventParsesPrivateOnly() {
    String input = "create event \"Private Event\" from 2025-04-01T12:00 "
            + "to 2025-04-01T13:00 private";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof CreateEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithUnknownPropertyToken() {
    String input = "create event \"Bug Bash\" from 2025-04-01T14:00 to 2025-04-01T15:00 foobar";
    parser.parse(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventMissingLocationValue() {
    String input = "create event \"Bug Bash\" from 2025-04-01T14:00 to 2025-04-01T15:00 location";
    parser.parse(input);
  }

  @Test
  public void testParseEditCalendarCommandValid() {
    String input = "edit calendar --name School --property timezone America/New_York";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof EditCalendarCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarCommandMissingNameValue() {
    String input = "edit calendar --name --property timezone America/New_York";
    parser.parse(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarCommandMissingPropertyValue() {
    String input = "edit calendar --name School --property";
    parser.parse(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarCommandMissingNewValue() {
    String input = "edit calendar --name School --property timezone";
    parser.parse(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseEditCalendarCommandWithNoFlags() {
    String input = "edit calendar School timezone America/New_York";
    parser.parse(input);
  }

  @Test
  public void testCreateEventFromToValid() {
    String input = "create event \"Meeting\" from 2025-04-01T10:00 to 2025-04-01T11:00";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof CreateEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventFromToEndBeforeStart() {
    String input = "create event \"Meeting\" from 2025-04-01T10:00 to 2025-04-01T09:00";
    parser.parse(input);
  }

  @Test
  public void testCreateEventFromOnly() {
    String input = "create event \"Holiday\" from 2025-04-01T00:00";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof CreateEventCommand);
  }

  @Test
  public void testCreateEventOnDate() {
    String input = "create event \"Birthday\" on 2025-04-02T00:00";
    ICommand command = parser.parse(input);
    assertTrue(command instanceof CreateEventCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventInvalidKeyword() {
    String input = "create event \"Something\" at 2025-04-01T09:00";
    parser.parse(input);
  }

  @Test
  public void testExportCalendarCommandExecutesSuccessfully() {
    controller.processCommand("create calendar --name exportCal --timezone UTC");
    controller.processCommand("use calendar --name exportCal");
    controller.processCommand("create event \"Meeting\" "
            + "from 2025-04-01T10:00 to 2025-04-01T11:00");
    boolean result = controller.processCommand("export cal exportTest.csv");

    assertTrue(result);
    String msg = view.getLastMessage();
    assertTrue(msg.contains("Calendar exported successfully to:"));
  }

  @Test
  public void testExportCalendarCommandExecutionFailsWithIllegalArgument() {
    controller.processCommand("create calendar --name illegalCal --timezone UTC");
    controller.processCommand("use calendar --name illegalCal");

    boolean result = controller.processCommand("export cal events.invalid");
    assertFalse(result);
    assertTrue(view.getLastMessage().
            contains("Parsing Error: Exported file must have a .csv extension"));
  }

  @Test
  public void testExportCalendarCommandHandlesIOException() {
    ICalendarModel model = new CalendarModel("testCal", ZoneId.of("UTC"));
    view.displayMessage("");
    ExportCalendarCommand command = new ExportCalendarCommand("test.csv") {
      @Override
      public boolean execute(ICalendarModel m, ICalendarView v) {
        try {
          throw new IOException("Disk error");
        } catch (IOException e) {
          v.displayError("Failed to export calendar: " + e.getMessage());
          return false;
        }
      }
    };

    boolean result = command.execute(model, view);
    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Failed to export calendar: Disk error"));
  }


  @Test
  public void testExportCalendarCommandHandlesIllegalArgumentException() {
    ICalendarModel model = new CalendarModel("testCal", ZoneId.of("UTC"));
    view.displayMessage("");
    ExportCalendarCommand command = new ExportCalendarCommand("test.csv") {
      @Override
      public boolean execute(ICalendarModel m, ICalendarView v) {
        try {
          throw new IllegalArgumentException("Unsupported file format");
        } catch (IllegalArgumentException e) {
          v.displayError("Export Error: " + e.getMessage());
          return false;
        }
      }
    };

    boolean result = command.execute(model, view);
    assertFalse(result);
    assertTrue(view.getLastMessage().contains("Export Error: Unsupported file format"));
  }


  @Test
  public void testParseCopySingleEventValid() {
    List<String> tokens = Arrays.asList("copy", "event", "\"Meeting\"", "on",
            "2025-04-01T10:00", "--target", "\"WorkCal\"", "to", "2025-04-02T11:00");

    ICalendarModel targetCal = new CalendarModel("WorkCal",
            ZoneId.of("America/New_York"));
    calendarManager.addCalendar("WorkCal", ZoneId.of("America/New_York"));

    ICommand command = parser.parse("copy event \"Meeting\" on "
            + "2025-04-01T10:00 --target \"WorkCal\" to 2025-04-02T11:00");
    assertTrue(command instanceof CopySingleEventCommand);
  }


  /**
   * A minimal test view implementation for CommandParser testing.
   */
  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = "";

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      if (events == null || events.isEmpty()) {
        this.lastMessage = "No events found";
      } else {
        this.lastMessage = "Displaying " + events.size() + " events";
      }
    }

    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayError(String error) {
      this.lastMessage = error;
    }

    @Override
    public void run() {
      return;
    }

    @Override
    public void setInput(Readable in) {
      ICalendarView.super.setInput(in);
    }

    @Override
    public void setOutput(Appendable out) {
      ICalendarView.super.setOutput(out);
    }

    public String getLastMessage() {
      return lastMessage;
    }
  }
}


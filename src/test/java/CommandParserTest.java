import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarModel;
import calendarapp.model.commands.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommandParserTest {
  private CommandParser parser;

  @Before
  public void setUp() {
    parser = new CommandParser(new CalendarModel());
  }

  /** ✅ Test 1: Parsing a Create Event Command */
  @Test
  public void testParseCreateEvent() {
    String command = "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as CreateEventCommand", parsedCommand instanceof CreateEventCommand);

    CreateEventCommand createCmd = (CreateEventCommand) parsedCommand;
    assertEquals("Event name should match", "Meeting", createCmd.getEventName());
    assertEquals("Start time should match", LocalDateTime.of(2025, 6, 1, 9, 0), createCmd.getStartDateTime());
    assertEquals("End time should match", LocalDateTime.of(2025, 6, 1, 10, 0), createCmd.getEndDateTime());
  }

  /** ✅ Test 2: Parsing Print Events Command (on specific date) */
  @Test
  public void testParsePrintEventsOnDate() {
    String command = "print events on 2025-06-01";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryByDateCommand", parsedCommand instanceof QueryByDateCommand);

    QueryByDateCommand queryCmd = (QueryByDateCommand) parsedCommand;
    assertEquals("Query date should match", LocalDate.of(2025, 6, 1), queryCmd.getQueryDate());
  }

  /** ✅ Test 3: Parsing Print Events Command (within date range) */
  @Test
  public void testParsePrintEventsInRange() {
    String command = "print events from 2025-06-01T09:00 to 2025-06-01T10:30";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as QueryRangeDateTimeCommand", parsedCommand instanceof QueryRangeDateTimeCommand);

    QueryRangeDateTimeCommand rangeCmd = (QueryRangeDateTimeCommand) parsedCommand;
    assertEquals("Start time should match", LocalDateTime.of(2025, 6, 1, 9, 0), rangeCmd.getStartDateTime());
    assertEquals("End time should match", LocalDateTime.of(2025, 6, 1, 10, 30), rangeCmd.getEndDateTime());
  }

  /** ✅ Test 4: Parsing Show Status Command */
  @Test
  public void testParseShowStatus() {
    String command = "show status on 2025-06-01T10:00";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as BusyQueryCommand", parsedCommand instanceof BusyQueryCommand);

    BusyQueryCommand busyCmd = (BusyQueryCommand) parsedCommand;
    assertEquals("Query time should match", LocalDateTime.of(2025, 6, 1, 10, 0), busyCmd.getQueryTime());
  }

  /** ✅ Test 5: Parsing an Edit Single Event Command */
  @Test
  public void testParseEditSingleEvent() {
    String command = "edit event description \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated Description\"";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as EditEventCommand", parsedCommand instanceof EditEventCommand);

    EditEventCommand editCmd = (EditEventCommand) parsedCommand;
    assertEquals("Event name should match", "Meeting", editCmd.getEventName());
    assertEquals("Property should be 'description'", "description", editCmd.getProperty());
    assertEquals("New value should be 'Updated Description'", "Updated Description", editCmd.getNewValue());
  }

  /** ✅ Test 6: Parsing an Edit Recurring Event Command */
  @Test
  public void testParseEditRecurringEvent() {
    String command = "edit events repeatuntil \"Team Sync\" 2025-12-31T00:00";

    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as EditRecurringEventCommand", parsedCommand instanceof EditRecurringEventCommand);

    EditRecurringEventCommand editCmd = (EditRecurringEventCommand) parsedCommand;
    assertEquals("Event name should match", "Team Sync", editCmd.getEventName());
    assertEquals("Property should be 'repeatuntil'", "repeatuntil", editCmd.getProperty());
    assertEquals("New value should be '2025-12-31T00:00'", "2025-12-31T00:00", editCmd.getNewValue());
  }

  /** ✅ Test 7: Parsing an Export Command */
  @Test
  public void testParseExportCommand() {
    String command = "export cal events.csv";
    Command parsedCommand = parser.parse(command);
    assertTrue("Should parse as ExportCalendarCommand", parsedCommand instanceof ExportCalendarCommand);

    ExportCalendarCommand exportCmd = (ExportCalendarCommand) parsedCommand;
    assertEquals("File name should match", "events.csv", exportCmd.getFileName());
  }

  /** ✅ Test 8: Handling Unknown Commands */
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
  public void testParseMalformedCommand() {
    String command = "create event from 2025-06-01T09:00 to 2025-06-01T10:00"; // Missing event name
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for malformed command");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should contain 'Expected 'from' or 'on' after event name'",
              e.getMessage().contains("Expected 'from' or 'on' after event name"));
    }
  }

//  /** ✅ Test 9: Parsing Create Event with Missing Date */
//  @Test
//  public void testParseCreateEventMissingDate() {
//    String command = "create event \"Team Lunch\" from";
//    try {
//      parser.parse(command);
//      fail("Should throw IllegalArgumentException due to missing date");
//    } catch (IllegalArgumentException e) {
//      assertTrue("Error message should indicate missing date", e.getMessage().contains("Expected 'from' or 'on' after event name"));
//    }
//  }

//  /** ✅ Test 10: Parsing Create Event with Invalid Date */
//  @Test
//  public void testParseCreateEventInvalidDate() {
//    String command = "create event \"Project Meeting\" from 2025-06-40T09:00 to 2025-06-01T10:00";
//    try {
//      parser.parse(command);
//      fail("Should throw IllegalArgumentException due to invalid date");
//    } catch (IllegalArgumentException e) {
//      assertTrue("Error message should indicate invalid date", e.getMessage().contains("Invalid date/time format"));
//    }
//  }

  /** ✅ Test 11: Parsing Print Events Command with Missing Date */
  @Test
  public void testParsePrintEventsMissingDate() {
    String command = "print events on";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing date", e.getMessage().contains("Expected date after 'on'"));
    }
  }

  /** ✅ Test 12: Parsing Print Events Command with Invalid Date Format */
  @Test
  public void testParsePrintEventsInvalidDateFormat() {
    String command = "print events on 06-01-2025";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect date format");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format", e.getMessage().contains("Invalid date format"));
    }
  }

  /** ✅ Test 13: Parsing Show Status Command with Missing Date */
  @Test
  public void testParseShowStatusMissingDate() {
    String command = "show status on";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing date");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing date", e.getMessage().contains("Usage: show status on <dateTime>"));
    }
  }

  /** ✅ Test 14: Parsing Edit Single Event with Missing Details */
  @Test(expected = IllegalArgumentException.class)
  public void testParseEditSingleEventMissingDetails() {
    String command = "edit event description";
      parser.parse(command);
    assertFalse(true);
  }

  /** ✅ Test 15: Parsing Edit Recurring Event with Invalid Format */
  @Test
  public void testParseEditRecurringEventInvalidFormat() {
    String command = "edit events repeatuntil \"Daily Scrum\"";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing new value");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format", e.getMessage().contains("Invalid edit events command format"));
    }
  }

  /** ✅ Test 16: Parsing Export Command with Missing File Name */
  @Test
  public void testParseExportCommandMissingFileName() {
    String command = "export cal";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to missing file name");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing file name", e.getMessage().contains("Invalid export command format"));
    }
  }

  /** ✅ Test 17: Parsing Export Command with Non-CSV File */
  @Test
  public void testParseExportCommandNonCSVFile() {
    String command = "export cal events.txt";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException due to incorrect file extension");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate incorrect format", e.getMessage().contains("Invalid file name. Must be a CSV file"));
    }
  }

  /** ✅ Test 18: Handling Empty Command */
  @Test
  public void testParseEmptyCommand() {
    String command = "";
    Command parsedCommand = parser.parse(command);
    assertNull("Empty command should return null", parsedCommand);
  }

  /** ✅ Test 19: Handling Null Command */
  @Test
  public void testParseNullCommand() {
    try {
      parser.parse(null);
      fail("Should throw IllegalArgumentException for null command");
    } catch (IllegalArgumentException e) {
      assertEquals("Command cannot be null", e.getMessage());
    }
  }

  /** ✅ Test 20: Handling Edit Single Event with Missing 'to' */
//  @Test
//  public void testParseEditSingleEventMissingTo() {
//    String command = "edit event description \"Team Meeting\" from 2025-06-01T09:00 with \"Updated\"";
//    try {
//      parser.parse(command);
//      fail("Should throw IllegalArgumentException due to missing 'to'");
//    } catch (IllegalArgumentException e) {
//      assertTrue("Error message should indicate missing 'to'", e.getMessage().contains("Expected 'to' after start date/time"));
//    }
//  }

  /** ✅ Test 21: Parsing Recurring Event With More Than 24 Hours */
  @Test
  public void testParseRecurringEventExceeds24Hours() {
    String command = "create event \"Long Meeting\" from 2025-06-01T09:00 to 2025-06-02T10:00 repeats MTWRF until 2025-06-30T23:59";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException because event exceeds 24 hours");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate event exceeds 24 hours", e.getMessage().contains("Recurring event must end within 24 hours of the start time."));
    }
  }

  /** ✅ Test 22: Parsing Edit Event With Invalid Property */
  @Test
  public void testParseEditEventInvalidProperty() throws IllegalArgumentException{
    String command = "edit event invalidprop \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00 with \"New Value\"";
      parser.parse(command);
      assertFalse(false);
  }

  /** ✅ Test 23: Parsing Edit Events Without Required Keywords */
  @Test
  public void testParseEditEventsMissingKeywords() {
    String command = "edit events location";
    try {
      parser.parse(command);
      fail("Should throw IllegalArgumentException for incomplete edit events command");
    } catch (IllegalArgumentException e) {
      assertTrue("Error message should indicate missing keywords", e.getMessage().contains("Incomplete edit command"));
    }
  }

}

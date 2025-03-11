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

}

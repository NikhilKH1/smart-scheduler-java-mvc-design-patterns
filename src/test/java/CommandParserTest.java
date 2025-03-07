import org.junit.Test;
import static org.junit.Assert.*;

import calendarapp.controller.CommandParser;
import calendarapp.model.Command;
import calendarapp.model.CreateEventCommand;

public class CommandParserTest {
  @Test
  public void testCreateSingleEventCommand() {
    CommandParser parser = new CommandParser();
    String command = "create event \"Meeting\" from 2025-05-10T10:00 to 2025-05-10T11:00";
    Command cmd = parser.parse(command);

    assertTrue(cmd instanceof CreateEventCommand);
    CreateEventCommand createCmd = (CreateEventCommand) cmd;

    assertEquals("Meeting", createCmd.getEventName());
    assertFalse("Should not be recurring", createCmd.isRecurring());
    assertEquals("2025-05-10T10:00", createCmd.getStartDateTime().toString());
    assertEquals("2025-05-10T11:00", createCmd.getEndDateTime().toString());
  }

  @Test
  public void testCreateRecurringEventCommand() {
    CommandParser parser = new CommandParser();
    String command = "create event \"Standup\" from 2025-05-01T09:00 to 2025-05-01T09:15 repeats MW for 3 times";
    Command cmd = parser.parse(command);

    assertTrue(cmd instanceof CreateEventCommand);
    CreateEventCommand cec = (CreateEventCommand) cmd;
    assertTrue("Should be recurring", cec.isRecurring());
    assertEquals("MW", cec.getWeekdays());
    assertEquals(3, cec.getRepeatCount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCommand() {
    CommandParser parser = new CommandParser();
    // Missing 'event' keyword
    parser.parse("create \"BadCommand\" from 2025-05-10T10:00 to 2025-05-10T11:00");
  }

  @Test
  public void testAllDayEventCommand() {
    CommandParser parser = new CommandParser();
    String command = "create event \"Holiday\" on 2025-12-25T00:00";
    Command cmd = parser.parse(command);
    assertTrue(cmd instanceof CreateEventCommand);
    CreateEventCommand createCmd = (CreateEventCommand) cmd;
    assertTrue(createCmd.isAllDay());
  }
}

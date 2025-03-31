import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import calendarapp.controller.ICalendarController;
import calendarapp.controller.IOCalendarController;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class IOCalendarControllerTest {

  private StringBuilder outputLog;
  private StringReader input;
  private IOCalendarController controller;

  /**
   * Simple mock that tracks calls and returns true/false based on content.
   */
  private static class MockController implements ICalendarController {
    private final StringBuilder log;
    private final boolean returnValue;

    public MockController(StringBuilder log, boolean returnValue) {
      this.log = log;
      this.returnValue = returnValue;
    }

    @Override
    public boolean processCommand(String commandInput) {
      log.append("Received: ").append(commandInput).append("\n");
      return returnValue;
    }
  }

  @Before
  public void setup() {
    outputLog = new StringBuilder();
  }

  @Test
  public void testRunProcessesValidCommands() throws IOException {
    input = new StringReader("create calendar --name TestCal --timezone UTC\nexit");
    MockController mock = new MockController(outputLog, true);

    controller = new IOCalendarController(input, outputLog, mock);
    controller.run();

    String output = outputLog.toString();
    assertTrue(output.contains("Enter commands"));
    assertTrue(output.contains("Received: create calendar --name TestCal --timezone UTC"));
  }

  @Test
  public void testRunHandlesFailedCommands() throws IOException {
    input = new StringReader("bad command\nexit");
    MockController mock = new MockController(outputLog, false);

    controller = new IOCalendarController(input, outputLog, mock);
    controller.run();

    String output = outputLog.toString();
    assertTrue(output.contains("Enter commands"));
    assertTrue(output.contains("Received: bad command"));
    assertTrue(output.contains("Command failed: bad command"));
  }

  @Test
  public void testRunSkipsEmptyLines() throws IOException {
    input = new StringReader("\n\nexit\n");
    MockController mock = new MockController(outputLog, true);

    controller = new IOCalendarController(input, outputLog, mock);
    controller.run();
    assertEquals("Enter commands (type 'exit' to quit):\n", outputLog.toString());

  }

  @Test
  public void testRunStopsOnExit() throws IOException {
    input = new StringReader("exit\ncreate calendar --name ShouldNotRun --timezone UTC\n");
    MockController mock = new MockController(outputLog, true);

    controller = new IOCalendarController(input, outputLog, mock);
    controller.run();

    String output = outputLog.toString();
    assertTrue(output.contains("Enter commands"));
    assertTrue(!output.contains("ShouldNotRun"));
  }
}

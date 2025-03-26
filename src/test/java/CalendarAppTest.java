import calendarapp.CalendarApp;
import calendarapp.controller.ICalendarController;
import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.io.ICommandSource;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * JUnit tests for the CalendarApp class.
 */
public class CalendarAppTest {

  private SecurityManager originalSecurityManager;

  private ICalendarView view;

  private ICalendarController controller;

  private static class NoExitSecurityManager extends SecurityManager {
    private Integer status;

    public Integer getStatus() {
      return status;
    }

    @Override
    public void checkPermission(java.security.Permission perm) {
      // method to checkPermission
    }


    @Override
    public void checkExit(int status) {
      this.status = status;
      throw new SecurityException("Intercepted System.exit(" + status + ")");
    }
  }

  @Before
  public void setUp() {
    ICalendarManager manager;
    CommandParser parser;
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());

    manager = new CalendarManager();
    view = new TestCalendarView();
    parser = new CommandParser(manager);
    controller = new CalendarController(manager, view, parser);
  }

  @After
  public void tearDown() {
    System.setSecurityManager(originalSecurityManager);
  }

  /**
   * A simple ICommandSource implementation for testing.
   */
  private static class TestCommandSource implements ICommandSource {
    private final Queue<String> commands;

    public TestCommandSource(Queue<String> commands) {
      this.commands = commands;
    }

    @Override
    public String getNextCommand() {
      return commands.poll();
    }

    @Override
    public void close() {
      // method to close
    }
  }

  @Test
  public void testInteractiveModeProcessesNonExitCommand() {
    Queue<String> commands = new LinkedList<>();
    commands.add("create calendar --name TestCal --timezone UTC");
    commands.add("exit");
    ICommandSource testSource = new CalendarAppTest.TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);

    String lastMessage = ((TestCalendarView) view).getLastMessage();
    assertTrue(lastMessage.contains("Calendar created: TestCal (UTC)"));
  }


  @Test
  public void testHeadlessModeCommandFailsTriggersExit() {
    Queue<String> commands = new LinkedList<>();
    commands.add("use calendar --name NonExistent");
    ICommandSource testSource = new CalendarAppTest.TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected SecurityException due to System.exit(1)");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("Intercepted System.exit(1)"));
    }
  }

  @Test
  public void testHeadlessModeThrowsExceptionTriggersExit() {
    Queue<String> commands = new LinkedList<>();
    commands.add("some command");
    ICommandSource testSource = new CalendarAppTest.TestCommandSource(commands);

    ICalendarController faultyController = new ICalendarController() {
      @Override
      public boolean processCommand(String command) {
        throw new RuntimeException("Simulated failure");
      }
    };

    try {
      CalendarApp.runHeadlessMode(faultyController, view, testSource);
      fail("Expected System.exit due to exception");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("Intercepted System.exit(1)"));
    }
  }


  @Test
  public void testInteractiveModeExitImmediately() {
    Queue<String> commands = new LinkedList<>();
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);

    assertNull("Expected no more commands after 'exit' command",
            testSource.getNextCommand());
  }

  @Test
  public void testInteractiveModeWithMultipleCommands() {
    Queue<String> commands = new LinkedList<>();
    commands.add("create calendar --name Work --timezone UTC");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);

    String lastMessage = ((TestCalendarView) view).getLastMessage();
    assertTrue(lastMessage.contains("Calendar created: Work (UTC)"));
  }

  @Test
  public void testInteractiveModeWithEmptyCommand() {
    Queue<String> commands = new LinkedList<>();
    commands.add("");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);

    String lastMessage = ((TestCalendarView) view).getLastMessage();
    assertTrue(lastMessage.toLowerCase().contains("parsing error"));
  }

  @Test
  public void testHeadlessModeWithEmptyCommandIgnored() {
    Queue<String> commands = new LinkedList<>();
    commands.add("");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected System.exit");
    } catch (SecurityException e) {
      assertTrue(e.getMessage().contains("Intercepted System.exit(0)"));
    }
  }

  @Test
  public void testHeadlessModeInvalidCommandExitsWithError() {
    Queue<String> commands = new LinkedList<>();
    commands.add("this is invalid command");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected System.exit due to invalid command");
    } catch (SecurityException e) {
      assertTrue(e.getMessage().contains("Intercepted System.exit(1)"));
    }
  }

  @Test
  public void testHeadlessModeExceptionInCommandExecution() {
    Queue<String> commands = new LinkedList<>();
    commands.add("create calendar");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected System.exit due to exception");
    } catch (SecurityException e) {
      assertTrue(e.getMessage().contains("Intercepted System.exit(1)"));
    }
  }

  @Test
  public void testInteractiveModeExitsOnEOF() {
    Queue<String> commands = new LinkedList<>();
    ICommandSource testSource = new TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);

    assertNull(testSource.getNextCommand());
  }

  @Test
  public void testHeadlessModeMultipleCommands() {
    Queue<String> commands = new LinkedList<>();
    commands.add("create calendar --name Home --timezone UTC");
    commands.add("use calendar --name Home");
    commands.add("create event \"Dinner\" from 2025-06-01T19:00 to 2025-06-01T20:00");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected System.exit due to 'exit' command");
    } catch (SecurityException e) {
      assertTrue(e.getMessage().contains("Intercepted System.exit(0)"));
    }
  }


  @Test
  public void testHeadlessModeExitSuccessfully() {
    Queue<String> commands = new LinkedList<>();
    commands.add("create calendar --name TestCal --timezone Europe/Paris");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected SecurityException due to System.exit");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("Intercepted System.exit(0)"));
    }
  }

  @Test
  public void testHeadlessModeErrorExit() {
    Queue<String> commands = new LinkedList<>();
    commands.add("invalid command");
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    try {
      CalendarApp.runHeadlessMode(controller, view, testSource);
      fail("Expected SecurityException due to System.exit with error code");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("Intercepted System.exit(1)"));
    }
  }

  @Test
  public void testAddDuplicateSingleEvent() {
    controller.processCommand("create calendar --name TestCal "
            + "--timezone Europe/Paris");
    controller.processCommand("use calendar --name TestCal");

    boolean firstResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );
    assertTrue(firstResult);

    boolean secondResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );
    assertFalse(secondResult);
  }

  @Test
  public void testRunHeadlessModeProcessesFileAndPrintsEvents() throws IOException {
    File tempFile = File.createTempFile("testCommands", ".txt");
    FileWriter writer = new FileWriter(tempFile);

    writer.write("create calendar --name TestCal --timezone Europe/Paris\n");
    writer.write("use calendar --name TestCal\n");
    writer.write("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00\n");
    writer.write("print events on 2025-06-01\n");
    writer.write("exit\n");
    writer.close();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    try {
      CalendarApp.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});
      fail("Expected SecurityException due to System.exit");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("Intercepted System.exit(0)"));
    }

    String output = outputStream.toString();
    assertTrue(output.contains("Event created successfully"));
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Events on 2025-06-01"));

    tempFile.delete();
  }

  /**
   * Mock view class for testing.
   */
  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = "";
    private List<ICalendarEvent> lastDisplayedEvents = null;

    @Override
    public void displayMessage(String message) {
      lastMessage = message;
    }

    @Override
    public void displayError(String errorMessage) {
      lastMessage = "ERROR: " + errorMessage;
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      lastDisplayedEvents = events;
      lastMessage = "Displaying " + events.size() + " events";
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public List<ICalendarEvent> getLastDisplayedEvents() {
      return lastDisplayedEvents;
    }
  }

}
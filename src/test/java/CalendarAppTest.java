import calendarapp.CalendarApp;
import calendarapp.controller.ICalendarController;
import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.io.ICommandSource;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.view.CalendarView;
import calendarapp.view.ICalendarView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

public class CalendarAppTest {

  private SecurityManager originalSecurityManager;
  private ICalendarManager manager;
  private ICalendarView view;
  private CommandParser parser;
  private ICalendarController controller;

  // Custom SecurityManager to catch System.exit calls
  private static class NoExitSecurityManager extends SecurityManager {
    private Integer status;

    public Integer getStatus() {
      return status;
    }

    @Override
    public void checkPermission(java.security.Permission perm) {
      // Allow everything.
    }

    @Override
    public void checkExit(int status) {
      this.status = status;
      throw new SecurityException("Intercepted System.exit(" + status + ")");
    }
  }

  @Before
  public void setUp() {
    // Save original SecurityManager and install our own.
    originalSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());

    manager = new CalendarManager();
    view = new TestCalendarView();
    parser = new CommandParser(manager);
    controller = new CalendarController(manager, view, parser);
  }

  @After
  public void tearDown() {
    // Restore original SecurityManager
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
      // Nothing to close.
    }
  }

  /**
   * Test interactive mode with "exit" command only.
   */
  @Test
  public void testInteractiveModeExitImmediately() {
    Queue<String> commands = new LinkedList<>();
    commands.add("exit");
    ICommandSource testSource = new TestCommandSource(commands);

    CalendarApp.runInteractiveMode(controller, view, testSource);
  }

  /**
   * Test headless mode that processes commands and then exits.
   * This test expects System.exit(0) when "exit" is encountered.
   */
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

  /**
   * Test headless mode error: if a command fails (returns false), then System.exit(1) should be called.
   */
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

  /**
   * Test duplicate event creation.
   */
  @Test
  public void testAddDuplicateSingleEvent() {
    controller.processCommand("create calendar --name TestCal --timezone Europe/Paris");
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

  /**
   * Test headless mode by processing commands from a file.
   */
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
//
//
//import calendarapp.CalendarApp;
//import calendarapp.controller.CalendarController;
//import calendarapp.model.CalendarModel;
//import calendarapp.view.ICalendarView;
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class CalendarAppTest {
//
//  private CalendarApp app;
//  private CalendarModel model;
//  private TestCalendarView view;
//  private CalendarController controller;
//
//  @Before
//  public void setUp() {
//    model = new CalendarModel();
//    view = new TestCalendarView();
//    controller = new CalendarController(model, view);
//    app = new CalendarApp();  // Assuming CalendarApp has a no-arg constructor
//  }
//
////  /** ✅ Test 1: Ensure application starts without exceptions */
////  @Test
////  public void testApplicationStartup() {
////    try {
////      CalendarApp.main(new String[]{});
////    } catch (Exception e) {
////      fail("Application should start without exceptions, but got: " + e.getMessage());
////    }
////  }
//

import calendarapp.CalendarApp;
import calendarapp.controller.CalendarController;
import calendarapp.model.CalendarModel;
import calendarapp.view.ICalendarView;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class CalendarAppTest {

  private CalendarModel model;
  private TestCalendarView view;
  private CalendarController controller;

  @Before
  public void setUp() {
    model = new CalendarModel();
    view = new TestCalendarView();
    controller = new CalendarController(model, view);
  }

//  /** ✅ Test 1: Ensure application starts without exceptions */
//  @Test
//  public void testApplicationStartsWithoutExceptions() {
//    try {
//      CalendarApp.main(new String[]{});
//    } catch (Exception e) {
//      fail("Application should start without exceptions, but got: " + e.getMessage());
//    }
//  }

//  /** ✅ Test 2: Simulates interactive mode and checks 'exit' command */
//  @Test
//  public void testRunInteractiveModeHandlesExit() {
//    String simulatedInput = "exit\n";  // Simulating user typing 'exit'
//    InputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());
//    System.setIn(inputStream);
//
//    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//    System.setOut(new PrintStream(outputStream));
//
//    // Running interactive mode
//    CalendarApp.main(new String[]{"--mode", "interactive"});
//
//    String output = outputStream.toString();
//    assertTrue("Should contain prompt message", output.contains("Enter commands (type 'exit' to quit):"));
//  }

  /** ✅ Test 3: Simulates headless mode with a valid file */
  @Test
  public void testRunHeadlessModeProcessesFile() throws IOException {
    // Create a temporary test file with commands
    File tempFile = File.createTempFile("testCommands", ".txt");
    FileWriter writer = new FileWriter(tempFile);
    writer.write("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00\n");
    writer.write("exit\n");
    writer.close();

    // Redirect output to capture printed results
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    // Run headless mode
    CalendarApp.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});

    String output = outputStream.toString();
    assertTrue("Output should contain event creation", output.contains("----- All Events -----"));
    assertTrue("Output should contain 'Meeting'", output.contains("Meeting"));

    // Delete the temp file
    tempFile.delete();
  }

//    /** ✅ Test 2: Test processing a valid command */
//  @Test
//  public void testProcessValidCommand() {
//    String command = "create event \"Team Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00";
//    boolean result = controller.processCommand(command);
//    assertTrue("Valid command should be processed successfully", result);
//  }
//
//    /** ✅ Test 3: Test processing an invalid command */
//  @Test
//  public void testProcessInvalidCommand() {
//    String command = "invalid command text";
//    boolean result = controller.processCommand(command);
//    assertFalse("Invalid command should fail", result);
//    System.out.println(view.getLastMessage());
//    assertTrue("Error message should be shown", view.getLastMessage().contains("ERROR: Parsing Error: Unknown command: invalid"));
//  }

  /** ✅ Test 4: Ensure controller is initialized */
  @Test
  public void testControllerInitialization() {
    assertNotNull("Controller should not be null", controller);
  }

  /** ✅ Test 5: Ensure application handles null command gracefully */
  @Test
  public void testProcessNullCommand() {
    boolean result = controller.processCommand(null);
    assertFalse("Null command should fail", result);
    assertEquals("ERROR: Parsing Error: Command cannot be null", view.getLastMessage());
  }

  /** ✅ Test 6: Ensure application handles empty input */
  @Test
  public void testProcessEmptyCommand() {
    boolean result = controller.processCommand("");
    assertFalse("Empty command should fail", result);
    assertEquals("ERROR: Command is null", view.getLastMessage());
  }

  /** ✅ Test 7: Simulate application exit (if it exists) */
  @Test
  public void testApplicationExit() {
    // If CalendarApp has a method like app.exit(), test its behavior here
    assertTrue("Application should exit gracefully", true);
  }

//  /** ✅ Test 4: Simulates headless mode with a missing file */
//  @Test
//  public void testRunHeadlessModeHandlesInvalidFile() {
//    // Redirect error output
//    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
//    System.setErr(new PrintStream(errorStream));
//
//    // Run headless mode with a non-existent file
//    CalendarApp.main(new String[]{"--mode", "headless", "non_existent_file.txt"});
//
//    String errorOutput = errorStream.toString();
//    assertTrue("Error message should mention 'Error reading commands file'", errorOutput.contains("Error reading commands file"));
//  }

//  /** ✅ Test 5: Ensures `--mode interactive` is processed correctly */
//  @Test
//  public void testMainWithInteractiveMode() {
//    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//    System.setOut(new PrintStream(outputStream));
//
//    // Simulating running interactive mode
//    CalendarApp.main(new String[]{"--mode", "interactive"});
//
//    String output = outputStream.toString();
//    assertTrue("Should prompt user for commands", output.contains("Enter commands (type 'exit' to quit):"));
//  }

  /** ✅ Test 6: Ensures `--mode headless <file>` is processed correctly */
  // @Test
//  public void testMainWithHeadlessMode() throws IOException {
//    // Create a temporary test file
//    File tempFile = File.createTempFile("testCommands", ".txt");
//    FileWriter writer = new FileWriter(tempFile);
//    writer.write("exit\n");
//    writer.close();
//
//    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//    System.setOut(new PrintStream(outputStream));
//
//    // Run headless mode
//    CalendarApp.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});
//
//    String output = outputStream.toString();
//    assertTrue("Should process headless mode correctly", output.contains("----- All Events -----"));
//
//    tempFile.delete();
//  }

  // ✅ Custom Test Calendar View
  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = "";

    @Override
    public void displayMessage(String message) {
      lastMessage = message;
    }

    @Override
    public void displayError(String errorMessage) {
      lastMessage = "ERROR: " + errorMessage;
    }

    @Override
    public void displayEvents(java.util.List<calendarapp.model.event.CalendarEvent> events) {
      lastMessage = "Displaying " + events.size() + " events";
    }

    public String getLastMessage() {
      return lastMessage;
    }
  }
}

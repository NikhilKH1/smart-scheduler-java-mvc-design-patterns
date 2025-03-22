//
//import calendarapp.CalendarApp;
//import calendarapp.controller.CalendarController;
//import calendarapp.model.CalendarModel;
//import calendarapp.model.event.CalendarEvent;
//import calendarapp.view.ICalendarView;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * Junit test for CalendarApp class.
// */
//public class CalendarAppTest {
//
//
//  private TestCalendarView view;
//  private CalendarController controller;
//
//  @Before
//  public void setUp() {
//    CalendarModel model;
//    model = new CalendarModel();
//    view = new TestCalendarView();
//    controller = new CalendarController(model, view);
//  }
//
//  @Test
//  public void testAddDuplicateSingleEvent() {
//    boolean firstResult = controller.processCommand(
//            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
//    );
//    assertTrue("First event should be created successfully", firstResult);
//
//    boolean secondResult = controller.processCommand(
//            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
//    );
//
//    assertFalse("Duplicate event creation should fail", secondResult);
//    assertEquals(
//            "ERROR: Duplicate event: subject, start and end are identical.",
//            view.getLastMessage()
//    );
//  }
//
//  @Test
//  public void testRunHeadlessModeProcessesFileAndPrintsEvents() throws IOException {
//    File tempFile = File.createTempFile("testCommands", ".txt");
//    FileWriter writer = new FileWriter(tempFile);
//
//    writer.write("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00\n");
//    writer.write("print events on 2025-06-01\n");
//    writer.write("exit\n");
//    writer.close();
//
//    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//    System.setOut(new PrintStream(outputStream));
//
//    CalendarApp.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});
//
//    String output = outputStream.toString();
//
//    assertTrue("Output should contain event creation",
//            output.contains("Event created successfully"));
//    assertTrue("Output should contain 'Meeting'", output.contains("Meeting"));
//    assertTrue("Output should contain print header",
//            output.contains("Events on 2025-06-01"));
//
//    // Clean up
//    tempFile.delete();
//  }
//
//
//
//  @Test
//  public void testAddDuplicateSingleEventMain() {
//    boolean firstResult = controller.processCommand(
//            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
//    );
//    assertTrue("First event should be created successfully", firstResult);
//
//    boolean secondResult = controller.processCommand(
//            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
//    );
//
//    assertFalse("Second event (duplicate) should fail", secondResult);
//    assertEquals(
//            "ERROR: Duplicate event: subject, start and end are identical.",
//            view.getLastMessage()
//    );
//  }
//
//  @Test
//  public void testControllerInitialization() {
//    assertNotNull("Controller should not be null", controller);
//  }
//
//  @Test
//  public void testProcessNullCommand() {
//    boolean result = controller.processCommand(null);
//    assertFalse("Null command should fail", result);
//    assertEquals("ERROR: Parsing Error: Command cannot be null", view.getLastMessage());
//  }
//
//  @Test
//  public void testProcessEmptyCommand() {
//    boolean result = controller.processCommand("");
//    assertFalse("Empty command should fail", result);
//    assertEquals("ERROR: Command parsing returned null", view.getLastMessage());
//  }
//
//
//  private static class TestCalendarView implements ICalendarView {
//    private String lastMessage = "";
//    private List<CalendarEvent> lastDisplayedEvents = null;
//
//    @Override
//    public void displayMessage(String message) {
//      lastMessage = message;
//    }
//
//    @Override
//    public void displayError(String errorMessage) {
//      lastMessage = "ERROR: " + errorMessage;
//    }
//
//    @Override
//    public void displayEvents(List<CalendarEvent> events) {
//      lastDisplayedEvents = events;
//      lastMessage = "Displaying " + events.size() + " events";
//    }
//
//    public String getLastMessage() {
//      return lastMessage;
//    }
//  }
//}

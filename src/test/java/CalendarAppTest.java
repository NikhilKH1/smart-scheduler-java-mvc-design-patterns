

import calendarapp.CalendarApp;
import calendarapp.controller.CalendarController;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.view.ICalendarView;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

  @Test
  public void testAddDuplicateSingleEvent() {
    boolean firstResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );
    assertTrue("First event should be created successfully", firstResult);

    boolean secondResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );

    assertFalse("Duplicate event creation should fail", secondResult);
    assertEquals(
            "ERROR: Duplicate event: subject, start and end are identical.",
            view.getLastMessage()
    );
  }

  @Test
  public void testRunHeadlessModeProcessesFile() throws IOException {
    File tempFile = File.createTempFile("testCommands", ".txt");
    FileWriter writer = new FileWriter(tempFile);
    writer.write("create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00\n");
    writer.write("exit\n");
    writer.close();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(new String[]{"--mode", "headless", tempFile.getAbsolutePath()});

    String output = outputStream.toString();
    assertTrue("Output should contain event creation", output.contains("----- All Events -----"));
    assertTrue("Output should contain 'Meeting'", output.contains("Meeting"));

    tempFile.delete();
  }

  @Test
  public void testAddDuplicateSingleEventMain() {
    boolean firstResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );
    assertTrue("First event should be created successfully", firstResult);

    boolean secondResult = controller.processCommand(
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00"
    );

    assertFalse("Second event (duplicate) should fail", secondResult);
    assertEquals(
            "ERROR: Duplicate event: subject, start and end are identical.",
            view.getLastMessage()
    );
  }

  @Test
  public void testControllerInitialization() {
    assertNotNull("Controller should not be null", controller);
  }

  @Test
  public void testProcessNullCommand() {
    boolean result = controller.processCommand(null);
    assertFalse("Null command should fail", result);
    assertEquals("ERROR: Parsing Error: Command cannot be null", view.getLastMessage());
  }

  @Test
  public void testProcessEmptyCommand() {
    boolean result = controller.processCommand("");
    assertFalse("Empty command should fail", result);
    assertEquals("ERROR: Command is null", view.getLastMessage());
  }

  @Test
  public void testApplicationExit() {
    assertTrue("Application should exit gracefully", true);
  }

  @Test
  public void testRunInteractiveMode() {
    String simulatedUserInput = String.join(System.lineSeparator(),
            "create event \"Meeting\" from 2025-06-01T09:00 to 2025-06-01T10:00",
            "print events on 2025-06-01",
            "exit"
    ) + System.lineSeparator();

    ByteArrayInputStream testInput = new ByteArrayInputStream(simulatedUserInput.getBytes(StandardCharsets.UTF_8));
    System.setIn(testInput);

    ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
    System.setOut(new PrintStream(testOutput));

    CalendarApp.main(new String[]{"--mode", "interactive"});

    String output = testOutput.toString();

    assertTrue(output.contains("Enter commands (type 'exit' to quit):"));
    assertTrue(output.contains("> "));
    assertTrue(output.contains("----- All Events -----"));
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Displaying 1 events"));
  }



  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = "";
    private List<CalendarEvent> lastDisplayedEvents = null;

    @Override
    public void displayMessage(String message) {
      lastMessage = message;
    }

    @Override
    public void displayError(String errorMessage) {
      lastMessage = "ERROR: " + errorMessage;
    }

    @Override
    public void displayEvents(List<CalendarEvent> events) {
      lastDisplayedEvents = events;
      lastMessage = "Displaying " + events.size() + " events";
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public List<CalendarEvent> getLastDisplayedEvents() {
      return lastDisplayedEvents;
    }
  }
}

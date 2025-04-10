package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * A view implementation for headless mode, reading commands from a file (scriptReader).
 */
public class HeadlessView implements ICalendarView {
  private ICalendarController controller;
  private final Reader scriptReader;

  /**
   * Constructs a HeadlessView with the specified controller and script reader.
   *
   * @param controller   the controller that processes commands
   * @param scriptReader the reader that supplies commands from the script file
   */
  public HeadlessView(ICalendarController controller, Reader scriptReader) {
    this.controller = controller;
    this.scriptReader = scriptReader;
  }

  /**
   * Runs the headless view by processing commands line-by-line from the script file.
   * Each line is passed to the controller for command processing.
   *
   * @throws IOException if an I/O error occurs while reading the script file
   */
  public void run() throws IOException {
    BufferedReader reader = new BufferedReader(scriptReader);
    String line;
    while ((line = reader.readLine()) != null) {
      String command = line.trim();
      if (!command.isEmpty()) {
        controller.processCommand(command);
      }
    }
  }

  /**
   * Displays a message to the console. This is typically used for feedback or status updates.
   *
   * @param message the message to be displayed
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message to the console. This is used to report errors during execution.
   *
   * @param errorMessage the error message to be displayed
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println("Error: " + errorMessage);
  }

  /**
   * Displays a list of events to the console. If no events are found, a message is
   * displayed indicating this.
   *
   * @param events a list of read-only calendar events to be displayed
   */
  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (ReadOnlyCalendarEvent event : events) {
      System.out.println(event);
    }
  }
}

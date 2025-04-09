package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ICalendarEvent;

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

  public HeadlessView(ICalendarController controller, Reader scriptReader) {
    this.controller = controller;
    this.scriptReader = scriptReader;
  }

  /**
   * Run the headless view by processing commands line-by-line from the script file.
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

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String errorMessage) {
    System.err.println("Error: " + errorMessage);
  }

  @Override
  public void displayEvents(List<ICalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (ICalendarEvent event : events) {
      System.out.println(event);
    }
  }
}

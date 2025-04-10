package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A view implementation for headless mode, reading commands from a script and writing output to an Appendable.
 */
public class HeadlessView implements ICalendarView {
  private final ICalendarController controller;
  private Readable in;
  private Appendable out;

  public HeadlessView(ICalendarController controller, Readable in, Appendable out) {
    this.controller = controller;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    try {
      BufferedReader reader = new BufferedReader(in instanceof BufferedReader
              ? (BufferedReader) in
              : new BufferedReader((Reader) in));

      String line;
      while ((line = reader.readLine()) != null) {
        String command = line.trim();
        if (command.isEmpty()) continue;

        if (command.equalsIgnoreCase("exit")) {
          out.append("Exiting.\n");
          break;
        }

        controller.processCommand(command);
      }
    } catch (IOException e) {
      try {
        out.append("Error reading commands: ").append(e.getMessage()).append("\n");
      } catch (IOException ex) {
        throw new RuntimeException("Failed to report read error", ex);
      }
    }
  }

  @Override
  public void displayMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write message", e);
    }
  }

  @Override
  public void displayError(String errorMessage) {
    try {
      out.append("Error: ").append(errorMessage).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write error message", e);
    }
  }

  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    try {
      if (events.isEmpty()) {
        out.append("No events found.\n");
        return;
      }

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

      for (ReadOnlyCalendarEvent event : events) {
        ZonedDateTime start = event.getStartDateTime();
        ZonedDateTime end = event.getEndDateTime();

        out.append("- ").append(event.getSubject()).append(": ")
                .append(start.format(formatter)).append(" to ")
                .append(end.format(formatter)).append("\n");

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
          out.append("  Description: ").append(event.getDescription()).append("\n");
        }
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          out.append("  Location: ").append(event.getLocation()).append("\n");
        }
        if (event.isPublic()) {
          out.append("  Public Event\n");
        }
        if (event.isAllDay()) {
          out.append("  All Day Event\n");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to display events", e);
    }
  }

  @Override
  public void setInput(Readable in) {
    this.in = in;
  }

  @Override
  public void setOutput(Appendable out) {
    this.out = out;
  }
}

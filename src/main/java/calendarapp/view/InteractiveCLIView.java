package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * An interactive CLI view for the calendar application using Readable and Appendable.
 */
public class InteractiveCLIView implements ICalendarView, Runnable {
  private final ICalendarController controller;
  private final Readable in;
  private final Appendable out;

  public InteractiveCLIView(ICalendarController controller, Readable in, Appendable out) {
    this.controller = controller;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    Scanner scanner = new Scanner(in);
    try {
      out.append("Welcome to the Interactive Calendar CLI!\n");
      out.append("Enter commands (type 'exit' to quit):\n");
    } catch (IOException e) {
      throw new RuntimeException("Output failed", e);
    }

    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.equalsIgnoreCase("exit")) {
        try {
          out.append("Exiting.\n");
        } catch (IOException e) {
          throw new RuntimeException("Failed to write to output", e);
        }
        break;
      }
      controller.processCommand(command);
    }
  }

  @Override
  public void displayMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to display message", e);
    }
  }

  @Override
  public void displayError(String errorMessage) {
    try {
      out.append("Error: ").append(errorMessage).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to display error", e);
    }
  }

  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    try {
      if (events.isEmpty()) {
        out.append("No events found.\n");
        return;
      }

      for (ReadOnlyCalendarEvent event : events) {
        ZonedDateTime start = event.getStartDateTime();
        ZonedDateTime end = event.getEndDateTime();

        out.append("- ").append(event.getSubject()).append(": ")
                .append(start.format(formatter)).append(" to ").append(end.format(formatter)).append("\n");

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
}

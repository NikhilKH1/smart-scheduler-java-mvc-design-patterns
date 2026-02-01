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

  /**
   * Constructs an InteractiveCLIView with the specified controller, input, and output streams.
   *
   * @param controller the calendar controller to process commands
   * @param in the Readable source to read user input from
   * @param out the Appendable destination to write output messages to
   */
  public InteractiveCLIView(ICalendarController controller, Readable in, Appendable out) {
    this.controller = controller;
    this.in = in;
    this.out = out;
  }

  /**
   * Starts the interactive CLI session. Continuously reads user commands from the input stream
   * and processes them until the user enters "exit". Displays welcome and exit messages.
   */
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

  /**
   * Displays a general message to the output stream.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to display message", e);
    }
  }

  /**
   * Displays an error message to the output stream, prefixed with "Error:".
   *
   * @param errorMessage the error message to display
   */
  @Override
  public void displayError(String errorMessage) {
    try {
      out.append("Error: ").append(errorMessage).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to display error", e);
    }
  }

  /**
   * Displays a list of calendar events to the output stream in a formatted manner.
   * Includes subject, start/end time, description, location, and public/all-day status.
   *
   * @param events the list of read-only calendar events to display
   */
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
}

package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * An interactive CLI view for the calendar application.
 * It prompts the user for commands in a loop and delegates command processing to the controller.
 */
public class InteractiveCLIView implements ICalendarView, Runnable {
  private ICalendarController controller;

  /**
   * Constructs an InteractiveCLIView with the specified controller.
   *
   * @param controller the controller that processes commands and manages the calendar
   */
  public InteractiveCLIView(ICalendarController controller) {
    this.controller = controller;
  }

  /**
   * Runs the interactive command input loop. The user is prompted to enter commands
   * through the command-line interface. The loop continues until the user types 'exit'.
   *
   * @see ICalendarController#processCommand(String)
   */
  public void run() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to the Interactive Calendar CLI!");
    System.out.println("Enter commands (type 'exit' to quit):");

    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.equalsIgnoreCase("exit")) {
        System.out.println("Exiting.");
        break;
      }
      controller.processCommand(command);
    }
  }

  /**
   * Displays a general message to the user through the console.
   *
   * @param message the message to be displayed
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays an error message to the user through the console. This method is called
   * when an error occurs during command processing or other operations.
   *
   * @param errorMessage the error message to be displayed
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println("Error: " + errorMessage);
  }

  /**
   * Displays a list of read-only calendar events to the user. Each event's details are
   * printed to the console, including subject, start and end times, description, location,
   * and other relevant information. If no events are found, a message is displayed indicating so.
   *
   * @param events a list of read-only calendar events to be displayed
   */
  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    for (ReadOnlyCalendarEvent event : events) {
      ZonedDateTime start = event.getStartDateTime();
      ZonedDateTime end = event.getEndDateTime();

      System.out.println("- " + event.getSubject() + ": " +
              start.format(formatter) + " to " + end.format(formatter));

      if (event.getDescription() != null && !event.getDescription().isEmpty()) {
        System.out.println("  Description: " + event.getDescription());
      }
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        System.out.println("  Location: " + event.getLocation());
      }
      if (event.isPublic()) {
        System.out.println("  Public Event");
      }
      if (event.isAllDay()) {
        System.out.println("  All Day Event");
      }
    }
  }
}

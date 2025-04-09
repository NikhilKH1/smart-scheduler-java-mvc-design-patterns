package calendarapp.view;

import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ICalendarEvent;

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

  public InteractiveCLIView(ICalendarController controller) {
    this.controller = controller;
  }


  /**
   * Runs the interactive command input loop.
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
    for (ICalendarEvent event : events) {
      ZonedDateTime start = event.getStartDateTime();
      ZonedDateTime end = event.getEndDateTime();

      System.out.println("- " + event.getSubject() + ": " +
              start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")) +
              " to " +
              end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")));

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
package calendarapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.view.CalendarView;

/**
 * Main class for running the calendar application.
 * The application can run in interactive mode or headless mode.
 */
public class CalendarApp {

  /**
   * The main entry point of the application.
   * Depending on the command-line arguments, the application runs in interactive mode or
   * headless mode.
   *
   * @param args command-line arguments; use "--mode interactive" for interactive mode or
   *             "--mode headless commandsfile" for headless mode
   */
  public static void main(String[] args) {
    CalendarManager manager = new CalendarManager();
    CalendarView view = new CalendarView();
    CommandParser parser = new CommandParser(manager);
    CalendarController controller = new CalendarController(manager, view, parser);

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      String mode = args[1].toLowerCase();
      if (mode.equals("interactive")) {
        runInteractiveMode(controller, view);
      } else if (mode.equals("headless") && args.length == 3) {
        runHeadlessMode(controller, view, args[2]);
      } else {
        System.err.println("Usage: --mode interactive OR --mode headless <commands-file>");
        System.exit(1);
      }
    } else {
      runInteractiveMode(controller, view);
    }
  }

  public static void runInteractiveMode(CalendarController controller, CalendarView view) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter commands (type 'exit' to quit):");
    while (true) {
      System.out.print("> ");
      String command = scanner.nextLine();
      if (command.equalsIgnoreCase("exit")) {
        break;
      }
      controller.processCommand(command);
    }
    scanner.close();
  }

  public static void runHeadlessMode(CalendarController controller, CalendarView view, String fileName) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String command;
      while ((command = reader.readLine()) != null) {
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
          continue;
        }

        if (trimmed.equalsIgnoreCase("exit")) {
          System.out.println("Exit command encountered. Terminating headless mode.");
          break;
        }

        try {
          boolean success = controller.processCommand(trimmed);
          if (!success) {
            System.err.println("Error executing command: '" + trimmed + "'. Command failed.");
            System.exit(1);
          }
        } catch (Exception e) {
          System.err.println("Error executing command: '" + trimmed + "'");
          System.err.println("Reason: " + e.getMessage());
          System.exit(1);
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading commands file: " + e.getMessage());
      System.exit(1);
    }
  }
}

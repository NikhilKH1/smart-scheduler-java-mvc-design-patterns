package calendarapp;

import calendarapp.controller.CalendarController;
import calendarapp.controller.ICalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.io.ConsoleCommandSource;
import calendarapp.io.FileCommandSource;
import calendarapp.io.ICommandSource;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.view.CalendarView;
import calendarapp.view.ICalendarView;

import java.io.IOException;

/**
 * The main class to run the Calendar Application.
 * It initializes the calendar manager, view, command parser, and controller,
 * and provides functionality to run the application in either interactive or headless mode.
 */
public class CalendarApp {

  /**
   * The entry point for the Calendar Application.
   * It processes the command-line arguments to determine the mode (interactive or headless)
   * and sets up the corresponding command source (console or file).
   *
   * @param args Command-line arguments specifying the mode and file for headless mode
   */
  public static void main(String[] args) {
    ICalendarManager manager = new CalendarManager();
    ICalendarView view = new CalendarView();
    CommandParser parser = new CommandParser(manager);
    ICalendarController controller = new CalendarController(manager, view, parser);

    ICommandSource commandSource = null;
    try {
      if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
        String mode = args[1].toLowerCase();
        if (mode.equals("interactive")) {
          commandSource = new ConsoleCommandSource();
        } else if (mode.equals("headless") && args.length == 3) {
          commandSource = new FileCommandSource(args[2]);
          runHeadlessMode(controller, view, commandSource);
          return;
        } else {
          System.err.println("Usage: --mode interactive OR --mode headless <commands-file>");
          System.exit(1);
        }
      } else {
        commandSource = new ConsoleCommandSource();
      }
      runInteractiveMode(controller, view, commandSource);
    } catch (IOException e) {
      System.err.println("Error initializing command source: " + e.getMessage());
      System.exit(1);
    } finally {
      if (commandSource != null) {
        commandSource.close();
      }
    }
  }

  /**
   * Runs the application in interactive mode, where the user can type commands
   * and interact with the calendar system via the console.
   * The user can type 'exit' to quit the interactive session.
   *
   * @param controller The controller responsible for processing commands
   * @param view The view that displays events and messages
   * @param commandSource The command source (console) for reading commands
   */
  public static void runInteractiveMode(ICalendarController controller, ICalendarView view,
                                        ICommandSource commandSource) {
    System.out.println("Enter commands (type 'exit' to quit):");
    String command;
    while ((command = commandSource.getNextCommand()) != null) {
      if (command.trim().equalsIgnoreCase("exit")) {
        break;
      }
      controller.processCommand(command);
    }
  }

  /**
   * Runs the application in headless mode, where commands are read from a file,
   * and the results are processed and executed without user interaction.
   * The program exits if the 'exit' command is encountered or if an error occurs.
   *
   * @param controller The controller responsible for processing commands
   * @param view The view that displays events and messages
   * @param commandSource The command source (file) for reading commands
   */
  public static void runHeadlessMode(ICalendarController controller, ICalendarView view,
                                     ICommandSource commandSource) {
    String command;
    while ((command = commandSource.getNextCommand()) != null) {
      String trimmed = command.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      if (trimmed.equalsIgnoreCase("exit")) {
        System.out.println("Exit command encountered. Terminating headless mode.");
        System.exit(0);
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
  }
}
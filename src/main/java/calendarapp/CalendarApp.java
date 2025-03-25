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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CalendarApp {

  public static void main(String[] args) {
    // Instantiate using interfaces
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
          return;  // Terminate after headless mode execution.
        } else {
          System.err.println("Usage: --mode interactive OR --mode headless <commands-file>");
          System.exit(1);
        }
      } else {
        // Default to console mode
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

  public static void runInteractiveMode(ICalendarController controller, ICalendarView view, ICommandSource commandSource) {
    System.out.println("Enter commands (type 'exit' to quit):");
    String command;
    while ((command = commandSource.getNextCommand()) != null) {
      if (command.trim().equalsIgnoreCase("exit")) {
        break;
      }
      controller.processCommand(command);
    }
  }

  public static void runHeadlessMode(ICalendarController controller, ICalendarView view, ICommandSource commandSource) {
    // In headless mode, any error should be displayed and the process terminated.
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
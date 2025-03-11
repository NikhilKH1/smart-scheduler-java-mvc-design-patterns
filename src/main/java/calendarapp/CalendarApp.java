package calendarapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import calendarapp.controller.CalendarController;
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
   *             "--mode headless <commands-file>" for headless mode
   */
  public static void main(String[] args) {
    CalendarModel model = new CalendarModel();
    CalendarView view = new CalendarView();
    CalendarController controller = new CalendarController(model, view);

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      String mode = args[1].toLowerCase();
      if (mode.equals("interactive")) {
        runInteractiveMode(controller, model, view);
      } else if (mode.equals("headless") && args.length == 3) {
        runHeadlessMode(controller, model, view, args[2]);
      } else {
        System.err.println("Usage: --mode interactive OR --mode headless <commands-file>");
        System.exit(1);
      }
    } else {
      runInteractiveMode(controller, model, view);
    }
  }

  /**
   * Runs the application in interactive mode.
   * Commands are read from standard input, processed,
   * and the current list of events is displayed after each command.
   *
   * @param controller the CalendarController to process commands
   * @param model      the CalendarModel holding event data
   * @param view       the CalendarView used to display events and messages
   */
  private static void runInteractiveMode(CalendarController controller,
                                         CalendarModel model, CalendarView view) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter commands (type 'exit' to quit):");
    while (true) {
      System.out.print("> ");
      String command = scanner.nextLine();
      if (command.equalsIgnoreCase("exit")) {
        break;
      }
      controller.processCommand(command);
      if (!command.trim().toLowerCase().startsWith("print")) {
        System.out.println("----- All Events -----");
        view.displayEvents(model.getEvents());
        System.out.println("----------------------");
      }
    }
    scanner.close();
  }

  /**
   * Runs the application in headless mode.
   * Commands are read from the specified file and processed one by one.
   * After processing each command, the current list of events is displayed.
   *
   * @param controller the CalendarController to process commands
   * @param model      the CalendarModel holding event data
   * @param view       the CalendarView used to display events and messages
   * @param fileName   the name of the file containing commands
   */
  private static void runHeadlessMode(CalendarController controller,
                                      CalendarModel model, CalendarView view, String fileName) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String command;
      while ((command = reader.readLine()) != null) {
        if (command.trim().isEmpty()) continue;
        if (command.equalsIgnoreCase("exit")) break;
        controller.processCommand(command);
        if (!command.trim().toLowerCase().startsWith("print")) {
          System.out.println("----- All Events -----");
          view.displayEvents(model.getEvents());
          System.out.println("----------------------");
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading commands file: " + e.getMessage());
      System.exit(1);
    }
  }
}

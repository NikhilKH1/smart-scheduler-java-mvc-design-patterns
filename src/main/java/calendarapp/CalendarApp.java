package calendarapp;

import java.io.*;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.ICalendarController;
import calendarapp.controller.IOCalendarController;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.view.CalendarView;
import calendarapp.view.ICalendarView;

public class CalendarApp {

  public static void main(String[] args) {
    try {
      run(args);
    } catch (IOException e) {
      System.err.println("I/O error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Refactored logic for testability.
   */
  public static void run(String[] args) throws IOException {
    ICalendarManager manager = new CalendarManager();
    ICalendarView view = new CalendarView();
    CommandParser parser = new CommandParser(manager);
    ICalendarController controller = new CalendarController(manager, view, parser);

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      String mode = args[1].toLowerCase();

      if (mode.equals("interactive")) {
        IOCalendarController ioController =
                new IOCalendarController(new InputStreamReader(System.in), System.out, controller);
        ioController.run();
      } else if (mode.equals("headless") && args.length == 3) {
        Reader fileReader = new BufferedReader(new FileReader(args[2]));
        IOCalendarController ioController =
                new IOCalendarController(fileReader, System.out, controller);
        ioController.run();
      } else {
        System.err.println("Usage: --mode interactive OR --mode headless <commands-file>");
        System.exit(1);
      }
    } else {
      IOCalendarController ioController =
              new IOCalendarController(new InputStreamReader(System.in), System.out, controller);
      ioController.run();
    }
  }
}

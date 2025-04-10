package calendarapp;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.ICalendarController;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;

/**
 * The main entry point for the Calendar application. This class initializes the core
 * components of the calendar system, including the calendar manager, command parser,
 * and controller. It then runs the application based on the provided arguments.
 */
public class CalendarApp {

  /**
   * The main method for the Calendar application. It initializes the necessary components and
   * starts the application by running the controller with the given arguments.
   *
   * @param args command-line arguments that are passed to the controller's run method
   * @see CalendarController#run(String[])
   */
  public static void main(String[] args) {
    ICalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);
    ICalendarController controller = new CalendarController(manager, parser);
    controller.run(args);
  }
}

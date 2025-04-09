package calendarapp;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;

public class CalendarApp {
  public static void main(String[] args) {
    ICalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);
    CalendarController controller = new CalendarController(manager, parser);
    controller.run(args);
  }
}

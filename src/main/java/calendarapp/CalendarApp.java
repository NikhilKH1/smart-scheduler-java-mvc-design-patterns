package calendarapp;

import java.io.*;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.ICalendarController;
import calendarapp.controller.IOCalendarHelper;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.view.CalendarView;
import calendarapp.view.ICalendarView;

public class CalendarApp {

  public static void main(String[] args) {
    try {
      run(args, new InputStreamReader(System.in), System.out);
    } catch (IOException e) {
      System.err.println("I/O error: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Refactored run method to decouple I/O for easier testing and flexibility.
   *
   * @param args command-line args
   * @param in   input source (can be System.in, StringReader, FileReader, etc.)
   * @param out  output destination (can be System.out, StringBuilder, etc.)
   * @throws IOException on I/O errors
   */
  public static void run(String[] args, Reader in, Appendable out) throws IOException {
    ICalendarManager manager = new CalendarManager();
    ICalendarView view = new CalendarView();
    CommandParser parser = new CommandParser(manager);
    ICalendarController controller = new CalendarController(manager, view, parser);

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      String mode = args[1].toLowerCase();

      if (mode.equals("interactive")) {
        IOCalendarHelper ioController = new IOCalendarHelper(in, out, controller);
        ioController.run();
      } else if (mode.equals("headless") && args.length == 3) {
        Reader fileReader = new BufferedReader(new FileReader(args[2]));
        IOCalendarHelper ioController = new IOCalendarHelper(fileReader, out, controller);
        ioController.run();
      } else {
        out.append("Usage: --mode interactive OR --mode headless <commands-file>\n");
      }
    } else {
      IOCalendarHelper ioController = new IOCalendarHelper(in, out, controller);
      ioController.run();
    }
  }

}

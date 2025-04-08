package calendarapp;

import calendarapp.controller.CalendarCommandRunner;
import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.ICalendarController;
import calendarapp.factory.DefaultCommandFactory;
import calendarapp.factory.ICommandFactory;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.view.CalendarGUIView;
import calendarapp.view.CalendarView;
import calendarapp.view.ICalendarView;

import java.io.*;

public class CalendarApp {

  public static void main(String[] args) {
    try {
      run(args, new InputStreamReader(System.in), System.out);
    } catch (IOException e) {
      System.err.println("I/O error: " + e.getMessage());
      System.exit(1);
    }
  }

  public static void run(String[] args, Reader in, Appendable out) throws IOException {
    ICalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    if (args.length == 0) {
      CalendarGUIView guiView = new CalendarGUIView(manager, null);
      CalendarController controller = new CalendarController(manager, guiView, parser);
      guiView.setController(controller);
      guiView.setCommandFactory(new DefaultCommandFactory());
      return;
    }

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      String mode = args[1].toLowerCase();

      if (mode.equals("interactive")) {
        ICalendarView view = new CalendarView();
        ICalendarController controller = new CalendarController(manager, view, parser);
        CalendarCommandRunner ioController = new CalendarCommandRunner(in, out, controller);
        ioController.run();
      } else if (mode.equals("headless") && args.length == 3) {
        ICalendarView view = new CalendarView();
        ICalendarController controller = new CalendarController(manager, view, parser);
        Reader fileReader = new BufferedReader(new FileReader(args[2]));
        CalendarCommandRunner ioController = new CalendarCommandRunner(fileReader, out, controller);
        ioController.run();
      } else {
        out.append("Usage: --mode interactive OR --mode headless <commands-file>\n");
      }
    } else {
      out.append("Invalid arguments. Try: --mode interactive OR --mode headless <file>\n");
    }
  }
}

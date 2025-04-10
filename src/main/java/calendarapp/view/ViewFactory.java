package calendarapp.view;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.factory.DefaultCommandFactory;
import calendarapp.factory.ICommandFactory;

import java.io.FileReader;

public class ViewFactory {

  public static ICalendarView createView(String[] args, ICalendarManager manager, CalendarController controller)
          throws Exception {

    if (args.length == 0) {
      CalendarGUIView guiView = new CalendarGUIView(manager, controller);
      guiView.setCommandFactory(new DefaultCommandFactory());
      guiView.setController(controller);
      return guiView;

    } else if (args.length >= 2 && "--mode".equals(args[0]) && "headless".equals(args[1])) {
      if (args.length < 3) throw new IllegalArgumentException("Provide script file path.");
      return new HeadlessView(controller, new FileReader(args[2]));

    } else if (args.length >= 2 && "--mode".equals(args[0]) && "interactive".equals(args[1])) {
      return new InteractiveCLIView(controller);

    } else {
      throw new IllegalArgumentException("Invalid mode. Use --mode headless <file> or --mode interactive");
    }
  }
}

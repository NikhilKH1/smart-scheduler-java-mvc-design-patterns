package calendarapp.controller;

import java.io.FileReader;
import java.io.IOException;

import calendarapp.controller.commands.ICommand;
import calendarapp.controller.commands.ICalendarManagerCommand;
import calendarapp.controller.commands.ICalendarModelCommand;
import calendarapp.factory.DefaultCommandFactory;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.CalendarGUIView;
import calendarapp.view.HeadlessView;
import calendarapp.view.ICalendarView;
import calendarapp.view.InteractiveCLIView;

/**
 * The Controller class is responsible for processing calendar-related commands.
 * The CalendarController manages the flow of commands and their execution,
 * interacts with the calendar manager to manipulate calendar data, and
 * communicates with the view to display results or errors.
 */
public class CalendarController implements ICalendarController {
  private final ICalendarManager calendarManager;
  private ICalendarView view;
  private final CommandParser parser;

  /**
   * Constructs a CalendarController with the specified calendar manager,
   * view, and command parser.
   *
   * @param calendarManager the calendar manager for managing calendars
   * @param view            the view used for displaying messages
   * @param parser          the parser used to parse user commands
   */
  public CalendarController(ICalendarManager calendarManager, ICalendarView view,
                            CommandParser parser) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.parser = parser;
  }

  public CalendarController(ICalendarManager manager, CommandParser parser) {
    this.calendarManager = manager;
    this.parser = parser;
  }

  /**
   * Processes the given command input by parsing it and executing the corresponding command.
   * The method determines whether the command is a calendar manager command or
   * a calendar model command, and delegates the execution to the appropriate handler.
   *
   * @param commandInput the input command as a string
   * @return true if the command was successfully processed, false otherwise
   */
  @Override
  public boolean processCommand(String commandInput) {
    if (commandInput == null || commandInput.trim().isEmpty()) {
      view.displayError("Parsing Error: Command cannot be null or empty");
      return false;
    }

    try {
      ICommand cmd = parser.parse(commandInput);
      if (cmd == null) {
        view.displayError("Parsing Error: Command parsing returned null");
        return false;
      }

      if (cmd instanceof ICalendarManagerCommand) {
        return ((ICalendarManagerCommand) cmd).execute(calendarManager, view);
      }

      if (cmd instanceof ICalendarModelCommand) {
        ICalendarModel activeCalendar = calendarManager.getActiveCalendar();
        if (activeCalendar == null) {
          view.displayError("No active calendar selected. Use "
                  + "'use calendar --name <calName>' first.");
          return false;
        }
        return ((ICalendarModelCommand) cmd).execute(activeCalendar, view);
      }

      view.displayError("Unsupported command type.");
      return false;

    } catch (IllegalArgumentException e) {
      view.displayError("Parsing Error: " + e.getMessage());
      return false;
    } catch (Exception e) {
      view.displayError("Execution Error: " + e.getMessage());
      return false;
    }
  }

  /**
   * Retrieves the active calendar from the calendar manager.
   *
   * @return the active calendar, or null if no calendar is selected
   */
  public ICalendarModel getActiveCalendar() {
    return calendarManager.getActiveCalendar();
  }

  /**
   * Retrieves the view associated with this controller.
   *
   * @return the view used for displaying information to the user
   */
  public ICalendarView getView() {
    return view;
  }

  public void setView(ICalendarView view) {
    this.view = view;
  }

  @Override
  public void run(String[] args) {
    try {
      if (args.length == 2 && args[0].equals("--mode") && args[1].equals("interactive")) {
        this.view = new InteractiveCLIView(this);
        view.displayMessage("Starting in Interactive CLI mode...");
        ((InteractiveCLIView) view).run();

      } else if (args.length == 3 && args[0].equals("--mode") && args[1].equals("headless")) {
        this.view = new HeadlessView(this, new FileReader(args[2]));
        view.displayMessage("Running in Headless mode with script: " + args[2]);
        ((HeadlessView) view).run();

      }  else if (args.length == 0) {
      CalendarGUIView guiView = new CalendarGUIView(calendarManager, this);
      this.view = guiView;
      guiView.setCommandFactory(new DefaultCommandFactory());
      guiView.initialize();
    }
    else {
        System.err.println("Invalid arguments. Use:");
        System.err.println("--mode interactive");
        System.err.println("--mode headless <script-file>");
        System.err.println("(or run with no arguments for GUI mode)");
      }
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }


}



package calendarapp.controller;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
 */
public class CalendarController implements ICalendarController {
  private final ICalendarManager calendarManager;
  private ICalendarView view;
  private final CommandParser parser;

  /**
   * Constructs a new instance of the `CalendarController` with the specified components.
   * This constructor initializes the `CalendarController` by associating it with the
   * provided `ICalendarManager`, `ICalendarView`, and `CommandParser`. These components
   * are essential for managing calendar data, displaying the view, and parsing user input commands.
   *
   * @param calendarManager An instance of `ICalendarManager`
   *                        responsible for managing calendar data.
   * @param view            An instance of `ICalendarView`
   *                        how the calendar is presented to the user.
   * @param parser          An instance of `CommandParser` used to parse and
   *                        process user input commands.
   */
  public CalendarController(ICalendarManager calendarManager, ICalendarView view,
                            CommandParser parser) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.parser = parser;
  }

  /**
   * Constructs a new instance of the `CalendarController` with the specified components.
   * This constructor initializes the `CalendarController` by associating it with the
   * provided `ICalendarManager` and `CommandParser`. These components
   * are essential for managing calendar data and parsing user input commands.
   *
   * @param manager An instance of `ICalendarManager`
   *                responsible for managing calendar data.
   * @param parser  An instance of `CommandParser` used to parse and
   *                process user input commands.
   */
  public CalendarController(ICalendarManager manager, CommandParser parser) {
    this.calendarManager = manager;
    this.parser = parser;
  }

  /**
   * Processes a command input.
   *
   * @param commandInput the command text to process
   * @return true if the command was processed successfully; false otherwise
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
   * Retrieves the currently active calendar model being used by the controller.
   *
   * @return the active ICalendarModel instance from the calendar manager
   */
  public ICalendarModel getActiveCalendar() {
    return calendarManager.getActiveCalendar();
  }

  /**
   * Returns the current view associated with this controller.
   *
   * @return the ICalendarView currently in use
   */
  public ICalendarView getView() {
    return view;
  }

  /**
   * Sets the view component to be used by this controller.
   *
   * @param view the ICalendarView to associate with the controller
   */
  public void setView(ICalendarView view) {
    this.view = view;
  }

  /**
   * Original run method for handling command-line args.
   */
  @Override
  public void run(String[] args) {
    try {
      if (args.length == 2 && args[0].equals("--mode") && args[1].equals("interactive")) {
        this.view = new InteractiveCLIView(this, new InputStreamReader(System.in),
                System.out);
        view.displayMessage("Starting in Interactive CLI mode...");
        view.run();

      } else if (args.length == 3 && args[0].equals("--mode") && args[1].equals("headless")) {
        this.view = new HeadlessView(this, new FileReader(args[2]), System.out);
        view.displayMessage("Running in Headless mode with script: " + args[2]);
        view.run();

      } else if (args.length == 0) {
        CalendarGUIView guiView = new CalendarGUIView(this);
        this.view = guiView;
        guiView.setCommandFactory(new DefaultCommandFactory());
        guiView.initialize();

      } else {
        System.err.println("Invalid arguments. Use:");
        System.err.println("--mode interactive");
        System.err.println("--mode headless <script-file>");
        System.err.println("(or run with no arguments for GUI mode)");
      }
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  /**
   * New method for running with injected input/output.
   */
  @Override
  public void run(Readable in, Appendable out) {
    this.view = new InteractiveCLIView(this, in, out);
    view.displayMessage("Starting in Interactive CLI mode...");
    view.run();
  }
}

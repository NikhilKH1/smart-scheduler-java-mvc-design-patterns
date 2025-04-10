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
          view.displayError("No active calendar selected. Use 'use calendar --name <calName>' first.");
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

  public ICalendarModel getActiveCalendar() {
    return calendarManager.getActiveCalendar();
  }

  public ICalendarView getView() {
    return view;
  }

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
        this.view = new InteractiveCLIView(this, new InputStreamReader(System.in), System.out);
        view.displayMessage("Starting in Interactive CLI mode...");
        view.run();

      } else if (args.length == 3 && args[0].equals("--mode") && args[1].equals("headless")) {
        this.view = new HeadlessView(this, new FileReader(args[2]), System.out);
        view.displayMessage("Running in Headless mode with script: " + args[2]);
        view.run();

      } else if (args.length == 0) {
        CalendarGUIView guiView = new CalendarGUIView(calendarManager, this);
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

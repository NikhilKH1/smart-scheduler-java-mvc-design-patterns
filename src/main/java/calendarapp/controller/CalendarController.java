package calendarapp.controller;

import calendarapp.controller.commands.ICommand;
import calendarapp.controller.commands.ICalendarManagerCommand;
import calendarapp.controller.commands.ICalendarModelCommand;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * The Controller class is responsible for processing calendar-related commands.
 * The CalendarController manages the flow of commands and their execution,
 * interacts with the calendar manager to manipulate calendar data, and
 * communicates with the view to display results or errors.
 */
public class CalendarController implements ICalendarController {
  private final ICalendarManager calendarManager;
  private final ICalendarView view;
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
}

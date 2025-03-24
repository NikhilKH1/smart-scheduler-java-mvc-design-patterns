package calendarapp.controller;

import calendarapp.controller.commands.ICommand;
import calendarapp.controller.commands.ICalendarManagerCommand;
import calendarapp.controller.commands.ICalendarModelCommand;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * This class processes user commands by parsing input strings, interacting with the calendar model,
 * and updating the calendar view with the results.
 */
public class CalendarController implements ICalendarController {
  private final ICalendarManager calendarManager;
  private final ICalendarView view;
  private final CommandParser parser;

  public CalendarController(ICalendarManager calendarManager, ICalendarView view, CommandParser parser) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.parser = parser;
  }

  @Override
  public boolean processCommand(String commandInput) {
    try {
      ICommand cmd = parser.parse(commandInput);
      if (cmd == null) {
        view.displayError("Command parsing returned null");
        return false;
      }
      boolean success;
      if (cmd instanceof ICalendarManagerCommand) {
        success = ((ICalendarManagerCommand) cmd).execute(calendarManager, view);
        return success;
      }

      if (cmd instanceof ICalendarModelCommand) {
        ICalendarModel activeCalendar = calendarManager.getActiveCalendar();
        if (activeCalendar == null) {
          view.displayError("No active calendar selected. Use 'use calendar --name <calName>' first.");
          return false;
        }
        success = ((ICalendarModelCommand) cmd).execute(activeCalendar, view);

        if (success) {
          if (activeCalendar.getEvents().isEmpty()) {
            view.displayMessage("No events found in calendar: " + activeCalendar.getName());
          } else {
            view.displayEvents(activeCalendar.getEvents());
          }
        }
        return success;
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
}

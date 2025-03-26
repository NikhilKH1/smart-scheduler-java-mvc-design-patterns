package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Interface for commands that operate at the calendar event level.
 * This interface is designed for commands that perform actions on a specific calendar's events.
 */
public interface ICalendarModelCommand extends ICommand {

  /**
   * Executes the command using the specified calendar model and view.
   *
   * @param model the calendar model that contains the events to operate on
   * @param view  the view used to display the result or any errors
   * @return true if the command executes successfully, false otherwise
   */
  public boolean execute(ICalendarModel model, ICalendarView view);
}

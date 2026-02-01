package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * This interface is intended for commands that interact with the calendar manager,
 * which is responsible for managing multiple calendars.
 */
public interface ICalendarManagerCommand extends ICommand {

  /**
   * Executes the command using the calendar manager.
   *
   * @param calendarManager the calendar manager handling multiple calendars
   * @param view            the view used to display messages
   * @return true if the command executes successfully, false otherwise
   */
  public boolean execute(ICalendarManager calendarManager, ICalendarView view);
}

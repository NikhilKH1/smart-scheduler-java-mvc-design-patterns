package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Interface for all calendar manager-level commands.
 */
public interface ICalendarManagerCommand extends ICommand {
  boolean execute(ICalendarManager calendarManager, ICalendarView view);
}

package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Interface for all calendar event-level commands.
 */
public interface ICalendarModelCommand extends ICommand {
  boolean execute(ICalendarModel model, ICalendarView view);
}

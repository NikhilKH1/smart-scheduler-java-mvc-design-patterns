package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Interface for all calendar manager-level commands.
 */
public interface CalendarManagerCommand extends Command {
  boolean execute(ICalendarManager calendarManager, ICalendarView view);
}

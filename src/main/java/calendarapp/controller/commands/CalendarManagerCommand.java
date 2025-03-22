package calendarapp.controller.commands;

import calendarapp.model.CalendarManager;
import calendarapp.view.ICalendarView;

/**
 * Interface for all calendar manager-level commands.
 */
public interface CalendarManagerCommand extends Command {
  boolean execute(CalendarManager calendarManager, ICalendarView view);
}

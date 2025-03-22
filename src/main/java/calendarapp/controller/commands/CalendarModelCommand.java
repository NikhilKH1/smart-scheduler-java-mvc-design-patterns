package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.view.ICalendarView;

/**
 * Interface for all calendar event-level commands.
 */
public interface CalendarModelCommand extends Command {
  boolean execute(CalendarModel model, ICalendarView view);
}

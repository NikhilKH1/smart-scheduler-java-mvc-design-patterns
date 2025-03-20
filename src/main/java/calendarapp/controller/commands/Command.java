package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.view.ICalendarView;
/**
 * Marker interface for commands.
 */
public interface Command {
  boolean execute(CalendarModel model, ICalendarView view);

}
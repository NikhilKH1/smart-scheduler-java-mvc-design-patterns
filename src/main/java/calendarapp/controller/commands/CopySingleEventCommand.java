package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.temporal.Temporal;

/**
 * Command to copy a single event to a different calendar at a new time.
 */
public class CopySingleEventCommand implements ICalendarManagerCommand {

  private final String eventName;
  private final Temporal sourceDateTime;
  private final String targetCalendarName;
  private final Temporal targetDateTime;

  /**
   * Constructs the command.
   *
   * @param eventName          the name of the event to copy
   * @param sourceDateTime     the date/time of the original event
   * @param targetCalendarName the name of the target calendar
   * @param targetDateTime     the new start date/time for the copied event
   */
  public CopySingleEventCommand(String eventName, Temporal sourceDateTime,
                                String targetCalendarName, Temporal targetDateTime) {
    this.eventName = eventName;
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
  }

  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel source = calendarManager.getActiveCalendar();
    ICalendarModel target = calendarManager.getCalendar(targetCalendarName);

    if (!(source instanceof CalendarModel) || !(target instanceof CalendarModel)) {
      view.displayError("Copy requires concrete CalendarModel implementations.");
      return false;
    }

    boolean success = ((CalendarModel) source).copySingleEventTo(
            (CalendarModel) source,
            eventName,
            sourceDateTime,
            (CalendarModel) target,
            targetDateTime
    );

    if (success) {
      view.displayMessage("Event '" + eventName + "' copied to calendar: " + targetCalendarName);
    } else {
      view.displayError("Failed to copy event. It may not exist or conflicts with "
              + "another event.");
    }

    return success;
  }
}

package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCommand implements ICalendarManagerCommand {

  private final ZonedDateTime sourceDate;
  private final String targetCalendarName;
  private final ZonedDateTime targetDate;

  /**
   * Constructs a CopyEventsOnDateCommand.
   *
   * @param sourceDate         the date on which events are copied from
   * @param targetCalendarName the name of the target calendar
   * @param targetDate         the date in the target calendar to set for the copied events
   */
  public CopyEventsOnDateCommand(ZonedDateTime sourceDate, String targetCalendarName,
                                 ZonedDateTime targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  /**
   * Executes the command by copying events from the active calendar to the target calendar.
   *
   * @param calendarManager the calendar manager used to access calendars
   * @param view            the view used to display messages
   * @return true if events were successfully copied, false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel source = calendarManager.getActiveCalendar();
    ICalendarModel target = calendarManager.getCalendar(targetCalendarName);

    if (!(source instanceof CalendarModel) || !(target instanceof CalendarModel)) {
      view.displayError("Copy requires concrete CalendarModel implementations.");
      return false;
    }

    boolean success = ((CalendarModel) source).copyEventsOnDateTo(
            (CalendarModel) source,
            sourceDate,
            (CalendarModel) target,
            targetDate
    );

    if (success) {
      view.displayMessage("Events copied successfully to calendar: " + targetCalendarName);
    } else {
      view.displayError("Some or all events could not be copied due to conflicts.");
    }

    return success;
  }


}

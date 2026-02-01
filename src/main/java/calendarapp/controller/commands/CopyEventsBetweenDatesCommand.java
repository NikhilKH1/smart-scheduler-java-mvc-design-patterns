package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.ZonedDateTime;

/**
 * Command to copy all events between two dates to another calendar.
 */
public class CopyEventsBetweenDatesCommand implements ICalendarManagerCommand {

  private final ZonedDateTime startDate;
  private final ZonedDateTime endDate;
  private final String targetCalendarName;
  private final ZonedDateTime targetStartDate;

  /**
   * Constructs the command.
   *
   * @param startDate          the start of the date range
   * @param endDate            the end of the date range
   * @param targetCalendarName the name of the calendar to copy events to
   * @param targetStartDate    the starting date in the target calendar
   */
  public CopyEventsBetweenDatesCommand(ZonedDateTime startDate, ZonedDateTime endDate,
                                       String targetCalendarName, ZonedDateTime targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  /**
   * Copies events between two dates from the active calendar to the target calendar,
   * starting at the specified target date. Displays success or error messages based on the result.
   *
   * @param calendarManager the calendar manager handling calendars
   * @param view            the view for displaying messages
   * @return true if events were copied successfully, false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel source = calendarManager.getActiveCalendar();
    ICalendarModel target = calendarManager.getCalendar(targetCalendarName);

    if (!(source instanceof CalendarModel) || !(target instanceof CalendarModel)) {
      view.displayError("Copy requires concrete CalendarModel implementations.");
      return false;
    }

    boolean success = ((CalendarModel) source).copyEventsBetweenTo(
            (CalendarModel) source, startDate, endDate, (CalendarModel) target, targetStartDate
    );

    if (success) {
      view.displayMessage("Events copied successfully to calendar: " + targetCalendarName);
    } else {
      view.displayError("Some or all events failed to copy due to conflicts.");
    }

    return success;
  }
}

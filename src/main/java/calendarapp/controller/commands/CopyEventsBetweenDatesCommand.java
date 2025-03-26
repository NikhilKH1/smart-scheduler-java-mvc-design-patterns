package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import java.time.temporal.Temporal;

/**
 * Command to copy all events between two dates to another calendar.
 */
public class CopyEventsBetweenDatesCommand implements ICalendarManagerCommand {

  private final Temporal startDate;
  private final Temporal endDate;
  private final String targetCalendarName;
  private final Temporal targetStartDate;

  /**
   * Constructs the command.
   *
   * @param startDate          the start of the date range
   * @param endDate            the end of the date range
   * @param targetCalendarName the name of the calendar to copy events to
   * @param targetStartDate    the starting date in the target calendar
   */
  public CopyEventsBetweenDatesCommand(Temporal startDate, Temporal endDate,
                                       String targetCalendarName, Temporal targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel source = calendarManager.getActiveCalendar();
    ICalendarModel target = calendarManager.getCalendar(targetCalendarName);

    if (!(source instanceof CalendarModel) || !(target instanceof CalendarModel)) {
      view.displayError("Copy requires concrete CalendarModel implementations.");
      return false;
    }

    boolean success = ((CalendarModel) source).copyEventsBetweenTo(
            (CalendarModel) source,
            startDate,
            endDate,
            (CalendarModel) target,
            targetStartDate
    );

    if (success) {
      view.displayMessage("Events copied successfully to calendar: " + targetCalendarName);
    } else {
      view.displayError("Some or all events failed to copy due to conflicts.");
    }

    return success;
  }
}

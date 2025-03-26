package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCommand implements ICalendarManagerCommand {
  private final Temporal sourceDate;
  private final String targetCalendarName;
  private final Temporal targetDate;

  /**
   * Constructs a CopyEventsOnDateCommand.
   *
   * @param sourceDate         the date on which events are copied from
   * @param targetCalendarName the name of the target calendar
   * @param targetDate         the date in the target calendar to set for the copied events
   */
  public CopyEventsOnDateCommand(Temporal sourceDate, String targetCalendarName,
                                 Temporal targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

  /**
   * Copies events from the active calendar on the given source date to the target calendar.
   * The event times are adjusted to match the target calendar's timezone and the target date.
   *
   * @param calendarManager the calendar manager used to access calendars
   * @param view            the view used to display messages
   * @return true if at least one event was copied, false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel sourceCalendar = calendarManager.getActiveCalendar();
    ICalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      view.displayError("Invalid source or target calendar.");
      return false;
    }

    boolean eventCopied = false;
    ZoneId targetZone = targetCalendar.getTimezone();

    LocalDate sourceLocalDate = LocalDate.from(sourceDate);
    LocalDate targetLocalDate = LocalDate.from(targetDate);

    for (ICalendarEvent event : sourceCalendar.getEventsOnDate(sourceLocalDate)) {
      ZonedDateTime eventStart = (ZonedDateTime) event.getStartDateTime();
      ZonedDateTime eventEnd = (ZonedDateTime) event.getEndDateTime();
      ZonedDateTime newStart = eventStart.withZoneSameInstant(targetZone)
              .withYear(targetLocalDate.getYear())
              .withMonth(targetLocalDate.getMonthValue())
              .withDayOfMonth(targetLocalDate.getDayOfMonth());
      ZonedDateTime newEnd = eventEnd.withZoneSameInstant(targetZone)
              .withYear(targetLocalDate.getYear())
              .withMonth(targetLocalDate.getMonthValue())
              .withDayOfMonth(targetLocalDate.getDayOfMonth());
      SingleEvent copied = new SingleEvent(event.getSubject(), newStart, newEnd,
              event.getDescription(), event.getLocation(), event.isPublic(), event.isAllDay(),
              null
      );

      boolean added = targetCalendar.addEvent(copied, true);
      if (added) {
        eventCopied = true;
      }
    }

    if (eventCopied) {
      view.displayMessage("Events copied successfully to calendar: " + targetCalendarName);
    } else {
      view.displayError("No events copied. Possible conflicts or"
              + " no events on the date.");
    }
    return eventCopied;
  }
}

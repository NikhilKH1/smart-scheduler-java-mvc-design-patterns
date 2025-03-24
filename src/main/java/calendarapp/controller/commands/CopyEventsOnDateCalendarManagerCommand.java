package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Command to copy all events on a specific date to another calendar.
 */
public class CopyEventsOnDateCalendarManagerCommand implements ICalendarManagerCommand {
  private final LocalDate sourceDate;
  private final String targetCalendarName;
  private final LocalDate targetDate;

  public CopyEventsOnDateCalendarManagerCommand(LocalDate sourceDate, String targetCalendarName, LocalDate targetDate) {
    this.sourceDate = sourceDate;
    this.targetCalendarName = targetCalendarName;
    this.targetDate = targetDate;
  }

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

    for (CalendarEvent event : sourceCalendar.getEventsOnDate(sourceDate)) {
      ZonedDateTime newStart = event.getStartDateTime().withZoneSameInstant(targetZone)
              .withYear(targetDate.getYear())
              .withMonth(targetDate.getMonthValue())
              .withDayOfMonth(targetDate.getDayOfMonth());

      ZonedDateTime newEnd = event.getEndDateTime().withZoneSameInstant(targetZone)
              .withYear(targetDate.getYear())
              .withMonth(targetDate.getMonthValue())
              .withDayOfMonth(targetDate.getDayOfMonth());

      SingleEvent copied = new SingleEvent(
              event.getSubject(),
              newStart,
              newEnd,
              event.getDescription(),
              event.getLocation(),
              event.isPublic(),
              event.isAllDay(),
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
      view.displayError("No events copied. Possible conflicts or no events on the date.");
    }
    return eventCopied;
  }
}

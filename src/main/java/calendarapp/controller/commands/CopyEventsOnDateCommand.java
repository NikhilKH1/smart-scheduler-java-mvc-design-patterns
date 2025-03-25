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

  public CopyEventsOnDateCommand(Temporal sourceDate, String targetCalendarName, Temporal targetDate) {
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

    LocalDate sourceLocalDate = LocalDate.from(sourceDate);
    LocalDate targetLocalDate = LocalDate.from(targetDate);

    for (ICalendarEvent event : sourceCalendar.getEventsOnDate(sourceLocalDate)) {
      // Cast start and end times to ZonedDateTime
      ZonedDateTime eventStart = (ZonedDateTime) event.getStartDateTime();
      ZonedDateTime eventEnd = (ZonedDateTime) event.getEndDateTime();

      // Adjust the start time to the target zone and date
      ZonedDateTime newStart = eventStart.withZoneSameInstant(targetZone)
              .withYear(targetLocalDate.getYear())
              .withMonth(targetLocalDate.getMonthValue())
              .withDayOfMonth(targetLocalDate.getDayOfMonth());

      // Adjust the end time to the target zone and date
      ZonedDateTime newEnd = eventEnd.withZoneSameInstant(targetZone)
              .withYear(targetLocalDate.getYear())
              .withMonth(targetLocalDate.getMonthValue())
              .withDayOfMonth(targetLocalDate.getDayOfMonth());

      // Create a new SingleEvent with the adjusted times
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

      // Attempt to add the new event to the target calendar
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

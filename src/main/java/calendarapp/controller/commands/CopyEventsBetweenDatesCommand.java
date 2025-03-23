package calendarapp.controller.commands;

import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Command to copy all events within a specified date range to another calendar.
 */
public class CopyEventsBetweenDatesCommand implements CalendarManagerCommand {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final String targetCalendarName;
  private final LocalDate targetStartDate;

  public CopyEventsBetweenDatesCommand(LocalDate startDate, LocalDate endDate, String targetCalendarName, LocalDate targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  @Override
  public boolean execute(CalendarManager calendarManager, ICalendarView view) {
    CalendarModel sourceCalendar = calendarManager.getActiveCalendar();
    CalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      view.displayError("Invalid source or target calendar.");
      return false;
    }
    boolean eventCopied = false;
    long daysOffset = java.time.temporal.ChronoUnit.DAYS.between(startDate, targetStartDate);
    for (CalendarEvent event : sourceCalendar.getEvents()) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
        ZonedDateTime newStart = event.getStartDateTime()
                .plusDays(daysOffset)
                .withZoneSameInstant(targetCalendar.getTimezone());
        ZonedDateTime newEnd = event.getEndDateTime()
                .plusDays(daysOffset)
                .withZoneSameInstant(targetCalendar.getTimezone());
        SingleEvent copied = new SingleEvent(
                event.getSubject(),
                newStart,
                newEnd,
                event.getDescription(),
                event.getLocation(),
                event.isPublic(),
                event.isAllDay(),
                UUID.randomUUID().toString() // new unique ID
        );
        if (targetCalendar.addEvent(copied, true)) {
          eventCopied = true;
        }
      }
    }
    if (eventCopied) {
      view.displayMessage("Events copied successfully to calendar: " + targetCalendarName);
      return true;
    } else {
      view.displayError("No events copied. Possible conflicts or no matching events in range.");
      return false;
    }
  }
}

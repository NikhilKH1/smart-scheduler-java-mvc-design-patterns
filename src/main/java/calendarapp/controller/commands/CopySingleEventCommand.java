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
 * Command to copy a specific event to another calendar with timezone adjustment.
 */
public class CopySingleEventCommand implements CalendarManagerCommand {
  private final String eventName;
  private final ZonedDateTime sourceDateTime;
  private final String targetCalendarName;
  private final ZonedDateTime targetDateTime;

  public CopySingleEventCommand(String eventName, ZonedDateTime sourceDateTime, String targetCalendarName, ZonedDateTime targetDateTime) {
    this.eventName = eventName;
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
  }

  @Override
  public boolean execute(CalendarManager calendarManager, ICalendarView view) {
    CalendarModel sourceCalendar = calendarManager.getActiveCalendar();
    CalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      view.displayError("Invalid source or target calendar.");
      return false;
    }
    for (CalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceDateTime)) {
        ZonedDateTime newStart = targetDateTime.withZoneSameInstant(targetCalendar.getTimezone());
        ZonedDateTime newEnd = event.getEndDateTime()
                .withZoneSameInstant(targetCalendar.getTimezone())
                .withHour(newStart.getHour())
                .withMinute(newStart.getMinute())
                .plusMinutes(event.getEndDateTime().minusMinutes(event.getStartDateTime().getMinute()).getMinute());

        SingleEvent copied = new SingleEvent(event.getSubject(), newStart, newEnd,
                event.getDescription(), event.getLocation(), event.isPublic(), event.isAllDay(), UUID.randomUUID().toString());
        if (targetCalendar.addEvent(copied, true)) {
          view.displayMessage("Event copied successfully.");
          return true;
        } else {
          view.displayError("Conflict detected while copying event.");
          return false;
        }
      }
    }
    view.displayError("Event not found.");
    return false;
  }
}

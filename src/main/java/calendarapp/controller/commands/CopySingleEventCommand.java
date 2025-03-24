package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Command to copy a specific event to another calendar with timezone adjustment.
 */
public class CopySingleEventCommand implements ICalendarManagerCommand {
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
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel sourceCalendar = calendarManager.getActiveCalendar();
    ICalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);

    if (sourceCalendar == null || targetCalendar == null) {
      view.displayError("Invalid source or target calendar.");
      return false;
    }

    for (CalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceDateTime)) {

        long durationMinutes = Duration.between(event.getStartDateTime(), event.getEndDateTime()).toMinutes();

        ZonedDateTime newStart = targetDateTime.withZoneSameInstant(targetCalendar.getTimezone());
        ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

        SingleEvent copied = new SingleEvent(
                event.getSubject(),
                newStart,
                newEnd,
                event.getDescription(),
                event.getLocation(),
                event.isPublic(),
                event.isAllDay(),
                null // Not recurring
        );

        if (targetCalendar.addEvent(copied, false)) {
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

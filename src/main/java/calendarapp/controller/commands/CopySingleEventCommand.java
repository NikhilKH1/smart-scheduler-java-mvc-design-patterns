package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

/**
 * Command to copy a specific event to another calendar with timezone adjustment.
 */
public class CopySingleEventCommand implements ICalendarManagerCommand {
  private final String eventName;
  private final Temporal sourceDateTime;
  private final String targetCalendarName;
  private final Temporal targetDateTime;


  /**
   * Constructs a CopySingleEventCommand.
   *
   * @param eventName the name of the event to be copied
   * @param sourceDateTime the original date and time of the event
   * @param targetCalendarName the name of the target calendar
   * @param targetDateTime the new date and time for the copied event
   */
  public CopySingleEventCommand(String eventName, Temporal sourceDateTime,
                                String targetCalendarName, Temporal targetDateTime) {
    this.eventName = eventName;
    this.sourceDateTime = sourceDateTime;
    this.targetCalendarName = targetCalendarName;
    this.targetDateTime = targetDateTime;
  }

  /**
   * Copies a single event from the active calendar to the target calendar.
   * The event is adjusted to match the target calendar's timezone.
   * If an event with the specified name and time is found, it is copied.
   * A success message is displayed if the event is copied successfully.
   * If a conflict occurs or the event is not found, an error message is displayed.
   *
   * @param calendarManager the calendar manager used to access calendars
   * @param view the view used to display messages
   * @return true if the event was copied successfully, false otherwise
   */
  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    ICalendarModel sourceCalendar = calendarManager.getActiveCalendar();
    ICalendarModel targetCalendar = calendarManager.getCalendar(targetCalendarName);

    if (sourceCalendar == null || targetCalendar == null) {
      view.displayError("Invalid source or target calendar.");
      return false;
    }

    ZonedDateTime sourceZoned = ZonedDateTime.from(sourceDateTime);
    ZonedDateTime targetZoned = ZonedDateTime.from(targetDateTime);

    for (ICalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceZoned)) {

        long durationMinutes = Duration.between(event.getStartDateTime(),
                event.getEndDateTime()).toMinutes();

        ZonedDateTime newStart = targetZoned.withZoneSameInstant(targetCalendar.getTimezone());
        ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

        SingleEvent copied = new SingleEvent(event.getSubject(), newStart, newEnd,
                event.getDescription(), event.getLocation(), event.isPublic(), event.isAllDay(),
                null
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

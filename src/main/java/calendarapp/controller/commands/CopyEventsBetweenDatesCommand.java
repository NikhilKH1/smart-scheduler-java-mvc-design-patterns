package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * Command to copy all events within a specified date range to another calendar.
 */
public class CopyEventsBetweenDatesCommand implements ICalendarManagerCommand {
  private final Temporal startDate;
  private final Temporal endDate;
  private final String targetCalendarName;
  private final Temporal targetStartDate;

  /**
   * Constructs a CopyEventsBetweenDatesCommand with the given dates and target calendar name.
   *
   * @param startDate          the start date of the event range to copy
   * @param endDate            the end date of the event range to copy
   * @param targetCalendarName the name of the target calendar to which events will be copied
   * @param targetStartDate    the date in the target calendar where events should be shifted
   */
  public CopyEventsBetweenDatesCommand(Temporal startDate, Temporal endDate,
                                       String targetCalendarName, Temporal targetStartDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCalendarName = targetCalendarName;
    this.targetStartDate = targetStartDate;
  }

  /**
   * Executes the copy events command by retrieving the source and target calendars,
   * calculating the days offset between the source start date and the target start date,
   * and copying events from the source calendar that fall within the specified date range.
   * The event times are adjusted by the calculated offset.
   * Displays a success message if events were copied; otherwise, displays an error message.
   *
   * @param calendarManager the calendar manager used to get the active and target calendars
   * @param view            the view to display messages
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
    LocalDate sourceStartLocalDate = LocalDate.from(startDate);
    LocalDate targetStartLocalDate = LocalDate.from(targetStartDate);
    long daysOffset = ChronoUnit.DAYS.between(sourceStartLocalDate, targetStartLocalDate);

    for (ICalendarEvent event : sourceCalendar.getEvents()) {
      ZonedDateTime eventStart = (ZonedDateTime) event.getStartDateTime();
      ZonedDateTime eventEnd = (ZonedDateTime) event.getEndDateTime();
      LocalDate eventDate = eventStart.toLocalDate();
      if (!eventDate.isBefore(sourceStartLocalDate)
              && !eventDate.isAfter(LocalDate.from(endDate))) {
        ZonedDateTime newStart = eventStart.plusDays(daysOffset)
                .withZoneSameInstant(targetCalendar.getTimezone());

        ZonedDateTime newEnd = eventEnd.plusDays(daysOffset)
                .withZoneSameInstant(targetCalendar.getTimezone());

        SingleEvent copied = new SingleEvent(event.getSubject(), newStart, newEnd,
                event.getDescription(), event.getLocation(), event.isPublic(), event.isAllDay(),
                UUID.randomUUID().toString()
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
      view.displayError("No events copied. Possible conflicts or "
              + "no matching events in range.");
      return false;
    }
  }
}
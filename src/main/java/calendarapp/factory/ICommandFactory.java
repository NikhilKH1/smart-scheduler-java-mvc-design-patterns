package calendarapp.factory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This interface defines methods for building various command strings related to
 * calendar events and calendars. These commands are used for creating events,
 * editing events, creating and switching between calendars, exporting calendars,
 * and querying events in a calendar system.
 */
public interface ICommandFactory {

  /**
   * Creates a command string for creating an event using an EventInput object.
   *
   * @param input the input containing event details
   * @return the command string for creating the event
   */
  public String createEventCommand(EventInput input);

  /**
   * Creates a command string for creating a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone of the calendar
   * @return the command string for creating the calendar
   */
  public String createCalendarCommand(String name, ZoneId timezone);

  /**
   * Creates a command string to switch to a calendar with the specified name.
   *
   * @param calendarName the name of the calendar to switch to
   * @return the command string for switching calendars
   */
  public String useCalendarCommand(String calendarName);

  /**
   * Creates a command string for exporting the calendar's data to the specified file path.
   *
   * @param filePath the path to which the calendar data should be exported
   * @return the command string for exporting the calendar
   */
  public String exportCalendarCommand(String filePath);

  /**
   * Creates a command string for editing an event using an EditInput object.
   *
   * @param input the input containing event edit details
   * @return the command string for editing the event
   */
  public String createEditCommand(EditInput input);

  /**
   * Creates a command string for editing a recurring event using an EditInput object.
   *
   * @param input the input containing recurring event edit details
   * @return the command string for editing the recurring event
   */
  public String createEditRecurringEventCommand(EditInput input);

  /**
   * Creates a command string to print events occurring between the specified start and end times.
   *
   * @param start the start time for the event range
   * @param end   the end time for the event range
   * @return the command string for printing the events
   */
  public String printEventsBetweenCommand(ZonedDateTime start, ZonedDateTime end);


  public String importCalendarCommand(String filePath);

}

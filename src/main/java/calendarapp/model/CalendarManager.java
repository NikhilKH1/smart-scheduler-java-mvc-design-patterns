package calendarapp.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple calendars by name and tracks the active calendar.
 * Provides methods to add, edit, use, and retrieve calendars. Also supports
 * copying events between calendars, either on specific dates or within date ranges.
 */
public class CalendarManager implements ICalendarManager {
  private final Map<String, ICalendarModel> calendars;
  private ICalendarModel activeCalendar;

  /**
   * Initializes the calendar manager with an empty set of calendars.
   * The active calendar is set to null initially.
   */
  public CalendarManager() {
    calendars = new HashMap<>();
  }

  /**
   * Adds a new calendar with the given name and timezone.
   *
   * @param name     the name of the calendar to be added
   * @param timezone the timezone of the calendar
   * @return true if the calendar was added successfully, false if a calendar already exists
   */
  public boolean addCalendar(String name, ZoneId timezone) {
    String cleanName = name.trim();
    if (calendars.containsKey(cleanName)) {
      return false;
    }
    ICalendarModel model = new CalendarModel(cleanName, timezone);
    calendars.put(cleanName, model);
    return true;
  }

  /**
   * Edits an existing calendar's properties.
   *
   * @param name     the name of the calendar to be edited
   * @param property the property to edit
   * @param newValue the new value for the specified property
   * @return true if the calendar was edited successfully
   * @throws IllegalArgumentException if the calendar is not found or for an unsupported property
   */
  public boolean editCalendar(String name, String property, String newValue) {
    ICalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue.trim())) {
          throw new IllegalArgumentException("Calendar name already exists: " + newValue);
        }
        calendars.remove(name.trim());
        ((CalendarModel) cal).setName(newValue.trim());
        calendars.put(newValue.trim(), cal);
        if (activeCalendar == cal) {
          activeCalendar = cal;
        }
        break;
      case "timezone":
        try {
          ZoneId newZone = ZoneId.of(newValue);
          cal.updateTimezone(newZone);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue);
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
    return true;
  }

  /**
   * Sets the specified calendar as the active calendar.
   *
   * @param name the name of the calendar to use as active
   * @return true if the calendar was successfully set as active, false otherwise
   */
  public boolean useCalendar(String name) {
    ICalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      return false;
    }
    activeCalendar = cal;
    return true;
  }

  /**
   * Returns the currently active calendar.
   *
   * @return the active calendar, or null if no active calendar is set
   */
  public ICalendarModel getActiveCalendar() {
    return activeCalendar;
  }

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar to retrieve
   * @return the calendar with the specified name, or null if no such calendar exists
   */
  public ICalendarModel getCalendar(String name) {
    String cleanName = name.trim();
    if (cleanName.startsWith("\"") && cleanName.endsWith("\"")) {
      cleanName = cleanName.substring(1, cleanName.length() - 1).trim();
    }
    return calendars.get(cleanName);
  }

  /**
   * Copies a single event from the active calendar to a target calendar on the specified dates.
   *
   * @param eventName          the name of the event to copy
   * @param sourceDateTime     the date and time of the event in the source calendar
   * @param targetCalendarName the name of the target calendar to copy the event to
   * @param targetDateTime     the date and time to set for the event in the target calendar
   * @return true if the event was copied successfully, false otherwise
   * @throws IllegalArgumentException if the source or target calendar is not found
   */
  public boolean copySingleEvent(String eventName, ZonedDateTime sourceDateTime,
                                 String targetCalendarName, ZonedDateTime targetDateTime) {
    ICalendarModel sourceCalendar = activeCalendar;
    ICalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copySingleEventTo(sourceCalendar, eventName, sourceDateTime,
            targetCalendar, targetDateTime);
  }

  /**
   * Copies all events on a specific date from the active calendar to a target calendar
   * on the specified date.
   *
   * @param sourceDate         the date of the events in the source calendar
   * @param targetCalendarName the name of the target calendar to copy the events to
   * @param targetDate         the date to set for the events in the target calendar
   * @return true if the events were copied successfully, false otherwise
   * @throws IllegalArgumentException if the source or target calendar is not found
   */
  public boolean copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                                  LocalDate targetDate) {
    ICalendarModel sourceCalendar = activeCalendar;
    ICalendarModel targetCalendar = getCalendar(targetCalendarName);

    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }

    ZonedDateTime sourceDateTime = sourceDate.atStartOfDay(sourceCalendar.getTimezone());
    ZonedDateTime targetDateTime = targetDate.atStartOfDay(targetCalendar.getTimezone());

    return sourceCalendar.copyEventsOnDateTo(sourceCalendar, sourceDateTime,
            targetCalendar, targetDateTime);
  }

  /**
   * Copies all events between a start and end date from the active calendar to a target calendar,
   * adjusting the start date in the target calendar.
   *
   * @param startDate          the start date of the events to copy
   * @param endDate            the end date of the events to copy
   * @param targetCalendarName the name of the target calendar to copy the events to
   * @param targetStartDate    the start date to set for the events in the target calendar
   * @return true if the events were copied successfully, false otherwise
   * @throws IllegalArgumentException if the source or target calendar is not found
   */
  public boolean copyEventsBetween(ZonedDateTime startDate, ZonedDateTime endDate,
                                   String targetCalendarName, ZonedDateTime targetStartDate) {
    ICalendarModel sourceCalendar = activeCalendar;
    ICalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }

    return sourceCalendar.copyEventsBetweenTo(sourceCalendar, startDate,
            endDate, targetCalendar, targetStartDate);
  }
}

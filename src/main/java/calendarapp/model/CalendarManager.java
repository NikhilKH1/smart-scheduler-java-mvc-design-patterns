package calendarapp.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Manages multiple calendars by name and tracks the active calendar.
 * Provides methods to add, edit, use, and retrieve calendars. Also supports
 * copying events between calendars, either on specific dates or within date ranges.
 */
public class CalendarManager implements ICalendarManager {
  private final Map<String, CalendarModel> calendars;
  private CalendarModel activeCalendar;

  /**
   * Initializes the calendar manager with an empty set of calendars.
   * The active calendar is set to null initially.
   */
  public CalendarManager() {
    calendars = new HashMap<>();
  }

  /**
   * Adds a new calendar with a unique name and specified timezone.
   * If a calendar with the same name already exists, it will not be added.
   *
   * @param name the calendar name
   * @param timezone the calendar's timezone
   * @return true if added successfully, false if a calendar with the same name exists.
   */
  public boolean addCalendar(String name, ZoneId timezone) {
    String cleanName = name.trim();
    if (calendars.containsKey(cleanName)) {
      return false;
    }
    CalendarModel model = new CalendarModel(cleanName, timezone);
    calendars.put(cleanName, model);
    return true;
  }

  /**
   * Edits a calendar's property (name or timezone).
   * If the name already exists in the calendar collection, an exception will be thrown.
   *
   * @param name the current calendar name
   * @param property the property to change ("name" or "timezone")
   * @param newValue the new value for the property
   * @return true if the update is successful
   * @throws IllegalArgumentException if the property or value is invalid
   */
  public boolean editCalendar(String name, String property, String newValue) {
    CalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue.trim())) {
          throw new IllegalArgumentException("Calendar name already exists: " + newValue);
        }
        calendars.remove(name.trim());
        cal.setName(newValue.trim());
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
   * Sets the active calendar by name.
   * The active calendar is used when performing operations that require a calendar context.
   *
   * @param name the calendar name to use
   * @return true if the calendar was found and set as active, false otherwise.
   */
  public boolean useCalendar(String name) {
    CalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      return false;
    }
    activeCalendar = cal;
    return true;
  }

  /**
   * Retrieves the currently active calendar.
   *
   * @return the active CalendarModel
   */
  public CalendarModel getActiveCalendar() {
    return activeCalendar;
  }

  /**
   * Returns a calendar by name after trimming and stripping quotes.
   *
   * @param name the calendar name
   * @return the CalendarModel if found, null otherwise.
   */
  public CalendarModel getCalendar(String name) {
    String cleanName = name.trim();
    if (cleanName.startsWith("\"") && cleanName.endsWith("\"")) {
      cleanName = cleanName.substring(1, cleanName.length() - 1).trim();
    }
    return calendars.get(cleanName);
  }

  /**
   * Copies a single event from the active calendar to the target calendar with timezone conversion.
   *
   * @param eventName the name of the event to copy
   * @param sourceDateTime the source event's date and time
   * @param targetCalendarName the target calendar's name
   * @param targetDateTime the target event's date and time
   * @return true if the event is successfully copied, false otherwise
   * @throws IllegalArgumentException if either the source or target calendar is not found
   */
  public boolean copySingleEvent(String eventName, ZonedDateTime sourceDateTime,
                                 String targetCalendarName, ZonedDateTime targetDateTime) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copySingleEventTo(sourceCalendar, eventName, sourceDateTime,
            targetCalendar, targetDateTime);
  }

  /**
   * Copies all events on a specific date from the active calendar to the target calendar.
   *
   * @param sourceDate the date of events to copy
   * @param targetCalendarName the target calendar's name
   * @param targetDate the target date for the copied events
   * @return true if the events are successfully copied, false otherwise
   * @throws IllegalArgumentException if either the source or target calendar is not found
   */
  public boolean copyEventsOnDate(LocalDate sourceDate, String targetCalendarName,
                                  LocalDate targetDate) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copyEventsOnDateTo(sourceCalendar, sourceDate,
            targetCalendar, targetDate);
  }

  /**
   * Copies all events within a date range from the active calendar to the target calendar.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   * @param targetCalendarName the target calendar's name
   * @param targetStartDate the target start date for the copied events
   * @return true if the events are successfully copied, false otherwise
   * @throws IllegalArgumentException if either the source or target calendar is not found
   */
  public boolean copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                   String targetCalendarName, LocalDate targetStartDate) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copyEventsBetweenTo(sourceCalendar, startDate,
            endDate, targetCalendar, targetStartDate);
  }
}

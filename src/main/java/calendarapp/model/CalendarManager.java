package calendarapp.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Manages multiple calendars by name and tracks the active calendar.
 */
public class CalendarManager implements ICalendarManager {
  private final Map<String, CalendarModel> calendars;
  private CalendarModel activeCalendar;

  public CalendarManager() {
    calendars = new HashMap<>();
  }

  /**
   * Adds a new calendar with a unique name and specified timezone.
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
   * @param name the current calendar name
   * @param property the property to change ("name" or "timezone")
   * @param newValue the new value for the property
   * @return true if update successful
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
   * @param name the calendar name to use
   * @return true if found and set as active, false otherwise.
   */
  public boolean useCalendar(String name) {
    CalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      return false;
    }
    activeCalendar = cal;
    return true;
  }

  public CalendarModel getActiveCalendar() {
    return activeCalendar;
  }

  /**
   * Returns a calendar by name after trimming and stripping quotes.
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
   */
  public boolean copySingleEvent(String eventName, ZonedDateTime sourceDateTime, String targetCalendarName, ZonedDateTime targetDateTime) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copySingleEventTo(sourceCalendar, eventName, sourceDateTime, targetCalendar, targetDateTime);
  }
  /**
   * Copies all events on a specific date to the target calendar.
   */
  public boolean copyEventsOnDate(LocalDate sourceDate, String targetCalendarName, LocalDate targetDate) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copyEventsOnDateTo(sourceCalendar, sourceDate, targetCalendar, targetDate);
  }

  /**
   * Copies all events within a date range to the target calendar.
   */
  public boolean copyEventsBetween(LocalDate startDate, LocalDate endDate, String targetCalendarName, LocalDate targetStartDate) {
    CalendarModel sourceCalendar = activeCalendar;
    CalendarModel targetCalendar = getCalendar(targetCalendarName);
    if (sourceCalendar == null || targetCalendar == null) {
      throw new IllegalArgumentException("Source or target calendar not found.");
    }
    return sourceCalendar.copyEventsBetweenTo(sourceCalendar, startDate, endDate, targetCalendar, targetStartDate);
  }
}
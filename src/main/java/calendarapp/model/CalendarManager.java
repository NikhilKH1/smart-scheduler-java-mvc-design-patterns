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

  public boolean addCalendar(String name, ZoneId timezone) {
    String cleanName = name.trim();
    if (calendars.containsKey(cleanName)) {
      return false;
    }
    ICalendarModel model = new CalendarModel(cleanName, timezone);
    calendars.put(cleanName, model);
    return true;
  }

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

  public boolean useCalendar(String name) {
    ICalendarModel cal = calendars.get(name.trim());
    if (cal == null) {
      return false;
    }
    activeCalendar = cal;
    return true;
  }

  public ICalendarModel getActiveCalendar() {
    return activeCalendar;
  }

  public ICalendarModel getCalendar(String name) {
    String cleanName = name.trim();
    if (cleanName.startsWith("\"") && cleanName.endsWith("\"")) {
      cleanName = cleanName.substring(1, cleanName.length() - 1).trim();
    }
    return calendars.get(cleanName);
  }

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

  @Override
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

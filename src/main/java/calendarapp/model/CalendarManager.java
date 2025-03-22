package calendarapp.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

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
    if (calendars.containsKey(name)) {
      return false;
    }
    CalendarModel model = new CalendarModel(name, timezone);
    calendars.put(name, model);
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
    CalendarModel cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    switch (property.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException("Calendar name already exists: " + newValue);
        }
        calendars.remove(name);
        cal.setName(newValue);
        calendars.put(newValue, cal);
        if (activeCalendar == cal) {
          activeCalendar = cal;
        }
        break;
      case "timezone":
        try {
          cal.setTimezone(ZoneId.of(newValue));
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
    CalendarModel cal = calendars.get(name);
    if (cal == null) {
      return false;
    }
    activeCalendar = cal;
    return true;
  }

  public CalendarModel getActiveCalendar() {
    return activeCalendar;
  }
}
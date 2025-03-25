package calendarapp.model;

import java.time.ZoneId;

public interface ICalendarManager {

  public boolean addCalendar(String name, ZoneId timezone);
  public boolean editCalendar(String name, String property, String newValue);
  public boolean useCalendar(String name);
  /**
   * Returns the currently active calendar.
   *
   * @return the active calendar, or null if no calendar is active
   */
  ICalendarModel getActiveCalendar();
  ICalendarModel getCalendar(String name);
}

package calendarapp.model;

import java.time.ZoneId;

/**
 * The ICalendarManager interface defines methods for managing multiple calendars.
 * It provides functionality to add, edit, and switch between calendars, as well as
 * retrieving the active calendar or a specific calendar by name.
 */
public interface ICalendarManager {

  /**
   * Adds a new calendar with the given name and timezone.
   *
   * @param name     the name of the calendar to be added
   * @param timezone the timezone of the calendar
   * @return true if the calendar was added successfully, false if a calendar with the
   * same name already exists
   */
  public boolean addCalendar(String name, ZoneId timezone);


  /**
   * Edits a calendar's property (name or timezone).
   *
   * @param name     the name of the calendar to be edited
   * @param property the property to be edited ("name" or "timezone")
   * @param newValue the new value for the specified property
   * @return true if the update was successful, false otherwise
   */
  public boolean editCalendar(String name, String property, String newValue);

  /**
   * Sets the calendar with the specified name as the active calendar.
   *
   * @param name the name of the calendar to be set as active
   * @return true if the calendar was found and set as active, false if the calendar does not exist
   */
  public boolean useCalendar(String name);

  /**
   * Returns the currently active calendar.
   *
   * @return the active calendar, or null if no calendar is active
   */
  public ICalendarModel getActiveCalendar();

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar to retrieve
   * @return the calendar with the specified name, or null if the calendar does not exist
   */
  public ICalendarModel getCalendar(String name);
}

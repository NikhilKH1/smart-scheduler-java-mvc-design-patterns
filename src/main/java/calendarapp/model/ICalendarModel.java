package calendarapp.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.time.ZoneId;

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;

/**
 * This interface defines the methods for the calendar model.
 * It includes methods to add events, retrieve events by date or time range,
 * check busy status, and edit events.
 */
public interface ICalendarModel {

  /**
   * Adds a new calendar event to the model.
   *
   * @param event       the calendar event to add
   * @param autoDecline true if conflicting events should be automatically declined;
   *                    false otherwise
   * @return true if the event was added successfully, false otherwise
   */
  public boolean addEvent(CalendarEvent event, boolean autoDecline);

  /**
   * Adds a recurring event to the model.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    true if conflicting occurrences should be automatically declined;
   *                       false otherwise
   * @return true if the recurring event was added successfully, false otherwise
   */
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline);

  /**
   * Retrieves all calendar events.
   *
   * @return a list of all calendar events in the model
   */
  public List<CalendarEvent> getEvents();

  /**
   * Retrieves calendar events that occur on a specific date.
   *
   * @param date the date to query for events
   * @return a list of calendar events on the specified date
   */
  public List<CalendarEvent> getEventsOnDate(LocalDate date);

  /**
   * Retrieves calendar events that occur between the specified start and end date/time.
   *
   * @param start the start date/time of the query range
   * @param end   the end date/time of the query range
   * @return a list of calendar events that fall within the specified range
   */
  public List<CalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end);

  /**
   * Checks if the calendar is busy at the specified date and time.
   *
   * @param dateTime the date and time to check for an event
   * @return true if there is an event occurring at the given date/time, false otherwise
   */
  public boolean isBusyAt(ZonedDateTime dateTime);

  /**
   * Edits an existing calendar event by replacing it with a new event.
   *
   * @param oldEvent the original event to be replaced
   * @param newEvent the new event with updated details
   * @return true if the event was updated successfully, false if a conflict occurred
   */
  public boolean editEvent(CalendarEvent oldEvent, CalendarEvent newEvent);

  /**
   * Edits a recurring event by updating one of its properties.
   *
   * @param eventName the name of the recurring event to edit
   * @param property  the recurring property to update (for example, repeat count,
   *                  repeat until date, etc.)
   * @param newValue  the new value for the specified property
   * @return true if the recurring event was updated successfully, false otherwise
   */
  public boolean editRecurringEvent(String eventName, String property, String newValue);

  boolean editSingleEvent(String property, String eventName, ZonedDateTime originalStart,
                          ZonedDateTime originalEnd, String newValue);

  boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime,
                         String newValue);

  boolean editEventsAll(String property, String eventName, String newValue);

  String getName();

  ZoneId getTimezone();

}

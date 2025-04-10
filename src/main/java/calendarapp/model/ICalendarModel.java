package calendarapp.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.ICalendarEvent;
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
  boolean addEvent(ICalendarEvent event, boolean autoDecline);

  /**
   * Adds a recurring event to the model.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    true if conflicting occurrences should be automatically declined;
   *                       false otherwise
   * @return true if the recurring event was added successfully, false otherwise
   */
  boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline);

  /**
   * Retrieves all calendar events.
   *
   * @return a list of all calendar events in the model
   */
  List<ReadOnlyCalendarEvent> getEvents();

  /**
   * Retrieves calendar events that occur on a specific date.
   *
   * @param date the date to query for events
   * @return a list of calendar events on the specified date
   */
  List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date);

  /**
   * Retrieves calendar events that occur between the specified start and end date/time.
   *
   * @param start the start date/time of the query range
   * @param end   the end date/time of the query range
   * @return a list of calendar events that fall within the specified range
   */
  List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end);

  /**
   * Checks if the calendar is busy at the specified date and time.
   *
   * @param dateTime the date and time to check for an event
   * @return true if there is an event occurring at the given date/time, false otherwise
   */
  boolean isBusyAt(ZonedDateTime dateTime);

  /**
   * Edits an existing calendar event by replacing it with a new event.
   *
   * @param oldEvent the original event to be replaced
   * @param newEvent the new event with updated details
   * @return true if the event was updated successfully, false if a conflict occurred
   */
  boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent);

  /**
   * Edits a recurring event by updating one of its properties.
   *
   * @param eventName the name of the recurring event to edit
   * @param property  the recurring property to update (for example, repeat count,
   *                  repeat until date, etc.)
   * @param newValue  the new value for the specified property
   * @return true if the recurring event was updated successfully, false otherwise
   */
  boolean editRecurringEvent(String eventName, String property, String newValue);

  /**
   * Edits a single event by its original start and end time.
   *
   * @param property      the property to update
   * @param eventName     the name of the event
   * @param originalStart the original start date/time
   * @param originalEnd   the original end date/time
   * @param newValue      the new value for the property
   * @return true if successfully edited
   */
  boolean editSingleEvent(String property, String eventName, ZonedDateTime originalStart,
                          ZonedDateTime originalEnd, String newValue);

  /**
   * Edits events from a specific start date/time onwards.
   *
   * @param property      the property to update
   * @param eventName     the event name
   * @param fromDateTime  the starting date/time filter
   * @param newValue      the new value for the property
   * @return true if successfully edited
   */
  boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime,
                         String newValue);

  /**
   * Edits all events matching the event name.
   *
   * @param property  the property to update
   * @param eventName the event name
   * @param newValue  the new value
   * @return true if successfully edited
   */
  boolean editEventsAll(String property, String eventName, String newValue);

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  String getName();

  /**
   * Gets the timezone of the calendar.
   *
   * @return the timezone
   */
  ZoneId getTimezone();

  /**
   * Updates the calendar's timezone and adjusts all events accordingly.
   *
   * @param newTimezone the new timezone to apply to the calendar
   */
  void updateTimezone(ZoneId newTimezone);

  /**
   * Copies a single event from the source calendar to the target calendar at the new datetime.
   *
   * @param sourceCalendar  the calendar to copy from
   * @param eventName       the name of the event
   * @param sourceDateTime  the start datetime of the original event
   * @param targetCalendar  the calendar to copy to
   * @param targetDateTime  the new start datetime for the copied event
   * @return true if the event was copied successfully, false otherwise
   */
  boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName,
                            ZonedDateTime sourceDateTime, ICalendarModel targetCalendar,
                            ZonedDateTime targetDateTime);

  /**
   * Copies all events from a specific date in the source calendar to a
   * new date in the target calendar.
   *
   * @param sourceCalendar the calendar to copy from
   * @param sourceDate     the source date to copy events from
   * @param targetCalendar the calendar to copy to
   * @param targetDate     the target date to place copied events
   * @return true if all events were copied successfully, false otherwise
   */
  boolean copyEventsOnDateTo(ICalendarModel sourceCalendar, ZonedDateTime sourceDate,
                             ICalendarModel targetCalendar, ZonedDateTime targetDate);

  /**
   * Copies all events in a date range from the source calendar to the target
   * calendar starting at a given date.
   *
   * @param sourceCalendar   the calendar to copy from
   * @param startDate        the start date of the source range
   * @param endDate          the end date of the source range
   * @param targetCalendar   the calendar to copy to
   * @param targetStartDate  the start date in the target calendar to begin placing events
   * @return true if all events were copied successfully, false otherwise
   */
  boolean copyEventsBetweenTo(ICalendarModel sourceCalendar, ZonedDateTime startDate,
                              ZonedDateTime endDate, ICalendarModel targetCalendar,
                              ZonedDateTime targetStartDate);

  List<ReadOnlyCalendarEvent> getReadOnlyEventsOnDate(LocalDate date);

  public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents();

}
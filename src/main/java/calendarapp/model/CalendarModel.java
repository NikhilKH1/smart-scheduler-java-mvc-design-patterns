package calendarapp.model;

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.ConflictChecker;
import calendarapp.utils.ModelHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class represents the calendar model that stores and manages calendar events.
 * It provides methods to add single and recurring events, query events by date or range,
 * check if a specific date and time is busy, and edit existing events.
 */
public class CalendarModel implements ICalendarModel {
  private final List<CalendarEvent> events;
  private final HashMap<String, RecurringEvent> recurringMap;

  /**
   * Constructs an empty calendar model.
   */
  public CalendarModel() {
    events = new ArrayList<>();
    recurringMap = new HashMap<>();
  }

  /**
   * Adds a single calendar event after checking for duplicates and conflicts.
   * If a duplicate event is found, an exception is thrown.
   * If a conflict exists and autoDecline is true, the event is not added.
   *
   * @param event       the calendar event to add
   * @param autoDecline flag indicating whether to automatically decline conflicting events
   * @return true if the event is added successfully; false otherwise
   * @throws IllegalArgumentException if a duplicate event exists
   */
  @Override
  public boolean addEvent(CalendarEvent event, boolean autoDecline) {
    if (duplicateExists(event)) {
      throw new IllegalArgumentException("Duplicate event: subject, start and end are identical.");
    }
    for (CalendarEvent existing : events) {
      if (ConflictChecker.hasConflict(existing, event)) {
        if (autoDecline) {
          return false;
        }
      }
    }
    events.add(event);
    return true;
  }

  /**
   * Adds a recurring event by generating individual occurrences.
   * Each occurrence is checked for duplicates and conflicts.
   * If a duplicate is found in the recurring series, an exception is thrown.
   * Occurrences that conflict and trigger auto-decline result in the recurring event
   * not being added.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    flag indicating whether to automatically decline conflicting occurrences
   * @return true if the recurring event is added successfully; false otherwise
   * @throws IllegalArgumentException if a duplicate occurrence is found
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    String seriesId = UUID.randomUUID().toString();
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences(seriesId);
    for (SingleEvent occ : occurrences) {
      if (duplicateExists(occ)) {
        throw new IllegalArgumentException("Duplicate event in recurring series.");
      }
      for (CalendarEvent existing : events) {
        if (ConflictChecker.hasConflict(existing, occ)) {
          if (autoDecline) {
            return false;
          }
        }
      }
    }
    events.addAll(occurrences);
    recurringMap.put(recurringEvent.getSubject(), recurringEvent);
    return true;
  }

  /**
   * Returns a copy of all events in the calendar.
   *
   * @return a list of calendar events
   */
  @Override
  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Returns a list of events that occur on the specified date.
   * An event is included if its start is on or before the date and its end is on
   * or after the date.
   *
   * @param date the date to query
   * @return a list of calendar events on the given date
   */
  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      LocalDate start = event.getStartDateTime().toLocalDate();
      LocalDate end = event.getEndDateTime().toLocalDate();
      if ((start.equals(date) || start.isBefore(date))
              && (end.equals(date) || end.isAfter(date))) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Returns a list of events that overlap with the given date and time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return a list of calendar events between the specified times
   */
  @Override
  public List<CalendarEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().isBefore(end) && event.getEndDateTime().isAfter(start)) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Checks if any event in the calendar occupies the specified date and time.
   *
   * @param dateTime the date and time to check
   * @return true if the calendar is busy at the given time; false otherwise
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (!dateTime.isBefore(event.getStartDateTime())
              && !dateTime.isAfter(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether an event with the same subject, start, and end already exists.
   *
   * @param newEvent the event to check for duplicates
   * @return true if a duplicate exists; false otherwise
   */
  private boolean duplicateExists(CalendarEvent newEvent) {
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(newEvent.getSubject())
              && event.getStartDateTime().equals(newEvent.getStartDateTime())
              && event.getEndDateTime().equals(newEvent.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Edits an existing event by replacing it with a new event.
   * The new event must not conflict with any other event.
   *
   * @param oldEvent the original event to be replaced
   * @param newEvent the new event to insert
   * @return true if the edit is successful; false if a conflict occurs
   */
  @Override
  public boolean editEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    events.remove(oldEvent);
    for (CalendarEvent e : events) {
      if (ConflictChecker.hasConflict(e, newEvent)) {
        events.add(oldEvent);
        return false;
      }
    }
    events.add(newEvent);
    return true;
  }

  /**
   * Edits a single event that matches the given subject and original start and end times.
   * The event is updated by applying the change specified by the property and new value.
   *
   * @param property      the property to update (name, description, startdatetime etc.)
   * @param eventName     the subject of the event
   * @param originalStart the original start date and time
   * @param originalEnd   the original end date and time
   * @param newValue      the new value for the property
   * @return true if the event is updated successfully; false if no matching event is found
   */
  public boolean editSingleEvent(String property, String eventName,
                                 LocalDateTime originalStart, LocalDateTime originalEnd,
                                 String newValue) {
    List<CalendarEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent
              && event.getSubject().equals(eventName)
              && event.getStartDateTime().equals(originalStart)
              && event.getEndDateTime().equals(originalEnd)) {
        matching.add(event);
      }
    }
    if (matching.isEmpty()) {
      return false;
    }
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old,
              property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  /**
   * Edits all events with the specified subject that start at or after the given date and time.
   * The specified property is updated with the new value.
   *
   * @param property     the property to update
   * @param eventName    the subject of the events to update
   * @param fromDateTime the start date and time from which to apply the edit
   * @param newValue     the new value for the property
   * @return true if events are updated successfully; false if no matching events are found
   */
  public boolean editEventsFrom(String property, String eventName,
                                LocalDateTime fromDateTime, String newValue) {
    List<CalendarEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName)
              && (!event.getStartDateTime().isBefore(fromDateTime))) {
        if (event instanceof SingleEvent) {
          matching.add(event);
        }
      }
    }
    if (matching.isEmpty()) {
      return false;
    }
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old,
              property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  /**
   * Edits all events with the specified subject by updating the given property.
   * If the property is one of the recurring event properties, the recurring event
   * is edited instead.
   *
   * @param property  the property to update
   * @param eventName the subject of the events to update
   * @param newValue  the new value for the property
   * @return true if events are updated successfully; false if no matching events are found
   */
  public boolean editEventsAll(String property, String eventName, String newValue) {
    if (property.equalsIgnoreCase("repeattimes")
            || property.equalsIgnoreCase("repeatuntil")
            || property.equalsIgnoreCase("repeatingdays")) {
      return editRecurringEvent(eventName, property, newValue);
    }
    List<CalendarEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName)) {
        if (event instanceof SingleEvent) {
          matching.add(event);
        }
      }
    }
    if (matching.isEmpty()) {
      return false;
    }
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old,
              property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  /**
   * Edits a recurring event by updating one of its recurring properties.
   * After updating the recurring event, all previous occurrences are removed and new occurrences
   * are generated using a new series identifier.
   *
   * @param eventName the subject of the recurring event
   * @param property  the recurring property to update (repeattimes, repeatuntil,
   *                  repeatingdays, description, or location)
   * @param newValue  the new value for the property
   * @return true if the recurring event is updated successfully
   * @throws IllegalArgumentException if the recurring event is not found or
   *                                  the property is unsupported
   */
  public boolean editRecurringEvent(String eventName, String property, String newValue) {
    RecurringEvent recurringEvent = recurringMap.get(eventName);
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event not found: " + eventName);
    }

    switch (property.toLowerCase()) {
      case "repeattimes":
        int newCount = Integer.parseInt(newValue);
        if (newCount <= 0) {
          throw new IllegalArgumentException("Repeat count must be a positive number.");
        }
        recurringEvent = new RecurringEvent(recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(), recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(), newCount, recurringEvent.getRepeatUntil(),
                recurringEvent.getDescription(), recurringEvent.getLocation(),
                recurringEvent.isPublic(), recurringEvent.isAllDay());
        break;
      case "repeatuntil":
        LocalDateTime newUntil = LocalDateTime.parse(newValue);
        recurringEvent = new RecurringEvent(recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(), recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(), recurringEvent.getRepeatCount(),
                newUntil, recurringEvent.getDescription(),
                recurringEvent.getLocation(), recurringEvent.isPublic(), recurringEvent.isAllDay());
        break;
      case "repeatingdays":
        recurringEvent = new RecurringEvent(recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(), recurringEvent.getEndDateTime(),
                newValue.toUpperCase(), recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(), recurringEvent.getDescription(),
                recurringEvent.getLocation(), recurringEvent.isPublic(), recurringEvent.isAllDay());
        break;
      case "description":
        recurringEvent = new RecurringEvent(recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(), recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(), recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(), newValue,
                recurringEvent.getLocation(), recurringEvent.isPublic(), recurringEvent.isAllDay());
        break;
      case "location":
        recurringEvent = new RecurringEvent(recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(), recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(), recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(), recurringEvent.getDescription(), newValue,
                recurringEvent.isPublic(), recurringEvent.isAllDay());
        break;
      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }

    events.removeIf(e -> e instanceof SingleEvent
            && ((SingleEvent) e).getSeriesId() != null
            && ((SingleEvent) e).getSubject().equals(eventName));

    String newSeriesId = UUID.randomUUID().toString();
    List<SingleEvent> newOccurrences = recurringEvent.generateOccurrences(newSeriesId);
    events.addAll(newOccurrences);
    recurringMap.put(eventName, recurringEvent);
    return true;
  }
}

package calendarapp.model;

import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a calendar model that stores events, supports adding, editing, and copying events,
 * and handles recurring events with the ability to update timezones.
 */
public class CalendarModel implements ICalendarModel {
  private final List<ICalendarEvent> events = new ArrayList<>();
  private final Map<String, RecurringEvent> recurringMap = new HashMap<>();

  private String name;
  private ZoneId timezone;

  /**
   * Constructs a new calendar model with the specified name and timezone.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone of the calendar
   */
  public CalendarModel(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  /**
   * Gets the name of the calendar.
   *
   * @return the name of the calendar
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the calendar.
   *
   * @param name the new name for the calendar
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the timezone of the calendar.
   *
   * @return the timezone of the calendar
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Sets the timezone for the calendar.
   *
   * @param timezone the new timezone for the calendar
   */
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }


  /**
   * Updates the timezone of the calendar and adjusts all events based on the new timezone.
   * Also regenerates recurring events based on the updated timezone.
   *
   * @param newTimezone the new timezone for the calendar
   */
  @Override
  public void updateTimezone(ZoneId newTimezone) {
    ZoneId oldTimezone = this.timezone;
    this.timezone = newTimezone;

    List<ICalendarEvent> updatedEvents = new ArrayList<>();
    for (ICalendarEvent event : events) {
      ZonedDateTime oldStart = event.getStartDateTime().withZoneSameInstant(newTimezone);
      ZonedDateTime oldEnd = event.getEndDateTime().withZoneSameInstant(newTimezone);

      if (event instanceof SingleEvent) {
        updatedEvents.add(new SingleEvent(
                event.getSubject(), oldStart, oldEnd,
                event.getDescription(), event.getLocation(),
                event.isPublic(), event.isAllDay(), ((SingleEvent) event).getSeriesId()
        ));
      } else {
        updatedEvents.add(event);
      }
    }

    events.clear();
    events.addAll(updatedEvents);

    Map<String, RecurringEvent> updatedRecurringMap = new HashMap<>();
    for (Map.Entry<String, RecurringEvent> entry : recurringMap.entrySet()) {
      RecurringEvent recurringEvent = entry.getValue();

      events.removeIf(e -> e instanceof SingleEvent
              && e.getSubject().equals(entry.getKey())
              && ((SingleEvent) e).getSeriesId() != null);

      RecurringEvent updatedRecurringEvent = recurringEvent.withUpdatedTimezone(newTimezone);
      List<SingleEvent> newOccurrences = updatedRecurringEvent.generateOccurrences(UUID.randomUUID().toString());

      events.addAll(newOccurrences);
      updatedRecurringMap.put(entry.getKey(), updatedRecurringEvent);
    }

    recurringMap.clear();
    recurringMap.putAll(updatedRecurringMap);
  }


  /**
   * Copies a single event from the source calendar to the target calendar with timezone adjustment.
   *
   * @param sourceCalendar the source calendar to copy from
   * @param eventName      the event name to copy
   * @param sourceDateTime the date and time of the event in the source calendar
   * @param targetCalendar the target calendar to copy to
   * @param targetDateTime the target date and time for the event in the target calendar
   * @return true if the event was successfully copied, false otherwise
   */
  @Override
  public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName,
                                   ZonedDateTime sourceDateTime, CalendarModel targetCalendar,
                                   ZonedDateTime targetDateTime) {
    for (ICalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceDateTime)) {
        long durationMinutes = Duration.between(event.getStartDateTime(), event.getEndDateTime()).toMinutes();
        ZonedDateTime newEnd = targetDateTime.plusMinutes(durationMinutes);
        ICalendarEvent copiedEvent = new SingleEvent(
                event.getSubject(), targetDateTime, newEnd,
                event.getDescription(), event.getLocation(),
                event.isPublic(), event.isAllDay(), null
        );
        return targetCalendar.addEvent(copiedEvent, false);
      }
    }
    return false;
  }



  /**
   * Copies all events on a specific date from the source calendar to the target calendar.
   *
   * @param sourceCalendar the source calendar to copy from
   * @param sourceDate     the date to copy events from in the source calendar
   * @param targetCalendar the target calendar to copy to
   * @param targetDate     the date for the copied events in the target calendar
   * @return true if all events were copied successfully, false otherwise
   */
  @Override
  public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, ZonedDateTime sourceDate,
                                    CalendarModel targetCalendar, ZonedDateTime targetDate) {
    boolean allCopied = true;
    LocalDate sourceLocalDate = sourceDate.toLocalDate();
    LocalDate targetLocalDate = targetDate.toLocalDate();
    ZoneId targetZone = targetCalendar.getTimezone();

    for (ICalendarEvent event : sourceCalendar.getEventsOnDate(sourceLocalDate)) {
      ZonedDateTime sourceEventStart = event.getStartDateTime();
      ZonedDateTime sourceEventEnd = event.getEndDateTime();

      ZonedDateTime shiftedStart = sourceEventStart.withZoneSameInstant(targetZone);
      long dayOffset = Duration.between(
              sourceLocalDate.atStartOfDay(targetZone),
              shiftedStart.toLocalDate().atStartOfDay(targetZone)
      ).toDays();

      ZonedDateTime newStart = ZonedDateTime.of(
              targetLocalDate.plusDays(dayOffset),
              shiftedStart.toLocalTime(),
              targetZone
      );

      long durationMinutes = Duration.between(sourceEventStart, sourceEventEnd).toMinutes();
      ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

      ICalendarEvent copiedEvent = new SingleEvent(
              event.getSubject(), newStart, newEnd,
              event.getDescription(), event.getLocation(),
              event.isPublic(), event.isAllDay(), null
      );

      if (!targetCalendar.addEvent(copiedEvent, false)) {
        allCopied = false;
      }
    }
    return allCopied;
  }




  /**
   * Copies all events within a specific date range from the source calendar to the target calendar.
   *
   * @param sourceCalendar  the source calendar to copy from
   * @param startDate       the start date of the range to copy events from
   * @param endDate         the end date of the range to copy events from
   * @param targetCalendar  the target calendar to copy to
   * @param targetStartDate the target start date for the copied events
   * @return true if all events were copied successfully, false otherwise
   */
  @Override
  public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, ZonedDateTime startDate,
                                     ZonedDateTime endDate, CalendarModel targetCalendar,
                                     ZonedDateTime targetStartDate) {
    boolean allCopied = true;

    long daysOffset = ChronoUnit.DAYS.between(startDate.toLocalDate(), targetStartDate.toLocalDate());

    for (ICalendarEvent event : sourceCalendar.getEventsBetween(startDate, endDate)) {
      long durationMinutes = Duration.between(event.getStartDateTime(), event.getEndDateTime()).toMinutes();
      ZonedDateTime newStart = event.getStartDateTime().plusDays(daysOffset).withZoneSameInstant(targetCalendar.getTimezone());
      ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

      ICalendarEvent copiedEvent = new SingleEvent(
              event.getSubject(), newStart, newEnd,
              event.getDescription(), event.getLocation(),
              event.isPublic(), event.isAllDay(), null
      );

      if (!targetCalendar.addEvent(copiedEvent, false)) {
        allCopied = false;
      }
    }
    return allCopied;
  }




  /**
   * Adds an event to the calendar, checking for duplicates and conflicts with existing events.
   *
   * @param event       the event to add
   * @param autoDecline if true, automatically declines conflicting events
   * @return true if the event was added successfully, false if there was a conflict
   */
  @Override
  public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
    if (duplicateExists(event)) {
      throw new IllegalArgumentException("Duplicate event detected.");
    }
    for (ICalendarEvent e : events) {
      if (ConflictChecker.hasConflict(e, event)) {
        return false;
      }
    }
    events.add(event);
    return true;
  }

  /**
   * Adds a recurring event to the calendar.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    if true, automatically declines conflicting events
   * @return true if the recurring event was added successfully, false if there was a conflict
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    String seriesId = UUID.randomUUID().toString();
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences(seriesId);

    for (SingleEvent occurrence : occurrences) {
      if (duplicateExists(occurrence)) {
        throw new IllegalArgumentException("Duplicate event in recurring series.");
      }
      for (ICalendarEvent existingEvent : events) {
        if (ConflictChecker.hasConflict(existingEvent, occurrence)) {
          return false;
        }
      }
    }

    events.addAll(occurrences);
    recurringMap.put(recurringEvent.getSubject(), recurringEvent);
    return true;
  }

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events
   */
  @Override
  public List<ICalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Gets all events that occur on a specific date.
   *
   * @param date the date to query
   * @return a list of events on that date
   */
  @Override
  public List<ICalendarEvent> getEventsOnDate(LocalDate date) {
    List<ICalendarEvent> result = new ArrayList<>();

    for (ICalendarEvent event : events) {
      LocalDate startDate = event.getStartDateTime().toLocalDate();
      LocalDate endDate = event.getEndDateTime().toLocalDate();

      if (!startDate.isAfter(date) && !endDate.isBefore(date)) {
        result.add(event);
      }
    }

    return result;
  }



  /**
   * Gets all events that occur between a specific start and end date.
   *
   * @param start the start date/time of the range
   * @param end   the end date/time of the range
   * @return a list of events that fall within the date range
   */
  @Override
  public List<ICalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    List<ICalendarEvent> result = new ArrayList<>();

    for (ICalendarEvent event : events) {
      ZonedDateTime eventStart = event.getStartDateTime();
      ZonedDateTime eventEnd = event.getEndDateTime();
      if (eventStart.isBefore(end) && eventEnd.isAfter(start)) {
        result.add(event);
      }
    }
    return result;
  }


  /**
   * Checks if there is a conflicting event at the specified time.
   *
   * @param dateTime the date and time to check
   * @return true if there is a conflict, false otherwise
   */
  @Override
  public boolean isBusyAt(ZonedDateTime dateTime) {
    for (ICalendarEvent event : events) {
      ZonedDateTime startZoned = event.getStartDateTime();
      ZonedDateTime endZoned = event.getEndDateTime();

      if (!dateTime.isBefore(startZoned) && dateTime.isBefore(endZoned)) {
        return true;
      }
    }
    return false;
  }



  /**
   * Checks if an event with the same name, start time, and end time already exists in the calendar.
   *
   * @param newEvent the event to check
   * @return true if the event already exists, false otherwise
   */
  private boolean duplicateExists(ICalendarEvent newEvent) {
    for (ICalendarEvent event : events) {
      if (event.getSubject().equals(newEvent.getSubject()) &&
              event.getStartDateTime().equals(newEvent.getStartDateTime()) &&
              event.getEndDateTime().equals(newEvent.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Edits an existing event in the calendar.
   *
   * @param oldEvent the existing event to edit
   * @param newEvent the updated event
   * @return true if the event was successfully edited, false if there was a conflict
   */
  @Override
  public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
    events.remove(oldEvent);
    for (ICalendarEvent event : events) {
      if (ConflictChecker.hasConflict(event, newEvent)) {
        events.add(oldEvent);
        return false;
      }
    }
    events.add(newEvent);
    return true;
  }

  /**
   * Edits a single event by updating a property with a new value.
   *
   * @param property      the property to update
   * @param eventName     the name of the event
   * @param originalStart the original start time of the event
   * @param originalEnd   the original end time of the event
   * @param newValue      the new value for the property
   * @return true if the event was successfully updated, false otherwise
   */
  @Override
  public boolean editSingleEvent(String property, String eventName,
                                 ZonedDateTime originalStart, ZonedDateTime originalEnd,
                                 String newValue) {
    for (ICalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {

        SingleEvent updated = ((SingleEvent) event).withUpdatedProperty(property, newValue);

        if (updated.getStartDateTime().isAfter(updated.getEndDateTime()) ||
                ConflictChecker.hasConflictExcept(event, updated, events)) {
          return false;
        }

        events.remove(event);
        events.add(updated);
        return true;
      }
    }
    return false;
  }



  /**
   * Edits events starting from a specified date.
   *
   * @param property     the property to update
   * @param eventName    the name of the event
   * @param fromDateTime the start date/time to filter events
   * @param newValue     the new value for the property
   * @return true if the events were successfully edited, false otherwise
   */
  @Override
  public boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime, String newValue) {
    List<SingleEvent> toUpdate = new ArrayList<>();

    for (ICalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              !event.getStartDateTime().isBefore(fromDateTime)) {
        toUpdate.add((SingleEvent) event);
      }
    }

    if (toUpdate.isEmpty()) {
      return false;
    }

    List<SingleEvent> updatedEvents = new ArrayList<>();
    for (SingleEvent event : toUpdate) {
      SingleEvent updated = event.withUpdatedProperty(property, newValue);
      if (updated.getStartDateTime().isAfter(updated.getEndDateTime()) ||
              ConflictChecker.hasConflictExcept(event, updated, events)) {
        return false;
      }
      updatedEvents.add(updated);
    }

    events.removeAll(toUpdate);
    events.addAll(updatedEvents);
    return true;
  }



  /**
   * Edits all events of a specific name by updating a property with a new value.
   *
   * @param property  the property to update
   * @param eventName the name of the event
   * @param newValue  the new value for the property
   * @return true if the events were successfully edited, false otherwise
   */
  @Override
  public boolean editEventsAll(String property, String eventName, String newValue) {
    if (isRecurringProperty(property)) {
      return editRecurringEvent(eventName, property, newValue);
    }

    List<SingleEvent> toUpdate = new ArrayList<>();
    for (ICalendarEvent event : events) {
      if (event instanceof SingleEvent && event.getSubject().equals(eventName)) {
        toUpdate.add((SingleEvent) event);
      }
    }

    if (toUpdate.isEmpty()) {
      return false;
    }

    List<SingleEvent> updatedEvents = new ArrayList<>();
    for (SingleEvent event : toUpdate) {
      SingleEvent updated = event.withUpdatedProperty(property, newValue);
      if (updated.getStartDateTime().isAfter(updated.getEndDateTime()) ||
              ConflictChecker.hasConflictExcept(event, updated, events)) {
        return false;
      }
      updatedEvents.add(updated);
    }

    events.removeAll(toUpdate);
    events.addAll(updatedEvents);
    return true;
  }


  /**
   * Edits a recurring event's property.
   *
   * @param eventName the name of the recurring event
   * @param property  the property to update
   * @param newValue  the new value for the property
   * @return true if the recurring event was successfully updated, false otherwise
   */
  @Override
  public boolean editRecurringEvent(String eventName, String property, String newValue) {
    RecurringEvent existingEvent = recurringMap.get(eventName);
    if (existingEvent == null) {
      return false;
    }

    RecurringEvent updatedEvent = existingEvent.withUpdatedProperty(property, newValue);
    List<SingleEvent> newOccurrences = updatedEvent.generateOccurrences(UUID.randomUUID().toString());

    for (SingleEvent newOccurrence : newOccurrences) {
      if (newOccurrence.getStartDateTime().isAfter(newOccurrence.getEndDateTime()) ||
              ConflictChecker.hasConflictExceptRecurring(eventName, newOccurrence, events)) {
        return false;
      }
    }

    events.removeIf(e -> e instanceof SingleEvent &&
            eventName.equals(e.getSubject()) &&
            ((SingleEvent) e).getSeriesId() != null);

    events.addAll(newOccurrences);
    recurringMap.put(eventName, updatedEvent);
    return true;
  }

  /**
   * Determines if a given property is related to a recurring event.
   * This method checks if the specified property is one of the key attributes associated
   * with a recurring event, such as repeat count, repeat until date, or repeating days.
   *
   * @param property the property to check
   * @return true if the property is related to a recurring event, false otherwise
   */
  private boolean isRecurringProperty(String property) {
    return property.equalsIgnoreCase("repeattimes")
            || property.equalsIgnoreCase("repeatuntil")
            || property.equalsIgnoreCase("repeatingdays");
  }
}

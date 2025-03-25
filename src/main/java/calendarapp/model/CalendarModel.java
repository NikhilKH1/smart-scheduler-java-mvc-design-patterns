package calendarapp.model;

import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalendarModel implements ICalendarModel {
  private final List<ICalendarEvent> events = new ArrayList<>();
  private final Map<String, RecurringEvent> recurringMap = new HashMap<>();

  private String name;
  private ZoneId timezone;

  public CalendarModel(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public ZoneId getTimezone() {
    return timezone;
  }
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }

  public void updateTimezone(ZoneId newTimezone) {
    ZoneId oldTimezone = this.timezone;
    this.timezone = newTimezone;

    List<ICalendarEvent> updatedEvents = new ArrayList<>();

    for (ICalendarEvent event : events) {
      ZonedDateTime oldStart = ZonedDateTime.from(event.getStartDateTime()).withZoneSameInstant(newTimezone);
      ZonedDateTime oldEnd = ZonedDateTime.from(event.getEndDateTime()).withZoneSameInstant(newTimezone);

      if (event instanceof SingleEvent) {
        SingleEvent updatedEvent = new SingleEvent(
                event.getSubject(),
                oldStart,
                oldEnd,
                event.getDescription(),
                event.getLocation(),
                event.isPublic(),
                event.isAllDay(),
                ((SingleEvent) event).getSeriesId()
        );
        updatedEvents.add(updatedEvent);
      } else {
        updatedEvents.add(event);
      }
    }

    events.clear();
    events.addAll(updatedEvents);

    Map<String, RecurringEvent> updatedRecurringMap = new HashMap<>();
    for (Map.Entry<String, RecurringEvent> entry : recurringMap.entrySet()) {
      RecurringEvent recurringEvent = entry.getValue();

      events.removeIf(e -> e instanceof SingleEvent &&
              e.getSubject().equals(entry.getKey()) &&
              ((SingleEvent) e).getSeriesId() != null);

      RecurringEvent updatedRecurringEvent = recurringEvent.withUpdatedTimezone(newTimezone);
      List<SingleEvent> newOccurrences = updatedRecurringEvent.generateOccurrences(UUID.randomUUID().toString());

      events.addAll(newOccurrences);
      updatedRecurringMap.put(entry.getKey(), updatedRecurringEvent);
    }

    recurringMap.clear();
    recurringMap.putAll(updatedRecurringMap);
  }

  public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName,
                                   Temporal sourceDateTime, CalendarModel targetCalendar,
                                   Temporal targetDateTime) {

    ZonedDateTime sourceZoned = ZonedDateTime.from(sourceDateTime);
    ZonedDateTime targetZoned = ZonedDateTime.from(targetDateTime);

    for (ICalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceZoned)) {
        long durationMinutes = Duration.between(
                event.getStartDateTime(),
                event.getEndDateTime()).toMinutes();

        ZonedDateTime newEnd = targetZoned.plusMinutes(durationMinutes);
        ICalendarEvent copiedEvent = new SingleEvent(
                event.getSubject(), targetZoned, newEnd,
                event.getDescription(), event.getLocation(),
                event.isPublic(), event.isAllDay(), null
        );

        return targetCalendar.addEvent(copiedEvent, false);
      }
    }
    return false;
  }

  public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, Temporal sourceDate,
                                    CalendarModel targetCalendar, Temporal targetDate) {
    boolean allCopied = true;
    LocalDate sourceLocalDate = LocalDate.from(sourceDate);
    LocalDate targetLocalDate = LocalDate.from(targetDate);

    for (ICalendarEvent event : sourceCalendar.getEventsOnDate(sourceLocalDate)) {
      long durationMinutes = Duration.between(
              event.getStartDateTime(), event.getEndDateTime()).toMinutes();

      ZonedDateTime newStart = ZonedDateTime.of(
              targetLocalDate, ZonedDateTime.from(event.getStartDateTime()).toLocalTime(),
              targetCalendar.getTimezone());

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

  public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, Temporal startDate,
                                     Temporal endDate, CalendarModel targetCalendar,
                                     Temporal targetStartDate) {
    boolean allCopied = true;

    LocalDate startLocalDate = LocalDate.from(startDate);
    LocalDate endLocalDate = LocalDate.from(endDate);
    LocalDate targetStartLocalDate = LocalDate.from(targetStartDate);

    long daysOffset = ChronoUnit.DAYS.between(startLocalDate, targetStartLocalDate);

    ZonedDateTime rangeStart = startLocalDate.atStartOfDay(sourceCalendar.getTimezone());
    ZonedDateTime rangeEnd = endLocalDate.plusDays(1).atStartOfDay(sourceCalendar.getTimezone());

    for (ICalendarEvent event : sourceCalendar.getEventsBetween(rangeStart, rangeEnd)) {
      long durationMinutes = Duration.between(
              event.getStartDateTime(), event.getEndDateTime()).toMinutes();

      ZonedDateTime newStart = ZonedDateTime.from(event.getStartDateTime())
              .plusDays(daysOffset)
              .withZoneSameInstant(targetCalendar.getTimezone());
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

  @Override
  public List<ICalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<ICalendarEvent> getEventsOnDate(Temporal date) {
    List<ICalendarEvent> result = new ArrayList<>();
    LocalDate queryDate = LocalDate.from(date);  // Convert Temporal to LocalDate

    for (ICalendarEvent event : events) {
      LocalDate eventStartDate = ZonedDateTime.from(event.getStartDateTime()).toLocalDate();
      LocalDate eventEndDate = ZonedDateTime.from(event.getEndDateTime()).toLocalDate();

      if (!eventStartDate.isAfter(queryDate) && !eventEndDate.isBefore(queryDate)) {
        result.add(event);
      }
    }
    return result;
  }


  @Override
  public List<ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
    List<ICalendarEvent> result = new ArrayList<>();
    ZonedDateTime startZoned = ZonedDateTime.from(start);
    ZonedDateTime endZoned = ZonedDateTime.from(end);

    for (ICalendarEvent event : events) {
      ZonedDateTime eventStart = ZonedDateTime.from(event.getStartDateTime());
      ZonedDateTime eventEnd = ZonedDateTime.from(event.getEndDateTime());

      if (eventStart.isBefore(endZoned) && eventEnd.isAfter(startZoned)) {
        result.add(event);
      }
    }
    return result;
  }


  @Override
  public boolean isBusyAt(Temporal dateTime) {
    ZonedDateTime checkTime = ZonedDateTime.from(dateTime);

    for (ICalendarEvent event : events) {
      ZonedDateTime startZoned = ZonedDateTime.from(event.getStartDateTime());
      ZonedDateTime endZoned = ZonedDateTime.from(event.getEndDateTime());

      if (!checkTime.isBefore(startZoned) && checkTime.isBefore(endZoned)) {
        return true;
      }
    }
    return false;
  }

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

  @Override
  public boolean editSingleEvent(String property, String eventName,
                                 Temporal originalStart, Temporal originalEnd,
                                 String newValue) {
    ZonedDateTime originalStartZoned = ZonedDateTime.from(originalStart);
    ZonedDateTime originalEndZoned = ZonedDateTime.from(originalEnd);

    for (ICalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStartZoned) &&
              event.getEndDateTime().equals(originalEndZoned)) {

        SingleEvent updated = ((SingleEvent) event).withUpdatedProperty(property, newValue);

        if (ZonedDateTime.from(updated.getStartDateTime()).isAfter(ZonedDateTime.from(updated.getEndDateTime())) ||
                hasConflictExcept(event, updated)) {
          return false;
        }

        events.remove(event);
        events.add(updated);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean editEventsFrom(String property, String eventName,
                                Temporal fromDateTime, String newValue) {
    ZonedDateTime fromZoned = ZonedDateTime.from(fromDateTime);
    List<SingleEvent> toUpdate = new ArrayList<>();

    for (ICalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              !ZonedDateTime.from(event.getStartDateTime()).isBefore(fromZoned)) {
        toUpdate.add((SingleEvent) event);
      }
    }

    if (toUpdate.isEmpty()) {
      return false;
    }

    List<SingleEvent> updatedEvents = new ArrayList<>();
    for (SingleEvent event : toUpdate) {
      SingleEvent updated = event.withUpdatedProperty(property, newValue);
      if (ZonedDateTime.from(updated.getStartDateTime()).isAfter(ZonedDateTime.from(updated.getEndDateTime())) ||
              hasConflictExcept(event, updated)) {
        return false;
      }
      updatedEvents.add(updated);
    }

    events.removeAll(toUpdate);
    events.addAll(updatedEvents);
    return true;
  }

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
      if (ZonedDateTime.from(updated.getStartDateTime()).isAfter(ZonedDateTime.from(updated.getEndDateTime())) ||
              hasConflictExcept(event, updated)) {
        return false;
      }
      updatedEvents.add(updated);
    }

    events.removeAll(toUpdate);
    events.addAll(updatedEvents);
    return true;
  }

  public boolean editRecurringEvent(String eventName, String property, String newValue) {
    RecurringEvent existingEvent = recurringMap.get(eventName);
    if (existingEvent == null) {
      return false;
    }

    RecurringEvent updatedEvent = existingEvent.withUpdatedProperty(property, newValue);
    List<SingleEvent> newOccurrences = updatedEvent.generateOccurrences(UUID.randomUUID().toString());

    for (SingleEvent newOccurrence : newOccurrences) {
      if (ZonedDateTime.from(newOccurrence.getStartDateTime()).isAfter(ZonedDateTime.from(newOccurrence.getEndDateTime())) ||
              hasConflictExceptRecurring(eventName, newOccurrence)) {
        return false;
      }
    }

    events.removeIf(e -> e instanceof SingleEvent &&
            eventName.equals(e.getSubject()) &&
            ((SingleEvent)e).getSeriesId() != null);

    events.addAll(newOccurrences);
    recurringMap.put(eventName, updatedEvent);
    return true;
  }

  private boolean hasConflictExcept(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
    for (ICalendarEvent existing : events) {
      if (!existing.equals(oldEvent) && ConflictChecker.hasConflict(existing, newEvent)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasConflictExceptRecurring(String eventName, ICalendarEvent newEvent) {
    for (ICalendarEvent existing : events) {
      if (!existing.getSubject().equals(eventName) && ConflictChecker.hasConflict(existing, newEvent)) {
        return true;
      }
    }
    return false;
  }

  private boolean isRecurringProperty(String property) {
    return property.equalsIgnoreCase("repeattimes") ||
            property.equalsIgnoreCase("repeatuntil") ||
            property.equalsIgnoreCase("repeatingdays");
  }
}

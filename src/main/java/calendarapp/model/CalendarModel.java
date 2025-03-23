package calendarapp.model;

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.model.ConflictChecker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalendarModel implements ICalendarModel {
  private final List<CalendarEvent> events = new ArrayList<>();
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
    // Update timezone field
    ZoneId oldTimezone = this.timezone;
    this.timezone = newTimezone;

    List<CalendarEvent> updatedEvents = new ArrayList<>();

    for (CalendarEvent event : events) {
      ZonedDateTime oldStart = event.getStartDateTime().withZoneSameInstant(newTimezone);
      ZonedDateTime oldEnd = event.getEndDateTime().withZoneSameInstant(newTimezone);

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
        // Keep recurring events as is (because they are templates)
        updatedEvents.add(event);
      }
    }

    // Replace all events with updated ones
    events.clear();
    events.addAll(updatedEvents);

    // Handle recurringMap: regenerate occurrences in new timezone
    Map<String, RecurringEvent> updatedRecurringMap = new HashMap<>();
    for (Map.Entry<String, RecurringEvent> entry : recurringMap.entrySet()) {
      RecurringEvent recurringEvent = entry.getValue();

      // Remove old occurrences
      events.removeIf(e -> e instanceof SingleEvent &&
              e.getSubject().equals(entry.getKey()) &&
              ((SingleEvent) e).getSeriesId() != null);

      // Recreate recurring event occurrences in new timezone
      RecurringEvent updatedRecurringEvent = recurringEvent.withUpdatedTimezone(newTimezone);
      List<SingleEvent> newOccurrences = updatedRecurringEvent.generateOccurrences(UUID.randomUUID().toString());

      events.addAll(newOccurrences);
      updatedRecurringMap.put(entry.getKey(), updatedRecurringEvent);
    }

    // Update recurring map
    recurringMap.clear();
    recurringMap.putAll(updatedRecurringMap);
  }


  public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName, ZonedDateTime sourceDateTime, CalendarModel targetCalendar, ZonedDateTime targetDateTime) {
    for (CalendarEvent event : sourceCalendar.getEvents()) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(sourceDateTime)) {

        // ✅ Correct duration calculation (preserves timezone correctness)
        long durationMinutes = Duration.between(
                event.getStartDateTime(),
                event.getEndDateTime()
        ).toMinutes();

        // ✅ Set new start explicitly to target timezone (already done by manager)
        ZonedDateTime newStart = targetDateTime;
        ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

        CalendarEvent copiedEvent = new SingleEvent(
                event.getSubject(),
                newStart,
                newEnd,
                event.getDescription(),
                event.getLocation(),
                event.isPublic(),
                event.isAllDay(),
                null
        );

        if (!targetCalendar.addEvent(copiedEvent, false)) {
          return false;
        }
        return true;
      }
    }
    return false;
  }

  public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, LocalDate sourceDate, CalendarModel targetCalendar, LocalDate targetDate) {
    boolean allCopied = true;
    for (CalendarEvent event : sourceCalendar.getEventsOnDate(sourceDate)) {

      long durationMinutes = Duration.between(event.getStartDateTime(), event.getEndDateTime()).toMinutes();

      ZonedDateTime newStart = ZonedDateTime.of(
              targetDate,
              event.getStartDateTime().toLocalTime(),
              targetCalendar.getTimezone()
      );

      ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

      CalendarEvent copiedEvent = new SingleEvent(
              event.getSubject(),
              newStart,
              newEnd,
              event.getDescription(),
              event.getLocation(),
              event.isPublic(),
              event.isAllDay(),
              null
      );

      if (!targetCalendar.addEvent(copiedEvent, false)) {
        allCopied = false;
      }
    }
    return allCopied;
  }

  public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, LocalDate startDate, LocalDate endDate, CalendarModel targetCalendar, LocalDate targetStartDate) {
    boolean allCopied = true;
    long daysOffset = ChronoUnit.DAYS.between(startDate, targetStartDate);

    for (CalendarEvent event : sourceCalendar.getEventsBetween(
            startDate.atStartOfDay(sourceCalendar.getTimezone()),
            endDate.plusDays(1).atStartOfDay(sourceCalendar.getTimezone()))) {

      long durationMinutes = Duration.between(event.getStartDateTime(), event.getEndDateTime()).toMinutes();

      ZonedDateTime newStart = event.getStartDateTime().plusDays(daysOffset).withZoneSameInstant(targetCalendar.getTimezone());
      ZonedDateTime newEnd = newStart.plusMinutes(durationMinutes);

      CalendarEvent copiedEvent = new SingleEvent(
              event.getSubject(),
              newStart,
              newEnd,
              event.getDescription(),
              event.getLocation(),
              event.isPublic(),
              event.isAllDay(),
              null
      );

      if (!targetCalendar.addEvent(copiedEvent, false)) {
        allCopied = false;
      }
    }
    return allCopied;
  }


  @Override
  public boolean addEvent(CalendarEvent event, boolean autoDecline) {
    if (duplicateExists(event)) {
      throw new IllegalArgumentException("Duplicate event detected.");
    }
    for (CalendarEvent e : events) {
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
      for (CalendarEvent existingEvent : events) {
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
  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (!event.getStartDateTime().toLocalDate().isAfter(date) &&
              !event.getEndDateTime().toLocalDate().isBefore(date)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public List<CalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().isBefore(end) && event.getEndDateTime().isAfter(start)) {
        result.add(event);
      }
    }
    return result;
  }

  @Override
  public boolean isBusyAt(ZonedDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (!dateTime.isBefore(event.getStartDateTime()) &&
              !dateTime.isAfter(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  private boolean duplicateExists(CalendarEvent newEvent) {
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(newEvent.getSubject()) &&
              event.getStartDateTime().equals(newEvent.getStartDateTime()) &&
              event.getEndDateTime().equals(newEvent.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean editEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    events.remove(oldEvent);
    for (CalendarEvent event : events) {
      if (ConflictChecker.hasConflict(event, newEvent)) {
        events.add(oldEvent);
        return false;
      }
    }
    events.add(newEvent);
    return true;
  }

  public boolean editSingleEvent(String property, String eventName,
                                 ZonedDateTime originalStart, ZonedDateTime originalEnd,
                                 String newValue) {
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {

        SingleEvent updated = ((SingleEvent) event).withUpdatedProperty(property, newValue);

        if (updated.getStartDateTime().isAfter(updated.getEndDateTime())) {
          return false;
        }
        if (hasConflictExcept(event, updated)) {
          return false;
        }

        events.remove(event);
        events.add(updated);
        return true;
      }
    }
    return false;
  }

  public boolean editEventsFrom(String property, String eventName,
                                ZonedDateTime fromDateTime, String newValue) {
    List<SingleEvent> toUpdate = new ArrayList<>();
    for (CalendarEvent event : events) {
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
    for (CalendarEvent event : events) {
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

    // Check conflicts and date validity for all new occurrences
    for (SingleEvent newOccurrence : newOccurrences) {
      if (newOccurrence.getStartDateTime().isAfter(newOccurrence.getEndDateTime()) ||
              hasConflictExceptRecurring(eventName, newOccurrence)) {
        return false; // Conflict or invalid dates detected
      }
    }

    // Remove old occurrences and add new validated occurrences
    events.removeIf(e -> e instanceof SingleEvent &&
            eventName.equals(e.getSubject()) &&
            ((SingleEvent)e).getSeriesId() != null);

    events.addAll(newOccurrences);
    recurringMap.put(eventName, updatedEvent);
    return true;
  }

  private boolean hasConflictExcept(CalendarEvent oldEvent, CalendarEvent newEvent) {
    for (CalendarEvent existing : events) {
      if (!existing.equals(oldEvent) && ConflictChecker.hasConflict(existing, newEvent)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasConflictExceptRecurring(String eventName, CalendarEvent newEvent) {
    for (CalendarEvent existing : events) {
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

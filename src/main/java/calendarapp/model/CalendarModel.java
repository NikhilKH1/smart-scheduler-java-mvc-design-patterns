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

public class CalendarModel implements ICalendarModel {
  // Primary storage for all events (both single and recurring occurrences)
  private final List<CalendarEvent> events;
  // Store recurring event rules by event subject (assuming unique subject for recurring events)
  private final HashMap<String, RecurringEvent> recurringMap;

  public CalendarModel() {
    events = new ArrayList<>();
    recurringMap = new HashMap<>();
  }

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

  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    // Generate a unique seriesId for this recurring event and generate occurrences.
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

  @Override
  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    List<CalendarEvent> result = new ArrayList<>();
    for (CalendarEvent event : events) {
      LocalDate start = event.getStartDateTime().toLocalDate();
      LocalDate end = event.getEndDateTime().toLocalDate();
      if ((start.equals(date) || start.isBefore(date)) &&
              (end.equals(date) || end.isAfter(date))) {
        result.add(event);
      }
    }
    return result;
  }

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

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (!dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(event.getEndDateTime())) {
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
    for (CalendarEvent e : events) {
      if (ConflictChecker.hasConflict(e, newEvent)) {
        events.add(oldEvent); // rollback
        return false;
      }
    }
    events.add(newEvent);
    return true;
  }

  // Edit a single event identified by subject, start, and end.
  public boolean editSingleEvent(String property, String eventName, LocalDateTime originalStart, LocalDateTime originalEnd, String newValue) {
    List<CalendarEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {
        matching.add(event);
      }
    }
    if (matching.isEmpty()) return false;
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old, property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  // Edit events that start exactly at a given time.
  public boolean editEventsFrom(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    List<CalendarEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(fromDateTime)) {
        if (event instanceof SingleEvent) {
          matching.add(event);
        }
      }
    }
    if (matching.isEmpty()) return false;
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old, property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  // Edit all events with the given subject.
  public boolean editEventsAll(String property, String eventName, String newValue) {
    // For recurrence properties, delegate to editRecurringEvent.
    if (property.equalsIgnoreCase("repeattimes") ||
            property.equalsIgnoreCase("repeatuntil") ||
            property.equalsIgnoreCase("repeatingdays")) {
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
    if (matching.isEmpty()) return false;
    events.removeAll(matching);
    List<CalendarEvent> updated = new ArrayList<>();
    for (CalendarEvent old : matching) {
      SingleEvent updatedEvent = ModelHelper.createUpdatedEvent((SingleEvent) old, property, newValue);
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

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
        recurringEvent = new RecurringEvent(
                recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(),
                recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(),
                newCount,
                recurringEvent.getRepeatUntil(),
                recurringEvent.getDescription(),
                recurringEvent.getLocation(),
                recurringEvent.isPublic(),
                recurringEvent.isAllDay()
        );
        break;
      case "repeatuntil":
        LocalDateTime newUntil = LocalDateTime.parse(newValue);
        recurringEvent = new RecurringEvent(
                recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(),
                recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(),
                recurringEvent.getRepeatCount(),
                newUntil,
                recurringEvent.getDescription(),
                recurringEvent.getLocation(),
                recurringEvent.isPublic(),
                recurringEvent.isAllDay()
        );
        break;
      case "repeatingdays":
        recurringEvent = new RecurringEvent(
                recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(),
                recurringEvent.getEndDateTime(),
                newValue.toUpperCase(),
                recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(),
                recurringEvent.getDescription(),
                recurringEvent.getLocation(),
                recurringEvent.isPublic(),
                recurringEvent.isAllDay()
        );
        break;
      case "description":
        recurringEvent = new RecurringEvent(
                recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(),
                recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(),
                recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(),
                newValue,
                recurringEvent.getLocation(),
                recurringEvent.isPublic(),
                recurringEvent.isAllDay()
        );
        break;
      case "location":
        recurringEvent = new RecurringEvent(
                recurringEvent.getSubject(),
                recurringEvent.getStartDateTime(),
                recurringEvent.getEndDateTime(),
                recurringEvent.getWeekdays(),
                recurringEvent.getRepeatCount(),
                recurringEvent.getRepeatUntil(),
                recurringEvent.getDescription(),
                newValue,
                recurringEvent.isPublic(),
                recurringEvent.isAllDay()
        );
        break;
      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }

    // Remove all occurrences for this recurring event
    events.removeIf(e -> e instanceof SingleEvent &&
            ((SingleEvent) e).getSeriesId() != null &&
            ((SingleEvent) e).getSubject().equals(eventName));

    // Generate new occurrences with a new seriesId
    String newSeriesId = UUID.randomUUID().toString();
    List<SingleEvent> newOccurrences = recurringEvent.generateOccurrences(newSeriesId);
    events.addAll(newOccurrences);
    recurringMap.put(eventName, recurringEvent);
    return true;
  }

}

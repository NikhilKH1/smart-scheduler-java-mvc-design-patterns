package calendarapp.model;

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Optimized model representing calendar data.
 */
public class CalendarModel implements ICalendarModel {
  private final List<CalendarEvent> events = new ArrayList<>();
  private final Map<String, RecurringEvent> recurringMap = new HashMap<>();

  @Override
  public boolean addEvent(CalendarEvent event, boolean unusedAutoDecline) {
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
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean unusedAutoDecline) {
    String seriesId = UUID.randomUUID().toString();
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences(seriesId);

    // Check conflicts
    for (SingleEvent occ : occurrences) {
      for (CalendarEvent e : events) {
        if (ConflictChecker.hasConflict(e, occ)) {
          return false;
        }
      }
    }

    // Check duplicates
    for (SingleEvent occ : occurrences) {
      if (duplicateExists(occ)) {
        throw new IllegalArgumentException("Duplicate event in recurring series.");
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
      if (!start.isAfter(date) && !end.isBefore(date)) {
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
    // Check for conflicts (excluding old event itself)
    for (CalendarEvent e : events) {
      if (!e.equals(oldEvent) && ConflictChecker.hasConflict(e, newEvent)) {
        return false;
      }
    }

    events.remove(oldEvent);
    events.add(newEvent);
    return true;
  }

  public boolean editSingleEvent(String property, String eventName,
                                 LocalDateTime originalStart, LocalDateTime originalEnd,
                                 String newValue) {
    List<SingleEvent> matching = new ArrayList<>();

    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {
        matching.add((SingleEvent) event);
      }
    }

    if (matching.isEmpty()) {
      return false;
    }

    events.removeAll(matching);
    List<SingleEvent> updated = new ArrayList<>();
    for (SingleEvent old : matching) {
      SingleEvent updatedEvent = old.withUpdatedProperty(property, newValue);

      // Check for conflicts with other events
      for (CalendarEvent e : events) {
        if (ConflictChecker.hasConflict(e, updatedEvent)) {
          events.addAll(matching); // Restore original
          return false;
        }
      }
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  public boolean editEventsFrom(String property, String eventName,
                                LocalDateTime fromDateTime, String newValue) {
    List<SingleEvent> matching = new ArrayList<>();

    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              !event.getStartDateTime().isBefore(fromDateTime)) {
        matching.add((SingleEvent) event);
      }
    }

    if (matching.isEmpty()) {
      return false;
    }

    events.removeAll(matching);
    List<SingleEvent> updated = new ArrayList<>();
    for (SingleEvent old : matching) {
      SingleEvent updatedEvent = old.withUpdatedProperty(property, newValue);
      for (CalendarEvent e : events) {
        if (ConflictChecker.hasConflict(e, updatedEvent)) {
          events.addAll(matching); // Restore original
          return false;
        }
      }
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  public boolean editEventsAll(String property, String eventName, String newValue) {
    if (isRecurringProperty(property)) {
      return editRecurringEvent(eventName, property, newValue);
    }

    List<SingleEvent> matching = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent && event.getSubject().equals(eventName)) {
        matching.add((SingleEvent) event);
      }
    }

    if (matching.isEmpty()) {
      return false;
    }

    events.removeAll(matching);
    List<SingleEvent> updated = new ArrayList<>();
    for (SingleEvent old : matching) {
      SingleEvent updatedEvent = old.withUpdatedProperty(property, newValue);
      for (CalendarEvent e : events) {
        if (ConflictChecker.hasConflict(e, updatedEvent)) {
          events.addAll(matching); // Restore
          return false;
        }
      }
      updated.add(updatedEvent);
    }
    events.addAll(updated);
    return true;
  }

  private boolean isRecurringProperty(String property) {
    String p = property.toLowerCase();
    return p.equals("repeattimes") || p.equals("repeatuntil") || p.equals("repeatingdays");
  }

  public boolean editRecurringEvent(String eventName, String property, String newValue) {
    RecurringEvent existingEvent = recurringMap.get(eventName);
    if (existingEvent == null) {
      throw new IllegalArgumentException("Recurring event not found: " + eventName);
    }

    RecurringEvent updatedEvent = existingEvent.withUpdatedProperty(property, newValue);

    // Remove old occurrences
    List<SingleEvent> toRemove = new ArrayList<>();
    for (CalendarEvent e : events) {
      if (e instanceof SingleEvent &&
              ((SingleEvent) e).getSeriesId() != null &&
              e.getSubject().equals(existingEvent.getSubject())) {
        toRemove.add((SingleEvent) e);
      }
    }
    events.removeAll(toRemove);

    String newSeriesId = UUID.randomUUID().toString();
    List<SingleEvent> newOccurrences = updatedEvent.generateOccurrences(newSeriesId);

    for (SingleEvent occ : newOccurrences) {
      for (CalendarEvent e : events) {
        if (ConflictChecker.hasConflict(e, occ)) {
          events.addAll(toRemove);
          return false;
        }
      }
    }

    events.addAll(newOccurrences);
    recurringMap.put(eventName, updatedEvent);
    return true;
  }
}

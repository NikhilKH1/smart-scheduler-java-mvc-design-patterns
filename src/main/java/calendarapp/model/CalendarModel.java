package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarModel {
  private final List<CalendarEvent> events;

  public CalendarModel() {
    events = new ArrayList<>();
  }

  public boolean addEvent(CalendarEvent event, boolean autoDecline) {
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

  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    for (SingleEvent occurrence : occurrences) {
      for (CalendarEvent existingEvent : events) {
        if (ConflictChecker.hasConflict(existingEvent, occurrence)) {
          if (autoDecline) {
            return false;
          }
        }
      }
    }
    events.addAll(occurrences);
    return true;
  }

  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }

  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    return events.stream()
            .filter(event -> event.getStartDateTime().isBefore(endOfDay)
                    && event.getEndDateTime().isAfter(startOfDay))
            .collect(Collectors.toList());
  }

  public List<CalendarEvent> getEventsBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return events.stream()
            .filter(event -> event.getStartDateTime().isBefore(endDateTime.plusSeconds(1)) // Include events ending exactly at the end time
                    && event.getEndDateTime().isAfter(startDateTime.minusSeconds(1))) // Include events starting exactly at the start time
            .collect(Collectors.toList());
  }


  public boolean isBusyAt(LocalDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (!dateTime.isBefore(event.getStartDateTime()) &&
              !dateTime.isAfter(event.getEndDateTime())) {
        return true;
      }
    }
    return false;
  }

  // Finds an event by subject and exact start/end times.
  public CalendarEvent findEvent(String subject, LocalDateTime start, LocalDateTime end) {
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(subject)
              && event.getStartDateTime().equals(start)
              && event.getEndDateTime().equals(end)) {
        return event;
      }
    }
    return null;
  }

  // Edits a single event by replacing oldEvent with newEvent after conflict checking.
  public boolean editEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    events.remove(oldEvent);
    for (CalendarEvent e : events) {
      if (ConflictChecker.hasConflict(e, newEvent)) {
        events.add(oldEvent); // Restore if conflict found.
        return false;
      }
    }
    events.add(newEvent);
    return true;
  }

  public boolean editSingleEvent(String property, String eventName, LocalDateTime originalStart, LocalDateTime originalEnd, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();

    // Identify all matching single events
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {
        matchingEvents.add(event);
      }
    }

    if (matchingEvents.isEmpty()) {
      return false; // No matching event found
    }

    // Remove matching events before updating
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();

    for (CalendarEvent oldEvent : matchingEvents) {
      SingleEvent updatedEvent = createUpdatedEvent((SingleEvent) oldEvent, property, newValue);
      updatedEvents.add(updatedEvent);
    }

    // Add updated events back
    events.addAll(updatedEvents);
    return true;
  }


  // Edit events that start exactly at the given date/time.
  public boolean editEventsFrom(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    // Gather matching events.
    List<CalendarEvent> matchingEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(fromDateTime)) {
        if (event instanceof SingleEvent) {
          matchingEvents.add(event);
        }
      }
    }
    if (matchingEvents.isEmpty()) return false;

    // Temporarily remove matching events.
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();
    for (CalendarEvent event : matchingEvents) {
      SingleEvent se = (SingleEvent) event;
      SingleEvent newEvent = createUpdatedEvent(se, property, newValue);
      // Check conflict against events not being updated.
      boolean conflict = false;
      for (CalendarEvent other : events) {
        if (ConflictChecker.hasConflict(other, newEvent)) {
          conflict = true;
          break;
        }
      }
      if (conflict) {
        // Conflict detected: roll back the removal.
        events.addAll(matchingEvents);
        return false;
      }
      updatedEvents.add(newEvent);
    }
    // No conflicts; add updated events back.
    events.addAll(updatedEvents);
    return true;
  }

  public boolean editEventsAll(String property, String eventName, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();

    // Find all instances of the event
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName)) {
        matchingEvents.add(event);
      }
    }

    if (matchingEvents.isEmpty()) {
      return false; // No matching events found
    }

    // Remove matching events before updating
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();

    for (CalendarEvent oldEvent : matchingEvents) {
      if (oldEvent instanceof SingleEvent) {
        updatedEvents.add(createUpdatedEvent((SingleEvent) oldEvent, property, newValue));
      } else if (oldEvent instanceof RecurringEvent) {
        // Update all occurrences of the recurring event
        RecurringEvent recurring = (RecurringEvent) oldEvent;
        List<SingleEvent> occurrences = recurring.generateOccurrences();
        for (SingleEvent occurrence : occurrences) {
          updatedEvents.add(createUpdatedEvent(occurrence, property, newValue));
        }
      }
    }

    // Add updated events back
    events.addAll(updatedEvents);
    return true;
  }

  // Helper method: creates a new SingleEvent with the updated property.
  private SingleEvent createUpdatedEvent(SingleEvent oldEvent, String property, String newValue) {
    String newSubject = oldEvent.getSubject();
    String newDescription = oldEvent.getDescription();
    String newLocation = oldEvent.getLocation();

    switch (property) {
      case "name":
        newSubject = newValue;
        break;
      case "description":
        newDescription = newValue;
        break;
      case "location":
        newLocation = newValue;
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }

    return new SingleEvent(
            newSubject,
            oldEvent.getStartDateTime(),
            oldEvent.getEndDateTime(),
            newDescription,
            newLocation,
            oldEvent.isPublic(),
            oldEvent.isAllDay()
    );
  }

}

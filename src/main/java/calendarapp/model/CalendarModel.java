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
    // Check for duplicate event.
    if (duplicateExists(event)) {
      throw new IllegalArgumentException("Duplicate event: subject, start and end are identical. Please modify at least one field.");
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

  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    for (SingleEvent occurrence : occurrences) {
      if (duplicateExists(occurrence)) {
        throw new IllegalArgumentException("Duplicate event in recurring series: subject, start and end are identical. Please modify at least one field.");
      }
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
            .filter(event -> event.getStartDateTime().isBefore(endDateTime)
                    && event.getEndDateTime().isAfter(startDateTime))
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

  // Returns true if an event with the same subject, start, and end already exists.
  private boolean duplicateExists(CalendarEvent newEvent) {
    for (CalendarEvent existing : events) {
      if (existing.getSubject().equals(newEvent.getSubject()) &&
              existing.getStartDateTime().equals(newEvent.getStartDateTime()) &&
              existing.getEndDateTime().equals(newEvent.getEndDateTime())) {
        return true;
      }
    }
    return false;
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

  // Edit a single event (by unique identification).
  public boolean editSingleEvent(String property, String eventName, LocalDateTime originalStart, LocalDateTime originalEnd, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event instanceof SingleEvent &&
              event.getSubject().equals(eventName) &&
              event.getStartDateTime().equals(originalStart) &&
              event.getEndDateTime().equals(originalEnd)) {
        matchingEvents.add(event);
      }
    }
    if (matchingEvents.isEmpty()) {
      return false;
    }
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();
    for (CalendarEvent oldEvent : matchingEvents) {
      SingleEvent updatedEvent = createUpdatedEvent((SingleEvent) oldEvent, property, newValue);
      updatedEvents.add(updatedEvent);
    }
    events.addAll(updatedEvents);
    return true;
  }

  // Edit events that start exactly at the given date/time.
  public boolean editEventsFrom(String property, String eventName, LocalDateTime fromDateTime, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName) && event.getStartDateTime().equals(fromDateTime)) {
        if (event instanceof SingleEvent) {
          matchingEvents.add(event);
        }
      }
    }
    if (matchingEvents.isEmpty()) return false;
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();
    for (CalendarEvent event : matchingEvents) {
      SingleEvent se = (SingleEvent) event;
      SingleEvent newEvent = createUpdatedEvent(se, property, newValue);
      boolean conflict = false;
      for (CalendarEvent other : events) {
        if (ConflictChecker.hasConflict(other, newEvent)) {
          conflict = true;
          break;
        }
      }
      if (conflict) {
        events.addAll(matchingEvents);
        return false;
      }
      updatedEvents.add(newEvent);
    }
    events.addAll(updatedEvents);
    return true;
  }

  // Edit all events with the given event name.
  public boolean editEventsAll(String property, String eventName, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName)) {
        matchingEvents.add(event);
      }
    }
    if (matchingEvents.isEmpty()) return false;
    events.removeAll(matchingEvents);
    List<CalendarEvent> updatedEvents = new ArrayList<>();
    for (CalendarEvent event : matchingEvents) {
      if (event instanceof SingleEvent) {
        updatedEvents.add(createUpdatedEvent((SingleEvent) event, property, newValue));
      }
    }
    events.addAll(updatedEvents);
    return true;
  }

  // Helper method: creates a new SingleEvent with the updated property.
  // Now supports editing:
  // - name, description, location
  // - startDateTime, endDateTime (full date-time)
  // - startDate, endDate (only the date portion)
  // - startTime, endTime (only the time portion)
  // - public (event visibility)
  private SingleEvent createUpdatedEvent(SingleEvent oldEvent, String property, String newValue) {
    String newSubject = oldEvent.getSubject();
    String newDescription = oldEvent.getDescription();
    String newLocation = oldEvent.getLocation();
    LocalDateTime newStartDateTime = oldEvent.getStartDateTime();
    LocalDateTime newEndDateTime = oldEvent.getEndDateTime();
    boolean newIsPublic = oldEvent.isPublic();

    switch (property.toLowerCase()) {
      case "name":
        newSubject = newValue;
        break;
      case "description":
        newDescription = newValue;
        break;
      case "location":
        newLocation = newValue;
        break;
      case "startdatetime":
        newStartDateTime = LocalDateTime.parse(newValue);
        if (newStartDateTime.isAfter(newEndDateTime)) {
          throw new IllegalArgumentException("Start time cannot be after end time.");
        }
        break;
      case "enddatetime":
        newEndDateTime = LocalDateTime.parse(newValue);
        if (newEndDateTime.isBefore(newStartDateTime)) {
          throw new IllegalArgumentException("End time cannot be before start time.");
        }
        break;
      case "startdate":
        newStartDateTime = LocalDate.parse(newValue).atTime(newStartDateTime.toLocalTime());
        break;
      case "enddate":
        newEndDateTime = LocalDate.parse(newValue).atTime(newEndDateTime.toLocalTime());
        break;
      case "starttime":
        newStartDateTime = newStartDateTime.toLocalDate().atTime(java.time.LocalTime.parse(newValue));
        break;
      case "endtime":
        newEndDateTime = newEndDateTime.toLocalDate().atTime(java.time.LocalTime.parse(newValue));
        break;
      case "public":
        newIsPublic = Boolean.parseBoolean(newValue);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }

    return new SingleEvent(
            newSubject,
            newStartDateTime,
            newEndDateTime,
            newDescription,
            newLocation,
            newIsPublic,
            oldEvent.isAllDay(),
            oldEvent.getSeriesId()
    );
  }
}

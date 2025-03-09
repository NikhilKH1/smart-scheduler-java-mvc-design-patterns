package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarModel {
  private final List<CalendarEvent> events;

  // Map to store recurrence parameters for recurring events by seriesId.
  private static final Map<String, RecurrenceParameters> recurrenceMap = new HashMap<>();

  public static class RecurrenceParameters {
    public String weekdays;
    public int repeatCount;
    public LocalDateTime repeatUntil;
  }

  public CalendarModel() {
    events = new ArrayList<>();
  }

  public boolean addEvent(CalendarEvent event, boolean autoDecline) {
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
    // Store recurrence parameters using the seriesId from the first occurrence.
    if (!occurrences.isEmpty()) {
      String seriesId = occurrences.get(0).getSeriesId();
      RecurrenceParameters params = new RecurrenceParameters();
      params.weekdays = recurringEvent.getWeekdays();
      params.repeatCount = recurringEvent.getRepeatCount();
      params.repeatUntil = recurringEvent.getRepeatUntil();
      recurrenceMap.put(seriesId, params);
    }
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

  public boolean editEventsAll(String property, String eventName, String newValue) {
    List<CalendarEvent> matchingEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getSubject().equals(eventName)) {
        matchingEvents.add(event);
      }
    }
    if (matchingEvents.isEmpty()) return false;

    // If the property is a recurrence property, update recurrence parameters.
    if (property.equalsIgnoreCase("repeattimes") ||
            property.equalsIgnoreCase("repeatuntil") ||
            property.equalsIgnoreCase("repeatingdays")) {
      // Collect unique series IDs from the matching events.
      Set<String> seriesIds = new HashSet<>();
      for (CalendarEvent event : matchingEvents) {
        if (event instanceof SingleEvent) {
          String seriesId = ((SingleEvent) event).getSeriesId();
          if (seriesId != null) {
            seriesIds.add(seriesId);
          }
        }
      }
      boolean allSuccess = true;
      for (String seriesId : seriesIds) {
        boolean res = editRecurringProperties(seriesId, property, newValue);
        if (!res) {
          allSuccess = false;
        }
      }
      return allSuccess;
    } else {
      // Otherwise, update properties normally.
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
  }


  // NEW: Edit recurrence properties for recurring events.
  // Supported properties: "repeattimes", "repeatuntil", "repeatingdays"
  public boolean editRecurringProperties(String seriesId, String property, String newValue) {
    RecurrenceParameters params = recurrenceMap.get(seriesId);
    if (params == null) {
      throw new IllegalArgumentException("No recurrence parameters found for series: " + seriesId);
    }
    switch (property.toLowerCase()) {
      case "repeattimes":
        int newCount = Integer.parseInt(newValue);
        if (newCount <= 0) {
          throw new IllegalArgumentException("Repeat count must be a positive number.");
        }
        params.repeatCount = newCount;
        break;
      case "repeatuntil":
        params.repeatUntil = LocalDateTime.parse(newValue);
        break;
      case "repeatingdays":
        params.weekdays = newValue.toUpperCase();
        break;
      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }
    // Find the base event for this series (the earliest event with the given seriesId).
    SingleEvent baseEvent = null;
    for (CalendarEvent e : events) {
      if (e instanceof SingleEvent) {
        SingleEvent se = (SingleEvent) e;
        if (seriesId.equals(se.getSeriesId())) {
          if (baseEvent == null || se.getStartDateTime().isBefore(baseEvent.getStartDateTime())) {
            baseEvent = se;
          }
        }
      }
    }
    if (baseEvent == null) return false;
    // Rebuild the recurring event with the updated recurrence parameters.
    RecurringEvent newRecurring = new RecurringEvent(
            baseEvent.getSubject(),
            baseEvent.getStartDateTime(),
            baseEvent.getEndDateTime(),
            baseEvent.getDescription(),
            baseEvent.getLocation(),
            true, // assuming public
            baseEvent.isAllDay(),
            params.weekdays,
            params.repeatCount,
            params.repeatUntil
    );
    // Remove all events in this series.
    events.removeIf(e -> e instanceof SingleEvent && seriesId.equals(((SingleEvent) e).getSeriesId()));
    List<SingleEvent> newOccurrences = newRecurring.generateOccurrences();
    events.addAll(newOccurrences);
    recurrenceMap.put(seriesId, params);
    return true;
  }

  // Helper method: creates a new SingleEvent with the updated property.
  // Supports editing: name, description, location,
  // startDateTime, endDateTime, startDate, endDate, startTime, endTime, public.
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
        newStartDateTime = newStartDateTime.toLocalDate().atTime(LocalTime.parse(newValue));
        break;
      case "endtime":
        newEndDateTime = newEndDateTime.toLocalDate().atTime(LocalTime.parse(newValue));
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

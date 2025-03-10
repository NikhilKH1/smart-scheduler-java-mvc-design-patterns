package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;

public interface ICalendarModel {
  boolean addEvent(CalendarEvent event, boolean autoDecline);
  boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline);
  List<CalendarEvent> getEvents();
  List<CalendarEvent> getEventsOnDate(LocalDate date);
  List<CalendarEvent> getEventsBetween(LocalDateTime start, LocalDateTime end);
  boolean isBusyAt(LocalDateTime dateTime);
  boolean editEvent(CalendarEvent oldEvent, CalendarEvent newEvent);
  // Recurrence editing: update recurrence properties for a recurring event identified by event name.
  boolean editRecurringEvent(String eventName, String property, String newValue);
}

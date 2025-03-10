package calendarapp.model;

import calendarapp.model.event.CalendarEvent;
import java.time.LocalDateTime;

public class EventValidator {
  /**
   * Validates a single event's properties.
   * @param event The event to validate.
   * @throws IllegalArgumentException if validation fails.
   */
  public static void validateSingleEvent(CalendarEvent event) {
    if (event.getSubject() == null || event.getSubject().trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be empty.");
    }
    if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
      throw new IllegalArgumentException("Event must have start and end times.");
    }
    if (!event.getStartDateTime().isBefore(event.getEndDateTime())) {
      throw new IllegalArgumentException("Event start time must be before end time.");
    }
  }

  /**
   * Validates recurrence settings for a recurring event.
   * @param start The initial start date/time.
   * @param end The initial end date/time.
   * @param weekdays The recurrence days string.
   * @param repeatCount The number of times to repeat.
   * @param repeatUntil The end boundary for recurrences.
   * @throws IllegalArgumentException if validation fails.
   */
  public static void validateRecurringEvent(LocalDateTime start, LocalDateTime end, String weekdays, int repeatCount, LocalDateTime repeatUntil) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Recurring event must have start and end times.");
    }
    // Ensure the event spans a single day.
    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException("Recurring event must span a single day (within 24 hours).");
    }
    if (!start.isBefore(end)) {
      throw new IllegalArgumentException("Event start time must be before end time.");
    }
    if (repeatCount <= 0) {
      throw new IllegalArgumentException("Repeat count must be a positive number.");
    }
    if (repeatUntil != null && repeatUntil.isBefore(start)) {
      throw new IllegalArgumentException("Repeat until date must be after the event start time.");
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new IllegalArgumentException("At least one recurring day must be specified.");
    }
  }
}

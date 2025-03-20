package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring calendar event with specific recurring rules.
 */
public class RecurringEvent extends AbstractCalendarEvent {

  private final String weekdays;
  private final int repeatCount;
  private final LocalDateTime repeatUntil;

  /**
   * Constructs a recurring event.
   *
   * @param subject       the event subject
   * @param startDateTime event start time
   * @param endDateTime   event end time
   * @param weekdays      days of the week on which the event repeats
   * @param repeatCount   number of repetitions
   * @param repeatUntil   repeat-until date
   * @param description   event description
   * @param location      event location
   * @param isPublic      visibility flag
   * @param isAllDay      flag for all-day event
   */
  public RecurringEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                        String weekdays, int repeatCount, LocalDateTime repeatUntil,
                        String description, String location, boolean isPublic, boolean isAllDay) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.weekdays = weekdays;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;
  }

  public String getWeekdays() {
    return weekdays;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates a list of SingleEvent occurrences from the recurring rule.
   *
   * @param seriesId Unique identifier for the series.
   * @return List of SingleEvent occurrences.
   */
  public List<SingleEvent> generateOccurrences(String seriesId) {
    List<SingleEvent> occurrences = new ArrayList<>();
    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }

    LocalDateTime currentStart = startDateTime;
    LocalDateTime currentEnd = endDateTime;
    int created = 0;

    while (true) {
      if (weekdays.indexOf(getDayChar(currentStart.getDayOfWeek())) >= 0) {
        occurrences.add(new SingleEvent(subject, currentStart, currentEnd, description,
                location, isPublic, isAllDay, seriesId));
        created++;

        if (repeatCount > 0 && created >= repeatCount) {
          break;
        }
      }

      currentStart = currentStart.plusDays(1);
      currentEnd = currentEnd.plusDays(1);

      if (repeatUntil != null && currentStart.isAfter(repeatUntil)) {
        break;
      }
    }
    return occurrences;
  }

  private char getDayChar(DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
      case MONDAY:
        return 'M';
      case TUESDAY:
        return 'T';
      case WEDNESDAY:
        return 'W';
      case THURSDAY:
        return 'R';
      case FRIDAY:
        return 'F';
      case SATURDAY:
        return 'S';
      case SUNDAY:
        return 'U';
      default:
        throw new IllegalArgumentException("Unknown day: " + dayOfWeek);
    }
  }

  /**
   * Returns a new RecurringEvent updated with the specified property.
   *
   * @param property The property to update.
   * @param newValue The new value for the property.
   * @return A new RecurringEvent instance with the updated property.
   */
  public RecurringEvent withUpdatedProperty(String property, String newValue) {
    String updatedWeekdays = weekdays;
    int updatedRepeatCount = repeatCount;
    LocalDateTime updatedRepeatUntil = repeatUntil;
    String updatedDescription = description;
    String updatedLocation = location;

    switch (property.toLowerCase().trim()) {
      case "repeattimes":
        updatedRepeatCount = Integer.parseInt(newValue);
        if (updatedRepeatCount <= 0) {
          throw new IllegalArgumentException("Repeat count must be a positive number.");
        }
        break;
      case "repeatuntil":
        updatedRepeatUntil = LocalDateTime.parse(newValue);
        break;
      case "repeatingdays":
        updatedWeekdays = newValue.toUpperCase();
        break;
      case "description":
        updatedDescription = newValue;
        break;
      case "location":
        updatedLocation = newValue;
        break;
      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }

    return new RecurringEvent(subject, startDateTime, endDateTime,
            updatedWeekdays, updatedRepeatCount, updatedRepeatUntil,
            updatedDescription, updatedLocation, isPublic, isAllDay);
  }
}
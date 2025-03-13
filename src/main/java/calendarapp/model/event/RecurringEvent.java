package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a recurring calendar event.
 * In addition to standard event properties, it includes recurring-specific properties such as
 * the days of the week on which the event repeats, the number of repetitions,
 * and an optional end date. It can generate individual occurrences of the event as single events.
 */
public class RecurringEvent extends AbstractCalendarEvent {

  private final String weekdays;
  private final int repeatCount;
  private final LocalDateTime repeatUntil;

  /**
   * Constructs a recurring event with the specified details.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param weekdays      a string representing the days of the week on which the event repeats
   * @param repeatCount   the number of times the event should repeat
   * @param repeatUntil   the date and time until which the event should repeat; null if not used
   * @param description   the event description
   * @param location      the event location
   * @param isPublic      true if the event is public; false otherwise
   * @param isAllDay      true if the event lasts all day; false otherwise
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

  /**
   * Returns the string representing the days of the week on which the event repeats.
   *
   * @return the weekdays string
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times the event should repeat.
   *
   * @return the repeat count
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Returns the date and time until which the event should repeat.
   *
   * @return the repeat-until date and time, or null if not set
   */
  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates occurrences of the recurring event as a list of single events.
   * Each occurrence is created for days that match the specified weekdays.
   * Occurrences will be generated until the repeat count is reached or until the repeatUntil date.
   *
   * @param seriesId a unique identifier to associate the occurrences with the recurring series
   * @return a list of single events representing the occurrences
   */
  public List<SingleEvent> generateOccurrences(String seriesId) {
    List<SingleEvent> occurrences = new ArrayList<>();
    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }

    LocalDateTime currentStart = startDateTime;
    LocalDateTime currentEnd = endDateTime;
    int occurrencesCreated = 0;

    while (true) {
      if (weekdays.indexOf(getDayChar(currentStart.getDayOfWeek())) >= 0) {
        occurrences.add(new SingleEvent(subject, currentStart, currentEnd, description,
                location, isPublic, isAllDay, seriesId));
        occurrencesCreated++;

        if (repeatCount > 0 && occurrencesCreated >= repeatCount) {
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

  /**
   * Returns a character representing the day of the week.
   * The mapping is: Monday = M, Tuesday = T, Wednesday = W, Thursday = R,
   * Friday = F, Saturday = S, Sunday = U.
   *
   * @param dayOfWeek the day of the week
   * @return the character representing the day
   * @throws IllegalArgumentException if the day is unknown
   */
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
}

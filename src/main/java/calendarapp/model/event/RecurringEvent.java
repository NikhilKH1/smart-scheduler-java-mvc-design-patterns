package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring calendar event with specific recurring rules.
 * This class defines the behavior for events that repeat based on a set of rules,
 * such as repeating days of the week, repeat count, and repeat until date.
 */
public class RecurringEvent extends AbstractCalendarEvent implements ReadOnlyCalendarEvent {

  private final String weekdays;
  private final int repeatCount;
  private final ZonedDateTime repeatUntil;

  /**
   * Returns a new RecurringEvent with the same properties but updated to the specified timezone.
   * This method converts both the start and end times of the event to the new timezone, and also
   * converts the repeat-until date (if present) to the new timezone.
   *
   * @param newZone the new timezone to apply to the event
   * @return a new RecurringEvent with the updated timezone
   */
  public RecurringEvent withUpdatedTimezone(ZoneId newZone) {
    return new RecurringEvent(this.subject,
            this.startDateTime.withZoneSameInstant(newZone),
            this.endDateTime.withZoneSameInstant(newZone), this.weekdays,
            this.repeatCount,
            (this.repeatUntil != null) ? this.repeatUntil.withZoneSameInstant(newZone) : null,
            this.description, this.location, this.isPublic, this.isAllDay);
  }

  /**
   * Constructs a recurring event with the specified parameters.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param weekdays      the days of the week on which the event occurs (e.g., "MWF")
   * @param repeatCount   the number of times the event repeats
   * @param repeatUntil   the date until which the event repeats (can be null)
   * @param description   the description of the event
   * @param location      the location of the event
   * @param isPublic      whether the event is public
   * @param isAllDay      whether the event is an all-day event
   */
  public RecurringEvent(String subject, ZonedDateTime startDateTime, ZonedDateTime endDateTime,
                        String weekdays, int repeatCount, ZonedDateTime repeatUntil,
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

  public Integer getRepeatCount() {
    return repeatCount;
  }

  @Override
  public boolean isRecurring() {
    return false;
  }

  public ZonedDateTime RepeatUntil() {
    return repeatUntil;
  }

  public List<SingleEvent> generateOccurrences(String seriesId) {
    List<SingleEvent> occurrences = new ArrayList<>();

    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }

    if (repeatCount <= 0 && repeatUntil == null) {
      return occurrences;
    }

    ZonedDateTime currentStart = this.startDateTime;
    ZonedDateTime currentEnd = this.endDateTime;

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

  public RecurringEvent withUpdatedProperty(String property, String newValue) {
    String updatedWeekdays = this.weekdays;
    int updatedRepeatCount = this.repeatCount;
    ZonedDateTime updatedRepeatUntil = this.repeatUntil;
    String updatedDescription = this.description;
    String updatedLocation = this.location;
    String updatedSubject = this.subject;
    ZonedDateTime updatedStart = this.startDateTime;
    ZonedDateTime updatedEnd = this.endDateTime;
    boolean updatedIsPublic = this.isPublic;
    boolean updatedIsAllDay = this.isAllDay;

    switch (property.toLowerCase().trim()) {
      case "repeattimes":
        try {
          updatedRepeatCount = Integer.parseInt(newValue);
          if (updatedRepeatCount <= 0) {
            throw new IllegalArgumentException("Repeat count must be positive.");
          }
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid number for repeattimes: " + newValue);
        }
        break;

      case "repeatuntil":
        updatedRepeatUntil = ZonedDateTime.parse(newValue);
        break;

      case "repeatingdays":
        updatedWeekdays = newValue.trim().toUpperCase();
        break;

      case "description":
        updatedDescription = newValue;
        break;

      case "location":
        updatedLocation = newValue;
        break;

      case "subject":
        updatedSubject = newValue;
        break;

      case "startdatetime":
        updatedStart = ZonedDateTime.parse(newValue);
        break;

      case "enddatetime":
        updatedEnd = ZonedDateTime.parse(newValue);
        break;

      case "public":
        updatedIsPublic = true;
        break;

      case "private":
        updatedIsPublic = false;
        break;

      default:
        throw new IllegalArgumentException("Unknown recurring event property: " + property);
    }

    return new RecurringEvent(updatedSubject, updatedStart, updatedEnd, updatedWeekdays,
            updatedRepeatCount, updatedRepeatUntil, updatedDescription,
            updatedLocation, updatedIsPublic, updatedIsAllDay);
  }
}

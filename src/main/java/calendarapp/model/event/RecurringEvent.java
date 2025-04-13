package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a recurring calendar event with specific recurring rules.
 * This class defines the behavior for events that repeat based on a set of rules,
 * such as repeating days of the week, repeat count, and repeat until date.
 */
public class RecurringEvent extends AbstractCalendarEvent {

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

  /**
   * Returns the weekdays on which the event repeats.
   *
   * @return the weekdays on which the event repeats
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times the event repeats.
   *
   * @return the repeat count
   */
  public Integer getRepeatCount() {
    return repeatCount;
  }

  /**
   * Indicates whether the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  @Override
  public boolean isRecurring() {
    return true;
  }

  /**
   * Returns the date until which the event repeats.
   *
   * @return the repeat-until date, or null if the event repeats indefinitely
   */
  public ZonedDateTime repeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates the occurrences of the recurring event based on the repeating rules.
   *
   * @param seriesId the unique ID for the series of events
   * @return a list of SingleEvent occurrences for the recurring event
   */
  public List<SingleEvent> generateOccurrences(String seriesId) {
    List<SingleEvent> occurrences = new ArrayList<>();

    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }
    if (repeatCount <= 0 && repeatUntil == null) {
      return occurrences;
    }

    if (seriesId == null || seriesId.isEmpty()) {
      seriesId = UUID.randomUUID().toString();
    }

    ZonedDateTime currentStart = this.startDateTime;
    ZonedDateTime currentEnd = this.endDateTime;
    int created = 0;

    while (true) {
      if (repeatUntil != null && currentStart.isAfter(repeatUntil)) {
        break;
      }
      if (weekdays.indexOf(getDayChar(currentStart.getDayOfWeek())) >= 0) {
        occurrences.add(new SingleEvent(subject, currentStart, currentEnd,
                description, location, isPublic, isAllDay, seriesId));
        created++;
        if (repeatCount > 0 && created >= repeatCount) {
          break;
        }
      }
      currentStart = currentStart.plusDays(1);
      currentEnd = currentEnd.plusDays(1);
    }
    return occurrences;
  }

  /**
   * Returns the character representing the given day of the week (e.g., "M" for Monday).
   *
   * @param dayOfWeek the DayOfWeek to convert
   * @return the character representing the given day of the week
   * @throws IllegalArgumentException if the DayOfWeek is unknown
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

  /**
   * Creates a new instance of the recurring event with an updated property.
   * This method allows modifying specific properties of the event, such as
   * the repeat count, repeat until date, weekdays, description, location, etc.
   *
   * @param property the property to be updated
   * @param newValue the new value for the specified property
   * @return a new RecurringEvent with the updated property
   * @throws IllegalArgumentException if the property is unknown or the new value is invalid
   */
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

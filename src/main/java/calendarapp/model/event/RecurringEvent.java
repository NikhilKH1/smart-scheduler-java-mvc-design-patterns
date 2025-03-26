package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring calendar event with specific recurring rules.
 * This class defines the behavior for events that repeat based on a set of rules,
 * such as repeating days of the week, repeat count, and repeat until date.
 */
public class RecurringEvent extends AbstractCalendarEvent {

  private final String weekdays;
  private final int repeatCount;
  private final Temporal repeatUntil;

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
            ZonedDateTime.from(this.startDateTime).withZoneSameInstant(newZone),
            ZonedDateTime.from(this.endDateTime).withZoneSameInstant(newZone), this.weekdays,
            this.repeatCount,
            (this.repeatUntil != null) ? ZonedDateTime.from(this.repeatUntil).
                    withZoneSameInstant(newZone) : null, this.description, this.location,
            this.isPublic, this.isAllDay);
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
  public RecurringEvent(String subject, Temporal startDateTime, Temporal endDateTime,
                        String weekdays, int repeatCount, Temporal repeatUntil,
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
   * Returns the weekdays on which the event occurs.
   *
   * @return a string representing the weekdays (e.g., "MWF" for Monday, Wednesday, Friday)
   */
  public String getWeekdays() {
    return weekdays;
  }

  /**
   * Returns the number of times the event repeats.
   *
   * @return the repeat count
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * Returns the date until which the event repeats.
   *
   * @return the repeat until date, or null if there is no end date for the repetition
   */
  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates a list of occurrences for this recurring event,
   * based on the specified recurrence rules.
   *
   * @param seriesId a unique identifier for the series of events
   * @return a list of SingleEvent occurrences for the recurring event
   */
  public List<SingleEvent> generateOccurrences(String seriesId) {
    List<SingleEvent> occurrences = new ArrayList<>();
    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }

    ZonedDateTime currentStart = ZonedDateTime.from(startDateTime);
    ZonedDateTime currentEnd = ZonedDateTime.from(endDateTime);
    ZonedDateTime repeatUntilZoned = (repeatUntil != null) ? ZonedDateTime.from(repeatUntil) : null;

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

      if (repeatUntilZoned != null && currentStart.isAfter(repeatUntilZoned)) {
        break;
      }
    }
    return occurrences;
  }

  /**
   * Converts a DayOfWeek to its corresponding character representation (e.g., "M" for Monday).
   *
   * @param dayOfWeek the day of the week to convert
   * @return the character representing the day of the week
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
   * Returns a new RecurringEvent updated with the specified property.
   * The event properties include fields like repeat count, repeat until, weekdays, etc.
   *
   * @param property the property to update
   * @param newValue the new value for the property
   * @return a new RecurringEvent with the updated property
   */
  public RecurringEvent withUpdatedProperty(String property, String newValue) {
    String updatedWeekdays = this.weekdays;
    int updatedRepeatCount = this.repeatCount;
    Temporal updatedRepeatUntil = this.repeatUntil;
    String updatedDescription = this.description;
    String updatedLocation = this.location;
    String updatedSubject = this.subject;
    Temporal updatedStart = this.startDateTime;
    Temporal updatedEnd = this.endDateTime;
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

package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring calendar event with specific recurring rules.
 */
public class RecurringEvent extends AbstractCalendarEvent {

  private final String weekdays;
  private final int repeatCount;
  private final Temporal repeatUntil;

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
   * Constructs a recurring event.
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

  public String getWeekdays() {
    return weekdays;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public Temporal getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Generates a list of SingleEvent occurrences from the recurring rule.
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

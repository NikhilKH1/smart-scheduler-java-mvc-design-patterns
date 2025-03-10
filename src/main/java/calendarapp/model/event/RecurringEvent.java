package calendarapp.model.event;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecurringEvent implements CalendarEvent {

  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String weekdays;
  private final int repeatCount;
  private final LocalDateTime repeatUntil;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final boolean isAllDay;

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

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return endDateTime;
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

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isAllDay() {
    return isAllDay;
  }

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
        occurrences.add(new SingleEvent(subject, currentStart, currentEnd, description, location, isPublic, isAllDay, seriesId));
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

  private char getDayChar(DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
      case MONDAY: return 'M';
      case TUESDAY: return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY: return 'R';
      case FRIDAY: return 'F';
      case SATURDAY: return 'S';
      case SUNDAY: return 'U';
      default: throw new IllegalArgumentException("Unknown day: " + dayOfWeek);
    }
  }
}

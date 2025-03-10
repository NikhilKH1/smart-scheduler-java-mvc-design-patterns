package calendarapp.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecurrenceUtil {
  /**
   * Generates occurrences for a recurring event.
   * Each occurrence is represented as a two-element array containing the start and end LocalDateTime.
   *
   * @param start The initial start date/time.
   * @param end The initial end date/time.
   * @param weekdays A String representing the days on which the event recurs (e.g. "MTW").
   * @param repeatCount Number of occurrences to generate (if > 0).
   * @param repeatUntil The last allowed date/time for an occurrence (if not null).
   * @return A list of LocalDateTime[] arrays, where index 0 is start and index 1 is end.
   */
  public static List<LocalDateTime[]> generateOccurrences(LocalDateTime start, LocalDateTime end, String weekdays, int repeatCount, LocalDateTime repeatUntil) {
    List<LocalDateTime[]> occurrences = new ArrayList<>();
    if (weekdays == null || weekdays.isEmpty()) {
      return occurrences;
    }
    LocalDateTime currentStart = start;
    LocalDateTime currentEnd = end;
    int count = 0;
    while (true) {
      // Check if the current day-of-week is among the specified weekdays.
      char dayChar = getDayChar(currentStart.getDayOfWeek());
      if (weekdays.indexOf(dayChar) >= 0) {
        occurrences.add(new LocalDateTime[]{ currentStart, currentEnd });
        count++;
        if (repeatCount > 0 && count >= repeatCount) {
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

  private static char getDayChar(DayOfWeek day) {
    switch (day) {
      case MONDAY:    return 'M';
      case TUESDAY:   return 'T';
      case WEDNESDAY: return 'W';
      case THURSDAY:  return 'R';
      case FRIDAY:    return 'F';
      case SATURDAY:  return 'S';
      case SUNDAY:    return 'U';
      default: throw new IllegalArgumentException("Unknown day: " + day);
    }
  }
}

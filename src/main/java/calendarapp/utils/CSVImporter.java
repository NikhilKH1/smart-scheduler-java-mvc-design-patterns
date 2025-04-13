package calendarapp.utils;

import calendarapp.model.ICalendarModel;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.model.event.ICalendarEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * A CSV-based implementation of the IImporter interface.
 * This class reads calendar events from a CSV file and imports them into the given calendar model.
 * Supports both single and recurring events based on CSV structure.
 */
public class CSVImporter implements IImporter {

  /**
   * An array of DateTimeFormatter objects used for parsing date strings in various formats.
   * These formats are used when attempting to parse a date string from the CSV file.
   */
  private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
          DateTimeFormatter.ofPattern("MM/dd/yyyy"),
          DateTimeFormatter.ISO_LOCAL_DATE
  };

  /**
   * Imports events from a CSV file into the specified calendar model.
   * The CSV must have the following structure in order:
   * name, startDate, startTime, endDate, endTime, calendarName (optional), description, location,
   * , weekdays (optional), repeatUntil (optional), repeatCount (optional)
   *
   * @param model the calendar model to import the events into
   * @param filePath the path to the CSV file
   * @throws IOException if an error occurs while reading the file
   */
  @Override
  public void importInto(ICalendarModel model, String filePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String header = reader.readLine();
      String line;
      DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) continue;

        String name = parts[0].replace("\"", "").trim();
        LocalDate startDate = tryParseDate(parts[1].trim());
        LocalTime startTime = LocalTime.parse(parts[2].trim(), timeFormat);
        LocalDate endDate = tryParseDate(parts[3].trim());
        LocalTime endTime = LocalTime.parse(parts[4].trim(), timeFormat);
        String desc = parts[6].trim();
        String loc = parts[7].trim();

        ZonedDateTime start = startDate.atTime(startTime).atZone(model.getTimezone());
        ZonedDateTime end = endDate.atTime(endTime).atZone(model.getTimezone());

        boolean isRecurring = false;
        String weekdays = "";
        String repeatUntilStr = "";
        String repeatCountStr = "";

        if (parts.length >= 10) {
          weekdays = parts[9].trim().toUpperCase();
          isRecurring = !weekdays.isEmpty();
          if (parts.length >= 11) {
            repeatUntilStr = parts[10].trim();
          }
          if (parts.length >= 12) {
            repeatCountStr = parts[11].trim();
          }
        }

        ICalendarEvent event;

        if (isRecurring) {
          int repeatCount = 0;
          ZonedDateTime repeatUntil = null;

          if (!repeatCountStr.isEmpty()) {
            repeatCount = Integer.parseInt(repeatCountStr);
          }
          if (!repeatUntilStr.isEmpty()) {
            repeatUntil = tryParseDate(repeatUntilStr).atStartOfDay(model.getTimezone());
          }

          event = new RecurringEvent(name, start, end, weekdays, repeatCount, repeatUntil,
                  desc, loc, true, start.toLocalTime().equals(LocalTime.MIN)
                  && end.toLocalTime().equals(LocalTime.of(23, 59, 59)));

        } else {
          event = new SingleEvent(name, start, end, desc, loc, true,
                  start.toLocalTime().equals(LocalTime.MIN)
                          && end.toLocalTime().equals(LocalTime.of(23, 59, 59)),
                  null);
        }

        model.addEvent(event, true);
      }
    }
  }

  /**
   * Attempts to parse a date string using supported date formats.
   * Supported formats include MM/dd/yyyy and ISO_LOCAL_DATE (yyyy-MM-dd).
   *
   * @param input the date string to parse
   * @return the parsed LocalDate
   * @throws IllegalArgumentException if the date format is invalid
   */
  private LocalDate tryParseDate(String input) {
    for (DateTimeFormatter fmt : DATE_FORMATS) {
      try {
        return LocalDate.parse(input, fmt);
      } catch (Exception ignored) {}
    }
    throw new IllegalArgumentException("Invalid date format: " + input);
  }

  /**
   * Parses a string of characters representing weekdays into a set of DayOfWeek values.
   * For example, "MTWRF" will return Monday through Friday.
   * M = Monday, T = Tuesday, W = Wednesday, R = Thursday, F = Friday, S = Saturday, U = Sunday.
   *
   * @param input the weekday characters string
   * @return a set of DayOfWeek values representing the weekdays
   */
  private Set<DayOfWeek> parseWeekdays(String input) {
    Set<DayOfWeek> result = new HashSet<>();
    for (char c : input.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M': result.add(DayOfWeek.MONDAY); break;
        case 'T': result.add(DayOfWeek.TUESDAY); break;
        case 'W': result.add(DayOfWeek.WEDNESDAY); break;
        case 'R': result.add(DayOfWeek.THURSDAY); break;
        case 'F': result.add(DayOfWeek.FRIDAY); break;
        case 'S': result.add(DayOfWeek.SATURDAY); break;
        case 'U': result.add(DayOfWeek.SUNDAY); break;
      }
    }
    return result;
  }
}
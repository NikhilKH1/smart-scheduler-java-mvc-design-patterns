package calendarapp.utils;

import calendarapp.model.event.CalendarEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class that exports calendar events to a CSV file format that can be
 * imported by Google Calendar.
 */
public class CSVExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports the provided calendar events to a CSV file.
   * The CSV file will contain columns for subject, start date, start time, end date, end time,
   * all day flag, description, location, and a flag indicating if the event is private.
   *
   * @param events   the list of calendar events to export
   * @param filePath the desired file path for the CSV file (can be relative or absolute)
   * @return the absolute file path of the generated CSV file
   * @throws IOException if an I/O error occurs during file writing
   */
  public static String exportToCSV(List<CalendarEvent> events, String filePath)
          throws IOException {
    File file = new File(filePath);
    try (FileWriter writer = new FileWriter(file)) {

      writer.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
              + "Description,Location,Private\n");

      for (CalendarEvent event : events) {
        String subject = event.getSubject();

        boolean isAllDay = event.isAllDay();
        boolean isPrivate = !event.isPublic();

        String startDate = "";
        String startTime = "";
        String endDate = "";
        String endTime = "";
        String allDayVal = isAllDay ? "TRUE" : "FALSE";

        if (event.getStartDateTime() != null) {
          startDate = event.getStartDateTime().format(DATE_FORMAT);
          startTime = isAllDay ? "" : event.getStartDateTime().format(TIME_FORMAT);
        }

        if (event.getEndDateTime() != null) {
          endDate = event.getEndDateTime().format(DATE_FORMAT);
          endTime = isAllDay ? "" : event.getEndDateTime().format(TIME_FORMAT);
        } else {
          allDayVal = "TRUE";
          endDate = startDate;
          endTime = "";
        }

        String privateVal = isPrivate ? "TRUE" : "FALSE";
        String description = event.getDescription() != null ? event.getDescription() : "";
        String location = event.getLocation() != null ? event.getLocation() : "";

        writer.append(escapeCSV(subject)).append(",")
                .append(escapeCSV(startDate)).append(",")
                .append(escapeCSV(startTime)).append(",")
                .append(escapeCSV(endDate)).append(",")
                .append(escapeCSV(endTime)).append(",")
                .append(escapeCSV(allDayVal)).append(",")
                .append(escapeCSV(description)).append(",")
                .append(escapeCSV(location)).append(",")
                .append(escapeCSV(privateVal)).append("\n");
      }
    }
    return file.getAbsolutePath();
  }

  /**
   * Escapes CSV values containing commas, quotes, or newlines.
   * If the value contains any of these characters, it will be enclosed in quotes
   * and any quotes in the value will be doubled.
   *
   * @param value the CSV value to escape
   * @return the escaped CSV value
   */
  private static String escapeCSV(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }
}

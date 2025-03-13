package calendarapp.utils;

import calendarapp.model.event.CalendarEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class to export calendar events to a CSV file format.
 */
public class CSVExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports the provided calendar events to a CSV file.
   * The CSV file will contain:
   * - Subject, Start Date, Start Time, End Date, End Time, All Day Event, Description, Location, Private
   *
   * @param events   the list of calendar events to export
   * @param filePath the desired file path for the CSV file
   * @return the absolute file path of the generated CSV file
   * @throws IOException if an error occurs during file writing
   */
  public static String exportToCSV(List<CalendarEvent> events, String filePath) throws IOException {
    if (events == null || events.isEmpty()) {
      throw new IOException("No events available for export.");
    }
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path must not be null or empty.");
    }

    File file = new File(filePath);
    File parentDir = file.getParentFile();
    if (parentDir != null && !parentDir.exists()) {
      if (!parentDir.mkdirs()) {
        throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
      }
    }

    try (FileWriter writer = new FileWriter(file)) {
      writer.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
              + "Description,Location,Private\n");

      for (CalendarEvent event : events) {
        writer.append(formatEvent(event)).append("\n");
      }
    }
    return file.getAbsolutePath();
  }

  /**
   * Formats a single event into a CSV row.
   *
   * @param event the event to format
   * @return the formatted CSV string
   */
  private static String formatEvent(CalendarEvent event) {
    String subject = event.getSubject();
    boolean isPrivate = !event.isPublic();

    String startDate = event.getStartDateTime().format(DATE_FORMAT);
    String startTime = event.isAllDay() ? "" : event.getStartDateTime().format(TIME_FORMAT);

    boolean isAllDay = event.isAllDay() || event.getEndDateTime() == null;

    String endDate = (event.getEndDateTime() != null) ? event.getEndDateTime().format(DATE_FORMAT)
            : startDate;
    String endTime = isAllDay ? "" : (event.getEndDateTime() != null
            ? event.getEndDateTime().format(TIME_FORMAT) : "");

    String allDayVal = isAllDay ? "TRUE" : "FALSE";
    String privateVal = isPrivate ? "TRUE" : "FALSE";
    String description = event.getDescription() != null ? event.getDescription() : "";
    String location = event.getLocation() != null ? event.getLocation() : "";

    return escapeCSV(subject) + "," +
            escapeCSV(startDate) + "," +
            escapeCSV(startTime) + "," +
            escapeCSV(endDate) + "," +
            escapeCSV(endTime) + "," +
            escapeCSV(allDayVal) + "," +
            escapeCSV(description) + "," +
            escapeCSV(location) + "," +
            escapeCSV(privateVal);
  }

  /**
   * Escapes CSV values containing commas, quotes, or newlines.
   *
   * @param value the CSV value to escape
   * @return the escaped CSV value
   */
  private static String escapeCSV(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }
}

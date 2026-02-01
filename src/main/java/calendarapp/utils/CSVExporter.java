package calendarapp.utils;

import calendarapp.model.event.ReadOnlyCalendarEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The CSVExporter class implements the IExporter interface to export calendar events to a CSV file.
 * The exported CSV file contains the event's subject, start and end date/times,
 * description, location, privacy status, and whether the event is all-day or not.
 */
public class CSVExporter implements IExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports the list of calendar events to a CSV file.
   *
   * @param events   the list of calendar events to be exported
   * @param filePath the path of the CSV file to be created
   * @return the absolute path of the created CSV file
   * @throws IOException              if an error occurs while writing to the file
   * @throws IllegalArgumentException if the file path is invalid or the file extension is not .csv
   */
  @Override
  public String export(List<ReadOnlyCalendarEvent> events, String filePath) throws IOException {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path must not be null or empty.");
    }
    if (!filePath.toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("Invalid file extension. File must end with .csv");
    }

    if (events == null || events.isEmpty()) {
      throw new IOException("No events available for export.");
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

      for (ReadOnlyCalendarEvent event : events) {
        writer.append(formatEvent(event)).append("\n");
      }
    }
    return file.getAbsolutePath();
  }

  /**
   * Formats a single calendar event as a CSV-compatible string.
   *
   * @param event the calendar event to be formatted
   * @return the event as a CSV string
   */
  private String formatEvent(ReadOnlyCalendarEvent event) {
    String subject = event.getSubject();
    boolean isPrivate = !event.isPublic();

    ZonedDateTime startDateTime = (ZonedDateTime) event.getStartDateTime();
    ZonedDateTime endDateTime = (ZonedDateTime) event.getEndDateTime();

    String startDate = startDateTime.format(DATE_FORMAT);
    String startTime = event.isAllDay() ? "" : startDateTime.format(TIME_FORMAT);

    boolean isAllDay = event.isAllDay() || endDateTime == null;

    String endDate = (endDateTime != null) ? endDateTime.format(DATE_FORMAT) : startDate;
    String endTime = isAllDay ? "" : (endDateTime != null ? endDateTime.format(TIME_FORMAT) : "");

    String allDayVal = isAllDay ? "TRUE" : "FALSE";
    String privateVal = isPrivate ? "TRUE" : "FALSE";
    String description = event.getDescription() != null ? event.getDescription() : "";
    String location = event.getLocation() != null ? event.getLocation() : "";

    return escapeCSV(subject) + "," + escapeCSV(startDate) + "," + escapeCSV(startTime) + "," +
            escapeCSV(endDate) + "," + escapeCSV(endTime) + "," + escapeCSV(allDayVal) + "," +
            escapeCSV(description) + "," + escapeCSV(location) + "," + escapeCSV(privateVal);
  }

  /**
   * Escapes a string for CSV formatting by adding quotes around it and escaping internal quotes.
   *
   * @param value the string to be escaped
   * @return the escaped string
   */
  private String escapeCSV(String value) {
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

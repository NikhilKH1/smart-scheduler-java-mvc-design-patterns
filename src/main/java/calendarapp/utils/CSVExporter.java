package calendarapp.utils;

import calendarapp.model.event.ICalendarEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter implements IExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  @Override
  public String export(List<ICalendarEvent> events, String filePath) throws IOException {
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

      for (ICalendarEvent event : events) {
        writer.append(formatEvent(event)).append("\n");
      }
    }
    return file.getAbsolutePath();
  }

  private String formatEvent(ICalendarEvent event) {
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

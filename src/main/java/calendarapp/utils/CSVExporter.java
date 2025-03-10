package calendarapp.utils;

import calendarapp.model.event.CalendarEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

  // Google Calendar typically expects dates in MM/dd/yyyy format and times in HH:mm (24-hour) or h:mm a (12-hour).
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports the provided calendar events to a CSV file in a format that Google Calendar can import.
   *
   * @param events   the list of calendar events to export
   * @param filePath the desired file path for the CSV file (can be relative or absolute)
   * @return the absolute file path of the generated CSV file
   * @throws IOException if an I/O error occurs during file writing
   */
  public static String exportToCSV(List<CalendarEvent> events, String filePath) throws IOException {
    File file = new File(filePath);
    try (FileWriter writer = new FileWriter(file)) {

      // Google Calendar expects these columns in this order:
      writer.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");

      for (CalendarEvent event : events) {
        // 1. Subject
        String subject = event.getSubject();

        // 2. & 3. Start Date & Time
        // 4. & 5. End Date & Time
        // 6. All Day Event
        //    - If isAllDay() is true, or there's no end date/time, we set times to empty and All Day to TRUE.
        // 7. Description
        // 8. Location
        // 9. Private (TRUE if event is private, FALSE otherwise)

        boolean isAllDay = event.isAllDay();
        boolean isPrivate = !event.isPublic(); // If not public, it's private.

        // Handle potential null endDateTime or all-day logic
        String startDate = "";
        String startTime = "";
        String endDate   = "";
        String endTime   = "";
        String allDayVal = isAllDay ? "TRUE" : "FALSE";

        // Format start date/time
        if (event.getStartDateTime() != null) {
          startDate = event.getStartDateTime().format(DATE_FORMAT);
          startTime = isAllDay ? "" : event.getStartDateTime().format(TIME_FORMAT);
        }

        // Format end date/time
        if (event.getEndDateTime() != null) {
          endDate = event.getEndDateTime().format(DATE_FORMAT);
          endTime = isAllDay ? "" : event.getEndDateTime().format(TIME_FORMAT);
        } else {
          // If no end time is provided, treat as all-day
          allDayVal = "TRUE";
          endDate   = startDate;
          endTime   = "";
        }

        // Private column
        String privateVal = isPrivate ? "TRUE" : "FALSE";

        // Description and Location may be null
        String description = event.getDescription() != null ? event.getDescription() : "";
        String location = event.getLocation() != null ? event.getLocation() : "";

        // Write out the row, escaping CSV as needed
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

package calendarapp.utils;

import calendarapp.model.event.CalendarEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {
  public static void exportToCSV(List<CalendarEvent> events, String filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.append("Subject,StartDateTime,EndDateTime,Description,Location\n");
      for (CalendarEvent event : events) {
        writer.append(escapeCSV(event.getSubject())).append(",");
        writer.append(escapeCSV(event.getStartDateTime().toString())).append(",");
        writer.append(escapeCSV(event.getEndDateTime().toString())).append(",");
        // For simplicity, we assume that event.toString() includes description and location,
        // or you can add additional getters to CalendarEvent.
        writer.append(escapeCSV(event.toString())).append("\n");
      }
    }
  }

  private static String escapeCSV(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }
}

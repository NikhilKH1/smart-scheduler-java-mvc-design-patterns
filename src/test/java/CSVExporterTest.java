

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.utils.CSVExporter;

import org.junit.Test;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class CSVExporterTest {

  @Test
  public void testExportEmptyEventList() {
    List<CalendarEvent> events = new ArrayList<>();
    String fileName = "test_empty.csv";

    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue(file.exists());
      assertTrue(file.length() > 0); // Should contain headers
    } catch (Exception e) {
      fail("Exporting an empty event list should not fail.");
    }
  }

  @Test
  public void testExportSingleEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Meeting", start, end, "Discussion", "Office", true, false, null));

    String fileName = "test_single_event.csv";

    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue(file.exists());
      assertTrue(file.length() > 0);
    } catch (Exception e) {
      fail("Exporting a single event should not fail.");
    }
  }

  @Test
  public void testExportMultipleEvents() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 2, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 2, 12, 0);

    events.add(new SingleEvent("Meeting", start1, end1, "Discussion", "Office", true, false, null));
    events.add(new SingleEvent("Call", start2, end2, "Client Call", "Remote", false, false, null));

    String fileName = "test_multiple_events.csv";

    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue(file.exists());
      assertTrue(file.length() > 0);
    } catch (Exception e) {
      fail("Exporting multiple events should not fail.");
    }
  }
}

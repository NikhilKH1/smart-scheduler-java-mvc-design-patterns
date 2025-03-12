import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.utils.CSVExporter;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.format.DateTimeFormatter;

/**
 * JUnit tests for the CSVExporter class.
 */
public class CSVExporterTest {

  @Test
  public void testExportEmptyEventList() {
    List<CalendarEvent> events = new ArrayList<>();
    String fileName = "test_empty.csv";

    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      assertTrue("Exported file should contain headers", file.length() > 0);
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
      assertTrue("Exported file should exist", file.exists());
      assertTrue("Exported file should not be empty", file.length() > 0);
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
      assertTrue("Exported file should exist", file.exists());
      assertTrue("Exported file should contain content", file.length() > 0);
    } catch (Exception e) {
      fail("Exporting multiple events should not fail.");
    }
  }

  @Test
  public void testExportEventWithSpecialCharacters() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    String subject = "Meeting, \"Special\" \nSession";
    String description = "Discuss, review \"Q1 Report\"";
    String location = "Office, Room \"101\"";
    events.add(new SingleEvent(subject, start, end, description, location, true, false, null));

    String fileName = "test_special_chars.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      assertTrue("Subject should be escaped",
              content.contains("\"Meeting, \"\"Special\"\" \nSession\""));
      assertTrue("Description should be escaped",
              content.contains("\"Discuss, review \"\"Q1 Report\"\"\""));
      assertTrue("Location should be escaped",
              content.contains("\"Office, Room \"\"101\"\"\""));
    } catch (Exception e) {
      fail("Exporting event with special characters should not fail: " + e.getMessage());
    }
  }

  @Test
  public void testExportEventWithNullFields() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Test Event", start, end, null, null, true, false, null));
    String fileName = "test_null_fields.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      assertTrue("Output should show empty fields for null values", content.contains("Test Event,"));
    } catch (Exception e) {
      fail("Exporting event with null fields should not fail: " + e.getMessage());
    }
  }

  @Test
  public void testExportEventWithNullEndDateTime() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    events.add(new SingleEvent("No End Event", start, null, "No End", "Office", true, false, null));
    String fileName = "test_null_end.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));

      String expectedStartDate = start.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
      String[] lines = content.split("\n");
      String[] fields = lines[1].split(",");
      assertEquals("Start Date should match", expectedStartDate, fields[1]);
      assertEquals("Start Time should be formatted (non-empty) when not an all-day event", "09:00", fields[2]);
      assertEquals("End Date should match Start Date", expectedStartDate, fields[3]);
      assertEquals("End Time should be empty", "", fields[4]);
      assertEquals("All Day flag should be TRUE", "TRUE", fields[5]);
    } catch (Exception e) {
      fail("Exporting event with null end date should not fail: " + e.getMessage());
    }
  }

  @Test
  public void testExportAllDayEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 23, 59, 59);
    events.add(new SingleEvent("All Day Event", start, end, "All Day Meeting", "Conference Room", true, true, null));
    String fileName = "test_all_day.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      String[] lines = content.split("\n");
      String[] fields = lines[1].split(",");
      assertEquals("For all-day event, startTime should be empty", "", fields[2]);
      assertEquals("For all-day event, endTime should be empty", "", fields[4]);
      assertEquals("For all-day event, all-day flag should be TRUE", "TRUE", fields[5]);
    } catch (Exception e) {
      fail("Exporting all-day event should not fail: " + e.getMessage());
    }
  }

  @Test
  public void testExportPrivateEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 15, 0);
    events.add(new SingleEvent("Private Event", start, end, "Confidential", "Home Office", false, false, null));
    String fileName = "test_private_event.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      String[] lines = content.split("\n");
      String[] fields = lines[1].split(",");
      assertEquals("Private flag should be TRUE for private event", "TRUE", fields[8]);
    } catch (Exception e) {
      fail("Exporting private event should not fail: " + e.getMessage());
    }
  }
}

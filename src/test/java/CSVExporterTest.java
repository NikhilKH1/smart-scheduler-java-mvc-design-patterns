import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.utils.CSVExporter;
import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import java.time.format.DateTimeFormatter;


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

  /**
   * Test exporting an event with fields containing special characters
   * (commas, quotes, and newlines) to ensure proper escaping.
   */
  @Test
  public void testExportEventWithSpecialCharacters() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    // Fields include commas, quotes, and newline characters.
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
      // Check that values are properly enclosed in quotes and internal quotes doubled.
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

  /**
   * Test exporting an event with null description and location.
   * This will ensure that null values are converted to empty strings.
   */
  @Test
  public void testExportEventWithNullFields() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    // Create an event with null description and location.
    events.add(new SingleEvent("Test Event", start, end, null, null, true, false, null));
    String fileName = "test_null_fields.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      // Verify that null description and location become empty strings (resulting in consecutive commas)
      assertTrue("Output should show empty fields for null values", content.contains("Test Event,"));
    } catch (Exception e) {
      fail("Exporting event with null fields should not fail: " + e.getMessage());
    }
  }

  @Test
  public void testExportEventWithNullEndDateTime() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    // End time is null; this should trigger the "else" branch.
    events.add(new SingleEvent("No End Event", start, null, "No End", "Office", true, false, null));
    String fileName = "test_null_end.csv";
    try {
      String path = CSVExporter.exportToCSV(events, fileName);
      File file = new File(path);
      assertTrue("Exported file should exist", file.exists());
      String content = new String(Files.readAllBytes(file.toPath()));
      // Expect the start date formatted correctly.
      String expectedStartDate = start.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
      // Verify that when endDateTime is null, the endDate equals the start date and endTime is empty.
      String[] lines = content.split("\n");
      // The header is on line 0; the event is on line 1.
      String[] fields = lines[1].split(",");
      // fields: 0: Subject, 1: Start Date, 2: Start Time, 3: End Date, 4: End Time, 5: All Day Event, etc.
      assertEquals("Start Date should match", expectedStartDate, fields[1]);
      assertEquals("Start Time should be formatted (non-empty) when not an all-day event", "09:00", fields[2]);
      // Since endDateTime is null, branch sets allDayVal to "TRUE", endDate to start date, endTime to empty.
      assertEquals("End Date should match Start Date", expectedStartDate, fields[3]);
      assertEquals("End Time should be empty", "", fields[4]);
      assertEquals("All Day flag should be TRUE", "TRUE", fields[5]);
    } catch (Exception e) {
      fail("Exporting event with null end date should not fail: " + e.getMessage());
    }
  }

  /**
   * Test exporting an event that is explicitly marked as an all-day event.
   * For such an event, both start time and end time fields should be empty and the all-day flag must be TRUE.
   */
  @Test
  public void testExportAllDayEvent() {
    List<CalendarEvent> events = new ArrayList<>();
    // For an all-day event, even if endDateTime is not null, isAllDay is true.
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
      // The event is on line 1.
      String[] fields = lines[1].split(",");
      // For all-day events, startTime and endTime should be empty.
      assertEquals("For all-day event, startTime should be empty", "", fields[2]);
      assertEquals("For all-day event, endTime should be empty", "", fields[4]);
      assertEquals("For all-day event, all-day flag should be TRUE", "TRUE", fields[5]);
    } catch (Exception e) {
      fail("Exporting all-day event should not fail: " + e.getMessage());
    }
  }

  /**
   * Test exporting an event that is marked as private.
   * For a private event, isPublic is false so the private flag in CSV (computed as !isPublic) should be TRUE.
   */
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
      // The last field is the Private column. For a private event (isPublic false), it should be "TRUE".
      assertEquals("Private flag should be TRUE for private event", "TRUE", fields[8]);
    } catch (Exception e) {
      fail("Exporting private event should not fail: " + e.getMessage());
    }
  }
}

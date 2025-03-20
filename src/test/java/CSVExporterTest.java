import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.utils.CSVExporter;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CSVExporterTest {

  private static final String TEST_RES_DIR = "test/res/";

  @Before
  public void setup() throws Exception {
    Files.createDirectories(Paths.get("test/res"));
  }

  @Test
  public void testExportSingleEvent() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Meeting", start, end, "Discussion",
            "Office", true, false, null));

    String fileName = "test/res/test_single_event.csv";
    CSVExporter.exportToCSV(events, fileName);

    File file = new File(fileName);
    assertTrue(file.exists());
  }

  @Test
  public void testExportEventWithSpecialCharacters() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Meeting, Planning & Review", start, end, "Discussion, planning session",
            "Office, Floor #2", true, false, null));

    String fileName = "test/res/test_special_chars.csv";
    CSVExporter.exportToCSV(events, fileName);

    File file = new File(fileName);
    assertTrue(file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Meeting, Planning & Review"));
  }

  @Test
  public void testExportEventWithNullFields() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    events.add(new SingleEvent("EventWithNull", start, null, null,
            null, true, false, null));

    String fileName = "test/res/test_null_fields.csv";
    CSVExporter.exportToCSV(events, fileName);

    File file = new File(fileName);
    assertTrue(file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Event"));
  }

  @Test
  public void testExportEventAllDay() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 2, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 2, 23, 59,
            59);
    events.add(new SingleEvent("All Day Event", start, end, "All day event",
            "Home", true, true, null));

    String fileName = "test/res/test_all_day_event.csv";
    CSVExporter.exportToCSV(events, fileName);

    File file = new File(fileName);
    assertTrue(file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("TRUE"));
  }

  @Test
  public void testExportEventPrivate() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 3, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 3, 15, 0);
    events.add(new SingleEvent("Private Event", start, end, "Confidential",
            "Home Office", false, false, null));

    String fileName = "test/res/test_private_event.csv";
    CSVExporter.exportToCSV(events, fileName);

    File file = new File(fileName);
    assertTrue(file.exists());
    String content = new String(Files.readAllBytes(file.toPath()));
    assertTrue(content.contains("Private Event") || content.contains("TRUE"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportEventFailureInvalidFileName() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Faulty", start, end, "desc",
            "loc", true, false, null));

    CSVExporter.exportToCSV(events, "test/res/invalidFile.txt");
  }



  @Test(expected = IllegalArgumentException.class)
  public void testExportWithInvalidFilename() throws Exception {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Event", start, end, "desc",
            "loc", true, false, null));

    CSVExporter.exportToCSV(events, "test/res/invalid_file.txt");
  }
}

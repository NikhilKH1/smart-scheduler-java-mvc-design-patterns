import calendarapp.model.event.SingleEvent;
import calendarapp.utils.CSVExporter;
import calendarapp.model.event.ICalendarEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CSVExporterTest {

  private CSVExporter exporter;
  private File tempFile;

  @Before
  public void setUp() {
    exporter = new CSVExporter();
  }

  @After
  public void tearDown() {
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  // A sample non-all-day event (isAllDay=false, non-null endDateTime)
  private SingleEvent createSampleEvent() {
    return new SingleEvent("Meeting",
            ZonedDateTime.of(2025, 6, 1, 9, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 6, 1, 10, 0, 0, 0, ZoneId.of("UTC")),
            "Discuss Q2 targets", "Office 101",
            true, false, null);
  }

  @Test
  public void testValidExport() throws Exception {
    List<ICalendarEvent> events = Collections.singletonList(createSampleEvent());
    tempFile = File.createTempFile("calendar_", ".csv");

    String exportedPath = exporter.export(events, tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      String header = reader.readLine();
      String data = reader.readLine();
      assertNotNull(header);
      assertNotNull(data);
      assertTrue(header.contains("Subject"));
      assertTrue(data.contains("Meeting"));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportWithNullFilePathThrows() throws Exception {
    exporter.export(Collections.singletonList(createSampleEvent()), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportWithEmptyFilePathThrows() throws Exception {
    exporter.export(Collections.singletonList(createSampleEvent()), " ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportWithInvalidExtensionThrows() throws Exception {
    exporter.export(Collections.singletonList(createSampleEvent()), "output.txt");
  }

  @Test(expected = IOException.class)
  public void testExportWithEmptyEventsThrows() throws Exception {
    tempFile = File.createTempFile("calendar_", ".csv");
    exporter.export(Collections.emptyList(), tempFile.getAbsolutePath());
  }

  @Test
  public void testEscapeCSVWithCommaAndQuote() throws Exception {
    Method escapeMethod = CSVExporter.class.getDeclaredMethod("escapeCSV", String.class);
    escapeMethod.setAccessible(true);
    CSVExporter exporter = new CSVExporter();

    String input = "Hello, \"World\"";
    String expected = "\"Hello, \"\"World\"\"\"";  // Quotes are escaped and the whole string is quoted

    String result = (String) escapeMethod.invoke(exporter, input);
    assertEquals(expected, result);
  }

  @Test
  public void testExportAllDayEvent() throws Exception {
    // Event explicitly marked as all-day; even though an endDateTime is provided, times will be empty.
    SingleEvent allDayEvent = new SingleEvent("Holiday",
            ZonedDateTime.of(2025, 6, 15, 0, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 6, 15, 23, 59, 0, 0, ZoneId.of("UTC")),
            "National Holiday", "Home",
            true, true, null);
    tempFile = File.createTempFile("allday_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(allDayEvent), tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine(); // skip header
      String line = reader.readLine();
      assertNotNull(line);
      String[] values = line.split(",");
      // CSV columns: Subject, Start Date, Start Time, End Date, End Time, All Day, Description, Location, Private
      // For an all-day event, start and end times should be empty, and the All Day flag should be "TRUE".
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      String expectedStartDate = ZonedDateTime.of(2025, 6, 15, 0, 0, 0, 0, ZoneId.of("UTC")).format(dateFormatter);
      String expectedEndDate = ZonedDateTime.of(2025, 6, 15, 23, 59, 0, 0, ZoneId.of("UTC")).format(dateFormatter);

      assertEquals("Holiday", values[0]);
      assertEquals(expectedStartDate, values[1]);
      assertEquals("", values[2]);            // Start Time empty for all-day event
      assertEquals(expectedEndDate, values[3]);
      assertEquals("", values[4]);            // End Time empty for all-day event
      assertEquals("TRUE", values[5]);          // All Day flag
    }
  }

  @Test
  public void testExportPrivateEvent() throws Exception {
    SingleEvent privateEvent = new SingleEvent("Lunch",
            ZonedDateTime.now(ZoneId.of("UTC")),
            ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1),
            "Personal", "Cafe", false, false, null);

    tempFile = File.createTempFile("private_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(privateEvent), tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine(); // skip header
      String line = reader.readLine();
      assertNotNull(line);
      // Check that the private flag (last column) is "TRUE" if the event is not public
      String[] values = line.split(",");
      assertTrue(values[8].equals("TRUE"));
    }
  }

  @Test
  public void testCreatesMissingDirectory() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "calendar_test_dir");
    if (tempDir.exists()) {
      tempDir.delete();
    }

    File file = new File(tempDir, "events.csv");
    SingleEvent event = createSampleEvent();

    String exportedPath = exporter.export(Collections.singletonList(event), file.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());
    assertTrue(tempDir.exists());

    // Cleanup
    new File(exportedPath).delete();
    tempDir.delete();
  }

  @Test
  public void testExportFailsWhenDirectoryCannotBeCreated() {
    // Given a file path with a parent directory that cannot be created (using a file as parent)
    File tempFile = new File("tempfile.txt");
    try {
      tempFile.createNewFile();  // create a file so it exists as a file (not a directory)
      String filePath = "tempfile.txt/export.csv";  // invalid: attempting to use a file as a directory

      CSVExporter exporter = new CSVExporter();
      ICalendarEvent dummyEvent = new SingleEvent(
              "Event", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
              "desc", "loc", true, false, null
      );
      List<ICalendarEvent> events = Collections.singletonList(dummyEvent);
      assertThrows(IOException.class, () -> exporter.export(events, filePath));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      tempFile.delete();
    }
  }

  // --- New tests to target the surviving mutants ---

  @Test
  public void testExportWithNullEndDateTime() throws Exception {
    // For line 79, 81, 82: When endDateTime is null, even if event.isAllDay() is false,
    // the computed isAllDay becomes true, endDate defaults to startDate, and endTime is empty.
    SingleEvent eventWithNullEnd = new SingleEvent("No End",
            ZonedDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneId.of("UTC")),
            null,  // null endDateTime
            "Description", "Location", true, false, null);
    tempFile = File.createTempFile("noend_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(eventWithNullEnd), tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      String header = reader.readLine();
      String data = reader.readLine();
      assertNotNull(header);
      assertNotNull(data);
      String[] values = data.split(",");
      // Columns: Subject, Start Date, Start Time, End Date, End Time, All Day, Description, Location, Private
      // For an event with null endDateTime:
      // - startDate is formatted from startDateTime
      // - endDate should default to startDate
      // - isAllDay becomes true (because endDateTime == null), so endTime is ""
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
      String expectedStartDate = ZonedDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneId.of("UTC")).format(dateFormatter);
      String expectedStartTime = ZonedDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneId.of("UTC")).format(timeFormatter);

      assertEquals("No End", values[0]);
      assertEquals(expectedStartDate, values[1]);  // Start Date
      assertEquals(expectedStartTime, values[2]);    // Start Time (since event.isAllDay() is false originally)
      assertEquals(expectedStartDate, values[3]);    // End Date defaults to Start Date (endDateTime is null)
      assertEquals("", values[4]);                   // End Time empty due to computed all-day
      // Also, All Day flag should be "TRUE" because of null endDateTime.
      assertEquals("TRUE", values[5]);
    }
  }

  @Test
  public void testExportNonAllDayEventCSVOutput() throws Exception {
    // For a non-all-day event with non-null endDateTime, the branch where event.isAllDay() is false.
    SingleEvent nonAllDay = new SingleEvent("NonAllDayEvent",
            ZonedDateTime.of(2025, 9, 1, 10, 30, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 9, 1, 11, 30, 0, 0, ZoneId.of("UTC")),
            "Regular Meeting", "Conference Room", true, false, null);
    tempFile = File.createTempFile("nonallday_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(nonAllDay), tempFile.getAbsolutePath());
    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine(); // skip header
      String line = reader.readLine();
      assertNotNull(line);
      String[] values = line.split(",");
      // Expected output for non-all-day event:
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
      String expectedStartDate = ZonedDateTime.of(2025, 9, 1, 10, 30, 0, 0, ZoneId.of("UTC")).format(dateFormatter);
      String expectedStartTime = ZonedDateTime.of(2025, 9, 1, 10, 30, 0, 0, ZoneId.of("UTC")).format(timeFormatter);
      String expectedEndDate = ZonedDateTime.of(2025, 9, 1, 11, 30, 0, 0, ZoneId.of("UTC")).format(dateFormatter);
      String expectedEndTime = ZonedDateTime.of(2025, 9, 1, 11, 30, 0, 0, ZoneId.of("UTC")).format(timeFormatter);

      assertEquals("NonAllDayEvent", values[0]);
      assertEquals(expectedStartDate, values[1]);
      assertEquals(expectedStartTime, values[2]);
      assertEquals(expectedEndDate, values[3]);
      assertEquals(expectedEndTime, values[4]);
      // All Day flag should be "FALSE"
      assertEquals("FALSE", values[5]);
      assertEquals("Regular Meeting", values[6]);
      assertEquals("Conference Room", values[7]);
    }
  }

  @Test
  public void testExportWithNullDescriptionAndLocation() throws Exception {
    // For lines 86 and 87: when description and location are null, they should output as empty strings.
    SingleEvent eventWithNullDescLoc = new SingleEvent("Null DescLoc",
            ZonedDateTime.of(2025, 8, 1, 14, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 8, 1, 15, 0, 0, 0, ZoneId.of("UTC")),
            null,  // null description
            null,  // null location
            true, false, null);
    tempFile = File.createTempFile("nulldesc_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(eventWithNullDescLoc), tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine(); // skip header
      String line = reader.readLine();
      assertNotNull(line);
      String[] values = line.split(",");
      // Expect empty strings for description (index 6) and location (index 7)
      assertEquals("", values[6]);
      assertEquals("", values[7]);
    }
  }
}

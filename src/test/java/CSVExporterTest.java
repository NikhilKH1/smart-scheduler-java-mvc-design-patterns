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
    String expected = "\"Hello, \"\"World\"\"\"";  // Double quotes inside, whole string in quotes

    String result = (String) escapeMethod.invoke(exporter, input);
    assertEquals(expected, result);  // Better than assertTrue
  }


  @Test
  public void testExportAllDayEvent() throws Exception {
    SingleEvent allDayEvent = new SingleEvent("Holiday",
            ZonedDateTime.of(2025, 6, 15, 0, 0, 0,
                    0, ZoneId.of("UTC")),
            ZonedDateTime.of(2025, 6, 15, 23, 59, 0,
                    0, ZoneId.of("UTC")),
            "National Holiday", "Home",
            true, true, null);

    tempFile = File.createTempFile("allday_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(allDayEvent),
            tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine(); // skip header
      String line = reader.readLine();
      assertNotNull(line);
      String[] values = line.split(",");
      assertEquals("TRUE", values[5]); // All Day Event
      assertEquals("", values[2]);     // Start Time
      assertEquals("", values[4]);     // End Time
    }
  }

  @Test
  public void testExportPrivateEvent() throws Exception {
    SingleEvent privateEvent = new SingleEvent("Lunch",
            ZonedDateTime.now(ZoneId.of("UTC")),
            ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1),
            "Personal", "Cafe", false, false, null);

    tempFile = File.createTempFile("private_", ".csv");

    String exportedPath = exporter.export(Collections.singletonList(privateEvent),
            tempFile.getAbsolutePath());
    assertTrue(new File(exportedPath).exists());

    try (BufferedReader reader = new BufferedReader(new FileReader(exportedPath))) {
      reader.readLine();
      String line = reader.readLine();
      assertTrue(line.endsWith("TRUE"));
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
    // Given a file path with a parent directory that cannot be created (use a file as parent)
    File tempFile = new File("tempfile.txt");

    try {
      tempFile.createNewFile();  // create a file
      String filePath = "tempfile.txt/export.csv";  // attempt to write inside it

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


}

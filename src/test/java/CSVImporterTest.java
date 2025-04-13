
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.utils.CSVImporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit tests for the CSVImporte class.
 */
public class CSVImporterTest {

  private File tempFile;
  private TestCalendarModel model;
  private CSVImporter importer;

  @Before
  public void setUp() throws IOException {
    importer = new CSVImporter();
    model = new TestCalendarModel();
    tempFile = File.createTempFile("test-events", ".csv");
  }

  @After
  public void tearDown() {
    if (tempFile.exists()) {
      tempFile.delete();
    }
  }

  @Test
  public void testImportSingleEvent() throws IOException {
    String content = "Name,StartDate,StartTime,EndDate,EndTime,ignored,Description,Location\n" +
            "Meeting,03/25/2024,09:00,03/25/2024,10:00,,Team Sync,Conference Room\n";
    writeToTempFile(content);
    importer.importInto(model, tempFile.getAbsolutePath());

    assertEquals(1, model.events.size());
    ICalendarEvent event = model.events.get(0);
    assertTrue(event.toString().contains("Meeting"));
  }

  @Test
  public void testImportRecurringEventWithRepeatCount() throws IOException {
    String content = "Name,StartDate,StartTime,EndDate,EndTime,ignored,Description,"
            + "Location,Weekdays,RepeatUntil,RepeatCount\n" +
            "Yoga,2024-04-01,07:00,2024-04-01,08:00,,Morning Yoga,Gym,MWF,,5\n";
    writeToTempFile(content);
    importer.importInto(model, tempFile.getAbsolutePath());

    assertEquals(1, model.events.size());
    assertTrue(model.events.get(0).toString().contains("Yoga"));
  }

  @Test
  public void testImportRecurringEventWithRepeatUntil() throws IOException {
    String content = "Name,StartDate,StartTime,EndDate,EndTime,ignored,Description,"
            + "Location,Weekdays,RepeatUntil\n" +
            "Yoga,04/01/2024,07:00,04/01/2024,08:00,,Morning Yoga,Gym,MWF,04/30/2024\n";
    writeToTempFile(content);
    importer.importInto(model, tempFile.getAbsolutePath());

    assertEquals(1, model.events.size());
  }

  @Test
  public void testImportWithInvalidDateThrowsException() throws IOException {
    String content = "Name,StartDate,StartTime,EndDate,EndTime,ignored,Description,Location\n" +
            "BadEvent,13/01/2024,09:00,13/01/2024,10:00,,Oops,Nowhere\n";
    writeToTempFile(content);

    try {
      importer.importInto(model, tempFile.getAbsolutePath());
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid date format"));
    }
  }

  @Test
  public void testEmptyLineAndShortLineSkipped() throws IOException {
    String content = "Name,StartDate,StartTime,EndDate,EndTime,ignored,Description,Location\n" +
            "\n" +
            "IncompleteLine,2024-04-01,07:00\n";
    writeToTempFile(content);
    importer.importInto(model, tempFile.getAbsolutePath());
    assertEquals(0, model.events.size());
  }

  private void writeToTempFile(String content) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
      writer.write(content);
    }
  }

  private static class TestCalendarModel implements ICalendarModel {
    List<ICalendarEvent> events = new ArrayList<>();

    @Override
    public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEvents() {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      return List.of();
    }

    @Override
    public boolean isBusyAt(ZonedDateTime dateTime) {
      return false;
    }

    @Override
    public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
      return false;
    }

    @Override
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return false;
    }

    @Override
    public boolean editSingleEvent(String property, String eventName,
                                   ZonedDateTime originalStart, ZonedDateTime originalEnd,
                                   String newValue) {
      return false;
    }

    @Override
    public boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime,
                                  String newValue) {
      return false;
    }

    @Override
    public boolean editEventsAll(String property, String eventName, String newValue) {
      return false;
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public ZoneId getTimezone() {
      return ZoneId.systemDefault();
    }

    @Override
    public void updateTimezone(ZoneId newTimezone) {

    }

    @Override
    public boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName,
                                     ZonedDateTime sourceDateTime, ICalendarModel targetCalendar,
                                     ZonedDateTime targetDateTime) {
      return false;
    }

    @Override
    public boolean copyEventsOnDateTo(ICalendarModel sourceCalendar, ZonedDateTime sourceDate,
                                      ICalendarModel targetCalendar, ZonedDateTime targetDate) {
      return false;
    }

    @Override
    public boolean copyEventsBetweenTo(ICalendarModel sourceCalendar, ZonedDateTime startDate,
                                       ZonedDateTime endDate, ICalendarModel targetCalendar,
                                       ZonedDateTime targetStartDate) {
      return false;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getReadOnlyEventsOnDate(LocalDate date) {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
      return List.of();
    }

  }
}

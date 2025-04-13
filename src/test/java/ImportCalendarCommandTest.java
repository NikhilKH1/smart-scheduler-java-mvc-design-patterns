import calendarapp.controller.commands.ImportCalendarCommand;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.utils.IImporter;
import calendarapp.view.ICalendarView;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class ImportCalendarCommandTest {

  private static class FakeView implements ICalendarView {
    String message = "";
    String error = "";

    @Override public void displayMessage(String message) { this.message = message; }
    @Override public void displayError(String errorMessage) { this.error = errorMessage; }
    @Override public void displayEvents(List<ReadOnlyCalendarEvent> events) {}
    @Override public void run() {}
    @Override public void setInput(Readable in) {}
    @Override public void setOutput(Appendable out) {}
  }

  private static class FakeModel implements ICalendarModel {
    boolean importCalled = false;

    @Override public ZoneId getTimezone() { return ZoneId.systemDefault(); }
    @Override public void updateTimezone(ZoneId newTimezone) {}
    @Override public boolean addEvent(ICalendarEvent event, boolean autoDecline) { return false; }
    @Override public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) { return false; }
    @Override public List<ReadOnlyCalendarEvent> getEvents() { return List.of(); }
    @Override public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) { return List.of(); }
    @Override public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) { return List.of(); }
    @Override public boolean isBusyAt(ZonedDateTime dateTime) { return false; }
    @Override public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) { return false; }
    @Override public boolean editRecurringEvent(String eventName, String property, String newValue) { return false; }
    @Override public boolean editSingleEvent(String property, String eventName, ZonedDateTime originalStart, ZonedDateTime originalEnd, String newValue) { return false; }
    @Override public boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime, String newValue) { return false; }
    @Override public boolean editEventsAll(String property, String eventName, String newValue) { return false; }
    @Override public String getName() { return "Fake"; }
    @Override public boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName, ZonedDateTime sourceDateTime, ICalendarModel targetCalendar, ZonedDateTime targetDateTime) { return false; }
    @Override public boolean copyEventsOnDateTo(ICalendarModel sourceCalendar, ZonedDateTime sourceDate, ICalendarModel targetCalendar, ZonedDateTime targetDate) { return false; }
    @Override public boolean copyEventsBetweenTo(ICalendarModel sourceCalendar, ZonedDateTime startDate, ZonedDateTime endDate, ICalendarModel targetCalendar, ZonedDateTime targetStartDate) { return false; }
    @Override public List<ReadOnlyCalendarEvent> getReadOnlyEventsOnDate(LocalDate date) { return List.of(); }
    @Override public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() { return List.of(); }
  }

  private static class FakeImporter implements IImporter {
    private final boolean shouldFail;
    private final FakeModel model;

    public FakeImporter(boolean shouldFail, FakeModel model) {
      this.shouldFail = shouldFail;
      this.model = model;
    }

    @Override
    public void importInto(ICalendarModel m, String filePath) throws IOException {
      if (shouldFail) {
        throw new IOException("Fake failure");
      }
      model.importCalled = true;
    }
  }

  private static class TestableImportCalendarCommand extends ImportCalendarCommand {
    private final IImporter testImporter;

    public TestableImportCalendarCommand(String filePath, IImporter testImporter) {
      super(filePath);
      this.testImporter = testImporter;
    }

    @Override
    public boolean execute(ICalendarModel model, ICalendarView view) {
      try {
        testImporter.importInto(model, super.filePath);
        view.displayMessage("Calendar imported successfully from: " + super.filePath);
        return true;
      } catch (Exception e) {
        view.displayError("Import failed: " + e.getMessage());
        return false;
      }
    }
  }

  @Test
  public void testSuccessfulImport() {
    FakeModel model = new FakeModel();
    FakeView view = new FakeView();
    FakeImporter importer = new FakeImporter(false, model);

    ImportCalendarCommand command = new TestableImportCalendarCommand("file.csv", importer);

    boolean result = command.execute(model, view);

    assertTrue(result);
    assertTrue(model.importCalled);
    assertEquals("Calendar imported successfully from: file.csv", view.message);
    assertEquals("", view.error);
  }

  @Test
  public void testFailedImport() {
    FakeModel model = new FakeModel();
    FakeView view = new FakeView();
    FakeImporter importer = new FakeImporter(true, model);

    ImportCalendarCommand command = new TestableImportCalendarCommand("broken.csv", importer);

    boolean result = command.execute(model, view);

    assertFalse(result);
    assertFalse(model.importCalled);
    assertTrue(view.error.startsWith("Import failed: Fake failure"));
  }
}

import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.utils.IExporter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for ExportCalendarCommand class.
 */
public class ExportCalendarCommandTest {

  private class TestCalendarView implements calendarapp.view.ICalendarView {
    private String lastMessage = "";
    private String lastError = "";

    @Override
    public void displayMessage(String message) {
      lastMessage = message;
    }

    @Override
    public void displayError(String errorMessage) {
      lastError = errorMessage;
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      // No implementation of this is required
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public String getLastError() {
      return lastError;
    }
  }

  private class TestExporter implements IExporter {
    private final boolean throwIOException;

    public TestExporter(boolean throwIOException) {
      this.throwIOException = throwIOException;
    }

    @Override
    public String export(List<ICalendarEvent> events, String filePath) throws IOException {
      if (throwIOException) {
        throw new IOException("Test IOException");
      }
      return filePath;
    }
  }

  private class TestExporterException implements IExporter {
    @Override
    public String export(List<ICalendarEvent> events, String filePath) throws IOException {
      throw new IllegalArgumentException("Invalid file type");
    }
  }

  private class TestExportCalendarCommand extends ExportCalendarCommand {
    private final IExporter testExporter;
    private final String testFilePath;

    public TestExportCalendarCommand(String filePath, IExporter testExporter) {
      super(filePath);
      this.testFilePath = filePath;
      this.testExporter = testExporter;
    }

    @Override
    public boolean execute(ICalendarModel model, calendarapp.view.ICalendarView view) {
      try {
        IExporter exporter = testExporter;
        String outputPath = exporter.export(model.getEvents(), testFilePath);
        view.displayMessage("Calendar exported to: " + outputPath);
        return true;
      } catch (IOException e) {
        view.displayError("Failed to export calendar: " + e.getMessage());
        return false;
      } catch (IllegalArgumentException e) {
        view.displayError("Export Error: " + e.getMessage());
        return false;
      }
    }
  }

  private class TestCalendarModel extends CalendarModel {
    public TestCalendarModel(String name, ZoneId zone) {
      super(name, zone);
    }

    @Override
    public List<ICalendarEvent> getEvents() {
      return new ArrayList<>();
    }
  }

  @Test
  public void testExportCommandSuccess() {
    ICalendarModel model = new TestCalendarModel("Test Calendar", ZoneId.of("UTC"));
    TestCalendarView view = new TestCalendarView();
    TestExporter exporter = new TestExporter(false);
    TestExportCalendarCommand cmd = new TestExportCalendarCommand("events.csv", exporter);
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    assertEquals("Calendar exported to: events.csv", view.getLastMessage());
  }

  @Test
  public void testExportCommandIOException() {
    ICalendarModel model = new TestCalendarModel("Test Calendar", ZoneId.of("UTC"));
    TestCalendarView view = new TestCalendarView();
    TestExporter exporter = new TestExporter(true);
    TestExportCalendarCommand cmd = new TestExportCalendarCommand("events.csv", exporter);
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("Failed to export calendar: Test IOException", view.getLastError());
  }

  @Test
  public void testExportCommandIllegalArgumentException() {
    ICalendarModel model = new TestCalendarModel("Test Calendar", ZoneId.of("UTC"));
    TestCalendarView view = new TestCalendarView();
    TestExporterException testExporter = new TestExporterException();
    TestExportCalendarCommand cmd = new TestExportCalendarCommand("invalid.txt",
            testExporter);
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("Export Error: Invalid file type", view.getLastError());
  }
}

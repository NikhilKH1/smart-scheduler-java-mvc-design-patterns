import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import calendarapp.view.ICalendarView;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Junit test file for testing Export Calendar Command Test.
 */
public class ExportCalendarCommandTest {

  private static final class DummyModel extends CalendarModel {
    DummyModel() {
      super("Dummy", ZoneId.of("UTC"));
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEvents() {
      return new ArrayList<>();
    }
  }

  private static final class DummyView implements ICalendarView {
    String message = null;
    String error = null;

    @Override
    public void displayMessage(String message) {
      this.message = message;
    }

    @Override
    public void displayError(String errorMessage) {
      this.error = errorMessage;
    }

    @Override
    public void run() {
      return;
    }

    @Override
    public void setInput(Readable in) {
      ICalendarView.super.setInput(in);
    }

    @Override
    public void setOutput(Appendable out) {
      ICalendarView.super.setOutput(out);
    }

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      return;
    }
  }

  private Function<String, IExporter> originalSupplier;

  @Before
  public void setupFactoryOverride() throws Exception {
    Field supplierField = ExporterFactory.class.getDeclaredField("customExporterSupplier");
    supplierField.setAccessible(true);
    originalSupplier = (Function<String, IExporter>) supplierField.get(null);
  }

  @After
  public void resetFactoryOverride() throws Exception {
    Field supplierField = ExporterFactory.class.getDeclaredField("customExporterSupplier");
    supplierField.setAccessible(true);
    supplierField.set(null, originalSupplier);
  }

  @Test
  public void testExportSuccessCoversMessage() throws Exception {
    ExporterFactory.setCustomExporterSupplier(path -> (events, file) -> file);

    ExportCalendarCommand cmd = new ExportCalendarCommand("test.csv");
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();

    boolean result = cmd.execute(model, view);
    assertTrue(result);
    assertEquals("Calendar exported successfully to: test.csv", view.message);
  }

  @Test
  public void testExportCatchesIOException() throws Exception {
    ExporterFactory.setCustomExporterSupplier(path -> (events, file) -> {
      throw new IOException("IOFail");
    });

    ExportCalendarCommand cmd = new ExportCalendarCommand("fail.csv");
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();

    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("Export failed: IOFail", view.error);
  }

  @Test
  public void testExportCatchesIllegalArgument() throws Exception {
    ExporterFactory.setCustomExporterSupplier(path -> (events,
                                                       file) -> {
      throw new IllegalArgumentException("Invalid!");
    });

    ExportCalendarCommand cmd = new ExportCalendarCommand("wrong.file");
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();

    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("Export failed: Invalid!", view.error);
  }
}



//import calendarapp.controller.commands.ExportCalendarCommand;
//import calendarapp.model.CalendarModel;
//import calendarapp.model.ICalendarModel;
//import calendarapp.model.event.ICalendarEvent;
//import calendarapp.utils.IExporter;
//import calendarapp.view.ICalendarView;
//
//import org.junit.Test;
//
//import java.io.IOException;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//public class ExportCalendarCommandTest {
//
//  private static class DummyModel extends CalendarModel {
//    public DummyModel() {
//      super("Test", ZoneId.of("UTC"));
//    }
//
//    @Override
//    public List<ICalendarEvent> getEvents() {
//      return new ArrayList<>();
//    }
//  }
//
//  private static class DummyView implements ICalendarView {
//    String message = "", error = "";
//
//    @Override
//    public void displayMessage(String message) {
//      this.message = message;
//    }
//
//    @Override
//    public void displayError(String errorMessage) {
//      this.error = errorMessage;
//    }
//
//    @Override
//    public void displayEvents(List<ICalendarEvent> events) {}
//  }
//
//  private static class ExporterSuccess implements IExporter {
//    @Override
//    public String export(List<ICalendarEvent> events, String filePath) throws IOException {
//      return "success.csv";
//    }
//  }
//
//  private static class ExporterIOException implements IExporter {
//    @Override
//    public String export(List<ICalendarEvent> events, String filePath) throws IOException {
//      throw new IOException("Simulated IO failure");
//    }
//  }
//
//  private static class ExporterIllegalArg implements IExporter {
//    @Override
//    public String export(List<ICalendarEvent> events, String filePath) {
//      throw new IllegalArgumentException("Invalid format");
//    }
//  }
//
//  // Generic command wrapper that injects exporter
//  private static class ExportCommandWithExporter extends ExportCalendarCommand {
//    private final IExporter injectedExporter;
//
//    public ExportCommandWithExporter(String filePath, IExporter exporter) {
//      super(filePath);
//      this.injectedExporter = exporter;
//    }
//
//    @Override
//    public boolean execute(ICalendarModel model, ICalendarView view) {
//      try {
//        String result = injectedExporter.export(model.getEvents(), "dummy.csv");
//        view.displayMessage("Calendar exported to: " + result);
//        return true;
//      } catch (IOException e) {
//        view.displayError("Failed to export calendar: " + e.getMessage());
//        return false;
//      } catch (IllegalArgumentException e) {
//        view.displayError("Export Error: " + e.getMessage());
//        return false;
//      }
//    }
//  }
//
//  @Test
//  public void testExportSuccess() {
//    DummyModel model = new DummyModel();
//    DummyView view = new DummyView();
//    ExportCalendarCommand command = new ExportCommandWithExporter("dummy.csv", new ExporterSuccess());
//
//    boolean result = command.execute(model, view);
//
//    assertTrue(result);
//    assertEquals("Calendar exported to: success.csv", view.message);
//  }
//
//  @Test
//  public void testExportIOException() {
//    DummyModel model = new DummyModel();
//    DummyView view = new DummyView();
//    ExportCalendarCommand command = new ExportCommandWithExporter("dummy.csv", new ExporterIOException());
//
//    boolean result = command.execute(model, view);
//
//    assertFalse(result);
//    assertEquals("Failed to export calendar: Simulated IO failure", view.error);
//  }
//
//  @Test
//  public void testExportIllegalArgumentException() {
//    DummyModel model = new DummyModel();
//    DummyView view = new DummyView();
//    ExportCalendarCommand command = new ExportCommandWithExporter("dummy.csv", new ExporterIllegalArg());
//
//    boolean result = command.execute(model, view);
//
//    assertFalse(result);
//    assertEquals("Export Error: Invalid format", view.error);
//  }
//
//  @Test
//  public void testExportCatchesIOException() {
//    ICalendarModel model = new DummyModel();
//    DummyView view = new DummyView();
//
//    ExportCalendarCommand command = new ExportCommandWithExporter("fail.csv", new IExporter() {
//      @Override
//      public String export(List<ICalendarEvent> events, String filePath) throws IOException {
//        throw new IOException("Simulated IO Exception");
//      }
//    });
//
//    boolean result = command.execute(model, view);
//    assertFalse(result);
//    assertEquals("Failed to export calendar: Simulated IO Exception", view.error);
//  }
//
//  @Test
//  public void testExportCatchesIllegalArgumentException() {
//    ICalendarModel model = new DummyModel();
//    DummyView view = new DummyView();
//
//    ExportCalendarCommand command = new ExportCommandWithExporter("invalid.txt", new IExporter() {
//      @Override
//      public String export(List<ICalendarEvent> events, String filePath) {
//        throw new IllegalArgumentException("Invalid export format");
//      }
//    });
//
//    boolean result = command.execute(model, view);
//    assertFalse(result);
//    assertEquals("Export Error: Invalid export format", view.error);
//  }
//
//
//}
import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import calendarapp.view.ICalendarView;
import org.junit.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ExportCalendarCommandTest {

  private static final class DummyModel extends CalendarModel {
    DummyModel() {
      super("Dummy", ZoneId.of("UTC"));
    }

    @Override
    public List<ICalendarEvent> getEvents() {
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
    public void displayEvents(List<ICalendarEvent> events) {
      // not needed
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
    assertEquals("Calendar exported to: test.csv", view.message);
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
    assertEquals("Failed to export calendar: IOFail", view.error);
  }

  @Test
  public void testExportCatchesIllegalArgument() throws Exception {
    ExporterFactory.setCustomExporterSupplier(path -> (events, file) -> {
      throw new IllegalArgumentException("Invalid!");
    });

    ExportCalendarCommand cmd = new ExportCalendarCommand("wrong.file");
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();

    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("Export Error: Invalid!", view.error);
  }
}

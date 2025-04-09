import calendarapp.utils.CSVExporter;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * JUnit test class for ExporterFactory class with full coverage and mutation killing.
 */
public class ExporterFactoryTest {

  @Before
  public void setUp() {
    ExporterFactory.clearCustomExporterSupplier();
  }

  @After
  public void tearDown() {
    ExporterFactory.clearCustomExporterSupplier(); // Reset state after each test
  }

  @Test
  public void testGetExporterReturnsCSVExporter() {
    IExporter exporter = ExporterFactory.getExporter("calendar.csv");
    assertNotNull(exporter);
    assertTrue(exporter instanceof CSVExporter);
  }

  @Test
  public void testGetExporterIgnoresCaseInExtension() {
    IExporter exporter = ExporterFactory.getExporter("CALENDAR.CSV");
    assertNotNull(exporter);
    assertTrue(exporter instanceof CSVExporter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetExporterThrowsOnUnsupportedExtension() {
    ExporterFactory.getExporter("calendar.txt");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetExporterThrowsOnEmptyFileName() {
    ExporterFactory.getExporter("");
  }

  @Test(expected = NullPointerException.class)
  public void testGetExporterThrowsOnNullFileName() {
    ExporterFactory.getExporter(null);
  }

  @Test
  public void testSetCustomExporterSupplierOverridesDefaultLogic() throws IOException {
    ExporterFactory.setCustomExporterSupplier(path -> (events, file) -> "custom-output.csv");
    IExporter exporter = ExporterFactory.getExporter("any.txt");
    assertEquals("custom-output.csv", exporter.export(null, null));
  }

  @Test
  public void testClearCustomExporterSupplierRestoresDefaultBehavior() {
    ExporterFactory.setCustomExporterSupplier(path -> (events, file) -> "custom-output.csv");
    ExporterFactory.clearCustomExporterSupplier();
    IExporter exporter = ExporterFactory.getExporter("calendar.csv");
    assertTrue(exporter instanceof CSVExporter);
  }
}

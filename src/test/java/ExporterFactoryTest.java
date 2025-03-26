import calendarapp.utils.CSVExporter;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExporterFactoryTest {

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
}

import org.junit.Test;

import calendarapp.utils.CSVImporter;
import calendarapp.utils.IImporter;
import calendarapp.utils.ImporterFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the ImporterFactory class.
 */
public class ImporterFactoryTest {

  @Test
  public void testGetImporter_WithCSVFile_ReturnsCSVImporter() {
    IImporter importer = ImporterFactory.getImporter("events.csv");
    assertNotNull(importer);
    assertTrue(importer instanceof CSVImporter);
  }

  @Test
  public void testGetImporter_WithUpperCaseExtension_ReturnsCSVImporter() {
    IImporter importer = ImporterFactory.getImporter("EVENTS.CSV");
    assertNotNull(importer);
    assertTrue(importer instanceof CSVImporter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetImporter_WithUnsupportedExtension_ThrowsException() {
    ImporterFactory.getImporter("events.txt");
  }
}

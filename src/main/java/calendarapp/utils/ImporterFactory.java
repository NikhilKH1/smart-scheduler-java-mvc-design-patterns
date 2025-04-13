package calendarapp.utils;

/**
 * A factory class to provide the appropriate importer based on file extension.
 */
public class ImporterFactory {

  /**
   * Returns an appropriate IImporter implementation based on the file extension.
   * Currently, supports importing from CSV files.
   *
   * @param filePath the path of the file to be imported
   * @return an instance of IImporter suitable for the given file type
   * @throws IllegalArgumentException if the file type is unsupported
   */
  public static IImporter getImporter(String filePath) {
    if (filePath.toLowerCase().endsWith(".csv")) {
      return new CSVImporter();
    }
    throw new IllegalArgumentException("Unsupported file format for import: " + filePath);
  }
}

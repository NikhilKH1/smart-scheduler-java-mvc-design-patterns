package calendarapp.utils;

public class ImporterFactory {
  public static IImporter getImporter(String filePath) {
    if (filePath.toLowerCase().endsWith(".csv")) {
      return new CSVImporter();
    }
    throw new IllegalArgumentException("Unsupported file format for import: " + filePath);
  }
}

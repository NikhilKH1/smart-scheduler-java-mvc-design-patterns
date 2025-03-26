package calendarapp.utils;

/**
 * The ExporterFactory class provides a method to obtain the appropriate exporter based
 * on the file extension.
 */
public class ExporterFactory {

  /**
   * Returns an appropriate exporter based on the given file path.
   * If the file path ends with ".csv", a CSVExporter is returned.
   * If the file path has an unsupported extension, an IllegalArgumentException is thrown.
   *
   * @param filePath the file path for which the exporter is needed
   * @return an instance of IExporter corresponding to the file type
   * @throws IllegalArgumentException if the file extension is not supported
   */
  public static IExporter getExporter(String filePath) {
    if (filePath.toLowerCase().endsWith(".csv")) {
      return new CSVExporter();
    }
    throw new IllegalArgumentException("Unsupported file type: " + filePath);
  }
}

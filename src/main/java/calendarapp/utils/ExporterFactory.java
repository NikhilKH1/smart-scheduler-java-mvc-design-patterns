package calendarapp.utils;

import java.util.function.Function;

/**
 * The ExporterFactory class provides a method to obtain the appropriate exporter based
 * on the file extension.
 */
public class ExporterFactory {
  private static Function<String, IExporter> customExporterSupplier = null;

  /**
   * Allows tests to inject a custom exporter factory.
   *
   * @param supplier a function that returns an IExporter for the given file path
   */
  public static void setCustomExporterSupplier(Function<String, IExporter> supplier) {
    customExporterSupplier = supplier;
  }

  /**
   * Clears the custom supplier and restores default exporter logic.
   */
  public static void clearCustomExporterSupplier() {
    customExporterSupplier = null;
  }

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
    if (customExporterSupplier != null) {
      return customExporterSupplier.apply(filePath);
    }

    if (filePath.toLowerCase().endsWith(".csv")) {
      return new CSVExporter();
    }

    throw new IllegalArgumentException("Unsupported file type: " + filePath);
  }
}

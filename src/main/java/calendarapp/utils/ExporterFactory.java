// File: utils/ExporterFactory.java
package calendarapp.utils;

public class ExporterFactory {
  public static IExporter getExporter(String filePath) {
    if (filePath.toLowerCase().endsWith(".csv")) {
      return new CSVExporter();
    }
    // Future support:
    // else if (filePath.toLowerCase().endsWith(".pdf")) return new PDFExporter();
    // else if (filePath.toLowerCase().endsWith(".txt")) return new TextExporter();

    throw new IllegalArgumentException("Unsupported file type: " + filePath);
  }
}

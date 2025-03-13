package calendarapp.model.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.utils.CSVExporter;

import java.io.IOException;
import java.util.List;

/**
 * Command to export calendar events to a CSV file.
 */
public class ExportCalendarCommand implements Command {
  private final CalendarModel model;
  private final String fileName;

  /**
   * Constructs an ExportCalendarCommand.
   *
   * @param model    the calendar model from which events are exported
   * @param fileName the name of the output CSV file
   */
  public ExportCalendarCommand(CalendarModel model, String fileName) {
    if (model == null || fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("Model and file name must not be null or empty.");
    }
    this.model = model;
    this.fileName = fileName;
  }

  /**
   * Executes the export operation.
   *
   * @return the path of the exported CSV file
   * @throws IOException if an error occurs during file writing
   */
  public String execute() throws IOException {
    List<CalendarEvent> events = model.getEvents();
    if (events.isEmpty()) {
      throw new IOException("No events available for export.");
    }
    return CSVExporter.exportToCSV(events, fileName);
  }

  /**
   * Gets the file name for the export.
   *
   * @return the file name of the exported CSV
   */
  public String getFileName() {
    return fileName;
  }
}

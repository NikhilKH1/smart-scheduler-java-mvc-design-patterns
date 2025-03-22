package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.utils.CSVExporter;
import calendarapp.view.ICalendarView;

import java.io.IOException;
import java.util.List;

/**
 * Command to export calendar events to a CSV file.
 */
public class ExportCalendarCommand implements CalendarModelCommand {
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
  @Override
  public boolean execute(CalendarModel model, ICalendarView view) {
    try {
      String filePath = CSVExporter.exportToCSV(model.getEvents(), fileName);
      view.displayMessage("Calendar exported successfully to: " + filePath);
      return true;
    } catch (IOException e) {
      view.displayError("Error exporting calendar: " + e.getMessage());
      return false;
    }
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

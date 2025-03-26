package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import calendarapp.view.ICalendarView;

import java.io.IOException;

/**
 * Command to export a calendar to a file using a supported exporter (CSV, PDF, etc).
 */
public class ExportCalendarCommand implements ICalendarModelCommand {

  private final String filePath;

  /**
   * Constructs an ExportCalendarCommand with the specified file path.
   *
   * @param filePath the destination file path for the exported calendar
   */
  public ExportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Executes the command to export calendar events to the specified file.
   *
   * @param model the calendar model containing events to export
   * @param view  the view used to display messages
   * @return true if the export is successful, false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      IExporter exporter = ExporterFactory.getExporter(filePath);
      String outputPath = exporter.export(model.getEvents(), filePath);
      view.displayMessage("Calendar exported to: " + outputPath);
      return true;
    } catch (IOException e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      view.displayError("Export Error: " + e.getMessage());
      return false;
    }
  }
}

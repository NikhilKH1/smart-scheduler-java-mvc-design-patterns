package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import calendarapp.view.ICalendarView;

import java.io.IOException;

/**
 * Command to export the calendar's events to a specified file.
 * Uses the ExporterFactory to choose the correct exporter based on the file path.
 */
public class ExportCalendarCommand implements ICalendarModelCommand {

  private final String filePath;

  /**
   * Constructs an ExportCalendarCommand to export the calendar's events to the given file path.
   *
   * @param filePath the path where the calendar events will be exported
   */
  public ExportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Executes the command to export the calendar's events.
   * It retrieves the appropriate exporter from the factory and exports the events to the
   * specified file path.
   *
   * @param model the calendar model containing the events to export
   * @param view  the calendar view used to display success or error messages
   * @return true if the export is successful, false otherwise
   */
  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      IExporter exporter = ExporterFactory.getExporter(filePath);
      String outputPath = exporter.export(model.getEvents(), filePath);
      view.displayMessage("Calendar exported successfully to: " + outputPath);
      return true;
    } catch (IOException | IllegalArgumentException e) {
      view.displayError("Export failed: " + e.getMessage());
      return false;
    }
  }
}

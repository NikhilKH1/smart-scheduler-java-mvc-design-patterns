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

  public ExportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

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

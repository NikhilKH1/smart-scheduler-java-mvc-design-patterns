package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.utils.ExporterFactory;
import calendarapp.utils.IExporter;
import calendarapp.view.ICalendarView;

import java.io.IOException;

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
      view.displayMessage("Calendar exported successfully to: " + outputPath);
      return true;
    } catch (IOException | IllegalArgumentException e) {
      view.displayError("Export failed: " + e.getMessage());
      return false;
    }
  }
}

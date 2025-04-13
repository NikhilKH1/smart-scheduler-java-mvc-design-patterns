package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.utils.IImporter;
import calendarapp.utils.ImporterFactory;
import calendarapp.view.ICalendarView;

public class ImportCalendarCommand implements ICalendarModelCommand {

  private final String filePath;

  public ImportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean execute(ICalendarModel model, ICalendarView view) {
    try {
      IImporter importer = ImporterFactory.getImporter(filePath);
      importer.importInto(model, filePath);
      view.displayMessage("Calendar imported successfully from: " + filePath);
      return true;
    } catch (Exception e) {
      view.displayError("Import failed: " + e.getMessage());
      return false;
    }
  }
}

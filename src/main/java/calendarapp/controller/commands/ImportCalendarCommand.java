package calendarapp.controller.commands;

import calendarapp.model.ICalendarModel;
import calendarapp.utils.IImporter;
import calendarapp.utils.ImporterFactory;
import calendarapp.view.ICalendarView;

/**
 * Command class that handles importing calendar events from a file into the model.
 * It uses the ImporterFactory to determine the correct importer based on the file type.
 * If the import is successful, a success message is displayed through the view.
 * If an error occurs during import, an error message is displayed instead.
 */
public class ImportCalendarCommand implements ICalendarModelCommand {

  private final String filePath;

  /**
   * Creates a new ImportCalendarCommand with the specified file path.
   *
   * @param filePath the path of the file to import calendar events from
   */
  public ImportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Executes the import operation.
   * Retrieves the appropriate importer using the file path,
   * imports the events into the calendar model,
   * and displays the result to the user through the view.
   *
   * @param model the calendar model where events should be imported
   * @param view the view used to display success or error messages
   * @return true if import is successful, false otherwise
   */
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

package calendarapp.model.commands;

import calendarapp.model.CalendarModel;
import calendarapp.utils.CSVExporter;

import java.io.IOException;

/**
 * Command to export calendar events to a CSV file.
 * This command utilizes CSVExporter to write all calendar events from the model into a CSV file.
 */
public class ExportCalendarCommand implements Command {

  private final CalendarModel model;
  private final String fileName;

  /**
   * Constructs an ExportCalendarCommand with the specified calendar model and file name.
   *
   * @param model    the calendar model containing events to export
   * @param fileName the desired file name for the CSV export
   */
  public ExportCalendarCommand(CalendarModel model, String fileName) {
    this.model = model;
    this.fileName = fileName;
  }

  /**
   * Executes the export command by writing calendar events to a CSV file.
   * A success message with the absolute file path is printed upon successful export.
   * If an error occurs, an error message is printed.
   */
  public void execute() {
    try {
      String absolutePath = CSVExporter.exportToCSV(model.getEvents(), fileName);
      System.out.println("Calendar exported successfully to: " + absolutePath);
    } catch (IOException e) {
      System.err.println("Error exporting calendar: " + e.getMessage());
    }
  }

  /**
   * Returns the file name used for the CSV export.
   *
   * @return the CSV file name
   */
  public String getFileName() {
    return fileName;
  }
}

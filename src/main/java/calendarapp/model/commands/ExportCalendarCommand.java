package calendarapp.model.commands;

import calendarapp.model.CalendarModel;
import calendarapp.utils.CSVExporter;
import java.io.IOException;

public class ExportCalendarCommand implements Command {

  private final CalendarModel model;
  private final String fileName;

  public ExportCalendarCommand(CalendarModel model, String fileName) {
    this.model = model;
    this.fileName = fileName;
  }

  public void execute() {
    try {
      String absolutePath = CSVExporter.exportToCSV(model.getEvents(), fileName);
      System.out.println("Calendar exported successfully to: " + absolutePath);
    } catch (IOException e) {
      System.err.println("Error exporting calendar: " + e.getMessage());
    }
  }
  public String getFileName() {
    return fileName;
  }
}

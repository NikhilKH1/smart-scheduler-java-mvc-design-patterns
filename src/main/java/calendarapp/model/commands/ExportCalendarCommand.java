package calendarapp.model.commands;

public class ExportCalendarCommand implements Command {
  private final String filePath;

  public ExportCalendarCommand(String filePath) {
    this.filePath = filePath;
  }

  public String getFilePath() {
    return filePath;
  }
}

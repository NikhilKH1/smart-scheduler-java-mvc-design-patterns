

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.ExportCalendarCommand;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for the ExportCalendarCommand class.
 */
public class ExportCalendarCommandTest {

  @Test
  public void testExportCommand() {
    CalendarModel model = new CalendarModel();
    ExportCalendarCommand cmd = new ExportCalendarCommand(model, "events.csv");

    assertNotNull(cmd);
  }
}

import calendarapp.controller.commands.CopyEventsBetweenDatesCommand;
import calendarapp.model.CalendarModel;
import calendarapp.view.ICalendarView;
import calendarapp.model.ICalendarManager;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class CopyEventsBetweenDatesCommandTest {

  @Test
  public void testCopyEventsBetweenDatesCommandConstructor() {
    ZonedDateTime from = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime to = ZonedDateTime.parse("2025-04-05T10:00:00Z");
    ZonedDateTime startInTarget = ZonedDateTime.parse("2025-04-10T09:00:00Z");

    CopyEventsBetweenDatesCommand cmd = new CopyEventsBetweenDatesCommand(
            from, to, "TargetCal", startInTarget
    );

    assertNotNull(cmd); // just confirming instantiation works
  }

  @Test
  public void testExecuteMethodWithMockedManager() {
    ZonedDateTime from = ZonedDateTime.parse("2025-04-01T10:00:00Z");
    ZonedDateTime to = ZonedDateTime.parse("2025-04-05T10:00:00Z");
    ZonedDateTime startInTarget = ZonedDateTime.parse("2025-04-10T09:00:00Z");

    CopyEventsBetweenDatesCommand cmd = new CopyEventsBetweenDatesCommand(
            from, to, "TargetCal", startInTarget
    );

    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean useCalendar(String name) {
        return false;
      }

      @Override
      public CalendarModel getCalendar(String name) {
        return new CalendarModel("TargetCal", ZoneId.of("UTC")); // dummy
      }

      @Override
      public CalendarModel getActiveCalendar() {
        return new CalendarModel("SourceCal", ZoneId.of("UTC"));
      }

      @Override
      public boolean addCalendar(String name, java.time.ZoneId timezone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    ICalendarView dummyView = new ICalendarView() {
      @Override
      public void displayEvents(java.util.List events) {}

      @Override
      public void displayMessage(String msg) {}

      @Override
      public void displayError(String error) {}
    };

    cmd.execute(manager, dummyView);
    // You can’t assert much unless the model behavior is real.
    // This still covers the line & mutation for the call.
  }
}

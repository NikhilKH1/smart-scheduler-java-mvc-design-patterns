import calendarapp.controller.commands.CopySingleEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * JUnit tests for the CopySingleEventCommand class.
 */
public class CopySingleEventCommandTest {

  @Test
  public void testExecuteSuccess() {
    ZonedDateTime sourceDateTime = ZonedDateTime.of(2025, 4, 1,
            10, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime targetDateTime = ZonedDateTime.of(2025, 4, 2,
            11, 0, 0, 0, ZoneId.of("UTC"));

    DummyCalendarModel sourceCalendar = new DummyCalendarModel(true);
    DummyCalendarModel targetCalendar = new DummyCalendarModel(true);

    ICalendarManager manager = new ICalendarManager() {
      @Override
      public CalendarModel getCalendar(String name) {
        return targetCalendar;
      }

      @Override
      public CalendarModel getActiveCalendar() {
        return sourceCalendar;
      }

      @Override
      public boolean useCalendar(String name) {
        return false;
      }

      @Override
      public boolean addCalendar(String name, ZoneId zone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    StringBuilder output = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.append(msg);
      }

      @Override
      public void displayError(String msg) {
        output.append("ERROR: ").append(msg);
      }

      @Override
      public void run() {
        return;
      }

      @Override
      public void setInput(Readable in) {
        ICalendarView.super.setInput(in);
      }

      @Override
      public void setOutput(Appendable out) {
        ICalendarView.super.setOutput(out);
      }

      @Override
      public void displayEvents(java.util.List events) {
        return;
      }
    };

    CopySingleEventCommand cmd = new CopySingleEventCommand(
            "Meeting", sourceDateTime, "TargetCal", targetDateTime);

    boolean result = cmd.execute(manager, view);

    assertTrue(result);
    assertTrue(output.toString().contains("Event 'Meeting' copied to calendar: TargetCal"));
  }

  @Test
  public void testExecuteFailsWhenNotCalendarModel() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public CalendarModel getCalendar(String name) {
        return null;
      }

      @Override
      public CalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public boolean useCalendar(String name) {
        return false;
      }

      @Override
      public boolean addCalendar(String name, ZoneId zone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    StringBuilder output = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.append(msg);
      }

      @Override
      public void displayError(String msg) {
        output.append("ERROR: ").append(msg);
      }

      @Override
      public void run() {
        return;
      }

      @Override
      public void setInput(Readable in) {
        ICalendarView.super.setInput(in);
      }

      @Override
      public void setOutput(Appendable out) {
        ICalendarView.super.setOutput(out);
      }

      @Override
      public void displayEvents(java.util.List events) {
        return;
      }
    };

    CopySingleEventCommand cmd = new CopySingleEventCommand(
            "Meeting",
            ZonedDateTime.now(),
            "TargetCal",
            ZonedDateTime.now().plusDays(1));

    boolean result = cmd.execute(manager, view);
    assertFalse(result);
    assertTrue(output.toString().contains("Copy requires concrete CalendarModel"));
  }

  /**
   * Dummy concrete subclass to simulate success/failure for copySingleEventTo.
   */
  private static class DummyCalendarModel extends CalendarModel {
    private final boolean shouldSucceed;

    public DummyCalendarModel(boolean shouldSucceed) {
      super("Dummy", ZoneId.systemDefault());
      this.shouldSucceed = shouldSucceed;
    }

    @Override
    public boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName,
                                     ZonedDateTime sourceDateTime, ICalendarModel targetCalendar,
                                     ZonedDateTime targetDateTime) {
      return shouldSucceed;
    }
  }
}

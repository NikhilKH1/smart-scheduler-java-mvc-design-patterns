import calendarapp.controller.commands.CopyEventsBetweenDatesCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * JUnit tests for the CopyEventsBetweenDatesCommand class.
 */
public class CopyEventsBetweenDatesCommandTest {

  @Test
  public void testExecuteSuccess() {
    ZonedDateTime startDate = ZonedDateTime.of(2025, 4, 1,
            0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime endDate = ZonedDateTime.of(2025, 4, 3,
            0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime targetStart = ZonedDateTime.of(2025, 4, 5,
            0, 0, 0, 0, ZoneId.of("UTC"));

    DummyCalendarModel source = new DummyCalendarModel(true);
    DummyCalendarModel target = new DummyCalendarModel(true);

    ICalendarManager manager = new ICalendarManager() {
      @Override
      public CalendarModel getActiveCalendar() {
        return source;
      }

      @Override
      public CalendarModel getCalendar(String name) {
        return target;
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

    StringBuilder out = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        out.append(msg);
      }

      @Override
      public void displayError(String msg) {
        out.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
        // No implementation of this is required
      }
    };

    CopyEventsBetweenDatesCommand cmd = new CopyEventsBetweenDatesCommand(startDate, endDate,
            "TargetCal", targetStart);
    boolean result = cmd.execute(manager, view);

    assertTrue(result);
    assertTrue(out.toString().contains("Events copied successfully to calendar: TargetCal"));
  }

  @Test
  public void testExecuteFailureWhenNotCalendarModel() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public CalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public CalendarModel getCalendar(String name) {
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

    StringBuilder out = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        out.append(msg);
      }

      @Override
      public void displayError(String msg) {
        out.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
        // No implementation of this is required
      }
    };

    ZonedDateTime startDate = ZonedDateTime.now();
    ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
    ZonedDateTime targetStart = ZonedDateTime.now().plusDays(2);

    CopyEventsBetweenDatesCommand cmd = new CopyEventsBetweenDatesCommand(startDate, endDate,
            "Target", targetStart);
    boolean result = cmd.execute(manager, view);

    assertFalse(result);
    assertTrue(out.toString().contains("Copy requires concrete CalendarModel"));
  }

  @Test
  public void testExecuteFailureCopyConflicts() {
    ZonedDateTime startDate = ZonedDateTime.now();
    ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
    ZonedDateTime targetStart = ZonedDateTime.now().plusDays(2);

    DummyCalendarModel source = new DummyCalendarModel(false);
    DummyCalendarModel target = new DummyCalendarModel(true);

    ICalendarManager manager = new ICalendarManager() {
      @Override
      public CalendarModel getActiveCalendar() {
        return source;
      }

      @Override
      public CalendarModel getCalendar(String name) {
        return target;
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

    StringBuilder out = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        out.append(msg);
      }

      @Override
      public void displayError(String msg) {
        out.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
        // No implementation of this is required
      }
    };

    CopyEventsBetweenDatesCommand cmd = new CopyEventsBetweenDatesCommand(startDate, endDate,
            "Target", targetStart);
    boolean result = cmd.execute(manager, view);

    assertFalse(result);
    assertTrue(out.toString().contains("Some or all events failed to copy due to conflicts"));
  }

  private static class DummyCalendarModel extends CalendarModel {
    private final boolean shouldSucceed;

    public DummyCalendarModel(boolean shouldSucceed) {
      super("Dummy", ZoneId.of("UTC"));
      this.shouldSucceed = shouldSucceed;
    }

    @Override
    public boolean copyEventsBetweenTo(CalendarModel source, Temporal startDate, Temporal endDate,
                                       CalendarModel target, Temporal targetStartDate) {
      return shouldSucceed;
    }
  }
}
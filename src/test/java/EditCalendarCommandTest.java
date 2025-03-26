import calendarapp.controller.commands.EditCalendarCommand;
import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * JUnit tests for the EditCalendarCommand class.
 */
public class EditCalendarCommandTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnNullCalendarName() {
    new EditCalendarCommand(null, "timezone", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnEmptyCalendarName() {
    new EditCalendarCommand("  ", "timezone", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnNullProperty() {
    new EditCalendarCommand("MyCal", null, "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnEmptyProperty() {
    new EditCalendarCommand("MyCal", " ", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnNullNewValue() {
    new EditCalendarCommand("MyCal", "timezone", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnEmptyNewValue() {
    new EditCalendarCommand("MyCal", "timezone", " ");
  }

  @Test
  public void testExecuteSuccess() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return true;
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
      public calendarapp.model.ICalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public calendarapp.model.ICalendarModel getCalendar(String name) {
        return null;
      }
    };

    StringBuilder log = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        log.append(msg);
      }

      @Override
      public void displayError(String msg) {
        log.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
      }
    };

    EditCalendarCommand cmd = new EditCalendarCommand("MyCal",
            "timezone", "UTC");
    boolean result = cmd.execute(manager, view);

    assertTrue(result);
    assertTrue(log.toString().contains("Calendar updated: MyCal"));
  }

  @Test
  public void testExecuteFailureReturnsFalse() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
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
      public calendarapp.model.ICalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public calendarapp.model.ICalendarModel getCalendar(String name) {
        return null;
      }
    };

    StringBuilder log = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        log.append(msg);
      }

      @Override
      public void displayError(String msg) {
        log.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
      }
    };

    EditCalendarCommand cmd = new EditCalendarCommand("MyCal",
            "timezone", "UTC");
    boolean result = cmd.execute(manager, view);

    assertFalse(result);
    assertFalse(log.toString().contains("Calendar updated"));
  }

  @Test
  public void testExecuteHandlesException() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        throw new RuntimeException("Simulated failure");
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
      public calendarapp.model.ICalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public calendarapp.model.ICalendarModel getCalendar(String name) {
        return null;
      }
    };

    StringBuilder log = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        log.append(msg);
      }

      @Override
      public void displayError(String msg) {
        log.append("ERROR: ").append(msg);
      }

      @Override
      public void displayEvents(java.util.List events) {
      }
    };

    EditCalendarCommand cmd = new EditCalendarCommand("MyCal",
            "timezone", "UTC");
    boolean result = cmd.execute(manager, view);

    assertFalse(result);
    assertTrue(log.toString().contains("ERROR: Edit calendar failed: Simulated failure"));
  }
}

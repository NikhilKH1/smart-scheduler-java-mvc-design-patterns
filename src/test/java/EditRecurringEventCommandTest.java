import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the EditRecurringEventCommand class.
 */
public class EditRecurringEventCommandTest {

  private class TestCalendarModel implements ICalendarModel {
    @Override
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return true;
    }

    @Override
    public boolean editSingleEvent(String property, String eventName, Temporal originalStart,
                                   Temporal originalEnd, String newValue) {
      return false;
    }

    @Override
    public boolean editEventsFrom(String property, String eventName, Temporal fromDateTime,
                                  String newValue) {
      return false;
    }

    @Override
    public boolean editEventsAll(String property, String eventName, String newValue) {
      return false;
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public boolean copyEventsBetweenTo(CalendarModel source, Temporal startDate, Temporal endDate,
                                       CalendarModel target, Temporal targetStartDate) {
      return true;
    }

    @Override
    public boolean copyEventsOnDateTo(CalendarModel source, Temporal sourceDate,
                                      CalendarModel target, Temporal targetDate) {
      return true;
    }

    @Override
    public boolean copySingleEventTo(CalendarModel source, String eventName,
                                     Temporal sourceDateTime, CalendarModel target,
                                     Temporal targetDateTime) {
      return true;
    }

    @Override
    public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public java.util.List<ICalendarEvent> getEvents() {
      return null;
    }

    @Override
    public List<ICalendarEvent> getEventsOnDate(Temporal date) {
      return List.of();
    }

    @Override
    public List<ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
      return List.of();
    }

    @Override
    public boolean isBusyAt(Temporal dateTime) {
      return false;
    }

    @Override
    public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
      return false;
    }

    @Override
    public ZoneId getTimezone() {
      return ZoneId.of("UTC");
    }

    @Override
    public void updateTimezone(ZoneId newTimezone) {
      // No implementation of this is required
    }
  }

  private class TestCalendarViewImpl implements ICalendarView {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream ps = new PrintStream(outputStream);

    @Override
    public void displayMessage(String message) {
      ps.println(message);
    }

    @Override
    public void displayError(String errorMessage) {
      ps.println(errorMessage);
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      // No implementation of this is required
    }

    public String getOutput() {
      return outputStream.toString();
    }
  }

  @Test
  public void testEditRecurringEventCommandSuccess() {
    ICalendarModel model = new TestCalendarModel();
    TestCalendarViewImpl view = new TestCalendarViewImpl();
    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil",
            "Daily Standup", "2025-12-31T00:00");
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    String output = view.getOutput();
    assertTrue(output.contains("Recurring event modified successfully."));
  }

  @Test
  public void testEditRecurringEventCommandWithError() {
    ICalendarModel model = new TestCalendarModel() {
      @Override
      public boolean editRecurringEvent(String eventName, String property, String newValue) {
        return false;
      }
    };
    TestCalendarViewImpl view = new TestCalendarViewImpl();
    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil",
            "Daily Standup", "2025-12-31T00:00");
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    String output = view.getOutput();
    assertTrue(output.contains("Failed to modify recurring event."));
  }
}

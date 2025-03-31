import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

  @Test
  public void testConvertToZonedDateTimeCoversAllPaths() throws Exception {
    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil", "Test", "2025-12-31T00:00");

    Method method = EditRecurringEventCommand.class.getDeclaredMethod(
            "convertToZonedDateTime", Temporal.class, ZoneId.class);
    method.setAccessible(true);

    ZoneId zone = ZoneId.of("UTC");

    // Case 1: Input is ZonedDateTime (covers line 86)
    ZonedDateTime inputZoned = ZonedDateTime.now(zone);
    Object result1 = method.invoke(cmd, inputZoned, zone);
    assertEquals(inputZoned, result1);  // Should return the same instance

    // Case 2: Input is LocalDateTime (covers line 89)
    LocalDateTime inputLocal = LocalDateTime.of(2025, 1, 1, 10, 0);
    Object result2 = method.invoke(cmd, inputLocal, zone);
    assertNotNull(result2);
    assertTrue(result2 instanceof ZonedDateTime);
    ZonedDateTime expected = ZonedDateTime.of(inputLocal, zone);
    assertEquals(expected, result2);
  }

  @Test
  public void testEditRecurringEventCommandThrowsException() {
    // Simulate a model that throws an exception during editRecurringEvent
    ICalendarModel model = new TestCalendarModel() {
      @Override
      public boolean editRecurringEvent(String eventName, String property, String newValue) {
        throw new RuntimeException("Simulated failure");
      }
    };

    TestCalendarViewImpl view = new TestCalendarViewImpl();

    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil",
            "SomeEvent", "2025-12-31T00:00");

    boolean result = cmd.execute(model, view);

    // Assert fallback path
    assertFalse(result); // ✅ line 68
    assertTrue(view.getOutput().contains("Execution Error:")); // ✅ line 67
  }

}

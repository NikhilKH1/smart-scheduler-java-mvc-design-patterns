import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private ZonedDateTime busyTime;
    private ZoneId zone = ZoneId.of("UTC");

    public void setBusyTime(ZonedDateTime busyTime) {
      this.busyTime = busyTime;
    }

    @Override
    public boolean isBusyAt(ZonedDateTime dateTime) {
      ZonedDateTime query = ZonedDateTime.from(dateTime);
      return query.equals(busyTime);
    }

    @Override
    public ZoneId getTimezone() {
      return zone;
    }

    /**
     * Updates the calendar's timezone and adjusts all events accordingly.
     *
     * @param newTimezone the new timezone to apply to the calendar
     */
    @Override
    public void updateTimezone(ZoneId newTimezone) {
      return;
    }

    @Override
    public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(calendarapp.model.event.RecurringEvent event,
                                     boolean autoDecline) {
      return false;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEvents() {
      return null;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      return null;
    }

    @Override
    public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
      return false;
    }

    @Override
    public boolean editSingleEvent(String property, String eventName,
                                   ZonedDateTime originalStart, ZonedDateTime originalEnd,
                                   String newValue) {
      return false;
    }

    @Override
    public boolean editEventsFrom(String property, String eventName, ZonedDateTime fromDateTime,
                                  String newValue) {
      return false;
    }

    @Override
    public boolean editEventsAll(String property, String eventName, String newValue) {
      return false;
    }

    private boolean editRecurringEventCalled = false;

    @Override
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      if ("Daily Standup".equals(eventName) && "repeatuntil".equals(property)) {
        editRecurringEventCalled = true;
        return true;
      }
      return false;
    }


    @Override
    public String getName() {
      return null;
    }

    @Override
    public boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName,
                                     ZonedDateTime sourceDateTime, ICalendarModel targetCalendar,
                                     ZonedDateTime targetDateTime) {
      return false;
    }

    @Override
    public boolean copyEventsOnDateTo(ICalendarModel sourceCalendar, ZonedDateTime sourceDate,
                                      ICalendarModel targetCalendar, ZonedDateTime targetDate) {
      return false;
    }

    @Override
    public boolean copyEventsBetweenTo(ICalendarModel sourceCalendar, ZonedDateTime startDate,
                                       ZonedDateTime endDate, ICalendarModel targetCalendar,
                                       ZonedDateTime targetStartDate) {
      return false;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getReadOnlyEventsOnDate(LocalDate date) {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
      return List.of();
    }
  }

  private class TestCalendarViewImpl implements ICalendarView {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream ps = new PrintStream(outputStream);

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      return;
    }

    @Override
    public void displayMessage(String message) {
      ps.println(message);
    }

    @Override
    public void displayError(String errorMessage) {
      ps.println(errorMessage);
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

    public String getOutput() {
      return outputStream.toString();
    }
  }

  @Test
  public void testEditRecurringEventCommandSuccess() {
    ICalendarModel model = new TestCalendarModel();
    TestCalendarViewImpl view = new TestCalendarViewImpl();

    ZonedDateTime repeatUntil = ZonedDateTime.parse("2025-12-31T00:00Z");

    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil",
            "Daily Standup", repeatUntil);

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
  public void testEditRecurringEventCommandThrowsException() {
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

    assertFalse(result);
    assertTrue(view.getOutput().contains("Execution Error:"));
  }

}

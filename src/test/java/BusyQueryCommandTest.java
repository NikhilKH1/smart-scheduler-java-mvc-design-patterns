import calendarapp.controller.commands.BusyQueryCommand;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for BusyQueryCommand class.
 */
public class BusyQueryCommandTest {

  private TestCalendarModel model;
  private TestCalendarView view;

  @Before
  public void setup() {
    model = new TestCalendarModel();
    view = new TestCalendarView();
  }

  @Test
  public void testExecuteWhenBusy() {
    ZonedDateTime queryTime = ZonedDateTime.of(2025, 5,
            5, 10, 0, 0, 0,
            ZoneId.of("UTC"));
    model.setBusyTime(queryTime);

    BusyQueryCommand cmd = new BusyQueryCommand(queryTime);
    boolean result = cmd.execute(model, view);

    assertTrue(result);
    assertEquals("Busy at 2025-05-05T10:00", view.getLastMessage());
  }

  @Test
  public void testExecuteWhenAvailable() {
    ZonedDateTime queryTime = ZonedDateTime.of(2025, 5,
            5, 14, 0, 0, 0,
            ZoneId.of("UTC"));
    model.setBusyTime(ZonedDateTime.of(2025, 5, 5,
            10, 0, 0, 0, ZoneId.of("UTC")));

    BusyQueryCommand cmd = new BusyQueryCommand(queryTime);
    boolean result = cmd.execute(model, view);

    assertTrue(result);
    assertEquals("Available at 2025-05-05T14:00", view.getLastMessage());
  }

  private static class TestCalendarModel implements ICalendarModel {
    private ZonedDateTime busyTime;
    private ZoneId zone = ZoneId.of("UTC");

    public void setBusyTime(ZonedDateTime busyTime) {
      this.busyTime = busyTime;
    }

    @Override
    public boolean isBusyAt(Temporal dateTime) {
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
      // No implementation of this is required
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
    public List<ICalendarEvent> getEvents() {
      return null;
    }

    @Override
    public List<ICalendarEvent> getEventsOnDate(Temporal date) {
      return null;
    }

    @Override
    public List<ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
      return null;
    }

    @Override
    public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
      return false;
    }

    @Override
    public boolean editSingleEvent(String property, String eventName,
                                   Temporal originalStart, Temporal originalEnd,
                                   String newValue) {
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
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return false;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public boolean copySingleEventTo(calendarapp.model.CalendarModel source, String eventName,
                                     Temporal sourceDateTime, calendarapp.model.
                                             CalendarModel target, Temporal targetDateTime) {
      return false;
    }

    @Override
    public boolean copyEventsOnDateTo(calendarapp.model.CalendarModel source, Temporal sourceDate,
                                      calendarapp.model.CalendarModel target, Temporal targetDate) {
      return false;
    }

    @Override
    public boolean copyEventsBetweenTo(calendarapp.model.CalendarModel source,
                                       Temporal startDate, Temporal endDate,
                                       calendarapp.model.CalendarModel target,
                                       Temporal targetStartDate) {
      return false;
    }
  }

  private static class TestCalendarView implements ICalendarView {
    private String lastMessage = null;
    private String lastError = null;

    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      // No implementation of this is required
    }

    @Override
    public void displayError(String errorMessage) {
      this.lastError = errorMessage;
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public String getLastError() {
      return lastError;
    }
  }
}
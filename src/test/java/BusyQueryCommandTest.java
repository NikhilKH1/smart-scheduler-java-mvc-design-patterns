//import calendarapp.controller.commands.BusyQueryCommand;
//import calendarapp.model.ICalendarModel;
//import calendarapp.view.ICalendarView;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.temporal.Temporal;
//
//import static org.junit.Assert.*;
//
//public class BusyQueryCommandTest {
//
//  private TestCalendarModel model;
//  private TestCalendarView view;
//
//  @Before
//  public void setup() {
//    model = new TestCalendarModel();
//    view = new TestCalendarView();
//  }
//
//  @Test
//  public void testExecuteWhenBusy() {
//    ZonedDateTime queryTime = ZonedDateTime.of(2025, 5, 5, 10, 0, 0, 0, ZoneId.of("UTC"));
//    model.setBusyTime(queryTime);
//
//    BusyQueryCommand cmd = new BusyQueryCommand(queryTime);
//    boolean result = cmd.execute(model, view);
//
//    assertTrue(result);
//    assertEquals("Busy at " + queryTime, view.getLastMessage());
//  }
//
//  @Test
//  public void testExecuteWhenAvailable() {
//    ZonedDateTime queryTime = ZonedDateTime.of(2025, 5, 5, 14, 0, 0, 0, ZoneId.of("UTC"));
//    model.setBusyTime(ZonedDateTime.of(2025, 5, 5, 10, 0, 0, 0, ZoneId.of("UTC")));  // Different time
//
//    BusyQueryCommand cmd = new BusyQueryCommand(queryTime);
//    boolean result = cmd.execute(model, view);
//
//    assertTrue(result);
//    assertEquals("Available at " + queryTime, view.getLastMessage());
//  }
//
//  @Test
//  public void testExecuteWithNonZonedTemporal() {
//    Temporal temporal = ZonedDateTime.of(2025, 5, 5, 10, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
//            .toLocalDateTime();  // Non-zoned temporal
//    ZonedDateTime expected = ZonedDateTime.of(2025, 5, 5, 10, 0, 0, 0, ZoneId.of("UTC"));
//    model.setBusyTime(expected);
//
//    BusyQueryCommand cmd = new BusyQueryCommand(temporal);
//    boolean result = cmd.execute(model, view);
//
//    assertTrue(result);
//    assertEquals("Busy at " + expected, view.getLastMessage());
//  }
//
//  // Manual mock of ICalendarModel
//  private static class TestCalendarModel implements ICalendarModel {
//    private ZonedDateTime busyTime;
//    private ZoneId zone = ZoneId.of("UTC");
//
//    public void setBusyTime(ZonedDateTime busyTime) {
//      this.busyTime = busyTime;
//    }
//
//    @Override
//    public boolean isBusyAt(Temporal dateTime) {
//      ZonedDateTime query = ZonedDateTime.from(dateTime);
//      return query.equals(busyTime);
//    }
//
//    @Override
//    public ZoneId getTimezone() {
//      return zone;
//    }
//
//    // --- unused methods stubbed out ---
//    @Override
//    public boolean addEvent(calendarapp.model.event.ICalendarEvent event, boolean autoDecline) {
//      return false;
//    }
//
//    @Override
//    public boolean addRecurringEvent(calendarapp.model.event.RecurringEvent event, boolean autoDecline) {
//      return false;
//    }
//
//    @Override
//    public java.util.List<calendarapp.model.event.ICalendarEvent> getEvents() {
//      return null;
//    }
//
//    @Override
//    public java.util.List<calendarapp.model.event.ICalendarEvent> getEventsOnDate(Temporal date) {
//      return null;
//    }
//
//    @Override
//    public java.util.List<calendarapp.model.event.ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
//      return null;
//    }
//
//    @Override
//    public boolean editEvent(calendarapp.model.event.ICalendarEvent oldEvent, calendarapp.model.event.ICalendarEvent newEvent) {
//      return false;
//    }
//
//    @Override
//    public boolean editSingleEvent(String property, String eventName, Temporal originalStart, Temporal originalEnd, String newValue) {
//      return false;
//    }
//
//    @Override
//    public boolean editEventsFrom(String property, String eventName, Temporal fromDateTime, String newValue) {
//      return false;
//    }
//
//    @Override
//    public boolean editEventsAll(String property, String eventName, String newValue) {
//      return false;
//    }
//
//    @Override
//    public boolean editRecurringEvent(String eventName, String property, String newValue) {
//      return false;
//    }
//
//    @Override
//    public String getName() {
//      return null;
//    }
//  }
//
//  // Manual mock of ICalendarView
//  private static class TestCalendarView implements ICalendarView {
//    private String lastMessage = null;
//    private String lastError = null;
//
//    @Override
//    public void displayMessage(String message) {
//      this.lastMessage = message;
//    }
//
//    @Override
//    public void displayEvents(java.util.List<calendarapp.model.event.ICalendarEvent> events) {
//      // no-op for test
//    }
//
//
//    @Override
//    public void displayError(String errorMessage) {
//      this.lastError = errorMessage;
//    }
//
//    public String getLastMessage() {
//      return lastMessage;
//    }
//
//    public String getLastError() {
//      return lastError;
//    }
//  }
//
//}
//

import calendarapp.controller.commands.QueryRangeDateTimeCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for QueryRangeDateTimeCommand Class.
 */
public class QueryRangeDateTimeCommandTest {

  private class TestCalendarModel implements ICalendarModel {
    private List<ICalendarEvent> events;

    public TestCalendarModel(List<ICalendarEvent> events) {
      this.events = events;
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
    public List<ICalendarEvent> getEvents() {
      return List.of();
    }

    @Override
    public List<ICalendarEvent> getEventsOnDate(Temporal date) {
      return List.of();
    }

    @Override
    public List<ICalendarEvent> getEventsBetween(java.time.temporal.Temporal start,
                                                 java.time.temporal.Temporal end) {
      return events;
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
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return false;
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
    public ZoneId getTimezone() {
      return null;
    }

    @Override
    public void updateTimezone(ZoneId newTimezone) {
      // No implementation of this is required
    }

    @Override
    public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName,
                                     Temporal sourceDateTime, CalendarModel targetCalendar,
                                     Temporal targetDateTime) {
      return false;
    }

    @Override
    public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, Temporal sourceDate,
                                      CalendarModel targetCalendar, Temporal targetDate) {
      return false;
    }

    @Override
    public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, Temporal startDate,
                                       Temporal endDate, CalendarModel targetCalendar,
                                       Temporal targetStartDate) {
      return false;
    }
  }

  private class TestCalendarView implements ICalendarView {
    private String message;
    private List<ICalendarEvent> displayedEvents;

    @Override
    public void displayMessage(String message) {
      this.message = message;
    }

    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      this.displayedEvents = events;
    }

    @Override
    public void displayError(String errorMessage) {
      // No implementation of this is required
    }

    public String getMessage() {
      return message;
    }

    public List<ICalendarEvent> getDisplayedEvents() {
      return displayedEvents;
    }
  }

  private class DummyCalendarEvent implements ICalendarEvent {
    private String subject;

    public DummyCalendarEvent(String subject) {
      this.subject = subject;
    }

    @Override
    public String getSubject() {
      return subject;
    }

    @Override
    public Temporal getStartDateTime() {
      return null;
    }

    @Override
    public Temporal getEndDateTime() {
      return null;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public String getLocation() {
      return "";
    }

    @Override
    public boolean isAllDay() {
      return false;
    }

    @Override
    public boolean isPublic() {
      return false;
    }
  }

  @Test
  public void testQueryRangeNoEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 11, 0);
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);
    List<ICalendarEvent> emptyList = new ArrayList<>();
    TestCalendarModel model = new TestCalendarModel(emptyList);
    TestCalendarView view = new TestCalendarView();
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    String expected = "No events found from " + start + " to " + end;
    assertEquals(expected, view.getMessage());
    assertNull(view.getDisplayedEvents());
  }

  @Test
  public void testQueryRangeWithEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 11, 0);
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);
    List<ICalendarEvent> events = new ArrayList<>();
    DummyCalendarEvent event = new DummyCalendarEvent("Meeting");
    events.add(event);
    TestCalendarModel model = new TestCalendarModel(events);
    TestCalendarView view = new TestCalendarView();
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    String expected = "Events from " + start + " to " + end + ":";
    assertEquals(expected, view.getMessage());
    List<ICalendarEvent> displayed = view.getDisplayedEvents();
    assertNotNull(displayed);
    assertEquals(1, displayed.size());
    assertEquals("Meeting", displayed.get(0).getSubject());
  }

  @Test
  public void testGetters() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 11, 0);
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);
    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
  }
}

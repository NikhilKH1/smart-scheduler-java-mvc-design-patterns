import calendarapp.controller.commands.QueryRangeDateTimeCommand;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for QueryRangeDateTimeCommand Class.
 */
public class QueryRangeDateTimeCommandTest {

  private class TestCalendarModel implements ICalendarModel {
    private List<ReadOnlyCalendarEvent> events;

    public TestCalendarModel(List<ReadOnlyCalendarEvent> events) {
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
    public List<ReadOnlyCalendarEvent> getEvents() {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
      return new ArrayList<>();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      List<ReadOnlyCalendarEvent> result = new ArrayList<>();
      for (ReadOnlyCalendarEvent event : events) {
        if (event.getStartDateTime().isAfter(start) && event.getEndDateTime().isBefore(end)) {
          result.add((ReadOnlyCalendarEvent) event);
        }
      }
      return result;
    }

    @Override
    public boolean isBusyAt(ZonedDateTime dateTime) {
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
      return;
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
      return events.stream()
              .filter(event -> event.getStartDateTime().
                      toLocalDate().equals(date))
              .collect(Collectors.toList());
    }

    @Override
    public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
      return List.of();
    }
  }


  private class TestCalendarView implements ICalendarView {
    private String message;
    private List<ReadOnlyCalendarEvent> displayedEvents;

    @Override
    public void displayMessage(String message) {
      this.message = message;
    }

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      this.displayedEvents = new ArrayList<>(events);
    }

    @Override
    public void displayError(String errorMessage) {
      return;
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

    public String getMessage() {
      return message;
    }

    public List<ReadOnlyCalendarEvent> getDisplayedEvents() {
      return displayedEvents;
    }
  }

  private class DummyCalendarEvent implements ICalendarEvent {
    private String subject;
    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;

    public DummyCalendarEvent(String subject, ZonedDateTime start, ZonedDateTime end) {
      this.subject = subject;
      this.startDateTime = start;
      this.endDateTime = end;
    }

    @Override
    public String getSubject() {
      return subject;
    }

    @Override
    public ZonedDateTime getStartDateTime() {
      return startDateTime;
    }

    @Override
    public ZonedDateTime getEndDateTime() {
      return endDateTime;
    }

    @Override
    public boolean isRecurring() {
      return false;
    }

    @Override
    public String getWeekdays() {
      return "";
    }

    @Override
    public ZonedDateTime repeatUntil() {
      return null;
    }

    @Override
    public Integer getRepeatCount() {
      return 0;
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

    @Override
    public ICalendarEvent withUpdatedProperty(String property, String newValue) {
      return null;
    }
  }


  @Test
  public void testQueryRangeNoEvents() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1,
            11, 0, 0, 0, ZoneId.of("America/New_York"));
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);
    List<ReadOnlyCalendarEvent> emptyList = new ArrayList<>();
    TestCalendarModel model = new TestCalendarModel(emptyList);
    TestCalendarView view = new TestCalendarView();
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    String expected = "No events found from " + start + " to " + end;
    assertEquals(expected, view.getMessage());
    assertNull(view.getDisplayedEvents());
  }


  @Test
  public void testGetters() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.of("Australia/Sydney"));
    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 11,
            0, 0, 0, ZoneId.of("Australia/Sydney"));
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);
    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
  }

}

import calendarapp.controller.commands.QueryByDateCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test class for QueryByDateCommand Class.
 */
public class QueryByDateCommandTest {

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
      List<ReadOnlyCalendarEvent> eventsOnDate = new ArrayList<>();
      for (ReadOnlyCalendarEvent event : events) {
        ZonedDateTime start = event.getStartDateTime();
        if (start != null && start.toLocalDate().equals(date)) {
          eventsOnDate.add(event);
        }
      }
      return eventsOnDate;
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      return List.of();
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
      // No implementation of this is required
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
              .filter(event -> event.getStartDateTime().toLocalDate().equals(date))
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
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      this.displayedEvents = new ArrayList<>(events); // Store the events here
    }

    @Override
    public void displayMessage(String message) {
      this.message = message;
    }

    @Override
    public void displayError(String errorMessage) {
      // No implementation of this is required
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

    public DummyCalendarEvent(String subject, ZonedDateTime startDateTime) {
      this.subject = subject;
      this.startDateTime = startDateTime;
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
      return null;
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
    public ZonedDateTime RepeatUntil() {
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
    public ICalendarEvent withUpdatedProperty(String property, String newValue){
      return null;
    }
  }

  @Test
  public void testQueryByDateNoEvents() {
    LocalDate date = LocalDate.of(2025, 6, 1);
    QueryByDateCommand cmd = new QueryByDateCommand(date);
    List<ReadOnlyCalendarEvent> emptyEvents = new ArrayList<>();
    TestCalendarModel model = new TestCalendarModel(emptyEvents);
    TestCalendarView view = new TestCalendarView();
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    assertEquals("No events found on " + date, view.getMessage());
    assertNull(view.getDisplayedEvents());
  }

  @Test
  public void testQueryByDateWithEvents() {
    LocalDate date = LocalDate.of(2025, 6, 1);
    ZonedDateTime eventStart = date.atStartOfDay(ZoneId.systemDefault());
    QueryByDateCommand cmd = new QueryByDateCommand(date);
    List<ReadOnlyCalendarEvent> events = new ArrayList<>();
    DummyCalendarEvent event = new DummyCalendarEvent("Meeting", eventStart);
    events.add(event);
    TestCalendarModel model = new TestCalendarModel(events);
    TestCalendarView view = new TestCalendarView();
    boolean result = cmd.execute(model, view);
    assertTrue(result);
    assertEquals("Events on " + date + ":", view.getMessage());
    assertNotNull(view.getDisplayedEvents());
    assertEquals(1, view.getDisplayedEvents().size());
    assertEquals("Meeting", view.getDisplayedEvents().get(0).getSubject());
  }

  @Test
  public void testGetQueryDate() {
    LocalDate date = LocalDate.of(2025, 6, 1);
    QueryByDateCommand cmd = new QueryByDateCommand(date);
    assertEquals(date, cmd.getQueryDate());
  }
}

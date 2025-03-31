import calendarapp.controller.commands.EditEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;

import static org.junit.Assert.*;

public class EditEventCommandTest {

  private static class DummyModel implements ICalendarModel {
    private final ZoneId zone = ZoneId.of("UTC");

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
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return false;
    }

    @Override public boolean editSingleEvent(String prop, String name, Temporal start,
                                             Temporal end, String value) {
      throw new RuntimeException("Simulated failure");
    }

    @Override public boolean editEventsFrom(String prop, String name, Temporal from, String value)
    {
      throw new RuntimeException("Simulated failure");
    }

    @Override public boolean editEventsAll(String prop, String name, String value) {
      throw new RuntimeException("Simulated failure");
    }

    @Override
    public String getName() {
      return "";
    }

    @Override public ZoneId getTimezone() {
      return zone;
    }

    @Override
    public void updateTimezone(ZoneId newTimezone) {

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

    // Stub other methods if needed
  }

  private static class DummyView implements ICalendarView {
    public String lastError = null;

    @Override
    public void displayEvents(List<ICalendarEvent> events) {

    }

    @Override public void displayMessage(String msg) {}
    @Override public void displayError(String msg) {
      lastError = msg;
    }
  }

  @Test
  public void testExecuteSingleHandlesException() {
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();
    ZonedDateTime now = ZonedDateTime.now();

    EditEventCommand cmd = new EditEventCommand("description",
            "Event1", now, now.plusHours(1), "New Desc");
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertNotNull(view.lastError);
    assertTrue(view.lastError.contains("Error while editing event"));
  }

  @Test
  public void testExecuteFromHandlesException() {
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();
    ZonedDateTime filter = ZonedDateTime.now();

    EditEventCommand cmd = new EditEventCommand("location", "Event2",
            filter, "New Location");
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertTrue(view.lastError.contains("Error while editing event"));
  }

  @Test
  public void testExecuteAllHandlesException() {
    DummyModel model = new DummyModel();
    DummyView view = new DummyView();

    EditEventCommand cmd = new EditEventCommand("subject", "Event3",
            "New Subject");
    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertTrue(view.lastError.contains("Error while editing event"));
  }

  @Test(expected = InvocationTargetException.class)
  public void testConvertToZonedDateTimeUnsupportedType() throws Exception {
    EditEventCommand cmd = new EditEventCommand("desc", "Event", "Val");

    // Get the private method and make it accessible
    var method = EditEventCommand.class
            .getDeclaredMethod("convertToZonedDateTime", Temporal.class, ZoneId.class);
    method.setAccessible(true); // <- This is what you were missing

    // Call with unsupported type: Instant
    method.invoke(cmd, java.time.Instant.now(), ZoneId.of("UTC"));
  }

    @Test
  public void testEditEventCommandProperties() {
    EditEventCommand cmd = new EditEventCommand("location", "Team Meeting",
            "New Room");

    assertEquals("location", cmd.getProperty());
    assertEquals("Team Meeting", cmd.getEventName());
    assertEquals("New Room", cmd.getNewValue());
  }

  @Test
  public void testGetModeAndFilterDateTime() {
    ZonedDateTime dt = ZonedDateTime.now();
    EditEventCommand cmd = new EditEventCommand("desc", "name", dt,
            "new");
    assertEquals(EditEventCommand.EditMode.FROM, cmd.getMode());
    assertEquals(dt, cmd.getFilterDateTime());
  }
}

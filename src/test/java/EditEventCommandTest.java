import calendarapp.controller.commands.EditEventCommand;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test file for Edit Event Command.
 */
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
    public List<ReadOnlyCalendarEvent> getEvents() {
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
      return List.of();
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
      throw new RuntimeException("Simulated failure");
    }

    @Override
    public boolean editEventsFrom(String property, String eventName,
                                  ZonedDateTime fromDateTime,
                                  String newValue) {
      throw new RuntimeException("Simulated failure");
    }

    @Override
    public boolean editEventsAll(String prop, String name, String value) {
      throw new RuntimeException("Simulated failure");
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public ZoneId getTimezone() {
      return zone;
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
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
      return List.of();
    }

  }

  private static class DummyView implements ICalendarView {
    public String lastError = null;

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      return;
    }

    @Override
    public void displayMessage(String msg) {
      return;
    }

    @Override
    public void displayError(String msg) {
      lastError = msg;
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

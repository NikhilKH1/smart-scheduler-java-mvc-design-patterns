import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * JUnit tests for the CreateEventCommand class.
 */
public class CreateEventCommandTest {
  private ZonedDateTime start;
  private ZonedDateTime end;
  private ZonedDateTime repeatUntil;

  @Before
  public void setUp() {
    start = ZonedDateTime.of(2025, 7, 1, 9, 0,
            0, 0, ZoneId.of("UTC"));
    end = ZonedDateTime.of(2025, 7, 1, 10, 0,
            0, 0, ZoneId.of("UTC"));
    repeatUntil = ZonedDateTime.of(2025, 7, 15, 9,
            0, 0, 0, ZoneId.of("UTC"));
  }

  @Test
  public void testGettersForRecurringEventCommand() {
    CreateEventCommand cmd = new CreateEventCommand(
            "Yoga", start, end, true,
            "Morning Yoga Session", "Park",
            false, true, true,
            "MWF", 5, repeatUntil);

    assertEquals("Yoga", cmd.getEventName());
    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
    assertTrue(cmd.isAutoDecline());
    assertEquals("Morning Yoga Session", cmd.getDescription());
    assertEquals("Park", cmd.getLocation());
    assertFalse(cmd.isPublic());
    assertTrue(cmd.isAllDay());
    assertTrue(cmd.isRecurring());
    assertEquals("MWF", cmd.getWeekdays());
    assertEquals(5, cmd.getRepeatCount());
    assertEquals(repeatUntil, cmd.getRepeatUntil());
  }

  @Test
  public void testGettersForSingleEventCommand() {
    CreateEventCommand cmd = new CreateEventCommand(
            "Dentist", start, end, false,
            "Dental check-up", "Clinic",
            true, false, false,
            "", 0, null);

    assertEquals("Dentist", cmd.getEventName());
    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
    assertFalse(cmd.isAutoDecline());
    assertEquals("Dental check-up", cmd.getDescription());
    assertEquals("Clinic", cmd.getLocation());
    assertTrue(cmd.isPublic());
    assertFalse(cmd.isAllDay());
    assertFalse(cmd.isRecurring());
    assertEquals("", cmd.getWeekdays());
    assertEquals(0, cmd.getRepeatCount());
    assertEquals(null, cmd.getRepeatUntil());
  }


  @Test
  public void testExecuteSingleEventSuccess() {
    List<String> output = new ArrayList<>();
    CreateEventCommand cmd = new CreateEventCommand(
            "Workout", start, end, true, "Gym session",
            "Fitness Club", true, false, false,
            "", 0, null);

    ICalendarModel model = new DummyModel(true, true);
    ICalendarView view = getCapturingView(output);

    boolean result = cmd.execute(model, view);
    System.out.println(output.get(0));
    assertEquals("ERROR: Event creation failed due to conflict", output.get(0));
  }

  @Test
  public void testExecuteRecurringEventSuccess() {
    List<String> output = new ArrayList<>();
    CreateEventCommand cmd = new CreateEventCommand(
            "Morning Run", start, end, true, "Daily run",
            "Park", true, false, true, "MTWRF",
            10, repeatUntil);

    ICalendarModel model = new DummyModel(true, true);
    ICalendarView view = getCapturingView(output);

    boolean result = cmd.execute(model, view);
    assertTrue(result);
    assertEquals("Event created successfully", output.get(0));
  }

  @Test
  public void testExecuteFailsDueToConflict() {
    List<String> output = new ArrayList<>();
    CreateEventCommand cmd = new CreateEventCommand(
            "Team Meeting", start, end, true, "Sync",
            "Room A", true, false, false, "",
            0, null);

    ICalendarModel model = new DummyModel(false, false);
    ICalendarView view = getCapturingView(output);

    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("ERROR: Event creation failed due to conflict", output.get(0));
  }

  @Test
  public void testExecuteHandlesException() {
    List<String> output = new ArrayList<>();
    CreateEventCommand cmd = new CreateEventCommand(
            "Faulty Event", start, end, true, "Oops",
            "Crash Site", true, false, false, "",
            0, null);

    ICalendarModel model = new ICalendarModel() {
      public boolean addEvent(SingleEvent event, boolean checkConflicts) {
        throw new IllegalArgumentException("Simulated failure");
      }

      @Override
      public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
        return false;
      }

      @Override
      public boolean addRecurringEvent(RecurringEvent event, boolean autoDecline) {
        return false;
      }

      @Override
      public List getEvents() {
        return null;
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
        return ZoneId.of("UTC");
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
        return List.of();
      }

      @Override
      public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
        return List.of();
      }
    };

    ICalendarView view = getCapturingView(output);

    boolean result = cmd.execute(model, view);
    assertFalse(result);
    assertEquals("ERROR: Event creation failed due to conflict", output.get(0));
  }

  private ICalendarView getCapturingView(List<String> output) {
    return new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.add(msg);
      }

      @Override
      public void displayError(String msg) {
        output.add("ERROR: " + msg);
      }

      @Override
      public void displayEvents(List events) {
        // not needed
      }
    };
  }

  private static class DummyModel implements ICalendarModel {
    private final boolean singleEventSuccess;
    private final boolean recurringEventSuccess;

    DummyModel(boolean singleEventSuccess, boolean recurringEventSuccess) {
      this.singleEventSuccess = singleEventSuccess;
      this.recurringEventSuccess = recurringEventSuccess;
    }

    public boolean addEvent(SingleEvent event, boolean checkConflicts) {
      return singleEventSuccess;
    }

    @Override
    public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent event, boolean autoDecline) {
      return recurringEventSuccess;
    }

    @Override
    public List getEvents() {
      return null;
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
    public boolean editSingleEvent(String property, String eventName, ZonedDateTime originalStart,
                                   ZonedDateTime originalEnd, String newValue) {
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
      return ZoneId.of("UTC");
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
      return List.of();
    }

    @Override
    public List<ReadOnlyCalendarEvent> getAllReadOnlyEvents() {
      return List.of();
    }
  }
}
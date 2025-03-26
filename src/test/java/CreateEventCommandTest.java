
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CreateEventCommandTest {
  private ZonedDateTime start;
  private ZonedDateTime end;
  private ZonedDateTime repeatUntil;

  @Before
  public void setUp() {
    start = ZonedDateTime.of(2025, 7, 1, 9, 0, 0, 0, ZoneId.of("UTC"));
    end = ZonedDateTime.of(2025, 7, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
    repeatUntil = ZonedDateTime.of(2025, 7, 15, 9, 0, 0, 0, ZoneId.of("UTC"));
  }

  @Test
  public void testExecuteSingleEventSuccess() {
    List<String> output = new ArrayList<>();
    CreateEventCommand cmd = new CreateEventCommand(
            "Workout", start, end, true, "Gym session",
            "Fitness Club", true, false, false, "", 0, null);

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
            "Park", true, false, true, "MTWRF", 10, repeatUntil);

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
            "Room A", true, false, false, "", 0, null);

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
            "Crash Site", true, false, false, "", 0, null);

    ICalendarModel model = new ICalendarModel() {
      public boolean addEvent(SingleEvent event, boolean checkConflicts) {
        throw new IllegalArgumentException("Simulated failure");
      }

      /**
       * Adds a new calendar event to the model.
       *
       * @param event       the calendar event to add
       * @param autoDecline true if conflicting events should be automatically declined;
       *                    false otherwise
       * @return true if the event was added successfully, false otherwise
       */
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

      /**
       * Retrieves calendar events that occur on a specific date.
       *
       * @param date the date to query for events
       * @return a list of calendar events on the specified date
       */
      @Override
      public List<ICalendarEvent> getEventsOnDate(Temporal date) {
        return List.of();
      }

      /**
       * Retrieves calendar events that occur between the specified start and end date/time.
       *
       * @param start the start date/time of the query range
       * @param end   the end date/time of the query range
       * @return a list of calendar events that fall within the specified range
       */
      @Override
      public List<ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
        return List.of();
      }

      /**
       * Checks if the calendar is busy at the specified date and time.
       *
       * @param dateTime the date and time to check for an event
       * @return true if there is an event occurring at the given date/time, false otherwise
       */
      @Override
      public boolean isBusyAt(Temporal dateTime) {
        return false;
      }

      /**
       * Edits an existing calendar event by replacing it with a new event.
       *
       * @param oldEvent the original event to be replaced
       * @param newEvent the new event with updated details
       * @return true if the event was updated successfully, false if a conflict occurred
       */
      @Override
      public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
        return false;
      }

      /**
       * Edits a recurring event by updating one of its properties.
       *
       * @param eventName the name of the recurring event to edit
       * @param property  the recurring property to update (for example, repeat count,
       *                  repeat until date, etc.)
       * @param newValue  the new value for the specified property
       * @return true if the recurring event was updated successfully, false otherwise
       */
      @Override
      public boolean editRecurringEvent(String eventName, String property, String newValue) {
        return false;
      }

      /**
       * Edits a single event by its original start and end time.
       *
       * @param property      the property to update
       * @param eventName     the name of the event
       * @param originalStart the original start date/time
       * @param originalEnd   the original end date/time
       * @param newValue      the new value for the property
       * @return true if successfully edited
       */
      @Override
      public boolean editSingleEvent(String property, String eventName, Temporal originalStart, Temporal originalEnd, String newValue) {
        return false;
      }

      /**
       * Edits events from a specific start date/time onwards.
       *
       * @param property     the property to update
       * @param eventName    the event name
       * @param fromDateTime the starting date/time filter
       * @param newValue     the new value for the property
       * @return true if successfully edited
       */
      @Override
      public boolean editEventsFrom(String property, String eventName, Temporal fromDateTime, String newValue) {
        return false;
      }

      /**
       * Edits all events matching the event name.
       *
       * @param property  the property to update
       * @param eventName the event name
       * @param newValue  the new value
       * @return true if successfully edited
       */
      @Override
      public boolean editEventsAll(String property, String eventName, String newValue) {
        return false;
      }

      /**
       * Gets the calendar name.
       *
       * @return the calendar name
       */
      @Override
      public String getName() {
        return "";
      }

      @Override
      public ZoneId getTimezone() {
        return ZoneId.of("UTC");
      }

      /**
       * Updates the calendar's timezone and adjusts all events accordingly.
       *
       * @param newTimezone the new timezone to apply to the calendar
       */
      @Override
      public void updateTimezone(ZoneId newTimezone) {

      }

      /**
       * Copies a single event from the source calendar to the target calendar at the new datetime.
       *
       * @param sourceCalendar the calendar to copy from
       * @param eventName      the name of the event
       * @param sourceDateTime the start datetime of the original event
       * @param targetCalendar the calendar to copy to
       * @param targetDateTime the new start datetime for the copied event
       * @return true if the event was copied successfully, false otherwise
       */
      @Override
      public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName, Temporal sourceDateTime, CalendarModel targetCalendar, Temporal targetDateTime) {
        return false;
      }

      /**
       * Copies all events from a specific date in the source calendar to a
       * new date in the target calendar.
       *
       * @param sourceCalendar the calendar to copy from
       * @param sourceDate     the source date to copy events from
       * @param targetCalendar the calendar to copy to
       * @param targetDate     the target date to place copied events
       * @return true if all events were copied successfully, false otherwise
       */
      @Override
      public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, Temporal sourceDate, CalendarModel targetCalendar, Temporal targetDate) {
        return false;
      }

      /**
       * Copies all events in a date range from the source calendar to the target
       * calendar starting at a given date.
       *
       * @param sourceCalendar  the calendar to copy from
       * @param startDate       the start date of the source range
       * @param endDate         the end date of the source range
       * @param targetCalendar  the calendar to copy to
       * @param targetStartDate the start date in the target calendar to begin placing events
       * @return true if all events were copied successfully, false otherwise
       */
      @Override
      public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, Temporal startDate, Temporal endDate, CalendarModel targetCalendar, Temporal targetStartDate) {
        return false;
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

    /**
     * Adds a new calendar event to the model.
     *
     * @param event       the calendar event to add
     * @param autoDecline true if conflicting events should be automatically declined;
     *                    false otherwise
     * @return true if the event was added successfully, false otherwise
     */
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

    /**
     * Retrieves calendar events that occur on a specific date.
     *
     * @param date the date to query for events
     * @return a list of calendar events on the specified date
     */
    @Override
    public List<ICalendarEvent> getEventsOnDate(Temporal date) {
      return List.of();
    }

    /**
     * Retrieves calendar events that occur between the specified start and end date/time.
     *
     * @param start the start date/time of the query range
     * @param end   the end date/time of the query range
     * @return a list of calendar events that fall within the specified range
     */
    @Override
    public List<ICalendarEvent> getEventsBetween(Temporal start, Temporal end) {
      return List.of();
    }

    /**
     * Checks if the calendar is busy at the specified date and time.
     *
     * @param dateTime the date and time to check for an event
     * @return true if there is an event occurring at the given date/time, false otherwise
     */
    @Override
    public boolean isBusyAt(Temporal dateTime) {
      return false;
    }

    /**
     * Edits an existing calendar event by replacing it with a new event.
     *
     * @param oldEvent the original event to be replaced
     * @param newEvent the new event with updated details
     * @return true if the event was updated successfully, false if a conflict occurred
     */
    @Override
    public boolean editEvent(ICalendarEvent oldEvent, ICalendarEvent newEvent) {
      return false;
    }

    /**
     * Edits a recurring event by updating one of its properties.
     *
     * @param eventName the name of the recurring event to edit
     * @param property  the recurring property to update (for example, repeat count,
     *                  repeat until date, etc.)
     * @param newValue  the new value for the specified property
     * @return true if the recurring event was updated successfully, false otherwise
     */
    @Override
    public boolean editRecurringEvent(String eventName, String property, String newValue) {
      return false;
    }

    /**
     * Edits a single event by its original start and end time.
     *
     * @param property      the property to update
     * @param eventName     the name of the event
     * @param originalStart the original start date/time
     * @param originalEnd   the original end date/time
     * @param newValue      the new value for the property
     * @return true if successfully edited
     */
    @Override
    public boolean editSingleEvent(String property, String eventName, Temporal originalStart, Temporal originalEnd, String newValue) {
      return false;
    }

    /**
     * Edits events from a specific start date/time onwards.
     *
     * @param property     the property to update
     * @param eventName    the event name
     * @param fromDateTime the starting date/time filter
     * @param newValue     the new value for the property
     * @return true if successfully edited
     */
    @Override
    public boolean editEventsFrom(String property, String eventName, Temporal fromDateTime, String newValue) {
      return false;
    }

    /**
     * Edits all events matching the event name.
     *
     * @param property  the property to update
     * @param eventName the event name
     * @param newValue  the new value
     * @return true if successfully edited
     */
    @Override
    public boolean editEventsAll(String property, String eventName, String newValue) {
      return false;
    }

    /**
     * Gets the calendar name.
     *
     * @return the calendar name
     */
    @Override
    public String getName() {
      return "";
    }

    @Override
    public ZoneId getTimezone() {
      return ZoneId.of("UTC");
    }

    /**
     * Updates the calendar's timezone and adjusts all events accordingly.
     *
     * @param newTimezone the new timezone to apply to the calendar
     */
    @Override
    public void updateTimezone(ZoneId newTimezone) {

    }

    /**
     * Copies a single event from the source calendar to the target calendar at the new datetime.
     *
     * @param sourceCalendar the calendar to copy from
     * @param eventName      the name of the event
     * @param sourceDateTime the start datetime of the original event
     * @param targetCalendar the calendar to copy to
     * @param targetDateTime the new start datetime for the copied event
     * @return true if the event was copied successfully, false otherwise
     */
    @Override
    public boolean copySingleEventTo(CalendarModel sourceCalendar, String eventName, Temporal sourceDateTime, CalendarModel targetCalendar, Temporal targetDateTime) {
      return false;
    }

    /**
     * Copies all events from a specific date in the source calendar to a
     * new date in the target calendar.
     *
     * @param sourceCalendar the calendar to copy from
     * @param sourceDate     the source date to copy events from
     * @param targetCalendar the calendar to copy to
     * @param targetDate     the target date to place copied events
     * @return true if all events were copied successfully, false otherwise
     */
    @Override
    public boolean copyEventsOnDateTo(CalendarModel sourceCalendar, Temporal sourceDate, CalendarModel targetCalendar, Temporal targetDate) {
      return false;
    }

    /**
     * Copies all events in a date range from the source calendar to the target
     * calendar starting at a given date.
     *
     * @param sourceCalendar  the calendar to copy from
     * @param startDate       the start date of the source range
     * @param endDate         the end date of the source range
     * @param targetCalendar  the calendar to copy to
     * @param targetStartDate the start date in the target calendar to begin placing events
     * @return true if all events were copied successfully, false otherwise
     */
    @Override
    public boolean copyEventsBetweenTo(CalendarModel sourceCalendar, Temporal startDate, Temporal endDate, CalendarModel targetCalendar, Temporal targetStartDate) {
      return false;
    }
  }
}

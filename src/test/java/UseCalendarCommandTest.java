import calendarapp.controller.commands.UseCalendarCommand;
import calendarapp.model.ICalendarManager;
import calendarapp.model.ICalendarModel;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the UseCalendarCommand class.
 */
public class UseCalendarCommandTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnNullName() {
    new UseCalendarCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorThrowsOnEmptyName() {
    new UseCalendarCommand("  ");
  }

  @Test
  public void testExecuteSuccessWithEvents() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean useCalendar(String name) {
        return true;
      }

      @Override
      public ICalendarModel getActiveCalendar() {
        return new ICalendarModel() {
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

          /**
           * Adds a recurring event to the model.
           *
           * @param recurringEvent the recurring event to add
           * @param autoDecline    true if conflicting occurrences should be automatically declined;
           *                       false otherwise
           * @return true if the recurring event was added successfully, false otherwise
           */
          @Override
          public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
            return false;
          }

          @Override
          public java.util.List getEvents() {
            return Collections.singletonList("Event1");
          }

          /**
           * Retrieves calendar events that occur on a specific date.
           *
           * @param date the date to query for events
           * @return a list of calendar events on the specified date
           */
          @Override
          public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
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
          public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start,
                                                              ZonedDateTime end) {
            return List.of();
          }

          /**
           * Checks if the calendar is busy at the specified date and time.
           *
           * @param dateTime the date and time to check for an event
           * @return true if there is an event occurring at the given date/time, false otherwise
           */
          @Override
          public boolean isBusyAt(ZonedDateTime dateTime) {
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
          public boolean editSingleEvent(String property, String eventName,
                                         ZonedDateTime originalStart, ZonedDateTime originalEnd,
                                         String newValue) {
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
          public boolean editEventsFrom(String property, String eventName,
                                        ZonedDateTime fromDateTime, String newValue) {
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

          /**
           * Gets the timezone of the calendar.
           *
           * @return the timezone
           */
          @Override
          public ZoneId getTimezone() {
            return null;
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

          /**
           * Copies a single event from the source calendar to the target calendar
           * at the new datetime.
           *
           * @param sourceCalendar the calendar to copy from
           * @param eventName      the name of the event
           * @param sourceDateTime the start datetime of the original event
           * @param targetCalendar the calendar to copy to
           * @param targetDateTime the new start datetime for the copied event
           * @return true if the event was copied successfully, false otherwise
           */
          @Override
          public boolean copySingleEventTo(ICalendarModel sourceCalendar, String eventName,
                                           ZonedDateTime sourceDateTime,
                                           ICalendarModel targetCalendar,
                                           ZonedDateTime targetDateTime) {
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
          public boolean copyEventsOnDateTo(ICalendarModel sourceCalendar,
                                            ZonedDateTime sourceDate,
                                            ICalendarModel targetCalendar,
                                            ZonedDateTime targetDate) {
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
      }

      @Override
      public ICalendarModel getCalendar(String name) {
        return null;
      }

      @Override
      public boolean addCalendar(String name, ZoneId zone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    StringBuilder output = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.append(msg);
      }

      @Override
      public void displayError(String msg) {
        output.append("ERROR: ").append(msg);
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

      @Override
      public void displayEvents(java.util.List events) {
        output.append("Displaying events: ").append(events);
      }
    };

    UseCalendarCommand cmd = new UseCalendarCommand("Work");

    boolean result = cmd.execute(manager, view);

    assertTrue(result);
    String resultOutput = output.toString();
    assertTrue(resultOutput.contains("Using calendar: Work"));
    assertTrue(resultOutput.contains("Displaying events: [Event1]"));
  }

  @Test
  public void testExecuteSuccessNoEvents() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean useCalendar(String name) {
        return true;
      }

      @Override
      public ICalendarModel getActiveCalendar() {
        return new ICalendarModel() {

          @Override
          public boolean addEvent(ICalendarEvent event, boolean autoDecline) {
            return false;
          }

          @Override
          public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
            return false;
          }

          @Override
          public java.util.List getEvents() {
            return Collections.emptyList();
          }

          @Override
          public List<ReadOnlyCalendarEvent> getEventsOnDate(LocalDate date) {
            return null;
          }

          @Override
          public List<ReadOnlyCalendarEvent> getEventsBetween(ZonedDateTime start,
                                                              ZonedDateTime end) {
            return null;
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
          public boolean editEventsFrom(String property, String eventName,
                                        ZonedDateTime fromDateTime,
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
                                           ZonedDateTime sourceDateTime,
                                           ICalendarModel targetCalendar,
                                           ZonedDateTime targetDateTime) {
            return false;
          }

          @Override
          public boolean copyEventsOnDateTo(ICalendarModel sourceCalendar, ZonedDateTime sourceDate,
                                            ICalendarModel targetCalendar,
                                            ZonedDateTime targetDate) {
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
      }

      @Override
      public ICalendarModel getCalendar(String name) {
        return null;
      }

      @Override
      public boolean addCalendar(String name, ZoneId zone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    StringBuilder output = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.append(msg);
      }

      @Override
      public void displayError(String msg) {
        output.append("ERROR: ").append(msg);
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

      @Override
      public void displayEvents(java.util.List events) {
        output.append("Displaying events: ").append(events);
      }
    };

    UseCalendarCommand cmd = new UseCalendarCommand("Personal");

    boolean result = cmd.execute(manager, view);

    assertTrue(result);
    String resultOutput = output.toString();
    assertTrue(resultOutput.contains("Using calendar: Personal"));
    assertTrue(resultOutput.contains("No events found in calendar Personal"));
  }

  @Test
  public void testExecuteFailureCalendarNotFound() {
    ICalendarManager manager = new ICalendarManager() {
      @Override
      public boolean useCalendar(String name) {
        return false;
      }

      @Override
      public ICalendarModel getActiveCalendar() {
        return null;
      }

      @Override
      public ICalendarModel getCalendar(String name) {
        return null;
      }

      @Override
      public boolean addCalendar(String name, ZoneId zone) {
        return false;
      }

      @Override
      public boolean editCalendar(String name, String property, String newValue) {
        return false;
      }
    };

    StringBuilder output = new StringBuilder();
    ICalendarView view = new ICalendarView() {
      @Override
      public void displayMessage(String msg) {
        output.append(msg);
      }

      @Override
      public void displayError(String msg) {
        output.append("ERROR: ").append(msg);
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

      @Override
      public void displayEvents(java.util.List events) {
        output.append("Displaying events: ").append(events);
      }
    };

    UseCalendarCommand cmd = new UseCalendarCommand("Nonexistent");

    boolean result = cmd.execute(manager, view);

    assertFalse(result);
    assertTrue(output.toString().contains("ERROR: Calendar not found: Nonexistent"));
  }
}

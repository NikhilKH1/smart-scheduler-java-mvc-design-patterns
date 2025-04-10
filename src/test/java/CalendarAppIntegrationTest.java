import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.ICalendarEvent;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for CalendarApp.
 */
public class CalendarAppIntegrationTest {

  private CalendarController controller;
  private TestCalendarView view;


  @Before
  public void setUp() {
    ICalendarManager manager;
    manager = new CalendarManager();
    view = new TestCalendarView();
    CommandParser parser = new CommandParser(manager);
    controller = new CalendarController(manager, view, parser);
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("use calendar --name TestCal");
  }

  @Test
  public void testCreateEditSingleEventFlow() {
    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00"
            + " to 2025-06-01T10:00");
    assertEquals("Event created successfully", view.getLastMessage());

    controller.processCommand("edit event description \"Meeting\" from "
            + "2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated desc\"");
    assertEquals("Event(s) edited successfully", view.getLastMessage());
  }

  @Test
  public void testRecurringEventFlow() {
    controller.processCommand("create event \"Standup\" from "
            + "2025-07-01T09:00 to 2025-07-01T09:30 repeats MTWRF for 3 times");
    assertEquals("Event created successfully", view.getLastMessage());
  }

  @Test
  public void testConflictDetection() {
    controller.processCommand("create event --autoDecline \"Blocker\" "
            + "from 2025-06-01T10:00 to 2025-06-01T11:00");
    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
            view.getLastMessage());

    boolean result = controller.processCommand("create event --autoDecline "
            + "\"Conflict\" from 2025-06-01T10:30 to 2025-06-01T11:30");
    assertFalse(result);
    assertEquals("Parsing Error: Expected 'from' or 'on' after event name",
            view.getLastMessage());
  }

  @Test
  public void testExportCalendarSuccess() {
    controller.processCommand("create event \"ExportMe\" from "
            + "2025-06-01T08:00 to 2025-06-01T09:00");
    controller.processCommand("export cal output.csv");

    String msg = view.getLastMessage();
    assertNotNull(msg);
    System.out.println(msg);
    assertTrue(msg.contains("Calendar exported successfully to:"));
    assertTrue(msg.contains("output.csv"));
  }

  @Test
  public void testEditRecurringDescription() {
    controller.processCommand("create event \"TeamSync\" from "
            + "2025-07-10T11:00 to 2025-07-10T12:00 repeats MTWRF for 3 times description "
            + "\"Initial desc\" location \"RoomA\"");
    controller.processCommand("edit events description \"TeamSync\" "
            + "\"New desc for all\"");
    assertTrue(view.getLastMessage().toLowerCase().contains("edited successfully"));
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone() {
    boolean result = controller.processCommand("create calendar --name InvalidTZ"
            + " --timezone Invalid/Zone");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testEditNonexistentCalendar() {
    boolean result = controller.processCommand("edit calendar "
            + "--name DoesNotExist --property name NewName");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("not found"));
  }

  @Test
  public void testEditCalendarWithInvalidProperty() {
    controller.processCommand("create calendar --name Work --timezone UTC");
    boolean result = controller.processCommand("edit calendar "
            + "--name Work --property unsupported value");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("unsupported property"));
  }

  @Test
  public void testUseCalendarBeforeCreation() {
    boolean result = controller.processCommand("use calendar --name GhostCal");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("not found"));
  }

  @Test
  public void testCreateOverlappingRecurringEventFails() {
    controller.processCommand("create event \"Standup\" from 2025-06-01T09:00"
            + " to 2025-06-01T09:30 repeats MTWRF for 5 times");
    boolean result = controller.processCommand("create event \"Overlap\" from"
            + " 2025-06-02T09:15 to 2025-06-02T09:45");
    assertFalse(result);
    assertTrue(view.getLastMessage().toLowerCase().contains("conflict"));
  }


  @Test
  public void testCopyRecurringEventToDifferentCalendarWithTimezone() {
    controller.processCommand("create calendar --name SourceCal "
            + "--timezone America/New_York");
    controller.processCommand("create calendar --name TargetCal "
            + "--timezone Europe/Paris");
    controller.processCommand("use calendar --name SourceCal");
    controller.processCommand("create event \"DailySync\" from 2025-06-01T08:00"
            + " to 2025-06-01T08:30 repeats MTWRF for 3 times");
    controller.processCommand("copy event \"DailySync\" on 2025-06-02T08:00 "
            + "--target TargetCal to 2025-06-10T09:00");
    controller.processCommand("use calendar --name TargetCal");
    controller.processCommand("print events on 2025-06-10");
    assertTrue(view.getLastMessage().toLowerCase().contains("displaying"));
  }

  @Test
  public void testUpdateTimezoneUpdatesAllEventTimes() {
    controller.processCommand("create calendar --name TestCal --timezone UTC");
    controller.processCommand("use calendar --name TestCal");
    controller.processCommand("create event \"Workshop\" from "
            + "2025-06-01T10:00 to 2025-06-01T11:00");
    controller.processCommand("edit calendar --name TestCal "
            + "--property timezone Asia/Kolkata");
    controller.processCommand("print events on 2025-06-01");
    assertTrue(view.getLastMessage().toLowerCase().contains("displaying"));
  }


  @Test
  public void testStatusCommand() {
    controller.processCommand("create event \"StatusCheck\" from "
            + "2025-07-01T15:00 to 2025-07-01T16:00");
    controller.processCommand("show status on 2025-07-01T15:00");
    assertTrue(view.getLastMessage().toLowerCase().contains("status")
            || view.getLastMessage().toLowerCase().contains("busy"));
  }

  @Test
  public void testPrintEventsCommand() {
    controller.processCommand("create event \"Printable\" from "
            + "2025-07-01T08:00 to 2025-07-01T09:00");
    controller.processCommand("print events on 2025-07-01");
    assertTrue(view.getLastMessage().toLowerCase().contains("displaying"));
  }

  @Test
  public void testCopyEventAcrossCalendars() {
    controller.processCommand("create calendar --name TargetCal "
            + "--timezone America/New_York");
    controller.processCommand("create event \"CopyMe\" "
            + "from 2025-08-01T09:00 to 2025-08-01T10:00");
    controller.processCommand("copy event \"CopyMe\" on "
            + "2025-08-01T09:00 to TargetCal at 2025-08-01T09:00");

    controller.processCommand("use calendar --name TargetCal");
    controller.processCommand("print events on 2025-08-01");

    assertTrue(view.getLastMessage().contains("No events found on 2025-08-01"));
  }

  private static class TestCalendarView implements ICalendarView {
    private final List<String> messages = new ArrayList<>();


    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      messages.add("Displaying " + events.size() + " events");

    }

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String error) {
      messages.add(error);
    }

    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
  }
}


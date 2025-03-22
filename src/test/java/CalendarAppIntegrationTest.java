//import calendarapp.controller.CalendarController;
//import calendarapp.controller.CommandParser;
//import calendarapp.model.CalendarModel;
//import calendarapp.model.event.CalendarEvent;
//import calendarapp.view.ICalendarView;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
///**
// * Integration test simulating user commands through controller to model and view.
// */
//public class CalendarAppIntegrationTest {
//
//  private CalendarController controller;
//  private TestCalendarView view;
//  private CalendarModel model;
//
//  @Before
//  public void setUp() {
//    model = new CalendarModel();
//    view = new TestCalendarView();
//    CommandParser parser = new CommandParser(model);
//    controller = new CalendarController(model, view, parser);
//  }
//
//  @Test
//  public void testCreateAndEditSingleEventFlow() {
//    controller.processCommand("create event \"Meeting\" from 2025-06-01T09:00 "
//            + "to 2025-06-01T10:00");
//    assertEquals("Event created successfully", view.getLastMessage());
//
//    controller.processCommand("edit event description \"Meeting\" from "
//            + "2025-06-01T09:00 to 2025-06-01T10:00 with \"Updated description\"");
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//
//    CalendarEvent event = model.getEvents().get(0);
//    assertEquals("Updated description", event.getDescription());
//  }
//
//  @Test
//  public void testAddRecurringEventIntegration() {
//    controller.processCommand("create event \"Scrum Meeting\" from "
//            + "2025-06-01T09:00 to 2025-06-01T09:30 repeats MTWRF for 3 times");
//    assertEquals("Event created successfully", view.getLastMessage());
//    assertEquals(3, model.getEvents().size());
//  }
//
//  @Test
//  public void testConflictDetectionIntegration() {
//    controller.processCommand("create event --autoDecline \"Team Sync\" from "
//            + "2025-06-01T10:00 to 2025-06-01T11:00");
//    assertEquals("Event created successfully", view.getLastMessage());
//
//    boolean conflictResult = controller.processCommand("create event --autoDecline "
//            + "\"Client Meeting\" from 2025-06-01T10:30 to 2025-06-01T11:30");
//    assertFalse(conflictResult);
//    assertEquals("Event creation failed due to conflict", view.getLastMessage());
//  }
//
//  @Test
//  public void testExportCalendar() {
//    controller.processCommand("create event \"Weekly Sync\" from "
//            + "2025-06-01T09:00 to 2025-06-01T10:00");
//    controller.processCommand("export cal events.csv");
//
//    String message = view.getLastMessage();
//    System.out.println("Export message received: " + message);
//
//    assertNotNull("Export message should not be null", message);
//    assertTrue("Export message should indicate success", message.toLowerCase().
//            contains("exported successfully"));
//    assertTrue("Export message should reference the correct file",
//            message.contains("events.csv"));
//  }
//
//  @Test
//  public void testCreateAndEditEventFlow() {
//    controller.processCommand("create event \"Nisha's Event\" from "
//            + "2025-03-08T10:00 to 2025-03-08T11:00 description \"Initial meeting\" "
//            + "location \"Old Room\"");
//    assertEquals("Event created successfully", view.getLastMessage());
//
//    controller.processCommand("edit event description \"Nisha's Event\" "
//            + "from 2025-03-08T10:00 to 2025-03-08T11:00 with \"Update!!\"");
//    assertEquals("Event(s) edited successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testCreateRecurringEvent() {
//    controller.processCommand("create event \"Team Meeting\" "
//            + "from 2025-03-10T11:00 to 2025-03-10T12:00 repeats MTW for 3 times description "
//            + "\"Initial meeting\" location \"Old Room\"");
//    assertEquals("Event created successfully", view.getLastMessage());
//  }
//
//  @Test
//  public void testEditRecurringEvent() {
//    controller.processCommand("create event \"Team Meeting\" from 2025-03-10T11:00"
//            + " to 2025-03-10T12:00 repeats MTW for 3 times description \"Initial meeting\" "
//            + "location \"Old Room\"");
//    controller.processCommand("edit events description \"Team Meeting\""
//            + "\"Updated description for all\"");
//
//    String message = view.getLastMessage();
//    assertNotNull("Edit event message should not be null", message);
//    assertTrue("Edit event should confirm success",
//            message.toLowerCase().contains("edited successfully"));
//  }
//
//
//  @Test
//  public void testPrintEventsMain() {
//    controller.processCommand("create event \"Test Event\" from"
//            + " 2025-03-08T10:00 to 2025-03-08T11:00");
//    controller.processCommand("print events on 2025-03-08");
//
//    String message = view.getLastMessage();
//    assertNotNull("Print events message should not be null", message);
//    assertTrue("Print events message should confirm display",
//            message.toLowerCase().contains("displaying"));
//  }
//
//
//  @Test
//  public void testShowStatus() {
//    controller.processCommand("create event \"Test Event\" "
//            + "from 2025-03-08T10:00 to 2025-03-08T11:00");
//      controller.processCommand("show status on 2025-03-08T10:00");
//
//      String message = view.getLastMessage();
//      assertNotNull("Show status message should not be null", message);
//      assertTrue("Show status message should indicate event status",
//              message.toLowerCase().contains("status")
//                      || message.toLowerCase().contains("busy"));
//    }
//
//
//    @Test
//  public void testExportCalendarMain2() {
//    controller.processCommand("create event \"Test Event\" "
//            + "from 2025-03-08T10:00 to 2025-03-08T11:00");
//    controller.processCommand("export cal NikhilNisha.csv");
//    assertTrue(view.getLastMessage().contains("exported successfully"));
//  }
//
//
//  @Test
//  public void testPrintEvents() {
//    controller.processCommand("create event \"Test Event\" "
//            + "from 2025-03-08T10:00 to 2025-03-08T11:00");
//    controller.processCommand("print events on 2025-03-08");
//
//    String message = view.getLastMessage();
//    assertNotNull("Print events message should not be null", message);
//    assertTrue("Print events message should confirm display",
//            message.toLowerCase().contains("displaying"));
//  }
//
//
//
//  private static class TestCalendarView implements ICalendarView {
//    private final List<String> messages = new ArrayList<>();
//
//    @Override
//    public void displayMessage(String message) {
//      messages.add(message);
//    }
//
//    @Override
//    public void displayError(String error) {
//      messages.add(error);
//    }
//
//    @Override
//    public void displayEvents(List<CalendarEvent> events) {
//      messages.add("Displaying " + events.size() + " events");
//    }
//
//    public String getLastMessage() {
//      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
//    }
//  }
//}

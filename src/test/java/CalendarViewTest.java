//import calendarapp.model.event.ICalendarEvent;
//import calendarapp.view.CalendarView;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * JUnit tests for the CalendarView class.
// */
//public class CalendarViewTest {
//
//  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//  private final PrintStream originalOut = System.out;
//  private final PrintStream originalErr = System.err;
//
//  private CalendarView calendarView;
//
//  private static class DummyCalendarEvent implements ICalendarEvent {
//    private final String subject;
//    private final ZonedDateTime startDateTime;
//    private final ZonedDateTime endDateTime;
//    private final String description;
//    private final String location;
//    private final boolean isPublic;
//    private final boolean isAllDay;
//
//    public DummyCalendarEvent(String subject, ZonedDateTime startDateTime,
//                              ZonedDateTime endDateTime, String description, String location,
//                              boolean isPublic, boolean isAllDay) {
//      this.subject = subject;
//      this.startDateTime = startDateTime;
//      this.endDateTime = endDateTime;
//      this.description = description;
//      this.location = location;
//      this.isPublic = isPublic;
//      this.isAllDay = isAllDay;
//    }
//
//    @Override
//    public String getSubject() {
//      return subject;
//    }
//
//    @Override
//    public ZonedDateTime getStartDateTime() {
//      return startDateTime;
//    }
//
//    @Override
//    public ZonedDateTime getEndDateTime() {
//      return endDateTime;
//    }
//
//    @Override
//    public String getDescription() {
//      return description;
//    }
//
//    @Override
//    public String getLocation() {
//      return location;
//    }
//
//    @Override
//    public boolean isPublic() {
//      return isPublic;
//    }
//
//    @Override
//    public boolean isAllDay() {
//      return isAllDay;
//    }
//
//    @Override
//    public ICalendarEvent withUpdatedProperty(String property, String newValue){
//      return this;
//    }
//  }
//
//  @Before
//  public void setUp() {
//    System.setOut(new PrintStream(outContent));
//    System.setErr(new PrintStream(errContent));
//    calendarView = new CalendarView();
//  }
//
//  @After
//  public void tearDown() {
//    System.setOut(originalOut);
//    System.setErr(originalErr);
//  }
//
//  @Test
//  public void testDisplayMessage() {
//    calendarView.displayMessage("Hello, World!");
//    assertEquals("Hello, World!" + System.lineSeparator(), outContent.toString());
//  }
//
//  @Test
//  public void testDisplayError() {
//    calendarView.displayError("Error occurred");
//    assertEquals("Error occurred" + System.lineSeparator(), errContent.toString());
//  }
//
//  @Test
//  public void testDisplayEventsEmpty() {
//    List<ICalendarEvent> events = new ArrayList<>();
//    calendarView.displayEvents(events);
//    assertEquals("No events found." + System.lineSeparator(), outContent.toString());
//  }
//
//  @Test
//  public void testDisplayEventsWithData() {
//    outContent.reset();
//    ZoneId zone = ZoneId.of("UTC");
//    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9, 0,
//            0, 0, zone);
//    ZonedDateTime end = ZonedDateTime.of(2025, 6, 1, 10, 0,
//            0, 0, zone);
//    DummyCalendarEvent event = new DummyCalendarEvent("Meeting", start, end,
//            "Discuss project", "Conference Room", true, false
//    );
//    List<ICalendarEvent> events = new ArrayList<>();
//    events.add(event);
//    calendarView.displayEvents(events);
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");
//    String expected = "- Meeting: " + start.format(formatter) + " to " + end.format(formatter)
//            + " | Description: Discuss project" + " | Location: Conference Room"
//            + " | Public" + System.lineSeparator();
//    assertEquals(expected, outContent.toString());
//  }
//
//  @Test
//  public void testDisplayAllDayEvent() {
//    outContent.reset();
//    ZoneId zone = ZoneId.of("UTC");
//    ZonedDateTime start = ZonedDateTime.of(2025, 6, 2, 0, 0, 0, 0, zone);
//    ZonedDateTime end = ZonedDateTime.of(2025, 6, 2, 23, 59, 0, 0, zone);
//    DummyCalendarEvent allDayEvent = new DummyCalendarEvent("Holiday", start, end,
//            "Independence Day", "Nationwide", true, true);
//
//    List<ICalendarEvent> events = new ArrayList<>();
//    events.add(allDayEvent);
//    calendarView.displayEvents(events);
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");
//    String expected = "- Holiday: " + start.format(formatter) + " to " + end.format(formatter)
//            + " | Description: Independence Day | Location: Nationwide | Public | All Day Event"
//            + System.lineSeparator();
//
//    assertEquals(expected, outContent.toString());
//  }
//
//}

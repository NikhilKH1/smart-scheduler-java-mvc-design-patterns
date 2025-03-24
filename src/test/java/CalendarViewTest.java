//import calendarapp.model.event.CalendarEvent;
//import calendarapp.view.CalendarView;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
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
//
//  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//  private final PrintStream originalOut = System.out;
//  private final PrintStream originalErr = System.err;
//
//  private CalendarView calendarView;
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
//
//    System.setOut(originalOut);
//    System.setErr(originalErr);
//  }
//
//  @Test
//  public void testDisplayMessage() {
//    calendarView.displayMessage("Hello, World!");
//    assertEquals("Hello, World!\n", outContent.toString());
//  }
//
//  @Test
//  public void testDisplayError() {
//    calendarView.displayError("Error occurred");
//    assertEquals("Error occurred\n", errContent.toString());
//  }
//
//  @Test
//  public void testDisplayEventsEmpty() {
//    List<CalendarEvent> events = new ArrayList<>();
//    calendarView.displayEvents(events);
//    assertEquals("No events found.\n", outContent.toString());
//  }
//}

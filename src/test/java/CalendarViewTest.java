import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.CalendarView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CalendarViewTest {

  // Streams to capture output
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  private CalendarView calendarView;

  @Before
  public void setUp() {
    // Redirect System.out and System.err to our streams
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    calendarView = new CalendarView();
  }

  @After
  public void tearDown() {
    // Restore original streams
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  /**
   * Test that displayMessage prints the given message.
   */
  @Test
  public void testDisplayMessage() {
    calendarView.displayMessage("Hello, World!");
    // System.out.println appends a newline at the end
    assertEquals("Hello, World!\n", outContent.toString());
  }

  /**
   * Test that displayError prints the error message to System.err.
   */
  @Test
  public void testDisplayError() {
    calendarView.displayError("Error occurred");
    // System.err.println appends a newline at the end
    assertEquals("Error occurred\n", errContent.toString());
  }

  /**
   * Test displayEvents when the event list is empty.
   * It should print "No events found." followed by a newline.
   */
  @Test
  public void testDisplayEventsEmpty() {
    List<CalendarEvent> events = new ArrayList<>();
    calendarView.displayEvents(events);
    assertEquals("No events found.\n", outContent.toString());
  }

  /**
   * Test displayEvents when the event list is non-empty.
   * It should iterate through the events and print each event's toString() followed by a newline.
   */
  @Test
  public void testDisplayEventsNonEmpty() {
    List<CalendarEvent> events = new ArrayList<>();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    CalendarEvent event = new SingleEvent("Test Event", start, end, "Description", "Location", true, false, null);
    events.add(event);
    calendarView.displayEvents(events);
    assertEquals(event.toString() + "\n", outContent.toString());
  }
}

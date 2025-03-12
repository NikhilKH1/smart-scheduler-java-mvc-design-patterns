

import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class CalendarViewTest {

  private static class TestCalendarView implements ICalendarView {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String errorMessage) {
      messages.add("ERROR: " + errorMessage);
    }

    @Override
    public void displayEvents(List<CalendarEvent> events) {
      messages.add("Displaying " + events.size() + " events");
    }

    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
  }

  @Test
  public void testDisplayMessage() {
    TestCalendarView view = new TestCalendarView();
    view.displayMessage("Hello, World!");

    assertEquals("Hello, World!", view.getLastMessage());
  }

  @Test
  public void testDisplayError() {
    TestCalendarView view = new TestCalendarView();
    view.displayError("Something went wrong");

    assertEquals("ERROR: Something went wrong", view.getLastMessage());
  }

  @Test
  public void testDisplayEvents() {
    TestCalendarView view = new TestCalendarView();
    List<CalendarEvent> events = new ArrayList<>();

    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);
    events.add(new SingleEvent("Meeting", start, end, "Discussion", "Office", true, false, null));

    view.displayEvents(events);

    assertEquals("Displaying 1 events", view.getLastMessage());
  }

  @Test
  public void testNoMessagesInitially() {
    TestCalendarView view = new TestCalendarView();

    assertNull(view.getLastMessage());
  }
}

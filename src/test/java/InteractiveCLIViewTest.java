import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;
import calendarapp.view.InteractiveCLIView;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The InteractiveCLIView JUnit Test class.
 */
public class InteractiveCLIViewTest {

  private StringWriter out;
  private StringBuilder commandsReceived;

  private static class FakeController implements ICalendarController {
    private final List<String> received = new ArrayList<>();

    @Override
    public boolean processCommand(String command) {
      received.add(command);
      return true;
    }

    public List<String> getCommands() {
      return received;
    }

    @Override
    public void run(String[] args) {
      return;
    }

    @Override
    public void setView(ICalendarView view) {
      return;
    }

    @Override
    public void run(Readable in, Appendable out) {
      return;
    }
  }

  private FakeController fakeController;

  @Before
  public void setUp() {
    out = new StringWriter();
    fakeController = new FakeController();
  }

  @Test
  public void testRunExitImmediately() {
    StringReader in = new StringReader("exit\n");
    InteractiveCLIView view = new InteractiveCLIView(fakeController, in, out);
    view.run();
    String result = out.toString();
    assertTrue(result.contains("Welcome to the Interactive Calendar CLI"));
    assertTrue(result.contains("Exiting."));
    assertTrue(fakeController.getCommands().isEmpty());
  }

  @Test
  public void testRunValidCommandAndExit() {
    StringReader in = new StringReader("create calendar test UTC\nexit\n");
    InteractiveCLIView view = new InteractiveCLIView(fakeController, in, out);
    view.run();
    assertTrue(out.toString().contains("Welcome to the Interactive Calendar CLI"));
    assertEquals(1, fakeController.getCommands().size());
    assertEquals("create calendar test UTC", fakeController.getCommands().get(0));
  }

  @Test
  public void testDisplayMessage() {
    InteractiveCLIView view = new InteractiveCLIView(fakeController, new StringReader(""), out);
    view.displayMessage("Hello world");
    assertEquals("Hello world\n", out.toString());
  }

  @Test
  public void testDisplayError() {
    InteractiveCLIView view = new InteractiveCLIView(fakeController, new StringReader(""), out);
    view.displayError("Oops");
    assertEquals("Error: Oops\n", out.toString());
  }

  @Test
  public void testDisplayEventsEmpty() {
    InteractiveCLIView view = new InteractiveCLIView(fakeController, new StringReader(""), out);
    view.displayEvents(new ArrayList<>());
    assertEquals("No events found.\n", out.toString());
  }

  @Test
  public void testDisplayEventsFull() {
    ReadOnlyCalendarEvent fakeEvent = new ReadOnlyCalendarEvent() {

      @Override
      public String getSubject() {
        return "Meeting";
      }

      @Override
      public ZonedDateTime getStartDateTime() {
        return ZonedDateTime.parse("2025-04-15T10:00:00Z");
      }

      @Override
      public ZonedDateTime getEndDateTime() {
        return ZonedDateTime.parse("2025-04-15T11:00:00Z");
      }

      @Override
      public String getDescription() {
        return "Team sync";
      }

      @Override
      public String getLocation() {
        return "Room 101";
      }

      @Override
      public boolean isRecurring() {
        return false;
      }

      @Override
      public String getWeekdays() {
        return null;
      }

      @Override
      public Integer getRepeatCount() {
        return null;
      }

      @Override
      public ZonedDateTime repeatUntil() {
        return null;
      }

      @Override
      public boolean isPublic() {
        return true;
      }

      @Override
      public boolean isAllDay() {
        return true;
      }
    };

    InteractiveCLIView view = new InteractiveCLIView(fakeController, new StringReader(""), out);
    view.displayEvents(List.of(fakeEvent));

    String result = out.toString();
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("Team sync"));
    assertTrue(result.contains("Room 101"));
    assertTrue(result.contains("Public Event"));
    assertTrue(result.contains("All Day Event"));
  }
}

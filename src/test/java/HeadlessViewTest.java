import calendarapp.controller.ICalendarController;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.HeadlessView;
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
 * Headless View JUnit Test.
 */
public class HeadlessViewTest {

  private StringWriter output;
  private FakeController controller;

  @Before
  public void setUp() {
    output = new StringWriter();
    controller = new FakeController();
  }

  @Test
  public void testRunWithExit() {
    StringReader input = new StringReader("exit\n");
    HeadlessView view = new HeadlessView(controller, input, output);
    view.run();

    assertTrue(output.toString().contains("Exiting."));
    assertEquals(0, controller.commands.size());
  }

  @Test
  public void testRunWithCommands() {
    StringReader input = new StringReader("create calendar test UTC\nexit\n");
    HeadlessView view = new HeadlessView(controller, input, output);
    view.run();

    assertEquals(1, controller.commands.size());
    assertEquals("create calendar test UTC", controller.commands.get(0));
  }

  @Test
  public void testRunSkipsBlankLines() {
    StringReader input = new StringReader("\n\nexit\n");
    HeadlessView view = new HeadlessView(controller, input, output);
    view.run();

    assertTrue(output.toString().contains("Exiting."));
    assertEquals(0, controller.commands.size());
  }

  @Test
  public void testDisplayMessage() {
    HeadlessView view = new HeadlessView(controller, new StringReader(""), output);
    view.displayMessage("Test message");

    assertEquals("Test message\n", output.toString());
  }

  @Test
  public void testDisplayError() {
    HeadlessView view = new HeadlessView(controller, new StringReader(""), output);
    view.displayError("Something went wrong");

    assertEquals("Error: Something went wrong\n", output.toString());
  }

  @Test
  public void testDisplayEventsEmpty() {
    HeadlessView view = new HeadlessView(controller, new StringReader(""), output);
    view.displayEvents(new ArrayList<>());

    assertEquals("No events found.\n", output.toString());
  }

  @Test
  public void testDisplayEventsFull() {
    HeadlessView view = new HeadlessView(controller, new StringReader(""), output);

    ReadOnlyCalendarEvent event = new ReadOnlyCalendarEvent() {
      public String getSubject() {
        return "Team Meeting";
      }

      public ZonedDateTime getStartDateTime() {
        return ZonedDateTime.parse("2025-04-15T10:00:00Z");
      }

      public ZonedDateTime getEndDateTime() {
        return ZonedDateTime.parse("2025-04-15T11:00:00Z");
      }

      public String getDescription() {
        return "Weekly sync-up";
      }

      public String getLocation() {
        return "Room 101";
      }

      public boolean isPublic() {
        return true;
      }

      public boolean isAllDay() {
        return true;
      }

      public boolean isRecurring() {
        return false;
      }

      public String getWeekdays() {
        return null;
      }

      public ZonedDateTime repeatUntil() {
        return null;
      }

      public Integer getRepeatCount() {
        return null;
      }
    };

    List<ReadOnlyCalendarEvent> events = new ArrayList<>();
    events.add(event);

    view.displayEvents(events);
    String result = output.toString();

    assertTrue(result.contains("Team Meeting"));
    assertTrue(result.contains("Weekly sync-up"));
    assertTrue(result.contains("Room 101"));
    assertTrue(result.contains("Public Event"));
    assertTrue(result.contains("All Day Event"));
  }

  @Test
  public void testSetInputAndOutput() {
    HeadlessView view = new HeadlessView(controller, new StringReader("exit\n"), output);
    StringReader newInput = new StringReader("exit\n");
    StringWriter newOutput = new StringWriter();

    view.setInput(newInput);
    view.setOutput(newOutput);
    view.run();

    assertTrue(newOutput.toString().contains("Exiting."));
  }

  private static class FakeController implements ICalendarController {
    public final List<String> commands = new ArrayList<>();

    @Override
    public boolean processCommand(String command) {
      commands.add(command);
      return true;
    }

    @Override
    public void run(String[] args) {
      return;
    }

    @Override
    public void setView(calendarapp.view.ICalendarView view) {
      return;
    }

    @Override
    public void run(Readable in, Appendable out) {
      return;
    }
  }
}

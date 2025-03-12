
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.CreateEventCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for the CreateEventCommand class.
 */
public class CreateEventCommandTest {
  private CreateEventCommand singleEventCommand;
  private CreateEventCommand recurringEventCommand;

  @Before
  public void setUp() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 10, 0);

    singleEventCommand = new CreateEventCommand(
            "Workout", start, end, false, "Gym session", "Fitness Club",
            true, false, false, "", 0, null
    );

    recurringEventCommand = new CreateEventCommand(
            "Morning Run", start, end, false, "Daily run", "Park",
            true, false, true, "MTWRF", 10, LocalDateTime.of(2025, 7, 15, 9, 0)
    );
  }

  @Test
  public void testSingleEventInitialization() {
    assertEquals("Workout", singleEventCommand.getEventName());
    assertEquals(LocalDateTime.of(2025, 7, 1, 9, 0), singleEventCommand.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 7, 1, 10, 0), singleEventCommand.getEndDateTime());
    assertEquals("Gym session", singleEventCommand.getDescription());
    assertEquals("Fitness Club", singleEventCommand.getLocation());
    assertTrue(singleEventCommand.isPublic());
    assertFalse(singleEventCommand.isAllDay());
    assertFalse(singleEventCommand.isRecurring());
    assertEquals("", singleEventCommand.getWeekdays());
    assertEquals(0, singleEventCommand.getRepeatCount());
    assertNull(singleEventCommand.getRepeatUntil());
  }

  @Test
  public void testRecurringEventInitialization() {
    assertEquals("Morning Run", recurringEventCommand.getEventName());
    assertEquals(LocalDateTime.of(2025, 7, 1, 9, 0), recurringEventCommand.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 7, 1, 10, 0), recurringEventCommand.getEndDateTime());
    assertEquals("Daily run", recurringEventCommand.getDescription());
    assertEquals("Park", recurringEventCommand.getLocation());
    assertTrue(recurringEventCommand.isPublic());
    assertFalse(recurringEventCommand.isAllDay());
    assertTrue(recurringEventCommand.isRecurring());
    assertEquals("MTWRF", recurringEventCommand.getWeekdays());
    assertEquals(10, recurringEventCommand.getRepeatCount());
    assertEquals(LocalDateTime.of(2025, 7, 15, 9, 0), recurringEventCommand.getRepeatUntil());
  }

  @Test
  public void testAutoDeclineFlag() {
    CreateEventCommand command = new CreateEventCommand(
            "Team Meeting", LocalDateTime.of(2025, 8, 1, 10, 0),
            LocalDateTime.of(2025, 8, 1, 11, 0), true,
            "Sprint review", "Office", false, false,
            false, "", 0, null
    );
    assertTrue(command.isAutoDecline());
  }

  @Test
  public void testCreateEventCommand() {
    CalendarModel model = new CalendarModel();
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 10, 0);

    CreateEventCommand cmd = new CreateEventCommand(
            "Team Meeting", start, end, false, "Sync up", "Room A", true, false, false, "", 0, null
    );

    assertEquals("Team Meeting", cmd.getEventName());
    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
    assertEquals("Sync up", cmd.getDescription());
    assertEquals("Room A", cmd.getLocation());
    assertTrue(cmd.isPublic());
  }

}

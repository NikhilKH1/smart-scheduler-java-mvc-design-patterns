
import org.junit.Test;

import java.time.ZoneId;

import calendarapp.controller.commands.CreateCalendarCommand;

import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for the CreateCalendarCommand class.
 */
public class CreateCalendarCommandTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NullCalendarName() {
    new CreateCalendarCommand(null, ZoneId.of("UTC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_EmptyCalendarName() {
    new CreateCalendarCommand("   ", ZoneId.of("UTC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NullTimezone() {
    new CreateCalendarCommand("MyCal",
            null);
  }

  @Test
  public void testGetters_ValidInputs() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("WorkCal",
            ZoneId.of("America/New_York"));
    assertEquals("WorkCal", cmd.getCalendarName());
    assertEquals(ZoneId.of("America/New_York"), cmd.getTimezone());
  }
}



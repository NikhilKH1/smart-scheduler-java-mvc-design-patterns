

import org.junit.Test;

import java.time.LocalDateTime;

import calendarapp.model.commands.BusyQueryCommand;

import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for the BusyQueryCommand class.
 */

public class BusyQueryCommandTest {

  @Test
  public void testBusyQuery() {
    LocalDateTime queryTime = LocalDateTime.of(2025, 6, 1, 9, 0);
    BusyQueryCommand cmd = new BusyQueryCommand(queryTime);
    assertEquals(queryTime, cmd.getQueryTime());
  }
}

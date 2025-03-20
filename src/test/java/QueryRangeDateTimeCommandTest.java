

import org.junit.Test;

import java.time.LocalDateTime;

import calendarapp.controller.commands.QueryRangeDateTimeCommand;

import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for the QueryRangeDateTimeCommand class.
 */
public class QueryRangeDateTimeCommandTest {

  @Test
  public void testQueryRange() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 1, 11, 0);
    QueryRangeDateTimeCommand cmd = new QueryRangeDateTimeCommand(start, end);

    assertEquals(start, cmd.getStartDateTime());
    assertEquals(end, cmd.getEndDateTime());
  }
}

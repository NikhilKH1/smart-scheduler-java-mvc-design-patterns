

import org.junit.Test;

import java.time.LocalDate;

import calendarapp.model.commands.QueryByDateCommand;

import static org.junit.Assert.assertEquals;


/**
 * JUnit tests for the QueryByDateCommand class.
 */
public class QueryByDateCommandTest {

  @Test
  public void testQueryByDate() {
    LocalDate date = LocalDate.of(2025, 6, 1);
    QueryByDateCommand cmd = new QueryByDateCommand(date);

    assertEquals(date, cmd.getQueryDate());
  }
}

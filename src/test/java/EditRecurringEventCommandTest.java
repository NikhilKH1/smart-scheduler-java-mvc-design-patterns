

import org.junit.Test;

import calendarapp.model.commands.EditRecurringEventCommand;

import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for the EditRecurringEventCommand class.
 */
public class EditRecurringEventCommandTest {

  @Test
  public void testEditRecurringEventCommandProperties() {
    EditRecurringEventCommand cmd = new EditRecurringEventCommand("repeatuntil", "Daily Standup", "2025-12-31T00:00");

    assertEquals("repeatuntil", cmd.getProperty());
    assertEquals("Daily Standup", cmd.getEventName());
    assertEquals("2025-12-31T00:00", cmd.getNewValue());
  }
}

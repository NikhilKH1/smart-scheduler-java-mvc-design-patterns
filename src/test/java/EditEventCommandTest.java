import org.junit.Test;

import calendarapp.controller.commands.EditEventCommand;

import static org.junit.Assert.assertEquals;


/**
 * JUnit tests for the EditEventCommand class.
 */
public class EditEventCommandTest {

  @Test
  public void testEditEventCommandProperties() {
    EditEventCommand cmd = new EditEventCommand("location", "Team Meeting",
            "New Room");

    assertEquals("location", cmd.getProperty());
    assertEquals("Team Meeting", cmd.getEventName());
    assertEquals("New Room", cmd.getNewValue());
  }
}

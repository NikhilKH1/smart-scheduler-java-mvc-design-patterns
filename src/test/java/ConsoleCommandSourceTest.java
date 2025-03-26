import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;

import calendarapp.io.ConsoleCommandSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit tests for the ConsoleCommandSource class.
 */
public class ConsoleCommandSourceTest {

  private final InputStream originalIn = System.in;
  private ConsoleCommandSource commandSource;

  @Before
  public void setUp() {
    String input = "command1\ncommand2\n";
    ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
    System.setIn(testIn);
    commandSource = new ConsoleCommandSource();
  }

  @After
  public void tearDown() {
    System.setIn(originalIn);
    commandSource.close();
  }

  @Test
  public void testGetNextCommandReadsCommandsInOrder() {
    String first = commandSource.getNextCommand();
    assertEquals("command1", first);
    String second = commandSource.getNextCommand();
    assertEquals("command2", second);
  }

  @Test(expected = NoSuchElementException.class)
  public void testGetNextCommandWhenNoMoreInput() {
    commandSource.getNextCommand();
    commandSource.getNextCommand();
    commandSource.getNextCommand();
  }

  @Test
  public void testClosePreventsFurtherInput() {
    commandSource.close();
    try {
      commandSource.getNextCommand();
      fail("Expected exception when reading after close");
    } catch (IllegalStateException | NoSuchElementException e) {
    }
  }
}
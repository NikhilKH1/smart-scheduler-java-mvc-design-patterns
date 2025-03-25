import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;

import calendarapp.io.ConsoleCommandSource;

import static org.junit.Assert.*;

public class ConsoleCommandSourceTest {

  private final InputStream originalIn = System.in;
  private ConsoleCommandSource commandSource;

  @Before
  public void setUp() {
    // Prepare an input stream with two commands separated by newline.
    String input = "command1\ncommand2\n";
    ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
    System.setIn(testIn);
    commandSource = new ConsoleCommandSource();
  }

  @After
  public void tearDown() {
    // Restore original System.in
    System.setIn(originalIn);
    // Ensure the scanner is closed if not already
    commandSource.close();
  }

  @Test
  public void testGetNextCommandReadsCommandsInOrder() {
    // First call should return "command1"
    String first = commandSource.getNextCommand();
    assertEquals("command1", first);

    // Second call should return "command2"
    String second = commandSource.getNextCommand();
    assertEquals("command2", second);
  }

  @Test(expected = NoSuchElementException.class)
  public void testGetNextCommandWhenNoMoreInput() {
    // Read all lines from the input stream.
    commandSource.getNextCommand(); // command1
    commandSource.getNextCommand(); // command2
    // Third call should throw NoSuchElementException because there's no more input.
    commandSource.getNextCommand();
  }

  @Test
  public void testClosePreventsFurtherInput() {
    // Close the command source.
    commandSource.close();
    try {
      commandSource.getNextCommand();
      fail("Expected exception when reading after close");
    } catch (IllegalStateException | NoSuchElementException e) {
      // Expected: after close, scanner.nextLine() will throw an exception.
    }
  }
}
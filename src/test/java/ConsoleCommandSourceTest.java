import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import calendarapp.io.ConsoleCommandSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit tests for the ConsoleCommandSource class.
 */
public class ConsoleCommandSourceTest {

  private final InputStream originalIn = System.in;
  private final PrintStream originalOut = System.out;
  private ConsoleCommandSource commandSource;

  @Before
  public void setUp() {
    // Setup System.in with sample input
    String input = "command1\ncommand2\n";
    ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
    System.setIn(testIn);
    commandSource = new ConsoleCommandSource();
  }

  @After
  public void tearDown() {
    System.setIn(originalIn);
    System.setOut(originalOut);
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
      // Expected exception caught.
    }
  }

  @Test
  public void testPromptPrinted() {
    // Redirect System.out to capture the prompt
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    // Prepare input for a single command
    String input = "dummyCommand\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));
    ConsoleCommandSource localCommandSource = new ConsoleCommandSource();

    // Invoke getNextCommand which should print the prompt
    localCommandSource.getNextCommand();

    // Assert that the output contains the prompt "> "
    String printedOutput = outContent.toString();
    assertTrue("Prompt not printed as expected", printedOutput.contains("> "));

    localCommandSource.close();
  }
}

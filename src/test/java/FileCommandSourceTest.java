import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import calendarapp.io.FileCommandSource;
import calendarapp.io.ICommandSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * JUnit tests for the FileCommandSource class.
 */
public class FileCommandSourceTest {

  private File tempFile;
  private ICommandSource commandSource;

  @Before
  public void setUp() throws IOException {
    tempFile = File.createTempFile("testFileCommandSource", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("line1\n");
      writer.write("line2\n");
    }
    commandSource = new FileCommandSource(tempFile.getAbsolutePath());
  }

  @After
  public void tearDown() {
    if (commandSource != null) {
      commandSource.close();
    }
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  @Test
  public void testGetNextCommand() {
    String first = commandSource.getNextCommand();
    assertEquals("line1", first);

    String second = commandSource.getNextCommand();
    assertEquals("line2", second);

    String third = commandSource.getNextCommand();
    assertNull(third);
  }

  @Test
  public void testClosePreventsFurtherInput() {
    commandSource.close();

    String result = commandSource.getNextCommand();
    assertNull(result);
  }

  @Test(expected = IOException.class)
  public void testConstructorWithNonexistentFile() throws IOException {
    new FileCommandSource("nonexistent_file_12345.txt");
  }
}

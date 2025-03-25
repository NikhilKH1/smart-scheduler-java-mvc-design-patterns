import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.NoSuchElementException;

import calendarapp.io.FileCommandSource;
import calendarapp.io.ICommandSource;

import static org.junit.Assert.*;

public class FileCommandSourceTest {

  private File tempFile;
  private ICommandSource commandSource;

  @Before
  public void setUp() throws IOException {
    // Create a temporary file with two lines.
    tempFile = File.createTempFile("testFileCommandSource", ".txt");
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("line1\n");
      writer.write("line2\n");
    }
    // Initialize FileCommandSource with the temporary file.
    commandSource = new FileCommandSource(tempFile.getAbsolutePath());
  }

  @After
  public void tearDown() {
    // Close the command source and delete the temporary file.
    if (commandSource != null) {
      commandSource.close();
    }
    if (tempFile != null && tempFile.exists()) {
      tempFile.delete();
    }
  }

  @Test
  public void testGetNextCommand() {
    // Read first line and assert equality.
    String first = commandSource.getNextCommand();
    assertEquals("line1", first);

    // Read second line.
    String second = commandSource.getNextCommand();
    assertEquals("line2", second);

    // When no more lines, it should return null.
    String third = commandSource.getNextCommand();
    assertNull(third);
  }

  @Test
  public void testClosePreventsFurtherInput() {
    // Close the command source.
    commandSource.close();

    // After closing, attempting to read further should either return null or throw an exception.
    // Our implementation catches IOException and returns null.
    String result = commandSource.getNextCommand();
    assertNull(result);
  }

  @Test(expected = IOException.class)
  public void testConstructorWithNonexistentFile() throws IOException {
    // Provide a file path that does not exist.
    new FileCommandSource("nonexistent_file_12345.txt");
  }
}

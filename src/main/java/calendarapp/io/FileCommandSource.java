package calendarapp.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class that reads commands from a file.
 * Implements the ICommandSource interface to retrieve commands from a file.
 */
public class FileCommandSource implements ICommandSource {
  private final BufferedReader reader;

  /**
   * Constructs a FileCommandSource object that initializes the reader to read
   * from the specified file.
   *
   * @param filePath the path to the file containing the commands
   * @throws IOException if an I/O error occurs while opening the file
   */
  public FileCommandSource(String filePath) throws IOException {
    this.reader = new BufferedReader(new FileReader(filePath));
  }

  /**
   * Retrieves the next command from the file.
   * If there are no more lines to read, it returns null.
   *
   * @return the next command as a String, or null if the end of the file is reached
   */
  @Override
  public String getNextCommand() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Closes the BufferedReader to release system resources.
   */
  @Override
  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      // No action needed if an error occurs while closing
    }
  }
}

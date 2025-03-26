package calendarapp.io;

/**
 * Interface that defines the source from which commands are retrieved.
 * Implementing classes should provide mechanisms for fetching commands
 * either from a console or a file.
 */
public interface ICommandSource {

  /**
   * Retrieves the next command as a string.
   * This method will return null if there are no more commands to read.
   *
   * @return the next command as a String, or null if no commands are left
   */
  String getNextCommand();

  /**
   * Closes the command source to release any system resources if needed.
   */
  void close();
}

package calendarapp.io;

public interface ICommandSource {
  /**
   * Retrieves the next command as a string.
   * Returns null if there are no more commands.
   */
  String getNextCommand();

  /**
   * Closes the command source (if needed).
   */
  void close();
}
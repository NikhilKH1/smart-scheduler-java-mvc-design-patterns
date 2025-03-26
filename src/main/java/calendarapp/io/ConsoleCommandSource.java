package calendarapp.io;

import java.util.Scanner;

/**
 * Class that provides commands from the console input.
 * Implements the ICommandSource interface to read commands entered by the user.
 */
public class ConsoleCommandSource implements ICommandSource {
  private final Scanner scanner;

  /**
   * Constructs a ConsoleCommandSource object that initializes the scanner to read from System.in.
   */
  public ConsoleCommandSource() {
    this.scanner = new Scanner(System.in);
  }

  /**
   * Retrieves the next command input by the user.
   *
   * @return the command entered by the user as a String
   */
  @Override
  public String getNextCommand() {
    System.out.print("> ");
    return scanner.nextLine();
  }

  /**
   * Closes the scanner to release system resources.
   */
  @Override
  public void close() {
    scanner.close();
  }
}

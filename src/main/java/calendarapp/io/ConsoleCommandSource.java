package calendarapp.io;

import java.util.Scanner;

public class ConsoleCommandSource implements ICommandSource {
  private final Scanner scanner;

  public ConsoleCommandSource() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public String getNextCommand() {
    System.out.print("> ");
    return scanner.nextLine();
  }

  @Override
  public void close() {
    scanner.close();
  }
}
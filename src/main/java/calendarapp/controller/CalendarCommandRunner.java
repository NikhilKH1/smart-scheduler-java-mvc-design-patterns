package calendarapp.controller;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

import calendarapp.factory.ICommandFactory;

/**
 * A controller helper that reads commands from a Readable and writes output to an Appendable.
 * This class decouples CLI/headless mode from the GUI or controller internals.
 */
public class CalendarCommandRunner {
  private final Readable in;
  private final Appendable out;
  private final ICalendarController controller;
  private final ICommandFactory commandFactory;

  /**
   * Basic constructor for CLI or headless mode, without command factory.
   */
  public CalendarCommandRunner(Readable in, Appendable out, ICalendarController controller) {
    this(in, out, controller, null);
  }

  /**
   * Optional constructor for GUI or factory-aware usage.
   */
  public CalendarCommandRunner(Readable in, Appendable out,
                               ICalendarController controller,
                               ICommandFactory commandFactory) {
    this.in = Objects.requireNonNull(in, "Input cannot be null");
    this.out = Objects.requireNonNull(out, "Output cannot be null");
    this.controller = Objects.requireNonNull(controller, "Controller cannot be null");
    this.commandFactory = commandFactory; // optional
  }

  /**
   * Starts processing commands from the input stream until "exit" or EOF.
   */
  public void run() throws IOException {
    Scanner scanner = new Scanner(in);
    out.append("Enter commands (type 'exit' to quit):\n");

    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.isEmpty()) continue;

      if (command.equalsIgnoreCase("exit")) {
        out.append("Exiting...\n");
        break;
      }

      try {
        boolean result = controller.processCommand(command);
        if (!result) {
          out.append("Command failed: ").append(command).append("\n");
        }
      } catch (Exception e) {
        out.append("Error while processing command: ").append(e.getMessage()).append("\n");
      }
    }
  }

  /**
   * Expose the factory if available (used in GUI contexts).
   */
  public ICommandFactory getFactory() {
    return commandFactory;
  }
}

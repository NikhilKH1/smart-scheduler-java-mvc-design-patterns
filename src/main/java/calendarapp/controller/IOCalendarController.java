package calendarapp.controller;


import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * A controller that reads commands from a Readable and writes output to an Appendable.
 * This makes the controller testable and decouples it from specific I/O like System.in/out.
 */
public class IOCalendarController {
  private final Readable in;
  private final Appendable out;
  private final ICalendarController controller;

  public IOCalendarController(Readable in, Appendable out, ICalendarController controller) {
    this.in = Objects.requireNonNull(in);
    this.out = Objects.requireNonNull(out);
    this.controller = Objects.requireNonNull(controller);
  }

  /**
   * Starts processing commands from the input stream until "exit" or input ends.
   */
  public void run() throws IOException {
    Scanner scanner = new Scanner(in);
    out.append("Enter commands (type 'exit' to quit):\n");
    while (scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();
      if (command.isEmpty()) {
        continue;
      }
      if (command.equalsIgnoreCase("exit")) {
        break;
      }
      boolean result = controller.processCommand(command);
      if (!result) {
        out.append("Command failed: ").append(command).append("\n");
      }
    }
  }

}

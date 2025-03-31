
package calendarapp;

import calendarapp.controller.ICalendarController;
import calendarapp.controller.IOCalendarController;

import java.io.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class CalendarAppTest {

  private static class DummyController implements ICalendarController {
    private final StringBuilder log = new StringBuilder();
    private final boolean shouldSucceed;

    public DummyController(boolean shouldSucceed) {
      this.shouldSucceed = shouldSucceed;
    }

    @Override
    public boolean processCommand(String commandInput) {
      log.append("Command: ").append(commandInput).append("\n");
      return shouldSucceed;
    }

    public String getLog() {
      return log.toString();
    }
  }

  @Test
  public void testInteractiveModeRunsSuccessfully() throws Exception {
    String inputCommands = "create calendar --name TestCal --timezone UTC\nexit\n";
    Reader input = new StringReader(inputCommands);
    StringWriter output = new StringWriter();

    DummyController dummy = new DummyController(true);
    IOCalendarController controller = new IOCalendarController(input, output, dummy);
    controller.run();

    assertTrue(output.toString().contains("Enter commands"));
    assertTrue(dummy.getLog().contains("create calendar"));
  }

  @Test
  public void testCommandFailurePrinted() throws Exception {
    String inputCommands = "bad command\nexit\n";
    Reader input = new StringReader(inputCommands);
    StringWriter output = new StringWriter();

    DummyController dummy = new DummyController(false);
    IOCalendarController controller = new IOCalendarController(input, output, dummy);
    controller.run();

    assertTrue(output.toString().contains("Command failed: bad command"));
  }

  @Test
  public void testEmptyCommandIsSkipped() throws Exception {
    String inputCommands = "\n\nexit\n";
    Reader input = new StringReader(inputCommands);
    StringWriter output = new StringWriter();

    DummyController dummy = new DummyController(true);
    IOCalendarController controller = new IOCalendarController(input, output, dummy);
    controller.run();

    assertTrue(output.toString().startsWith("Enter commands"));
    assertFalse(dummy.getLog().contains("Command:"));
  }

  @Test
  public void testOnlyExitCommand() throws Exception {
    String inputCommands = "exit\n";
    Reader input = new StringReader(inputCommands);
    StringWriter output = new StringWriter();

    DummyController dummy = new DummyController(true);
    IOCalendarController controller = new IOCalendarController(input, output, dummy);
    controller.run();

    assertTrue(output.toString().contains("Enter commands"));
    assertEquals("", dummy.getLog());
  }

  @Test
  public void testMultipleMixedCommands() throws Exception {
    String inputCommands = "  \ncreate event \"Meeting\" from 2025-06-01T10:00 to "
            + "2025-06-01T11:00\nbad one\nexit\n";

    Reader input = new StringReader(inputCommands);
    StringWriter output = new StringWriter();

    DummyController dummy = new DummyController(false); // simulate failure
    IOCalendarController controller = new IOCalendarController(input, output, dummy);
    controller.run();

    String out = output.toString();
    assertTrue(out.contains("Command failed: create event"));
    assertTrue(out.contains("Command failed: bad one"));
  }

  @Test
  public void testRunNoArgsDefaultsToInteractive() throws IOException {
    // Simulate user entering a single command and then 'exit'
    String simulatedInput = "create calendar --name TestCal --timezone UTC\nexit\n";
    InputStream testIn = new ByteArrayInputStream(simulatedInput.getBytes());
    ByteArrayOutputStream testOut = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    InputStream originalIn = System.in;

    System.setIn(testIn);
    System.setOut(new PrintStream(testOut));

    try {
      String[] args = {};
      CalendarApp.run(args);
      String output = testOut.toString();
      assertTrue(output.contains("Enter commands"));
    } finally {
      System.setIn(originalIn); // Restore original input/output
      System.setOut(originalOut);
    }
  }


  @Test
  public void testRunInteractiveModeExplicit() throws IOException {
    // Simulate user entering a command followed by 'exit'
    String simulatedInput = "create calendar --name TestCal --timezone UTC\nexit\n";
    InputStream testIn = new ByteArrayInputStream(simulatedInput.getBytes());
    ByteArrayOutputStream testOut = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    InputStream originalIn = System.in;

    System.setIn(testIn);
    System.setOut(new PrintStream(testOut));

    try {
      String[] args = {"--mode", "interactive"};
      CalendarApp.run(args);
      String output = testOut.toString();
      assertTrue(output.contains("Enter commands"));
    } finally {
      System.setIn(originalIn);
      System.setOut(originalOut);
    }
  }


  @Test
  public void testRunHeadlessModeWithFile() throws IOException {
    File tempFile = File.createTempFile("calendar-test", ".txt");
    try (PrintWriter pw = new PrintWriter(tempFile)) {
      pw.println("create calendar --name AutoCal --timezone UTC");
      pw.println("exit");
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};
    CalendarApp.run(args);
    assertTrue(tempFile.exists()); // file was read successfully
    tempFile.delete();
  }

  @Test
  public void testRunInvalidArgumentsPrintsUsage() throws IOException {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    forbidSystemExitCall();
    try {
      String[] args = {"--mode", "unknown"};
      CalendarApp.run(args);
      fail("Expected System.exit to be called");
    } catch (ExitTrappedException e) {
      assertEquals(1, e.status); // Expected exit code
      assertTrue(err.toString().toLowerCase().contains("usage"));
    } finally {
      enableSystemExit();
      System.setErr(originalErr);
    }
  }



  @Test
  public void testRunIOExceptionTriggersErrorMessage() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    try {
      String[] args = {"--mode", "headless", "nonexistent_file.txt"};
      try {
        CalendarApp.run(args);
        fail("Expected IOException to be thrown");
      } catch (IOException e) {
        assertTrue(e.getMessage().contains("nonexistent_file.txt"));
      }
    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  public void testMainCatchesIOExceptionAndExits() {
    // Backup original System.err and SecurityManager
    PrintStream originalErr = System.err;
    SecurityManager originalSecurityManager = System.getSecurityManager();

    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));

    // Custom SecurityManager to intercept System.exit
    class ExitInterceptingSecurityManager extends SecurityManager {
      @Override
      public void checkPermission(java.security.Permission perm) {
        // allow everything
      }

      @Override
      public void checkExit(int status) {
        throw new SecurityException("Intercepted System.exit(" + status + ")");
      }
    }

    System.setSecurityManager(new ExitInterceptingSecurityManager());

    try {
      // Simulate args that will cause IOException (e.g., headless mode with non-existent file)
      String[] args = {"--mode", "headless", "nonexistent_file.txt"};
      CalendarApp.main(args);
      fail("Expected SecurityException due to System.exit");
    } catch (SecurityException se) {
      assertTrue(se.getMessage().contains("System.exit(1)")); // exit was called
      assertTrue(errContent.toString().toLowerCase().contains("i/o error")); // error message was printed
    } finally {
      // Restore
      System.setErr(originalErr);
      System.setSecurityManager(originalSecurityManager);
    }
  }

  @Test
  public void testMainCallsRunMethod() {
    // Redirect System.in with "exit" so the controller terminates immediately
    ByteArrayInputStream testIn = new ByteArrayInputStream("exit\n".getBytes());
    InputStream originalIn = System.in;
    PrintStream originalOut = System.out;

    // Capture System.out
    ByteArrayOutputStream testOut = new ByteArrayOutputStream();
    System.setIn(testIn);
    System.setOut(new PrintStream(testOut));

    try {
      // Run the main method with no arguments (defaults to interactive mode)
      CalendarApp.main(new String[]{});

      // Look for a known side effect from IOCalendarController.run()
      String output = testOut.toString();
      assertTrue(output.contains("Enter commands (type 'exit' to quit):"));
    } finally {
      // Reset the streams to avoid interfering with other tests
      System.setIn(originalIn);
      System.setOut(originalOut);
    }
  }





  static class ExitTrappedException extends SecurityException {
    public final int status;
    public ExitTrappedException(int status) {
      this.status = status;
    }
  }

  private static void forbidSystemExitCall() {
    final SecurityManager securityManager = new SecurityManager() {
      @Override
      public void checkPermission(java.security.Permission perm) {
        // Allow everything else
      }

      @Override
      public void checkExit(int status) {
        super.checkExit(status);
        throw new ExitTrappedException(status);
      }
    };
    System.setSecurityManager(securityManager);
  }

  private static void enableSystemExit() {
    System.setSecurityManager(null);
  }

}

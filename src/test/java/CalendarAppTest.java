import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import calendarapp.CalendarApp;

public class CalendarAppTest {

  @Test
  public void testMainMethod_InvokesControllerRun() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    String[] args = {"dummy"};
    CalendarApp.main(args);

    System.setOut(originalOut);
    String output = outContent.toString();
    assertTrue(output.contains("Invalid arguments. Use:"));
  }
}

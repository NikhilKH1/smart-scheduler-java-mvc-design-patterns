
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import calendarapp.CalendarApp;

/**
 * Calendar App JUnit Test.
 */
public class CalendarAppTest {


  @Test
  public void testMainRunsHeadlessModeWithFakeFile() {
    try {
      CalendarApp.main(new String[]{"--mode", "headless", "nonexistent-script.txt"});
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("nonexistent"));
    }
  }

  @Test
  public void testMainInvalidArgumentsHandledGracefully() {
    try {
      CalendarApp.main(new String[]{"--invalidmode"});
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("nonexistent"));
    }
  }
}



import calendarapp.model.ConflictChecker;
import calendarapp.model.event.SingleEvent;
import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class ConflictCheckerTest {

  @Test
  public void testEventsConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 30);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "", true, false, null);

    assertTrue("These events should conflict", ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "", true, false, null);

    assertFalse("These events should not conflict", ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testAdjacentEventsNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 11, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "", true, false, null);

    assertFalse("Adjacent events should NOT conflict", ConflictChecker.hasConflict(event1, event2));
  }

  @Test
  public void testSeparateEventsNoConflict() {
    LocalDateTime start1 = LocalDateTime.of(2025, 6, 1, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 6, 1, 10, 0);
    LocalDateTime start2 = LocalDateTime.of(2025, 6, 1, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, 6, 1, 12, 0);

    SingleEvent event1 = new SingleEvent("Event 1", start1, end1, "", "", true, false, null);
    SingleEvent event2 = new SingleEvent("Event 2", start2, end2, "", "", true, false, null);

    assertFalse("Non-overlapping events should NOT conflict", ConflictChecker.hasConflict(event1, event2));
  }
}

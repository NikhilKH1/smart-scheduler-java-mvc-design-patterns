import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import calendarapp.model.RecurringEvent;
import calendarapp.model.SingleEvent;

import static org.junit.Assert.*;

public class RecurringEventTest {

  private RecurringEvent recurringEvent;

  @Before
  public void setUp() {
    recurringEvent = new RecurringEvent(
            "Algorithms Class",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "MWF",
            5,
            null
    );
  }

  @Test
  public void testGenerateOccurrencesForFixedCount() {
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Expected exactly 5 occurrences", 5, occurrences.size());

    assertEquals(LocalDateTime.of(2025, 3, 10, 10, 0), occurrences.get(0).getStartDateTime()); // Monday
    assertEquals(LocalDateTime.of(2025, 3, 12, 10, 0), occurrences.get(1).getStartDateTime()); // Wednesday
    assertEquals(LocalDateTime.of(2025, 3, 14, 10, 0), occurrences.get(2).getStartDateTime()); // Friday
    assertEquals(LocalDateTime.of(2025, 3, 17, 10, 0), occurrences.get(3).getStartDateTime()); // Next Monday
    assertEquals(LocalDateTime.of(2025, 3, 19, 10, 0), occurrences.get(4).getStartDateTime()); // Next Wednesday
  }

  @Test
  public void testGenerateOccurrencesUntilDate() {
    recurringEvent = new RecurringEvent(
            "Data Structures Class",
            LocalDateTime.of(2025, 4, 1, 9, 0),
            LocalDateTime.of(2025, 4, 1, 10, 0),
            "TR",
            0,
            LocalDateTime.of(2025, 4, 10, 0, 0)
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Expected exactly 4 occurrences", 4, occurrences.size());

    assertEquals(LocalDateTime.of(2025, 4, 1, 9, 0), occurrences.get(0).getStartDateTime()); // Tuesday
    assertEquals(LocalDateTime.of(2025, 4, 3, 9, 0), occurrences.get(1).getStartDateTime()); // Thursday
    assertEquals(LocalDateTime.of(2025, 4, 8, 9, 0), occurrences.get(2).getStartDateTime()); // Next Tuesday
    assertEquals(LocalDateTime.of(2025, 4, 10, 9, 0), occurrences.get(3).getStartDateTime()); // Next Thursday
  }

//  @Test
//  public void testNoOccurrencesGenerated() {
//    recurringEvent = new RecurringEvent(
//            "Empty Class",
//            LocalDateTime.of(2025, 5, 1, 9, 0),
//            LocalDateTime.of(2025, 5, 1, 10, 0),
//            "",
//            5,
//            null
//    );
//
//    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
//    assertTrue("Expected no occurrences to be generated", occurrences.isEmpty());
//  }

  @Test
  public void testOccurrencesRespectWeekdays() {
    recurringEvent = new RecurringEvent(
            "Weekly Review",
            LocalDateTime.of(2025, 6, 2, 14, 0),
            LocalDateTime.of(2025, 6, 2, 15, 0),
            "F",
            2,
            null
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Expected exactly 2 occurrences", 2, occurrences.size());

    assertEquals(LocalDateTime.of(2025, 6, 6, 14, 0), occurrences.get(0).getStartDateTime()); // First Friday
    assertEquals(LocalDateTime.of(2025, 6, 13, 14, 0), occurrences.get(1).getStartDateTime()); // Next Friday
  }
}
